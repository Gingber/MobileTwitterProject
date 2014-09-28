package com.ict.twitter.hbase;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.ict.twitter.analyser.beans.TimeLine;

public class MessageTwitterHbase extends TwitterHbase{
	public MessageTwitterHbase(String tableName) {
		super(tableName);
		SetFamilyNameAndColumns();
	}
	public boolean InsertIntoTable(TimeLine[] allTimeLine) throws IOException{
		HTable table=new HTable (conf,tableName);
		table.setAutoFlush(false);
		for(int i=0;i<allTimeLine.length;i++){
//			Put put=new Put(Bytes.toBytes(allTimeLine[i].id));
			Put put=new Put(Exchange(allTimeLine[i],"id",null));
			for(int j=0;j<familyNames.length;j++){
				byte[] familyName=Bytes.toBytes(familyNames[j]);
				if(!columnsmap.containsKey(familyNames[j])){//���Զ����Ż�
					put.add(familyName, null, Exchange(allTimeLine[i],familyNames[j],null));
				}else{
					String[] allcol=columnsmap.get(familyNames[j]);
					for(int k=0;k<allcol.length;k++){
						String column=allcol[k];
						put.add(familyName,Bytes.toBytes(column),Exchange(allTimeLine[i],familyNames[j],column));
					}
				}				
			}
			table.put(put);
		}
		table.flushCommits();
		table.close();
		return true;
	}
	
	public byte[] Exchange(TimeLine timeline,String currentFamilyName,String columnname){
		String result="";
		switch(currentFamilyName){
			case "id":
				String msgId=timeline.getId();
				String createTime=timeline.getDate();
				result=Convert(msgId, createTime);
				break;
			case "user_id":
				result=timeline.getAuthor();
				break;
			case "title":
				result=timeline.getContent();
				break;
			case "date":
				result=timeline.getDate();
				if(columnname.equalsIgnoreCase("create_time")){
					result=timeline.getDate();
				}else if(columnname.equalsIgnoreCase("crawl_time")){
					result=sdf.format(new Date());
				}
				break;
			case "link":
				result=timeline.getLink();
				break;
			case "is_reteet":
				if(timeline.is_reteet){
					result="1";
				}else{
					result="0";
				}
				break;
			case "origin_user_name":
				result=timeline.getOrigin_user_name();
				break;
			case "origin_message_id":
				result=timeline.getOrigin_tweet_id();
				break;
			case "detail":{
				if(columnname.equalsIgnoreCase("retw_count")){
					result=Integer.toBinaryString(timeline.getReTWcount());
				}else if(columnname.equalsIgnoreCase("reply_count")){
					result=Integer.toBinaryString(timeline.getReplyCount());
				}
				break;
			}
				
		}
		if(result==null){
			return null;
		}
		return Bytes.toBytes(result);
	}
	@Override
	protected void SetFamilyNameAndColumns() {
		familyNames=new String[]{"user_id","title","date","link","detail","is_reteet","origin_user_name","origin_message_id"};
		columnsmap=new HashMap<String,String[]>();
		String[] dateColumn=new String[]{"create_time","crawl_time"};
		String[] detailColumn=new String[]{"retw_count","reply_count"};
		columnsmap.put("date", dateColumn);
		columnsmap.put("detail", detailColumn);
	}
	
	private String Convert(String id, String creatTime){
		SimpleDateFormat sdf=new SimpleDateFormat("YYYYMMddHHmmss");
		String date = creatTime.replaceAll("[-: ]", "");
		Long longId=Long.parseLong(id);
		String t=String.format("%14s-%018d-00000000000www.twitter.com",date,longId);
		System.out.println(t);
		return t;
	}
	public static void main(String[] args){
		MessageTwitterHbase mt=new MessageTwitterHbase("message");
		mt.Convert("100389086652665856" , "2013-12-31 21:11:39");
	}

}
