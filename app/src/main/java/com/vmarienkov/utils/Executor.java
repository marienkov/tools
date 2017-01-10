package com.vmarienkov.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Executor implements Handler.Callback {
    private static final String TAG = "Executor";

    private static final int MSG_WORK_TASK = 1;
    private static final int MSG_CALLBACK = 2;

    private enum SendAs{ PLAIN, AT_TIME, DELAYED }

    private Handler workerHandler;
    private Handler uiHandler;
    private HandlerThread workThread;

    private volatile boolean stopped;

    public Executor(String name) {
        workThread = new HandlerThread(name);
        workThread.start();
        workerHandler = new Handler(workThread.getLooper(), this);
        uiHandler = new Handler(Looper.getMainLooper(), this);
    }

    public <Result> MessageBuilder<Result> obtain(Callable<Result> task) {
        return new MessageBuilder<Result>().task(task);
    }


    @Override
    protected void finalize() throws Throwable {
        try {
            stop();
        } catch (Throwable e) {}
        super.finalize();
    }

    public void stop() {
        stopped = true;

        workerHandler.removeCallbacksAndMessages(null);
        workerHandler.getLooper().quit();
        workThread.interrupt();

        uiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (stopped) {
            return false;
        }

        switch (msg.what) {
            case MSG_WORK_TASK: {
                final Task<?> task = (Task) msg.obj;
                task.callTask();

                if (task.hasCallback()) {
                    uiHandler.sendMessage(uiHandler.obtainMessage(MSG_CALLBACK, task));
                }
                return true;
            }
            case MSG_CALLBACK: {
                final Task<?> task = (Task) msg.obj;
                task.callCallback();
                return true;
            }
        }
        return false;
    }

    public class MessageBuilder<V> {
        Callable<V> task;
        Callback<V> callback;

        SendAs type = SendAs.PLAIN;
        long time = 0;

        public MessageBuilder<V> task(Callable<V> task) {
            this.task = task;
            return this;
        }

        public MessageBuilder<V> callback(Callback<V> callback) {
            this.callback = callback;
            return this;
        }

        public MessageBuilder<V> asIs() {
            this.type = SendAs.PLAIN;
            this.time = 0;
            return this;
        }

        public MessageBuilder<V> delayed(long delay) {
            this.type = SendAs.DELAYED;
            this.time = delay;
            return this;
        }

        public MessageBuilder<V> atTime(long time) {
            this.type = SendAs.AT_TIME;
            this.time = time;
            return this;
        }

        public void enqueue() {
            if (stopped){
                Log.w(TAG, "Executor stopped no work could be enqueued");
                return;
            }

            if (task == null) {
                throw new RuntimeException("background task not specified");
            }

            final Message msg = workerHandler.obtainMessage(MSG_WORK_TASK, new Task(task, callback));
            switch (type){
                case PLAIN:
                    workerHandler.sendMessage(msg);
                    break;
                case AT_TIME:
                    workerHandler.sendMessageAtTime(msg, time);
                    break;
                case DELAYED:
                    workerHandler.sendMessageDelayed(msg, time);
                    break;
            }
        }
    }

    static class Task<V> {
        V result;
        final Callable<V> task;
        final Callback<V> callback;

        Task(Callable<V> task, Callback<V> callback) {
            this.task = task;
            this.callback = callback;
        }

        boolean hasCallback() {
            return callback != null;
        }

        void callTask() {
            result = task.call();
        }

        void callCallback() {
            if (hasCallback()) {
                callback.call(result);
            }
        }
    }

    public interface Callable<V> {
        V call();
    }

    public interface Callback<V> {
        void call(V arg);
    }
}
