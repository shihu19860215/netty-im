package com.shihu.im.server.util;

import com.shihu.im.common.define.User;
import io.netty.channel.Channel;

import java.util.*;

public class ServerContext {
    private static Map<Channel,Long> channelTokenMap=new HashMap<>();
    private static Map<Long,Channel> tokenChannelMap=new HashMap<>();
    private static Map<Long,User> tokenUserMap=new HashMap<>();
    private static Set<String> usernameSet=new HashSet<>();

    public synchronized static boolean loginUser(Long token,User user,Channel channel){
        if(usernameSet.contains(user.getUsername())){
            return false;
        }
        channelTokenMap.put(channel,token);
        tokenChannelMap.put(token,channel);
        tokenUserMap.put(token,user);
        usernameSet.add(user.getUsername());
        return true;
    }

    public synchronized static void loginOut(Channel channel){
        Long token=channelTokenMap.get(channel);
        tokenChannelMap.remove(token);
        User user=tokenUserMap.get(token);
        tokenUserMap.remove(token);
        if(null!=user){
            usernameSet.remove(user.getUsername());
        }
        channelTokenMap.remove(channel);
    }

    public static Long getTokenByChannel(Channel channel){
        return channelTokenMap.get(channel);
    }

    /*public static Channel getChannelById(Long id){
        for(Map.Entry<Long,User> entry:tokenUserMap.entrySet()){
            if(id.equals(entry.getValue().getId())){
                return tokenChannelMap.get(entry.getKey());
            }
        }
        return null;
    }*/
    public static List<User> getALlUser(){
        List<User> userList=new ArrayList<>();
        for(Map.Entry<Long,User> entry:tokenUserMap.entrySet()){
            userList.add(entry.getValue());
        }
        return userList;
    }

    public static User getUserByToken(Long token){
        return tokenUserMap.get(token);
    }

    public static Set<Channel> getAllChannel(){
        return channelTokenMap.keySet();
    }
    public static boolean hasUser(String username){
        return usernameSet.contains(username);
    }
    public static boolean hasChannel(Channel channel){
        return channelTokenMap.containsKey(channel);
    }
}
