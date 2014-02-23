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
package com.BombingGames.WurfelEngine.Core.Gameobjects;

import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Map.AbstractPosition;
import com.BombingGames.WurfelEngine.Core.Map.Coordinate;
import com.BombingGames.WurfelEngine.Core.Map.Map;
import com.BombingGames.WurfelEngine.Core.Map.Point;
import com.BombingGames.WurfelEngine.WE;
import com.BombingGames.WurfelEngine.shooting.Bullet;

/**
 *An entity is a game object wich is self aware that means it knows it's position.
 * @author Benedikt
 */
public abstract class AbstractEntity extends AbstractGameObject implements IsSelfAware {
       /**
     *
     */
    public static final char CATEGORY = 'e';
   
    /**Containts the names of the objects. index=id*/
    public static final String[] NAMELIST = new String[OBJECTTYPESCOUNT]; 
    
    /** A list containing the offset of the objects. */
    public static final int[][][] OFFSET = new int[OBJECTTYPESCOUNT][VALUESCOUNT][2];
    
    private Point point;//the position in the map-grid
   
    static {
        NAMELIST[40] = "player";
        OFFSET[40][0][0] = 54-80;
        OFFSET[40][0][1] = 37-40;
        OFFSET[40][1][0] = 55-80;
        OFFSET[40][1][1] = 38-40;
        OFFSET[40][2][0] = 53-80;
        OFFSET[40][2][1] = 35-40;
        OFFSET[40][3][0] = 46-80;
        OFFSET[40][3][1] = 33-40;
        OFFSET[40][4][0] = 53-80;
        OFFSET[40][4][1] = 35-40;
        OFFSET[40][5][0] = 64-80;
        OFFSET[40][5][1] = 33-40;
        OFFSET[40][6][0] = 53-80;
        OFFSET[40][6][1] = 33-40;
        OFFSET[40][7][0] = 46-80;
        OFFSET[40][7][1] = 33-40;
        NAMELIST[41] = "smoke test";
        NAMELIST[42] = "character shadow";
        OFFSET[42][0][0] = -80;
        OFFSET[42][0][1] = 40;
    }
    
    private boolean destroy;
   
    /**
     * Create an abstractEntity. You should use Block.getInstance(int) 
     * @param id 
     * @see com.BombingGames.Game.Gameobjects.Block#getInstance(int) 
     */
    protected AbstractEntity(int id){
        super(id,0);
    }
    
    /**
     * Create an entity through this factory method..
     * @param id the object id of the entity.
     * @param value The value at start.
     * @param point The coordiantes where you place it.
     * @return the entity.
     */
    public static AbstractEntity getInstance(int id, int value, Point point){
        AbstractEntity entity;
        //define the default SideSprites
        switch (id){
            case 12:
                entity = new Bullet(id);
            break;
            case 15:case 16: case 19:case 20:case 21:
                entity = new AnimatedEntity(id, value, new int[]{300}, true, false);
            break;         
            case 30:
                entity = new Player(id, point);
                break;
            case 31: //explosion
                entity = new AnimatedEntity(
                            id,
                            value,
                            new int[]{700,2000},
                            true,
                            false
                        );
                break;
            case 32:
                entity = new CharacterShadow(id);
                break;
             
            default:
                entity = WE.getCurrentConfig().getEntitiyFactory().produce(id, value, point); 
                break; 
        }
        
        entity.setPos(point);
        entity.setValue(value);
        return entity;
    }
    
    @Override
    public int getDepth(AbstractPosition pos){
        return (int) (
            pos.getPoint().getRelY()//Y
            
            + pos.getHeight()/Math.sqrt(2)//Z
            + (getDimensionZ() - 1) * GAME_EDGELENGTH/6/Math.sqrt(2)
        );
    }
    
    //IsSelfAware implementation
    @Override
    public Point getPos() {
        return point;
    }

    @Override
    public void setPos(AbstractPosition pos) {
        this.point = pos.getPoint();
    }
    
    /**
     * 
     * @param height 
     */
    public void setHeight(float height) {
        point.setHeight(height);
    }
    
  
    /**
     * Is the entity laying/standing on the ground?
     * @return true when on the ground
     */
    public boolean onGround(){
        if (getPos().getHeight() <= 0) return true; //if entity is under the map
        
        //check if one pixel deeper is on ground.
        int z = (int) ((getPos().getHeight()-1)/GAME_EDGELENGTH);
        if (z > Map.getBlocksZ()-1) z = Map.getBlocksZ()-1;
        
        return
            new Coordinate(
                point.getCoord().getRelX(),
                point.getCoord().getRelY(),
                z,
                true
            ).getBlock().isObstacle();
    }
    
    /**
     * add this entity to the map-> let it exist
     */
    public void exist(){
        Controller.getMap().getEntitys().add(this);
    }
  
    /**
     *
     * @return
     */
    @Override
    public char getCategory() {
        return CATEGORY;
    } 
    
    @Override
    public String getName() {
        return NAMELIST[getId()];
    }
    
    /**
     *The offset is the offset of the sprite image.
     * @return
     */
    @Override
    public int getOffsetX() {
        return OFFSET[getId()][getValue()][0];
    }

    /**
     *The offset is the offset of the sprite image.
     * @return
     */
    @Override
    public int getOffsetY() {
        return OFFSET[getId()][getValue()][1];
    } 
    
   /**
     * Deletes the object from the map. The opposite to exist();
     */
    public void destroy(){
        destroy=true;
    }

    /**
     *
     * @return
     */
    public boolean shouldBeDestroyed() {
        return destroy;
    }
}