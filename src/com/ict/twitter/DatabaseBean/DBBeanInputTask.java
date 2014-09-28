package com.ict.twitter.DatabaseBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.ict.twitter.CrawlerServer.InputType;
import com.ict.twitter.tools.DbOperation;

public class DBBeanInputTask {
	java.sql.PreparedStatement pst;
	java.sql.PreparedStatement newInput;
	java.sql.PreparedStatement statusModify;//用于修改inputTask的Status状态
	
	
	public boolean CheckHasNewInput(){
		ResultSet rs=null;
		DbOperation dbOp=new DbOperation();
		Connection con = dbOp.GetConnection();
		try {
			java.sql.PreparedStatement newInput=con.prepareStatement("Select count(*) from inputtask where Status='Created'");;
			rs=newInput.executeQuery();
			if(rs.next()){
				int result=rs.getInt(1);
				return result>0;
			}else{
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally{
			try{
				rs.close();
				con.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
		}	
	}
	public Vector<InputTaskBean> GetTask(InputType inputType){
		Vector<InputTaskBean> vector;
		DbOperation dbOp=new DbOperation();
		Connection con = dbOp.GetConnection();
		try {
			String GetNewTaskByType="SELECT ID,TaskName,TaskParameter,TaskWeight from inputtask where Status='Created' AND InputType=?";
			java.sql.PreparedStatement pst=con.prepareStatement(GetNewTaskByType);
			pst.setString(1, inputType.toString());
			ResultSet rs=pst.executeQuery();
			vector=new Vector<InputTaskBean>();
			while(rs.next()){
				InputTaskBean bean=new InputTaskBean();
				bean.ID=rs.getInt(1);
				bean.TaskName=rs.getString(2);
				bean.TaskParameter=rs.getString(3);
				bean.TaskWeight=rs.getInt(4);
				vector.add(bean);				
			}
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			vector=null;
		} finally{
			try{				
				pst.close();
				con.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return vector;
	}
	public Vector<InputTaskBean> GetAllTask(){
		Vector<InputTaskBean> vector;
		DbOperation dbOp=new DbOperation();
		Connection con = dbOp.GetConnection();
		try {
			PreparedStatement PstAll=con.prepareStatement("Select ID,TaskName,TaskParameter,TaskParameter2,InputType,TaskWeight from inputtask where Status='Created'");
			ResultSet rs=PstAll.executeQuery();
			vector=new Vector<InputTaskBean>();
			while(rs.next()){
				InputTaskBean bean=new InputTaskBean();
				bean.ID=rs.getInt(1);
				bean.TaskName=rs.getString(2);
				bean.TaskParameter=rs.getString(3);
				bean.TaskParameter2=rs.getString(4);
				bean.InputType=InputType.valueOf(rs.getString(5));
				bean.TaskWeight=rs.getInt(6);
				vector.add(bean);				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			vector=null;
		} finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return vector;
	}
	public boolean ModifyStatus(String status,int ID){
		DbOperation dbOp=new DbOperation();
		Connection con = dbOp.GetConnection();
		try{
			statusModify=con.prepareStatement("UPDATE inputtask SET Status=? where Id=?");
			statusModify.setString(1, status);
			statusModify.setInt(2, ID);
			int ret=statusModify.executeUpdate();
			if(ret==1){
				return true;
			}
		}catch(SQLException ex){
			ex.printStackTrace();
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
		
	}
	
	public static void main(String[] args){
		DBBeanInputTask dbinput=new DBBeanInputTask();
		Vector<InputTaskBean> vector=dbinput.GetTask(InputType.Topic);
		System.out.println(vector.get(0).ID);
	}
}
