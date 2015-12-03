/*
 * If this software is used for a game the official „Wurfel Engine“ logo or its name must be visible in an intro screen or main menu.
 *
 * Copyright 2015 Benedikt Vogler.
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
package com.bombinggames.caveland.GameObjects;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Vector2;
import com.bombinggames.caveland.Game.Events;
import com.bombinggames.wurfelengine.core.Gameobjects.Block;
import com.bombinggames.wurfelengine.core.Gameobjects.MovableEntity;
import com.bombinggames.wurfelengine.core.Map.Point;

/**
 *
 * @author Benedikt Vogler
 */
public class IdleAI implements Telegraph {
	
	private final MovableEntity body;
	private Point home;
	private float timeTillMove;

	public IdleAI(MovableEntity body) {
		this.body = body;
	}
	
	void update(float dt) {
		if (home == null) {
			home = body.getPosition();
		}
		
		timeTillMove -= dt;
		
		if (timeTillMove < 0) {
			timeTillMove = 1500;
			
			if (body.getPosition().distanceTo(home) > Block.GAME_EDGELENGTH * 4) //if too far away
			{
				MessageManager.getInstance().dispatchMessage(
					0,
					this,
					body,
					Events.moveTo.getId(),
					home
				);
			} else {
				MessageManager.getInstance().dispatchMessage(
					0,
					this,
					body,
					Events.moveTo.getId(),
					home.cpy().add(
						new Vector2(
							(float) (Math.random() - 0.5f),
							(float) (Math.random() - 0.5f)
						).nor().scl(Block.GAME_EDGELENGTH * 2)
					)
				);
			}
		}
		
	}
	
	@Override
	public boolean handleMessage(Telegram msg) {
		return false;
	}
	
}
