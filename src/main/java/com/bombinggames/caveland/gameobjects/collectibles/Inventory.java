package com.bombinggames.caveland.gameobjects.collectibles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.bombinggames.caveland.game.CLGameView;
import com.bombinggames.caveland.gameobjects.Ejira;
import com.bombinggames.caveland.gameobjects.Interactable;
import com.bombinggames.wurfelengine.core.Camera;
import com.bombinggames.wurfelengine.core.GameView;
import com.bombinggames.wurfelengine.core.gameobjects.AbstractEntity;
import com.bombinggames.wurfelengine.core.gameobjects.AbstractGameObject;

/**
 * The inventory is a special limited collectible container. It also moves the
 * content at the players position (kinda like in its backpack).
 *
 * @author Benedikt Vogler
 */
public class Inventory extends CollectibleContainer {

	private static final long serialVersionUID = 3L;

	/**
	 *
	 * an invnetory needs a player where it is attached to
	 *
	 * @param player
	 */
	public Inventory(Ejira player) {
		super(player);
		setName("Inventory");
		setHidden(true);
	}

	/**
	 * Spawns the inventory at the owner.
	 *
	 * @return
	 */
	public AbstractEntity spawn() {
		getOwner().ifPresent(t -> super.spawn(((Ejira)t).getPosition()));
		return this;
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		if (hasPosition() && getOwner().isPresent() && ((Ejira)getOwner().get()).hasPosition())
			getPosition().set(((Ejira)getOwner().get()).getPosition());
	}

	/**
	 * Get a reference to the prototype and keeps the item in inventory.
	 *
	 * @param def
	 * @return can return null
	 */
	@Override
	public Collectible getCollectible(CollectibleType def) {
		if (get(2) != null && get(2).getType() == def) {
			return get(2);
		} else if (get(1) != null && get(1).getType() == def) {
			return get(1);
		} else if (get(0) != null && get(0).getType() == def) {
			return get(0);
		}
		return null;
	}

	/**
	 * Does not alter the invetory.
	 *
	 * @return Only reference. Can return null.
	 */
	public Collectible getFrontCollectible() {
		if (get(0) != null) {
			return get(0);
		} else if (get(1) != null) {
			return get(1);
		} else if (get(2) != null) {
			return get(2);
		}
		return null;
	}

	/**
	 * Add item at the back.
	 *
	 * @param col the item you add
	 * @return false if inventory is full. True if sucessfull.
	 */
	@Override
	public final boolean add(Collectible col) {
		if (size() < 3) {
			super.add(col);
			return true;
		}
		return false;
	}

	/**
	 * Add item at the front.
	 *
	 * @param col
	 * @return false if inventory is full. True if sucessfull.
	 */
	@Override
	public final boolean addFront(Collectible col) {
		if (col == null) {
			return false;
		}
		if (size() < 3) {
			super.addFront(col);
			return true;
		}
		return false;
	}

	/**
	 * Get a copy of the content. Does not change anything
	 *
	 * @return can have null in array
	 */
	public Collectible[] getContentAsArray() {
		return new Collectible[]{get(0), get(1), get(2)};
	}

	/**
	 * Get type definitions for every inventory slot. Does not alter anything.
	 *
	 * @return can have null inside array
	 */
	public CollectibleType[] getContentDef() {
		return new CollectibleType[]{
			(get(0) == null ? null : get(0).getType()),
			(get(1) == null ? null : get(1).getType()),
			(get(2) == null ? null : get(2).getType())};
	}
	
	/**
	 * check if the inventory contains a type of this item
	 *
	 * @param ingredient
	 * @return the amount the item is contained
	 */
	public int contains(CollectibleType ingredient) {
		int count = 0;
		if (getContentDef()[0] != null && getContentDef()[0] == ingredient) {
			count++;
		}
		if (getContentDef()[1] != null && getContentDef()[1] == ingredient) {
			count++;
		}
		if (getContentDef()[2] != null && getContentDef()[2] == ingredient) {
			count++;
		}
		return count;
	}

	/**
	 *
	 * @param view
	 * @param camera
	 */
	public void drawHUD(GameView view, Camera camera){
		//draw background for highlit sprite
		Sprite bgSprite = new Sprite(AbstractGameObject.getSprite('i', (byte) 10, (byte) 0));

		float left = (camera.getScreenPosX() + camera.getWidthScreenSpc() * 0.75f)+ bgSprite.getWidth() / 2;
		int y = (int) ((view.getStage().getHeight() - camera.getScreenPosY() - camera.getHeightScreenSpc() + 10));

		float leftbgSprite = (camera.getScreenPosX() + camera.getWidthScreenSpc() * 0.75f);
		// / view.getEqualizationScale()
		bgSprite.setPosition(leftbgSprite, y);
		bgSprite.draw(view.getSpriteBatchProjection());
		bgSprite.setX(leftbgSprite + 80);
		bgSprite.setScale(0.5f);
		bgSprite.setY(bgSprite.getY() - 20);
		bgSprite.draw(view.getSpriteBatchProjection());
		bgSprite.setX(leftbgSprite + 140);
		bgSprite.draw(view.getSpriteBatchProjection());

		Collectible ent = get(0);
		if (ent != null) {
			ent.render(view, (int) leftbgSprite, y);
			if (ent instanceof Interactable) {
				Sprite button = new Sprite(AbstractGameObject.getSprite('i', (byte) 23, Interactable.YUp));
				button.setPosition(left-90, y-30);
				button.setScale(0.4f);
				button.draw(view.getSpriteBatchProjection());
			}
		}
		ent = get(1);
		if (ent != null) {
			ent.setScaling(0.4f);
			ent.render(view, (int) (left + 80), y);
			ent.setScaling(1);
		}
		ent = get(2);
		if (ent != null) {
			ent.setScaling(0.4f);
			ent.render(view, (int) (left + 140), y);
			ent.setScaling(1);
		}
		
//		bgSprite.setX(leftbgSprite + 80);
//		bgSprite.setScale(0.5f);
//		bgSprite.setY(bgSprite.getY() - 20);
//		bgSprite.draw(view.getSpriteBatch());
//		bgSprite.setX(leftbgSprite + 140);
//		bgSprite.draw(view.getSpriteBatch());
		
//		for (int i = 0; i < size(); i++) {
//			MovableEntity ent = get(i);
//			if (ent != null) {
//				int x = (int) ((int) left + i * inventoryPadding / view.getEqualizationScale());
//				if (i != 0) {
//					ent.setScaling(-0.4f);
//				}
//				ent.render(view, x, y);
//				if (i != 0) {
//					ent.setScaling(0);
//				}
//			}
//		}
	}

	/**
	 * calls the action method for the first slot item.
	 *
	 * @param view
	 * @param actor
	 */
	public void action(CLGameView view, AbstractEntity actor) {
		//Get the first item and activate it. Then put it back.
		Collectible item = retrieveCollectible(0);
		if (item != null && item instanceof Interactable && ((Interactable) item).interactable()) {
			//spawn it
			item.setFloating(false);
			item.allowPickup();
			item.setHidden(false);
			((Interactable) item).interact(view, actor);
		}
		if (item!= null && !item.shouldBeDisposed()) {
			//turn back if not destroyed
			addFront(item);
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
}
