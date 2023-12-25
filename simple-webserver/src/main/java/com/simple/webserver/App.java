package com.simple.webserver;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        System.out.println("Starting web server on port " + port);
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/test", new StoreHandler());
        httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        httpServer.start();
    }

}
