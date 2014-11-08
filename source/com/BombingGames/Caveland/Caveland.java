package com.BombingGames.Caveland;

import com.BombingGames.Caveland.Game.CustomGameController;
import com.BombingGames.Caveland.Game.CustomGameView;
import com.BombingGames.Caveland.Game.GunTestController;
import com.BombingGames.Caveland.Game.GunTestView;
import com.BombingGames.Caveland.Game.SplitScreenView;
import com.BombingGames.Caveland.MainMenu.MainMenuScreen;
import com.BombingGames.WurfelEngine.Core.BasicMainMenu.BasicMenuItem;
import com.BombingGames.WurfelEngine.Core.WorkingDirectory;
import com.BombingGames.WurfelEngine.WE;
import java.io.File;
import java.io.InputStream;

/**
 *A test project to test if the engine can run as a library.
 * @author Benedikt Vogler
 */
public class Caveland {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WE.construct("Wurfelengine V" + WE.VERSION, args);
		
        
        BasicMenuItem[] menuItems = new BasicMenuItem[]{
            new BasicMenuItem(0, "Load Map", CustomGameController.class, CustomGameView.class, new CustomConfiguration()),
            new BasicMenuItem(1, "Generate", CustomGameController.class, CustomGameView.class, new CustomConfiguration()),
            new BasicMenuItem(2, "Guns", GunTestController.class, GunTestView.class, new CustomConfiguration()),
            new BasicMenuItem(4, "Split Screen", CustomGameController.class, SplitScreenView.class, new CustomConfiguration()),
            new BasicMenuItem(5, "Options"),
            new BasicMenuItem(6, "Exit")
        };   
        
        WE.setMainMenu(new MainMenuScreen(menuItems));
        WE.launch();
		if (!new File(WorkingDirectory.getMapsFolder()+"/default").exists()){
			InputStream in = Caveland.class.getClassLoader().getResourceAsStream("com/BombingGames/Caveland/defaultmap.zip");
			WorkingDirectory.unpackMap(
				"default",
				in
			);
		}
    }

	public static String getCredits(){
		return "Credits\n" +
			"\n" +
			"ein Spiel von\n" +
			"Benedikt S. Vogler\n" +
			"\n" +
			"Graphik\n" +
			"Frederic Brückner\n" +
			"\n" +
			"Musik & Sound\n" +
			"Felix von Dohlen\n" +
			"Marcel Gohsen\n" +
			"\n" +
			"Qualitätssicherung\n" +
			"Thomas Vogt\n" +
			"\n" +
			"Hintergrund Geschichte\n" +
			"Felix Günther\n" +
			"\n" +
			"Web Development\n" +
			"René Weißer\n" +
			"\n" +
			"Speziellen Dank an\n" +
			"Vanya Gercheva\n" +
			"Ulrike Vogler\n" +
			"Gereon Vogler\n" +
			"Bernhard Vogler\n" +
			"Pia Lenßen\n" +
			"reddit.com/r/Gamedev\n" +
			"Bauhaus Universität Weimar";
	
	}	
    
}
