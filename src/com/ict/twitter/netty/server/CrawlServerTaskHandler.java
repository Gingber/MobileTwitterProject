package com.ict.twitter.netty.server;

import java.util.Vector;

import com.ict.monitor.bean.OutPutTask;
import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.task.beans.Task;
import com.ict.twitter.task.beans.Task.TaskType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class CrawlServerTaskHandler extends ChannelInboundHandlerAdapter {
	public CrawlerServer crawlserver;
	public CrawlServerTaskHandler(CrawlerServer cs){
		this.crawlserver=cs;		
	}
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Vector<OutPutTask> result=(Vector<OutPutTask>)msg;
        for(int i=0;i<result.size();i++){
        	System.out.println("服务器接收到外部Netty任务："+result.get(i).getTaskName()+":"+result.get(i).getTaskType());
            Task task=new Task();
            task.setTargetString(result.get(i).getTaskName());
            if(result.get(i).getTaskType().equalsIgnoreCase("KeyUser")){
            	task.setOwnType(TaskType.TimeLine);
            }else if(result.get(i).getTaskType().equalsIgnoreCase("KeyWord")){
            	task.setOwnType(TaskType.Search);
            }else{
            	System.err.println("服务器接受到数据异常");
            }
            if(crawlserver==null){
            	continue;
            }
            this.crawlserver.addUrgentTask(task);
        }
    	
        ctx.flush();
        ctx.close();
    }
}
