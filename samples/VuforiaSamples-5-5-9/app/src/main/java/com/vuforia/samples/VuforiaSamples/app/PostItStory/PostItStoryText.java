
package com.vuforia.samples.VuforiaSamples.app.PostItStory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by alessiosperoni on 12/09/16.
 */
public class PostItStoryText extends Activity {

    private static final String baseDir = "postit-story";
    public static final int WORDREQUEST = 1;
    private static final int CORRECT = 1;
    private static final int WRONG = -1;
    private static final int NEUTRAL = 0;

    private TextView storyStep;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private FrameLayout correctImage;
    private FrameLayout wrongImage;
    private ImageButton cameraButton;
    private TextView mAboutTextTitle;
    private Node storyNode;
    private int storyPosition = 0;
    private int max = -1;
    private String correctWord = "";
    int[] correctAnswers;
    XPath xPath;

    String storyURL = "someUrl";
    String textString = "Android is a Software \u25FC\u25FC\u25FC\u25FC\u25FC";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story);

        Bundle extras = getIntent().getExtras();
        final int position = extras.getInt("story_id", -1);



        String webText;
        storyStep = (TextView) findViewById(R.id.story_text);





        nextButton = (ImageButton) findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyPosition++;
                setStoryStep(storyPosition);
            }
        });

        prevButton = (ImageButton) findViewById(R.id.button_back);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storyPosition--;
                setStoryStep(storyPosition);
            }
        });
        cameraButton = (ImageButton) findViewById(R.id.button_cam);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCameraActivity();
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
        XPathFactory factory = XPathFactory.newInstance();
        xPath = factory.newXPath();

        if(directories.length < position){
            return;
        }

        String directory = directories[position];
        try {
            FileReader storyXmlFile = new FileReader(new File(storyDirectory,
                    directory + File.separator + "info.xml"));
            InputSource storyXml = new InputSource(storyXmlFile);
            storyNode = (Node) xPath.evaluate("/",
                    storyXml, XPathConstants.NODE);
            NodeList items = (NodeList) xPath.evaluate("/story/item",
                    storyNode, XPathConstants.NODESET);
            max = items.getLength();
            correctAnswers = new int[max];
            for(int i = 0; i < correctAnswers.length; i++){
                correctAnswers[i] = NEUTRAL;
            }
            setStoryStep(1);
        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStoryStep(int position){

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

        try {
            MessageFormat path = new MessageFormat("/story/item[@id={0}]");
            String pathString = path.format(new Object[]{position});
            Node phraseNode = (Node) xPath.evaluate(pathString, storyNode, XPathConstants.NODE);

            String phraseText = phraseNode.getTextContent();
            String[] split = phraseText.split("#");

            for(int i = 0; i < split.length; i++){
                split[i] = split[i].replaceAll("(\r|\n|\t)", "");
            }

            if(split.length == 3){
                int dictionaryReference = Integer.parseInt(split[1]);
                path = new MessageFormat("/story/dictionary/item[@id={0}]/secondLanguageWord/syntax");
                pathString = path.format(new Object[]{dictionaryReference});
                Node hiddenNode = (Node) xPath.evaluate(pathString, storyNode, XPathConstants.NODE);
                correctWord = hiddenNode.getTextContent();
                StringBuffer buffer = new StringBuffer();
                for(int i = 0; i< correctWord.length(); i++){
                    buffer.append('◼');
                }
                split[1] = buffer.toString();

                int startIndex = split[0].length();
                int lastIndex = startIndex +  correctWord.length();
                path = new MessageFormat("{0}{1}{2}");
                showHiddenWord(path.format(split), startIndex, lastIndex);
            }else{
                showTextWithoutQuestion(split[0]);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
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

        storyStep.setText(ss);
        storyStep.setMovementMethod(LinkMovementMethod.getInstance());
        storyStep.setHighlightColor(Color.TRANSPARENT);
    }

    private void showTextWithoutQuestion(String t){
        storyStep.setText(t);
        cameraButton.setVisibility(View.INVISIBLE);
    }


    private void launchCameraActivity(){

        Intent postit = new Intent(this, PostItStory.class);
        startActivityForResult(postit, WORDREQUEST);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == WORDREQUEST){
            if(resultCode == RESULT_OK){

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

        storyStep.setText(ss);

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

        storyStep.setText(ss);
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
