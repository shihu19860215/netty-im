package com.shihu.im.client.client;

import com.shihu.im.common.msg.IMMessageProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class IMClientInitializer extends ChannelInitializer<Channel> {
    final private static IMClientHandle imServerHandle=new IMClientHandle();
    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast((new IdleStateHandler(0, 28, 0, TimeUnit.SECONDS)))
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(IMMessageProto.IMMessage.getDefaultInstance()))
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(imServerHandle);

    }
}
