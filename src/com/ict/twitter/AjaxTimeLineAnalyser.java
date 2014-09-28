package com.ict.twitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import com.ict.twitter.analyser.beans.MessageDetail;
import com.ict.twitter.analyser.beans.TimeLine;
import com.ict.twitter.analyser.beans.TwiUser;
import com.ict.twitter.hbase.MessageTwitterHbase;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.tools.AllHasInsertedException;
import com.ict.twitter.tools.DbOperation;
import com.ict.twitter.tools.MulityInsertDataBase;
import com.ict.twitter.tools.ReadTxtFile;


public class AjaxTimeLineAnalyser extends AjaxAnalyser{
	boolean isdebug=false;
	
	public AjaxTimeLineAnalyser(MulityInsertDataBase batchdb,Task task) {
		
		super(batchdb,task);
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args){
		ReadTxtFile rtf=new ReadTxtFile("2014-02-25 21-23-38");
		String content=rtf.readALL();
		System.out.println(content);
		JSONParser parser = new JSONParser();
		Map<?, ?> json = null;
		try {
			json = (Map<?, ?>) parser.parse(content);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String html=(String) json.get("items_html");
		Vector<TwiUser> users =new Vector<TwiUser>();
		AjaxTimeLineAnalyser atl=new AjaxTimeLineAnalyser(null,null);
		try {
			atl.doAnalyser(html, users);
		} catch (AllHasInsertedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public AnalyserCursor doAnalyser(String src,Vector<TwiUser> users) throws AllHasInsertedException{
		Document doc=Jsoup.parse(src, "/");
		AnalyserCursor result=new AnalyserCursor();
		//doc.getelementsby
		Elements twitterMessages=doc.getElementsByAttributeValueStarting("class", "js-stream-item stream-item stream-item");
		if(twitterMessages.size()<=2){
			System.out.println("[Warning] twitter count"+twitterMessages.size());
		}
		Vector<TimeLine> vector= new Vector<TimeLine>();
		Vector<MessageDetail> msgdetailvector=new Vector<MessageDetail>();
		for(Element t:twitterMessages){
			try{
				Element content=t.getElementsByAttributeValueStarting("class", "js-tweet-text tweet-text").first();
				Element time=t.getElementsByAttributeValueStarting("class", "_timestamp js-short-timestamp").first();
				String timeStr=time.attr("data-time");
				timeStr=timeTrans.Convert(timeStr);
				Element firstDiv=t.children().first();
				String tweet_id=firstDiv.attr("data-tweet-id");
				String user_name=firstDiv.attr("data-screen-name");	
				
				result.lastID=tweet_id;
				TimeLine timeline=new TimeLine(tweet_id,user_name,content.text(),timeStr);
				//����ת����Ϊ��
				ReTweetCrawl(firstDiv,timeline);
				//����ת����Ϊ���
				List<String> relUser=getUserID(content,users);
				String url=GetURL(content);
				int hashTagCount=GetHashTagCount(content);
				String PicUrl=GetPicURL(content);
				if(relUser!=null||url!=null||PicUrl!=null){
					MessageDetail msgdetail=new MessageDetail();
					msgdetail.setMessageid(timeline.getId());
					msgdetail.setWeburl(url);
					msgdetail.setUsers(relUser);
					msgdetail.setImgurl(PicUrl);
					msgdetail.setHash_tag_count(hashTagCount);
					msgdetailvector.add(msgdetail);
					
				}
				vector.add(timeline);
			}catch(NullPointerException ex){
				ex.printStackTrace();
			}			
		}
		System.out.println("��ҳ���ȡ��������"+vector.size());
		TimeLine[] timelines = new TimeLine[vector.size()];
		vector.toArray(timelines);
		if(isdebug){
			for(int i=0;i<timelines.length;i++)
				timelines[i].show();
		}
		for(int i=0;i<timelines.length;i++){
			timelines[i].setMainTypeID(task.getMainTypeID());
			timelines[i].setTaskTrackID(task.getTaskTrackID());
		}
		
		MessageDetail[] ss=msgdetailvector.toArray(new MessageDetail[msgdetailvector.size()]);
		if(this.HbaseEnable){
			MessageTwitterHbase mth=(MessageTwitterHbase)super.hbase;
			try {
				mth.InsertIntoTable(timelines);
				System.out.println("�ɰ�TW_д��Message Hbase�ɹ�");
			} catch (IOException e) {
				System.err.println("�ɰ�TW_д��Message Hbaseʧ��");
				e.printStackTrace();
			}
		}
		super.batchdb.insertIntoMessageDetail(ss);
		if(task.getTargetTableName()!=null&&!(task.getTargetTableName().equalsIgnoreCase("null"))){
			super.batchdb.insertIntoMessage(timelines,task.getTargetTableName());
		}else{
			super.batchdb.insertIntoMessage(timelines,"message");
		}
		
		result.size=twitterMessages.size();
		return result;
		
	}
	protected List<String> getUserID(Element content,Vector<TwiUser> users){
		if(content==null){
			return null;
		}
		List<String> res=new ArrayList<String>();
		Elements eles=content.getElementsByAttributeValue("class","twitter-atreply pretty-link");
		for(Element t:eles){
			String userid=(t.attr("href").replaceFirst("/", "@"));
			users.add(new TwiUser(userid, userid, 0, 0));
			res.add(userid);
		}
		return res;
	}
	//����Է�ԭ�����ĵ��ж�
	//<span class="js-retweet-text">�� <a class="pretty-link js-user-profile-link" href="/Kyry4enkoKatia" data-user-id="2304882066"><b>Kateryna Kyrychenko</b></a> ת��</span>
	private boolean ReTweetCrawl(Element tweet,TimeLine timeline){
		boolean flag=false;
		
		if(tweet!=null){
			if(tweet.attributes().hasKey("data-retweet-id")){
				flag=true;
				timeline.setId(tweet.attr("data-retweet-id"));
				timeline.setOrigin_tweet_id(tweet.attr("data-tweet-id"));
				timeline.setOrigin_user_name(tweet.attr("data-screen-name"));
				timeline.setIs_reteet(true);
				Element t=tweet.select("a[class=pretty-link js-user-profile-link]").first();
				if(t!=null){
					String user_name=t.attr("href");
					if(user_name.indexOf('/')!=-1){
						user_name=user_name.substring(user_name.indexOf('/')+1);
					}
					timeline.setAuthor(user_name);//�˴��൱��ת�����û���˭
				}		
				
			}

		}else{
			flag=false;
			timeline.setIs_reteet(false);
		}
		return flag;
	}
	
	protected String GetURL(Element content){
		if(content==null){
			return null;
		}
		Elements t=content.getElementsByAttributeValue("class", "twitter-timeline-link");
		if(t.size()>0){
			String url=t.first().attr("data-expanded-url");
			return url;
		}else{
			return null;
		}		
	}
	protected String GetPicURL(Element content){
		Elements t=content.getElementsByTag("img");
		if(t.size()>0){
			String url=t.first().attr("src");
			return url;
		}else{
			return null;
		}
	}
	protected int GetHashTagCount(Element content){
		Elements HashTags=content.getElementsByAttributeValueStarting("class", "twitter-hashtag");
		if(HashTags!=null){
			return HashTags.size();
		}
		return 0;
	}
	
}
