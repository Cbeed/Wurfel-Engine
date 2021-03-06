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
package com.bombinggames.caveland.gameobjects;

import com.badlogic.gdx.graphics.Color;
import com.bombinggames.wurfelengine.core.GameView;
import static com.bombinggames.wurfelengine.core.gameobjects.AbstractGameObject.getSprite;
import com.bombinggames.wurfelengine.core.gameobjects.Side;
import com.bombinggames.wurfelengine.core.map.Point;
import com.bombinggames.wurfelengine.core.map.rendering.GameSpaceSprite;
import com.bombinggames.wurfelengine.core.map.rendering.RenderCell;
import java.util.Random;

/**
 * Renders a grass block with wind on top.
 *
 * @author Benedikt Vogler
 */
public class GrassBlock extends RenderCell {

	private static final long serialVersionUID = 1L;
	public static final float WINDAMPLITUDE = 20f;
	private final static Random RANDOMGENERATOR = new java.util.Random();
	private static GameSpaceSprite grasSprite;
	private static float wind;
	private static float windWholeCircle;

	static void setMainForce(Point position) {
		posXForce = position.x;
		posYForce = position.y;
		posZForce = position.z;
	}

	public static void initGrass() {
		grasSprite = new GameSpaceSprite(getSprite('e', (byte) 7, (byte) 0));
		grasSprite.setOrigin(grasSprite.getWidth() / 2f, 0);
	}

	public static void updateWind(float dt) {
		windWholeCircle = (windWholeCircle + dt * 0.01f) % WINDAMPLITUDE;
		wind = Math.abs(windWholeCircle - WINDAMPLITUDE / 2)//value between 0 and amp/2
			- WINDAMPLITUDE / 2;//value between -amp/2 and + amp/2
	}
	private final float seed;

	//lokaler wind
	private static float posXForce;
	private static float posYForce;
	private static float posZForce;
	private static float force = 4000;
	private static float noisenum = 0.1f;

	public GrassBlock(byte id, byte value) {
		super(id, value);
		seed = RANDOMGENERATOR.nextFloat();
	}

	@Override
	public void renderSide(GameView view, Point pos, Side side, Color color) {
		super.renderSide(view, pos, side, color);
		if (false && side == Side.TOP && getCoord()!=null) {
			GameSpaceSprite gras = grasSprite;
			for (int i = 0; i < 10; i++) {
				//game space
				float xPos = pos.getX();
				float yPos = pos.getY();
				int xOffset = (int) (Math.abs((xPos - seed * 17) * i * (yPos)) % RenderCell.GAME_EDGELENGTH - RenderCell.GAME_EDGELENGTH2);
				int yOffset = (int) (Math.abs(((xPos - i) * 3 * (yPos * seed * 11 - i))) % RenderCell.GAME_EDGELENGTH - RenderCell.GAME_EDGELENGTH2);
				if (Math.abs(xOffset) + Math.abs(yOffset) < RenderCell.GAME_DIAGLENGTH2) {
					gras.setColor(
						getLightlevel(Side.TOP, (byte) 1, Channel.Red) / 2f,
						getLightlevel(Side.TOP, (byte) 1, Channel.Green) / 2f - (xOffset + i) % 7 * 0.005f,
						getLightlevel(Side.TOP, (byte) 1, Channel.Blue) / 2f,
						1
					);	
					gras.setPosition(
						xPos + xOffset,
						yPos + RenderCell.GAME_DIAGLENGTH2+yOffset,//there is something wrong with the rendering, so set center to the front
						pos.getZ()+RenderCell.GAME_EDGELENGTH
					);

					//wind
					float distanceToForceCenter = (xPos + xOffset - posXForce+100) * (xPos + xOffset - posXForce+100)
						+ (-yPos * 2 + yOffset - posYForce + 900) * (-yPos * 2 + yOffset - posYForce + 900);
					float forceRot;
					if (distanceToForceCenter > 200000) {
						forceRot = 0;
					} else {
						forceRot = 600000 / (distanceToForceCenter);
						if (posXForce < xPos) {
							forceRot *= -1;
						}
						if (forceRot > 90) {
							forceRot = 90;
						}
						if (forceRot < -90) {
							forceRot = -90;
						}
					}
					gras.setRotation(i * 0.4f - 10.2f + wind + RANDOMGENERATOR.nextFloat() * noisenum * WINDAMPLITUDE / 2+forceRot*0.3f);
					gras.draw(view.getSpriteBatchWorld());
				}
			}
		}
	}

}
