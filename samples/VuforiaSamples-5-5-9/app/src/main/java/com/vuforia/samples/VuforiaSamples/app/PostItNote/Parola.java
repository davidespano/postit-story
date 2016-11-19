package com.vuforia.samples.VuforiaSamples.app.PostItNote;

/**
 * Created by alessiosperoni on 21/07/16.
 */
public class Parola {
    private String pathfl;
    private String pathsl;
    private String pathTexture;
    private int id_marker;

    public Parola(){
        pathfl = "1/audiofl.mp3";
        pathsl = "1/audiosl.mp3";
        pathTexture = "1/ARLayer.png";
        id_marker = 0;
    }
    public Parola(int id_marker, String pathfl, String pathsl, String pathTexture){
        this.id_marker=id_marker;
        this.pathTexture=pathTexture;
        this.pathsl = pathsl;
        this.pathfl=pathfl;
    }


    public String getPathfl() {
        return pathfl;
    }

    public void setPathfl(String pathfl) {
        this.pathfl = pathfl;
    }

    public String getPathsl() {
        return pathsl;
    }

    public void setPathsl(String pathsl) {
        this.pathsl = pathsl;
    }

    public String getPathTexture() {
        return pathTexture;
    }

    public void setPathTexture(String pathTexture) {
        this.pathTexture = pathTexture;
    }

    public int getId_marker() {
        return id_marker;
    }

    public void setId_marker(int id_marker) {
        this.id_marker = id_marker;
    }
}
