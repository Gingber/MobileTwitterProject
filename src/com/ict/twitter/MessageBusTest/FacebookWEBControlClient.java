package com.ict.twitter.MessageBusTest;

import com.ict.twitter.CrawlerNode.runFBAjaxNodes;
import com.ict.twitter.CrawlerServer.FBCrawlerServer;
import com.ict.twitter.plantform.LogSys;

public class FacebookWEBControlClient extends ControlClient {
	public FBCrawlerServer server;
	public Thread serverThread;
	public FacebookWEBControlClient(ClientsID name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean ToStart(){
		LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerServer");
		server=new FBCrawlerServer("-Command Start -Deepth 10 -KeySearchCount 10".split(" "));
		serverThread=new Thread(server);
		serverThread.setName("FacebookWebCrawlerServer");
		serverThread.start();
		LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerNodes�ڵ�");
		if(runFBAjaxNodes.run()){
			LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerNodes�ڵ�ɹ�");
		}else{
			LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerNodes�ڵ�ʧ��");
			return false;
		}
		return true;
	}

	@Override
	public boolean ToPause() {
		//Console ������Ϣ��Server
		//CrawlServer ���͸�Node
		//Node ������ͣ
		LogSys.nodeLogger.info("FacebookWEBControlClient user Crawlserver to TellNodeToPause");
		server.TellNodeToPause();
		
		
		return true;
	}
	
	@Override
	public boolean ToResume() {
		//�����ֹͣ�󣬻ָ�����Ҫ���뵱ʱ��Task�б�ActiveMQ
		//�������ֹͣ��ָ���ֻҪȫ����Node��ʼ�ɼ����ɡ�		
		server.TellNodeToResume();
		return true;
		
	}
	

	@Override
	public boolean ToDisplay(){
		LogSys.clientLogger.info("FacebookWeb�յ�չʾTwitterNode ��Ϣ");
		LogSys.clientLogger.info("FacebookWeb Nodes ��Ϣ����--------------");
		if(server!=null){
			server.nodeManager.show();
			LogSys.clientLogger.info("FacebookWeb TaskSize{"+server.nodeManager.getTaskSizeCount()+"}");			
		}
		LogSys.clientLogger.info("---------------------------------------");
		return true;
	}
	
	public boolean ToStop(){
		LogSys.clientLogger.info("FacebookWeb�յ�ֹͣCrawlerServer��Ϣ");
		if(server.StopCrawlServer()){
			serverThread.stop();
			return true;
		}
		else{
			return false;
		}		
	}

	public boolean ToRestart(){
		LogSys.clientLogger.info("FacebookWeb�յ��ָ�����CrawlerServer��Ϣ");
		server=new FBCrawlerServer("-Command Start -Deepth 10 -KeySearchCount 10".split(" "));
		server.RestartCrawlServer();
		server.isResume=true;
		serverThread=new Thread(server);
		serverThread.setName("FacebookWebCrawlerServer");
		serverThread.start();
		if(runFBAjaxNodes.run()){
			LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerNodes�ڵ�ɹ�");
		}else{
			LogSys.clientLogger.info("����FacebookWeb�ɼ�ϵͳCrawlerNodes�ڵ�ʧ��");
			return false;
		}
		return true;
	}
	



}
