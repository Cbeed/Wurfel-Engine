package com.BombingGames.Game;

import com.BombingGames.Game.Blocks.Block;
import com.BombingGames.Game.Blocks.Blockpointer;
import com.BombingGames.Game.Blocks.Player;

/**
 *The camera locks to the player by default. It can be changed with <i>focusblock()</i>.
 * @author Benedikt
 */
public class Camera {
    private int x, y, width, height;
    private int leftborder, topborder, rightborder, bottomborder;
    
    private boolean focus = false;
    private Blockpointer focusblock;
    
    private float zoom = 1;
    
    private final int screenX, screenY, screenWidth, screenHeight;

    /**
     * Creates a camera. Screen does refer to the output of the camera not the real size on the display.
     * @param x The screen coordinates
     * @param y The screen coordinate
     * @param width The screen width of the camera
     * @param height The screen height of the camera
     * @param scale The zoom factor.
     */
    public Camera(int x, int y,int width, int height, float scale) {
        screenX = x;
        screenY = y;
        screenWidth = (int) (width / scale);
        screenHeight = (int) (height / scale);
        this.width = (int) (screenWidth / zoom);
        this.height = (int) (screenHeight / zoom);
    } 
       
    /**
     * Updates the camera
     */
    public void update() {
        if (focus) {//focus on block
             x = focusblock.getX() * Block.WIDTH
                + Block.WIDTH / 2 *(focusblock.getY() % 2)
                + focusblock.getBlock().getOffsetX()
                - Gameplay.getView().getCamera().width / 2;
            
            y = (int) (
                (focusblock.getY()/2f - focusblock.getZ()) * Block.HEIGHT
                - Gameplay.getView().getCamera().height/2
                + focusblock.getBlock().getOffsetY() * (1/Block.ASPECTRATIO)
                );
            
        } else {//focus on player
            Player player = Gameplay.getController().getPlayer();
            x = player.getCoordX() * Block.WIDTH
                + Block.WIDTH / 2 *(player.getCoordY() % 2)
                + player.getOffsetX()
                - Gameplay.getView().getCamera().width / 2;
            
            y = (int) (
                (player.getCoordY()/2f - player.getCoordZ()) * Block.HEIGHT
                - Gameplay.getView().getCamera().height/2
                + player.getOffsetY() * (1/Block.ASPECTRATIO)
                );
        }
        
        //update borders
        leftborder = x/Block.WIDTH -1;
        if (leftborder < 0) leftborder= 0;
        
        rightborder = (x+width)/Block.WIDTH+2;
        if (rightborder >= Map.getBlocksX()) rightborder = Map.getBlocksX()-1;
        
        topborder = y / (Block.HEIGHT/2)-1;
        if (topborder < 0) topborder= 0;
        
        bottomborder = (y+height) / (Block.HEIGHT/2) + Chunk.getBlocksZ()*2;
        if (bottomborder >= Map.getBlocksY()) bottomborder = Map.getBlocksY()-1;
        
    }
    
    public void draw() {
        Gameplay.getView().g.scale(getZoom(), getZoom());
        Controller.getMap().draw();
        
        Gameplay.getView().g.scale(1/getZoom(), 1/getZoom());
        //GUI
        if (Controller.getMap().getMinimap() != null)
            Controller.getMap().getMinimap().draw(); 
    }
    
    /**
     * Set the zoom factor and regenerates the sprites.
     * @param zoom
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        width = (int) (screenWidth / zoom);
        height = (int) (screenHeight / zoom);
    }
    
    /**
     * Returns the zoomfactor.
     * @return
     */
    public float getZoom() {
        return zoom;
    }
    
    /**
     * Returns the zoom multiplied by the EqualizationScale
     * @return 
     */
    public float getAbsZoom() {
        return zoom*Gameplay.getView().getEqualizationScale();
    }

    
    /**
     * Use this if you want to focus on a special block
     * @param blockpointer
     */
    public void FocusOnBlock(Blockpointer blockpointer){
        focus = true;
        focusblock = blockpointer;        
    }
    
    /**
     * The camera now follows the player
     */
    public void FocusOnPlayer(){
        focus = false;
    }
    
    /**
     * Returns the left border of the visible area.
     * @return 
     */
    public int getLeftBorder(){
        return leftborder;
    }
    
    /**
     * Returns the right border of the visible area.
     * @return
     */
    public int getRightBorder(){
        return rightborder;
    }
    
    /**
     * Returns the top seight border of the deepest block
     * @return measured in blocks
     */
    public int getTopBorder(){
        return topborder;
    }
    
     /**
     * Returns the bottom seight border of the highest block
     * @return measured in blocks
     */
    public int getBottomBorder(){
        return bottomborder;
    }
    
  /**
     * The Camera Position in the game world.
     * @return 
     */
    public int getX() {
        return x;
    }

    /**
     * The Camera Position in the game world.
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * The Camera Position in the game world.
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * The Camera Position in the game world.
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

   /**
    * The amount of pixel which are visible in Y direction (game pixels). For screen pixels use <i>ScreenHeight()</i>.
    * @return
    */
   public int getGroundHeight() {
        return height;
    }


    /**
     * The amount of pixel which are visible in x direction (game pixels). For screen pixels use <i>ScreenWidth()</i>.
     * @return
     */
    public int getWidth() {
        return width;
    }


    /**
     * Returns the amount of (game) pixels visible in Y direction. Ground level+ the width of the slope.
     * @return
     */
    public int getTotalHeight() {
        return height + Block.HEIGHT*Chunk.getBlocksZ();
    }

    
    /**
     * False= Focus on player, true= Focus is on a block
     * @return
     */
    public boolean getFocus(){
        return focus;
    }

    
    /**
     * Returns the height of the camera output.
     * @return
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Returns the width of the camera output.
     * @return
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Returns the position of the cameras output.
     * @return
     */
    public int getScreenX() {
        return screenX;
    }

    /**
     * Returns the position of the camera
     * @return
     */
    public int getScreenY() {
        return screenY;
    }
    
    /**
     * Returns the pixel-coordinates (screen) of a block's center.
     * @param x
     * @param y
     * @param z
     * @return an array with [0]-x and [1]-y 
     */
    public int[] getCenterofBlock(int x, int y, int z){
        int result[] = new int[2];
        result[0] = -Gameplay.getView().getCamera().getX()+x*Block.WIDTH + (y%2) * (int) (Block.WIDTH/2) + Controller.getMapDataUnsafe(x, y, z).getOffsetX();
        result[1] = (int) (-Gameplay.getView().getCamera().getY()+y*Block.HEIGHT/2 - z*Block.HEIGHT + Controller.getMapDataUnsafe(x, y, z).getOffsetY() * (1/Block.ASPECTRATIO));
        return result;
    }
}
