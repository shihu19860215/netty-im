package com.shihu.im.server.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class ImServer {
    final private static Logger LOGGER=Logger.getLogger(ImServer.class.getName());
    final private static int nettyPort=9000;
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettyPort))
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childHandler(new IMServerInitializer());
        ChannelFuture future=bootstrap.bind().sync();
        if(future.isSuccess()){
            LOGGER.info("启动 cim server 成功");
        }

    }

    public static void main(String[] args) throws InterruptedException {
        new ImServer().start();
    }
}
