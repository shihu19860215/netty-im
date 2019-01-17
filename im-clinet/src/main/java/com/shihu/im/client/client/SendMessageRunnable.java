package com.shihu.im.client.client;

import com.shihu.im.client.util.ClientContext;
import com.shihu.im.common.define.User;
import com.shihu.im.common.msg.IMMessageProto;
import io.netty.channel.Channel;

import javax.sound.midi.Soundbank;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class SendMessageRunnable implements Runnable {
    public Channel channel;

    public SendMessageRunnable(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            try {
                String str=scanner.readLine();
                if(null!=str&&str.length()>0){
                    if("help".equals(str)){
                        System.out.println("-- login:?username:?password");
                        System.out.println("-- loginout");
                        System.out.println("-- users");
                        System.out.println("-- send:>info");
                        System.out.println("-- sendto:?toname:?info");
                        System.out.println("-- findusername:?profix");
                        System.out.println("-- exit");
                    }else if("users".equals(str)){
                        users();
                    }else if("loginout".equals(str)){
                        loginout();
                    }else if("exit".equals(str)){
                        loginout();
                        System.out.println("system exit");
                        System.exit(1);
                    }else{
                        String[] strs=str.split(":");
                        if(strs.length>1){
                            switch (strs[0]){
                                case "login":{
                                    if(strs.length==3){
                                        login(strs[1],strs[2]);
                                    }else {
                                        System.out.println("error parm!(exp:login:username:password)");
                                    }
                                    break;
                                }
                                case "send":{
                                    send(str.substring(strs[0].length()+1));
                                    break;
                                }
                                case "sendto":{
                                    if(strs.length>2){
                                        String toName=strs[1];
                                        String info=str.substring(strs[0].length()+strs[1].length()+2);
                                        sendto(toName,info);
                                    }
                                    break;
                                }
                                case "findusername":{
                                    findusername(str.substring(strs[0].length()+1));
                                    break;
                                }
                                default:{
                                    System.out.println("command is not a right command,please input help.");
                                    break;
                                }
                            }
                        }else {
                            System.out.println("command is not a right command,please input help.");
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void loginout(){
        if(null==ClientContext.getToken()){
            System.out.print("-- ");
            System.out.println("you are not login!please login first");
            return;
        }
        IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                .setType(IMMessageProto.IMMessage.MsgType.LoginOut)
                .build();
        channel.writeAndFlush(imMessage);
        ClientContext.setToken(null);
        ClientContext.setUser(new User());
        System.out.println("loginout success");
    }

    private void sendto(String toName,String info){
        if(null==ClientContext.getToken()){
            System.out.print("-- ");
            System.out.println("you are not login!please login first");
            return;
        }
        Long id=ClientContext.getIdByName(toName);
        if(null==id){
            System.out.print("-- ");
            System.out.println("not find username:"+toName);
            return;
        }
        IMMessageProto.IMMessage.GeneralMsgRequest generalMsgRequest=IMMessageProto.IMMessage.GeneralMsgRequest.newBuilder()
                .setInfo(info)
                .build();
        IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                .setGeneralMsgRequest(generalMsgRequest)
                .setType(IMMessageProto.IMMessage.MsgType.GeneralMsg)
                .setTo(id)
                .setToken(ClientContext.getToken())
                .build();
        channel.writeAndFlush(imMessage);

    }

    private void send(String info){
        if(null==ClientContext.getToken()){
            System.out.print("-- ");
            System.out.println("you are not login!please login first");
            return;
        }
        IMMessageProto.IMMessage.GeneralMsgRequest generalMsgRequest=IMMessageProto.IMMessage.GeneralMsgRequest.newBuilder()
                .setInfo(info)
                .build();
        IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                .setGeneralMsgRequest(generalMsgRequest)
                .setType(IMMessageProto.IMMessage.MsgType.GeneralMsg)
                .setToken(ClientContext.getToken())
                .build();
        channel.writeAndFlush(imMessage);
    }
    private void findusername(String profix){
        System.out.print("-- ");
        if(null==ClientContext.getToken()){
            System.out.println("you are not login!please login first");
            return;
        }
        List<String> nameList=ClientContext.getNamesProfix(profix);
        int i=0;
        for(;i<nameList.size()-1;i++){
            System.out.print(nameList.get(i)+",");
        }
        System.out.println(nameList.get(i));
    }

    private void users(){
        System.out.print("-- ");
        if(null==ClientContext.getToken()){
            System.out.println("you are not login!please login first");
            return;
        }
        List<String> nameList=ClientContext.getNames();
        int i=0;
        for(;i<nameList.size()-1;i++){
            System.out.print(nameList.get(i)+",");
        }
        System.out.println(nameList.get(i));
    }

    private void login(String username,String password){
        ClientContext.getUser().setUsername(username);
        IMMessageProto.IMMessage.LoginRequest loginRequest=IMMessageProto.IMMessage.LoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();
        IMMessageProto.IMMessage imMessage=IMMessageProto.IMMessage.newBuilder()
                .setLoginRequest(loginRequest)
                .setType(IMMessageProto.IMMessage.MsgType.Login)
                .build();
        channel.writeAndFlush(imMessage);
    }
}
