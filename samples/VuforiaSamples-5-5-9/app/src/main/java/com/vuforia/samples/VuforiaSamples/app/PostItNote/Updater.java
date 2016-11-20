package com.vuforia.samples.VuforiaSamples.app.PostItNote;

import android.media.MediaPlayer;
import android.util.Log;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by alessiosperoni on 21/07/16.
 */
public class Updater {

    private static final String ns = null;
    private static int counter = 0;
    private static Updater singleton;

    private Node storyNode;
    private XPath xPath;
    private NodeList items;
    List<Parola> words;
    private boolean loaded = false;


    private Updater(){
        words = new ArrayList<>();
        loaded = false;
    }

    public static Updater getInstance(){
        if(singleton == null){
            singleton = new Updater();
        }

        return singleton;
    }

    public  void loadStoryResources(String directory){

        try {
            XPathFactory factory = XPathFactory.newInstance();
            xPath = factory.newXPath();
            FileReader storyXmlFile = new FileReader(new File(directory + File.separator + "info.xml"));
            InputSource storyXml = new InputSource(storyXmlFile);
            storyNode = (Node) xPath.evaluate("/",
                    storyXml, XPathConstants.NODE);

            words.clear();

            items = (NodeList) xPath.evaluate("/story/dictionary/item",
                    storyNode, XPathConstants.NODESET);
            for(int i = 0; i< items.getLength(); i++){
                Node item = items.item(i);

                Parola p = new Parola();
                p.setId_marker(Integer.parseInt(
                        (String)xPath.evaluate("@id", item, XPathConstants.STRING)));
                p.setPathfl(directory + File.separator +
                        (String)xPath.evaluate("firstLanguageWord/audio/@path", item, XPathConstants.STRING));
                p.setPathsl(directory + File.separator +
                        (String)xPath.evaluate("secondLanguageWord/audio/@path", item, XPathConstants.STRING));
                p.setPathTexture(directory + File.separator +
                        (String)xPath.evaluate("ARLayer/@path", item, XPathConstants.STRING));
                words.add(p);
            }
            loaded = true;

        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLoaded(){
        return loaded;
    }

    public int getSteps(){
        int steps = -1;
        try {
            NodeList items = (NodeList) xPath.evaluate("/story/item",
                    storyNode, XPathConstants.NODESET);
            steps = items.getLength();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } finally {
            return steps;
        }
    }

    public String getStoryPhrase(int position){
        String phraseText ="";
        try {

            MessageFormat path = new MessageFormat("/story/item[@id={0}]");
            String pathString = path.format(new Object[]{position});
            Node phraseNode = null;
            phraseNode = (Node) xPath.evaluate(pathString, storyNode, XPathConstants.NODE);
            phraseText = phraseNode.getTextContent();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } finally {
            return phraseText;
        }
    }

    public String getWord(int reference){
        String correctWord = "";
        try {
            MessageFormat path = new MessageFormat("/story/dictionary/item[@id={0}]/secondLanguageWord/syntax");
            String pathString = path.format(new Object[]{reference});
            Node hiddenNode = (Node) xPath.evaluate(pathString, storyNode, XPathConstants.NODE);
            correctWord = hiddenNode.getTextContent();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } finally {
            return correctWord;
        }
    }

    public List<Parola> getWords(){
        return words;
    }

    public void playAudio(int id){
        for(Parola p : words){
            if(p.getId_marker() == id){
                playAudio(p.getPathsl());
                return;
            }
        }
    }

    public void playAudio(String path){
        try {
            MediaPlayer audio = new MediaPlayer();
            audio.setDataSource(path);
            audio.prepare();
            audio.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
