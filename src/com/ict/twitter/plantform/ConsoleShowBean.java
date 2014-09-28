package com.ict.twitter.plantform;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ConsoleShowBean implements ActionListener{
	DatabaseCounter dbCounter;
	ConsoleCenterView mcs;
	PlatFormMain platForm;
	public ConsoleShowBean(PlatFormMain _platForm){
		dbCounter=new DatabaseCounter();
		this.platForm=_platForm;
	}

	public void SetStatus(String string){
		mcs.jLabelStatus.setText(string);
		mcs.invalidate();
	}
	public void TwitterAPIShow(int[] value){		
		mcs.TwitterAPIShow(value);
	}
	public void showWindow() {
		// TODO Auto-generated method stub
		mcs=new ConsoleCenterView(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg) {
		if(arg.getActionCommand().equals("����CrawlServer")){
			LogSys.clientLogger.info("����CrawlServer....");
			platForm.controlServer.SendCommandToAllClientToStartUp();
			mcs.jButtonStart.setEnabled(false);
			mcs.jButtonStop.setEnabled(true);
			mcs.jButtonResume.setEnabled(false);
			
		}else if(arg.getActionCommand().equals("StatusTwitterWEB")){
			LogSys.clientLogger.info("������ʾ��ǰ�����ڵ�״̬");
			platForm.controlServer.SendComandToAllClientToDisplayNodesStatus();
			LogSys.clientLogger.info("������ʾ�ڵ�״̬��ָ��");
			
		}else if(arg.getActionCommand().equals("��ͣCrawlerServer")){			
			platForm.controlServer.SendCommandToAllClientToPause();
			mcs.jButtonPause.setEnabled(false);
			mcs.jButtonResume.setEnabled(true);
			LogSys.clientLogger.info("PlatForm������ͣCrawlerServerָ��");
			
		}else if(arg.getActionCommand().equals("�ָ�CrawlerServer")){			
			platForm.controlServer.SendCommandToAllClientToResume();
			mcs.jButtonPause.setEnabled(true);
			mcs.jButtonResume.setEnabled(false);
			LogSys.clientLogger.info("PlatForm���ͻָ�CrawlerServerָ��");			
		}else if(arg.getActionCommand().equals("ֹͣCrawlerServer")){			
			platForm.controlServer.SendCommandToAllClientToStop();
			mcs.jButtonStop.setEnabled(false);
			LogSys.clientLogger.info("PlatForm����ֹͣCrawlerServerָ��");			
		}else if(arg.getActionCommand().equals("����CrawlerServer")){			
			platForm.controlServer.SendCommandToAllClientToRestart();
			mcs.jButtonStop.setEnabled(false);
			LogSys.clientLogger.info("PlatForm��������CrawlerServerָ��");			
		}
	}
	
	
	public void EnableButtons(){
		
	}
	
}
