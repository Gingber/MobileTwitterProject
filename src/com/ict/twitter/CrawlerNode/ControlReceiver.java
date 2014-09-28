package com.ict.twitter.CrawlerNode;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.MessageBus.*;
import com.ict.twitter.plantform.LogSys;

public class ControlReceiver extends Receiver {

	public ControlReceiver(javax.jms.Connection connection,String queue, Node _node,boolean isTopic) {
		super(connection,isTopic,queue, _node,false,null);
		isNode=true;
		this.node=_node;
		
	}	
	public ControlReceiver(javax.jms.Connection connection,String controlC2S, CrawlerServer _server,boolean isTopic,MessageListener msg) {
		super(connection,false,controlC2S, _server,false,msg);
		isNode=false;
		server=_server;
	}
	

	public void onMessage(Message _resmsg) {
		TextMessage resmsg=(TextMessage)_resmsg;
		String msg = null;		
		try {
			msg = resmsg.getText();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(isNode){
			LogSys.nodeLogger.debug("��node.NodeName"+"��"+"�ڵ��յ�����״ָ̬��"+msg);
			if(msg.equalsIgnoreCase("STOPALL")){
				LogSys.nodeLogger.debug("�ͻ�����ն��У�ֹͣ����with Task+"+node.taskBuffer.size()+"��.");
				node.CleanTask();
			}else if(msg.equalsIgnoreCase("STARTALL")){
				System.out.println("�ͻ���������������");
				LogSys.nodeLogger.info("�ͻ���Ŀǰ����������with Task"+node.taskBuffer.size()+"��.");
			}else if(msg.startsWith("NEWSTEP")){
				node.getNodeStatusBean().isBusy=true;				
				String[] all=msg.split(" ");
				if(all.length!=2){
					System.out.println("ָ�����"+msg);
					return;
					
				}else{
					String command=all[1];
					node.getNodeStatusBean().curStep=NodeStep.valueOf(command);
					System.out.println("�ͻ����յ��µĲɼ�״̬"+command);
				}
			}else if(msg.startsWith("REPORTSTATUS")){
				node.getNodeStatusBean().HeartBeat(node.controlUpload);
				
			}else if(msg.startsWith("PAUSE")){
				System.out.println("�ͻ����յ�PAUSEָ��");				
				node.Pause();
			}else if(msg.startsWith("RESUME")){
				System.out.println("�ͻ����յ�RESUMEָ��");	
				node.Resume();
			}
		}	
	}





}
