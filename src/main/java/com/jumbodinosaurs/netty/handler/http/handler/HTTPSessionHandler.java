package com.jumbodinosaurs.netty.handler.http.handler;

import com.jumbodinosaurs.commands.OperatorConsole;
import com.jumbodinosaurs.log.LogManager;
import com.jumbodinosaurs.log.Session;
import com.jumbodinosaurs.netty.handler.IHandlerHolder;
import com.jumbodinosaurs.netty.handler.http.util.HTTPHandler;
import com.jumbodinosaurs.netty.handler.http.util.HTTPRequest;
import com.jumbodinosaurs.netty.handler.http.util.HTTPResponse;
import com.jumbodinosaurs.util.OptionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class HTTPSessionHandler extends SimpleChannelInboundHandler<String> implements IHandlerHolder
{
    
    
    @Override
    public void channelRead(ChannelHandlerContext context,
                            Object msg)
    {
        String message = (String) msg;
        try
        {
            
            Channel channel = context.channel();
            Session session = new Session(channel, message);
            boolean allowConnection = false;
            if(OptionUtil.isWhiteListOn())
            {
                
                for(String ip : OptionUtil.getWhiteList())
                {
                    if(session.getWho().contains(ip))
                    {
                        allowConnection = true;
                        break;
                    }
                }
                
            }
            else
            {
                allowConnection = true;
            }
            
            
            if(allowConnection)
            {
                HTTPResponse response = new HTTPResponse();
                response.setMessage501();
                //TODO FIX THIS
                boolean isEncrypted = context.pipeline().names().contains("io.netty.handlerHolder.ssl.SslHandler");
                HTTPRequest request = new HTTPRequest(session.getMessage(), isEncrypted, session.getWho());
                if(request.isHTTP())
                {
                    if(!isEncrypted && OptionUtil.shouldUpgradeInsecureConnections())
                    {
                        response.setMessageToRedirectToHTTPS(request);
                    }
                    else
                    {
                        response = new HTTPHandler(request).generateResponse();
                    }
                    
                    //Would be kinda point less to hash a password if we saved it over in logs.json :P
                    session.setMessage(request.getCensoredMessage());
                    session.setMessageSent(response.getMessageToLog());
                }
                //Send Message
                context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                
                
                OperatorConsole.printMessageFiltered(session.toString(), true, false);
                
                LogManager.log(session);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            OperatorConsole.printMessageFiltered("Error Sending Message", false, true);
        }
        
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext context,
                                Throwable cause)
    {
        if(!(cause.getMessage() != null || cause.getMessage()
                                                .contains("no cipher suites in common") || cause.getMessage().contains(
                "not an SSL/TLS") || cause.getMessage().contains(
                "Client requested protocol SSLv3 not enabled or not supported") || cause.getMessage().contains(
                "Connection reset by peer")))
        {
            OperatorConsole.printMessageFiltered(cause.getMessage(), false, true);
        }
        context.close();
    }
    
    
    @Override
    public void channelRead0(ChannelHandlerContext context,
                             String message)
    {
        context.fireChannelRead(message);
    }
    
    @Override
    public SimpleChannelInboundHandler<String> getInstance()
    {
        return new HTTPSessionHandler();
    }
}
