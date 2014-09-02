/*
 * If this software is used for a game the official „Wurfel Engine“ logo or its name must be visible in an intro screen or main menu.
 * 
 * Copyright 2014 Benedikt Vogler.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * * Neither the name of Benedikt Vogler nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.BombingGames.WurfelEngine.Core;

import com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractGameObject;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Block;
import com.BombingGames.WurfelEngine.Core.Map.Chunk;
import com.BombingGames.WurfelEngine.Core.Map.Intersection;
import com.BombingGames.WurfelEngine.Core.Map.Map;
import com.BombingGames.WurfelEngine.Core.Map.Point;
import com.BombingGames.WurfelEngine.WE;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.ArrayList;

/**
 * The GameView manages everything what should be drawn in an active game.
 * @author Benedikt
 */
public class GameView implements GameManager {
    private final ArrayList<Camera> cameras = new ArrayList<>(6);//max 6 cameras
    
    
    private ShapeRenderer igShRenderer;
    
    private Controller controller;
    
    private int drawmode;
    
    private OrthographicCamera hudCamera;
    private boolean keyF5isUp;
    
    private Stage stage;
        
    private boolean initalized;
    
    
    /**
     * Shoud be called before the object get initialized.
     * Initializes class fields.
     */
    public static void classInit(){
        //set up font
        //font = WurfelEngine.getInstance().manager.get("com/BombingGames/WurfelEngine/EngineCore/arial.fnt"); //load font
        //font.scale(2);

        //font.scale(-0.5f);
        
        //load sprites
        Block.loadSheet();
    }
    
    /**
     *Loades some files and set up everything. This should be done after creating and linking the view.
     * @param controller
     */
    public void init(final Controller controller){
        Gdx.app.debug("View", "Initializing");
        
        this.controller = controller;
        
        //clear old stuff
        cameras.clear();
        
        //set up renderer
        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        

        igShRenderer = new ShapeRenderer();
        
        //set up stage
        stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, EngineView.getBatch());//spawn at fullscreen
        
        //load cursor


        initalized = true;
    }
    
    /**
     *Updates every camera and everything else which must be updated.
     * @param delta time since last update in ms.
     */
    public void update(final float delta){
        AbstractGameObject.resetDrawCalls();
        
        stage.act(delta);
        
        //update cameras
        for (Camera camera : cameras) {
            if (camera.togglesChunkSwitch()) {
                //earth to right
                if (camera.getVisibleLeftBorder() <= 0)
                    Controller.getMap().setCenter(3);
                else
                    if (camera.getVisibleRightBorder() >= Map.getBlocksX()-1) 
                        Controller.getMap().setCenter(5); //earth to the left

                //scroll up, earth down            
                if (camera.getVisibleTopBorder() <= 0)
                    Controller.getMap().setCenter(1);
                else
                    if (camera.getVisibleBottomBorder() >= Map.getBlocksY()-1)
                        Controller.getMap().setCenter(7); //scroll down, earth up
            }
            camera.update();
        }
        
        // toggle the dev menu?
        if (keyF5isUp && Gdx.input.isKeyPressed(Keys.F5)) {
            controller.getDevTools().setVisible(!controller.getDevTools().isVisible());
            keyF5isUp = false;
        }
        keyF5isUp = !Gdx.input.isKeyPressed(Keys.F5);
    }
    
    /**
     * Main method which is called every time and renders everything.
     */
    public void render(){       
        //Gdx.gl10.glViewport(0, 0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        //clear screen if wished
        if (WE.getCurrentConfig().clearBeforeRendering()){
            Gdx.gl10.glClearColor(0, 0, 0, 1);
            Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
        }

        //render every camera
        if (cameras.isEmpty()){
            Gdx.gl10.glClearColor(0.5f, 1, 0.5f, 1);
            Gdx.gl10.glClear(GL10.GL_COLOR_BUFFER_BIT);
            drawString("No camera set up", Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Color.BLACK.cpy());
        } else {
            for (Camera camera : cameras) {
                camera.render(this, camera);
            }
        }
               
        //render HUD and GUI
        {
            // hudCamera.zoom = 1/equalizationScale;
            hudCamera.update();
            hudCamera.apply(Gdx.gl10);

            EngineView.getBatch().setProjectionMatrix(hudCamera.combined);
            igShRenderer.setProjectionMatrix(hudCamera.combined);
            EngineView.getShapeRenderer().setProjectionMatrix(hudCamera.combined);
            Gdx.gl10.glLineWidth(1);

            //set viewport of hud to cover whole window
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

            //end of setup
            
            controller.getDevTools().render(this);

            //render buttons
            stage.draw();

            //scale to fit
            //hudCamera.zoom = 1/equalizationScale;

            if (Controller.getLightengine() != null)
                Controller.getLightengine().render(this);

            if (controller.getMinimap() != null)
                controller.getMinimap().render(this); 
        }
    }
       
    /**
     * The equalizationScale is a factor which scales the GUI/HUD to have the same relative size with different resolutions.
     * @return the scale factor
     */
    public float getEqualizationScale() {
        return Gdx.graphics.getWidth() / WE.getCurrentConfig().getRenderResolutionWidth();
    }

    
   /**
     * Reverts the perspective and transforms it into a coordiante which can be used in the game logic.
     * @param x the x position on the screen
     * @param camera the camera where the position is on
     * @return the relative (to current loaded map) game coordinate
     */
    public float screenXtoGame(final int x, final Camera camera){
        return x / camera.getScaling()- camera.getScreenPosX()+ camera.getViewportPosX();
    }
    
   /**
     * Reverts the perspective and transforms it into a coordiante which can be used in the game logic.
     * @param y the y position on the screen
     * @param camera the camera where the position is on
     * @return the relative game coordinate
     */
    public float screenYtoGame(final int y, final Camera camera){
        return (y / camera.getScaling() + camera.getViewportPosY())*2 - camera.getScreenPosY();
    }
    
    /**
     * Returns deepest layer
     * @param x
     * @param y
     * @return 
     */
     public Point screenToGameFlat(final int x, final int y){
        //identify clicked camera
        Camera camera;
        int i = 0;
        do {          
            camera = cameras.get(i);
            i++;
        } while (
                i < cameras.size()
                && !(x > camera.getScreenPosX() && x < camera.getScreenPosX()+camera.getScreenWidth()
                && y > camera.getScreenPosY() && y < camera.getScreenPosY()+camera.getScreenHeight())
        );
 
        //find points
        return new Point(
                screenXtoGame(x, camera),
                screenYtoGame(y, camera),
                0,
                true
            );
    }
     
    /**
     * Returns the position belonging to a point on the screen. Does raytracing to find the intersection.
     * @param x the x position on the screen
     * @param y the y position on the screen
     * @return the position on the map. Deepest layer.
     */
    public Intersection screenToGameRaytracing(final int x, final int y){
        Point p = screenToGameFlat(x,y);
        float deltaZ = Chunk.getGameHeight()-Block.GAME_EDGELENGTH-p.getHeight();
        p.addVector(0, (float) (deltaZ/Math.sqrt(2)*2), deltaZ);//top of map

        return p.raycast(new Vector3(0,-1, -0.70710678f), 5000);
    }
    
    /**
     *
     * @return
     */
    public int getDrawmode() {
        return drawmode;
    }

    /**
     *The batch must be began before claling this method.
     * @param drawmode
     */
    public void setDrawmode(final int drawmode) {
        if (drawmode != this.drawmode){
            this.drawmode = drawmode;
            EngineView.getBatch().end();
            //GameObject.getSpritesheet().getFullImage().endUse();
            Gdx.gl10.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, drawmode);
            //GameObject.getSpritesheet().getFullImage().startUse();
            EngineView.getBatch().begin();
        }
    }

    /**
     *Draw a string using the last active color.
     * @param msg
     * @param xPos
     * @param yPos
     */
    public void drawString(final String msg, final int xPos, final int yPos) {
        EngineView.getBatch().begin();
        setDrawmode(GL10.GL_MODULATE);
        EngineView.getFont().draw(EngineView.getBatch(), msg, xPos, yPos);
        EngineView.getBatch().end();
    }
    
    /**
     *Draw a string in a color.
     * @param msg
     * @param xPos
     * @param yPos
     * @param color
     */
    public void drawString(final String msg, final int xPos, final int yPos, final Color color) {
        EngineView.getBatch().setColor(color);
        EngineView.getBatch().begin();
        setDrawmode(GL10.GL_MODULATE);
        EngineView.getFont().draw(EngineView.getBatch(), msg, xPos, yPos);
        EngineView.getBatch().end();
    }
    
    /**
     *Draw multi-lines with this method
     * @param text
     * @param xPos space from left
     * @param yPos space from top
     * @param color the colro of the text.
     */
    public void drawText(final String text, final int xPos, final int yPos, final Color color){
        EngineView.getFont().setColor(Color.BLACK);
        EngineView.getFont().setScale(0.51f);
        EngineView.getBatch().begin();
        setDrawmode(GL10.GL_MODULATE);
        EngineView.getFont().drawMultiLine(EngineView.getBatch(), text, xPos, yPos);
        EngineView.getBatch().end();
        
        EngineView.getFont().setColor(Color.WHITE);
        EngineView.getFont().setScale(0.5f);
        EngineView.getBatch().begin();
        EngineView.getFont().drawMultiLine(EngineView.getBatch(), text, xPos, yPos);
        EngineView.getBatch().end();
        EngineView.getFont().setScale(1f);
    }
    
    /**
     *
     * @return
     */
    public OrthographicCamera getHudCamera() {
        return hudCamera;
    } 



    /**
     *
     * @return
     */
    public ShapeRenderer getIgShRender() {
        return igShRenderer;
    }
    

    /**
     *
     * @return
     */
    public Controller getController() {
        return controller;
    }
    
     /**
     * Returns a camera.
     * @return The virtual cameras rendering the scene
     */
    public ArrayList<Camera> getCameras() {
        return cameras;
    }

    /**
     * Add a camera.
     * @param camera
     */
    protected void addCamera(final Camera camera) {
        this.cameras.add(camera);
    }
    
     /**
     * should be called when the window get resized
     * @param width
     * @param height 
     */
    public void resize(final int width, final int height) {
        for (Camera camera : cameras) {
            camera.resize(width, height);
        }
        stage.setViewport(width, height);
        EngineView.getStage().setViewport(width, height);
        hudCamera.setToOrtho(false, width, height);
        Gdx.gl.glViewport(0, 0, width,height);
    }

    /**
     * The libGDX scene2d stage
     * @return 
     */
    public Stage getStage() {
        return stage;
    }


    /**
     *
     * @return
     */
    @Override
    public boolean isInitalized() {
        return initalized;
    }

 
   /**
     *override to specify what should happen when the mangager becomes active
     */
    @Override
    public void onEnter(){
        
    }
    
    @Override
    public final void enter() {
        EngineView.addInputProcessor(stage);//the input processor must be added every time because they are only 
        Gdx.input.setCursorImage(EngineView.getCursor(), 8, 8);
        onEnter();
    }
    
}