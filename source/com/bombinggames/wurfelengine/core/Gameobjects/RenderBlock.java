/*
 * Copyright 2013 Benedikt Vogler.
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
 * * Neither the name of Bombing Games nor Benedikt Vogler nor the names of its contributors 
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
package com.bombinggames.wurfelengine.core.Gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.bombinggames.wurfelengine.WE;
import com.bombinggames.wurfelengine.core.Camera;
import com.bombinggames.wurfelengine.core.Controller;
import com.bombinggames.wurfelengine.core.GameView;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_DEPTH;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_DEPTH2;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_DEPTH4;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_HEIGHT;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_HEIGHT2;
import static com.bombinggames.wurfelengine.core.Gameobjects.Block.VIEW_WIDTH2;
import com.bombinggames.wurfelengine.core.Map.AbstractPosition;
import com.bombinggames.wurfelengine.core.Map.Coordinate;
import com.bombinggames.wurfelengine.core.View;

/**
 * A RenderBlock is a wonderful piece of information and a geometrical object. It is something which can be rendered and therefore render information saved. A RenderBlock should not be shared across cameras. It is an extension to the coredata saved in the map. The core data is shared so changing this renderblock changes the data in the map.
 * @see Block
 * @author Benedikt Vogler
 */
public class RenderBlock extends AbstractGameObject {
    private static final long serialVersionUID = 1L;
	/**
	 * {id}{value}{side}
	 */
    private static AtlasRegion[][][] blocksprites = new AtlasRegion[Block.OBJECTTYPESNUM][Block.VALUESNUM][3];
	
	private static String destructionSound;
        
    /**
     * a list where a representing color of the block is stored
     */
    private static final Color[][] colorlist = new Color[Block.OBJECTTYPESNUM][Block.VALUESNUM];
	private static boolean fogEnabled;
	private static boolean staticShade;
	private static float ambientOcclusion;

	/**
	 * Indicate whether the blocks should get shaded independent of the light engine by default.
	 * @param shade 
	 */
	public static void setStaticShade(boolean shade){
		staticShade = shade;
	}
	
	    /**
     *  Returns a sprite sprite of a specific side of the block
     * @param id the id of the block
     * @param value the value of teh block
     * @param side Which side?
     * @return an sprite of the side
     */
    public static AtlasRegion getBlockSprite(final byte id, final byte value, final Side side) {
        if (getSpritesheet() == null) throw new NullPointerException("No spritesheet found.");
        
        if (blocksprites[id][value][side.getCode()] == null){ //load if not already loaded
            AtlasRegion sprite = getSpritesheet().findRegion('b'+Byte.toString(id)+"-"+value+"-"+side.getCode());
            if (sprite == null){ //if there is no sprite show the default "sprite not found sprite" for this category
                
                Gdx.app.debug("debug", 'b'+Byte.toString(id)+"-"+value +"-"+ side.getCode() +" not found");
                
                sprite = getSpritesheet().findRegion("b0-0-"+side.getCode());
                
                if (sprite == null) {//load generic error sprite if category sprite failed
                    sprite = getSpritesheet().findRegion("error");
                    if (sprite == null) throw new NullPointerException("Sprite and category error not found and even the generic error sprite could not be found. Something with the sprites is fucked up.");
                }
            }
            blocksprites[id][value][side.getCode()] = sprite;
            return sprite;
        } else {
            return blocksprites[id][value][side.getCode()];
        }
    }
	
	/**
	 * checks if a sprite is defined. if not the error sprite will be rendered
	 * @param block
	 * @return 
	 */
	public static boolean isSpriteDefined(final Block block){
		if (getSpritesheet() == null) return false;
		AtlasRegion sprite;
		if (block.hasSides())
			sprite = getSpritesheet().findRegion('b'+Byte.toString(block.getId())+"-"+block.getValue()+"-0");
		else
			sprite = getSpritesheet().findRegion('b'+Byte.toString(block.getId())+"-"+block.getValue());
		return 	sprite != null;
	}

    
   /**
     * Returns a color representing the block. Picks from the sprite sprite.
     * @param id id of the RenderBlock
     * @param value the value of the block.
     * @return copy of a color representing the block
     */
    public static Color getRepresentingColor(final byte id, final byte value){
        if (colorlist[id][value] == null){ //if not in list, add it to the list
            colorlist[id][value] = new Color();
            int colorInt;
            
            if (Block.getInstance(id, value).hasSides()){//if has sides, take top block    
                AtlasRegion texture = getBlockSprite(id, value, Side.TOP);
                if (texture == null) return new Color();
                colorInt = getPixmap().getPixel(
                    texture.getRegionX()+VIEW_DEPTH2, texture.getRegionY()+VIEW_DEPTH4);
            } else {
                AtlasRegion texture = getSprite('b', id, value);
                if (texture == null) return new Color();
                colorInt = getPixmap().getPixel(
                    texture.getRegionX()+VIEW_DEPTH2, texture.getRegionY()+VIEW_DEPTH2);
            }
            Color.rgba8888ToColor(colorlist[id][value], colorInt);
            return colorlist[id][value].cpy(); 
        } else return colorlist[id][value].cpy(); //return value when in list
    }
	
	/**
	 * set the sound to be played if a block gets destroyed.
	 * @param destructionSound 
	 */
	public static void setDestructionSound(String destructionSound) {
		RenderBlock.destructionSound = destructionSound;
	}
	
	/**
     *
     * @return
     */
    public static AtlasRegion[][][] getBlocksprites() {
        return blocksprites;
    }
	
    /**
     *dipsose the static fields
     */
    public static void staticDispose(){
        blocksprites = new AtlasRegion[Block.OBJECTTYPESNUM][Block.VALUESNUM][3];//{id}{value}{side}
    }
	
	
	private final Block blockData;
	private Coordinate coord;
	
	/**
	 * 
	 * @param id 
	 */
    public RenderBlock(byte id){
        super(id);
		blockData = Block.getInstance(id);
		fogEnabled = WE.CVARS.getValueB("enableFog");//refresh cache
		ambientOcclusion = WE.CVARS.getValueF("ambientOcclusion");
    }
	
	/**
	 * 
	 * @param id
	 * @param value 
	 */
	public RenderBlock(byte id, byte value){
		super(id);
		blockData = Block.getInstance(id, value);
		fogEnabled = WE.CVARS.getValueB("enableFog");//refresh cache
		ambientOcclusion = WE.CVARS.getValueF("ambientOcclusion");
	}
	
	/**
	 * Create a new render block referencing to an existing coreData object.
	 * @param data 
	 */
	public RenderBlock(Block data){
		super(data.getId());
		blockData = data;
		fogEnabled = WE.CVARS.getValueB("enableFog");//refresh cache
		ambientOcclusion = WE.CVARS.getValueF("ambientOcclusion");
	}
	
	
	@Override
	public boolean isObstacle() {
		return blockData.isObstacle();
	}

    @Override
    public String getName() {
        return  blockData.getName();
    }
	
	/**
	 * places the object on the map. You can extend this to get the coordinate. RenderBlock may be placed without this method call.
	 * @param coord the position on the map
	 * @return itself
	 */
	public RenderBlock spawn(Coordinate coord){
		setPosition(coord);
		Controller.getMap().setBlock(this);
		return this;
	};
    
    @Override
    public void render(final GameView view, final Camera camera) {
        if (!isHidden()) {
            if (hasSides()) {
				Coordinate coords = getPosition();
				byte clipping = blockData.getClipping();
                if ((clipping & (1 << 1)) == 0)
                    renderSide(view, camera, coords, Side.TOP, staticShade);
				if ((clipping & 1) == 0)
                    renderSide(view, camera, coords, Side.LEFT, staticShade);
                if ((clipping & (1 << 2)) == 0)
                    renderSide(view, camera, coords, Side.RIGHT, staticShade);
            } else
                super.render(view, camera);
        }
    }
    
    /**
     * Render the whole block at a custom position. Checks if hidden.
     * @param view the view using this render method
     * @param xPos rendering position (screen)
     * @param yPos rendering position (screen)
     */
    @Override
    public void render(final GameView view, final int xPos, final int yPos) {
        if (!isHidden()) {
            if (hasSides()) {
				renderSide(view, xPos, yPos+(VIEW_HEIGHT+VIEW_DEPTH), Side.TOP);
				renderSide(view, xPos, yPos, Side.LEFT);
				renderSide(view, xPos+VIEW_WIDTH2, yPos, Side.RIGHT);
			} else {
				super.render(view, xPos, yPos);
			}
        }
    }

    /**
     * Renders the whole block at a custom position.
     * @param view the view using this render method
     * @param xPos rendering position of the center
     * @param yPos rendering position of the center
     * @param color when the block has sides its sides gets shaded using this color.
     * @param staticShade makes one side brighter, opposite side darker
     */
    public void render(final GameView view, final int xPos, final int yPos, Color color, final boolean staticShade) {
        if (!isHidden()) {
            if (hasSides()) {
				renderSide(
					view,
					(int) (xPos-VIEW_WIDTH2*(1+getScaling())),
					(int) (yPos+VIEW_HEIGHT*(1+getScaling())),
					Side.TOP,
					color
				);

				if (staticShade) {
					if (color==null)
						color = new Color(0.75f, 0.75f, 0.75f, 1);
					else
						color = color.cpy().add(0.25f, 0.25f, 0.25f, 0);
				}
				renderSide(
					view,
					(int) (xPos-VIEW_WIDTH2*(1+getScaling())),
					yPos,
					Side.LEFT,
					color
				);

				if (staticShade) {
					color = color.cpy().sub(0.25f, 0.25f, 0.25f, 0);
				}
				renderSide(
					view,
					xPos,
					yPos,
					Side.RIGHT,
					color
				);
            } else
                super.render(view, xPos, yPos+VIEW_DEPTH4, color);
        }
    }
       
	/**
     * Render a side of a block at the position of the coordinates.
     * @param view the view using this render method
	 * @param camera
     * @param coords the coordinates where to render 
     * @param side The number identifying the side. 0=left, 1=top, 2=right
	 * @param staticShade
     */
    public void renderSide(
		final GameView view,
		final Camera camera,
		final AbstractPosition coords,
		final Side side,
		final boolean staticShade
	){
		Color color;
		if (fogEnabled) {
			//can use CVars for dynamic change. using harcored values for performance reasons
			float factor = (float) (Math.exp( 0.025f*(camera.getVisibleFrontBorderHigh()-coords.toCoord().getY()-18.0) )-1 );
			//float factor = (float) (Math.exp( 0.0005f*(coords.getDepth(view)-500) )-1 );
			color = new Color(0.5f+0.3f*factor, 0.5f+0.4f*factor, 0.5f+1f*factor, 1);
		} else
			color = Color.GRAY.cpy();
		
		//if vertex shaded then use different shading for each side
		if (Controller.getLightEngine() != null && !Controller.getLightEngine().isShadingPixelBased()) {
			color = Controller.getLightEngine().getColor(side).mul(color.r+0.5f, color.g+0.5f, color.b+0.5f, color.a+0.5f);
        }
		
        renderSide(
			view,
            coords.getViewSpcX(view) - VIEW_WIDTH2 + ( side == Side.RIGHT ? (int) (VIEW_WIDTH2*(1+getScaling())) : 0),//right side is  half a block more to the right,
            coords.getViewSpcY(view) - VIEW_HEIGHT2 + ( side == Side.TOP ? (int) (VIEW_HEIGHT*(1+getScaling())) : 0),//the top is drawn a quarter blocks higher,
            side,
            staticShade ?
				side == Side.RIGHT
				? color.sub(0.25f, 0.25f, 0.25f, 0)
				: (
					side == Side.LEFT
						? color.add(0.25f, 0.25f, 0.25f, 0)
						: color
					)
				: color//pass color if not shading static
        );
		//render ambient occlusion
		if (ambientOcclusion>0) {
			int aoFlags = getBlockData().getAOFlags();
			if (side==Side.LEFT && ((byte) (aoFlags)) != 0){//only if top side and there is ambient occlusion
				Coordinate aopos = getPosition().cpy();
				if ((aoFlags & (1 << 5)) != 0){//if bottom
					renderAO(view, camera, aopos, (byte) 10);
				}
				if ((aoFlags & (1 << 7)) != 0){//if left
					renderAO(view, camera, aopos, (byte) 9);
				}
			}

			if (side==Side.TOP && ((byte) (aoFlags>>8)) != 0){//only if top side and there is ambient occlusion
				Coordinate aopos = getPosition().cpy().addVector(0, 0, 1);
				if ((aoFlags & 1 << 8) != 0){//if back
					renderAO(view, camera, aopos, (byte) 2);
				}
				if ((aoFlags & (1 << 9)) != 0){//if back right
					renderAO(view, camera, aopos, (byte) 0);
				}
				if ((aoFlags & (1 << 10)) != 0){//if right
					renderAO(view, camera, aopos, (byte) 3);
				}
				if ((aoFlags & (1 << 11)) != 0){//if front right
					renderAO(view, camera, aopos, (byte) 4);
				}
				if ((aoFlags & (1 << 13)) != 0){//if front left
					renderAO(view, camera, aopos, (byte) 5);
				}
				if ((aoFlags & (1 << 14)) != 0){//if left
					renderAO(view, camera, aopos, (byte) 6);
				}
				if ((aoFlags & (1 << 15)) != 0){//if back left
					renderAO(view, camera, aopos, (byte) 1);
				}
			}

			if (side==Side.RIGHT && ((byte) (aoFlags>>16)) != 0){//only if top side and there is ambient occlusion
				Coordinate aopos = getPosition().cpy();
				if ((aoFlags & (1 << 17)) != 0){//if left
					renderAO(view, camera, aopos, (byte) 7);
				}
				if ((aoFlags & (1 << 19)) != 0){//if back left
					renderAO(view, camera, aopos, (byte) 8);
				}
			}
		}
    }
	
	/**
	 * helper function
	 * @param view
	 * @param camera
	 * @param aopos
	 * @param value 
	 */
	private void renderAO(final GameView view, final Camera camera, final AbstractPosition aopos, final byte value){
		SimpleEntity ao = new SimpleEntity((byte) 2, value);
		ao.setPosition(aopos);
		ao.setColor(new Color(0.5f, 0.5f, 0.5f, ambientOcclusion));
		ao.render(view, camera);
	}
	
    /**
     * Ignores lightlevel.
     * @param view the view using this render method
     * @param xPos rendering position
     * @param yPos rendering position
     * @param side The number identifying the side. 0=left, 1=top, 2=right
     */
    public void renderSide(final View view, final int xPos, final int yPos, final Side side){
		Color color;
		if (Controller.getLightEngine() != null && !Controller.getLightEngine().isShadingPixelBased()) {
			color = Controller.getLightEngine().getColor(side);
        } else
			color = Color.GRAY.cpy();
		 
        renderSide(
			view,
            xPos,
            yPos,
            side,
            color
        );
    }
    /**
     * Draws a side of a block at a custom position. Apllies color before rendering and takes the lightlevel into account.
     * @param view the view using this render method
     * @param xPos rendering position
     * @param yPos rendering position
     * @param side The number identifying the side. 0=left, 1=top, 2=right
     * @param color a tint in which the sprite gets rendered. If null color gets ignored
     */
    public void renderSide(final View view, final int xPos, final int yPos, final Side side, Color color){
        Sprite sprite = new Sprite(getBlockSprite(getSpriteId(), getValue(), side));
        sprite.setPosition(xPos, yPos);
        if (getScaling() != 0) {
            sprite.setOrigin(0, 0);
            sprite.scale(getScaling());
        }
		
		if (color!=null) {
			color.r *= blockData.getLightlevelR(side);
			if (color.r>1) color.r=1;
			color.g *= blockData.getLightlevelG(side);
			if (color.g>1) color.g=1;
			color.b *= blockData.getLightlevelB(side);
			if (color.b>1) color.b=1;
			
			sprite.setColor(color);
		}
 
		//draw only outline or regularly?
        if (view.debugRendering()){
            ShapeRenderer sh = view.getShapeRenderer();
            sh.begin(ShapeRenderer.ShapeType.Line);
            sh.rect(xPos, yPos, sprite.getWidth(), sprite.getHeight());
            sh.end();
        } else {
			sprite.draw(view.getSpriteBatch());
			increaseDrawCalls();
		}
    }

	/**
	 * Update the block. Should only be used for cosmetic logic because this is only called for blocks which are covered by a camera.
	 * @param dt time in ms since last update
	 */
    public void update(float dt) {
    }
    


    /**
     *
     * @return
     */
    @Override
    public char getCategory() {
        return 'b';
    }

	/**
	 *
	 * @return
	 */
	@Override
	public int getDimensionZ() {
		return 1;
	}

	/**
	 * Overwrite to define what should happen (view only) if the block is getting destroyed? Sets the value to -1. So be carefull when to call super.onDestroy().
	 * @since v1.4
	 */
	public void onDestroy() {
		blockData.setValue((byte) -1);
		if (destructionSound != null) WE.SOUND.play(destructionSound);
	}

	@Override
	public Coordinate getPosition() {
		return coord;
	}

	@Override
	public void setPosition(AbstractPosition pos) {
		coord = pos.toCoord();
	}
	
	/**
	 * keeps reference
	 * @param coord 
	 */
	public void setPosition(Coordinate coord){
		this.coord = coord;
	}	
	
	/**
	 * gets the identifier and stores them in the map
	 * @return 
	 */
	public Block toStorageBlock(){
		return Block.getInstance(getId(), getValue());
	}
	
	/**
	 * hides the block after it?
	 * @return 
	 */
	public boolean hidingPastBlock(){
		return blockData.hasSides() && !blockData.isTransparent();
	}

	@Override
	public boolean isTransparent() {
		return blockData.isTransparent();
	}

	@Override
	public boolean hasSides() {
		if (blockData==null) return false;
		return blockData.hasSides();
	}

	@Override
	public byte getId() {
		if (blockData==null) return 0;
		return blockData.getId();
	}

	@Override
	public byte getValue() {
		if (blockData==null) return 0;
		return blockData.getValue();
	}

	@Override
	public float getLightlevelR() {
		return blockData.getLightlevelR();
	}
	
	@Override
	public float getLightlevelG() {
		return blockData.getLightlevelG();
	}
	
	@Override
	public float getLightlevelB() {
		return blockData.getLightlevelB();
	}

	@Override
	public void setLightlevel(float lightlevel) {
		blockData.setLightlevel(lightlevel);
	}

	/**
	 * 
	 * @param lightlevel 1 default
	 * @param side 
	 */
	public void setLightlevel(float lightlevel, Side side) {
		blockData.setLightlevel(lightlevel, side);
	}

	@Override
	public void setValue(byte value) {
		blockData.setValue(value);
	}

	@Override
	public boolean isLiquid() {
		if (blockData==null) return false;
		return blockData.isLiquid();
	}
	

	/**
	 *
	 * @return
	 */
	public Block getBlockData() {
		return blockData;
	}
}