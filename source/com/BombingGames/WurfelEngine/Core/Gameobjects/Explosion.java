package com.BombingGames.WurfelEngine.Core.Gameobjects;

import com.BombingGames.WurfelEngine.Core.Camera;
import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Map.Coordinate;
import com.BombingGames.WurfelEngine.Core.Map.Point;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;

/**
 *
 * @author Benedikt Vogler
 */
public class Explosion extends AbstractEntity {
	private static final long serialVersionUID = 1L;
	private static String explosionsound;
	
	private final int radius;
	private final int damage;
	private transient Camera camera;

	/**
	 * simple explosion without screen shake. Default radius is 2. Damage 500.
	 */
	public Explosion() {
		super(0);
		this.radius = 2;
		damage = 500;
		setSaveToDisk(false);
	}

	
	/**
	 * 
	 * @param radius the radius in game world blocks
	 * @param damage [0;1000]
	 * @param camera can be null. used for screen shake
	 */
	public Explosion(int radius, int damage, Camera camera) {
		super(0);
		this.radius = radius;
		this.damage = damage;
		if (explosionsound == null)
            explosionsound = "explosion";
		this.camera = camera;
		setSaveToDisk(false);
    }

	@Override
	public void update(float dt) {
	}

	/**
	 * explodes
	 * @return 
	 */
	
	@Override
	public AbstractEntity spawn(Point point) {
		super.spawn(point);
		//replace blocks by air
		for (int x=-radius; x<radius; x++){
			for (int y=-radius*2; y<radius*2; y++) {
				for (int z=-radius; z<radius; z++){
					Coordinate coord = point.cpy().getCoord().addVector(x, y, z);
					if (x*x + (y/2)*(y/2)+ z*z <= radius*radius){//check if in radius
						coord.destroy();
						
						//get every entity which is attacked
						ArrayList<MovableEntity> list =
							Controller.getMap().getEntitysOnCoord(coord,
								MovableEntity.class
							);
						for (MovableEntity ent : list) {
							if (!(ent instanceof PlayerWithWeapon))//don't damage player with weapons
								ent.damage(damage);
						}
						
						new Dust(
							1700,
							coord.getPoint().getVector().sub(point.getVector())
								.nor().scl(AbstractGameObject.GAME_EDGELENGTH*4f),//move from center to outside
							new Color(0.5f,0.45f,0.4f,1f)
						).spawn(coord.getPoint().cpy());//spawn at center
					}
				}
			}	
		}
		
		if (camera!=null)
			camera.shake(radius*100/3f, 100);
		if (explosionsound != null)
			Controller.getSoundEngine().play(explosionsound);
		dispose();
		return this;
	}
}