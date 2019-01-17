package com.shihu.im.common.util;

public class IdGenerate extends IdGenerateAbstract{
    final private static IdGenerate idGenerate=new IdGenerate();
    private IdGenerate(){
        super(1,4);
    }
    public static long getUID(){
        return idGenerate.getUniqueID();
    }
    @Override
    protected Integer getServerId() {
        return 1;
    }
}
