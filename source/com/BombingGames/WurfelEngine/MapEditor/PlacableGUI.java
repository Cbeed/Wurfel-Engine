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
package com.BombingGames.WurfelEngine.MapEditor;

import com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractEntity;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Block;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Selection;
import com.BombingGames.WurfelEngine.Core.Map.Coordinate;
import com.BombingGames.WurfelEngine.WE;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shows the current "color"(block) selection in the editor.
 * @author Benedikt Vogler
 */
public class PlacableGUI extends WidgetGroup {
	private int id =1;
	private int value=0;
	private Image image;
	private Label label;
	private Label blockPosition;
	private Class<? extends AbstractEntity> entityClass;
	private PlaceMode mode = PlaceMode.Blocks;
	private final Slider slider;
	/** parent stage*/
	private Stage stage;

/**
 * 
 * @param stage parent stage
 * @param selection the selection-Entity where the color comes from
 * @param left left mouse button tool?
 */
	public PlacableGUI(Stage stage, Selection selection, boolean left) {
		this.stage = stage;
		
		if (left)
			setPosition(200, stage.getHeight()-300);
		else
			setPosition(stage.getWidth()-200, stage.getHeight()-300);
		
		image = new Image(new BlockDrawable(id,value,-0.4f));
		image.setPosition(50, 60);
		addActor(image);
		slider = new Slider(0, 10, 1, false, WE.getEngineView().getSkin());
		slider.setPosition(0, 20);
		slider.addListener(new ChangeListenerImpl(this));
		addActor(slider);
		
		label = new Label(Integer.toString(id) + " - "+ Integer.toString(value), WE.getEngineView().getSkin());
		addActor(label);
		
		blockPosition = new Label(selection.getPosition().getCoord().toString(), WE.getEngineView().getSkin());
		blockPosition.setPosition(50, 0);
		addActor(blockPosition);
	}
	
	public void update(Selection selection){
		blockPosition.setText(selection.getPosition().getCoord().toString());
	}

	public int getId() {
		return id;
	}
		
	public int getValue() {
		return value;
	}	
	
	
	public void setBlock(int id, int value) {
		this.id = id;
		this.value = value;
		label.setText(Integer.toString(id) + " - "+ Integer.toString(value));
		image.setDrawable(new BlockDrawable(id,value,-0.4f));
	}

	public void setId(int id) {
		this.id = id;
		label.setText(Integer.toString(id) + " - "+ Integer.toString(value));
		image.setDrawable(new BlockDrawable(id,value,-0.4f));
	}

	public void setValue(int value) {
		this.value = value;
		label.setText(Integer.toString(id) + " - "+ Integer.toString(value));
		image.setDrawable(new BlockDrawable(id,value,-0.4f));
	}
	
	/**
	 * Get a new instance of a selected block.
	 * @param coord the position of the block instance
	 * @return a new Block instance of the selected id and value.
	 */
	public Block getBlock(Coordinate coord){
		Block block = Block.getInstance(id, value);
		block.setPosition(coord);
		return block;
	}
	
	/**
	 * Trys returning a new instance of a selected entity class.
	 * @return if it fails returns null 
	 */
	public AbstractEntity getEntity(){
		if (entityClass == null) return null;
		try {
			return entityClass.newInstance();
		} catch (InstantiationException | IllegalAccessException ex) {
			Logger.getLogger(PlacableGUI.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void setEntity(String name, Class<? extends AbstractEntity> entclass) {
		entityClass = entclass;
		label.setText(name);
		image.setDrawable(new EntityDrawable(entclass));
	}

	public void setMode(PlaceMode mode) {
		this.mode = mode;
		if (mode==PlaceMode.Blocks)
			slider.setVisible(true);
		else slider.setVisible(false);
	}
	
	public PlaceMode getMode() {
		return mode;
	}

	/**
	 * Relative movement.
	 * @param amount 
	 */
	void moveToCenter(float amount) {
		if (getX() < stage.getWidth()/2)
			setX(getX()+amount);
		else
			setX(getX()-amount);
	}

	/**
	 * Absolute position.
	 * @param amount 
	 */
	void moveToBorder(float amount) {
		if (getX() < stage.getWidth()/2)
			setX(amount);
		else
			setX(stage.getWidth()-amount);
	}

	private static class ChangeListenerImpl extends ChangeListener {
		private PlacableGUI parent;

		ChangeListenerImpl(PlacableGUI parent) {
			this.parent = parent;
		}

		@Override
		public void changed(ChangeListener.ChangeEvent event, Actor actor) {
			parent.setValue((int) ((Slider)actor).getValue());
		}
	}

}
