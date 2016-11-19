/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.Help;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.PostItNote;
import com.vuforia.samples.VuforiaSamples.app.PostItStory.PostItStorySelection;


// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends Activity
{
    
    private String mActivities[] = {"Post-It Notes","Post-It Story","Help"};
    
    TextView storytellingText, exploringText;
    ImageView storytellingImage, exploringImage;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            R.layout.activities_list_text_view, mActivities);
        
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        
        setContentView(R.layout.activities_list);
        //setListAdapter(adapter);

        storytellingText = (TextView) findViewById(R.id.storytellingText);
        exploringText = (TextView) findViewById(R.id.exploringText);
        storytellingImage = (ImageView) findViewById(R.id.storytellingImage);
        exploringImage = (ImageView) findViewById(R.id.exploringImage);

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

        storytellingText.setOnClickListener(storytellingClick);
        storytellingImage.setOnClickListener(storytellingClick);
        exploringText.setOnClickListener(exploringClick);
        exploringImage.setOnClickListener(exploringClick);

    }
    
    

    public void changeActivity(int position)
    {
        
        Intent intent = null;
        //intent.putExtra("ABOUT_TEXT_TITLE", mActivities[position]);
        
        switch (position)
        {
            case 0:
                intent = new Intent(this, PostItNote.class);
                //intent.putExtra("ACTIVITY_TO_LAUNCH", "app.PostItNote.PostItNote");
                //intent.putExtra("ABOUT_TEXT", "PostItNote/PIN_about.html");
                break;
            case 1:
                intent = new Intent(this, PostItStorySelection.class);
                //intent.putExtra("ACTIVITY_TO_LAUNCH","app.PostItStory.PostItStorySelection");
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
