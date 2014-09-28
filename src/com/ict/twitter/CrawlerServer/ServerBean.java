package com.ict.twitter.CrawlerServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.sql.*;

import com.ict.twitter.DAO.DBKeyUserDAO;
import com.ict.twitter.StatusTrack.CrawlUserDB;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;
import com.ict.twitter.tools.DbOperation;
import com.ict.twitter.tools.ReadTxtFile;
import com.ict.twitter.tools.SaveTxtFile;
public class ServerBean implements Serializable{
	private static final long serialVersionUID = -9133824015130047653L;
	public static String aname="~~~~";
	public static boolean isFirstChuizhi=true;
	CrawlUserDB crawluser=new CrawlUserDB();
	DBKeyUserDAO dbkeyuser=new DBKeyUserDAO();
	List<NormalUser> normalUserList=Collections.synchronizedList(new UserList<NormalUser>());
	List<NormalUser> keyUsers=Collections.synchronizedList(new UserList<NormalUser>());		
	//��ʼ���ؼ�������
	public void InitSearch(String file,int max,CrawlerServer server){
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			String t;
			int i=0;
			while((t=br.readLine())!=null){
				Task task=new Task();
				task.setOwnType(TaskType.Search);
				task.setTargetString(t);
				server.addKeyWord(task);				
				i++;
				if(i>max){
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//���ݴ����tableName���ɶ�Ӧ��Message�ɼ�����
	public void InitCustomer(CrawlerServer server,String tableName,TaskType tasktype){
		DbOperation dbo=new DbOperation();
		Connection con=dbo.GetConnection();
		Vector<String> userlist=new Vector<String>();
		try {
			PreparedStatement pst=con.prepareStatement("select UserName from keyuser where weight >3");
			ResultSet rs=pst.executeQuery();
			while(rs.next()){
				userlist.add(rs.getString(1));				
			}
			for(String t:userlist){
				Task task=new Task();
				task.setOwnType(tasktype);
				task.setTargetString(t);
				server.addKeyUserTask(task);
			}
			System.out.println("��������ӹؼ��û�-Timeline"+userlist.size());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	//���ݴ����tableName���ɶ�Ӧ��Message�ɼ�����
	public void InitCustomerByFile(CrawlerServer server,String FileName){
		DbOperation dbo=new DbOperation();
		Connection con=dbo.GetConnection();
		Vector<String> userlist=null;
		Properties pro=new Properties();
		TaskType tasktype=null;
		String NameFileName=null;
		int pageCount=-1;
		String targetTableName=null;
		try{
			pro.load(new FileInputStream(new File(FileName+"\\"+"config.ini")));
			String taskTypeStr=pro.getProperty("TaskType");
			tasktype=TaskType.fromString(taskTypeStr);
			NameFileName=FileName+"\\"+pro.getProperty("FileName");
			String PageCountStr=pro.getProperty("PageCount");
			if(PageCountStr!=null){
				pageCount=Integer.parseInt(PageCountStr);
			}
			targetTableName=pro.getProperty("TargetTableName");
			if(tasktype==null||FileName==null||targetTableName==null){
				LogSys.crawlerServLogger.error("ָ�����ֶ������ļ�������");
				System.exit(-1);
				return;
			}
			
		}catch(Exception ex){
			LogSys.crawlerServLogger.error("ָ�����ֶ������ļ��в�����");
			ex.printStackTrace();
		}
		
		
		try {
			ReadTxtFile rxf=new ReadTxtFile(NameFileName);
			userlist=rxf.read();
			for(String t:userlist){
				if(t.equalsIgnoreCase("")){
					continue;
				}
				Task task=new Task();
				task.setOwnType(tasktype);
				task.setTargetString(t);
				task.setPageCount(pageCount);
				task.setTargetTableName(targetTableName);
				server.addKeyUserTask(task);
			}
			System.out.println("��������ӹؼ��û�-Timeline"+userlist.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void InitRefresh(CrawlerServer server,String tableName,String targetTableName,TaskType tasktype,int pageCount){
		DbOperation dbo=new DbOperation();
		Connection con=dbo.GetConnection();
		Vector<String> userlist=new Vector<String>();
		try {
			PreparedStatement pst=con.prepareStatement("select taskStr from "+tableName+" "+" where Status='Fail'");
			ResultSet rs=pst.executeQuery();
			while(rs.next()){
				userlist.add(rs.getString(1));				
			}
			for(String t:userlist){
				Task task=new Task();
				task.setPageCount(-1);
				task.setOwnType(tasktype);
				task.setTargetString(t);
				task.setTargetTableName(targetTableName);
				task.setPageCount(pageCount);
				server.addKeyUserTask(task);
			}
			LogSys.crawlerServLogger.info("��������ӹؼ��û�-Timeline"+userlist.size());
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void InitSample(CrawlerServer server,String fileName,String targetTableName,TaskType tasktype){
		try {
			ReadTxtFile rxf=new ReadTxtFile(fileName);
			Vector<String> userlist=rxf.read();
			int count=0;
			for(String t:userlist){
				if(t.equalsIgnoreCase("")){
					continue;
				}
				Task task=new Task();
				task.setOwnType(tasktype);
				task.setTargetString(t);
				task.setTargetTableName(targetTableName);
				task.setAddParameter("UseUserID");
				task.setPageCount(-1);
				server.addKeyUserTask(task);
				count++;
				if(count%100==0){
					System.out.println("�������"+count+"/"+userlist.size());
				}
				
			}
			System.out.println("��������ӹؼ��û�-Profile"+userlist.size());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	
	//׼����ʼ��ֱ�ɼ����������
	public void InitChuizhi(String file,CrawlerServer server,boolean isFirst){
		Vector<String> res=new Vector<String>();
		if(isFirst){
			server.isFirstChuiZhi=false;
			ReadTxtFile rxf=new ReadTxtFile(file);
			res=rxf.read();			
			
		}
		
		//����KeyWordsSearch���ֵĹؼ��û�		
		for(NormalUser nu:keyUsers){
			res.add(nu.userID);
		}			
		for(String t:res){			
			Task task=new Task();
			task.setOwnType(TaskType.TimeLine);
			task.setTargetString(t);
			server.addKeyUserTask(task);
			
			task=new Task(); 
			task.setOwnType(TaskType.Following);
			task.setTargetString(t);
			server.addKeyUserTask(task);
			
			task=new Task();
			task.setOwnType(TaskType.Followers);
			task.setTargetString(t);
			server.addKeyUserTask(task);
			
			task=new Task();
			task.setOwnType(TaskType.About);
			task.setTargetString(t);
			server.addKeyUserTask(task);	
		}
		
		
		
	}
	
	
	
	//��ʼ�����вɼ���Ҫһֱ������Ҫ���м��~~
	//2013-03-07 ������i+=3������bug,�ɼ���������
	//NormalUserList size �ιʵ���0����������
	public int InitBingxing(CrawlerServer server,int deepth){
		LogSys.crawlerServLogger.debug("CrawlerServer NormalUserList Size:"+normalUserList.size());
		for(int i=0;i<normalUserList.size();i++){
			if(i%100==0){
				try {
					LogSys.crawlerServLogger.info("��ǰ���е�"+i+"�����ݵ����NormalUser����,��["+normalUserList.size()+"]");
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			NormalUser nu=normalUserList.get(i);
			Task task=new Task();
			task.setTargetString(nu.userID);
			task.setOwnType(TaskType.TimeLine);
			server.addTask(task);
			task.setOwnType(TaskType.Following);
			server.addTask(task);
			task.setOwnType(TaskType.Followers);
			server.addTask(task);
			task.setOwnType(TaskType.About);
			server.addTask(task);
		}
		int size=normalUserList.size();
		normalUserList.clear();		
		return size;

		
	}//initBingxing() Ends
	
	
	/*
	 * keyUser.contains(user) ֵ����ȶ����
	 */
	public void addNormalUser(NormalUser user,int deepth){
		//��������û��б����Ѿ����ڡ� 
		if(keyUsers.contains(user)){
			return ;
		}		
		normalUserList.add(user);
		crawluser.insertUserItem(user.userID, -1, false, null, deepth);		
	} 
	public void showNormalUserList(){
		for(NormalUser u:normalUserList){
			System.out.print(u.userID+":"+u.sum+"\t");
		}
		System.out.println();
	}
	
	public void addKeyUser(NormalUser user){
		//��������û��б����Ѿ����ڡ�
		try {
			dbkeyuser.CheckAndInsert(user.getUserID());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(keyUsers.contains(user)){
			return ;
		}
		
		crawluser.insertUserItem(user.userID, -1, true, null, 0);	
		keyUsers.add(user);
	} 
	
	


	public static void ADDKEYUSER(ServerBean sb, NormalUser nu) {
		sb.addKeyUser(nu);
		
	}
	
}
