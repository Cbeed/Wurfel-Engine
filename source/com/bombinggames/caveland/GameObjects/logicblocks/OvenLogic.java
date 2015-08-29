/*
 *
 * Copyright 2015 Benedikt Vogler.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * If this software is used for a game the official „Wurfel Engine“ logo or its name must be
 *   visible in an intro screen or main menu.
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
package com.bombinggames.caveland.GameObjects.logicblocks;

import com.badlogic.gdx.math.Vector3;
import com.bombinggames.caveland.Game.CustomGameView;
import com.bombinggames.caveland.GameObjects.Ejira;
import com.bombinggames.caveland.GameObjects.Interactable;
import com.bombinggames.caveland.GameObjects.SmokeEmitter;
import com.bombinggames.caveland.GameObjects.collectibles.Collectible;
import com.bombinggames.caveland.GameObjects.collectibles.CollectibleContainer;
import com.bombinggames.caveland.GameObjects.collectibles.CollectibleType;
import com.bombinggames.wurfelengine.core.Gameobjects.AbstractEntity;
import com.bombinggames.wurfelengine.core.Gameobjects.AbstractLogicBlock;
import com.bombinggames.wurfelengine.core.Gameobjects.Block;
import com.bombinggames.wurfelengine.core.Gameobjects.SimpleEntity;
import com.bombinggames.wurfelengine.core.Map.Coordinate;

/**
 * The manager of the logic of the oven block.
 * @author Benedikt Vogler
 */
public class OvenLogic extends AbstractLogicBlock implements Interactable{
	private static final long serialVersionUID = 1L;
	private final SmokeEmitter emitter;
	private final float PRODUCTIONTIME = 3000;
	private float productionCountDown;
	private float burntime;
	private final CollectibleContainer container = new CollectibleContainer((byte) 0);
	private final SimpleEntity fire = new SimpleEntity((byte) 17);

	/**
	 * 
	 * @param block
	 * @param coord 
	 */
	public OvenLogic(Block block, Coordinate coord) {
		super(block, coord);
		emitter = (SmokeEmitter) new SmokeEmitter().spawn(coord.toPoint());
		emitter.setHidden(true);
		emitter.setParticleStartMovement(Vector3.Z.cpy());
		emitter.setParticleTTL(1000);
		emitter.setActive(false);
		fire.setLightlevel(10);
	}
	
	@Override
	public void interact(CustomGameView view, AbstractEntity actor) {
		if (actor instanceof Ejira){
			//lege objekte aus Inventar  hier rein
			Collectible frontItem = ((Ejira) actor).getInventory().retrieveFrontItem();
			if (frontItem != null && (frontItem.getType()==CollectibleType.Coal || frontItem.getType()==CollectibleType.Ironore))
				addCollectible(frontItem);
		}
	}

	public void addCollectible(Collectible collectible) {
		if (collectible.getType() == CollectibleType.Coal) {
			burntime += 20000;//20s
		} else {
			container.addCollectible(collectible);
		}
	}
	
	@Override
	public void update(float dt) {
		if (burntime > 0) {//while the oven is burning
			emitter.setActive(true);
			fire.setHidden(false);
			emitter.setParticleSpread(new Vector3(1.2f, 1.2f, -0.1f));
			
			//burn ironore
			if (productionCountDown==0) {
				Collectible ironore = container.retrieveCollectibleReference(CollectibleType.Ironore);
				if (ironore != null) {
					ironore.dispose();
					productionCountDown = PRODUCTIONTIME;
				}
			}
			
			//check if production timer reaches zero
			float oldCountDown = productionCountDown;
			//decrease timer
			if (productionCountDown > 0) {
				productionCountDown -= dt;
				emitter.setParticleSpread(new Vector3(3f, 1f, -0.5f));
			}
			//clamp
			if (productionCountDown < 0)
				productionCountDown = 0;

			//if reached zero this frame
			if (oldCountDown > 0 && productionCountDown==0) {
				//produce
				( (Collectible) CollectibleType.Iron.createInstance().spawn(getPosition().toCoord().addVector(0, 0, 1).toPoint())).sparkle();
			}
		}
		//decrease timer
		if (burntime > 0)
			burntime -= dt;
		//clamp if reached bottom
		if (burntime < 0) {
			burntime = 0;
			fire.setHidden(true);
			emitter.setActive(false);
		}
	}
}