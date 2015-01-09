/*
 * Copyright 2013 Benedikt Vogler.
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
 * * Neither the name of Bombing Games nor Benedikt Vogler nor the names of its contributors 
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
package com.BombingGames.WurfelEngine.Core.LightEngine;

import com.BombingGames.WurfelEngine.Core.CVar;
import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.GameView;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Sides;
import com.BombingGames.WurfelEngine.Core.Map.Chunk;
import com.BombingGames.WurfelEngine.Core.Map.LinkedWithMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;


/**
 * This Light engine calculates phong shading for three normals over the day.
 * @author Benedikt Vogler
 * @version 1.1.7
 * @since  WE1.1
 */
public class LightEngine implements LinkedWithMap {
    /**
     * The Version of the light engine.
     */
    public static final String Version = "1.1.7";

    private boolean renderData = false;
    //diagramm data
    private int posX = 250;
    private int posY = Gdx.graphics.getHeight()-250;
    private final int size = 500;
	private boolean normalMapRendering = true;
    
    
    //diffuse light
    private final float k_diff = 100/255f; //the min and max span. value between 0 and 1 empirisch bestimmter Reflexionsfaktor für diffuse Komponente der Reflexion
    private float I_diff0, I_diff1, I_diff2;
    
    //specular light
    private final int n_spec = 12; //  constant factor describing the Oberflächenbeschaffenheit (rau smaller 32, glatt bigger 32, infinity would be a perfect mirror)
    private final float k_specular = 1-k_diff; //empirisch bestimmter reflection factor of mirroring component of reflection. Value "k_diff+kspecular <= 1" therefore 1-k_diff is biggest possible value 
    private float I_spec1;
             
    /**the brightness of each side including amb+diff+spec. The value should be between 0 and 1*/
    private static float I_0, I_1, I_2;
    
    private GlobalLightSource sun;
    private GlobalLightSource moon; 

    /**
     * 
     */
    public LightEngine() {
        sun = new GlobalLightSource(-Controller.getMap().getWorldSpinDirection(), 0, new Color(255, 255, 255, 1), new Color(0.1f, 0.1f, 0, 1), 60);
		//add moon if not using normalMaprendering
        if (!normalMapRendering)
			moon = new GlobalLightSource(
				180-Controller.getMap().getWorldSpinDirection(),
				0,
				new Color(0.2f,0.4f,0.8f,1),
				new Color(0, 0, 0.1f, 1),
				45
			);
		
		normalMapRendering = CVar.get("LEnormalMapRendering").getValueb();
    }

    /**
     *
     * @param xPos the x position of the diagrams position (center)
     * @param yPos the y position of the diagrams position (center) 
     */
    public LightEngine(int xPos, int yPos) {
        this();
        this.posX = xPos;
        this.posY = yPos;
    }
    
    
    
    /**
     * 
     * @param dt
     */
    public void update(float dt) {
        sun.update(dt);
		
        if (moon != null) {
			moon.update(dt);
			float moonI = moon.getPower();
			//calcualte moon light in diff and spec
			float tmp = (float) (moonI * k_diff * Math.cos(((moon.getHeight()) * Math.PI)/180) * Math.cos(((moon.getAzimuth()-45)*Math.PI)/180));
			if (tmp>0) I_diff0+=tmp;

			tmp = (float) (moonI * k_diff * Math.cos(((moon.getHeight()-90)*Math.PI)/180));   
			if (tmp>0) I_diff1+=tmp;

			tmp = (float) (moonI  * k_diff * Math.cos(((moon.getHeight())*Math.PI)/180)*Math.cos(((moon.getAzimuth()-135)*Math.PI)/180));
			if (tmp>0) I_diff2+=tmp;
			
			//specular
			I_spec1 +=(float) (
				moonI
				* k_specular
				* Math.pow(
					Math.sin((moon.getHeight())*Math.PI/180)*Math.sin((moon.getAzimuth())*Math.PI/180)/Math.sqrt(2)//y
				  + Math.sin((moon.getHeight()-90)*Math.PI/180)/Math.sqrt(2)//z
				,n_spec)
				*(n_spec+2)/(2*Math.PI)
			);
		}
		
        float sunI = sun.getPower();
        
        //diffusion
        //diff0
        I_diff0 = (float) (sunI  * k_diff * Math.cos(((sun.getHeight())  * Math.PI)/180) * Math.cos(((sun.getAzimuth() -45)*Math.PI)/180));
        if (I_diff0<0) I_diff0=0;
        
       
        //diff0
        I_diff1 = (float) (sunI * k_diff * Math.cos(((sun.getHeight() -90)*Math.PI)/180)); 
        if (I_diff1<0) I_diff1=0;
        
        //diff2
        I_diff2 = (float) (sunI * k_diff * Math.cos(((sun.getHeight()) *Math.PI)/180)*Math.cos(((sun.getAzimuth() -135)*Math.PI)/180));
        if (I_diff2<0) I_diff2=0;
        
        //specular
        
        //it is impossible to get specular light with a GlobalLightSource over the horizon on side 0 and 2. Just left in case i it someday helps somebody.
//        I_spec0 =(int) (
//                        sunI
//                        * k_specular
//                        * Math.pow(
//                            Math.sin((sun.getHeight())*Math.PI/180)*Math.sin((sun.getAzimuth())*Math.PI/180)* Math.sqrt(2)/Math.sqrt(3)//y
//                          + Math.sin((sun.getHeight()-75)*Math.PI/180)/Math.sqrt(3)//z
//                        ,n_spec)
//                        *(n_spec+2)/(2*Math.PI)
//                        );

        
        I_spec1 = (float) (
            sunI
            * k_specular
            * Math.pow(
                Math.sin(sun.getHeight()*Math.PI/180)*Math.sin(sun.getAzimuth()*Math.PI/180)/ Math.sqrt(2)//y
              + Math.sin((sun.getHeight()-90)*Math.PI/180)/ Math.sqrt(2)//z
            ,n_spec)
            *(n_spec+2)/(2*Math.PI)
        );
         
      //it is impossible to get specular light with a GlobalLightSource over the horizon on side 0 and 2. Just left in case it someday may help somebody.
        //        I_spec2 =(int) (
        //                        sunI
        //                        * k_specular
        //                        * Math.pow(
        //                            Math.cos((sun.getHeight() - 35.26) * Math.PI/360)
        //                           *Math.cos((sun.getAzimuth() + 180) * Math.PI/360)
        //                        ,n_spec)
        //                        *(n_spec+2)/(2*Math.PI)
        //                        );   
               
        I_0 = I_diff0;
        I_1 = I_diff1 + I_spec1;
        I_2 = I_diff2;
        
        
        //update input
        if (Gdx.input.isButtonPressed(0)&& renderData){
            //sun.setHeight(sun.getHeight()+Gdx.input.getDeltaY()*30f);
            sun.setAzimuth(Gdx.input.getX());
            if (moon != null)
				moon.setAzimuth(Gdx.input.getX()-180);
        }
    }
    
    /**
     * Returns the average brightness.
     * @return
     */
    public static float getBrightness() {
        return (I_0+I_1+I_2)/3f;
    }
	
	/**
	 * 
	 * @return the normal of the sun
	 */
	public Vector3 getNormal() {
		return new Vector3(
			(float) -Math.cos(sun.getAzimuth()*Math.PI/180f),
			(float) Math.sin(sun.getAzimuth()*Math.PI/180f),
			(float) Math.sin(sun.getHeight()*Math.PI/180f)
		);
	}

	/**
	 * 
	 * @return true if rendering via normal map
	 */
	public boolean isNormalMapRenderingEnabled() {
		return normalMapRendering;
	}
    
        /**
     * Gets average color. 
     * @return pseudoGrey color
     * @see #getColor(com.BombingGames.WurfelEngine.Core.Gameobjects.Sides)
     */
    public Color getColor(){
		if (normalMapRendering)
			return Color.WHITE;
        return getAmbient().add( getEmittingLights().mul( getBrightness() ) );
        //more precise (?) but slower
//        float r = (getColor(Sides.LEFT).r + getColor(Sides.TOP).r + getColor(Sides.RIGHT).r)/3f;
//        float g = (getColor(Sides.LEFT).g + getColor(Sides.TOP).g + getColor(Sides.RIGHT).g)/3f;
//        float b = (getColor(Sides.LEFT).b + getColor(Sides.TOP).b + getColor(Sides.RIGHT).b)/3f;
//        return new Color(r,g,b,1);
    }
        
    /**
     * Get's the brightness to a normal.
     * @param normal 0 left 1 top or 2 right
     * @return pseudoGrey color
     */
    public Color getColor(Sides normal){
          if (normal==Sides.LEFT)
            return getAmbient().add(getDiff(normal));
        else if (normal==Sides.TOP)
            return getAmbient().add(getDiff(normal)).add(getSpec(normal));
        else
            return getAmbient().add(getDiff(normal));
    }
    
    /**
     * 
     * @param normal
     * @return pseudoGrey color
     */
     private Color getDiff(Sides normal){
        if (normal==Sides.LEFT)
            return getEmittingLights().mul(I_diff0);
        else if (normal==Sides.TOP)
            return getEmittingLights().mul(I_diff1);//only top side can have specular light
        else
            return getEmittingLights().mul(I_diff2);
    }
     
    /**
     * 
     * @param normal
     * @return pseudoGrey color
     */
     private Color getSpec(Sides normal){
        if (normal==Sides.LEFT)
            return Color.BLACK.cpy();
        else if (normal==Sides.TOP)
            return getEmittingLights().mul(I_spec1);
        else
            return Color.BLACK.cpy();
    }
    
    
    /**
     * Returns the sum of every light source's ambient light
     * @return a color with a tone
     */
    private Color getAmbient(){
		Color amb = sun.getAmbient();
		if (moon!= null)
			amb.add(moon.getAmbient());
        return amb;
    }
    
    /**
     * Mix of both light sources. Used for diff and spec.
     * @return a color with a tone
     */
    private Color getEmittingLights(){
		Color light = sun.getLight();
		if (moon!= null)
			light.add(moon.getLight());
        return light;
    }
    
     /**
     * Calculates the light level based on the sun shining straight from the top
	 * @param chunk
     */
    public void calcSimpleLight(Chunk chunk){
        for (
			int x=chunk.getData()[0][0][0].getPosition().getX(),
				maxX=chunk.getData()[0][0][0].getPosition().getX()+Chunk.getBlocksX();
			x< maxX;
			x++
		){
            for (
				int y=chunk.getData()[0][0][0].getPosition().getY(),
				maxY=chunk.getData()[0][0][0].getPosition().getY()+Chunk.getBlocksY();
				y < maxY;
				y++
			) {
                //find top most renderobject
                int topmost = Chunk.getBlocksZ()-1;//start at top
                while (Controller.getMap().getBlock(
						x,
						y,
						topmost
					).isTransparent() && topmost > 0 ){
                    topmost--;
                }
                
                if (topmost>0) {
                    //start at topmost renderobject and go down. Every step make it a bit darker
                    for (int level = topmost; level >= 0; level--){
                        Controller.getMap().getBlock(x,y,level).setLightlevel(.5f + .5f*level / (float) topmost);
                    }
                }
            }
        }         
    }

    /**
     *
     * @return
     */
    public boolean isRenderingData() {
        return renderData;
    }

    /**
     *Should diagrams be rendered showing the data of the LE.
     * @param render
     */
    public void renderData(boolean render) {
        this.renderData = render;
    }
    
    
    
    /**
     *Shows the data of the light engine in diagramms.
     * @param view 
     */
    public void render(GameView view){
        if (renderData) {
            
            //g.setLineWidth(2);
            ShapeRenderer shR = view.getShapeRenderer();
            
            //surrounding sphere
            Gdx.gl20.glLineWidth(2);
            shR.setColor(Color.BLACK);
            shR.begin(ShapeType.Line);
                shR.circle(posX, posY, size);


                //cut through
                shR.translate(posX, posY, 0);
                shR.scale(1f, (0.5f), 1f);
                shR.circle(0, 0, size);
                shR.scale(1f, (2), 1f);
                shR.translate(-posX, -posY, 0);

                //perfect/correct line
                shR.setColor(Color.ORANGE);
                if ((sun.getMaxAngle()/90f-0.5f) != 0) {
                    shR.translate(posX, posY, 0);
                    shR.rotate(0, 0, 1, -Controller.getMap().getWorldSpinDirection());
                    shR.scale(1f, (sun.getMaxAngle()/90f-0.5f), 1f);
                    shR.circle(0, 0, size);
                    shR.scale(1f, (1/(sun.getMaxAngle()/90f-0.5f)), 1f);
                    shR.rotate(0, 0, 1, +Controller.getMap().getWorldSpinDirection());
                    shR.translate(-posX, -posY, 0);
                } else {
                    shR.line(posX-size, posY, posX+size, posY);
                }

                //sun position
                //longitude
                shR.setColor(Color.RED);
                shR.line(
                    posX +(int) (size* getNormal().x),
                    posY -(int) (size/2*getNormal().y),
                    posX,
                    posY
                );

                //latitude
                shR.setColor(Color.MAGENTA);
                shR.line(
                    posX -(int) (size/2 * (1-getNormal().z)+size/2),
                    posY +(int) (size/2*getNormal().z),
                    posX,
                    posY
                );

                //long+lat of sun position
                shR.setColor(Color.YELLOW);
                shR.line(
                    posX +(int) ( size*getNormal().x ),
                    posY +(int) (size*( -getNormal().y/2+getNormal().z)),
                    posX,
                    posY
                 );

                shR.setColor(Color.BLUE);
				if (moon != null) {
					shR.line(
						posX +(int) ( size*Math.sin((moon.getAzimuth()+90)*Math.PI/180) * Math.sin((moon.getHeight()-90)*Math.PI/180) ),
						posY +(int) ( size/2*Math.sin((moon.getAzimuth())*Math.PI/180) * Math.sin((moon.getHeight()-90)*Math.PI/180)) +(int) (size/2*Math.sin((moon.getHeight())*Math.PI/180)),
						posX,
						posY
					 );
				}
            shR.end();

            int y = Gdx.graphics.getHeight()-150;
            view.drawString("Lat: "+sun.getHeight(), 600, y, Color.WHITE);
            view.drawString("Long: "+sun.getAzimuth(), 600, y+=10, Color.WHITE);
            view.drawString("PowerSun: "+sun.getPower()*100+"%", 600, y+=10, Color.WHITE);
			if (moon != null)
				view.drawString("PowerMoon: "+moon.getPower()*100+"%", 600, y+=10, Color.WHITE);
            view.drawString("Ambient: "+getAmbient().toString(), 600, y+=10, Color.WHITE);
            view.drawString("avg. color: "+getColor().toString(), 600, y+=10, Color.WHITE);
			
            shR.begin(ShapeType.Filled);
                //draw ambient light
                shR.setColor(Color.WHITE);
                shR.rect(600, y+=10, 70, 70);
                shR.setColor(getAmbient());
                shR.rect(610, y+=10, 50, 50);
				if (moon != null) {
					shR.setColor(moon.getAmbient());
					shR.rect(600, y, 20, 25);
				}
                shR.setColor(sun.getAmbient());
                shR.rect(600, y+25, 20, 25);
                
                view.drawText("+", 670, y+25, Color.WHITE);
                //draw emmittinlights
                shR.setColor(Color.WHITE);
                shR.rect(680, y-10, 70, 70);
                shR.setColor(getEmittingLights().mul( getBrightness()));
                shR.rect(690, y, 50, 50);
                
                view.drawText("=", 760, y+25, Color.WHITE);
                 //draw result
                shR.setColor(Color.WHITE);
                shR.rect(770, y-10, 70, 70);
                shR.setColor(getColor(Sides.LEFT));
                shR.rect(780, y, 25, 30);
                shR.setColor(getColor(Sides.TOP));
                shR.rect(780, y+25, 50, 30);
                shR.setColor(getColor(Sides.RIGHT));
                shR.rect(805, y, 25, 30);
                
                shR.setColor(getColor());
                shR.rect(800, y+20, 15, 15);


                 //info bars

                //left side
                y = Gdx.graphics.getHeight()-100;
                view.drawText(Float.toString(I_diff0), (int) (I_0*size), y, Color.WHITE);
                shR.setColor(Color.RED);
                shR.rect(0, y, I_diff0*size, 8);


                //top side
                y = Gdx.graphics.getHeight()-180;
                view.drawText(I_diff1+"\n+"+ I_spec1+"\n="+I_1, (int) (I_1*size), y, Color.WHITE);
                shR.setColor(Color.RED);
                shR.rect(0, y, I_diff1*size, 8);
                shR.setColor(Color.BLUE);
                shR.rect(I_diff1*size, y, I_spec1*size, 6);

                //right side
                y = Gdx.graphics.getHeight()-260;
                view.drawText(Float.toString(I_diff2), (int) (I_2*size), y, Color.WHITE);
                shR.setColor(Color.RED);
                shR.rect(0, y, I_diff2*size, 8);
            
            shR.end();
            Gdx.gl20.glLineWidth(1);
        }
    }
    
    /**
     *
     */
    public void setToNoon(){
        sun.setAzimuth(90);
		if (moon != null) 
			moon.setAzimuth(270);
    }
    
    /**
     *
     */
    public void setToNight(){
        sun.setAzimuth(270);
        moon.setAzimuth(90);
    }

	@Override
	public void onMapChange() {
	}

	@Override
	public void onChunkChange(Chunk chunk) {
		calcSimpleLight(chunk);
	}
	
	
}
