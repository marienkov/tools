package com.cloudmade.mytoolsapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vmarienkov.utils.Executor;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Executor executor = new Executor("test");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor.obtain(new Executor.Callable<String>() {
            @Override
            public String call() {
                return new Date(System.currentTimeMillis()) + "";
            }
        }).callback(new Executor.Callback<String>() {
            @Override
            public void call(String arg) {
                Log.d(TAG, "call: " + arg);
            }
        }).delayed(5000).enqueue();

        Log.d(TAG, "onCreate: at " + new Date(System.currentTimeMillis()));
    }
}
