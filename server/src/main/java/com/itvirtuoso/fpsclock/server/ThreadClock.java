package com.itvirtuoso.fpsclock.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kenji on 15/02/28.
 */
public class ThreadClock {
    private Socket mSocket;
    private ExecutorService mSender;
    private ExecutorService mReceiver;

    public ThreadClock(Socket socket) {
        mSocket = socket;
    }

    public void start() {
        mSender = Executors.newSingleThreadExecutor();
        mSender.execute(new Sender());
        mSender.shutdown();

        mReceiver = Executors.newSingleThreadExecutor();
        mReceiver.execute(new Receiver());
        mReceiver.shutdown();
    }

    private class Sender implements Runnable {
        @Override
        public void run() {
            try {
                runInner();
            } catch (IOException e) {
                System.out.println(e.toString());
            } finally {
                if (!mReceiver.isTerminated()) {
                    mReceiver.shutdownNow();
                }
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
                ByteBuffer buffer = ByteBuffer.allocate(1 + Long.SIZE / 8);
                buffer.put((byte)0);
                buffer.putLong(currentTime);
                mSocket.getOutputStream().write(buffer.array());
                nextTime = currentTime + 16;
            }
        }
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                runInner();
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (!mSender.isTerminated()) {
                    mSender.shutdownNow();
                }
            }
        }

        private void runInner() throws IOException {
            while (true) {
                byte[] data = new byte[8];
                int result = mSocket.getInputStream().read(data);
                if (result < 0) {
                    break;
                }
                mSocket.getOutputStream().write((byte)1);
                mSocket.getOutputStream().write(data);
            }
        }
    }
}
