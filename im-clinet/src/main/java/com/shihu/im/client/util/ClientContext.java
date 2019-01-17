package com.shihu.im.client.util;

import com.shihu.im.common.define.User;
import com.shihu.im.common.msg.IMMessageProto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientContext {
    private static User user=new User();
    private static Long token;
    private static Map<Long,String> userIdNAmeMap=new ConcurrentHashMap<>();

    public static String getUsernameById(Long id){
        return userIdNAmeMap.get(id);
    }

    public static void initUserIdNameMap(IMMessageProto.IMMessage.SysnMsgResponse sysnMsgResponse){
        List<IMMessageProto.IMMessage.User> list=sysnMsgResponse.getUsersList();
        userIdNAmeMap.clear();
        if(null!=list){
            for(IMMessageProto.IMMessage.User user:list){
                userIdNAmeMap.put(user.getId(),user.getUsername());
            }
        }

    }

    public static void removeUserById(Long id){
        userIdNAmeMap.remove(id);
    }

    public static void addUser(User user){
        userIdNAmeMap.put(user.getId(),user.getUsername());
    }

    public static Long getIdByName(String name){
        Long id=null;
        List<String> list=new LinkedList<>();
        for(Map.Entry<Long,String> entry:userIdNAmeMap.entrySet()){
            if(name.equals(entry.getValue())){
                id=entry.getKey();
                break;
            }
        }
        return id;
    }

    public static List<String> getNames(){
        List<String> list=new LinkedList<>();
        for(Map.Entry<Long,String> entry:userIdNAmeMap.entrySet()){
            list.add(entry.getValue());
        }
        return list;
    }
    public static List<String> getNamesProfix(String profix){
        List<String> list=new LinkedList<>();
        for(Map.Entry<Long,String> entry:userIdNAmeMap.entrySet()){
            if(entry.getValue().startsWith(profix)){
                list.add(entry.getValue());
            }
        }
        return list;
    }


    public static Long getToken() {
        return token;
    }

    public static void setToken(Long token) {
        ClientContext.token = token;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        ClientContext.user = user;
    }
}
