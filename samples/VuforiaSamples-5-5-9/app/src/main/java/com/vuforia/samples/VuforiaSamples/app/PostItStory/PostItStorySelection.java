/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.app.PostItStory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.Help;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.AboutScreen;


// This activity starts activities which demonstrate the Vuforia features
public class PostItStorySelection extends Activity
{
    
    private String mActivities[] = {"Post-It Notes","Post-It Story","Help"};

    TextView savedText, searchText;
    ImageView savedImage, searchImage;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        
        setContentView(R.layout.story_selection);
        //setListAdapter(adapter);

        savedText = (TextView) findViewById(R.id.savedStoriesText);
        searchText = (TextView) findViewById(R.id.searchonlineText);
        savedImage = (ImageView) findViewById(R.id.savedStoriesImages);
        searchImage = (ImageView) findViewById(R.id.searchonlineImage);

        View.OnClickListener storytellingClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeActivity(1);
            }
        };

        View.OnClickListener exploringClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeActivity(0);
            }
        };

        savedText.setOnClickListener(storytellingClick);
        savedImage.setOnClickListener(storytellingClick);
        searchText.setOnClickListener(exploringClick);
        searchImage.setOnClickListener(exploringClick);

    }
    
    

    public void changeActivity(int position)
    {
        Intent intent = null;

        switch (position)
        {
            case 0:
                intent = new Intent(this, PostItStoryList.class);
                //intent.putExtra("ACTIVITY_TO_LAUNCH", "app.PostItStory.PostItStoryList");
                //intent.putExtra("ABOUT_TEXT", "PostItNote/PIN_about.html");
                break;
            case 1:
                intent = new Intent(this, PostItStoryList.class);
                //intent.putExtra("ACTIVITY_TO_LAUNCH","app.PostItStory.PostItStoryList");
                //intent.putExtra("ABOUT_TEXT", "PostItStory/PIS_about.html");
                break;
            case 2:
                intent =  new Intent(this, Help.class);
                //intent.setClassName("com.vuforia.samples.VuforiaSamples", "com.vuforia.samples.VuforiaSamples.app.PostItStory.Help");
                //startActivity(intent);
                //intent.putExtra("ACTIVITY_TO_LAUNCH", "app.PostItNote.PostItNote");
                //intent.putExtra("ABOUT_TEXT", "PostItNote/PIN_about.html");

                //intent.putExtra("ACTIVITY_TO_LAUNCH", "app.PostItNote.Help");
                //intent.putExtra("HELP_TEXT", "PostItNotes/hlp_about.html");

                break;

        }
        startActivity(intent);
        
    }
}
