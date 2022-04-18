package com.example.defensecommander;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class Missile {

        private final MainActivity mainActivity;
        private final ImageView imageView;
        private final AnimatorSet aSet = new AnimatorSet();
        private final int screenHeight;
        private final int screenWidth;
        private final long screenTime;
        private static final String TAG = "Missile";
        private final boolean hit = false;
        int startY;
        int endY;
        int startX;
        int endX;

        Missile(int screenWidth, int screenHeight, long screenTime, final MainActivity mainActivity) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.screenTime = screenTime;
            this.mainActivity = mainActivity;

            imageView = new ImageView(mainActivity);

            startY = (-100);
            endY = screenHeight;
            startX = (int)((Math.random() * screenWidth));
            endX = (int) (Math.random() * screenWidth);

            imageView.setX(startX);
            imageView.setY(startY);

            float a = calculateAngle(startX, startY, endX, endY);

            imageView.setZ(-10);
            imageView.setRotation(a);

            mainActivity.runOnUiThread(() -> mainActivity.getLayout().addView(imageView));

        }

        AnimatorSet setData(final int drawId) {

            mainActivity.runOnUiThread(() -> imageView.setImageResource(drawId));

            ObjectAnimator yAnim = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
            yAnim.setInterpolator(new LinearInterpolator());
            yAnim.setDuration(screenTime);
            yAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainActivity.runOnUiThread(() -> {
                        if (!hit) {
                            mainActivity.getLayout().removeView(imageView);
                            mainActivity.removePlane(Missile.this);
                        }
                        Log.d(TAG, "run: NUM VIEWS " +
                                mainActivity.getLayout().getChildCount());
                    });

                }
            });


            ObjectAnimator xAnim = ObjectAnimator.ofFloat(imageView, "x", startX, endX);
            xAnim.setInterpolator(new LinearInterpolator());
            xAnim.setDuration(screenTime);
            xAnim.addUpdateListener(listener -> {
            if ((float)(yAnim.getAnimatedValue()) > (screenHeight * 0.85)){
                makeGroundBlast();
                mainActivity.getLayout().removeView(imageView);
                mainActivity.removePlane(Missile.this);
            }
        });
            aSet.playTogether(xAnim, yAnim);
            return aSet;
        }

        public float calculateAngle(double x1, double y1, double x2, double y2) {
            double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
            // Keep angle between 0 and 360
            angle = angle + Math.ceil(-angle / 360) * 360;
            return (float) (190.0f - angle);
         }

        void stop() {
            aSet.cancel();
        }

        float getX() {
            return imageView.getX();
        }

        float getY() {
            return imageView.getY();
        }

        float getWidth() {
            return imageView.getWidth();
        }

        float getHeight() {
            return imageView.getHeight();
        }

        void interceptorBlast(float x, float y) {

            final ImageView iv = new ImageView(mainActivity);
            iv.setImageResource(R.drawable.explode);

            iv.setTransitionName("Missile Intercepted Blast");

            int w = imageView.getDrawable().getIntrinsicWidth();
            int offset = (int) (w * 0.5);

            iv.setX(x - offset);
            iv.setY(y - offset);
            iv.setRotation((float) (360.0 * Math.random()));

            aSet.cancel();

            mainActivity.getLayout().removeView(imageView);
            mainActivity.getLayout().addView(iv);

            final ObjectAnimator alpha = ObjectAnimator.ofFloat(iv, "alpha", 0.0f);
            alpha.setInterpolator(new LinearInterpolator());
            alpha.setDuration(3000);
            alpha.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainActivity.getLayout().removeView(imageView);
                }
            });
            alpha.start();
        }

    private void makeGroundBlast() {
        SoundPlayer.getInstance().start("missile_miss");
        final ImageView explodeView = new ImageView(mainActivity);
        explodeView.setImageResource(R.drawable.explode);

        explodeView.setTransitionName("Ground Blast");

        float w = imageView.getDrawable().getIntrinsicWidth();
        int offset = (int) (w * 0.5);

        explodeView.setX(this.getX() - offset);

        explodeView.setY(this.getY() - offset);

        explodeView.setZ(-15);

        aSet.cancel();

        mainActivity.getLayout().removeView(imageView);
        mainActivity.getLayout().addView(explodeView);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(explodeView, "alpha", 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(imageView);
            }
        });
        alpha.start();

        mainActivity.applyMissileBlast(this, explodeView.getId());
    }

}
