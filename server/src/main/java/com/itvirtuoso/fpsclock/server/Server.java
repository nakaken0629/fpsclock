package com.itvirtuoso.fpsclock.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);
        listener.bind(new InetSocketAddress(5000));
        System.out.println("Start server...");
        while (true) {
            Socket socket = listener.accept();
            ThreadClock clock = new ThreadClock(socket);
            clock.start();
        }
    }
}
