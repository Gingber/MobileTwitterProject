package com.ict.twitter.plantform;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.MessageBusTest.MessageBusCleanner;
import com.ict.twitter.MessageBusTest.ControlServer;

public class PlatFormMain {
	public static Log log= LogFactory.getLog(ControlServer.class);
	public String status;
	ConsoleCenterView msgShow;
	ConsoleShowBean consoleShowBean;
	ControlServer controlServer;
	CrawlerServer _server;
	public static boolean CommonTest(){
		PropertyConfigurator.configure ("config/log4j_Main.properties" ) ;
		log.info("�������.......");
		NetworkTest nt=new NetworkTest();
		boolean[] netWork=nt.TestNetwork();
		if(!(netWork[0]||netWork[1])){
			log.info("���粻ͨ��,�����������ڴ���ģʽ�¼����������.......");
			return false;
		}
		MessageBusTest mt=new MessageBusTest();
		log.info("����ͨ��......");
		if(!mt.doTest()){
			log.error("��Ϣ�����쳣,������Ϣ��������.......");
			return false;
		}else{
			log.debug("��Ϣ���߹�������");
		}
		
		return true;
	}
	public void start(){
		PropertyConfigurator.configure ("config/log4j_Main.properties" ) ;
		consoleShowBean=new ConsoleShowBean(this);
		consoleShowBean.showWindow();
		PlatFormMain.log.info("��ʾ�����Ѿ�����");
		
		consoleShowBean.SetStatus("��ʼ��...");
		PlatFormMain.log.info("����״̬Ϊ��ʼ��");
		if(!netTest()){
			PlatFormMain.log.error("�����Լ�Active�����쳣");
			consoleShowBean.SetStatus("����ActiveMQ�쳣");
			return;
		}
		if(!MongoDBTest.doTest()){
			log.error("MongoDB�쳣,����MongoDB����.......");
			consoleShowBean.SetStatus("MongoDB�쳣,����MongoDB����.......");
			return;
		}		
		PlatFormMain.log.info("�����Ϣ����");
		cleanMessageBus();
		showTaskSize();
		consoleShowBean.SetStatus("��Ϣ����������");
		if(!startControlServer()){
			PlatFormMain.log.error("����ControlServer�쳣");
			return;
		}else{
			consoleShowBean.SetStatus("��ControlServer�������ɹ�,�ڵ���"+controlServer.allNodeCondition.size());
			consoleShowBean.mcs.EnableButton();
		}		
		if(!startReporter()){
			PlatFormMain.log.info("ControlCenter ���ݿ�㱨��ʱ��");
			consoleShowBean.SetStatus("��ControlCenter ���ݿ�㱨��ʱ��������ʧ��");
		}else{
			PlatFormMain.log.info("��ControlCenter ���ݿ�㱨��ʱ���������ɹ�");
		}
		
		
	} 
	

	public void SetInfoToShowWindow(String str){
		consoleShowBean.SetStatus(str);
	}
	
/*
 	public boolean startTwitterAPIServer(){
		try{
			PlatFormMain.log.info("׼������TwitterWeb Server");
			consoleShowBean.SetStatus("����TwitterWeb Server....");
			_server=new CrawlerServer(null);
			Thread t=new Thread(_server);
			t.start();
			PlatFormMain.log.info("�������TwitterWeb Server");
			consoleShowBean.SetStatus("����TwitterWeb Server....");
			return true;
		}catch (Exception ex){
			ex.printStackTrace();
			return false;			
		}
		
	}
*/
	
	

	public boolean startControlServer(){
		PlatFormMain.log.info("����ControlServer��......");
		try{
			controlServer=new ControlServer();
			Thread thread=new Thread(controlServer);
			thread.start();
			//ѭ�����״ֱ̬���ڵ�����Ϊֹ��
			while(controlServer.status!="INIT��Ok"){
				SetInfoToShowWindow(controlServer.status);
				PlatFormMain.log.info("controlSer ��������");
				Thread.sleep(1000);				
			}
			
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
			
		}		
		return true;
	}
	

	private boolean netTest(){
		return PlatFormMain.CommonTest();
	}
	private boolean cleanMessageBus(){
		MessageBusCleanner.Clean();
		return true;
	}
	private boolean showTaskSize(){
		
		return true;
	}
	
	public boolean startReporter(){
		Timer t=new Timer();
		int time=600000;
		Properties properties=new Properties();
		try {
			properties.load(new FileInputStream("config/PlatFormConsole.ini"));
			time=Integer.parseInt(properties.getProperty("report.refreshTime"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(java.lang.NumberFormatException e){
			time=600000;
		}
		t.schedule(new TotalCountShowSchedule(consoleShowBean),10000,time);
		
		return true;
	}
	
	
	public static void main(String[] args){	
		PlatFormMain pfm=new PlatFormMain();
		pfm.start();
	}

}
