package com.example.defensecommander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class Interceptor {

    private static int count = 0;
    private final int id;
    private static int missileCount = 0;
    private static final String TAG = "Interceptor";
    private final MainActivity mainActivity;
    private ImageView imageview;
    private ObjectAnimator moveX, moveY;
    private final float startX;
    private final float startY;
    private float endX;
    private float endY;
    private static int idVal = -1;
    static final int INTERCEPTOR_BLAST = 120;
    private static final double DISTANCE_TIME = 0.75;

    Interceptor(MainActivity mainActivity, float startX, float startY, float endX, float endY) {
        this.mainActivity = mainActivity;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.id = count++;
        initialize();
    }

    private void initialize() {

        if (missileCount < 3) {
            imageview = new ImageView(mainActivity);
            imageview.setId(idVal--);
            imageview.setImageResource(R.drawable.interceptor);
            imageview.setTransitionName("Interceptor " + id);
            Log.d(TAG, "Interceptor " + id);

            final int www = (int) (imageview.getDrawable().getIntrinsicWidth() * 0.5);

            imageview.setX(startX);
            imageview.setY(startY);
            imageview.setZ(-10);

            endX -= www;
            endY -= www;

            float a = calculateAngle(imageview.getX(), imageview.getY(), endX, endY);

            imageview.setRotation(a);
            mainActivity.getLayout().addView(imageview);

            double distance = Math.sqrt((endY - imageview.getY()) * (endY - imageview.getY()) + (endX - imageview.getX()) * (endX - imageview.getX()));


            moveX = ObjectAnimator.ofFloat(imageview, "x", endX);
            moveX.setInterpolator(new AccelerateInterpolator());
            moveX.setDuration((long) (distance * DISTANCE_TIME));

            moveY = ObjectAnimator.ofFloat(imageview, "y", endY);
            moveY.setInterpolator(new AccelerateInterpolator());
            moveY.setDuration((long) (distance * DISTANCE_TIME));

            moveX.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainActivity.getLayout().removeView(imageview);
                    makeBlast();
                    missileCount--;
                }
            });
        }
        else {
            Log.d(TAG, "I'm sorry, there are already " + missileCount + " missiles in the air.");
        }
    }

    private void makeBlast() {
        SoundPlayer.getInstance().start("interceptor_blast");
        final ImageView explodeView = new ImageView(mainActivity);
        explodeView.setImageResource(R.drawable.i_explode);

        explodeView.setTransitionName("Interceptor blast");

        float w = explodeView.getDrawable().getIntrinsicWidth();
        explodeView.setX(this.getX() - (w/2));

        explodeView.setY(this.getY() - (w/2));

        explodeView.setZ(-15);

        mainActivity.getLayout().addView(explodeView);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(explodeView, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(explodeView);
            }
        });
        alpha.start();

        mainActivity.applyInterceptorBlast(this, imageview.getId());
    }

    void launch() {
        if (missileCount < 3) {
            moveX.start();
            moveY.start();
            missileCount++;
        }
        else{
            Log.d(TAG, "I'm sorry, there are already " + missileCount + " missiles in the air.");
        }
    }

    float getX() {
        int xVar = imageview.getWidth() / 2;
        return imageview.getX() + xVar;
    }

    float getY() {
        int yVar = imageview.getHeight() / 2;
        return imageview.getY() + yVar;
    }

    private float calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (180.0f - angle);

    }
}
