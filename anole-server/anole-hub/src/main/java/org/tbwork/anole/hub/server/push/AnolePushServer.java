package org.tbwork.anole.hub.server.push;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tbwork.anole.hub.TimeEncoder;
import org.tbwork.anole.hub.TimeServerHandler;  
import org.tbwork.anole.hub.server.push.handler.AuthenticationHandler;
import org.tbwork.anole.hub.server.push.handler.MainLogicHandler;
import org.tbwork.anole.hub.server.push.handler.NewConnectionHandler;

import com.google.common.base.Preconditions;


/**
 * AnolePushServer is used for anole subscriber client.
 * A subscriber client can establish a long connection 
 * with the AnolePushServer, and then continuously receive
 * messages pushed by the server.The management of clients is
 * maintained by the server which means the client do not
 * worry about the waste of connection caused by the network
 * problem or omitting to call "close()" method.
 * @author Tommy.Tang
 */
@Component
public class AnolePushServer {

	volatile boolean started;
	static final Logger logger = LoggerFactory.getLogger(AnolePushServer.class);
	Channel channel = null;
	EventLoopGroup bossGroup = null;
	EventLoopGroup workerGroup = null;
	
	@Autowired
	AuthenticationHandler authenticationHandler;
	
	@Autowired
	MainLogicHandler mainLogicHandler;
	
	@Autowired
	NewConnectionHandler newConnectionHandler;
	
	public void start(int port){
		if(!started) //DCL-1
		{
			synchronized(AnolePushServer.class)
			{
				if(!started)//DCL-2
				{
					executeStart(port); 
				}
			}
		} 
	}
	
	public void close()
	{
		if(!started) //DCL-1
		{
			synchronized(AnolePushServer.class)
			{
				if(!started)//DCL-2
				{
					executeClose(); 
				}
			}
		} 
	}
	private void executeStart(int port){
		Preconditions.checkArgument(port>0, "port should be > 0");
		bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast( 
                    		 new ObjectEncoder(),
                    		 newConnectionHandler, 
                    		 new ObjectDecoder(ClassResolvers.cacheDisabled(null)), 
                    		 authenticationHandler, 
                    		 mainLogicHandler
                    		 );
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)      
             .childOption(ChannelOption.SO_KEEPALIVE, true); 

             // Bind and start to accept incoming connections. 
			 ChannelFuture f = b.bind(port).sync(); 
             if(f.isSuccess())
             {    
            	 channel = f.channel();
            	 logger.info("[:)] Anole push server at local address (port = {}) started succesfully !", port);
            	 started = true;
             }
			 
        }catch(InterruptedException e){ 
        	logger.error("[:(] Anole push server failed to start at port {}!", port);
			e.printStackTrace();
        }  
	}
	
	private void executeClose(){ 
		try {
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			logger.error("[:(] Anole push server failed to close. Inner message: {}", e.getMessage());
			e.printStackTrace();
		}finally{ 
			if(!channel.isActive())
			{
				logger.info("[:)] Anole push server closed successfully !");		
				workerGroup.shutdownGracefully();
		        bossGroup.shutdownGracefully();
				started = false;
			}
		} 
	}
	
	/**
	 * Ping and clean bad connections.
	 */
	private void startMonitor(){
		
		
		
	}
	
}
