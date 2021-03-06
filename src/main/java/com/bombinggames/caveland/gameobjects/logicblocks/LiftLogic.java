package com.bombinggames.caveland.gameobjects.logicblocks;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.bombinggames.caveland.game.CLGameView;
import com.bombinggames.caveland.game.CavelandBlocks;
import com.bombinggames.caveland.gameobjects.Interactable;
import com.bombinggames.caveland.gameobjects.LiftBasket;
import com.bombinggames.caveland.gameobjects.Portal;
import com.bombinggames.wurfelengine.core.gameobjects.AbstractEntity;
import com.bombinggames.wurfelengine.core.gameobjects.MovableEntity;
import com.bombinggames.wurfelengine.core.map.AbstractBlockLogicExtension;
import com.bombinggames.wurfelengine.core.map.Coordinate;
import com.bombinggames.wurfelengine.core.map.rendering.RenderCell;

/**
 *
 * @author Benedikt Vogler
 */
public class LiftLogic extends AbstractBlockLogicExtension implements Interactable, Telegraph {

	private transient LiftBasket basket;

	/**
	 *
	 * @param dt
	 */
	@Override
	public void update(float dt) {
		//teleport minecarts
		Portal portal = getPortal();
		if (portal != null) {
			portal.setActive(false);
		}

		//spawn lift only if a lift is built
		if (portal != null) {
			Coordinate ground = portal.getExitPortal().getGround();
			if (basket == null) {
				basket = new LiftBasket();
			}
			if (!basket.hasPosition()) {
				basket.spawn(getPosition().toPoint());
			}

			if (ground.getBlockId() != CavelandBlocks.CLBlocks.LIFT_Ground.getId()) {
				ground.setBlock(CavelandBlocks.CLBlocks.LIFT_Ground.getId());
			}
			
			
		}
	}

	@Override
	public void interact(CLGameView view, AbstractEntity actor) {
		if (basket != null) {
			//start
			if (basket.getMovementDir() == 0 && getPosition().distanceToHorizontal(basket) < RenderCell.GAME_EDGELENGTH) {
				basket.setMovementDir(-1);

				if (actor instanceof MovableEntity) {
					basket.setPassenger((MovableEntity) actor);
				}
			} else {
				//get back up
				if (basket.getMovementDir() <= 0) {
					basket.setMovementDir(1);
				}
			}
		}
	}

	@Override
	public boolean interactable() {
		return true;
	}

	@Override
	public void dispose() {
		if (basket != null) {
			basket.dispose();
		}
		getPosition().getEntitiesNearbyHorizontal(RenderCell.GAME_EDGELENGTH2, LiftBasket.class).forEach(e -> e.dispose());
	}

	@Override
	public boolean interactableOnlyWithPickup() {
		return false;
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		return false;
	}

	/**
	 * get portal which leads inside
	 * @return 
	 */
	private Portal getPortal() {
		AbstractBlockLogicExtension holeLogic = getPosition().toCoord().add(0, 0, -1).getLogic();
		if (holeLogic != null && holeLogic instanceof CaveEntryBlockLogic) {
			return ((CaveEntryBlockLogic) holeLogic).getPortal();
		}
		return null;
	}
}
