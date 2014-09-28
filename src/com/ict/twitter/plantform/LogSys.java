package com.ict.twitter.plantform;

import java.text.SimpleDateFormat;

import org.apache.log4j.*;

import org.apache.log4j.PropertyConfigurator;

public class LogSys {

	/**
	 * @param args
	 */

	static{
		PropertyConfigurator.configure ("config/log4j_Main.properties" );
		Logger.getLogger("org.apache.zookeeper").setLevel(Level.WARN);
		Logger.getLogger("org.apache.hadoop.hbase.zookeeper").setLevel(Level.WARN);
		Logger.getLogger("org.apache.hadoop.hbase.client").setLevel(Level.WARN);
	}
	public static Logger debugLogger=Logger.getLogger("E");
	public static Logger nodeLogger=Logger.getLogger("NODELOGER");
	public static Logger crawlerServLogger=Logger.getLogger("CRAWLERSERVERLOGER");
	public static Logger clientLogger=Logger.getLogger("CLIENTLOGER");
	
	public static void main(String[] args) {
		java.util.Date data=new java.util.Date();
		SimpleDateFormat dateformat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
		nodeLogger.info("����NODE"+dateformat1.format(data));
		crawlerServLogger.error("�Ҿ���CrawlerServer"+dateformat1.format(data));
		clientLogger.error("����CLIENT"+dateformat1.format(data));
		
		
	}

}
