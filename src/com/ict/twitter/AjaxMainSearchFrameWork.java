package com.ict.twitter;

import org.apache.http.impl.client.DefaultHttpClient;
//�����˲ɼ��ڵ�Ļ������Ժ�����

import com.ict.twitter.CrawlerNode.AjaxNode;
import com.ict.twitter.tools.DbOperation;


public abstract class AjaxMainSearchFrameWork implements Runnable {	
	//�ɼ�������
	protected String Name;
	protected DbOperation DBOp;
	
	
	//�����Ĳ�������
	protected DefaultHttpClient httpclient;
	protected WebOperationAjax webOperation;
	protected AjaxNode node;
	public abstract void run();
	public abstract void doWork();
	

}
