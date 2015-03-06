package com.BombingGames.Caveland.GameObjects;

import com.BombingGames.WurfelEngine.Core.Camera;
import com.BombingGames.WurfelEngine.Core.GameView;
import com.BombingGames.WurfelEngine.Core.Gameobjects.MovableEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Benedikt Vogler
 */
public class Inventory implements Serializable {
	private static final long serialVersionUID = 1L;
	private Slot[] slot = new Slot[3];
	private static final boolean enableStacking = false;
	private CustomPlayer player;

	public Inventory(CustomPlayer player) {
		slot[0] = new Slot();
		slot[1] = new Slot();
		slot[2] = new Slot();
		this.player = player;
	}
	
	/**
	 * reduces the counter and deletes the object from inventory
	 * @return the frontmost element. can return null if empty.
	 * @throws java.lang.CloneNotSupportedException
	 */
	public Collectible getFrontItem() throws CloneNotSupportedException {
		Collectible result = null;
		if (slot[0].counter>0){
			result = slot[0].take();
		} else if (slot[1].counter>0){
			result = slot[1].take();
		}else if (slot[2].counter>0){
			result = slot[2].take();
		}
		
		if (result==null) return null;
		return result;
	}
	
	/**
	 * 
	 * @param ent
	 * @return false if inventory is full
	 */
	public final boolean add(Collectible ent){
		if (enableStacking) {
			if (ent.isCollectable()) {
				if (slot[0].prototype != null && slot[0].prototype.getId() ==ent.getId() && slot[0].counter<10){
					slot[0].increase();
					return true;
				} else if (slot[1].prototype != null && slot[1].prototype.getId() ==ent.getId() && slot[1].counter<10){
					slot[1].increase();
					return true;
				} else if (slot[2].prototype != null && slot[2].prototype.getId() ==ent.getId() && slot[2].counter<10){
					slot[2].increase();
					return true;
				} else if (slot[2].prototype == null ) {
					slot[2].setPrototype(ent);
					return true;
				} else if (slot[1].prototype == null ) {
					slot[1].setPrototype(ent);
					return true;
				} else if (slot[0].prototype == null ) {
					slot[0].setPrototype(ent);
					return true;
				}
			}
		} else {
			if (slot[2].prototype == null ) {
				slot[2].setPrototype(ent);
				return true;
			} else if (slot[1].prototype == null ) {
				slot[1].setPrototype(ent);
				return true;
			} else if (slot[0].prototype == null ) {
				slot[0].setPrototype(ent);
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * put everything if in the inventory.
	 * @param list
	 * @return everything 
	 */
	public ArrayList<Collectible> addAll(ArrayList<Collectible> list) {
		if (list != null) {
		Iterator<Collectible> it = list.iterator();
			while (it.hasNext()) {
			  Collectible ent = it.next();
				if (add(ent))
					it.remove();
			}
		}
		return list;
	}
	
	/**
	 *Renders the inventory in the HUD.
	 * @param view
	 * @param camera
	 */
	public void render(GameView view, Camera camera){
		for (int i = 0; i < slot.length; i++) {
			MovableEntity ent = slot[i].prototype;
			if (ent!=null) {
				int x = (int) ((int) (view.getStage().getWidth()-400+i*100)/view.getEqualizationScale());
				int y = (int) ((view.getStage().getHeight()-camera.getScreenPosY()-camera.getHeightInScreenSpc()+10)/view.getEqualizationScale()); 
				ent.render(view, x, y);
				if (enableStacking)
					view.drawString(Integer.toString(slot[i].counter),  x+20, y+30,false);
			}
		}
	}
	
	/**
	 * Updates the items in the slots.
	 * @param dt 
	 */
	public void update(float dt){
		for (Slot currentSlot : slot) {
			if (currentSlot.prototype != null){
				currentSlot.prototype.update(dt);
				if (currentSlot.prototype.shouldBeDisposed()){
					currentSlot.prototype = null;
					currentSlot.counter = 0;
				}
			}
		}
	}
	
	/**
	 * Works only for three stacks.
	 * @param left true if left, false to right
	 */
	public void switchItems(boolean left){
		if (slot[0].counter>0) {//switch three items
			if (left){
				Slot tmp = slot[0];
				slot[0] = slot[1];
				slot[1] = slot[2];
				slot[2] = tmp;
			}else {
				Slot tmp = slot[1];
				slot[1] = slot[0];
				slot[0] = slot[2];
				slot[2] = tmp;
			}
		} else if (slot[1].counter>0){
			Slot tmp = slot[1];
			slot[1] = slot[2];
			slot[2] = tmp;
		}
	}

	/**
	 * the amount of stacks in the inventory
	 * @return 
	 */
	public int size() {
		return slot.length;
	}
	
	/**
	 * calls the action method for the first slot item.
	 */
	public void action(){
		try {
			//Get the first item and activate it. Then put it back.
			Collectible item = getFrontItem();
			if(item!=null) {
				item.action();
				add(item);
			}
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public boolean isEmpty(){
		boolean empty = true;
		for (Slot currentSlot : slot) {
			if (!currentSlot.isEmpty())
				empty=false;
		}
		return empty;
	}
	
	private class Slot implements Serializable {
		private int counter;
		private Collectible prototype;

		/**
		 * Takes one object from the slot
		 * @return 
		 */
		private Collectible take() throws CloneNotSupportedException {
			counter--;
			Collectible tmp;
			if (enableStacking) {
				tmp = prototype.clone();
			} else {
				tmp = prototype;
			}
			if (counter <= 0)
				prototype=null;
			tmp.setPosition(player.getPosition().cpy());//independent of player position now
			return tmp;
		}

		protected void increase(){
			counter++;
		}

		public void setPrototype(Collectible prototype) {
			prototype.setPosition(player.getPosition());
			this.prototype = prototype;
			counter=1;
		}

		private boolean isEmpty() {
			return counter<=0;
		}
	}

}
