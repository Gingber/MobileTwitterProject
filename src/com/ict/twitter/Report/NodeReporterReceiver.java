package com.ict.twitter.Report;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.MessageBus.Receiver;
//���������ڲɼ��������ˣ����ղɼ��ڵ�Ļ㱨��Ϣ��
public class NodeReporterReceiver extends	Receiver{

	public NodeReporterReceiver(javax.jms.Connection connetion,String queue, CrawlerServer server, boolean isTopic) {
		super(connetion,false,queue, server, false,null);
	}
	public void onMessage(Message resmsg) {
		try {
			if(resmsg instanceof ObjectMessage){
			ReportData rpdata=(ReportData)((ObjectMessage)resmsg).getObject();
			server.onReceiveReportFromNode(rpdata);
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	

	
}
