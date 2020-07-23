package com.hms.puzzleforkids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Arrays;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class GameOverActivity extends AppCompatActivity {

    MyHelperClass myHelperClass;
    SharedPreferences sharedPreferences;
    RelativeLayout relativeLayoutMidGameOver;
    Button soundButtonGameOver;
    TextView yourScore, yourBestScore;

    int score, bestScore;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        bestScore = sharedPreferences.getInt("best_score", 0);

        layouts();
        loadInterstitialAd();
    }

    private void layouts() {

        relativeLayoutMidGameOver = findViewById(R.id.relativeLayoutMidGameOver);
        myHelperClass = new MyHelperClass(this, R.raw.gameover);
        myHelperClass.createRelativeLayouts(relativeLayoutMidGameOver,
                Arrays.asList(getResources().getStringArray(R.array.game_over_title)),
                getResources().getDimension(R.dimen.text_size),
                (int) getResources().getDimension(R.dimen.sixty_dp),
                (int) getResources().getDimension(R.dimen.sixty_dp),
                (int) getResources().getDimension(R.dimen.one_dp));
        soundButtonGameOver = findViewById(R.id.soundButtonGameOver);
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonGameOver.setBackground(getResources().getDrawable(R.drawable.sound_on));
        }
        else {
            soundButtonGameOver.setBackground(getResources().getDrawable(R.drawable.sound_off));
        }

        yourScore = findViewById(R.id.your_score);
        yourBestScore = findViewById(R.id.your_best_score);

        yourScore.setText(String.format("Score: %s", Integer.toString(score)));
        yourBestScore.setText(String.format("Best score: %s", Integer.toString(bestScore)));
    }

    private void loadInterstitialAd() {

        RequestConfiguration conf= new RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE).build();

        MobileAds.setRequestConfiguration(conf);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.INTERSTITIAL_AD_ID));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(GameOverActivity.this, GameActivity.class));
                finish();
            }
        });
    }


    public void playOnClick(View view) {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        else {
            startActivity(new Intent(this, GameActivity.class));
            finish();
        }
    }

    public void shareOnClick(View view) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, "I just " + score +
                " scored in Puzzle For Kids. Can you beat me?\n\n" +
                "https://play.google.com/store/apps/details?id=" + getPackageName());

        try {
            startActivity(Intent.createChooser(intent, ""));

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(GameOverActivity.this,
                    "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myHelperClass.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void soundClickGameOver(View view) {
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonGameOver.setBackground(getResources().getDrawable(R.drawable.sound_off));
            sharedPreferences.edit().putBoolean("music", false).apply();
            myHelperClass.pauseMusic();
        } else {
            soundButtonGameOver.setBackground(getResources().getDrawable(R.drawable.sound_on));
            sharedPreferences.edit().putBoolean("music", true).apply();
            myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
        }
    }
}
