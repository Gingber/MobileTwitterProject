package com.ict.twitter.CrawlerSchedul;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.tools.DbOperation;

public class ReportTotalScheduler extends TimerTask{
	private CrawlerServer crawlserver;
	private DbOperation dbo;
	public ReportTotalScheduler(CrawlerServer _crawlserver){
		this.crawlserver=_crawlserver;
		dbo=new DbOperation();
	}

	@Override
	public void run() {
		//��ȡ��ǰ��Ųɼ����ݵ����ݿ���Ŀ�� message,user,userRel
		//ִ�����ݿ�������������ݿ���
		Connection con=dbo.GetConnection();
		ResultSet rs=null;
		try{
			Statement sta=con.createStatement();
			rs=sta.executeQuery("select max(message.id)-min(message.id),max(user.id)-min(user.id),max(user_relationship.id)-min(user_relationship.id) from message,user,user_relationship;");
			if(rs.next()){
				int message=rs.getInt(1);
				int user=rs.getInt(2);
				int userRel=rs.getInt(3);
				if(crawlserver.crawlReporter!=null){
					boolean flag=crawlserver.crawlReporter.doReportIncrementTotal(message,user, userRel);
					if(flag){
						LogSys.crawlerServLogger.debug(String.format("��ʱ���㱨ȫ�����ݳɹ�:message:%d,user:%d,userRel:%d",message,user,userRel));
					}
				}
			}
		}catch(SQLException ex){
			ex.printStackTrace();
		}finally{
			try {
				rs.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}

}
