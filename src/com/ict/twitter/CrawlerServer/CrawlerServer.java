package com.ict.twitter.CrawlerServer;

import com.ict.twitter.CrawlerMessage.MessageBusComponent;
import com.ict.twitter.CrawlerNode.ControlReceiver;
import com.ict.twitter.CrawlerNode.ControlSender;
import com.ict.twitter.CrawlerNode.NodeReport;
import com.ict.twitter.CrawlerNode.NodeStep;
import com.ict.twitter.CrawlerSchedul.CrawlServerScheduler;
import com.ict.twitter.CrawlerSchedul.KeyUserScheduler;
import com.ict.twitter.CrawlerSchedul.KeyWordsScheduler;
import com.ict.twitter.CrawlerSchedul.PlantScheduler;
import com.ict.twitter.CrawlerSchedul.ReportTotalScheduler;
import com.ict.twitter.MessageBus.GetAceiveMqConnection;
import com.ict.twitter.MessageBus.MessageBusNames;
import com.ict.twitter.MessageBus.MessageBussConnector;
import com.ict.twitter.MessageBus.Receiver;
import com.ict.twitter.MessageBus.Sender;
import com.ict.twitter.MessageBusTest.ControlClient;
import com.ict.twitter.MessageBusTest.MessageBusCleanner;
import com.ict.twitter.Report.CrawlerServerReporter;
import com.ict.twitter.Report.NodeReporterReceiver;
import com.ict.twitter.Report.ReportData;
import com.ict.twitter.StatusTrack.MyTracker;
import com.ict.twitter.netty.server.CrawlServerTaskHandler;
import com.ict.twitter.netty.server.DiscardServer;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.plantform.PlatFormMain;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.MainType;
import com.ict.twitter.task.beans.Task.TaskType;
import com.ict.twitter.tools.BasePath;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
public class CrawlerServer extends MessageBusComponent implements Runnable,MessageBusNames{

	/**
	 * ����Ҫ�ģ���
	 */
	public static final String Name="TwitterWEB";
	public enum ServerStep{
		init,searchStart,searchEnd,keyuserCaijiStart,keyuserCaijiEnd,normalCaijiStart,normalCaijiEnd
	}
	protected static enum OP {START, STOP, DUMP, RESTART, CUSTORM, REFRESH, SAMPLE};
	public com.ict.twitter.CrawlerServer.CrawlerServer.ServerStep currentstep=ServerStep.init;
	public int Normal_User_Deepth = 20;
	
	public static long maxTime = 1417363200;
	public static final long HALF_DAY=1000*60*60*12;
	//�������
	Receiver controlReceiver;	
	ControlSender controlSender;
	//Task���
	Sender taskSender;
	Sender urgentTaskSender;
	Sender keyWordAndTopicTaskSender;
	Sender keyUserTaskSender;
	//NormalUser ���
	Receiver NormalReceiver;
	Receiver KeyUserReceiver;
	Receiver nodeReporterReceiver;
	
	/***************************************/
	public HashMap<String,ReportData> NodeReportData;
	public Object reportlock=new Object();
	public ReportData ServerReportData;//��������Ҫ�㱨�������ۺϣ����Ը����ɼ��ڵ������
	public CrawlerServerReporter crawlReporter;
	/***************************************/
	
	/***************************************/
	CrawlServerScheduler schedule;
	
	/****************************************/
	MessageBusManagement msgbusmag;
	/****************************************/
	public static long count;
	public static long TaskID;
	
	public boolean isFirstChuiZhi=true;
	public boolean isFirstBingXing=true;

	public ServerBean sb=new ServerBean();
	
	public boolean isResume=false;
	
	
	//�����ӽڵ������Ϣ
	public NodeManager nodeManager;
	private ServerControlReceiverListener scr;
	/***************************/
	protected String args[];
	protected OP op=null;
	protected int deepth=10;
	protected int keySearchCount=100;
	protected String CustomTaskFolderDir="";
	/***************************/
	
	/***************************/
	private MyTracker tracker;
	/***************************/
	CrawlServerTaskHandler csHandler;
	DiscardServer ds;
	/******************************/
	public CrawlerServer(){
		//this("-Command Start -Deepth 10 -KeySearchCount 10".split(" "));	
		this("-Command Start -FolderDir D:\\ServerTask -Deepth 10 -KeySearchCount 10".split(" "));	
		
	}
	public CrawlerServer(String[] args){
		LogSys.crawlerServLogger.info("Crawlserver��ʼ��");
		if (args.length < 1) {
			System.err.println("Usage: CrawlerServer -Command [Start|Stop|Dump|Restart] -Deepth 10 -KeySearchCount 10");
		    return;
	    }
		this.args=args;
	}
	public static void main(String[] args){
		long currentTime = System.currentTimeMillis()/1000;
		if(currentTime < maxTime) {
			for(int i=0;i<args.length;i++){
				System.out.print(args[i]+" ");
			}
			CrawlerServer crawler =  new CrawlerServer(args);
			Thread mthread= new Thread(crawler);
			mthread.setName("CrawlServer");
			mthread.start();
		}
	}
	@Override
	public void run() {
		checkArgs();
		if(op==OP.START){
			Initiallize();
			StartCrawlServer();
		}else if(op==OP.CUSTORM){
			LogSys.crawlerServLogger.info("���ڽ����û��Զ���ɼ�");
			Initiallize();
			StartCustormServer();
		}else if(op==OP.REFRESH){
			Initiallize();
			StartRefreshServer();
		}
		else if(op==OP.STOP){
			System.err.println("�˴���Ҫ�޸�");
			System.exit(-1);
			//StopCrawlServer();
		}else if(op==OP.SAMPLE){
			LogSys.crawlerServLogger.info("���ڽ�����������ɼ�");
			Initiallize();
			this.StartSampleServer();			
		}
		
	}
	public void checkArgs(){
		System.out.println("���������ڼ�����ò���");
		for(int i=0;i<args.length;i++){
			if(args[i].equals("-Command")){
				String command=args[++i];
				if(command.equals("Start"))
					op=OP.START;
				else if(command.equals("Stop")){
					op=OP.STOP;
				}else if(command.equals("Dump")){
					op=OP.DUMP;
				}else if(command.equals("Restart")){
					op=OP.RESTART;
				}else if(command.equals("Custorm")){
					op=OP.CUSTORM;
					if(args[++i].equalsIgnoreCase("-FolderDir")){
						this.CustomTaskFolderDir=args[++i];
						System.out.println("ָ���������ļ����ǣ�"+CustomTaskFolderDir);
					}else{
						System.err.println("û��ָ��FoldDir");
						System.exit(-1);
					}
				}else if(command.equals("Refresh")){
					op=OP.REFRESH;
				}else if(command.equalsIgnoreCase("Sample")){
					op=OP.SAMPLE;
					StartSampleServer();
				}
			}else if(args[i].equals("-Deepth")){
				deepth=Integer.parseInt(args[++i]);
			}else if(args[i].equals("-KeySearchCount")){
				keySearchCount=Integer.parseInt(args[++i]);
			}
		}
		this.Normal_User_Deepth=deepth;
	}

	public int run(String[] args) throws Exception {
				
		return 0;
	}
	
	



	public void Initiallize(){

		javax.jms.Connection connection=GetAceiveMqConnection.StaticGetConnection();
		LogSys.crawlerServLogger.info("--------------Server��ʼ��-------------------");		
		basepath=BasePath.getBase();		
		NodeReportData=new HashMap<String,ReportData>();
		ServerReportData=new ReportData();
		nodeManager=new NodeManager();
		scr=new ServerControlReceiverListener(this);
		urgentTaskSender=new Sender(connection,MessageBusNames.UrgentTask+"?consumer.prefetchSize=0",false);
		keyUserTaskSender=new Sender(connection,MessageBusNames.KeyUserTask+"?consumer.prefetchSize=0",false);
		keyWordAndTopicTaskSender=new Sender(connection,MessageBusNames.KeyWordAndTopicTask+"?consumer.prefetchSize=0",false);
		taskSender=new Sender(connection,MessageBusNames.Task+"?consumer.prefetchSize=0",false);	
		
		//taskReceiver=new Receiver(connection, false, MessageBusNames.Task, this, true, null);
		controlReceiver=new ControlReceiver(connection,MessageBusNames.ControlC2S,this,false,scr);
   		controlSender=new ControlSender(connection,MessageBusNames.ControlS2C,true);
		NormalReceiver=new NormalUserReceiver(connection,MessageBusNames.NormalID,this,false);
		KeyUserReceiver=new KeyUserReceiver(connection,MessageBusNames.KeyID,this,false);
		nodeReporterReceiver=new NodeReporterReceiver(connection,MessageBusNames.ReportTwitterWEB,this,false);
		

		crawlReporter=new CrawlerServerReporter("TwitterWEB");
		tracker=new MyTracker();
		
		//��ʼ��MessageBussManagement
		msgbusmag=new MessageBusManagement();
	}

	public static String basepath;
	
	public boolean StartCrawlServer(){
		LogSys.crawlerServLogger.info("�ɼ����ܿض˿�ʼ");
		StartReportTimer();
		StartSchedulTimer();//����������
		StartKeyUserSchedulTimer();//������KeyUser�Ķ�ʱ�ɼ�
		StartKeyWordSchedulTimer();//������KeyWords�Ķ�ʱ�ɼ�
		StartPlantSchedulerTimer();//������ͨ����ʱ����������InputTask��ˢ�£�
		StartNettyTaskMonitor();//�����ⲿ�����TCP
		try{
			CollectionNodes();
			KeyWordSearch(false);		
			CrawlerServerKeyUserSearch(false);
			CrawlerServerNorUserSearch(this.deepth,false);
			LogSys.crawlerServLogger.info("�����ɼ�����All is Finish");
		}catch(Exception ex){
			ex.printStackTrace();
			LogSys.crawlerServLogger.error("crawlServer exit with error");
		}
		LogSys.crawlerServLogger.info("�ɼ����ܿض�ֹͣ");
	
		return true;
	}
	//�����û��Զ���ɼ�
	private boolean StartCustormServer(){
		LogSys.crawlerServLogger.info("�ɼ����ܿض�[�Զ���ģʽ]����");
		try{
			CollectionNodes();
			sb.InitCustomerByFile(this,this.CustomTaskFolderDir);
			sendNewStep(NodeStep.search_start);
			SleepWithCount(5000);			
			sendNewStep(NodeStep.search_end);
			
			LogSys.crawlerServLogger.info("�����ɼ�����All is Finish");
		}catch(Exception ex){
			ex.printStackTrace();
			LogSys.crawlerServLogger.error("crawlServer exit with error");
		}
		LogSys.crawlerServLogger.info("�ɼ����ܿض�ֹͣ");
		return true;
	}

	private boolean StartRefreshServer(){
		LogSys.crawlerServLogger.info("�ɼ����ܿض�[ˢ�¹ؼ��˻�������ģʽ]����");
		currentstep=ServerStep.searchStart;
		try{
			CollectionNodes();
			//ֻȡ��һҳ���û���Ϣ
			sb.InitRefresh(this,"crawlstatus_0309_1427","message_result_0309",TaskType.TimeLine, -1);
			sendNewStep(NodeStep.search_start);
			while(currentstep!=ServerStep.searchEnd){
				CollectNodesStatus();
				SleepWithCount(60000);				
				if(nodeManager.canNextStepByTaskBusName(MessageBusNames.KeyUserTask)){
					currentstep=ServerStep.searchEnd;
					nodeManager.show();
				}else{
					LogSys.crawlerServLogger.debug("���ܽ�����һ���ɼ�״̬");
					
				}
			}
			
			LogSys.crawlerServLogger.info("�ɼ����ܿض�[ˢ�¹ؼ��˻�������ģʽ]FINISH");
		}catch(Exception ex){
			ex.printStackTrace();
			LogSys.crawlerServLogger.error("crawlServer exit with error");
		}
		
		return true;
	}
	
	private boolean StartSampleServer(){
		LogSys.crawlerServLogger.info("�ɼ����ܿض�[����û�Profile]����");
		currentstep=ServerStep.searchStart;
		try{
			CollectionNodes();
			//ֻȡ��һҳ���û���Ϣ
			sb.InitSample(this, "RandomUser_2.txt", "user_profile_sample_2", TaskType.About);
			sendNewStep(NodeStep.search_start);
			while(currentstep!=ServerStep.searchEnd){
				CollectNodesStatus();
				SleepWithCount(60000);				
				if(nodeManager.canNextStepByTaskBusName(MessageBusNames.KeyUserTask)){
					currentstep=ServerStep.searchEnd;
					nodeManager.show();
				}else{
					LogSys.crawlerServLogger.debug("���ܽ�����һ���ɼ�״̬");
					
				}
			}
			
			LogSys.crawlerServLogger.info("�ɼ����ܿض�[����û�Profile]FINISH");
		}catch(Exception ex){
			ex.printStackTrace();
			LogSys.crawlerServLogger.error("crawlServer exit with error");
		}
		
		return true;
	}
		
	
	/*Try To Stop Current CrawlServer~~
	 * 1:TellNodeToPause (don't get Task from MessageBus);
	 * 2:TaskMessage Bus Save To File
	 * 3:Save Current Status--CollentionNodes,MainSearch,KeyUser,NormalUser
	 * 3:
	 * 4:CrawlServerToStop;
	 * 5:finish;
	 * 
	 * */
	public boolean StopCrawlServer(){
		this.TellNodeToPause();
		//�ȴ�ȷ��---miss
		CrawlerServerDumper crawlerDumper=new CrawlerServerDumper(this);
		if(crawlerDumper.TaskSaver("Output\\Facebook\\TaskDump.dat"))
			LogSys.crawlerServLogger.info("Success To Save Task To File");
		else{
			LogSys.crawlerServLogger.info("Fail To Save Task To File");
			return false;
		}
		/*���浱ǰ״̬
		 * Status;ServerBean;
		*/
		if(crawlerDumper.OtherStatusSaver("Output\\Facebook\\OtherStatusDump.dat"))
			LogSys.crawlerServLogger.info("Success To Save Status To File");
		else{
			LogSys.crawlerServLogger.info("Fail To Save Status To File");
			return false;
		}
		return true;	
		
	}
	//restart from file
	
	public boolean RestartCrawlServer(){
		LogSys.crawlerServLogger.info("�ɼ�����ʼ�ָ�");			
		LogSys.crawlerServLogger.info("�ɼ����ָ���ɿ�ʼ����");
		this.TellNodeToPause();
		CrawlerServerDumper crawlerDumper=new CrawlerServerDumper(this);
		if(crawlerDumper.TaskResume("Output\\Facebook\\TaskDump.dat"))
			LogSys.crawlerServLogger.info("Success To Resume Task From File To CrawlServer");
		else{
			LogSys.crawlerServLogger.error("Fail To Resume Task From File To CrawlServer");
			return false;
		}
		if(crawlerDumper.OtherStatusResumer("Output\\Facebook\\OtherStatusDump.dat"))
			LogSys.crawlerServLogger.info("Success To Resume Status From File To CrawlServer");		
		else{
			LogSys.crawlerServLogger.error("Fail To Resume Status From File To CrawlServer");
			return false;
		}
		this.CollectionNodes();
		this.TellNodeToResume();
		switch(this.currentstep){
			case init:
			case searchStart:
				KeyWordSearch(true);
			case searchEnd:
			case keyuserCaijiStart:
				CrawlerServerKeyUserSearch(true);
			case keyuserCaijiEnd:
			case normalCaijiStart:
				CrawlerServerNorUserSearch(this.deepth,true);
			case normalCaijiEnd:
			default:
				System.err.println("û�з��ֿ��Իָ�����㣬��ǰ״̬��"+currentstep);

		}
		LogSys.crawlerServLogger.info("�ָ��ɼ�����All is Finish");
		return true;
	}
	
	
	protected void SleepWithCount(int count){
		try {
			Thread.sleep(count);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected void CollectionNodes(){
		LogSys.crawlerServLogger.info("�����ڵ�15S");
		SleepWithCount(15000);		
		LogSys.crawlerServLogger.info("��ǰ״̬"+nodeManager.currentstep());
		LogSys.crawlerServLogger.info("�ڵ����"+nodeManager.nodecount);	
	}
	protected void KeyWordSearch(boolean isResume){
		currentstep=ServerStep.searchStart;
		LogSys.crawlerServLogger.info("�ؼ���������ʼ");			
		//�ǳ���Ҫ
		if(!isResume)
			KeyWordsSearch(keySearchCount);
		while(currentstep!=ServerStep.searchEnd){
			CollectNodesStatus();
			SleepWithCount(60000);				
			if(nodeManager.canNextStepByTaskBusName(MessageBusNames.KeyWordAndTopicTask)){
				currentstep=ServerStep.searchEnd;
				nodeManager.show();
			}else{
				LogSys.crawlerServLogger.debug("���ܽ�����һ���ɼ�״̬");
				
			}
		}
		LogSys.crawlerServLogger.info("�ؼ�����������");
	}
	protected void CrawlerServerKeyUserSearch(boolean isResume){
		
		currentstep=ServerStep.keyuserCaijiStart;
		LogSys.crawlerServLogger.info("�ؼ��û��ɼ���ʼ");		
		if(!isResume)
			ChuizhiCaiji();
		while(currentstep!=ServerStep.keyuserCaijiEnd){
			CollectNodesStatus();
			SleepWithCount(20000);
			if(nodeManager.canNextStepByTaskBusName(MessageBusNames.KeyUserTask))
				currentstep=ServerStep.keyuserCaijiEnd;
		}
		LogSys.crawlerServLogger.info("�ؼ��û��ɼ�����");
	}
	protected int CrawlerServerNorUserSearch(int deepth,boolean isResume){
		this.setCrawlServerDeepth(deepth);//����CrawlServer����Ⱥ͵ݹ����ȱ���һ��
		currentstep=ServerStep.normalCaijiStart;
		LogSys.crawlerServLogger.info("��ͨ�û��ɼ���ʼ---��ȣ�"+deepth+"-----");		
		if(!isResume)
			NormalCaiji();
		
		while(currentstep!=ServerStep.normalCaijiEnd){
			CollectNodesStatus();
			SleepWithCount(20000);
			if(nodeManager.canNextStepByTaskBusName(MessageBusNames.Task))
				currentstep=ServerStep.normalCaijiEnd;
		}
		LogSys.crawlerServLogger.info("��ͨ�û��ɼ�����---��ȣ�"+deepth+"-----");
		if(deepth==1){
			return 0;
		}else{
			return CrawlerServerNorUserSearch(deepth-1,false);
		}
	}
	
		
	//�ɼ������û���Ϣ������
	public void ChuizhiCaiji(){							
		String filelocation=basepath+"/UsefulFile/KeyIDs.txt";
		//��Ӵ����ݿ��ж�ȡ�ؼ��û���һ��
		sb.InitChuizhi(filelocation, this,isFirstChuiZhi);
		SleepWithCount(5000);
		sendNewStep(NodeStep.keyuser_start);
		
		isFirstChuiZhi=false;
		
		
	}
	public void KeyWordsSearch(int max){
//		msgbusmag.cleanQueueByName(MessageBusNames.KeyWordAndTopicTask);
//		System.out.println("���KeyWordAndTopic����");
		String filelocation=basepath+"/UsefulFile/minganci_min.txt";
		sb.InitSearch(filelocation,max,this);
		SleepWithCount(5000);
		sendNewStep(NodeStep.search_start);

	}
	public void NormalCaiji(){
		LogSys.crawlerServLogger.info("��Server��-----------��ʼ���вɼ�-----------------");			
		//׼�����вɼ���������Ϣ
		int count=sb.InitBingxing(this,deepth);
		LogSys.crawlerServLogger.info("��Server���ܹ��¼Ӳ�������������"+3*count+"��");
		SleepWithCount(5000);
		sendNewStep(NodeStep.normaluser_start);
		
		
	}
	public boolean addTask(Task task){
		task.setMainType(MainType.Normal);
		int taskTrackerID=tracker.AddTask(task);
		task.setTaskTrackID(taskTrackerID);
		taskSender.Send(task.TaskTOString());
		return true;
	}
	public boolean addUrgentTask(Task task){
		task.setMainType(MainType.Urgent);
		int taskTrackerID=tracker.AddTask(task);
		task.setTaskTrackID(taskTrackerID);//����TaskTrackID;
		urgentTaskSender.Send(task.TaskTOString());
		return true;
	}
	public boolean addKeyWord(Task task){
		task.setMainType(MainType.KeyWord);
		int taskTrackerID=tracker.AddTask(task);
		task.setTaskTrackID(taskTrackerID);
		keyWordAndTopicTaskSender.Send(task.TaskTOString());
		return true;
	}
	public boolean addTopic(Task task){
		task.setMainType(MainType.Topic);
		int taskTrackerID=tracker.AddTask(task);
		task.setTaskTrackID(taskTrackerID);
		keyWordAndTopicTaskSender.Send(task.TaskTOString());
		return true;
	}
	public boolean addKeyUserTask(Task task){
		task.setMainType(MainType.KeyUser);
		int taskTrackerID=tracker.AddTask(task);
		task.setTaskTrackID(taskTrackerID);
		keyUserTaskSender.Send(task.TaskTOString());
		return true;
	}
	
	
	//�����ͨ�û�
	public void addNormalUser(NormalUser nu){		
		sb.addNormalUser(nu,deepth);		
	}
	
	public void addKeyUser(NormalUser nu){
		sb.addKeyUser(nu);
	}
	public int showNormalUserSize(){
		return sb.normalUserList.size();
	}
	
	
	//���нڵ�ֹͣ����
	public boolean stopTaskOnAllNode(){
		LogSys.crawlerServLogger.info("����ֹͣ����ָ��");
		controlSender.Send("STOPALL");
		return true;
	}

	public boolean startTaskOnAllNode(){
		LogSys.crawlerServLogger.info("֪ͨ�ڵ���������");
		controlSender.Send("STARTALL");
		return true;
	}
	
	public boolean sendNewStep(NodeStep step){
		LogSys.crawlerServLogger.info("֪ͨ���нڵ�����µĹ���״̬"+step);
		controlSender.Send("NEWSTEP "+step.toString());
		return true;
	}
	private boolean CollectNodesStatus(){
		controlSender.Send("REPORTSTATUS");		
		return true;
	}
	
	//������node ��ͣ�ɼ�
	public void TellNodeToPause(){
		LogSys.crawlerServLogger.info("CrawlerServer Send Pause");
		controlSender.Send("PAUSE");
	}
	//������node �ָ��ɼ�
	public void TellNodeToResume(){
		controlSender.Send("RESUME");		
	}
	
	
	
	
	
	
	/*
	 * 1��������������е���Ϣ
	 */
	public void ResetMessageBus(){
		MessageBusCleanner.Clean();

	}

	public void getNewNode(String nodeName) {
		PlatFormMain.log.info("������̨�����ܵ��µĽڵ�");
		nodeManager.addNode(nodeName);
	}
	public void oneNodeFinishWork(String nodeName,NodeStep currentstep){
		nodeManager.onnodefinish(nodeName,currentstep);
		nodeManager.onnodefinish(nodeName, currentstep);
	}
	
	public void addNormalUserByID() {
		
		
	}
	public void StartReportTimer(){
		try{
			LogSys.crawlerServLogger.info("��������[����]StartReporterTimer.....");
			Timer timer=new Timer();
			timer.schedule(new TimerTask(){
				public void run() {
					try {
						crawlReporter.doReportIncrementByDataBase(ServerReportData);
						ResetServerReportData();//�㱨��Ͻ�����ղ���;
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						System.err.println("�㱨���ִ������»㱨");
						e.printStackTrace();
					}
				}
			}, 1000, 10000);//ÿ10s��㱨���������л㱨
			LogSys.crawlerServLogger.info("�����ɹ�[����]StartReporterTimer");
			
			LogSys.crawlerServLogger.info("��������[ȫ��]StartReporterTimer.....");
			Timer timerQuanliang=new Timer();
			timerQuanliang.schedule(
					new ReportTotalScheduler(this), 
					20000, //ÿ�λ㱨��ǰ20������
					600000);//ƽ��ÿ5���ӻ㱨һ��ȫ������
			LogSys.crawlerServLogger.info("�����ɹ�[ȫ��]StartReporterTimer");
		}catch(Exception ex){
			ex.printStackTrace();
			LogSys.nodeLogger.error("������ʱ��ʧ��");
		}
	}
	
	//�յ�HeartReport
	public void onReceiveReportFromNode(ReportData rpdata){
		//������֪ʶ��ȡ��Ӧ�����ݲ���ӵ�
		//System.out.printf("Message:%d,User:%d,UserRel:%d\n",rpdata.message_increment,rpdata.user_increment,rpdata.user_rel_increment);
		synchronized (reportlock) {
			ServerReportData.add(rpdata);//�����������ݽ����ۼӣ�
		}
		
		
//		if(NodeReportData.containsKey(rpdata.NodeName)){
//			ReportData tmprpdata=NodeReportData.get(rpdata.NodeName);
//			tmprpdata.add(rpdata);			
//		}else{
//			NodeReportData.put(rpdata.NodeName, rpdata);
//			LogSys.crawlerServLogger.debug("Crawler �յ���Ϣ�����½ڵ�("+rpdata.NodeName+")");
//		}
		
	}
	private void ResetServerReportData(){
		synchronized (reportlock) {
			ServerReportData=new ReportData();
		}
	}
	private void StartSchedulTimer(){
		Timer time=new Timer();
		schedule=new CrawlServerScheduler(this);
		time.schedule(schedule, 10000, 60000);//ƽ��ÿ����ִ��һ��
		System.out.println("����CrawlerSchedul�ɹ�");
	}
	private void StartKeyUserSchedulTimer(){
		Timer time=new Timer();
		KeyUserScheduler kuser=new KeyUserScheduler(this);
		long minute=60000;
		long hour=60*minute;
		long day=hour*24;
		time.schedule(kuser, HALF_DAY,day);
		System.out.println("����KeyUserSchedul�ɹ�");
	}
	//����KeyWord����
	private void StartKeyWordSchedulTimer(){
		Timer time=new Timer();
		KeyWordsScheduler keyword=new KeyWordsScheduler(this);
		long minute=60000;
		long hour=60*minute;
		long day=hour*24;
		time.schedule(keyword, HALF_DAY, day);//ÿ�����keyUser10min���Keywords
		System.out.println("����KeyUserSchedul�ɹ�");
	}
	//InputTask ������
	private void StartPlantSchedulerTimer(){
		Timer time=new Timer();
		PlantScheduler keyword=new PlantScheduler(this);
		long minute=60000;
		long hour=60*minute;
		long day=hour*24;
		time.schedule(keyword, HALF_DAY, day);//ÿ�춨ʱ����InputTask�������·�������
		System.out.println("����PlantScheduler�ɹ�");
	}
	
	/*
	 * ͨ��Monitor��������������
	 */
	private void StartNettyTaskMonitor(){
		ds=new DiscardServer(8080,this);
		Thread t=new Thread(ds,"");
		t.start();
		System.out.println("����StartNettyTaskMonitor�ɹ�");
			
	}
	
	
	public void ShowAndLog(String msg){
		LogSys.crawlerServLogger.info("��CRAWLERSERVER��"+msg);
		
	}
	private void setCrawlServerDeepth(int dep){
		this.deepth=dep;
	}

	

	
	
	

	

}
