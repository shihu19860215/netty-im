package com.shihu.im.client.client;

import com.shihu.im.client.util.ClientContext;
import com.shihu.im.common.define.User;
import com.shihu.im.common.msg.IMMessageProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.IOException;
import java.util.Date;
import java.util.List;


@ChannelHandler.Sharable
public class IMClientHandle extends SimpleChannelInboundHandler<IMMessageProto.IMMessage> {
    final private static IMMessageProto.IMMessage PING_MSG=IMMessageProto.IMMessage.newBuilder().setType(IMMessageProto.IMMessage.MsgType.Ping).build();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMMessageProto.IMMessage imMessage) throws Exception {
        IMMessageProto.IMMessage.MsgType msgType=imMessage.getType();
        if(IMMessageProto.IMMessage.MsgType.Login==msgType){
            loginResponse(channelHandlerContext.channel(),imMessage);
        }else if (IMMessageProto.IMMessage.MsgType.LoginOut==msgType){

        }else if (IMMessageProto.IMMessage.MsgType.Ping==msgType){

        }else if (IMMessageProto.IMMessage.MsgType.GeneralMsg==msgType){
            generalMsgRespone(imMessage);
        }else if (IMMessageProto.IMMessage.MsgType.Sysn==msgType){
            sysnMsgResponse(imMessage.getSysnMsgResponse());
        }else if (IMMessageProto.IMMessage.MsgType.Notice==msgType){
            noticeMsg(imMessage);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                    ctx.channel().writeAndFlush(PING_MSG);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException){
            System.out.println("server is disconnect");
            ctx.channel().close();
            System.out.println("pause any key exit");
            System.in.read();
            System.exit(1);
        }else {
            super.exceptionCaught(ctx, cause);
        }
    }

    private void noticeMsg(IMMessageProto.IMMessage imMessage){
        IMMessageProto.IMMessage.NoticeMsg noticeMsg = imMessage.getNoticeMsg();
        if(noticeMsg.getType()==IMMessageProto.IMMessage.NoticeType.login){
            List<IMMessageProto.IMMessage.User> userList=noticeMsg.getUsersList();
            if(null!=userList){
                for(IMMessageProto.IMMessage.User u:userList){
                    User user=new User();
                    user.setId(u.getId());
                    user.setUsername(u.getUsername());
                    ClientContext.addUser(user);
                    System.out.println("!! "+user.getUsername()+" is login");
                }
            }
        }else if(noticeMsg.getType()==IMMessageProto.IMMessage.NoticeType.loginout){
            List<IMMessageProto.IMMessage.User> userList=noticeMsg.getUsersList();
            if(null!=userList){
                for(IMMessageProto.IMMessage.User u:userList){
                    ClientContext.removeUserById(u.getId());
                }
            }

        }
    }

    private void sysnMsgResponse(IMMessageProto.IMMessage.SysnMsgResponse sysnMsgResponse){
        ClientContext.initUserIdNameMap(sysnMsgResponse);
    }

    private void generalMsgRespone(IMMessageProto.IMMessage imMessage){
        long from=imMessage.getFrom();
        long to=imMessage.getTo();
        String fromName=ClientContext.getUsernameById(from);
        String toName=ClientContext.getUsernameById(to);
        //不是群发并且无法找到发给谁
        if(0!=to&&null==toName){
            return;
        }
        if(null!=fromName){
            StringBuilder sb=new StringBuilder();
            sb.append("-- ");
            if(ClientContext.getUser().getUsername().equals(fromName)){
                sb.append("[Me]");
            }else {
                sb.append(fromName);
            }
            sb.append(" to: ");
            if(0==to){
                sb.append("[All]");
            }else if(ClientContext.getUser().getUsername().equals(toName)){
                sb.append("[Me]");
            }else if(null!=toName){
                sb.append(toName);
            }
            sb.append(" say: ")
                    .append(imMessage.getGeneralMsgRespone().getInfo());
            System.out.println(sb.toString());
        }
    }

    private void loginResponse(Channel channel, IMMessageProto.IMMessage imMessage){
        IMMessageProto.IMMessage.LoginResponse loginResponse=imMessage.getLoginResponse();
        IMMessageProto.IMMessage.ResultState resultState=loginResponse.getState();
        if(IMMessageProto.IMMessage.ResultState.Success==resultState){
            System.out.println("login success");;
            if(0!=imMessage.getFrom()){
                ClientContext.getUser().setId(imMessage.getFrom());
            }
            ClientContext.setToken(imMessage.getToken());
            IMMessageProto.IMMessage imMessageRequest=IMMessageProto.IMMessage.newBuilder()
                    .setType(IMMessageProto.IMMessage.MsgType.Sysn)
                    .build();
            channel.writeAndFlush(imMessageRequest);
        }else {
            System.out.println(loginResponse.getMessage());
        }
    }
}
