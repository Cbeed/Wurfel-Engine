/*
 * If this software is used for a game the official „Wurfel Engine“ logo or its name must be visible in an intro screen or main menu.
 *
 * Copyright 2016 Benedikt Vogler.
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
package com.bombinggames.wurfelengine.extension;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bombinggames.wurfelengine.core.Controller;
import com.bombinggames.wurfelengine.core.GameView;
import com.bombinggames.wurfelengine.core.map.Chunk;
import com.bombinggames.wurfelengine.core.map.rendering.RenderChunk;
import java.util.LinkedList;

/**
 *
 * @author Benedikt Vogler
 */
public class MiniMapChunkDebug {
	private boolean visible = true;
	
	    /**
	 * distance from left
	 */
	private final int posX;
	/**
	 * distance from bottom
	 */
	private final int posY;

	public MiniMapChunkDebug(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
	}
	   
	
	/**
     * Renders the Minimap.
     * @param view the view using this render method 
     */
    public void render(final GameView view) {
        if (visible) {
			ShapeRenderer sh = view.getShapeRenderer();
			sh.begin(ShapeRenderer.ShapeType.Filled);
			sh.setColor(0, 1, 0, 1);
			Chunk[][] mapdata = Controller.getMap().getData();
            for (Chunk[] chunkX : mapdata) {
				for (Chunk chunkY : chunkX) {
					if (chunkY!=null)
						sh.rect(posX+chunkY.getChunkX()*20, posY-chunkY.getChunkY()*20, 19, 19);
				}
			}
			sh.setColor(1, 1, 0, 0.1f);
			LinkedList<RenderChunk> rS = view.getRenderStorage().getData();
			for (RenderChunk chunk : rS) {
				sh.rect(posX+chunk.getChunkX()*20, posY-chunk.getChunkY()*20, 19, 19);
			}
			sh.end();
			
//				//camera position
//				view.drawString(
//					camera.getViewSpaceX() +" | "+ camera.getViewSpaceY(),
//					posX,
//					(int) (posY- 3*Chunk.getBlocksY()*scaleY + 15),
//					Color.WHITE
//				);
//			}
        }
    }
	
}
