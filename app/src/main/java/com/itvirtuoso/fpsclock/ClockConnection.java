package com.itvirtuoso.fpsclock;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kenji on 15/02/28.
 */
public class ClockConnection {
    private static final String TAG = ClockConnection.class.getName();

    private Handler mHandler;
    private Socket mSocket;
    private List<ClockListener> mListeners;
    private ExecutorService mReceiverLoop;

    public ClockConnection() {
        mHandler = new Handler();
        mSocket = new Socket();
        mListeners = new ArrayList<>();
    }

    public void addListener(ClockListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void connect(SocketAddress address) {
        try {
            mSocket.connect(address);
            registerReceiverLoop();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ClockListener listener : mListeners) {
                        listener.onConnectionSuccess();
                    }
                }
            });
        } catch (final IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ClockListener listener : mListeners) {
                        listener.onConnectionFail(e);
                    }
                }
            });
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                /* nop */
            } finally {
                mSocket = null;
            }
        }
        for (ClockListener listener : mListeners) {
            listener.onConnectionClose();
        }
    }

    private void registerReceiverLoop() {
        mReceiverLoop = Executors.newSingleThreadExecutor();
        mReceiverLoop.execute(new Receiver());
        mReceiverLoop.shutdown();
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                runInner();
            } catch (SocketException e) {
                Log.e(TAG, "ソケットエラー", e);
            } catch (IOException e) {
                Log.e(TAG, "通信エラー", e);
            } finally {
                if (mSocket != null) {
                    try {
                        mSocket.close();
                    } catch (IOException e) {
                        /* nop */
                    } finally {
                        mSocket = null;
                    }
                }
            }
        }

        private void runInner() throws IOException {
            byte[] data = new byte[8];
            int index = 0;
            while (true) {
                int value = mSocket.getInputStream().read();
                if (value < 0) {
                    break;
                }
                data[index++] = (byte) value;
                if (index < 8) {
                    continue;
                }
                index = 0;
                ByteBuffer buffer = ByteBuffer.wrap(data);
                final long time = buffer.getLong();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (ClockListener listener : mListeners) {
                            listener.onDraw(time);
                        }
                    }
                });
            }
        }
    }
}
