package com.hms.puzzleforkids;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Arrays;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayoutMain, relativeLayoutBanner;
    Button soundButtonMain;
    MyHelperClass myHelperClass;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        myHelperClass = new MyHelperClass(this, R.raw.themem);
        layouts();

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
        relativeLayoutBanner.addView(mAdView);
    }

    private void layouts() {

        relativeLayoutMain = findViewById(R.id.relativeLayoutMain);
        relativeLayoutBanner = findViewById(R.id.relativeLayoutBanner);
        soundButtonMain = findViewById(R.id.soundButtonMain);

        myHelperClass.createRelativeLayouts(relativeLayoutMain,
                Arrays.asList(getResources().getStringArray(R.array.main_title)),
                getResources().getDimension(R.dimen.text_size),
                (int) getResources().getDimension(R.dimen.sixty_dp),
                (int) getResources().getDimension(R.dimen.sixty_dp),
                (int) getResources().getDimension(R.dimen.one_dp));

        soundButtonMain = findViewById(R.id.soundButtonMain);
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonMain.setBackground(getResources().getDrawable(R.drawable.sound_on));
        }
        else {
            soundButtonMain.setBackground(getResources().getDrawable(R.drawable.sound_off));
        }
    }

    public void playOnClick(View view) {
        startActivity(new Intent(this, GameActivity.class));
        finish();
    }

    public void feedbackOnClick(View view) {

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("mailto:"+getResources().getString(R.string.email)));
        intent.putExtra(Intent.EXTRA_SUBJECT, getPackageName());
        intent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(intent);
    }

    public void fiveStarOnClick(View view) {

        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        }
        // if there is no Google Play on device
        catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        myHelperClass.pauseMusic();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //If you want to change default parameters delete app and rerun
        if (sharedPreferences.getBoolean("first_run", true)) {
            // Do first run stuff here then set 'first_run' as false
            // using the following line to edit/commit prefs
            sharedPreferences.edit().putBoolean("first_run", false).apply();
            sharedPreferences.edit().putBoolean("music", true).apply();
            sharedPreferences.edit().putInt("best_score", 0).apply();
        }
        myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
    }

    public void soundClickMain(View view) {
        if (sharedPreferences.getBoolean("music", true)) {
            soundButtonMain.setBackground(getResources().getDrawable(R.drawable.sound_off));
            sharedPreferences.edit().putBoolean("music", false).apply();
            myHelperClass.pauseMusic();
        } else {
            soundButtonMain.setBackground(getResources().getDrawable(R.drawable.sound_on));
            sharedPreferences.edit().putBoolean("music", true).apply();
            myHelperClass.startMusic(sharedPreferences.getBoolean("music", true));
        }
    }
}
