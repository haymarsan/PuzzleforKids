package com.hms.puzzleforkids;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyHelperClass {

    private Context context;
    private TypedArray typedArray;
    private int[] images;
    private Boolean musicChoice;
    private int mediaFile;
    private MediaPlayer mediaPlayer;


    MyHelperClass(Context context, int mediaFile) {
        this.context = context;
        this.typedArray = context.getResources().obtainTypedArray(R.array.letter_background);
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE);
        this.musicChoice = sharedPreferences.getBoolean("music", true);
        this.mediaFile = mediaFile;

        setBackGround();

        createMusic();
    }

    private void createMusic() {
        mediaPlayer = MediaPlayer.create(context, mediaFile);
        mediaPlayer.setVolume(context.getResources().getInteger(R.integer.volume_50),
                context.getResources().getInteger(R.integer.volume_50));
        mediaPlayer.setLooping(true);

        startMusic(musicChoice);
    }

    public void startMusic(Boolean musicChoice) {
        if (musicChoice) {
            if (mediaPlayer != null) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        }
    }

    public void pauseMusic () {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    private void setBackGround() {
        images = new int[typedArray.length()];

        for(int i = 0; i < typedArray.length(); i++) {
            images[i] = typedArray.getResourceId(i, 0);
        }
        typedArray.recycle();
    }

    public void createRelativeLayouts(RelativeLayout relativeLayout, List<String> strings, float text_size, int width, int height, int margin) {
        int previousRelativeLayout = 0;

        RelativeLayout[] relativeLayouts = new RelativeLayout[strings.size()];

        for (int i = 0; i < strings.size(); i++) {
            relativeLayouts[i] = new RelativeLayout(context);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            int currentRelativeLayout = previousRelativeLayout + 1;
            relativeLayouts[i].setId(currentRelativeLayout);
            layoutParams.addRule(RelativeLayout.BELOW, previousRelativeLayout);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            relativeLayouts[i].setLayoutParams(layoutParams);
            previousRelativeLayout = currentRelativeLayout;
            List<Character> ch = convertStringToCharList(strings.get(i));
            createTextView(relativeLayouts[i] ,ch, text_size, width, height, margin);
            relativeLayout.addView(relativeLayouts[i], layoutParams);
        }
    }

    private void createTextView(RelativeLayout relativeLayout ,List<Character> ch, float text_size, int width, int height, int margin) {
        int previousTextView = 0;
        for(int i = 0; i < ch.size(); i++) {
            final TextView textView = new TextView(context);
            textView.setText(String.valueOf(ch.get(i)));
            textView.setBackground(ContextCompat.getDrawable(context, images[new Random().nextInt(images.length)]));
            textView.setTextSize(text_size);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(0xffffffff);

            int currentTextView = previousTextView + 1;
            textView.setId(currentTextView);
            final RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(width, height);
            layoutParams.setMargins(margin,
                    margin,
                    0, 0);
            layoutParams.addRule(RelativeLayout.END_OF, previousTextView);
            textView.setLayoutParams(layoutParams);

            previousTextView = currentTextView;
            YoYo.with(Techniques.Pulse)
                    .duration(800)
                    .repeat(-1)
                    .playOn(textView);
            relativeLayout.addView(textView, layoutParams);
        }

    }

    public List<Character> convertStringToCharList(String str) {

        // Create an empty List of character
        List<Character> chars = new ArrayList<>();

        // For each character in the String
        // add it to the List
        for (char ch : str.toCharArray()) {

            chars.add(ch);
        }

        // return the List
        return chars;
    }
}
