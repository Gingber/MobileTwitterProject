package com.ict.twitter.CrawlerServer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;



import com.ict.twitter.MessageBus.GetAceiveMqConnection;
import com.ict.twitter.MessageBus.MessageBusNames;
import com.ict.twitter.MessageBus.MessageBussConnector;
import com.ict.twitter.MessageBus.Sender;
import com.ict.twitter.plantform.LogSys;

public class MessageBusStatusDetect{

	String MessageBusName;
	Queue queue;
	Session session;
	MessageProducer producer;
	Message msg;
	MessageConsumer consumer;
	public long oldSize=0;
	public MessageBusStatusDetect(){
		try{
			ActiveMQConnection connection=GetAceiveMqConnection.StaticGetConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue replyTo = session.createTemporaryQueue();
			consumer= session.createConsumer(replyTo);
			//consumer.setMessageListener(this);
			msg = session.createMessage();
			msg.setJMSReplyTo(replyTo);
			//������׼������
			

			//System.out.println("���ս��Start");
			//MapMessage reply = (MapMessage) consumer.receive();		

			//System.out.println("���ս��Finish");
					
			
			
			
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(-1);
		}

	}
	public long getCount(String newMessageBusNames){
		String queueName = "ActiveMQ.Statistics.Destination."+newMessageBusNames;
		String queueNameForUser="Task����"+"["+newMessageBusNames+"]";
		try{
			queue=session.createQueue(queueName);
			producer=session.createProducer(null);	
			producer.send(queue, msg);
			MapMessage reply = (MapMessage) consumer.receive(5000);	
			if(reply==null){
				LogSys.crawlerServLogger.info(queueNameForUser+"�ϵĴ�С��ȡ��ʱ");					
				return -1;
			}
			long size=reply.getLong("size");
			if(oldSize==size){
				;
			}else{
				oldSize=size;
				LogSys.crawlerServLogger.debug(queueNameForUser+"�ϵĴ�С_���ս��Finish("+size+")");					
			}
			return reply.getLong("size");
		}catch(JMSException ex){
			ex.printStackTrace();
			LogSys.crawlerServLogger.error(ex.getErrorCode());
			LogSys.crawlerServLogger.error(ex.getMessage());
			return -1;
		}
		
		
	}
	/*
	 * ������Ϣ�����е���Ŀ��
	 */
	public static void main(String[] args) {
		Sender send=new Sender(MessageBusNames.Task,false);
		for(int i=0;i<=10;i++){
			send.Send(i);
		}
		long count=0;
		MessageBusStatusDetect MessageBusTest=new MessageBusStatusDetect();
		count=MessageBusTest.getCount(MessageBusNames.Task);
		System.out.println("cOunt    "+count);
		//MessageBusCleanner.CleanTaskBus();
		count=MessageBusTest.getCount(MessageBusNames.Task);
		System.out.println("cOunt2   "+count);
		
	}

	/*
	@Override
	public void onMessage(Message arg0) {
		//�յ����ߵ���Ϣ
		System.out.println("~~~~~~~~~~~~");
		try{
			MapMessage reply=(MapMessage)arg0;
			for (Enumeration e = reply.getMapNames();e.hasMoreElements();) {
			    String name = e.nextElement().toString();
			    System.err.println(name + "=" + reply.getObject(name));
			}
		}catch(javax.jms.JMSException ex){
			ex.printStackTrace();
		}
	}
	
	*/

}
