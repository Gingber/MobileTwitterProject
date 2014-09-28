package com.ict.twitter.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import com.ict.twitter.analyser.beans.*;
import com.ict.twitter.plantform.LogSys;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class MulityInsertDataBase {
	private String ip="";
	private String user,password;
	private String databaseName;
	private final String encode="utf-8";
	private Connection connection;
	private PreparedStatement messageps=null;
	private PreparedStatement messagdetailps=null;
	
	
	private PreparedStatement userprofile=null;
	
	private PreparedStatement messagereteet=null;
	
	
	public MulityInsertDataBase(){
		String base = BasePath.getBase();
		ReadTxtFile rxf = new ReadTxtFile(base + "/config/clientproperties.ini");
		Vector<String> vector = rxf.read();
		for (String t : vector) {
			if(t.startsWith("http.dbaddressIP")){
				String res = t.substring(t.indexOf('=') + 1);
				this.ip= res;
			}			
			if (t.startsWith("http.dbusername")) {
				String res = t.substring(t.indexOf('=') + 1);
				this.user = res;
			} else if (t.startsWith("http.dbpassword")) {
				String res = t.substring(t.indexOf('=') + 1);
				this.password=res;
			} else if (t.startsWith("http.databasename")) {
				String res = t.substring(t.indexOf('=') + 1);
				this.databaseName=res;
			}
		}		
	}
	public static void main2(String[] args){
		MulityInsertDataBase mm =  new MulityInsertDataBase();
		TwiUser users[]= new TwiUser[200];
		for(int i=0;i<200;i++){
			TwiUser user=new TwiUser();
			user.setName(Integer.toString(i));
			user.setAliasName("i");
			user.setLocation("");
			user.setSummarized("");
			user.setProfileImageUrl("");
			user.setWebpageLink("");
			users[i]=user;
		}
		try {
			mm.insertIntoUser(users,"user");
		} catch (AllHasInsertedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String[] args){
		
	}
	
	public Connection getConnection(){
		
		try {
			if(connection!=null&&connection.isValid(10)){
				return connection;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + ip
					+ ":3306/" + databaseName
					+ "?useUnicode=true&continueBatchOnError=true&autoReconnect=true&characterEncoding=" + encode, user,
					password);
			connection.setAutoCommit(false);
		} catch (Exception e) {
			LogSys.nodeLogger.error("Error loading Mysql Driver!");
			e.printStackTrace();
		}
		LogSys.nodeLogger.debug("Success to connect to SQLServer  [IP:]"+ip+" [DBName:"+databaseName+"]");
		
		return connection;
	}
	
	public boolean insertIntoMessage(TimeLine[] timeline,String targetTableName) throws AllHasInsertedException{
		Connection con=this.getConnection();
		PreparedStatement insertWithTarget=null;
		int errorcount=0;
		try {
			con.setAutoCommit(false);
			insertWithTarget = con.prepareStatement("insert into"+" "+targetTableName+"(channel_id,message_id,title,user_id,create_time,crawl_time,other1,other2,is_reteet,origin_user_name,origin_message_id) values(?,?,?,?,?,?,?,?,?,?,?)");
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			insertWithTarget.clearBatch();
			for(int i=0;i<timeline.length;i++){
				insertWithTarget.setInt(1, 6);
				insertWithTarget.setString(2,timeline[i].getId());
				insertWithTarget.setString(3, timeline[i].getContent());
				insertWithTarget.setString(4, timeline[i].getAuthor());
				insertWithTarget.setString(5, timeline[i].getDate());
				insertWithTarget.setTimestamp(6, time);
				insertWithTarget.setString(7, Integer.toString(timeline[i].getTaskTrackID()));//timelineû�м����Ӧ��
				insertWithTarget.setString(8, Integer.toString(timeline[i].getMainTypeID()));//other1����ΪTaskTrackerID,other2����ΪMainTypeID
				insertWithTarget.setBoolean(9, timeline[i].isIs_reteet());
				insertWithTarget.setString(10, timeline[i].getOrigin_user_name());
				insertWithTarget.setString(11, timeline[i].getOrigin_tweet_id());
				insertWithTarget.addBatch();				
			}
			insertWithTarget.executeBatch();
			insertWithTarget.close();
			con.commit();	
		} catch( BatchUpdateException ex){
			//ex.printStackTrace();
			int[] res = ex.getUpdateCounts();
			checkBatch(res);
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("��ǰ���ݲ������"+errorcount+"��");
			return false;
		}		
		return true;
		
	}

	
	private boolean insertIntoMessage(TimeLine[] timeline) throws AllHasInsertedException{
		Connection con=this.getConnection();
		try {
			con.setAutoCommit(false);
			if(messageps==null){
				messageps = con.prepareStatement("insert into message(channel_id,message_id,title,user_id,create_time,crawl_time,other1,other2) values(?,?,?,?,?,?,?,?)");
			}
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			for(int i=0;i<timeline.length;i++){
				messageps.setInt(1, 6);
				messageps.setString(2,timeline[i].getId());
				messageps.setString(3, timeline[i].getContent());
				messageps.setString(4, timeline[i].getAuthor());
				messageps.setString(5, timeline[i].getDate());
				messageps.setTimestamp(6, time);
				messageps.setString(7, Integer.toString(timeline[i].getTaskTrackID()));//timelineû�м����Ӧ��
				messageps.setString(8, Integer.toString(timeline[i].getMainTypeID()));//other1����ΪTaskTrackerID,other2����ΪMainTypeID
				messageps.addBatch();				
			}
			messageps.executeBatch();
			con.commit();	
		} catch( BatchUpdateException ex){
			int[] res = ex.getUpdateCounts();
			checkBatch(res);
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	/*Insert Messagdeatil
	 * RelUser
	 * URL
	 * ImgURL
	*/
	public boolean insertIntoMessageDetail(MessageDetail[] MessageDetail){
		Connection con=this.getConnection();
		try {
			con.setAutoCommit(false);
			
			if(messagdetailps==null){
				messagdetailps = con.prepareStatement("insert into message_detail(`message_id`,`rel_ids`,`hash_tag_count`,`web_url`,`img_url`) values(?,?,?,?,?)");
			}
			messagdetailps.clearBatch();
			for(int i=0;i<MessageDetail.length;i++){
				if(MessageDetail[i].getMessageid()==null){
					LogSys.crawlerServLogger.debug("Insert into message-detail error with Messageid==null");
					continue;
				}
				messagdetailps.setString(1, MessageDetail[i].getMessageid());
				if(MessageDetail[i].getUsers()!=null){
					StringBuffer sb=new StringBuffer();
					for(int j=0;j<MessageDetail[i].getUsers().size();j++){
						sb.append(MessageDetail[i].getUsers().get(j));
					}
					messagdetailps.setString(2, sb.toString());
				}else{
					messagdetailps.setString(2, null);
				}
				messagdetailps.setInt(3, MessageDetail[i].getHash_tag_count());
				messagdetailps.setString(4,MessageDetail[i].getWeburl());
				messagdetailps.setString(5,MessageDetail[i].getImgurl());
				messagdetailps.addBatch();
				
			}
			messagdetailps.executeBatch();
			con.commit();	
		} catch( BatchUpdateException ex){
			int[] res = ex.getUpdateCounts();
			//ex.printStackTrace();
			try {
				checkBatch(res);
			} catch (AllHasInsertedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}
	
	public boolean insertIntoMessageReTeet(MessageReteet[] MessageReteet){
		Connection con=this.getConnection();
		try {
			con.setAutoCommit(false);
			
			if(messagereteet==null){
				messagereteet = con.prepareStatement("insert into message_reteet(`message_id`,`user_id`,`user_name`,`user_aliasname`,`content`,`date`) values(?,?,?,?,?,?)");
			}
			messagereteet.clearBatch();
			for(int i=0;i<MessageReteet.length;i++){
				MessageReteet mr=MessageReteet[i];
				messagereteet.setString(1, mr.getMessage_id());
				messagereteet.setString(2,mr.getUser_id());
				messagereteet.setString(3, mr.getUser_name());
				messagereteet.setString(4, mr.getUser_aliasname());
				messagereteet.setString(5, mr.getContent());
				messagereteet.setString(6, mr.getDate());
				messagereteet.addBatch();
				
			}
			messagereteet.executeBatch();
			con.commit();
			
		} catch( BatchUpdateException ex){
			int[] res = ex.getUpdateCounts();
			try {
				checkBatch(res);
			} catch (AllHasInsertedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean insertIntoUser(TwiUser[] users,String usertable) throws AllHasInsertedException{
		Connection con=this.getConnection();
		try {
			
			PreparedStatement userps = con.prepareStatement("insert into "+usertable+"(channel_id,user_id,real_name,crawl_time) values(?,?,?,?)");
			
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			userps.clearBatch();
			for(int i=0;i<users.length;i++){
				userps.setInt(1, 6);
				userps.setString(2,users[i].getName());
				userps.setString(3, users[i].getAliasName());
				userps.setTimestamp(4, time);		
				userps.addBatch();
				
			}
			userps.executeBatch();
			con.commit();
		}catch( BatchUpdateException ex){
			//ex.printStackTrace();
			int[] res = ex.getUpdateCounts();
			checkBatch(res);
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return true;
	}
	
	public boolean insertIntoUserRel(UserRelationship[] rels,String targetTableName) throws AllHasInsertedException{
		Connection con=this.getConnection();
		try {
			PreparedStatement userrelps = con.prepareStatement("insert into "+targetTableName+"(channel_id,user_id_A,user_id_B,link_type,crawl_time) values(?,?,?,?,?)");
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			for(int i=0;i<rels.length;i++){
				UserRelationship userel =rels[i];
				userrelps.setInt(1, 6);
				userrelps.setString(2, userel.getUser_A());
				userrelps.setString(3, userel.getUser_B());
				userrelps.setString(4, userel.getLinkType());
				userrelps.setTimestamp(5, time);
				userrelps.addBatch();
			}
			userrelps.executeBatch();
			con.commit();
		}catch(BatchUpdateException ex){
			int[] res = ex.getUpdateCounts();
			//ex.printStackTrace();
			checkBatch(res);			
		}catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
		
	}
	

	

	
	
	private void checkBatch(int[] updateCounts) throws AllHasInsertedException{
		int OKRows=0,NoInfoRows=0,FailRows=0;
		for(int i=0;i<updateCounts.length;i++){
			if (updateCounts[i] >= 0) {
				OKRows++;
		      } else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
		        NoInfoRows++;
		      } else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
		    	//System.out.println("["+i+"]Failed to execute; updateCount=Statement.EXECUTE_FAILED");
		        FailRows++;
		      }
		}
		System.out.println(String.format("Success:%d NoInfo:%d Failed:%d",OKRows,NoInfoRows,FailRows));
		if(FailRows==updateCounts.length){
			throw new AllHasInsertedException("���е����ݶ��������");
		}
	}
	
	public void insertIntoUserProfile(UserProfile profile,String tableName) throws AllHasInsertedException{
		Connection con=this.getConnection();
		
		try {
			userprofile=con.prepareStatement("INSERT INTO "+tableName+"(user_id,user_name,user_aliasname,profile_url,profile_image,tweet,following,follower,crawl_time,location,intruduction,is_alive) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			userprofile.setString(1, profile.getUser_id());
			userprofile.setString(2, profile.getUser_name());
			userprofile.setString(3, profile.getUser_aliasname());
			userprofile.setString(4, profile.getPicture_url());
			userprofile.setBytes(5, profile.getPicturedata());
			userprofile.setInt(6, profile.getTweet());
			userprofile.setInt(7, profile.getFollowing());
			userprofile.setInt(8, profile.getFollower());
			userprofile.setTimestamp(9, time);
			userprofile.setString(10, profile.getLocation());
			userprofile.setString(11,profile.getSelfintroduction());
			userprofile.setBoolean(12, profile.isIs_alive());
			userprofile.executeUpdate();
			con.commit();
			userprofile.close();
		}catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex){
			System.out.println("�ظ�����");
			throw new AllHasInsertedException("�ظ�����");
		}
		catch(SQLException ex){
			System.out.println("Errorcode"+ex.getErrorCode());
			ex.printStackTrace();
		}catch(Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Other Exception");
			e.printStackTrace();
		}
	}
	
	public void UpdateUserProfileByName(UserProfile profile){
		Connection con=this.getConnection();
		
		try {
			
			PreparedStatement userprofile=con.prepareStatement("update user_profile set user_id=?,user_aliasname=?,profile_url=?,profile_image=?,tweet=?,following=?,follower=?,crawl_time=?,location=?,intruduction=? where user_name=?");
			
			java.sql.Timestamp time = new Timestamp(System.currentTimeMillis());
			userprofile.setString(11, profile.getUser_name());
			
			userprofile.setString(1, profile.getUser_id());
			userprofile.setString(2, profile.getUser_aliasname());
			userprofile.setString(3, profile.getPicture_url());
			userprofile.setBytes(4, profile.getPicturedata());
			userprofile.setInt(5, profile.getTweet());
			userprofile.setInt(6, profile.getFollowing());
			userprofile.setInt(7, profile.getFollower());
			userprofile.setTimestamp(8, time);
			userprofile.setString(9, profile.getLocation());
			userprofile.setString(10,profile.getSelfintroduction());
			userprofile.executeUpdate();
			con.commit();
		}catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex){
			System.out.println("����Profile");
		}
		catch(SQLException ex){
			System.out.println("Errorcode"+ex.getErrorCode());
			ex.printStackTrace();
		}catch(Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Other Exception");
			e.printStackTrace();
		}
	}
	
	public void getDatafromprofile(){
		Connection con=this.getConnection();
		try{
			Statement sta=con.createStatement();
			ResultSet rs=sta.executeQuery("select profile_image from user_profile limit 0,1");
			rs.next();
			InputStream ins=rs.getBinaryStream(1);
			
			File f=new File("Output/Twitter/take_picture_fromdatabase.jpg");
			if(!f.exists())
				f.createNewFile();
			FileOutputStream fos=new FileOutputStream(f);
			byte[] buffer=new byte[1000];
			int length=0;
			while((length=ins.read(buffer))>0){
				fos.write(buffer, 0, length);
			}
			fos.close();
			ins.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	
	
	
}
