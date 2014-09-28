package com.ict.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.net.SyslogAppender;

import com.ict.twitter.plantform.LogSys;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.tools.BasePath;
import com.ict.twitter.tools.SaveTxtFile;


public class WebOperationAjax {
	

	/**
	 * ���URI���޸ģ���path����  query ���ж��������޸�
	 * ԭ����
	 */
	
	public static String logFile="TwitterOpenLog.html";
	public static String base=BasePath.GetWebOpLogDir();	
	public static boolean debug=false;
	
	
	
	//"/searches?page=1&q='"+targetString+"'";
	public static String openLink(DefaultHttpClient httpclient,Task task, String linkAddress,int count) throws RuntimeException{
		if(count<=2){
			LogSys.nodeLogger.debug("The Retry["+count+"] OpenLink with Address:"+linkAddress);
			String res=openLink(httpclient,linkAddress);
			if(debug){
				SaveToTextFile(res,task);	
			}
			if(res!=null&&res.length()>=1){
				return res;
			}else{
				return openLink(httpclient,task,linkAddress,count+1);
			}
		}else{
			return null;
		}
	}
	private static void SaveToTextFile(String Content,Task currentTask){
		Date date=new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
		SimpleDateFormat sdfDate=new SimpleDateFormat("yyyy-MM-dd");
		String dayStr=sdfDate.format(date);
		String DateTimeStr=sdf.format(date);
		String TaskStr=currentTask.TaskToFileName();
		String FileSaveName=base+"/"+dayStr+"/"+DateTimeStr+"["+TaskStr+"]"+".txt";
		SaveTxtFile sxf=new SaveTxtFile(FileSaveName, false);
		sxf.Append(Content);
		sxf.close();
	}
	
	private  static String openLink(DefaultHttpClient httpclient,String linkAddress) throws RuntimeException{
		URI uri=null;
		HttpGet httpget=null;
		int questIndex=linkAddress.indexOf('?');
		int qIndex=linkAddress.indexOf('q');
		if(questIndex!=-1&&qIndex!=-1){
			try {
				String path=linkAddress.substring(0,linkAddress.indexOf('?'));
				String query=linkAddress.substring(linkAddress.indexOf('?')+1,linkAddress.length());				
				uri=new URI(null,null,path,query,null);
				LogSys.nodeLogger.info("���Դ���ҳaddress��"+uri.toString());
				httpget= new HttpGet(uri);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out.println("�﷨����");			
				e.printStackTrace();
			}catch(Exception ex){
				ex.printStackTrace();
				System.exit(0);
				
			}			
		}else{
			httpget=new HttpGet(linkAddress);
		}				
		httpget.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,"utf-8");
		httpget.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,HttpVersion.HTTP_1_0);
		httpget.getParams().setParameter(CoreProtocolPNames.USER_AGENT,"Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)");
		HttpHost targetHost = new HttpHost("twitter.com", 443, "https");
		BasicHttpContext localcontext = new BasicHttpContext();
		httpclient.getParams().setParameter(HTTP.CONTENT_ENCODING, "utf-8");
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 40000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 40000);		
		StringBuffer sb=new StringBuffer();
		HttpResponse response = null;
		try {
			response = httpclient.execute(targetHost, httpget, localcontext);
			StatusLine state =response.getStatusLine();
			int stateCode=state.getStatusCode();
			boolean needReLogin=false;
			if(HttpStatus.SC_OK==stateCode){
				BufferedReader in = new BufferedReader(	new A2NFilter(new InputStreamReader(response.getEntity().getContent(),"utf-8")));
				String inputLine = null;
				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine+"\r\n");
					//�ж��Ƿ���Ҫ���µ�¼��Twitter
					if(inputLine.contains("Sign in to Twitter")||inputLine.contains("<form action=\"https://twitter.com/sessions\"")){
						LogSys.nodeLogger.error("��Ҫ���µ�¼");
						needReLogin=true;
					}
				}
				in.close();				
			}else if(HttpStatus.SC_MOVED_PERMANENTLY == stateCode 
					|| HttpStatus.SC_MOVED_TEMPORARILY == stateCode
					|| HttpStatus.SC_SEE_OTHER == stateCode
					|| HttpStatus.SC_TEMPORARY_REDIRECT == stateCode){
				Header[] headers=response.getHeaders("location");
				if(headers!=null&&headers.length>0){
					String redirectLocation = headers[0].getValue();
					String redirectAddress;
					if(redirectLocation!=null&&redirectLocation.isEmpty()==false){
						redirectAddress=redirectLocation;
						LogSys.nodeLogger.error("������ת~:location to"+redirectAddress);
						
					}else{
						redirectAddress="/";
						LogSys.nodeLogger.error("������ת~:location to"+redirectAddress);
					}
				}				
			}
			
			//ִ�к���ˣ�������ҳ���쳣���,��Ҫ���µ�¼��������������µ�¼
			if(needReLogin){				
				AdvanceLoginManager twlogin = new AdvanceLoginManager(httpclient);
				twlogin.trylogin();
				return null;
			}
			
						
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			LogSys.nodeLogger.debug("ClientProtocalException e");
		}catch(HttpHostConnectException ex){
			return null;
		}catch(org.apache.http.ConnectionClosedException ex){
			LogSys.nodeLogger.error("�ļ��������ر�");
			ex.printStackTrace();
			return sb.toString();
		}
		catch (java.net.SocketTimeoutException x){
			LogSys.nodeLogger.debug("��ȡ�ļ���ʱ");
			httpget.abort();
			return null;			
		}catch (RuntimeException ex) {
	         httpget.abort();
	         throw ex;
	    }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}finally{
			if(httpget!=null){
				httpget.abort();
			}
			if(response!=null){
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					System.err.println("response ����ʧ��");
					e.printStackTrace();
				}
			}else{
				return null;
			}
		}		
		return sb.toString();           
		
	}
	
	
		
	public static String getLogFile() {
		return logFile;
	}

	public static void setLogFile(String logFile) {
		WebOperationAjax.logFile = logFile;
	}
	
	public static String str1="/i/profiles/show/BigBang_CBS/timeline?include_available_features=1&include_entities=1";
	public static void main(String[] args){
		TwitterClientManager cm=new TwitterClientManager();
		DefaultHttpClient httpclient = cm.getClientNoProxy();
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000); 
//		LoginManagerTest lgtest=new LoginManagerTest(httpclient);
		//lgtest.doLogin();
		System.out.println("����ҳ��ing");
		int errorcount=0;
		
		String test1="/i/search/timeline?src=typd&type=recent&include_available_features=1&include_entities=1&q='֧��+��'";
		String oldtest="/i/profiles/show/BigBang_CBS/timeline?include_available_features=1&include_entities=1";
		String t=WebOperationAjax.openLink(httpclient,test1);
		//byte[] res=StringToChar(t);
		//System.out.println(res);
		System.out.println("content"+t);
		if(t.length()<=100){
			errorcount++;
			System.err.println("û���յ�����");
		}		
	}
	public static char[] StringToChar(String source){
		char[] temp=new char[source.length()];
		for(int i=0;i<source.length()-1;i++){
			if(source.charAt(i)=='\\'&&source.charAt(i+1)=='u'){
				String t="0x"+source.substring(i+2,i+6);
				byte bt=Byte.decode(t);				
			}
		}
		return temp;
	}

	public static byte[] getSource(DefaultHttpClient httpclient,String url){
		if(url==null||url.equalsIgnoreCase("null")){
			LogSys.clientLogger.error("Profile URL is��NULL"+url);
			return null;
		}
		URI uri=null;
		try {
			uri=new URI(url);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		HttpGet get = new HttpGet(uri);
		HttpResponse response=null;
		byte[] res=null;
		InputStream input;
		try{
			response=httpclient.execute(get);
			int state=response.getStatusLine().getStatusCode();
			if(state==HttpStatus.SC_OK){
				HttpEntity entity=response.getEntity();
				input=entity.getContent();
				byte[] buffer=new byte[1024*1024*1];
				int readlength=0;
				while((readlength=input.read(buffer))>0){
					if(res==null){
						res=new byte[readlength];
						System.arraycopy(buffer, 0, res, 0, readlength);
					}else{
						byte[] newArray=new byte[res.length+readlength];
						System.arraycopy(res, 0, newArray, 0, res.length);
						System.arraycopy(buffer, 0, newArray, res.length, readlength);
						res=newArray;
						
					}
				}
				input.close();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			get.abort();			
		}
		return res;
	}


}
