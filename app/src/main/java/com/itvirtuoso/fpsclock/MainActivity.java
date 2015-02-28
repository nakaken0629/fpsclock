package com.itvirtuoso.fpsclock;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    public void onConnectionClose() {
        /* nop */
    }

    public static class PlaceholderFragment extends Fragment {
        private TextView mDateText;
        private TextView mTimeText;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_digital, container, false);
            mDateText = (TextView) rootView.findViewById(R.id.date_text);
            mTimeText = (TextView) rootView.findViewById(R.id.time_text);
            return rootView;
        }

        public void draw(long time) {
            Date date = new Date(time);
            mDateText.setText(new SimpleDateFormat("yyyy/MM/dd").format(date));
            mTimeText.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(date));
        }
    }
}
