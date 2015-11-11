package com.bombinggames.caveland.GameObjects;

import com.bombinggames.wurfelengine.core.Gameobjects.AbstractEntity;
import com.bombinggames.wurfelengine.core.Gameobjects.MovableEntity;
import com.bombinggames.wurfelengine.core.Map.Chunk;
import com.bombinggames.wurfelengine.core.Map.Coordinate;
import com.bombinggames.wurfelengine.extension.AimBand;

/**
 * Teleports every object in this cell.
 *
 * @author Benedikt Vogler
 */
public class Portal extends AbstractEntity {

	private static final long serialVersionUID = 2L;
	private Coordinate target = new Coordinate(0, 0, Chunk.getBlocksZ() - 1);
	/**
	 * indicates whether the portal is open or not
	 */
	private transient boolean active = true;
	private transient AimBand particleBand;

	/**
	 * teleports to 0 0 Chunk.getBlocksZ()-1 by default
	 */
	public Portal() {
		super((byte) 0);
		setIndestructible(true);
		setName("Portal");
	}

	/**
	 * teleports to 0 0 Chunk.getBlocksZ()-1 by default
	 *
	 * @param id
	 */
	public Portal(byte id) {
		super(id);
		setIndestructible(true);
		setName("Portal");
	}

	/**
	 * copy safe
	 *
	 * @return
	 */
	public Coordinate getTarget() {
		if (target == null) {
			return null;
		} else {
			return target.cpy();
		}
	}

	/**
	 * Set the coordiante where the portal teleports to.
	 *
	 * @param target
	 */
	public void setTarget(Coordinate target) {
		this.target = target;
	}

	@Override
	public void update(float dt) {
		if (hasPosition() && active && getTarget() != null) {
			if (particleBand != null) {
				particleBand.update();
			}

			//move things in the portal
			getPosition().toCoord().getEntitiesInside(MovableEntity.class)
				.forEach((AbstractEntity e) -> {
					if (e.getPosition().getZ() <= getPosition().toPoint().getZ() + 10//must be in the first part of the block
						&& e != this //don't teleport itself
						&& ((MovableEntity) e).isColiding()//only teleport things which collide
						) {
						e.setPosition(getTarget());
					}
				}
				);
		}
	}

	/**
	 * Set the portal to "open" or "closed"
	 *
	 * @param b
	 */
	public void setActive(boolean b) {
		active = b;
	}

	/**
	 * indicates whether the portal is open or not
	 *
	 * @return
	 */
	public boolean isActive() {
		return active;
	}

	@Override
	public void onSelectInEditor() {
		if (target != null) {
			if (particleBand == null) {
				particleBand = new AimBand(this, target);
			} else {
				particleBand.setGoal(target);
			}
		}
	}

	@Override
	public void onUnSelectInEditor() {
		if (particleBand != null) {
			particleBand.dispose();
			particleBand = null;
		}
	}
}
