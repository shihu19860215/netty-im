package com.shihu.im.server;


import com.shihu.im.server.server.ImServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.Inet4Address;


@SpringBootApplication
public class Application  {
    public static void main(String[] args) throws InterruptedException {
        new ImServer().start();
    }


}
