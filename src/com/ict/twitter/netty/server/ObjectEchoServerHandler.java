package com.ict.twitter.netty.server;
import java.util.Vector;

import com.ict.monitor.bean.OutPutTask;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


public class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter{
	
	
	public void channelRead(
            ChannelHandlerContext ctx, Object msg) throws Exception {
        Vector<OutPutTask> result=(Vector<OutPutTask>)msg;
        for(int i=0;i<result.size();i++){
        	System.out.println(result.get(i).getTaskName()+":"+result.get(i).getTaskType());
            
        }
        ctx.flush();
        ctx.close();
    }

	

}
