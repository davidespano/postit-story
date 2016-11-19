/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.PostItNote;

import java.nio.Buffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;

import com.vuforia.Marker;
import com.vuforia.MarkerResult;
import com.vuforia.MarkerTracker;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.TrackableResult;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.Vec2F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Texture;


// The renderer class for the FrameMarkers sample. 
public class PostItNoteRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "PostItNoteRenderer";
    
    SampleApplicationSession vuforiaAppSession;
    PostItNote mActivity;
    
    public boolean mIsActive = false;
    
    private Vector<Texture> mTextures;
    
    // OpenGL ES 2.0 specific:
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
    
    // Constants:
    static private float kMeshScale = 25.0f;
    static private float kMeshTranslate = 25.0f;

    private Fumetto Fumetto = new Fumetto();

    // Trackable dimensions
    public Vec2F targetPositiveDimensions[] = new Vec2F[PostItNote.NUM_TARGETS];

    public Matrix44F modelViewMatrix_a[] = new Matrix44F[PostItNote.NUM_TARGETS];

    public PostItNoteRenderer(PostItNote activity,
                              SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;

        for (int i = 0; i < PostItNote.NUM_TARGETS; i++)
            targetPositiveDimensions[i] = new Vec2F();

        for (int i = 0; i < PostItNote.NUM_TARGETS; i++)
            modelViewMatrix_a[i] = new Matrix44F();
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    void initRendering()
    {
        Log.d(LOGTAG, "initRendering");
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        
        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
    }
    
    
    void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Get the state from Vuforia and mark the beginning of a rendering
        // section
        State state = Renderer.getInstance().begin();
        
        // Explicitly render the Video Background
        Renderer.getInstance().drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // We must detect if background reflection is active and adjust the
        // culling direction.
        // If the reflection is active, this means the post matrix has been
        // reflected as well,
        // therefore standard counter clockwise face culling will result in
        // "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        // Set the viewport
        int[] viewport = vuforiaAppSession.getViewport();
        GLES20.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);

        float temp[] = { 0.0f, 0.0f };
        for (int i = 0; i < PostItNote.NUM_TARGETS; i++)
        {
            targetPositiveDimensions[i].setData(temp);
        }

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);

            //Cosa sucede qui? *****************************************************
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose()).getData();

            // Choose the texture based on the target name:
            int textureIndex = 0;
            
            // Check the type of the trackable:
            assert (trackableResult.getType() == MarkerTracker.getClassType());
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();
            
            textureIndex = marker.getMarkerId();

            //Salvo la matrice per calcolare il tocco
            modelViewMatrix_a[textureIndex] = Tool.convertPose2GLMatrix(trackableResult.getPose());


            targetPositiveDimensions[textureIndex] = marker.getSize();

            // The pose delivers the center of the target, thus the dimensions
            // go from -width/2 to width/2, same for height
            temp[0] = targetPositiveDimensions[textureIndex].getData()[0] / 2.0f;
            temp[1] = targetPositiveDimensions[textureIndex].getData()[1] / 2.0f;
            targetPositiveDimensions[textureIndex].setData(temp);



            assert (textureIndex < mTextures.size());
            Texture thisTexture = mTextures.get(textureIndex);
            
            // Select which model to draw:
            Buffer vertices = Fumetto.getVertices();
            Buffer normals = Fumetto.getNormals();
            Buffer indices = Fumetto.getIndices();
            Buffer texCoords = Fumetto.getTexCoords();
            int numIndices = Fumetto.getNumObjectIndex();
            
            float[] modelViewProjection = new float[16];

            Matrix.translateM(modelViewMatrix, 0, -kMeshTranslate,
                -kMeshTranslate, 0.f);
            Matrix.scaleM(modelViewMatrix, 0, kMeshScale, kMeshScale,
                    kMeshScale);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            GLES20.glUseProgram(shaderProgramID);
            
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, vertices);
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, normals);
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, texCoords);
            
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);
            
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                thisTexture.mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                GLES20.GL_UNSIGNED_SHORT, indices);
            
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            SampleUtils.checkGLError("FrameMarkers render frame");
            
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        Renderer.getInstance().end();
        
    }
    //0 = no, 1 = sx, 2 = dx
    int isTapOnScreenInsideTarget(int target, float x, float y)
    {
        // Here we calculate that the touch event is inside the target
        Vec3F intersection;

        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        intersection = SampleMath.getPointToPlaneIntersection(
                SampleMath.Matrix44FInverse(vuforiaAppSession.getProjectionMatrix()),
                modelViewMatrix_a[target],
                metrics.widthPixels,
                metrics.heightPixels,
                new Vec2F(x, y),
                new Vec3F(0, 0, 0),
                new Vec3F(0, 0, 1)
        );

        // The target returns as pose the center of the trackable. The following
        // if-statement simply checks that the tap is within this range
        if ((       intersection.getData()[0] >= -(targetPositiveDimensions[target].getData()[0])*4)
                && (intersection.getData()[0] <= 0)
                && (intersection.getData()[1] >= 0)
                && (intersection.getData()[1] <=  (targetPositiveDimensions[target].getData()[1])*3))
            return 1;
        else if ((  intersection.getData()[0] >= 0)
                && (intersection.getData()[0] <=  (targetPositiveDimensions[target].getData()[0])*4)
                && (intersection.getData()[1] >= 0)
                && (intersection.getData()[1] <=  (targetPositiveDimensions[target].getData()[1])*3))
            return 2;
        else
            return 0;
    }

    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
    
}
