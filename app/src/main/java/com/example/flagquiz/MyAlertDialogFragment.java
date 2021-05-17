package com.example.flagquiz;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class MyAlertDialogFragment extends DialogFragment {

    private static int totalguesses;


    private MainActivityFragment mf = new MainActivityFragment();


    public MyAlertDialogFragment(int totalGuesses) {
        this.totalguesses = totalGuesses;
    }

    public static MyAlertDialogFragment newInstance (int title) {
        MyAlertDialogFragment frag = new MyAlertDialogFragment(totalguesses);
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())

                .setMessage(getString(R.string.result, totalguesses, (1000 / (double) totalguesses)))

                .setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {


                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mf.doPositiveClick();

                            }
                        }
                ).setNegativeButton("cancel", new DialogInterface.OnClickListener() {


                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mf.doNegativeClick();

                    }
                }
        ).create();

    }



}


