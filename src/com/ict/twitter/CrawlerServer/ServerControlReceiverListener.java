package com.ict.twitter.CrawlerServer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import com.ict.twitter.CrawlerNode.NodeHeartBeatReport;
import com.ict.twitter.plantform.LogSys;

public class ServerControlReceiverListener implements MessageListener{
	CrawlerServer crawlServer;
	public ServerControlReceiverListener(CrawlerServer _crawlServer){crawlServer=_crawlServer;}
	
	public void onMessage(Message msg){
		//System.out.println("CrawlerServer�յ�������Ϣ"+msg.toString());
		if(msg instanceof ObjectMessage){
			ObjectMessage obj=(ObjectMessage)msg;
			try {
				NodeHeartBeatReport report=(NodeHeartBeatReport)obj.getObject();
				crawlServer.nodeManager.onHeartBeatReceive(report);
				return;
				
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception ex){
				System.out.println("����"+msg.toString());
				ex.printStackTrace();
				LogSys.crawlerServLogger.error("����������һ����Ϣ�����ǲ�ȷ���Ƿ�Ϊ�ϴ����������ģ����߸ո����ɵ�");
			}
			
		}
		if(msg instanceof TextMessage){
			System.out.println("�������Ŀ���ͨ���յ��ı���Ϣ");
			TextMessage txtmsg=(TextMessage)msg;			
			String text=null;
			try {
				text = txtmsg.getText();
				LogSys.crawlerServLogger.info("�������˿�����Ϣ"+text);
			} catch (JMSException e) {
				e.printStackTrace();
				return;
			}
			
		}

		

		
		
	}
	public static void main(String[] args){
		
		
	}

}
