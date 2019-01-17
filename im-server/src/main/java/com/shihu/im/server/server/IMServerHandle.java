package com.shihu.im.server.server;

import com.shihu.im.common.define.User;
import com.shihu.im.common.msg.IMMessageProto;
import com.shihu.im.common.util.IdGenerate;
import com.shihu.im.server.util.ServerContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.IOException;
import java.util.List;

@ChannelHandler.Sharable
public class IMServerHandle extends SimpleChannelInboundHandler<IMMessageProto.IMMessage> {
    private int loss_connect_time=0;
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMMessageProto.IMMessage imMessage) throws Exception {
        IMMessageProto.IMMessage.MsgType msgType=imMessage.getType();
        if(IMMessageProto.IMMessage.MsgType.Login==msgType){
            loginRequest(channelHandlerContext.channel(),imMessage);
        }else if (IMMessageProto.IMMessage.MsgType.LoginOut==msgType){
            loginOut(channelHandlerContext.channel());
        }else if (IMMessageProto.IMMessage.MsgType.Ping==msgType){

        }else if (IMMessageProto.IMMessage.MsgType.GeneralMsg==msgType){
            generalMsgRequest(channelHandlerContext.channel(),imMessage);
        }else if (IMMessageProto.IMMessage.MsgType.Sysn==msgType){
            sysnMsgRequest(channelHandlerContext.channel());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                if (loss_connect_time > 3) {
                    loginOut(ctx.channel());
                    ctx.channel().close();
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException){
            loginOut(ctx.channel());
        }else {
            super.exceptionCaught(ctx, cause);
        }
    }

    private void loginOut(Channel channel){
        User user=ServerContext.getUserByToken(ServerContext.getTokenByChannel(channel));
        ServerContext.loginOut(channel);
        loginNotice(channel,user,IMMessageProto.IMMessage.NoticeType.loginout);

    }

    private void sysnMsgRequest(Channel channel){
        IMMessageProto.IMMessage.SysnMsgResponse.Builder builder=IMMessageProto.IMMessage.SysnMsgResponse.newBuilder();
        List<User> userList=ServerContext.getALlUser();
        for(int i=0;i<userList.size();i++){
            IMMessageProto.IMMessage.User msgUser=IMMessageProto.IMMessage.User.newBuilder()
                    .setId(userList.get(i).getId())
                    .setUsername(userList.get(i).getUsername())
                    .build();
            builder.addUsers(msgUser);
        }
        IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                .setType(IMMessageProto.IMMessage.MsgType.Sysn)
                .setSysnMsgResponse(builder)
                .build();
        channel.writeAndFlush(imMessage);
    }

    private void generalMsgRequest(Channel channel,IMMessageProto.IMMessage imMessage){
        IMMessageProto.IMMessage.GeneralMsgRequest generalMsgRequest=imMessage.getGeneralMsgRequest();
        Long from=ServerContext.getUserByToken(imMessage.getToken()).getId();
        if(null!=from){
            long to=imMessage.getTo();
            IMMessageProto.IMMessage.GeneralMsgRespone generalMsgRespone=IMMessageProto.IMMessage.GeneralMsgRespone.newBuilder()
                    .setInfo(generalMsgRequest.getInfo())
                    .build();
            IMMessageProto.IMMessage imMessageResponse=IMMessageProto.IMMessage.newBuilder()
                    .setFrom(from)
                    .setTo(to)
                    .setType(IMMessageProto.IMMessage.MsgType.GeneralMsg)
                    .setGeneralMsgRespone(generalMsgRespone)
                    .build();
            for(Channel c:ServerContext.getAllChannel()){
                c.writeAndFlush(imMessageResponse);
            }
        }
    }

    private void loginRequest(Channel channel,IMMessageProto.IMMessage imMessage){
        IMMessageProto.IMMessage.LoginRequest loginRequest=imMessage.getLoginRequest();
        String username=loginRequest.getUsername();
        String password=loginRequest.getPassword();
        IMMessageProto.IMMessage.ResultState state=IMMessageProto.IMMessage.ResultState.Fail;
        String message=null;
        Long token=null;
        Long id=null;
        if(null==username||!username.equals(password)){
            message="username password is not matching";
        }else if(ServerContext.hasUser(username)){
            message=username+" is exist";
        }else if(ServerContext.hasChannel(channel)){
            message=username+" you are logined,can't login again";
        }else {
            id=(long)username.hashCode();
            User user=new User();
            user.setId(id);
            user.setUsername(username);
            token= IdGenerate.getUID();
            boolean result=ServerContext.loginUser(token,user,channel);
            if(result){
                state=IMMessageProto.IMMessage.ResultState.Success;
                loginNotice(channel,user,IMMessageProto.IMMessage.NoticeType.login);
            }else {
                message="login fail,please login again";
            }
        }
        IMMessageProto.IMMessage.LoginResponse.Builder builder=IMMessageProto.IMMessage.LoginResponse.newBuilder();
        if(null!=message){
            builder.setMessage(message);
        }
        if(null!=token){
            builder.setId(id);
        }
        builder.setState(state);
        IMMessageProto.IMMessage.Builder builder2=IMMessageProto.IMMessage.newBuilder();
        if(null!=token){
            builder2.setToken(token);
        }
        IMMessageProto.IMMessage imMessageResponse= builder2.setType(IMMessageProto.IMMessage.MsgType.Login)
                .setLoginResponse(builder)
                .build();
        channel.writeAndFlush(imMessageResponse);
    }

    //登录消息通知
    private void loginNotice(Channel channel, User user, IMMessageProto.IMMessage.NoticeType noticeType){
        if(null!=user){
            if(noticeType==IMMessageProto.IMMessage.NoticeType.login){
                System.out.println(user.getUsername()+" is login.");
            }else {
                System.out.println(user.getUsername()+" is loginout.");
            }
            IMMessageProto.IMMessage.User userMsg=IMMessageProto.IMMessage.User.newBuilder()
                    .setId(user.getId())
                    .setUsername(user.getUsername())
                    .build();
            IMMessageProto.IMMessage.NoticeMsg noticeMsg=IMMessageProto.IMMessage.NoticeMsg.newBuilder()
                    .addUsers(userMsg)
                    .setType(noticeType)
                    .build();
            IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                    .setType(IMMessageProto.IMMessage.MsgType.Notice)
                    .setNoticeMsg(noticeMsg)
                    .build();
            for(Channel c:ServerContext.getAllChannel()){
                if(c!=channel){
                    c.writeAndFlush(imMessage);
                }
            }
        }
    }
}
