package com.itvirtuoso.fpsclock;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements ClockListener {
    private static final String TAG = MainActivity.class.getName();
    private Handler mHandler;
    private ClockConnection mConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnection = new ClockConnection();
        mConnection.addListener(MainActivity.this);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mConnection.connect(new InetSocketAddress("192.168.11.103", 5000));
                return null;
            }
        }.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mConnection.disconnect();
                mConnection = null;
                return null;
            }
        }.execute();
    }

    @Override
    public void onConnectionSuccess() {
        Toast.makeText(MainActivity.this, "接続に成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFail(Exception e) {
        Toast.makeText(MainActivity.this, "接続に失敗", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDraw(long time) {
        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().getFragments().get(0);
        if (fragment != null) {
            fragment.draw(time);
        }
    }

    @Override
    public void pong(long time) {
        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().getFragments().get(0);
        if (fragment != null) {
            fragment.pong(time);
        }
    }

    @Override
    public void onConnectionClose() {
        /* nop */
    }

//    @Override
    public void onPing() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mConnection.ping();
                } catch (IOException e) {
                    Log.e(TAG, "pingでエラー", e);
                }
                return null;
            }
        }.execute();
    }

    public static class PlaceholderFragment extends Fragment {
//        private static final String TAG = PlaceholderFragment.class.getName();

        private MainActivity mListener;

        private TextView mPingText;
        private Button mPingButton;
        private TextView mDateText;
        private TextView mTimeText;

        public PlaceholderFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mListener = (MainActivity) activity;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_digital, container, false);
            mPingText = (TextView) rootView.findViewById(R.id.ping_text);
            mPingButton = (Button) rootView.findViewById(R.id.ping_button);
            mDateText = (TextView) rootView.findViewById(R.id.date_text);
            mTimeText = (TextView) rootView.findViewById(R.id.time_text);

            mPingButton.setOnClickListener(new PingButtonClickListener());
            return rootView;
        }

        public void draw(long time) {
            Date date = new Date(time);
            mDateText.setText(new SimpleDateFormat("yyyy/MM/dd").format(date));
            mTimeText.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(date));
        }

        public void pong(long time) {
            long currentTime = System.currentTimeMillis();
            long delta = currentTime - time;
            Log.d(TAG, "time " + delta + " ms(= " + currentTime + " - " + time + ")");
            mPingText.setText("time " + delta + " ms");
        }

        private class PingButtonClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                mListener.onPing();
            }
        }
    }
}
