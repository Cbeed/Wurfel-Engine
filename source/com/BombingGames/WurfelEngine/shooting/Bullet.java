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
package com.BombingGames.WurfelEngine.shooting;

import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractEntity;
import com.BombingGames.WurfelEngine.Core.Gameobjects.AnimatedEntity;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Explosion;
import com.BombingGames.WurfelEngine.Core.Gameobjects.MovableEntity;
import com.BombingGames.WurfelEngine.Core.Gameobjects.SimpleEntity;
import com.BombingGames.WurfelEngine.WE;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;

/**
 *A bullet is a moving object which can destroy and damage entities or the world.
 * @author Benedikt Vogler
 */
public class Bullet extends AbstractEntity {
    private static Sound explosionsound;
    private Vector3 dir;//movement
    private float speed;
    private int damage;
    private int distance =0;//distance traveled
    private MovableEntity parent;//no self shooting
    private int maxDistance = 1000;//default maxDistance
    private int explosive = 0;
    private int impactSprite;
    
    /**
	 * You can set a different sprite via {@link #setGraphicsId(int) }. It uses the engine default sprite.
     * @see #setGraphicsId(int) 
     */
    public Bullet(){
        super(12);//default graphics id is 12
    }

    /**
     *
     */
    public static void init(){
        if (explosionsound == null)
            explosionsound = WE.getAsset("com/BombingGames/WurfelEngine/Core/Sounds/explosion2.ogg");
    }
   
    @Override
    public void update(float dt) {
        //dir.z=-delta/(float)maxDistance;//fall down
        Vector3 dMov = dir.cpy().scl(dt*speed);
        //dMov.z /= 1.414213562f;//mixed screen and game space together?
        getPosition().addVector(dMov);
        setRotation(getRotation()+dt);
        
        //only spawn specific distance then destroy self
        distance += dMov.len();
        if (distance > maxDistance)
            dispose();
                
        //block hit -> spawn effect
        if (getPosition().onLoadedMapHorizontal() && getPosition().getBlockClamp().isObstacle()){
            if (impactSprite!= 0)
                new AnimatedEntity(impactSprite, 0, new int[]{1000} , true, false).spawn(getPosition().cpy());
            dispose();
        }
        
        //check character hit
         //get every character on this coordinate
        ArrayList<MovableEntity> entitylist;
        entitylist = Controller.getMap().getEntitysOnCoord(getPosition().getCoord(), MovableEntity.class);
        entitylist.remove(parent);//remove self from list to prevent self shooting
        if (!entitylist.isEmpty()) {
            entitylist.get(0).damage(damage);//damage only the first unit on the list
            new SimpleEntity(16).spawn(getPosition().cpy());//spawn blood
            dispose();
        }
    }

    /**
     *
     * @param dir
     */
    public void setDirection(Vector3 dir) {
        this.dir = dir;
    }
    
    /**
     *
     * @param speed
     */
    public void setSpeed(float speed){
        this.speed = speed;
    }

    /**
     *
     * @param parent
     */
    public void setParent(MovableEntity parent) {
        this.parent = parent;
    }
    
    /**
     *
     * @param maxDistance
     */
    public void setMaxDistance(int maxDistance){
        this.maxDistance = maxDistance;
    }

    /**
     *
     * @param damage
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    /**
     *
     * @param ex
     */
    public void setExplosive(int ex){
        explosive = ex;
    }
    
      /**
     * Spawns explosion.
     */
    private void explode(int radius){
       new Explosion(radius,WE.getGameplay().getView().getCameras().get(0)).spawn(getPosition());
    }

    @Override
    public void dispose() {
        if (explosive>0) explode(3);
        super.dispose();
    }

    /**
     * Set the sprite which get spawned when the bullet hits.
     * @param id  if you don't want an impact sprite set id to0.
     */
    public void setImpactSprite(int id) {
        impactSprite = id;
    }

    /**
     *
     * @return the distance traveled.
     */
    public int getDistance() {
        return distance;
    }
    
    
}