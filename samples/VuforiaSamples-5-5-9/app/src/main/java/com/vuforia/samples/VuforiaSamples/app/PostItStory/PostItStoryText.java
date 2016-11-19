
package com.vuforia.samples.VuforiaSamples.app.PostItStory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.Updater;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;

/**
 * Created by alessiosperoni on 12/09/16.
 */
public class PostItStoryText extends Activity {

    private static final String baseDir = "postit-story";
    public static final int WORDREQUEST = 1;
    private static final int CORRECT = 1;
    private static final int WRONG = -1;
    private static final int NEUTRAL = 0;

    private TextView storyText;
    private TextView storyStep;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private FrameLayout correctImage;
    private FrameLayout wrongImage;
    private ImageButton cameraButton;
    private TextView mAboutTextTitle;
    private int storyPosition = 0;
    private int max = -1;
    private int nAnswers = 0;
    private String correctWord = "";
    private String[] answers;
    private int[] answerState;
    private File currentPath;
    private int startIndex, lastIndex;
    String text;

    String storyURL = "someUrl";
    String textString = "Android is a Software \u25FC\u25FC\u25FC\u25FC\u25FC";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story);

        Bundle extras = getIntent().getExtras();
        final int position = extras.getInt("story_id", -1);



        String webText;
        storyText = (TextView) findViewById(R.id.story_text);
        storyStep = (TextView) findViewById(R.id.stepText);

        nextButton = (ImageButton) findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyPosition++;
                setStoryText(storyPosition);
            }
        });

        prevButton = (ImageButton) findViewById(R.id.button_back);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyPosition--;
                setStoryText(storyPosition);
            }
        });
        cameraButton = (ImageButton) findViewById(R.id.button_cam);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(answerState[storyPosition] == 0){
                    launchCameraActivity();
                }
            }
        });


        correctImage = (FrameLayout) findViewById(R.id.correct_feedback);
        wrongImage = (FrameLayout) findViewById(R.id.wrong_feedback);

        initStory(position);

    }


    private void initStory(int position){
        File sdcard = Environment.getExternalStorageDirectory();
        File storyDirectory = new File(sdcard, baseDir);
        String[] directories = storyDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return new File(file, s).isDirectory();
            }
        });


        if(directories.length < position){
            return;
        }

        String directory = directories[position];
        currentPath = new File(storyDirectory.getAbsolutePath() + File.separator + directory);
        Updater.getInstance().loadStoryResources(currentPath.getAbsolutePath());
        max = Updater.getInstance().getSteps();
        answerState = new int[max];
        answers = new String[max];
        for(int i = 0; i < answerState.length; i++){
            answerState[i] = NEUTRAL;
            if(Updater.getInstance().getStoryPhrase(i + 1).contains("#")){
                nAnswers++;
            }
        }

        setStoryText(1);
    }

    private int correctAnswerCount(){
        int c = 0;
        for(int i = 0; i< answerState.length; i++){
            c += answerState[i] == CORRECT ? 1:0;
        }
        return c;
    }

    private int wrongAnswerCount(){
        int c = 0;
        for(int i = 0; i< answerState.length; i++){
            c += answerState[i] == WRONG ? 1:0;
        }

        return c;
    }

    private boolean answerCompleted(){
        return correctAnswerCount() + wrongAnswerCount() == nAnswers;
    }

    private void showSummary(){
        StringBuffer buffer = new StringBuffer();
        int[] bold = new int[4];
        bold[0] = 0;
        buffer.append("CORRECT ANSWERS\n");
        int correctCount = correctAnswerCount();
        int wrongCount = wrongAnswerCount();
        buffer.append(correctCount).append("/").append(nAnswers).append("\n");
        bold[1] = buffer.length();

        for(int i = 0; i < answerState.length; i++){
            if(answerState[i] == CORRECT){
                buffer.append(answers[i]).append(",");
            }
        }
        if(correctCount > 0){
            buffer.delete(buffer.length() -1, buffer.length());
        }
        buffer.append("\n\n");

        bold[2] = buffer.length();
        buffer.append("WRONG ANSWERS\n");
        buffer.append(correctCount).append("/").append(nAnswers).append("\n");
        bold[3] = buffer.length();

        for(int i = 0; i < answerState.length; i++){
            if(answerState[i] == WRONG){
                buffer.append(answers[i]).append(",");
            }
        }
        if(wrongCount > 0){
            buffer.delete(buffer.length() -1, buffer.length());
        }

        SpannableString spannableString = new SpannableString(buffer.toString());


        spannableString.setSpan(new StyleSpan(Typeface.BOLD),bold[0],bold[1],0);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),bold[2],bold[3],0);
        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                0, buffer.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        storyText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        storyText.setText(spannableString);
        storyText.setMovementMethod(LinkMovementMethod.getInstance());
        storyText.setHighlightColor(Color.TRANSPARENT);
        LinearLayout bottom = (LinearLayout) findViewById(R.id.layBut);
        bottom.setVisibility(View.INVISIBLE);
        storyStep.setVisibility(View.INVISIBLE);

    }

    private void setStoryText(int position){

        if(answerCompleted()){
            showSummary();
        }

        if(position >= 1 && position <= max){
            storyPosition = position;
        }

        if(position == 1){
            prevButton.setVisibility(View.INVISIBLE);
        }else{
            prevButton.setVisibility(View.VISIBLE);
        }

        if(position == max){
            nextButton.setVisibility(View.INVISIBLE);
        }else{
            nextButton.setVisibility(View.VISIBLE);
        }


        String phraseText = Updater.getInstance().getStoryPhrase(position);
        String[] split = phraseText.split("#");

        for(int i = 0; i < split.length; i++){
            split[i] = split[i].replaceAll("(\r|\n|\t)", "");
        }

        if(split.length == 3){
            int dictionaryReference = Integer.parseInt(split[1]);
            correctWord = Updater.getInstance().getWord(dictionaryReference);
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i< correctWord.length(); i++){
                buffer.append('◼');
            }
            split[1] = buffer.toString();

            startIndex = split[0].length();
            lastIndex = startIndex +  correctWord.length();
            MessageFormat path = new MessageFormat("{0}{1}{2}");
            text = path.format(split);
            showHiddenWord(text, startIndex, lastIndex);
            storyStep.setVisibility(View.VISIBLE);
            storyStep.setText(storyPosition + "/" + nAnswers);
        }else{
            showTextWithoutQuestion(split[0]);
            storyStep.setVisibility(View.INVISIBLE);
        }



    }


    private void showHiddenWord(String textString, int startIndex, int lastIndex) {
        cameraButton.setVisibility(View.VISIBLE);
        SpannableString ss = new SpannableString(textString);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                launchCameraActivity();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        ss.setSpan(clickableSpan, startIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new StyleSpan(Typeface.BOLD),startIndex,lastIndex,0);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), startIndex, lastIndex, 0);

        storyText.setText(ss);
        storyText.setMovementMethod(LinkMovementMethod.getInstance());
        storyText.setHighlightColor(Color.TRANSPARENT);
    }

    private void showTextWithoutQuestion(String t){
        storyText.setText(t);
        cameraButton.setVisibility(View.INVISIBLE);
    }


    private void launchCameraActivity(){

        Intent postit = new Intent(this, PostItStory.class);
        postit.putExtra("story", currentPath.getAbsolutePath());
        startActivityForResult(postit, WORDREQUEST);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == WORDREQUEST){
            if(resultCode == RESULT_OK){
                int dictionaryReference = data.getIntExtra("answer", -1);
                if(dictionaryReference != -1){
                    String selectedWord = Updater.getInstance().getWord(dictionaryReference);
                    if(selectedWord.equals(correctWord)){
                        answerState[storyPosition] = 1;
                        updateTextOk(text, correctWord, startIndex, lastIndex);
                    }else{
                        answerState[storyPosition] = -1;
                        updateTextWrong(text, selectedWord, correctWord, startIndex, lastIndex);
                    }
                }
            }
        }
    }

    private void updateTextOk(String text, String correct, int startIndex, int lastIndex){
        String text1 = text.substring(0, startIndex);
        String text2 = text.substring(lastIndex);

        lastIndex = startIndex + correct.length();
        String finalText = text1 + correct + text2;
        SpannableString ss = new SpannableString(finalText);

        ss.setSpan(new StyleSpan(Typeface.BOLD),startIndex,lastIndex,0);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), startIndex, lastIndex, 0);

        storyText.setText(ss);

        animateResult(correctImage);
        cameraButton.setImageResource(R.drawable.correct);
        cameraButton.setVisibility(View.INVISIBLE);

    }

    private void updateTextWrong(
            String text,
            String wrong,
            String correct,
            int startIndex,
            int lastIndex){
        String text1 = text.substring(0, startIndex);
        String text2 = text.substring(lastIndex);

        lastIndex = startIndex + wrong.length() + correct.length() +1;
        int midIndex = startIndex + wrong.length() +1;
        String finalText = text1 + wrong + " " + correct + text2;
        SpannableString ss = new SpannableString(finalText);

        ss.setSpan(new StyleSpan(Typeface.BOLD),startIndex,lastIndex,0);
        ss.setSpan(new StrikethroughSpan(),startIndex,midIndex,0);
        ss.setSpan(new ForegroundColorSpan(Color.BLACK), startIndex, lastIndex, 0);

        storyText.setText(ss);
        animateResult(wrongImage);
        cameraButton.setImageResource(R.drawable.wrong);
        cameraButton.setVisibility(View.INVISIBLE);
    }


    private void initAnimation(final FrameLayout btn){

        final ScaleAnimation growAnim = new ScaleAnimation(
                1.0f, 2.0f, 1.0f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(
                2.0f, 1.0f, 2.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(1000);
        shrinkAnim.setDuration(1000);

        btn.setAnimation(growAnim);


        growAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                btn.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation){}

            @Override
            public void onAnimationRepeat(Animation animation){}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                btn.setAnimation(growAnim);
                cameraButton.setVisibility(View.VISIBLE);
                btn.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void animateResult(final FrameLayout btn){

        correctImage.setVisibility(View.INVISIBLE);
        wrongImage.setVisibility(View.INVISIBLE);

        initAnimation(btn);
        btn.setVisibility(View.VISIBLE);
        btn.getAnimation().start();



    }

}
