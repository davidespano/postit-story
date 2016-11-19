package com.vuforia.samples.VuforiaSamples.app.PostItNote;

import android.util.Log;
import android.util.Xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

    public static List<Parola> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readDictionary(parser);
        } finally {
            in.close();
        }
    }

    private static List<Parola> readDictionary(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Parola> items = new ArrayList<Parola>();

        parser.require(XmlPullParser.START_TAG, ns, "dictionary");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }
        return items;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private static Parola readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        Parola word = new Parola();
        word.setId_marker(counter);
        counter++;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("firstLanguageWord")) {

                Log.d("Debug: Cerco FL ",name);
                word.setPathfl(readPathFL(parser));
                Log.d("Debug: esco da FL", name);
            } else if (name.equals("secondLanguageWord")) {
                word.setPathsl(readPathSL(parser));
            } else if (name.equals("ARLayer")) {
                word.setPathTexture(readPath(parser));
                parser.next();
            } else {
                Log.d("Debug:", parser.getName());
                parser.nextTag();
                Log.d("Debug: ", parser.getName());
                //skip(parser);
            }
        }
        return word;
    }

    private static String readPathFL(XmlPullParser parser) throws IOException, XmlPullParserException {

        Log.d("Debug: Entro nella ", "funzioneFL");
        parser.require(XmlPullParser.START_TAG, ns, "firstLanguageWord");

        Log.d("Debug: rilevo di nuovo", "FLW");
        String secondLanguageWord = readAudio(parser);

        Log.d("Debug3: riconosco audio", secondLanguageWord);
        parser.require(XmlPullParser.END_TAG, ns, "firstLanguageWord");
        return secondLanguageWord;
    }
    private static String readPathSL(XmlPullParser parser) throws IOException, XmlPullParserException {

        Log.d("Debug: Entro nella ", "funzioneSL");
        parser.require(XmlPullParser.START_TAG, ns, "secondLanguageWord");

        Log.d("Debug: rilevo di nuovo", "FLW");
        String firstLanguageWord = readAudio(parser);

        Log.d("Debug3: riconosco audio", firstLanguageWord);
        parser.require(XmlPullParser.END_TAG, ns, "secondLanguageWord");
        return firstLanguageWord;
    }

    private static String readAudio(XmlPullParser parser) throws IOException, XmlPullParserException {

        Log.d("Debug: Richiedo audio", "?");
        //parser.nextToken();
        parser.nextTag();

        Log.d("Debug: prossimo tag", parser.getName());
        parser.nextTag();

        Log.d("Debug: prossimo tag", parser.getName());
        parser.nextTag();

        Log.d("Debug: prossimo tag", parser.getName());

        parser.require(XmlPullParser.START_TAG, ns, "syntax");
        parser.next();
        parser.next();
        parser.require(XmlPullParser.END_TAG, ns, "syntax");
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "phonetics");
        parser.next();
        parser.next();
        parser.require(XmlPullParser.END_TAG, ns, "phonetics");

        Log.d("Debug: prossimo tag", parser.getName());
        parser.nextTag();
        Log.d("Debug: prossimo tagA", parser.getName());
        //parser.require(XmlPullParser.START_TAG, ns, "audio");
        String audio = "";
        audio = readPath(parser);
        parser.nextTag();
        parser.nextTag();

        Log.d("Debug: prossimo tagAA", parser.getName());
        //parser.require(XmlPullParser.END_TAG, ns, "audio");
        return audio;
    }

    private static String readPath(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        //if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getAttributeValue(null,"path");
            Log.d("Debug3: riconosco path", result);
            //parser.nextTag();
        //}
        return result;
    }


    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    public static List<Parola> updateNote() throws IOException, XmlPullParserException {
        File f=new File("/storage/emulated/0/lezione/info.xml");
        InputStream is = new FileInputStream(f);

        List<Parola> list = parse(is);

        //return (Parola[]) list.toArray();

        Log.d("Fino a qui","tuttobene");
        return list;
    }

    public static Parola[] updateNote2()throws ParserConfigurationException, IOException, SAXException {
        Parola[] parolas;
        // Costruiamo una factory per processare il nostro flusso di dati
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Instanziamo un nuovo Documento
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Carichiamo il nostro documento da un file (assicuratevi sia nel path giusto)
        Document doc = builder.parse(new File("/storage/emulated/0/lezione/info.xml"));
        doc.normalizeDocument();

        // Prendiamo il primo nodo - come suggerisce il metodo - la radice
        Node root = doc.getFirstChild();

        parolas = new Parola[(root.getChildNodes().getLength()-1)/2];
        int indexParolas = 0;

        Log.d("Debug1: indice parole", indexParolas+"");
        // Iteriamo per ogni nodo presente nella lista dei nodi della radice
        for (int i = 0; i < root.getChildNodes().getLength(); i++) {

            //Integer id = Integer.getInteger(root.getAttributes().getNamedItem("id").getTextContent());

            Log.d("Debug2: ", root.getChildNodes().getLength()+"");
            // Sapendo che il primo nodo è il nodo item procediamo iterando nei suoi nodi figli
            Node item = root.getChildNodes().item(i);
            //Integer id = Integer.parseInt(item.getAttributes().getNamedItem("id").getTextContent());

            //Avrà 3 elementi: firstLanguageWord, secondLanguageWord, ARLayer

            if(item.getNodeName()=="item") {

                Log.d("Debug3: riconosco item", indexParolas+"");
                for (int y = 0; y < item.getAttributes().getLength(); y++) {
                    parolas[indexParolas] = new Parola();
                    if (item.getAttributes().item(y).getNodeName() == "id")
                        //System.out.println(item.getNodeName());
                        parolas[indexParolas]
                                .setId_marker(
                                        Integer.parseInt(item.getAttributes().item(y).getNodeValue()));
                    //System.out.println("iterazione: "+item.getAttributes().item(y).getNodeValue());
                    indexParolas++;
                }

                for (int j = 0; j < item.getChildNodes().getLength(); j++) {
                    Node element = item.getChildNodes().item(j);
                    if (element.getNodeType() == Node.ELEMENT_NODE) {
                        if (element.getNodeName() == "firstLanguageWord") {
                            for (int l = 0; l < element.getChildNodes().getLength(); l++) {
                                Node audio = element.getChildNodes().item(l);
                                if (audio.getNodeName() == "audio") {
                                    parolas[i].setPathfl(audio.getAttributes().item(0).getNodeValue());
                                }
                            }
                        } else if (element.getNodeName() == "secondLanguageWord") {
                            for (int l = 0; l < element.getChildNodes().getLength(); l++) {
                                Node audio = element.getChildNodes().item(l);
                                if (audio.getNodeName() == "audio") {
                                    parolas[i].setPathsl(audio.getAttributes().item(0).getNodeValue());
                                }
                            }
                        } else if (element.getNodeName() == "ARLayer") {
                            parolas[i].setPathTexture(element.getAttributes().item(0).getNodeValue());
                        }
                    }
                }
            }
            else{
                Log.d("Debug4", "Non riconosco item");
            }

        }
        return parolas;
    }
}
