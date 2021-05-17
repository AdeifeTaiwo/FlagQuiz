package com.example.flagquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;

import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;


import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class MainActivityFragment extends DialogFragment {
    private List<String> quizCountriesList;
    private static final String TAG = "FlagQuiz Activity";
    private static final int FLAG_IN_QUIZ = 10;
    private List <String> fileNameList;
    private Set<String> regionsSet;
    public int totalGuesses;
    private String correctAnswer;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private int correctAnswers;
    private Animation shakeAnimation;
    private LinearLayout quizLinearLayout;
    private TextView answerTextView;
    private ImageView flagImageView;
    private LinearLayout [] guessLinearLayouts;
    private TextView questionNumberTextView;
    public AssetManager assets;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       View view =  inflater.inflate(R.layout.fragment_main_activity, container, false);

        super.onCreateView(inflater,container,savedInstanceState);
        fileNameList = new ArrayList<>();// diamond operators
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();
        assets = getActivity().getAssets();



        // load the shake animation that's used for incorrect answers
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.anim);
        shakeAnimation.setRepeatCount(3);

        // get references to GUI components
        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView)view.findViewById(R.id.imageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);

        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // configor listeners for the guess buttons

        for(LinearLayout row : guessLinearLayouts){
            for(int column = 0; column < row.getChildCount(); column++){
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
        questionNumberTextView.setText( getString(R.string.question, 1, FLAG_IN_QUIZ));



        return view;


    }

    public void updateGuessRows(SharedPreferences sharedPreferences){
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices)/2;
        for(LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        for(int row = 0; row< guessRows; row++ ){
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
        }

    }

    public int getTotalGuesses(){
        return totalGuesses;
    }

    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resetQuiz(){
        AssetManager assets = getActivity().getAssets();

        fileNameList.clear();

        try{
            for(String region : regionsSet){
                String [] paths = assets.list(region);
                for(String path : paths)
                    fileNameList.add(path.replace(".png",""));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading image file names", e);
        }
        correctAnswers = 0;
        totalGuesses = 0;
        quizCountriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        // add FLAG_IN_QUIZ random file names to the quizCountriesList
        while(flagCounter <= FLAG_IN_QUIZ){
            int randomIndex = random.nextInt(numberOfFlags);

            // get the random file name
            String filename = fileNameList.get(randomIndex);
            if(!quizCountriesList.contains(filename)){
                quizCountriesList.add(filename);// add file to country list
                ++flagCounter;
            }
        }
        loadNextFlag();

    }






    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadNextFlag()  {
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // name of asset are Nigeria, Togo
        answerTextView.setText("");

        // display current question number
        questionNumberTextView.setText(getString(R.string.question, (correctAnswers+1), FLAG_IN_QUIZ));

        // extract the region from the nextw image's name

        String region = nextImage.substring(0,nextImage.indexOf('-'));

        // Use assetManager to load next image from assets folder
        assets = getActivity().getAssets();

        // get an InputStream to the asset represent the next flag
        // and try to use the Input Stream

        try(InputStream stream = assets.open(region + "/" + nextImage + ".png")){

            // load the asset as a drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
            animate(false);

        } catch (IOException e) {
            Log.e(TAG, "Error loading image file names", e);
        }
        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add 2, 4, 6, 8 guess Buttons based on the value of guessRows
        for(int row = 0; row < guessRows; row++){
            for(int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButtons's
                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(fileName));


            }
        }
        // randomly replace Button with the correct answer
        int row = random.nextInt(guessRows);
        int column = random.nextInt(2); // pick random column
        LinearLayout randomRow = guessLinearLayouts[row];


        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }


    // parses the country flag file name and returns the country name
    private String getCountryName(String name){
        return name.substring(name.indexOf('-')+ 1).replace('_', ' ');

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void animate(boolean animateOut){
        if(correctAnswers == 0)
            return;

        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight())/2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom())/2;

        // calculate animation radius

        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());

        Animator animator;

        if(animateOut){

            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,radius,0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                    loadNextFlag();

                }
            });

        }
        else{
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,0,radius);


        }
        animator.setDuration(500);
        animator.start();
    }


    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            Button guessButton = ((Button) view);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses;

            if(guess.equals(answer)){
                ++correctAnswers;// increment the number of the correct answer
                // display correct answer in green text

                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));

                disableButtons();

                if(correctAnswers == 2){
                    //DialogFragment quizResults = MyAlertDialogFragment.newInstance(2);
                    MyAlertDialogFragment quizResults = new MyAlertDialogFragment(getTotalGuesses());



                    /** DialogFragment quizResults = new DialogFragment(){


                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage(getString(R.string.result, totalGuesses, (1000/(double) totalGuesses)));
                            //builder.setMessage("awesome");
                            // Reset quiz
                            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    resetQuiz();
                                }
                            }
                            );
                            return builder.create();
                        }
                   }; **/
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), TAG);



                }
                else { // answer is correct but quiz is not over load the next flag after 2 seconds
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);

                        }
                    }, 500); // 2000 milliseconds for 2 second
                }

            }
            else{
                flagImageView.startAnimation(shakeAnimation);

                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false);

            }

        }

    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void doPositiveClick(){
        resetQuiz();


    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void doNegativeClick(){
        Log.i("FramentAlertDialog", "Negative Click");

    }

    private void disableButtons(){
        for(int row = 0; row < guessRows; row++){
            LinearLayout guessRow = guessLinearLayouts[row];
            for(int i=0; i< guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }
    }



}