package com.hms.puzzleforkids;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class GameActivity extends AppCompatActivity implements RewardedVideoAdListener, GameOverDialog.GameOverDialogListener {

    Button soundButtonGame;
    SharedPreferences sharedPreferences;
    MyHelperClass myHelperClass;
    TextView clock;
    TypedArray question, answer;
    int[] questionList, answerList;
    int questionCounter, currentQuestion, remainingTime, counter, score;
    Boolean dragEnd, touchable;
    List<List<Object>> listOfLists;
    RelativeLayout relativeLayoutMidGame, relativeLayoutBotGame, banner;
    CountDownTimer countDownTimer;

    MediaPlayer correctAnswerSound, wrongAnswerSound, yey;

    RewardedVideoAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        myHelperClass = new MyHelperClass(this, R.raw.game);
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        remainingTime = getResources().getInteger(R.integer.remainingTime);
        questionCounter = 0;
        currentQuestion = 0;
        counter = 0;
        score = 10;
        touchable = true;

        layouts();

        correctAnswerSound = MediaPlayer.create(this, R.raw.correct);
        wrongAnswerSound = MediaPlayer.create(this, R.raw.wrong);
        yey = MediaPlayer.create(this, R.raw.yey);
        arrays();
        questionGenerator();

        RequestConfiguration conf= new RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE).build();

        MobileAds.setRequestConfiguration(conf);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        final AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(getResources().getString(R.string.BANNER_AD_ID));

        mAdView.loadAd(new AdRequest.Builder().build());
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mAdView.setLayoutParams(params);
        banner.addView(mAdView);
        loadRewardedVideoAd();
    }

    @Override
    public void applyRewardVideo(Boolean result) {

        if(result) {
            showRewardedVideoAd();
        }
        else  {
            Intent intent = new Intent(this, GameOverActivity.class);
            intent.putExtra("score", score * 10);
            if (sharedPreferences.getInt("best_score", 0) < score * 10) {
                sharedPreferences.edit().putInt("best_score", score * 10).apply();
            }
            startActivity(intent);
            finish();
        }
    }

    private void loadRewardedVideoAd() {
        RequestConfiguration conf= new RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE).build();

        MobileAds.setRequestConfiguration(conf);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mRewardedVideoAd.loadAd(getResources().getString(R.string.REWARD_AD_ID),
                new AdRequest.Builder().build());
    }

    private void showRewardedVideoAd() {
        if(isRewardedVideoAdLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    private Boolean isRewardedVideoAdLoaded() {
        return mRewardedVideoAd.isLoaded();
    }


    View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (touchable) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(data, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                touchable = false;

            }
            return true;
        }

    };

    View.OnDragListener onDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            View view = (View) event.getLocalState();
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    //True answer
                    if(((TextView) v).getText().equals(((TextView) view).getText())) {
                        v.setBackground(view.getBackground());
                        dragEnd = false;
                        touchable = true;
                        counter++;

                        YoYo.with(Techniques.FlipInX)
                                .duration(1000)
                                .repeat(1)
                                .playOn(v);

                        sound(true);
                        if (counter == 4) {
                            score += 2;
                            if (sharedPreferences.getBoolean("music", true)) {
                                yey = MediaPlayer.create(getApplicationContext(), R.raw.yey);
                                yey.setVolume(100, 100);
                                yey.start();
                            }
                            Handler handler = new Handler(Looper.getMainLooper());
                            countDownTimer.cancel();
                            YoYo.with(Techniques.Tada)
                                    .duration(1000)
                                    .repeat(1)
                                    .playOn(relativeLayoutMidGame.getChildAt(0));
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    relativeLayoutMidGame.removeView(relativeLayoutMidGame.getChildAt(0));
                                    relativeLayoutBotGame.removeView(relativeLayoutBotGame.getChildAt(0));
                                    questionGenerator();
                                    counter = 0;
                                    startCountDownTimer(remainingTime);
                                }
                            }, 2000);
                        }
                    }
                    //Wrong answer
                    else {
                        sound(false);
                        score--;
                        YoYo.with(Techniques.Shake)
                                .duration(800)
                                .repeat(0)
                                .playOn(view);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (dragEnd) {
                        view.setVisibility(View.VISIBLE);
                        touchable = true;
                    }
                    return true;
                case DragEvent.ACTION_DRAG_STARTED:
                    dragEnd = true;
                    return true;
            }
            return true;
        }
    };

    private void questionGenerator() {
        if (currentQuestion + 4 > listOfLists.size()) {
            currentQuestion = 0;
            Collections.shuffle(listOfLists);
        }
        List<List<Object>> questions = new ArrayList<>();
        List<List<Object>> answers = new ArrayList<>();
        for (int i = currentQuestion; i < currentQuestion + 4; i++) {
            List<Object> objects = new ArrayList<>();
            objects.add(listOfLists.get(i).get(0));
            objects.add(listOfLists.get(i).get(2));
            questions.add(objects);
            List<Object> objects1 = new ArrayList<>();
            objects1.add(listOfLists.get(i).get(1));
            objects1.add(listOfLists.get(i).get(2));
            answers.add(objects1);
        }

        Collections.shuffle(answers);
        createGridLayouts(relativeLayoutMidGame, questions, true);
        createGridLayouts(relativeLayoutBotGame, answers, false);
        currentQuestion += 4;



    }

    private void createGridLayouts(RelativeLayout relativeLayout, List<List<Object>> list, Boolean result) {
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount(1);

        for (int i = 0; i < 4; i++) {
            final RelativeLayout rl = new RelativeLayout(this);
            final TextView textView = new TextView(this);
            textView.setBackground(getResources().getDrawable(R.drawable.circle_white));
            final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                    ((int) getResources().getDimension(R.dimen.eighty_dp),
                            (int) getResources().getDimension(R.dimen.eighty_dp));
            layoutParams.setMargins(25, 0, 25, 0);
            rl.addView(textView, layoutParams);

            createTextViews(rl, (int) list.get(i).get(0), (int) list.get(i).get(1), result);

            final RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            gridLayout.addView(rl, layoutParams1);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(gridLayout, params);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createTextViews(RelativeLayout relativeLayout, int imageDrawable, int imageId, Boolean result) {
        final TextView textView = new TextView(this);
        textView.setText(String.valueOf(imageId));
        textView.setTextColor(getResources().getColor(R.color.colorTransparent));
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                ((int) getResources().getDimension(R.dimen.sixty_dp),
                        (int) getResources().getDimension(R.dimen.sixty_dp));
        textView.setBackground(getResources().getDrawable(imageDrawable));

        if (result) {
            textView.setOnDragListener(onDragListener);
        }
        else {
            textView.setOnTouchListener(onTouchListener);
        }

        layoutParams.setMargins(25, 0, 25, 0);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        relativeLayout.addView(textView, layoutParams);
    }

    private void arrays() {
        question = getResources().obtainTypedArray(R.array.question);
        answer = getResources().obtainTypedArray(R.array.answer);
        questionList = new int[question.length()];
        answerList = new int[answer.length()];
        createList(questionList, question);
        createList(answerList, answer);
        listOfLists = new ArrayList<>();

        for (int i = 0; i < question.length(); i++) {
            List<Object> objects = new ArrayList<>();
            objects.add(questionList[i]);
            objects.add(answerList[i]);
            objects.add(i);
            listOfLists.add(objects);
        }

        question.recycle();
        answer.recycle();
    }

    private void createList(int[] list, TypedArray typedArray) {
        for (int i = 0; i < typedArray.length(); i++) {
            list[i] = typedArray.getResourceId(i, 0);
        }
    }

    private void layouts() {
        soundButtonGame = findViewById(R.id.soundButtonGame);
        clock = findViewById(R.id.clock);
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonGame.setBackground(getResources().getDrawable(R.drawable.sound_on));
        }
        else {
            soundButtonGame.setBackground(getResources().getDrawable(R.drawable.sound_off));
        }

        relativeLayoutMidGame = findViewById(R.id.relativeLayoutMidGame);
        relativeLayoutBotGame = findViewById(R.id.relativeLayoutBotGame);
        banner = findViewById(R.id.banner);
    }

    public void soundClickGame(View view) {
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonGame.setBackground(getResources().getDrawable(R.drawable.sound_off));
            sharedPreferences.edit().putBoolean("music", false).apply();
            myHelperClass.pauseMusic();
        } else {
            soundButtonGame.setBackground(getResources().getDrawable(R.drawable.sound_on));
            sharedPreferences.edit().putBoolean("music", true).apply();
            myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
        }
    }

    public void startCountDownTimer(int time_) {

        countDownTimer = new CountDownTimer(time_ * 1000 + 100, 1000){

            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                clock.setText(millisUntilFinished / 1000 + "s");
                remainingTime = (int) (millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                openDialog();
            }
        }.start();
    }

    private void openDialog() {
        if (isRewardedVideoAdLoaded()) {
            GameOverDialog gameOverDialog = new GameOverDialog();
            gameOverDialog.show(getSupportFragmentManager(), null);
        }
        else {
            Intent intent = new Intent(this, GameOverActivity.class);
            //TODO Feature
            intent.putExtra("score", score * 10);
            if (sharedPreferences.getInt("best_score", 0) < score * 10) {
                sharedPreferences.edit().putInt("best_score", score * 10).apply();
            }
            startActivity(intent);
            finish();
        }
    }

    private void sound(Boolean result) {

        if (sharedPreferences.getBoolean("music", true)) {
            if (result) {
                if (correctAnswerSound.isPlaying() || correctAnswerSound != null) {
                    correctAnswerSound.stop();
                    correctAnswerSound.release();
                }
                correctAnswerSound = MediaPlayer.create(this, R.raw.correct);
                correctAnswerSound.setVolume(100, 100);
                correctAnswerSound.start();
            }
            else {
                if (wrongAnswerSound.isPlaying() || wrongAnswerSound != null) {
                    wrongAnswerSound.stop();
                    wrongAnswerSound.release();
                }
                wrongAnswerSound = MediaPlayer.create(this, R.raw.wrong);
                wrongAnswerSound.setVolume(100, 100);
                wrongAnswerSound.start();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myHelperClass.pauseMusic();
        countDownTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
        if (remainingTime != 0) {
            startCountDownTimer(remainingTime);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        countDownTimer.cancel();
        finish();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (remainingTime == 0) {
            Intent intent = new Intent(this, GameOverActivity.class);
            intent.putExtra("score", score * 10);
            if (sharedPreferences.getInt("best_score", 0) < score * 10) {
                sharedPreferences.edit().putInt("best_score",score * 10).apply();
            }
            startActivity(intent);
            finish();
        }
        else {
            loadRewardedVideoAd();
            countDownTimer.cancel();
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        remainingTime = 30;
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
