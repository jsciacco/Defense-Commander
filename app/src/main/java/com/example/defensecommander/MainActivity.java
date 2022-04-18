package com.example.defensecommander;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.defensecommander.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Locale;

//J.C. Sciaccotta

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int GAME_OVER_TIME_OUT = 6000;
    public static int screenHeight;
    public static int screenWidth;
    private ViewGroup layout;
    private MissileMaker planeMaker;
    private final ArrayList<Base> activeBases = new ArrayList<>();
    private ImageView launcher1;
    private ImageView launcher2;
    private ImageView launcher3;
    private int scoreValue;
    private int levelValue;
    private TextView score, level;
    private static final int NUM_LEVELS = 5;
    private Base base1;
    private Base base2;
    private Base base3;
    private ActivityMainBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupFullScreen();
        getScreenDimensions();

        layout = findViewById(R.id.layout);
        score = findViewById(R.id.score);
        level = findViewById(R.id.levelText);

        SoundPlayer.getInstance().setupSound(this, "interceptor_blast", R.raw.interceptor_blast, false);
        SoundPlayer.getInstance().setupSound(this, "launch_interceptor", R.raw.launch_interceptor, false);
        SoundPlayer.getInstance().setupSound(this, "interceptor_hit_missile", R.raw.interceptor_hit_missile, false);
        SoundPlayer.getInstance().setupSound(this, "missile_miss", R.raw.missile_miss, false);
        SoundPlayer.getInstance().setupSound(this, "launch_missile", R.raw.launch_missile, false);
        SoundPlayer.getInstance().setupSound(this, "base_blast", R.raw.base_blast, false);

        SoundPlayer.getInstance().start("background");

        new CloudScroller(this, layout, R.drawable.clouds, 60000);

        layout.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                handleTouch(motionEvent.getX(), motionEvent.getY());
            }
            return false;
        });

        int resId1 = R.id.launcher1;
        int resId2 = R.id.launcher2;
        int resId3 = R.id.launcher3;

        long delay = NUM_LEVELS * 1000;
        long planeTime = (long) ((delay * 0.5) + (Math.random() * delay));
        base1 = new Base(screenWidth, screenHeight, planeTime, this, "Base1");
        base2 = new Base(screenWidth, screenHeight, planeTime, this, "Base2");
        base3 = new Base(screenWidth, screenHeight, planeTime, this, "Base3");
        activeBases.add(base1);
        activeBases.add(base2);
        activeBases.add(base3);

        final AnimatorSet bs1 = base1.setData(resId1);
        final AnimatorSet bs2 = base2.setData(resId2);
        final AnimatorSet bs3 = base3.setData(resId3);

        this.runOnUiThread(bs1::start);
        this.runOnUiThread(bs2::start);
        this.runOnUiThread(bs3::start);

        setLevel(1);

        planeMaker = new MissileMaker(this, screenWidth, screenHeight);
        new Thread(planeMaker).start();

    }

    public ViewGroup getLayout() {
        return layout;
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    private void setupFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void incrementScore() {
        scoreValue++;
        score.setText(String.format(Locale.getDefault(), "%d", scoreValue));
    }

    public void setLevel(final int value) {
        runOnUiThread(() -> level.setText(String.format(Locale.getDefault(), "Level: %d", value)));
        levelValue = value;
    }

    public void doStop() {
        binding.gameOverView.setVisibility(View.INVISIBLE);
        binding.levelText.setVisibility(View.INVISIBLE);
        binding.score.setVisibility(View.INVISIBLE);
        binding.button2.setVisibility(View.VISIBLE);
        binding.textViewData.setVisibility(View.VISIBLE);
        ScoreDatabaseHandler dbh =
                new ScoreDatabaseHandler(this, "NUL", -1, 0);
        new Thread(dbh).start();
    }
    public void finishApp(View v) {
        //Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        SoundPlayer.getInstance().stop("background");
        finish();
        System.exit(0);
        //startActivity(intent);
    }
    public void setResults(String s, String t, int tenthScore) {

        if ((t == "") && (scoreValue > tenthScore)){
            this.runOnUiThread(() -> {
                // This is where your UI code goes.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                EditText et;
                et = new EditText(this);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(et);
                builder.setTitle("You are a Top-Player!");
                builder.setMessage("Please enter your initials (up to 3 characters):");
                builder.setPositiveButton("OK", (dialog, id) -> {
                    ScoreDatabaseHandler ebh =
                            new ScoreDatabaseHandler(this, et.getText().toString(), scoreValue, levelValue);
                    new Thread(ebh).start();

                });
                builder.setNegativeButton("Cancel", (dialog, id) -> {
                    binding.textViewData.setText(s);
                    Toast.makeText(MainActivity.this, "You changed your mind!", Toast.LENGTH_SHORT).show();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }
        else {
            binding.textViewData.setText(s);
        }
    }
    public void removePlane(Missile p) {
        planeMaker.removePlane(p);
    }

    public void removeBase(Base b) {
        activeBases.remove(b);
    }

    public void handleTouch(float x1, float y1) {

        if (launcher1 == null)
            launcher1 = findViewById(R.id.launcher1);
        if (launcher2 == null)
            launcher2 = findViewById(R.id.launcher2);
        if (launcher3 == null)
            launcher3 = findViewById(R.id.launcher3);

        float distance1 = 0;
        float distance2 = 0;
        float distance3 = 0;
        float leastDistance = 0;
        double startX = 0;
        double startY = 0;

        if (!activeBases.isEmpty()) {
            for (Base b: activeBases){
                if (b.getID() == "Base1") {
                    float x = (float)Math.sqrt(((y1 - launcher1.getY()) * (y1 - launcher1.getY())) + ((x1 - launcher1.getX()) * (x1 - launcher1.getX())));
                    distance1 = x;
                }
                if (b.getID() == "Base2") {
                    float y = (float)Math.sqrt(((y1 - launcher2.getY()) * (y1 - launcher2.getY())) + ((x1 - launcher2.getX()) * (x1 - launcher2.getX())));
                    distance2 = y;
                }
                if (b.getID() == "Base3") {
                    float z = (float)Math.sqrt(((y1 - launcher3.getY()) * (y1 - launcher3.getY())) + ((x1 - launcher3.getX()) * (x1 - launcher3.getX())));
                    distance3 = z;
                }
            }
        }
        if (distance1 != 0 && distance2 != 0 && distance3 != 0) {
            leastDistance = Math.min(Math.min(distance1, distance2), distance3);
            if (leastDistance == distance1) {
                startX = launcher1.getX() + (0.5 * launcher1.getWidth());
                startY = launcher1.getY() + (0.5 * launcher1.getHeight());
            }
            if (leastDistance == distance2) {
                startX = launcher2.getX() + (0.5 * launcher2.getWidth());
                startY = launcher2.getY() + (0.5 * launcher2.getHeight());
            }
            if (leastDistance == distance3) {
                startX = launcher3.getX() + (0.5 * launcher3.getWidth());
                startY = launcher3.getY() + (0.5 * launcher3.getHeight());
            }
        }
        if (distance1 != 0 && distance2 != 0 && distance3 == 0) {
            leastDistance = Math.min(distance1, distance2);
            if (leastDistance == distance1) {
                startX = launcher1.getX() + (0.5 * launcher1.getWidth());
                startY = launcher1.getY() + (0.5 * launcher1.getHeight());
            }
            if (leastDistance == distance2) {
                startX = launcher2.getX() + (0.5 * launcher2.getWidth());
                startY = launcher2.getY() + (0.5 * launcher2.getHeight());
            }
        }
        if (distance1 != 0 && distance2 == 0 && distance3 != 0) {
            leastDistance = Math.min(distance1, distance3);
            if (leastDistance == distance1) {
                startX = launcher1.getX() + (0.5 * launcher1.getWidth());
                startY = launcher1.getY() + (0.5 * launcher1.getHeight());
            }
            if (leastDistance == distance3) {
                startX = launcher3.getX() + (0.5 * launcher3.getWidth());
                startY = launcher3.getY() + (0.5 * launcher3.getHeight());
            }
        }
        if (distance1 == 0 && distance2 != 0 && distance3 != 0) {
            leastDistance = Math.min(distance2, distance3);

            if (leastDistance == distance2) {
                startX = launcher2.getX() + (0.5 * launcher2.getWidth());
                startY = launcher2.getY() + (0.5 * launcher2.getHeight());
            }
            if (leastDistance == distance3) {
                startX = launcher3.getX() + (0.5 * launcher3.getWidth());
                startY = launcher3.getY() + (0.5 * launcher3.getHeight());
            }
        }
        if (distance1 == 0 && distance2 == 0 && distance3 != 0) {
            startX = launcher3.getX() + (0.5 * launcher3.getWidth());
            startY = launcher3.getY() + (0.5 * launcher3.getHeight());

        }
        if (distance1 == 0 && distance2 != 0 && distance3 == 0) {
            startX = launcher2.getX() + (0.5 * launcher2.getWidth());
            startY = launcher2.getY() + (0.5 * launcher2.getHeight());

        }
        if (distance1 != 0 && distance2 == 0 && distance3 == 0) {
            startX = launcher1.getX() + (0.5 * launcher1.getWidth());
            startY = launcher1.getY() + (0.5 * launcher1.getHeight());

        }
        float a = calculateAngle(startX, startY, x1, y1);
        Log.d(TAG, "handleTouch: " + a);
        Interceptor i = new Interceptor(this,  (float) (startX - 10), (float) (startY - 30), x1, y1);
        SoundPlayer.getInstance().start("launch_interceptor");
        i.launch();
    }

    public float calculateAngle(double x1, double y1, double x2, double y2) {
        double angle = Math.toDegrees(Math.atan2(x2 - x1, y2 - y1));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        return (float) (190.0f - angle);
    }


    public void applyInterceptorBlast(Interceptor interceptor, int id) {
        planeMaker.applyInterceptorBlast(interceptor, id);

        if (launcher1 == null)
            launcher1 = findViewById(R.id.launcher1);
        if (launcher2 == null)
            launcher2 = findViewById(R.id.launcher2);
        if (launcher3 == null)
            launcher3 = findViewById(R.id.launcher3);

        Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

        float x1 = interceptor.getX();
        float y1 = interceptor.getY();
        float x2 = 0;
        float y2 = 0;

        Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Base> nowGone = new ArrayList<>();
        ArrayList<Base> temp = new ArrayList<>(activeBases);

        for (Base b: temp) {

            if (b.getID() == "Base1"){
                x2 = (float) ((launcher1.getX()) + (0.5 * launcher1.getWidth()));
                y2 = (float) ((launcher1.getY()) + (0.5 * launcher1.getHeight()));
            }

            if (b.getID() == "Base2"){
                x2 = (float) ((launcher2.getX()) + (0.5 * launcher2.getWidth()));
                y2 = (float) ((launcher2.getY()) + (0.5 * launcher2.getHeight()));
            }
            if (b.getID() == "Base3"){
                x2 = (float) ((launcher3.getX()) + (0.5 * launcher3.getWidth()));
                y2 = (float) ((launcher3.getY()) + (0.5 * launcher3.getHeight()));
            }

            Log.d(TAG, "applyInterceptorBlast:    Interceptor: " + x2 + ", " + y2);


            float f = (float) Math.sqrt((y2 - y1) * (y2-y1) + (x2-x1) * (x2 - x1));
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + f);

            if (f < 250) {

                SoundPlayer.getInstance().start("base_blast");
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + f);
                b.destruct(x2-150, y2-150);
                nowGone.add(b);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");


        }

        for (Base b : nowGone) {
            if (b.getID() == "Base1"){
                this.getLayout().removeView(launcher1);
            }
            if (b.getID() == "Base2"){
                this.getLayout().removeView(launcher2);
            }
            if (b.getID() == "Base3"){
                this.getLayout().removeView(launcher3);
            }
            activeBases.remove(b);
            Log.d(TAG, "activebases: " + activeBases.size());
        }

        if (activeBases.isEmpty()) {
            planeMaker.setRunning(false);
            CloudScroller.stop();
            binding.gameOverView.setVisibility(View.VISIBLE);
            ImageView imageView = findViewById(R.id.gameOverView);
            final ObjectAnimator alpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f);
            alpha.setInterpolator(new LinearInterpolator());
            alpha.setDuration(GAME_OVER_TIME_OUT);
            alpha.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    doStop();
                }
            });
            alpha.start();
        }
    }

    public void applyMissileBlast(Missile missile, int id) {

        if (launcher1 == null)
            launcher1 = findViewById(R.id.launcher1);
        if (launcher2 == null)
            launcher2 = findViewById(R.id.launcher2);
        if (launcher3 == null)
            launcher3 = findViewById(R.id.launcher3);

        Log.d(TAG, "applyMissileBlast: -------------------------- " + id);

        float x1 = missile.getX();
        float y1 = missile.getY();
        float x2 = 0;
        float y2 = 0;

        Log.d(TAG, "applyMissileBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Base> nowGone = new ArrayList<>();
        ArrayList<Base> temp = new ArrayList<>(activeBases);

        for (Base b: temp) {

            if (b.getID() == "Base1"){
                x2 = (float) ((launcher1.getX()) + (0.5 * launcher1.getWidth()));
                y2 = (float) ((launcher1.getY()) + (0.5 * launcher1.getHeight()));
            }

            if (b.getID() == "Base2"){
                x2 = (float) ((launcher2.getX()) + (0.5 * launcher2.getWidth()));
                y2 = (float) ((launcher2.getY()) + (0.5 * launcher2.getHeight()));
            }
            if (b.getID() == "Base3"){
                x2 = (float) ((launcher3.getX()) + (0.5 * launcher3.getWidth()));
                y2 = (float) ((launcher3.getY()) + (0.5 * launcher3.getHeight()));
            }

            Log.d(TAG, "applyMissileBlast:    Missile: " + x2 + ", " + y2);


            float f = (float) Math.sqrt((y2 - y1) * (y2-y1) + (x2-x1) * (x2 - x1));
            Log.d(TAG, "applyMissileBlast:    DIST: " + f);

            if (f < 250) {

                SoundPlayer.getInstance().start("base_blast");
                Log.d(TAG, "applyMissileBlast:    Hit: " + f);
                b.destruct(x2-150, y2-150);
                nowGone.add(b);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");


        }

        for (Base b : nowGone) {
            if (b.getID() == "Base1"){
                this.getLayout().removeView(launcher1);
            }
            if (b.getID() == "Base2"){
                this.getLayout().removeView(launcher2);
            }
            if (b.getID() == "Base3"){
                this.getLayout().removeView(launcher3);
            }
            activeBases.remove(b);
            Log.d(TAG, "activebases: " + activeBases.size());
            }

        if (activeBases.isEmpty()) {
            planeMaker.setRunning(false);
            CloudScroller.stop();
            binding.gameOverView.setVisibility(View.VISIBLE);
            ImageView imageView = findViewById(R.id.gameOverView);
            final ObjectAnimator alpha = ObjectAnimator.ofFloat(imageView, "alpha", 0.0f, 1.0f);
            alpha.setInterpolator(new LinearInterpolator());
            alpha.setDuration(GAME_OVER_TIME_OUT);
            alpha.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    doStop();
                }
            });
            alpha.start();
        }
    }
}