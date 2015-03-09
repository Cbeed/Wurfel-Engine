package com.BombingGames.Caveland.GameObjects;

import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Explosion;
import com.BombingGames.WurfelEngine.WE;

/**
 *
 * @author Benedikt Vogler
 */
public class TFlint extends Collectible {
	private static final long serialVersionUID = 2L;
	private static final float TIMETILLEXPLOSION = 2000;
	private float timer = TIMETILLEXPLOSION;
	private boolean lit;

	public TFlint() {
		super(CollectibleType.EXPLOSIVES);
		setFriction(0.02f);
	}

	/**
	 * copy constructor
	 * @param collectible 
	 */
	public TFlint(TFlint collectible) {
		super(collectible);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		if (!shouldBeDisposed()) {
			if (lit)
				timer-=dt;
			if (timer <= 0) {
				new Explosion(
					2,
					500,
					WE.getGameplay().getView().getCameras().get(0)
				).spawn(getPosition());
				dispose();
			}
		}
	}
	
	@Override
	public Collectible clone() throws CloneNotSupportedException {
		return new TFlint(this);
	}

	@Override
	public void action() {
		super.action();
		Controller.getSoundEngine().play("hiss", getPosition());
		setValue(5);
		lit = true;
		timer = TIMETILLEXPLOSION;
	}
}
