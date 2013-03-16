package com.BombingGames.Game;

import com.BombingGames.Game.CustomGame.CustomGameController;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * The Gameplay State. This is state where the magic happens.
 * @author Benedikt
 */
public class Gameplay extends BasicGameState { 
  /**
     * Contains the Message System
     */
    public static final MsgSystem MSGSYSTEM = new MsgSystem();    
    
    private View view = null;
    private Controller controller = null;
     
    
    /**
     * 
     * @return
     */
    @Override
    public int getID() {
        return 2;
    }
     
    /**
     * 
     * @param gc
     * @param sbg
     * @throws SlickException
     */
    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
    }
    
    /**
     * 
     * @param container
     * @param game
     * @throws SlickException
     */
    @Override 
    public void enter(GameContainer container, StateBasedGame game) throws SlickException{
        container.setClearEachFrame(false);
        //Wurfelengine.getGameContainer().setSmoothDeltas(true);
        
        controller = new CustomGameController(container, game);
        view = new View(container, controller);
        controller.setView(view);
    }    

    /**
     * 
     * @param gc
     * @param sbg
     * @param delta
     * @throws SlickException
     */
    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        controller.update(delta);
    }

    /**
     * 
     * @param container
     * @param game
     * @param g
     * @throws SlickException
     */
    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        view.render(g);
    }
}