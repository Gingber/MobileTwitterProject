//���ݿ������ļ�

package com.ict.twitter.tools;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import com.ict.twitter.Report.NodeReporter;
import com.ict.twitter.Report.ReportDataType;
import com.ict.twitter.plantform.LogSys;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class DbOperation {
	
	

	private static String ip = "127.0.0.1";

	private static String driver = "com.mysql.jdbc.Driver";

	private static String user = "";

	private static String password = "";

	private static String databaseName = "http_twitter";

	private static String encode = "utf-8";
	private static int patchCount=0;
	private Connection connect = null;
	Statement stmt = null;
	public static int connectionCount;
	
	public DbOperation() {
		String base = BasePath.getBase();
		ReadTxtFile rxf = new ReadTxtFile(base + "/config/clientproperties.ini");
		Vector<String> vector = rxf.read();
		for (String t : vector) {
			if(t.startsWith("http.dbaddressIP")){
				String res = t.substring(t.indexOf('=') + 1);
				DbOperation.ip = res;
			}
			
			if (t.startsWith("http.dbusername")) {
				String res = t.substring(t.indexOf('=') + 1);
				DbOperation.user = res;
			} else if (t.startsWith("http.dbpassword")) {
				String res = t.substring(t.indexOf('=') + 1);
				DbOperation.password = res;
			} else if (t.startsWith("http.databasename")) {
				String res = t.substring(t.indexOf('=') + 1);
				DbOperation.databaseName = res;
			}
		}
		this.reginster();
		this.conDB();
	}

	/**
	 * ����mysql����
	 */
	public void reginster() {
		try {
			Class.forName(driver); // ����MYSQL JDBC��������

		} catch (Exception e) {
			System.out.print("Error loading Mysql Driver!");
			e.printStackTrace();
		}
	}

	/**
	 * �������ݿ�
	 * 
	 * @return ���Ӷ���
	 */

	public Connection conDB(){
		Connection connect = null;
		try {
			connect = DriverManager.getConnection("jdbc:mysql://" + ip
					+ ":3306/" + databaseName
					+ "?useUnicode=true&continueBatchOnError=true&autoReconnect=true&characterEncoding=" + encode, user,
					password);
			// ����URLΪ jdbc:mysql//��������ַ/���ݿ���
			// �����2�������ֱ��ǵ�½�û���������

		} catch (Exception e) {
			System.out.print("Fail to access DataBase!");
			e.printStackTrace();
			System.exit(-1);
			
		}
		this.connect = connect;
		return connect;
	}

	public void createStmt() {
		try {
			stmt = this.connect.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Connection conDB(String ip, String databaseName, String user,String password){
		connectionCount++;
		LogSys.nodeLogger.info("������һ�����ӣ��ܹ����������ǣ���"+connectionCount);
		Connection connect = null;
		try {
			connect = DriverManager.getConnection("jdbc:mysql://" + ip
					+ ":3306/" + databaseName
					+ "?rewriteBatchedStatements=true&continueBatchOnError=true&useUnicode=true&characterEncoding=" + encode, user,
					password);
			// ����URLΪ jdbc:mysql//��������ַ/���ݿ���
			// �����2�������ֱ��ǵ�½�û���������
			System.out.println("Success connect Mysql server!");
			stmt = this.connect.createStatement();
		} catch (Exception e) {
			System.out.print("get data error!");
			e.printStackTrace();
		}
		this.connect = connect;
		return connect;
	}

	public boolean insertWithoutBatch(String insertSql){
		try {
			if(this.connect==null||this.connect.isClosed()){
				this.connect=conDB();
			}
			if(stmt==null||stmt.isClosed()){
				stmt=this.connect.createStatement();
			}
			//��������
			if(UTF8Filter.IsAllUTF8(insertSql)){
				insertSql=UTF8Filter.CleanLeaveUTF8(insertSql);
			}
			stmt.executeUpdate(insertSql);
		}
		catch (MySQLIntegrityConstraintViolationException ex) {
			//LogSys.nodeLogger.debug("�����ظ���SQL���Ϊ��"+insertSql);
			return false;
		} catch(com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException ex){
			LogSys.nodeLogger.error("SQL�к��д����ʾ����"+insertSql);
		}catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogSys.nodeLogger.error("����Sql����ǣ�"+insertSql);
			System.out.println(insertSql);
			return false;
		}catch(Exception ex){
			return false;
			
		}
		return true;
	}
	
	public boolean insertRightNow(){
		try {
			if(this.connect==null||this.connect.isClosed()){
				this.connect=conDB();
			}
			if(stmt==null||stmt.isClosed()){
				stmt=this.connect.createStatement();
			}
			stmt.executeBatch();			
			return true;
		}catch(SQLException e){
			return false;
		}
	}
	
	/*
	 * modify by shanjixi
	 * add batch mod ,if it does not work please roll back to one insert by one.
	 */
	public boolean insert(String insertSql) {
		try {
			if(this.connect==null||this.connect.isClosed()){
				this.connect=conDB();
			}
			if(stmt==null||stmt.isClosed()){
				stmt=this.connect.createStatement();
			}
			//��������
			if(UTF8Filter.IsAllUTF8(insertSql)){
				insertSql=UTF8Filter.CleanLeaveUTF8(insertSql);
			}
			stmt.addBatch(insertSql);
			if(patchCount>=20){
				//connect.setAutoCommit(true);
				patchCount=0;
				stmt.executeBatch();
				stmt.clearBatch();
				//connect.commit();
				//connect.setAutoCommit(true);
			}else{
				patchCount++;
				
			}
			
			return true;
		}catch (BatchUpdateException e){
			System.err.println("����ִ�е�Batch/100");
			System.err.println("Error Code "+e.getErrorCode());
			if(e.getErrorCode()==1062){
				LogSys.nodeLogger.debug("�����ظ�with SQL("+insertSql+")");
				return false;
			}
			System.err.println("SqlState "+e.getSQLState());
			System.err.println("Message "+e.getMessage());
			e.printStackTrace();
			return false;
		} 
		catch (MySQLIntegrityConstraintViolationException ex) {
			LogSys.nodeLogger.debug("�����ظ���SQL���Ϊ��"+insertSql);
			return false;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LogSys.nodeLogger.error("����Sql����ǣ�"+insertSql);
			System.out.println(insertSql);
			return false;
		}		
	}
	public boolean insert(String insertSql,ReportDataType reportType){
		boolean istrue=this.insert(insertSql);
		if(istrue){
			NodeReporter.count(reportType);
		}		
		return istrue;
	}
	

	public void close() {
		if (this.stmt != null) {
			try {
				this.stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			this.stmt = null;
		}
		if (this.connect != null) {
			try {
				connect.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connect = null;
		}

	}

	// ɾ�����ݿ�������ж���
	public void clearDB() {
		insert("delete from message;");
		insert("delete from message_relationship;");
		insert("delete from user;");
		insert("delete from user_relationship;");
	}
	public void closeConn() {
		// TODO Auto-generated method stub
		this.close();
	}

	public void closeStmt() {
		// TODO Auto-generated method stub
		this.close();
	}
	public static void main(String[] args){
		DbOperation dbo=new DbOperation();
		Connection con=dbo.conDB();	
		
		dbo.insert("insert into  message(channel_id,message_id,title,crawl_time) values(1,1000001,'TESTTEST','2011-01-01 00:00:00')");
		dbo.insert("insert into  message(channel_id,message_id,title,crawl_time) values(1,1000002,'TESTTEST','2011-01-01 00:00:00')");
		for(int i=1;i<=7;i++){
			dbo.insert("insert into  message(channel_id,message_id,title,crawl_time) values(1,1000000,'TESTTEST','2011-01-01 00:00:00')");

		}
		dbo.insert("insert into  message(channel_id,message_id,title,crawl_time) values(1,1000009,'TESTTEST','2011-01-01 00:00:00')");
		
		
	}
	public Connection GetConnection(){
		try {
			if(this.connect!=null&&this.connect.isValid(20)){
				return this.connect;
			}else{
				return this.conDB();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return this.conDB();
		}
	}
}
