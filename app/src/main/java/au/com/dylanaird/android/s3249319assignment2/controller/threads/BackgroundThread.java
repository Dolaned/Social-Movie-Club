package au.com.dylanaird.android.s3249319assignment2.controller.threads;

/**
 * Created by Dylan on 13/10/2015.
 */
public class BackgroundThread extends Thread {

    Runnable runnable;

    public BackgroundThread(Runnable r) {
        runnable = r;
    }

    public void setRunnable(Runnable r) {
        runnable = r;
    }

    public void runThread() {
        this.runnable.run();
    }


}
