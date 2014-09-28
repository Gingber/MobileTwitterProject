package com.ict.twitter.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.tools.DBFactory;

public class CrawlerServerReporter{
	
	
	private String ClinetID;
	private Connection con;
	public int message_count,message_rel_count,user_count,user_rel_count;
	PreparedStatement ps;
	public CrawlerServerReporter(String _ClientID,Connection _con){
		this.ClinetID=_ClientID;this.con=_con;
		initiallize();
	}
	public CrawlerServerReporter(String _cleintID){
		this.ClinetID=_cleintID;
		con=(new DBFactory()).getConnection();
		initiallize();
	}
	
	
	public void close(){
		try {
			if(ps!=null&&!ps.isClosed()){
				ps.close();
			}
			if(con!=null&&!con.isClosed()){
				con.close();
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public CrawlerServerReporter(String clinetID, Connection con, int messageCount,int messageRelCount, int userCount, int userRelCount) {
		super();
		ClinetID = clinetID;
		this.con = con;
		message_count = messageCount;
		message_rel_count = messageRelCount;
		user_count = userCount;
		user_rel_count = userRelCount;
		initiallize();
		
	}
	public boolean initiallize(){
		try {
			if(con==null||con.isClosed()){
				con=(new DBFactory()).getConnection();
				if(con==null){
					System.out.println("�������Ӵ����Ѿ�");
					return false;
				}
			}
			if(ps==null){
				String psStr="INSERT INTO `statistics` "+
						"(`state`,"+"`message_count`,`message_increment`,"+
						"`user_count`,`user_increment`,`followship_count`,`followship_increment`"+
						") VALUES(?,?,?,?,?,?,?)";
				System.out.println(psStr);
				ps=con.prepareStatement(psStr);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	//Report������Ϣ��ÿ���ӻ㱨һ�Σ�����ÿ����Ϣ�㱨һ��
	public boolean doReportIncrementByDataBase(ReportData rpdata) throws SQLException{
		initiallize();
		ps.setBoolean(1, true);//State������������Ϣ1��������2��ȫ��
		ps.setInt(2, -1);
		ps.setInt(3, rpdata.message_increment);
		ps.setInt(4, -1);
		ps.setInt(5, rpdata.user_increment);
		ps.setInt(6, -1);//��Ϣ
		ps.setInt(7, rpdata.user_rel_increment);//�û���ϵ��������Ϣ
		try{
			ps.executeUpdate();
		}catch(SQLException ex){
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		return true;		
	}
	//Reportȫ����Ϣ��ÿ���ӻ㱨һ�Σ�����ÿ����Ϣ�㱨һ��
	public boolean doReportIncrementTotal(int message,int user,int userRel) throws SQLException{
		initiallize();
		ps.setBoolean(1, false);//State������������Ϣ1��������0��ȫ��
		ps.setInt(2,message );
		ps.setInt(3, 0);
		ps.setInt(4, user);
		ps.setInt(5, 0);
		ps.setInt(6, userRel);//��Ϣ
		ps.setInt(7, 0);//�û���ϵ��������Ϣ
		try{
			ps.executeUpdate();
		}catch(SQLException ex){
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			LogSys.crawlerServLogger.error("�㱨ȫ������ʧ��"+message+","+user+","+userRel+"");
			LogSys.crawlerServLogger.error(ex.fillInStackTrace());
		}
		LogSys.crawlerServLogger.info("�㱨ȫ�����ݳɹ�"+message+","+user+","+userRel+"");
		return true;
	}
	public static void main(String[] args){
		Connection con=(new DBFactory()).getConnection();
		String id="FacebookWEB";
		int message=0;
		int message_rel=1;
		int user=2;
		int user_rel=33;
		CrawlerServerReporter cr=new CrawlerServerReporter(id, con,message,message_rel,user,user_rel);
		for(int i=0;i<2;i++){
			ReportData rpdata=new ReportData(1,2,1,3,"NULL");
			try {
				cr.doReportIncrementByDataBase(rpdata);
				System.out.println("�㱨�ɹ�");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("�㱨ʧ��");
				e.printStackTrace();
			}
		}
	}
	

	
	

}
