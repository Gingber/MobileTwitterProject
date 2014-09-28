package com.ict.twitter.CrawlerNode;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.ict.twitter.tools.DbOperation;

public class runTWAjaxNodes {

	public static long maxTime = 1417363200;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long currentTime = System.currentTimeMillis()/1000;
		if(currentTime < maxTime) {
			run();
			RunExpriedMonitor();
		}
	}
	public static boolean run(){
		DbOperation dbOper=new DbOperation();
		try{
			PropertyConfigurator.configure ("config/log4j_Main.properties" ) ;
			Properties properties=new Properties();
			properties.load(new FileInputStream("config/clientproperties.ini"));
			int nodecount=Integer.parseInt((String)properties.get("node.nodecount"));
			String name="TWAjaxNode";
			for(int i=0;i<nodecount;i++){				
				String nodeName=String.format("%s-%2d", name,i);
				AjaxNode node=new AjaxNode(nodeName,dbOper);
				Thread thread=new Thread(node);
				thread.setName("nodeName");
				thread.start();	
			}
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	private static void RunExpriedMonitor(){
		RunStatusMonitor rsm=new RunStatusMonitor();
		Thread ts=new Thread(rsm);
		ts.setDaemon(true);
	}

}
