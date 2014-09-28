package com.ict.twitter;

import java.util.Map;

import com.ict.twitter.analyser.beans.UserProfile;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ict.twitter.task.beans.Task;
import com.ict.twitter.tools.MulityInsertDataBase;
import com.ict.twitter.tools.ReadTxtFile;

public class AjaxProfileAnalyser extends AjaxAnalyser {
	
	
	
	

	public AjaxProfileAnalyser(MulityInsertDataBase batchdb, Task task) {
		super(batchdb, task);
		// TODO Auto-generated constructor stub
	}
	public void doAnylyze(String content,UserProfile userprofile){
		String picture_url="";
		int tweet=0,following=0,follower=0;
		String location=null,selfIntroductionstr=null;
		
		Document doc=Jsoup.parse(content, "/");
		Elements userNamediv=doc.getElementsByAttributeValueContaining("class","fullname js-action-profile-name show-popup-with-id");
		if(userNamediv.size()>0){
			userprofile.setUser_aliasname(userNamediv.get(0).text());
		}
		
		Elements picture=doc.getElementsByAttributeValueContaining("class", "profile-picture u-floatLeft media-thumbnail js-nav js-tooltip");
		if(picture.size()>0){
			picture_url=picture.get(0).child(0).attr("src");
		}else{
			picture_url="null";
		}
		Elements locationElements=doc.getElementsByAttributeValue("class", "location profile-field");
		if(locationElements!=null&&locationElements.size()>0){
			location=locationElements.first().ownText();
		}else{
			location="null";
		}
		Elements selfIntroduction=doc.getElementsByAttributeValue("class", "bio profile-field");
		if(selfIntroduction!=null&&selfIntroduction.size()>0){
			selfIntroductionstr=selfIntroduction.first().ownText();
		}else{
			selfIntroductionstr="null";
		}
		
		Elements CountElement=doc.getElementsByAttributeValue("class", "default-footer");
		if(CountElement!=null&&CountElement.size()>0){
			Element target=CountElement.first();
			tweet=this.getCount(target, "tweet_stats");
			following=this.getCount(target, "following_stats");
			follower=this.getCount(target, "follower_stats");
		}else{
			tweet=-1;following=-1;follower=-1;
		}
		userprofile.setTweet(tweet);
		userprofile.setFollower(follower);
		userprofile.setFollowing(following);
		userprofile.setPicture_url(picture_url);
		userprofile.setLocation(location);
		userprofile.setSelfintroduction(selfIntroductionstr);
		
	}
	private int getCount(Element ele,String dataElementTerm) throws NumberFormatException{
		Elements allElements = ele.getElementsByAttributeValue("data-element-term",dataElementTerm);
		if(allElements!=null&&allElements.size()>0){
			Element target=allElements.first();
			String count=target.getElementsByTag("strong").attr("title");
			count=count.replaceAll(",", "");
			int res=0;
			res=Integer.parseInt(count);
			
			return res;
		}
		return -1;
	}
	
	
	public static void main(String[] args) {
		JSONParser parser = new JSONParser();
		ReadTxtFile rtf=new ReadTxtFile("2014-02-27 13-57-01");
		String content=rtf.readALL();
		try {
			Map map=(Map) parser.parse(content);
			String html=(String)map.get("html");
			AjaxProfileAnalyser ajaxprofileana=new AjaxProfileAnalyser(null, null);
			UserProfile up=new UserProfile();
			ajaxprofileana.doAnylyze(html, up);
			System.out.println(up.toString());
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
