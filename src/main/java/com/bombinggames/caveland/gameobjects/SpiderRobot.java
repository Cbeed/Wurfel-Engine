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
package com.bombinggames.caveland.gameobjects;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.math.Vector3;
import com.bombinggames.caveland.game.CavelandBlocks;
import com.bombinggames.caveland.gameobjects.collectibles.Collectible;
import com.bombinggames.wurfelengine.WE;
import com.bombinggames.wurfelengine.core.Events;
import com.bombinggames.wurfelengine.core.gameobjects.MoveToAi;
import com.bombinggames.wurfelengine.core.map.Coordinate;
import com.bombinggames.wurfelengine.core.map.Point;
import com.bombinggames.wurfelengine.core.map.rendering.RenderCell;
import com.bombinggames.wurfelengine.extension.shooting.Laserdot;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A robot can carry one collectible. The scanner lets the robot look for minerals ({@link #enableScanner() }). 
 * @author Benedikt Vogler
 */
public class SpiderRobot extends Robot{
	
	private static final long serialVersionUID = 2L;
	
	private transient long walkingSound;
	private transient Laserdot laserdot;
	private transient boolean moveUp;
	private transient boolean moveRight;
	private transient float scanHeight;
	private transient float laserRotate;
	private transient Coordinate workingBlock;
	private transient Coordinate storage;
	private transient Collectible carry;

	/**
	 *
	 */
	public SpiderRobot() {
		setType(1);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		
		//stop soudn when time is freezed
		if (dt == 0 && walkingSound != 0) {
			WE.SOUND.stop("robot2walk", walkingSound);
			walkingSound = 0;
		}
		
		if (hasPosition()) {
			boolean hadNoBlock = false;
			if (workingBlock == null || workingBlock.getBlockId()== 0) {
				hadNoBlock = true;
			}
			updateSight(dt);
			//validate workingBlock
			if (workingBlock != null) {
				byte id = workingBlock.getBlockId();
				if (id == 0) {
					workingBlock = null;
				} else {
					if (!(id == CavelandBlocks.CLBlocks.COAL.getId()
						|| id == CavelandBlocks.CLBlocks.IRONORE.getId()
						|| id == CavelandBlocks.CLBlocks.CRYSTAL.getId()
						|| id == CavelandBlocks.CLBlocks.SULFUR.getId())
					) {
						workingBlock = null;
					}
				}
				//had no blok but now has
				if (hadNoBlock && workingBlock != null) {
					WE.SOUND.play("robotWeep", getPosition());
				}
			} else {
				enableScanner();
			}
			
			if (carry != null) {
				//move carry to storage
				carry.setPosition(getPosition().cpy());
				if (storage != null) {
					if (getComponents(MoveToAi.class) == null) {
						MessageManager.getInstance().dispatchMessage(
							this,
							this,
							Events.moveTo.getId(),
							storage.toPoint()
						);
					}

					//drop carry
					if (getPosition().toCoord().equals(storage)) {
						//verify flags existence
						LinkedList<DropSpaceFlag> flagsnearby = storage.getEntitiesInside(DropSpaceFlag.class);
						if (flagsnearby.isEmpty()) {
							storage = null;
						}
						//place them on storage
						if (storage != null) {
							carry.setHidden(false);
							carry.allowPickup();
							carry.setPosition(storage);
							carry = null;
							MessageManager.getInstance().dispatchMessage(
								this,
								this,
								Events.standStill.getId()
							);
						}
					}
				} else {
					enableScanner();
				}
			} else {
				//gather resources
				if (workingBlock != null) {
					//if nearby
					if (getPosition().distanceTo(workingBlock) < RenderCell.GAME_EDGELENGTH*1.8f) {
						MessageManager.getInstance().dispatchMessage(
							this,
							this,
							Events.standStill.getId()
						);
						if (performAttack()) {
							workingBlock.damage((byte) 1);
							WE.SOUND.play("impact", getPosition());
							int block = workingBlock.getBlock();
							if ((block & 255) != 0 && ((block >> 16) & 255) % 8 == 0) {
								carry = CavelandBlocks.getLoot((byte) (block & 255)).createInstance();
								carry.spawn(getPosition().cpy());
								//carry.setHidden(true);
								carry.preventPickup();
							}
						}
					} else {
						Point moveToBlock = workingBlock.toPoint();
						//if block is below move away
						if (workingBlock.getZ() < getPosition().toCoord().getZ()) {
							moveToBlock = workingBlock.cpy().add(1, 0, 0).toPoint();
						}
						MessageManager.getInstance().dispatchMessage(
							this,
							this,
							Events.moveTo.getId(),
							moveToBlock
						);
					}
				}
			}
			
			if (storage != null && workingBlock != null) {
				disableScanner();
			}

			//sound
			if (getMovementHor().len2() > 0 && isOnGround()) {
				if (walkingSound == 0l) {
					walkingSound = WE.SOUND.loop("robot2walk", getPosition());
				}
			} else {
				WE.SOUND.stop("robot2walk", walkingSound);
				walkingSound = 0;
			}
		}
	}
	
	protected ArrayList<Coordinate> nearbyResources(){
		ArrayList<Coordinate> coordList = new ArrayList<>(2);
		
		for (int x = -4; x < 4; x++) {
			for (int y = -4; y < 4; y++) {
				for (int z = -2; z < 2; z++) {
					Coordinate tmpCoord = getPosition().toCoord().add(x, y, z);
					byte id = tmpCoord.getBlockId();
					if ((id == CavelandBlocks.CLBlocks.COAL.getId()
						|| id == CavelandBlocks.CLBlocks.IRONORE.getId()
						|| id == CavelandBlocks.CLBlocks.CRYSTAL.getId()
						|| id == CavelandBlocks.CLBlocks.SULFUR.getId())
						&& getPosition().canSee(tmpCoord.toPoint(), 12)
					) {
						coordList.add(tmpCoord);
					}
				}
			}
		}
		return coordList;
	}

	@Override
	public Vector3 getAiming() {
		return new Vector3(getOrientation(), scanHeight).rotate(Vector3.Z, laserRotate*90f).nor();
	}

	@Override
	public void removeFromMap() {
		WE.SOUND.stop("robot2walk", walkingSound);
		walkingSound = 0;
		if (laserdot!=null) {
			laserdot.dispose();
		}
		if (carry != null) {
			carry.allowPickup();
		}
		super.removeFromMap();
	}
	
	/**
	 * The scanner lets the robot look for minerals. does nothing if already on
	 */
	public void enableScanner() {
		if (laserdot == null) {
			laserdot = (Laserdot) new Laserdot().spawn(getPosition().cpy());
			laserdot.setColor(COLORTEAM.cpy());
		}
	}

	/**
	 * 
	 */
	public void disableScanner() {
		if (laserdot != null) {
			laserdot.dispose();
			laserdot = null;
		}
	}

	/**
	 * updates the laser light sight related things
	 * @param dt 
	 */
	private void updateSight(float dt) {
		if (laserdot != null && laserdot.hasPosition()) {
			//look for resources
			if (moveUp) {
				scanHeight += dt / 300f;
			} else {
				scanHeight -= dt / 300f;
			}

			if (scanHeight >= 1) {
				moveUp = false;
				scanHeight = 1;
			}

			if (scanHeight <= -0.3f) {
				moveUp = true;
				scanHeight = -0.3f;
			}

			if (moveRight) {
				laserRotate += dt / 500f;
			} else {
				laserRotate -= dt / 500f;
			}

			if (laserRotate >= 1) {
				moveRight = false;
				laserRotate = 1;
			}

			if (laserRotate <= -1) {
				moveRight = true;
				laserRotate = -1;
			}

			laserdot.update(getPosition().cpy().add(getOrientation().scl(20f)).add(0, 0, 20), getAiming()
			);

			//find storage
			if (storage == null) {
				LinkedList<DropSpaceFlag> flagsnearby = laserdot.getPosition().getEntitiesNearby(RenderCell.GAME_EDGELENGTH, DropSpaceFlag.class);
				if (!flagsnearby.isEmpty()) {
					storage = flagsnearby.getFirst().getPosition().toCoord();
					WE.SOUND.play("robotWeep", getPosition());
				}
			}

			//find block to work on
			byte id = laserdot.getPosition().getBlockId();
			if (id != 0) {
				if (id == CavelandBlocks.CLBlocks.COAL.getId()
					|| id == CavelandBlocks.CLBlocks.IRONORE.getId()
					|| id == CavelandBlocks.CLBlocks.CRYSTAL.getId()
					|| id == CavelandBlocks.CLBlocks.SULFUR.getId()
				) {
					if (workingBlock == null || workingBlock.getBlockId()== 0) {
						workingBlock = laserdot.getPosition().toCoord();
						//go to resources
						MessageManager.getInstance().dispatchMessage(
							this,
							this,
							Events.moveTo.getId(),
							laserdot.getPosition().cpy()
						);
					}
				}
			}
		}
	}
}
