package com.ict.twitter;

import java.util.Map;
import java.util.Vector;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ict.twitter.AjaxAnalyser.AnalyserCursor;
import com.ict.twitter.Report.ReportData;
import com.ict.twitter.analyser.beans.TwiUser;
import com.ict.twitter.hbase.MessageTwitterHbase;
import com.ict.twitter.hbase.TwitterHbase;
import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;
import com.ict.twitter.tools.AllHasInsertedException;
import com.ict.twitter.tools.DbOperation;
import com.ict.twitter.tools.MulityInsertDataBase;


public class AjaxSearchCrawl extends AjaxCrawl{

	/**
	 * @param args
	 *        304600005539422208
	 * max_id=304600005539422207
	 * 
	 * https://twitter.com
	 */
	String baseURL="/i/search/timeline?q='%s'&src=typd&f=realtime&include_available_features=1&latent_count=0&include_entities=1&last_note_ts=0&oldest_unread_id=0";
	String max_id_str="&scroll_cursor=%s";
	DefaultHttpClient httpclient;
	private JSONParser parser = new JSONParser();
	
	public AjaxSearchCrawl(DefaultHttpClient _httpclient,DbOperation dboperation){
		super.dboperation=dboperation;
		this.httpclient=_httpclient;
	}
	public static void main(String[] args) {
		TwitterClientManager cm=new TwitterClientManager();
		DefaultHttpClient httpclient = cm.getClientByIpAndPort("192.168.120.67",8087);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 100000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 100000); 
		TwitterLoginManager lgtest=new TwitterLoginManager(httpclient);
		lgtest.doLogin();
		AjaxSearchCrawl test=new AjaxSearchCrawl(httpclient,null);
		MulityInsertDataBase dbo = new MulityInsertDataBase();
		Vector<TwiUser> users=new Vector<TwiUser>(20);
		Task task=new Task(TaskType.Search,"���Ƴ�");
		ReportData reportData=new ReportData();
		//MessageTwitterHbase msghbase=new MessageTwitterHbase("message");
		//test.SetHabae(msghbase, true);
		test.doCrawl(task, dbo, users, reportData);
		
		
		
		
		System.out.println("current Search String Size:"+users.size());
	}
	
	
	public boolean doCrawl(Task task,MulityInsertDataBase dbo,Vector<TwiUser> RelateUsers,ReportData reportData){
		String keyWords=task.getTargetString();
		boolean has_next=false;
		String next_max_id="";
		AjaxSearchAnalyser ana=new AjaxSearchAnalyser(dbo,task);
		if(this.Hbase_Enable){
			ana.SetHabae(hbase,true);
		}
		String URL;
		int count=0;
		String lastCursor="";
		do{
			WebOperationResult webres=WebOperationResult.Success;
			int re_trycount=0;
			if(next_max_id==null||next_max_id.equals("")){
				URL=String.format(baseURL,keyWords,"");
			}else{
				URL=String.format(baseURL+max_id_str,keyWords,next_max_id);
			}
			String content=super.openLink(httpclient, URL,task,re_trycount,webres);
			
			Map map=null;
			if(content==null){
				System.out.println("HttpClint����Ajax����Ϊ�ջ򳤶Ȳ���");
				System.out.println("CurrentURL:"+URL);
				System.out.println("Content:"+content);
				super.SaveWebOpStatus(task, URL, count, WebOperationResult.Fail, dbo);
				break;
			}else{
				count++;	
			}
			super.SaveWebOpStatus(task, URL, count, WebOperationResult.Success, dbo);
			try {
				map = (Map)parser.parse(content);				
			}catch (ParseException e) {
				// TODO Auto-generated catch block
				LogSys.nodeLogger.debug(content);
				LogSys.nodeLogger.error("������ʱ��ǰ�ɼ��Ĺؼ�����--"+keyWords);
				e.printStackTrace();				
				return false;				
			}
			has_next=(Boolean)map.get("has_more_items");
			String html=(String)map.get("items_html");
			AnalyserCursor res=null;
			try {
				res = ana.doAnalyse(html,RelateUsers,reportData);
			} catch (AllHasInsertedException e) {
				//ϵͳ�����ظ��ɼ���ֹͣ��ǰ�ɼ�����
				has_next=false;
				e.printStackTrace();
				//LogSys.nodeLogger.debug("��ǰSearch�ɼ��Ѿ���⣬�����ɼ�["+keyWords+"]");
				
				LogSys.nodeLogger.debug("��ǰSearch�ɼ����,ֹͣ�ɼ�["+keyWords+"]");
				break;
			}catch (Exception ex){
				has_next=false;
				LogSys.nodeLogger.debug("��ǰSearchAnalyse������������["+keyWords+"]");
				ex.printStackTrace();
				return false;
			}	
			if(map.get("has_more_items")!=null){
				has_next=(boolean)map.get("has_more_items");
			}else{
				has_next=false;
			}
			if(map.get("scroll_cursor")!=null){
				String currentCursor=(String)map.get("scroll_cursor");
				if(lastCursor.equals(currentCursor)){
					has_next=false;
				}else{
					has_next=true;
					next_max_id=(String)map.get("scroll_cursor");
					lastCursor=currentCursor;
				}
			}else{
				has_next=false;
			}
				
			
		}while(has_next==true);
		return true;

	}
}
