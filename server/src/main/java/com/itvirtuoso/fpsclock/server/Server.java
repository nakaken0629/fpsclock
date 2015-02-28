package com.itvirtuoso.fpsclock.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);
        listener.bind(new InetSocketAddress(5000));
        System.out.println("Start server...");
        while (true) {
            Socket socket = listener.accept();
            ThreadClock clock = new ThreadClock(socket);
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(clock);
            service.shutdown();
        }
    }
}
