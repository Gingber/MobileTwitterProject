package com.ict.twitter.netty.server;
import com.ict.twitter.CrawlerServer.CrawlerServer;
import com.ict.twitter.plantform.LogSys;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class DiscardServer implements Runnable {

    private int port;
    private CrawlerServer CS;
    public DiscardServer(int port) {
        this.port = port;
    }
    public DiscardServer(int port,CrawlerServer CS){
    	this.port=port;
    	this.CS=CS;   	
    }
    public void run(){
    	try{
    		this.dowork();	
    	}catch(java.net.BindException exbind){
    		LogSys.crawlerServLogger.error("8080Netty端口已经被占用");
    		System.err.println("由于端口占用，Crawlserver无法监听Nety，退出");
    		System.exit(-2);
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		LogSys.crawlerServLogger.error("无法创建NettyServer");
    		System.exit(-2);
    	}
    }
    public void dowork() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class) // (3)
             .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(
                    		new ObjectEncoder(),
                    		new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                    		new CrawlServerTaskHandler(CS)
                    		 );
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)          // (5)
             .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new DiscardServer(port).dowork();
    }
}