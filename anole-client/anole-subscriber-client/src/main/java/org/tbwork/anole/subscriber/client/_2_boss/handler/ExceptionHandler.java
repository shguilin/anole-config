package org.tbwork.anole.subscriber.client._2_boss.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;  
import org.tbwork.anole.subscriber.client._2_worker.impl.AnoleSubscriberClient;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise; 
/**
 * Deal with all exceptions.
 * @author tommy.tang
 */  
public class ExceptionHandler extends ChannelHandlerAdapter {

	static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class); 
    private AnoleSubscriberClient asc = AnoleSubscriberClient.instance();
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { 
    	if(cause instanceof IOException) {
    		asc.setConnected(false);
    		logger.warn("The Anole server (address = {}) disconnected initiatively! ", ctx.channel().remoteAddress());
    	}
    	else {
    		cause.printStackTrace();
    	} 
        ctx.close();
    }
    
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
        asc.setConnected(false);
    }
}