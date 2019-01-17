package com.shihu.im.common.util;



/**
 *
 * 根据系统自己设置 serviceIdBitCount，indexBitCount
 * serviceIdBitCount indexBitCount 加起来不要超过 28
 * serviceIdBitCount为服务id所占位数
 * 默认4位(0-15)
 * indexBitCount为每秒内生成最大数
 * 进过测试(100个线程生成1000万个id，时间所需855毫秒)
 * 进过测试(100个线程生成5000万个id，时间所需3856毫秒)
 * indexBitCount=14时速度最快
 */
public abstract class IdGenerateAbstract {
    /**
     * 服务器id所占位数
     */
    private int serviceIdBitCount=4;
    /**
     * 每毫秒生成id数
     */
    private int indexBitCount=14;
    /**
     * 2018-01-01 00:00:00
     */
    private static long ID_BEGIN_TIME= 1514736000000L;

    /**
     * 服务器id
     */
    private static volatile Integer serviceId;
    /**
     * 服务器id位移后的值
     */
    private static volatile Long serviceIdBitShiftValue;
    /**
     * 最后一次index为0时的时间戳
     */
    private volatile long lastCurrentTimeMillis=0L;
    /**
     * 时间戳需要的位移位数
     */
    private int currentTimeMillisDiffBitCount;
    /**
     *  计数累计
     */
    private volatile int i=0;
    /**
     *  计数累计 最大值
     */
    private int indexBit;


    public IdGenerateAbstract() {
        this.currentTimeMillisDiffBitCount=serviceIdBitCount+indexBitCount;
        this.indexBit=(0x01<<indexBitCount)-1;
    }

    public IdGenerateAbstract(int serviceIdBitCount, int indexBitCount) {
        this.serviceIdBitCount = serviceIdBitCount;
        this.indexBitCount = indexBitCount;
        this.currentTimeMillisDiffBitCount=serviceIdBitCount+indexBitCount;
        this.indexBit=(0x01<<indexBitCount)-1;
    }

    public synchronized long getUniqueID() {
        long now=System.currentTimeMillis();
        if(now!=lastCurrentTimeMillis){
            lastCurrentTimeMillis=now;
            i=0;
            long destID =  now- ID_BEGIN_TIME;
            destID = (destID << currentTimeMillisDiffBitCount) + getServiceIdBitShiftValue() ;
            return destID;
        }
        if(i++>indexBit){
            i=0;
            while (lastCurrentTimeMillis==now){
                now=System.currentTimeMillis();
            }
            lastCurrentTimeMillis=now;
        }
        long destID =  now- ID_BEGIN_TIME;
        destID = (destID << currentTimeMillisDiffBitCount) + i +getServiceIdBitShiftValue();
        return destID;
    }

    private Long getServiceIdBitShiftValue(){
        if(null==serviceIdBitShiftValue){
            initServiceIdAndServiceIdBitShiftValue();
        }
        return serviceIdBitShiftValue;
    }

    private Integer initServiceIdAndServiceIdBitShiftValue(){
        if(serviceId==null){
            synchronized (this){
                if(serviceId==null){
                    Integer id=getServerId();
                    if(null==id){
                        throw new RuntimeException("获取服务器Id失败");
                    }
                    serviceIdBitShiftValue=new Long(id<<indexBitCount);
                    serviceId=id;
                }
            }
        }
        return serviceId;
    }



    abstract protected Integer getServerId();
}
