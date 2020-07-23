package com.hms.puzzleforkids;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class GameOverDialog extends AppCompatDialogFragment {

    private GameOverDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.game_over_dialog, null);

        builder.setView(view);
        setCancelable(false);

        Button playVideo = view.findViewById(R.id.playVideo);
        Button nope = view.findViewById(R.id.nope);



        playVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.applyRewardVideo(true);
                dismiss();
            }
        });


        nope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.applyRewardVideo(false);
                dismiss();
            }
        });


        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (GameOverDialogListener) context;
        } catch (ClassCastException e) {
            throw  new ClassCastException(context.toString() + "must dialog");
        }
    }

    public interface GameOverDialogListener {
        void applyRewardVideo(Boolean result);
    }
}
