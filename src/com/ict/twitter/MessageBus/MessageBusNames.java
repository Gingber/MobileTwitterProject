package com.ict.twitter.MessageBus;

public interface MessageBusNames {
	public static final String ControlC2S="CONTROLC2S";
	public static final String ControlS2C="CONTROLS2C";
	public static final String CrawlInfoC2S="CRAWLINFOC2S";
	public static final String KeyID="KEYID";
	public static final String NormalID="NORMALID";
	public static final String ReportTwitterWEB="REPORTTWITTERWEB";
	public static final String ReportTwitterAPI="REPORTTWITTERAPI";
	
	public static final String UrgentTask="UrgentTask"; //������ȼ�4
	public static final String KeyWordAndTopicTask="KeyWordAndTopicTask"; //���ȼ�3
	public static final String KeyUserTask="KeyUserTask";//����ص��˻� //���ȼ�2

	
	public static final String Task="TASK";//��ͨ���ȼ����ȼ�1
	
	
	public String[] names={ControlC2S,ControlS2C,CrawlInfoC2S,KeyID,NormalID,ReportTwitterWEB,ReportTwitterAPI,UrgentTask,KeyWordAndTopicTask,KeyUserTask,Task};
	

}
