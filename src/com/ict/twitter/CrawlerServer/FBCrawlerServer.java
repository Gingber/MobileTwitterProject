package com.ict.twitter.CrawlerServer;

import com.ict.twitter.CrawlerNode.NodeStep;
import com.ict.twitter.CrawlerServer.CrawlerServer.OP;
import com.ict.twitter.MessageBus.MessageBusNames;
import com.ict.twitter.MessageBusTest.ControlClient;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.plantform.PlatFormMain;

public class FBCrawlerServer extends CrawlerServer {
	public static final String Name="FacebookWEBCrawl";	
	public enum ServerStep{
		init,keyuserCaijiStart,keyuserCaijiEnd,normalCaijiStart,normalCaijiEnd
	}
	public com.ict.twitter.CrawlerServer.FBCrawlerServer.ServerStep currentstep=ServerStep.init;
	
	
	@Override 
	public void run(){
		checkArgs();
		if(op==OP.START){
			Initiallize();
			StartCrawlServer();
		}else if(op==OP.STOP){
			System.err.println("�˴���Ҫ�޸�");
			System.exit(-1);
			//StopCrawlServer();
		}
		
	}
	public FBCrawlerServer(String[] args){
		LogSys.crawlerServLogger.info("Crawlserver��ʼ��");
		if (args.length < 1) {
			System.err.println("Usage: CrawlerServer -Command [Start|Stop|Dump|Restart] -Deepth 10 -KeySearchCount 10");
		    return;
	    }
		this.args=args;
	}

	public static void main(String[] args){
		for(int i=0;i<args.length;i++){
			System.out.print(args[i]+" ");
		}
		FBCrawlerServer crawler =  new FBCrawlerServer(args);
		Thread mthread= new Thread(crawler);
		mthread.setName("CrawlServer");
		mthread.start();
	}
	public boolean StartCrawlServer(){
		LogSys.crawlerServLogger.info("�ɼ����ܿض˿�ʼ");
		try{
			CollectionNodes();
			CrawlerServerKeyUserSearch(false);
			NorUserSearchWithDepth(Normal_User_Deepth,false);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		LogSys.crawlerServLogger.info("�ɼ����ܿض�ֹͣ");
		LogSys.crawlerServLogger.info("�����ɼ�����All is Finish");
		return true;
	}
	public boolean RestartCrawlServer(){
		LogSys.crawlerServLogger.info("FBCrawlerServer Run Method isStart");			
		LogSys.crawlerServLogger.info("FBCrawlerServer��ʼ�ָ�");			
		LogSys.crawlerServLogger.info("FBCrawlerServer��ʼ����");
		boolean flag=true;
		CollectionNodes();
		switch(this.currentstep){
			case init:
			case keyuserCaijiStart:
				CrawlerServerKeyUserSearch(flag);
				flag=false;
			case keyuserCaijiEnd:
			case normalCaijiStart:
				NorUserSearchWithDepth(Normal_User_Deepth,flag);
				flag=false;
			case normalCaijiEnd:
			
			break;
		}
		LogSys.crawlerServLogger.info("FBCrawlerServer�ָ��ɼ�����");
		return true;		
	}

	
	
	
	//ͨ���ݹ��߼�ʵ�����Զ�ȷ������һ�㿪ʼ��
	private int NorUserSearchWithDepth(int deepth,boolean isResume){
		LogSys.crawlerServLogger.info("FBCrawlServer NormalUser Turn:["+deepth+"]");
		currentstep=ServerStep.normalCaijiStart;
		LogSys.crawlerServLogger.info("��ͨ�û��ɼ���ʼ");		
		if(!isResume)
			NormalCaiji();
		while(currentstep!=ServerStep.normalCaijiEnd){
			CollectNodesStatus();
			SleepWithCount(20000);
			if(nodeManager.canNextStepByTaskBusName(MessageBusNames.Task))
				currentstep=ServerStep.normalCaijiEnd;
		}
		LogSys.crawlerServLogger.info("��ͨ�û��ɼ�����");
		if(deepth==1){
			return 0;
		}else{
			return NorUserSearchWithDepth(deepth-1,false);
		}
	}
	
	
	
	
	
	
	private boolean CollectNodesStatus(){
		controlSender.Send("REPORTSTATUS");		
		return true;
	}
	//�ɼ������û���Ϣ������
	@Override
	public void ChuizhiCaiji(){							
		String filelocation=basepath+"/UsefulFile/Facebook/FBKeyIDs.txt";
		sb.InitChuizhi(filelocation, this,isFirstChuiZhi);
		SleepWithCount(5000);
		sendNewStep(NodeStep.keyuser_start);		
		isFirstChuiZhi=false;		
	}
	
	

	@Override
	public void NormalCaiji(){
		LogSys.crawlerServLogger.info("��Server��-----------��ʼ���вɼ�-----------------");			
		//׼�����вɼ���������Ϣ
		int count=sb.InitBingxing(this,deepth);
		LogSys.crawlerServLogger.info("��Server���ܹ��¼Ӳ�������������"+count+"��");
		SleepWithCount(5000);
		sendNewStep(NodeStep.normaluser_start);		
	}
}
