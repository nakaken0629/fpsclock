package com.itvirtuoso.fpsclock;

/**
 * Created by kenji on 15/02/28.
 */
public interface ClockListener {
    void onConnectionSuccess();
    void onConnectionFail(Exception e);
    void onDraw(long time);
    void onConnectionClose();
}
