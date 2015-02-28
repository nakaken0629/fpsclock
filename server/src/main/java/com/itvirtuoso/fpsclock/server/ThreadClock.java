package com.itvirtuoso.fpsclock.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by kenji on 15/02/28.
 */
public class ThreadClock implements Runnable {
    private Socket socket;

    public ThreadClock(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("connected from " + this.socket.getInetAddress().toString());
            runInner();
        } catch (IOException e) {
            System.out.println("disconnected from " + this.socket.getInetAddress().toString());
        }
    }

    private void runInner() throws IOException {
        long nextTime = 0;
        while (true) {
            long currentTime = System.currentTimeMillis();
            if (currentTime < nextTime) {
                Thread.yield();
                continue;
            }
            ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);
            buffer.putLong(currentTime);
            socket.getOutputStream().write(buffer.array());
            nextTime = currentTime + 16;
        }
    }
}
