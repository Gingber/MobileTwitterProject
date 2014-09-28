package com.ict.twitter.CrawlerSchedul;

import java.sql.SQLException;

import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.DAO.DBInputTaskDAO;
import com.ict.twitter.plantform.LogSys;
import com.mongodb.DBCollection;

public class PlantScheduler extends BasicScheduler{
	final int  MaxID=17;
	public PlantScheduler(CrawlerServer _crawlserver) {
		super(_crawlserver);
		// TODO Auto-generated constructor stub
	}
	
	
	/*1:�����Ϣ�����Ƿ�Ϊ�գ������Ϊ�յĻ����ء�
	 *2:��ȡ���еĹؼ��û�������ϴ���ȡʱ��
	 * 
	 * @see com.ict.twitter.CrawlerSchedul.BasicScheduler#run()
	 */
	
	public void ResetInputTaskTable(){
		DBInputTaskDAO inputtask=new DBInputTaskDAO();
		try {
			inputtask.InitTable();
			CrawlServerScheduler cs=new CrawlServerScheduler(this.crawlserver);
			cs.checkUserInsert();
			LogSys.crawlerServLogger.info("��ʱ��ˢ��InputTask��");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@Override
	public void run() {
		ResetInputTaskTable();
		
	}
	

}
