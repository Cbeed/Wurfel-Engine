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
package com.BombingGames.WurfelEngine.Core.Map;

import com.BombingGames.WurfelEngine.Core.Controller;
import static com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractGameObject.GAME_EDGELENGTH;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Block;
import com.BombingGames.WurfelEngine.Core.View;
import com.badlogic.gdx.math.Vector3;
import java.io.Serializable;

/**
 *A
 * @author Benedikt Vogler
 */
public abstract class AbstractPosition implements Serializable {
    private final int referenceX;//top left chunk x coordinate
    private final int referenceY;//top left chunk Y coordinate
    private float height;

    /**
     *
     */
    public static final float SQRT2 = 1.4142135623730950488016887242096980785696718753769480f;

    /**
     *
     */
    public static final float SQRT12 = 0.7071067811865475244008443621048490392848359376884740f;
    /**
     * With custom reference
     * @param topleftX the chunk's X coordinate of the chunk at the top left
     * @param topleftY the chunk's Y coordinate of the chunk at the top left 
     */
    public AbstractPosition(final int topleftX, final int topleftY) {
        this.referenceX = topleftX;
        this.referenceY = topleftY;
    }

    
    /**
     * With the currently loaded top left chunk.
     */
    public AbstractPosition() {
        referenceX = Controller.getMap().getChunkCoords(0)[0];
        referenceY = Controller.getMap().getChunkCoords(0)[1];
    }

    /**
     * Geht the height (z-value) of the coordinate (game dimension).
     * @return
     */
    public float getHeight() {
        return height;
    }

    /**
     * 
     * @param height 
     */
    public void setHeight(float height) {
        this.height = height;
    }
    
    /**
     *The z coordinate is absolute even when used as relative coordinate because there are no chunks in Z direction.
     * @return game coordinate
     */
    public int getZ(){
        float tmp = height / GAME_EDGELENGTH;
        if (tmp<0)
            return -1;
        else
            return (int) tmp;
    }
    
   /**
    * 
    * @return 
    */
    protected int getReferenceX() {
        return referenceX;
    }

    /**
     * 
     * @return 
     */
    protected int getReferenceY() {
        return referenceY;
    }
    
     /**
     * Calculate position in view space.
	 * @param View
     * @return Returns the center of the projected (screen) x-position where the object is rendered without regarding the camera. It also adds the cell offset.
     */
    public abstract int getViewSpcX(View View);
    
    /**
     * Calculate position in view space.
	 * @param View
     * @return Returns the center of the projected (screen) y-position where the object is rendered without regarding the camera. It also adds the cell offset.
     */
    public abstract int getViewSpcY(View View);
    
    /**
     * If needed calculates it and creates new instance else return itself.
     * @return the point representation
     */
    public abstract Point getPoint();
    
    /**
     * If needed calculates it and creates new instance else return itself.
     * @return the coordinate representation
     */
    public abstract Coordinate getCoord();
    
    /**
     * 
     * @return Get the block at the position. If the coordiante is outside the map crash. Faster than "getBlockSafe()"
     * @see #getBlockSafe() 
     */
    public abstract Block getBlock();
    
    /**
     * Slower than getBlock().
     * @return Get the block at the position. If the coordiante is outside the map return null. 
     *  @see #getBlock() 
     */
    public abstract Block getBlockSafe();

    /**
     *
     * @return a copy of the object.
     */
    public abstract AbstractPosition cpy(); 
    
    /**
     * Checks if the position is on the chunks currently in memory. Horizontal checks only.
     * @return 
     */
    public abstract boolean onLoadedMapHorizontal();
	
	/**
     * Checks if the position is on the chunks currently in memory. Chgecks all axis'.
     * @return 
     */
    public abstract boolean onLoadedMap();
	
	
    
    /**
     *
     * @param vector
     * @return returns itself
     */
    public abstract AbstractPosition addVector(float[] vector);
    
    /**
     *
     * @param vector
     * @return returns itself
     */
    public abstract AbstractPosition addVector(Vector3 vector);
    
    /**
     * Add a vector to the position. Implementation may differ.
     * @param x  Dependent on implementation.
     * @param y Dependent on implementation.
     * @param z Dependent on implementation.
     * @return returns itself
     */
    public abstract AbstractPosition addVector(float x, float y, float z);
    
    
        /**
     * Returns the depth of the object. The depth is an int value wich is needed for producing the list of the renderorder. The higher the value the later it will be drawn.
	 * @param view
     * @return the depth in game size
     */
    public abstract int getDepth(View view);
}
