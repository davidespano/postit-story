/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.app.PostItStory;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.vuforia.samples.VuforiaSamples.R;
import com.vuforia.samples.VuforiaSamples.app.PostItNote.Help;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.AboutScreen;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


// This activity starts activities which demonstrate the Vuforia features
public class PostItStoryList extends ListActivity
{

    private static final String baseDir = "postit-story";
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();

    TextView savedText, searchText;
    ImageView savedImage, searchImage;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initStoryFile();

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.activities_list_text_view,
                new String[] {"title", "words" },
                new int[] {android.R.id.text1, android.R.id.text2 });

        setContentView(R.layout.storieslist);
        setListAdapter(adapter);


    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {

        Intent intent = new Intent(this, PostItStoryText.class);
        intent.putExtra("story_id", position);
        startActivity(intent);
        
    }

    private void initStoryFile(){
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File storyDirectory = new File(sdcard, baseDir);
            String[] directories = storyDirectory.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            });
            XPathFactory factory = XPathFactory.newInstance();
            XPath xPath = factory.newXPath();
            Map<String, String> datum = new HashMap<String, String>(2);
            for(String story : directories){
                FileReader storyXmlFile = new FileReader(new File(storyDirectory,
                        story + File.separator+ "info2.xml"));
                InputSource storyXml = new InputSource(storyXmlFile);
                Node storyNode = (Node) xPath.evaluate("/",
                        storyXml, XPathConstants.NODE);
                NodeList items = (NodeList) xPath.evaluate("/story/item",
                            storyNode, XPathConstants.NODESET);
                int words = 0;
                for(int i = 0; i< items.getLength(); i++){
                    String value = items.item(i).getFirstChild().getNodeValue();
                    if(value != null && value.contains("#")){
                        words ++;
                    }
                }
                String title = (String) xPath.evaluate("/story/@name",
                        storyNode,
                        XPathConstants.STRING);

                datum = new HashMap<>(2);
                datum.put("title", title);
                datum.put("words", "(" + words + " words)");
                data.add(datum);

            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
