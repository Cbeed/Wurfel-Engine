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
package com.BombingGames.WurfelEngine.Core.Map.Iterators;

import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Gameobjects.HasID;
import java.util.Iterator;

/**
 *An iterator iterating over a 3d array
 * @author Benedikt Vogler
 */
public class DataIterator implements Iterator<HasID>{
	private int x, y, z;
	private int relativeX;
	private int relativeY;
	private HasID[][][] data;
	private int limitZ;

	/**
	 * 
	 * @param data
	 * @param startingZ the starting layer
	 * @param limitZ  the last layer 
	 * @param relativeX the x coordinate of top left
	 * @param relativeY the y coordinate of top left
	 */
	public DataIterator(
		HasID[][][] data,
		final int startingZ,
		final int limitZ,
		final int relativeX,
		final int relativeY
	) {
		x=-1;//start at -1 because the first call of next should return the first element
		y=0;
		z=startingZ;
		this.limitZ=limitZ;
		this.data = data;
		this.relativeX = relativeX;
		this.relativeY = relativeY;
	}
	
	/**
	 * set the top/last limit of the iteration (including).
	 * @param zLimit 
	 */
	public void setTopLimitZ(int zLimit) {
		this.limitZ = zLimit;
	}

	@Override
	public boolean hasNext() {
		return (
			   x < data.length-1
			|| y < data[0].length-1
			|| z < limitZ
		);
	}

	@Override
	public HasID next() {
		if (x<data.length-1)
			x++;
		else if (y<data[0].length-1){
			y++;
			x=0;
		} else if (z<limitZ) {
			z++;
			y=0;
			x=0;
		}
		
		if (z<0){
			//current pos -1 in z
			return Controller.getMap().getGroundBlock();
		} else {
			return data[x][y][z];
		}
	}

	@Override
	public void remove() {
	}
	
	/**
	 * get the indices position of the iterator
	 * @return 
	 */
	public int[] getCurrentIndex(){
		return new int[]{x,y,z};
	}
	
}