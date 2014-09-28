package com.ict.twitter.CrawlerServer;

import java.io.*;

import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnection;

import java.util.Iterator;
import java.util.Vector;

import com.ict.twitter.MessageBus.GetAceiveMqConnection;
import com.ict.twitter.MessageBus.MessageBusNames;
import com.ict.twitter.MessageBus.Receiver;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;
import com.ict.twitter.tools.SimpleXmlAnalyser;

public class CrawlerServerDumper {

	/**
	 * @param args
	 */
	CrawlerServer crawlerServer;
	
	public static void main(String[] args) {
		CrawlerServerDumper cd =  new CrawlerServerDumper(null);
		cd.TaskSaver("Output\\Facebook\\TaskDump.dat");

	}
	public CrawlerServerDumper(CrawlerServer server){
		this.crawlerServer=server;
	}
	
	//����������ػָ�����
	public boolean TaskResume(String fileName){
		javax.jms.Connection connection=GetAceiveMqConnection.StaticGetConnection();
		Vector<Task> vector=new Vector<Task>();
		String filePath=fileName;
		if(filePath==null){
			filePath="Output\\Facebook\\TaskDump.dat";
		}
		FileInputStream fis;
		ObjectInputStream ois=null;
		try {
			fis = new FileInputStream(filePath);
			ois = new ObjectInputStream(fis);			
			Object object=null;
			try{
				while((object=ois.readObject())!=null){
					if(object instanceof Task){
						Task task=(Task)object;
						vector.add(task);
					}
				}
			}catch(java.io.EOFException e){
				System.out.println("End of File");
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally{
			try{
				ois.close();
				connection.close();
			}catch(IOException ex){
				ex.printStackTrace();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		int count=0;
		for(Iterator<Task> it=vector.iterator();it.hasNext();count++){
			Task task=it.next();
			if(!crawlerServer.addTask(task)){
				LogSys.crawlerServLogger.info("�������лָ�Task ʧ�ܵ�ǰ��("+count+")�����ǣ�"+task.toString());
				return false;
			}
		}
		LogSys.crawlerServLogger.info("�������лָ�Task ��Ŀ��("+count+")");		
		return true;
	
	}
	
	//û�п�������Vector �����д洢��
	//����������ر������
	public boolean TaskSaver(String fileName){
		ActiveMQConnection connection=GetAceiveMqConnection.StaticGetConnection();
		
		String filePath=fileName;
		if(filePath==null){
			filePath="Output\\Facebook\\TaskDump.dat";
		}
		LogSys.crawlerServLogger.debug("~~~NE22W~~~");
		FileOutputStream fos;
		ObjectOutputStream oos=null;
		try {
			fos = new FileOutputStream(filePath);
			oos = new ObjectOutputStream(fos);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		Receiver tr=new Receiver(connection, false, MessageBusNames.Task, crawlerServer, true, null);
		String taskStr=null;
		int count=0;
		try{
			while((taskStr=tr.Receive())!=null){				
				Task task=StringToTask(taskStr);
				oos.writeObject(task);
				count++;
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}finally{
			LogSys.crawlerServLogger.info("�������ж�ȡ�����浽�ļ���Task ��Ŀ:"+count+"\t"+"FileName:Output\\Facebook\\TaskDump.dat");
			try {
				connection.close();
				oos.flush();
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("�ر��ļ�ʧ��");
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				System.out.println("�ر�Connectionʧ��");
				e.printStackTrace();
			}
		}
		return true;
		
		
	}
	
	public boolean OtherStatusSaver(String filePath){
		if(filePath==null){
			filePath="Output\\Facebook\\OtherStatusDump.dat";
		}
		FileOutputStream fos;
		ObjectOutputStream oos=null;
		try {
			fos = new FileOutputStream(filePath);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(crawlerServer.currentstep);
			oos.writeObject(crawlerServer.sb);
			oos.writeObject(crawlerServer.Normal_User_Deepth);
			oos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			try{
				oos.close();
			}catch(IOException ex){
				ex.printStackTrace();				
			}
			
		}
		return true;
	}
	public boolean OtherStatusResumer(String filePath){
		if(filePath==null){
			filePath="Output\\Facebook\\OtherStatusDump.dat";
		}
		FileInputStream fis;
		ObjectInputStream ois=null;
		try {
			fis = new FileInputStream(filePath);
			ois = new ObjectInputStream(fis);
			Object object=ois.readObject();
			if(object instanceof CrawlerServer.ServerStep){
				CrawlerServer.ServerStep step = (CrawlerServer.ServerStep)object;
				crawlerServer.currentstep = step;
				LogSys.crawlerServLogger.info("Resume CurrentSetp"+step);
			}
			Object serverBeanObject=ois.readObject();
			if(serverBeanObject instanceof ServerBean){
				ServerBean resumesb = (ServerBean)serverBeanObject;
				crawlerServer.sb=resumesb;
				LogSys.crawlerServLogger.info("Resume ServerBean");
			}
			Object CrawlDepthObject=ois.readObject();
			if(CrawlDepthObject instanceof Integer){
				Integer depth = (Integer)CrawlDepthObject;
				crawlerServer.Normal_User_Deepth=depth.intValue();
				LogSys.crawlerServLogger.info("Resume Depth"+crawlerServer.Normal_User_Deepth);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try{
				ois.close();
			}catch(IOException ex){
				ex.printStackTrace();				
			}
			
		}
		return true;
	}

	private Task StringToTask(String mes){
		Task t=new Task();
		String str=mes;
		if(str==null){			
			return null;
		}
		SimpleXmlAnalyser simxml=new SimpleXmlAnalyser(str);		
		String first=simxml.getFirstValueByTag("type");		
		String valuestr=simxml.getFirstValueByTag("value");
		t.setOwnType(TaskType.fromString(first));
		t.setTargetString(valuestr);
		return t;
	}


	
}
