package com.ict.twitter.netty.server;
import java.util.Vector;

import com.ict.monitor.bean.OutPutTask;

import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
public class SendTaskClient {
    private final String host;
    private final int port;
    private SendTaskClientHandler handler=null;

    
    public SendTaskClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public SendTaskClient(Vector<OutPutTask> tasks){
    	host="127.0.0.1";
    	port=8080;
    	handler=new SendTaskClientHandler(tasks);
    	
    }
    
    public void doSend() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ObjectEncoder(),
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            handler);
                }
             });
            
            // Start the connection attempt.
            b.connect(host, port).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    public static void main(String[] args){
    	
    }
}
