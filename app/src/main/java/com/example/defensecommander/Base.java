package com.example.defensecommander;

import static com.example.defensecommander.Interceptor.INTERCEPTOR_BLAST;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;

public class Base {

        private final MainActivity mainActivity;
        private final ImageView imageView;
        private final AnimatorSet aSet = new AnimatorSet();
        private final int screenHeight;
        private final int screenWidth;
        private final long screenTime;
        private final String id;
        private static final String TAG = "Plane";
        private final boolean hit = false;

        Base(int screenWidth, int screenHeight, long screenTime, final MainActivity mainActivity, String id) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.screenTime = screenTime;
            this.mainActivity = mainActivity;
            this.id = id;


            imageView = new ImageView(mainActivity);
            imageView.setX(-500);

            mainActivity.runOnUiThread(() -> mainActivity.getLayout().addView(imageView));

        }

        AnimatorSet setData(final int drawId) {
            mainActivity.runOnUiThread(() -> imageView.setImageResource(drawId));

            int startY = (int) (Math.random() * screenHeight * 0.8);
            int endY = (startY + (Math.random() < 0.5 ? 150 : -150));

            ObjectAnimator xAnim = ObjectAnimator.ofFloat(imageView, "x", -200, (screenWidth + 200));
            xAnim.setInterpolator(new LinearInterpolator());
            xAnim.setDuration(screenTime);
            xAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainActivity.runOnUiThread(() -> {
                        if (!hit) {
                            mainActivity.getLayout().removeView(imageView);
                            //mainActivity.removeBase(Base.this);
                        }
                        Log.d(TAG, "run: NUM VIEWS " +
                                mainActivity.getLayout().getChildCount());
                    });

                }
            });

            ObjectAnimator yAnim = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
            yAnim.setInterpolator(new LinearInterpolator());
            yAnim.setDuration(screenTime);

            aSet.playTogether(xAnim, yAnim);
            return aSet;

        }

        void stop() {
            aSet.cancel();
        }

        float getX() {
            return (imageView.getX() + ((float)0.5 * imageView.getWidth()));
        }

        float getY() {
            return (imageView.getY() + ((float)0.5 * imageView.getHeight()));
        }

        String getID(){
            return id;
        }
        float getWidth() {
            return imageView.getWidth();
        }

        float getHeight() {
            return imageView.getHeight();
        }

        void destruct(float x, float y) {

            mainActivity.getLayout().removeView(imageView);
            final ImageView iv = new ImageView(mainActivity);
            iv.setImageResource(R.drawable.blast);

            iv.setTransitionName("Base Destruct");

            iv.setX(x);
            iv.setY(y);

            aSet.cancel();

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

}
