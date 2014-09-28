package com.ict.twitter.CrawlerServer;
import java.util.Collection;

import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.web.RemoteJMXBrokerFacade;
import org.apache.activemq.web.config.SystemPropertiesConfiguration;

public class MessageBusManagement {
	RemoteJMXBrokerFacade createConnector;
	public MessageBusManagement(){
		createConnector = new RemoteJMXBrokerFacade(); 
		System.setProperty("webconsole.jmx.url","service:jmx:rmi:///jndi/rmi://192.168.120.66:1099/jmxrmi");  
		System.setProperty("webconsole.jmx.user", "admin");  
		System.setProperty("webconsole.jmx.password", "activemq");
		SystemPropertiesConfiguration configuration = new SystemPropertiesConfiguration();
		createConnector.setConfiguration(configuration);  
		
	}
	public static void main(String[] args){
		MessageBusManagement mb=new MessageBusManagement();
		Collection<QueueViewMBean> beans=mb.ListAllQueue();
		for (QueueViewMBean queueViewMBean : beans) {  
			System.out.println("Queue beanName =" + queueViewMBean.getName());  
			System.out.println("ConsumerCount ="+ queueViewMBean.getConsumerCount());  
			System.out.println("DequeueCount =" + queueViewMBean.getDequeueCount());  
			System.out.println("EnqueueCount =" + queueViewMBean.getEnqueueCount());  
			System.out.println("DispatchCount ="    + queueViewMBean.getDispatchCount());  
			System.out.println("ExpiredCount =" + queueViewMBean.getExpiredCount());  
			System.out.println("MaxEnqueueTime ="+ queueViewMBean.getMaxEnqueueTime());  
			System.out.println("ProducerCount ="    + queueViewMBean.getProducerCount());  
			System.out.println("MemoryPercentUsage ="+ queueViewMBean.getMemoryPercentUsage());  
			System.out.println("MemoryLimit =" + queueViewMBean.getMemoryLimit());  
		}  
		mb.createConnector.shutdown();
	}
	
	public Collection<QueueViewMBean> ListAllQueue(){
		Collection<QueueViewMBean> queueViewList=null;
		try {
			queueViewList = createConnector.getQueues();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		return queueViewList;
//		for (QueueViewMBean queueViewMBean : queueViewList) {  
//		    System.out.println("Queue beanName =" + queueViewMBean.getName());  
//		    System.out.println("ConsumerCount ="+ queueViewMBean.getConsumerCount());  
//		    System.out.println("DequeueCount =" + queueViewMBean.getDequeueCount());  
//		    System.out.println("EnqueueCount =" + queueViewMBean.getEnqueueCount());  
//		    System.out.println("DispatchCount ="    + queueViewMBean.getDispatchCount());  
//		    System.out.println("ExpiredCount =" + queueViewMBean.getExpiredCount());  
//		    System.out.println("MaxEnqueueTime ="+ queueViewMBean.getMaxEnqueueTime());  
//		    System.out.println("ProducerCount ="    + queueViewMBean.getProducerCount());  
//		    System.out.println("MemoryPercentUsage ="+ queueViewMBean.getMemoryPercentUsage());  
//		    System.out.println("MemoryLimit =" + queueViewMBean.getMemoryLimit());  
//		}  
	}
	
	
	
	public boolean cleanQueueByName(String queuename){
		 Collection<QueueViewMBean> list=this.ListAllQueue();
		 boolean flag=false;
		 for(QueueViewMBean bean:list){
			 if(bean.getName().equalsIgnoreCase(queuename)){
				 try {
					bean.purge();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					flag=false;
					e.printStackTrace();
				}
				 flag=true;
				 break;
			 }
		 }
		 return flag;
	}
}
