package com.smartdeviceny.workinprogress;


public class Lock {
    public static final Object lock = new Object();
    public static boolean notified = false;

    public static void pause() {
        synchronized (lock) {
            //Clog.w(TestUtil.testLogTag, "pausing " + Thread.currentThread().getName());
            while (!notified) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                    continue; // recheck and go back to waiting if still not notified
                }
            }
        }
        notified = false;
        //Clog.w(TestUtil.testLogTag, "unpausing " + Thread.currentThread().getName());
    }

    public static void pause(long time) {
        synchronized (lock) {
           // Clog.w(TestUtil.testLogTag, "pausing " + Thread.currentThread().getName());
            if (!notified) {
                try {
                    lock.wait(time);
                } catch (InterruptedException ignored) {
                    // wake up
                }
            }
        }
        notified = false;
        //Clog.w(TestUtil.testLogTag, "unpausing " + Thread.currentThread().getName());
    }

    public static void unpause() {
        //Clog.w(TestUtil.testLogTag, "notify from " + Thread.currentThread().getName());
        synchronized (lock) {
            lock.notifyAll();
            notified = true;
        }
    }

    public static void explicitSleep(long time){
        try {
            //Clog.w(TestUtil.testLogTag, "explicitSleep " + Thread.currentThread().getName());
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}