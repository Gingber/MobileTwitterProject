package com.ict.twitter.CrawlerSchedul;

import java.util.TimerTask;
import java.util.Vector;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.CrawlerServer.InputType;
import com.ict.twitter.DatabaseBean.DBBeanInputTask;
import com.ict.twitter.DatabaseBean.InputTaskBean;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;

public class CrawlServerScheduler extends TimerTask{
	private CrawlerServer crawlserver;
	private DBBeanInputTask inputTaskOp;
	public CrawlServerScheduler(CrawlerServer crawlserver){
		this.crawlserver=crawlserver;
		inputTaskOp=new DBBeanInputTask();
	}	
	public void run() {
		checkUserInsert();
		
	}
	//����Ƿ����û����������
	public void checkUserInsert(){
		if(inputTaskOp.CheckHasNewInput()){//�����ִ����²��������ʱ�����²�������ݼ��뵽�ɼ������
			LogSys.crawlerServLogger.debug("�����µ���Ҫ���������");
			Vector<InputTaskBean> vector=inputTaskOp.GetAllTask();
			for(int i=0;i<vector.size();i++){
				LogSys.crawlerServLogger.debug("�������������������:"+vector.get(i).TaskName+":"+vector.get(i).InputType);
				InserIntoMessbus(vector.get(i));
				inputTaskOp.ModifyStatus("Crawling", vector.get(i).ID);
			}			
		}
	}
	private void InserIntoMessbus(InputTaskBean inputTask){
		//���ݻ�ȡ����InputTask����CrawlServer�������м������
		if(inputTask.InputType==InputType.Topic){
			String parameter=inputTask.TaskParameter;
			if(parameter!=null){
				String[] keywords=parameter.split(" ");
				for(int i=0;i<keywords.length;i++){
					Task task=new Task();
					task.setTrack(true);
					task.setMainTypeID(inputTask.ID);//����MainTypeID
					task.setOwnType(TaskType.Search);
					task.setTargetString(keywords[i]);
					crawlserver.addTopic(task);
				}
			}
			
			String parameter2=inputTask.TaskParameter2;
			if(parameter2!=null){
				String[] keywords2=parameter2.split(" ");
				for(int i=0;i<keywords2.length;i++){
					Task task=new Task();
					task.setTrack(true);
					task.setMainTypeID(inputTask.ID);//����MainTypeΪ�û������ID
					task.setOwnType(TaskType.TimeLine);
					task.setTargetString(keywords2[i]);
					crawlserver.addTopic(task);
					task.setOwnType(TaskType.Following);////�����û��ķ�˿
					task.setTargetString(keywords2[i]);
					crawlserver.addTopic(task);
					task.setOwnType(TaskType.Followers);//�����û��Ĺ�ע
					task.setTargetString(keywords2[i]);
					crawlserver.addTopic(task);
					task.setOwnType(TaskType.About);//�����û�����ҳ
					task.setTargetString(keywords2[i]);
					crawlserver.addTopic(task);
				}
			}
			

		}
		if(inputTask.InputType==InputType.KeyUser){
			Task task=new Task();
			task.setTrack(true);
			task.setMainTypeID(inputTask.ID);
			task.setTargetString(inputTask.TaskName);//�����������Ҳ��Ϊ�ؼ��ʽ�������
			task.setOwnType(TaskType.TimeLine);//��ע�ص��˻�������
			crawlserver.addKeyUserTask(task);
			task.setOwnType(TaskType.Following);
			crawlserver.addKeyUserTask(task);
			task.setOwnType(TaskType.Followers);
			crawlserver.addKeyUserTask(task);
		}
		if(inputTask.InputType==InputType.KeyWord){
			Task task=new Task();
			task.setTrack(true);
			task.setMainTypeID(inputTask.ID);//Ҫ���ٵ�MainTypeID��
			task.setTargetString(inputTask.TaskName);//�����������Ҳ��Ϊ�ؼ��ʽ�������
			task.setOwnType(TaskType.Search);//��������ΪSearch����
			crawlserver.addKeyUserTask(task);
			
		}
		if(inputTask.InputType==InputType.NorUser){
			Task task=new Task();
			task.setTrack(true);
			task.setMainTypeID(inputTask.ID);//Ҫ���ٵ�MainTypeIDID��
			task.setTargetString(inputTask.TaskName);//����ͨ�û��ļ������ڶ���ͨ�û���ʱ�䣬��˿�Ƚ��м���
			task.setOwnType(TaskType.TimeLine);//
			crawlserver.addTask(task);
			task.setOwnType(TaskType.Following);
			crawlserver.addTask(task);
			task.setOwnType(TaskType.Followers);
			crawlserver.addTask(task);
		}
	}
	
}
