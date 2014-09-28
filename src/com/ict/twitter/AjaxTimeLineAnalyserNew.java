package com.ict.twitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ict.facebook.TimeLine.TimeLineAnalyser;
import com.ict.twitter.AjaxAnalyser.AnalyserCursor;
import com.ict.twitter.analyser.beans.TimeLine;
import com.ict.twitter.analyser.beans.TwiUser;
import com.ict.twitter.hbase.MessageTwitterHbase;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;
import com.ict.twitter.tools.AllHasInsertedException;
import com.ict.twitter.tools.MulityInsertDataBase;
import com.ict.twitter.tools.ReadTxtFile;
import com.sun.org.apache.bcel.internal.generic.SWAP;

public class AjaxTimeLineAnalyserNew extends AjaxTimeLineAnalyser{
	boolean isdebug=false;
	//TEMPLATE
	private String HeadClassValue="ProfileTweet-header u-cf u-textMute";
	private String ContentClassValue="ProfileTweet-text js-tweet-text u-dir";
	private String FootClassValue="ProfileTweet-actionList u-cf js-actions ProfileTweet-actionList--inlineFooter";
	
	private String TimeClassValue="js-short-timestamp";
	private String UrlClassValue = "ProfileTweet-timestamp";
	
	private int CurrentUserTweetCount=0;
	
	
	
	public AjaxTimeLineAnalyserNew(MulityInsertDataBase batchdb,Task task) {
		super(batchdb,task);
	}
	public static void main(String[] args){
		ReadTxtFile rtf=new ReadTxtFile("UsefulFile/Twitter/2014-04-29 10-20-50-385[wenyunchao-TimeLine-0-0].txt");
		String content=rtf.readALL();
		System.out.println(content);
		JSONParser parser = new JSONParser();
		MulityInsertDataBase mulityDB=new MulityInsertDataBase();
		Map<?, ?> json = null;
		try {
			json = (Map<?, ?>) parser.parse(content);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String html=(String) json.get("items_html");
		Vector<TwiUser> users =new Vector<TwiUser>();
		Task t=new Task(TaskType.TimeLine, "wenyunchao");
		AjaxTimeLineAnalyserNew atl=new AjaxTimeLineAnalyserNew(mulityDB,t);
		try {
			atl.doAnalyser(html, users);
		} catch (AllHasInsertedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	@Override
	public AnalyserCursor doAnalyser(String src,Vector<TwiUser> users) throws AllHasInsertedException{

		AnalyserCursor resultCursor=new AnalyserCursor();
		Document doc=Jsoup.parse(src, "/");
		Elements allElements=doc.getElementsByAttributeValueStarting("class","StreamItem js-stream-item");
		Vector<TimeLine> vector= new Vector<TimeLine>();
		CurrentUserTweetCount=allElements.size();
		for(Element t:allElements){
			String content_result=null;
			String time_result=null;
			String tweet_id_result=null;
			String user_id_result=null;
			String user_name_result=null;
			String user_alias_name_result=null;
			boolean isReteet=false;
			String originUserName=null;
			String originUserAliasName=null;
			String originMessageID=null;
			String tweetUrl = null;
			
			
			Elements contents=t.getElementsByAttributeValueStarting("class", ContentClassValue);
			Element content=(contents.size()>0?contents.get(0):null);
			if(content!=null){
				content_result=content.text();
				List<String> relUser=getUserID(content,users);
			}
			
			Elements times=t.getElementsByAttributeValueStarting("class", TimeClassValue);
			Element time=(times.size()>0?times.get(0):null);
			if(time!=null){
				time_result=timeTrans.Convert(time.attr("data-time"));
			}
			
			Elements urls = t.getElementsByAttributeValueStarting("class", UrlClassValue);
			Element url = (urls.size()>0?urls.get(0):null);
			if(url != null) {
				tweetUrl = "https://twitter.com" + url.attr("href");
			}
			
			Element MetaData=t.children().first();
			if(MetaData!=null){
				tweet_id_result=MetaData.attr("data-tweet-id");
				user_name_result=MetaData.attr("data-screen-name");
				user_alias_name_result=MetaData.attr("data-name");
				String retweeterName=MetaData.attr("data-retweeter");
				if(retweeterName.length()==0){
					isReteet=false;
				}else{
					
					originUserName=user_name_result;
					originUserAliasName=user_alias_name_result;
					user_name_result=retweeterName;
					user_alias_name_result="ת����";
					
					//CurrentID=460598753812959232
					originMessageID=tweet_id_result;
					tweet_id_result=MetaData.attr("data-retweet-id");
					isReteet=true;
				}
				TimeLine timeline=new TimeLine();
				timeline.setAuthor(user_name_result);
				timeline.setContent(content_result);
				timeline.setDate(time_result);
				timeline.setId(tweet_id_result);
				timeline.setIs_reteet(isReteet);
				timeline.setLink(tweetUrl);
				timeline.setOrigin_user_name(originUserName);
				timeline.setOrigin_tweet_id(originMessageID);
				int[] result=FooterAnalyse(t.getElementsByAttributeValueStarting("class","ProfileTweet-actionList u-cf js-actions").first());
				timeline.setReplyCount(result[0]);
				timeline.setReTWcount(result[1]);
				timeline.setFavoriteCount(result[2]);
				vector.add(timeline);
				resultCursor.lastID=timeline.getId();//��Ǳ�����������ID���������һ�ֵı���
			}
			
		}//end for(Element t: all)
		System.out.println("��ҳ���ȡ��������"+vector.size());
		TimeLine[] timelines = new TimeLine[vector.size()];
		vector.toArray(timelines);
		for(int i=0;i<timelines.length;i++){
			timelines[i].setMainTypeID(task.getMainTypeID());
			timelines[i].setTaskTrackID(task.getTaskTrackID());
		}
		if(this.HbaseEnable){
			MessageTwitterHbase mth=(MessageTwitterHbase)super.hbase;
			try {
				mth.InsertIntoTable(timelines);
				System.out.println("�°�TW_д��Message Hbase�ɹ�");
			} catch (IOException e) {
				System.err.println("�°�TW_д��Message Hbaseʧ��");
				e.printStackTrace();
			}
		}
		if(task.getTargetTableName()!=null&&!(task.getTargetTableName().equalsIgnoreCase("null"))){
			super.batchdb.insertIntoMessage(timelines,task.getTargetTableName());
		}else{
			super.batchdb.insertIntoMessage(timelines,"message");
		}
		resultCursor.size=vector.size();
		return resultCursor;
		
		
		
		
	}
	public int[] FooterAnalyse(Element footerElement){
		int[] footer=new int[3];
		Elements all=footerElement.getElementsByAttribute("data-tweet-stat-count");
		for(int i=0;i<all.size();i++){
			Element t=all.get(i);
			if(t!=null){
				footer[i]=Integer.parseInt(t.attr("data-tweet-stat-count"));
			}
		}
		return footer;
		
	}

}
