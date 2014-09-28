package com.ict.twitter.CrawlerServer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import com.ict.twitter.CrawlerNode.NodeHeartBeatReport;
import com.ict.twitter.CrawlerNode.NodeStep;
import com.ict.twitter.MessageBus.MessageBusNames;
import com.ict.twitter.plantform.LogSys;

public class NodeManager {
	public class NodeStatus{
		public NodeStatus(boolean isBusy, int taskSize, NodeStep currentstep) {
			super();
			this.isBusy = isBusy;
			this.taskSize = taskSize;
			this.currentstep = currentstep;
		}
		boolean isBusy;
		int taskSize;
		NodeStep currentstep;
	}
	
	int nodecount=0;
	int finishcount=0;
	NodeStep NodeManagerStep;
	Hashtable<String,NodeStatus> allNodeStatus;
	private MessageBusStatusDetect mbDetect;
	
	public NodeManager(){
		allNodeStatus=new Hashtable<String,NodeStatus>();
		NodeManagerStep=NodeStep.init;
		mbDetect=new MessageBusStatusDetect();
	}
	public long getTaskSizeCount(){
		return mbDetect.getCount(MessageBusNames.Task);
	}
	
	public boolean canNextStepByTaskBusName(String busname){
		long taskSize=mbDetect.getCount(busname);
		if(taskSize>0||taskSize==-1){			
			return false;			
		}
		Iterator<NodeStatus> it=allNodeStatus.values().iterator();
		int allNodecount=allNodeStatus.size();
		finishcount=0;
		while(it.hasNext()){
			NodeStatus status=it.next();
			if(status.isBusy==false){
				finishcount++;
			}			
		}
		LogSys.crawlerServLogger.info("�Ѿ��ɹ�������һ�׶�(Finish��"+finishcount+"  TotalCount:"+allNodecount+" CurrentSetp:"+NodeManagerStep);
		return true;
	}
	
	private boolean canNextStep(){
		/*1:������ߵ�״̬���Ƿ�ȫ����������Ϣ�Ѿ���������ϣ������������Ƿ�Ϊ�գ�
		  2:�������ɼ��ڵ��Ƿ��ڿ���״̬
		  3:��ȫ��Ϊ�棬�򷵻�true,���򷵻�false.
		*/
		long taskSize=mbDetect.getCount(MessageBusNames.Task);
		if(taskSize>0||taskSize==-1){			
			return false;			
		}
		Iterator<NodeStatus> it=allNodeStatus.values().iterator();
		int allNodecount=allNodeStatus.size();
		finishcount=0;
		while(it.hasNext()){
			NodeStatus status=it.next();
			if(status.isBusy==false){
				finishcount++;
			}			
		}
		LogSys.crawlerServLogger.info("�Ѿ��ɹ�������һ�׶�(Finish��"+finishcount+"  TotalCount:"+allNodecount+" CurrentSetp:"+NodeManagerStep);
		return true;
	}
	
	public boolean nextstep(){
		
		Iterator<NodeStatus> it=allNodeStatus.values().iterator();
		int allNodecount=allNodeStatus.size();
		if(allNodeStatus.size()==0){
			return false;
		}
		finishcount=0;
		while(it.hasNext()){
			NodeStatus status=it.next();
			if(status.isBusy==false){
				finishcount++;
			}			
		}
		
		if(finishcount<allNodecount){
				LogSys.crawlerServLogger.debug("�޷�������һ�׶�Finish��"+finishcount+"_TotalCOunt:"+allNodecount+" CurrentSetp:"+NodeManagerStep);
				return false;
		}else{
			LogSys.crawlerServLogger.info("���Գɹ�������һ�׶�(Finish��"+finishcount+"  TotalCount:"+allNodecount+" CurrentSetp:"+NodeManagerStep);
			Iterator<NodeStatus> totalStatus=allNodeStatus.values().iterator();
			NodeStatus one=totalStatus.next();
			if(one==null){
				return false;
			}else{
				one.taskSize=0;
				one.isBusy=true;
				it=allNodeStatus.values().iterator();				
				while(it.hasNext()){
					NodeStatus status=it.next();
					status.taskSize=0;
					status.isBusy=true;
				}
				finishcount=0;
				NodeManagerStep=one.currentstep;
			}
			LogSys.crawlerServLogger.info("�Ѿ��ɹ�������һ�׶�(Finish��"+finishcount+"  TotalCount:"+allNodecount+" CurrentSetp:"+NodeManagerStep);
			
			return true;
//				
//			}
			
		}
	}
	
	public void setNodeIsFinish(String name){

		
		
	}
	
	public NodeStep currentstep(){
		return NodeManagerStep;
	}
	public void addNode(String nodeName){
		if(allNodeStatus.get(nodeName)==null){
			nodecount++;
//			���node�㷨��			
			NodeStatus newnode=new NodeStatus(Boolean.TRUE,0,currentstep());			
			allNodeStatus.put(nodeName,newnode);
		}
		
	}
	public void removeNode(String nodeName){
		if(allNodeStatus.get(nodeName)==null){
			return;
		}
		allNodeStatus.remove(nodeName);
	}
	public void onnodefinish(String nodeName,NodeStep curstep){
		NodeStatus node=allNodeStatus.get(nodeName);
		if(node==null){
			return;
		}
		if(node.currentstep==NodeStep.init||node.currentstep==NodeStep.search_end||node.currentstep==NodeStep.keyuser_end||node.currentstep==NodeStep.normaluser_end){
			node.currentstep=curstep;
			node.isBusy=false;
			node.taskSize=0;
			Iterator<NodeStatus> it=allNodeStatus.values().iterator();
			int count=0;
			while(it.hasNext()){
				NodeStatus tmp=it.next();
				if(!tmp.isBusy){
					count++;
				}
			}
			finishcount=count;
			
		}
		
			
		
	}
	
	//�����յ�������Ϣ��~~
	public void onHeartBeatReceive(NodeHeartBeatReport report){
		NodeStatus node=allNodeStatus.get(report.name);
		if(node==null){
			addNode(report.name);
			node=allNodeStatus.get(report.name);			
		}
		node.currentstep=report.currentstep;
		node.isBusy=report.isBusy;
		node.taskSize=report.taskBufferSize;
		LogSys.crawlerServLogger.debug("�յ�����  "+report.name+"��������Ϣ"+nodeToString(node,report.name));
		onnodefinish(report.name, node.currentstep);

	}
	
	private int getBusyCount(){
		Iterator<NodeStatus> it=allNodeStatus.values().iterator();
		int count=0;
		while(it.hasNext()){
			NodeStatus tmp=it.next();
			if(!tmp.isBusy){
				count++;
			}
		}
		return count;
	}
	
	public String show(){
		StringBuilder sb=new StringBuilder();
		sb.append("------------------------------------------------------------\r\n");
		sb.append("nodecount:"+allNodeStatus.values().size()+" ");
		finishcount=this.getBusyCount();
		sb.append("finishcount:"+finishcount+" ");
		Enumeration <String> it=allNodeStatus.keys();
		int count=0;
		sb.append("\n\r");
		while(it.hasMoreElements()){
			String nodeName=it.nextElement();			
			NodeStatus stat=allNodeStatus.get(nodeName);
			nodeShow(nodeName,stat,sb);
			count++;
			if(count%5==0){
				sb.append("\n\r");
			}
			
		}		
		sb.append("------------------------------------------------------------\r\n");
		LogSys.crawlerServLogger.info("TIMER��ʱ����ʾ"+sb.toString());
		return sb.toString();
	}
	public void nodeShow(String name,NodeStatus node,StringBuilder sb){
		String t=name+":";
		if(node.isBusy)
			t+="BUSY ";
		else
			t+="FREE ";
		t+="["+node.currentstep.toString()+"]";
		t+="["+node.taskSize+"]";
		t+="\t";
		sb.append(t);		
	}
	public String nodeToString(NodeStatus node,String name){
		String t=name+":";
		if(node.isBusy)
			t+="BUSY ";
		else
			t+="FREE ";
		t+="["+node.currentstep.toString()+"]";
		t+="\t";
		t+=" ";
		t+="["+node.taskSize+"]";
		return t;
	}
	
	
	public static void main(String[] args){
		NodeManager nodeManager=new NodeManager();
		System.out.println("��ʼ��");
		nodeManager.show();
		
		
		nodeManager.addNode("node1");
		nodeManager.addNode("node2");
		nodeManager.onnodefinish("node2",NodeStep.search_end);
		nodeManager.onnodefinish("node1",NodeStep.search_end);		
		nodeManager.show();
		System.out.println("��һ��");
		nodeManager.nextstep();	
		
			
		
		
		//nodeManager.onnodefinish("node2",NodeStep.keyuser_end);
		nodeManager.onnodefinish("node1",NodeStep.keyuser_end);
		
		System.out.println("��2��");
		nodeManager.nextstep();
		nodeManager.show();
		
		
		nodeManager.onnodefinish("node2",NodeStep.normaluser_end);
		nodeManager.onnodefinish("node1",NodeStep.normaluser_end);
		
		System.out.println("��3��");
		nodeManager.nextstep();
		nodeManager.show();		
		
	}
	
}
