package com.vmarienkov.utils;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

/**
 * Created by vmarienkov on 1/10/2017.
 */
@RunWith(AndroidJUnit4.class)
public class ExecutorTest {

    private static final String TAG = ExecutorTest.class.getSimpleName();

    private Executor executor;

    @Before
    public void setUp() throws Exception {
        executor = new Executor("test");
    }

    @After
    public void tearDown() throws Exception {
        executor.stop();
    }

    @Test(timeout = 5000)
    public void testEnqueue() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        executor.obtain(new Executor.Callable<String>() {
            @Override
            public String call() {
                return "Test";
            }
        }).callback(new Executor.Callback<String>() {
            @Override
            public void call(String arg) {
                assertEquals("Test", arg);
                signal.countDown();
            }
        }).enqueue();
        signal.await();
    }

    @Test(timeout = 5000)
    public void testDelayedEnqueue() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        executor.obtain(new Executor.Callable<String>() {
            @Override
            public String call() {
                return "Test";
            }
        }).callback(new Executor.Callback<String>() {
            @Override
            public void call(String arg) {
                assertEquals("Test", arg);
                signal.countDown();
            }
        }).delayed(3000).enqueue();
        signal.await();
    }

    @Test(timeout = 5000)
    public void testAtTimeEnqueue() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        executor.obtain(new Executor.Callable<String>() {
            @Override
            public String call() {
                return "Test";
            }
        }).callback(new Executor.Callback<String>() {
            @Override
            public void call(String arg) {
                assertEquals("Test", arg);
                signal.countDown();
            }
        }).atTime(SystemClock.uptimeMillis() + 3000).enqueue();
        signal.await();
    }

}