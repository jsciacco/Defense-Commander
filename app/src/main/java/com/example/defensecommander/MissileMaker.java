package com.example.defensecommander;

import android.animation.AnimatorSet;
import android.util.Log;

import java.util.ArrayList;

import static com.example.defensecommander.Interceptor.INTERCEPTOR_BLAST;

public class MissileMaker implements Runnable {

    private static final String TAG = "MissileMaker";
    private final MainActivity mainActivity;
    private boolean isRunning;
    private final ArrayList<Missile> activePlanes = new ArrayList<>();
    private final int screenWidth;
    private final int screenHeight;
    private static final int NUM_LEVELS = 5;
    private int level = 1;
    private int count = 0;
    private static long delay = NUM_LEVELS * 1000;


    MissileMaker(MainActivity mainActivity, int screenWidth, int screenHeight) {
        this.mainActivity = mainActivity;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    void setRunning(boolean running) {
        isRunning = running;
        ArrayList<Missile> temp = new ArrayList<>(activePlanes);
        for (Missile p : temp) {
            p.stop();
        }
    }

    @Override
    public void run() {
        setRunning(true);

        try {
            Thread.sleep((long) (delay * 0.5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (isRunning) {

            int resId = pickPlane();

            long planeTime = (long) ((delay * 0.5) + (Math.random() * delay));
            final Missile plane = new Missile(screenWidth, screenHeight, planeTime, mainActivity);
            activePlanes.add(plane);
            final AnimatorSet as = plane.setData(resId);

            mainActivity.runOnUiThread(as::start);
            SoundPlayer.getInstance().start("launch_missile");

            count++;
            if (count > NUM_LEVELS){
                level++;
                Log.d(TAG, "run: LEVEL " + level);
                delay-=500;
                if (delay <= 0){
                    delay = 1;
                }
                mainActivity.setLevel(level);
                Log.d(TAG, "run: LEVEL NOW " + level);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run: DELAY NOW " + delay);
                count = 0;
            }
            long sleepTime = getSleepTime();

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int pickPlane() {
        double d = Math.random();
        if (d < 0.25)
            return R.drawable.missile;
        else if (d < 0.5)
            return R.drawable.missile;
        else if (d < 0.75)
            return R.drawable.missile;
        else
            return R.drawable.missile;

    }

    private long getSleepTime(){
        double d = Math.random();
        if (d < 0.1)
            return 1;
        else if (d < 0.2)
            return (long)(0.5 * (delay));
        else
            return (delay);
    }

    void removePlane(Missile p) {
        activePlanes.remove(p);
    }

    void applyInterceptorBlast(Interceptor interceptor, int id) {
        Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

        float x1 = interceptor.getX();
        float y1 = interceptor.getY();

        Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Missile> nowGone = new ArrayList<>();
        ArrayList<Missile> temp = new ArrayList<>(activePlanes);

        for (Missile m : temp) {

            float x2 = (int) (m.getX() + (0.5 * m.getWidth()));
            float y2 = (int) (m.getY() + (0.5 * m.getHeight()));

            Log.d(TAG, "applyInterceptorBlast:    Missile: " + x2 + ", " + y2);


            float f = (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + f);

            if (f < INTERCEPTOR_BLAST) {

                SoundPlayer.getInstance().start("interceptor_hit_missile");
                mainActivity.incrementScore();
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + f);
                m.interceptorBlast(x2, y2);
                nowGone.add(m);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");


        }

        for (Missile m : nowGone) {
            activePlanes.remove(m);
        }
    }
}
