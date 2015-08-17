/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bombinggames.wurfelengine.core.console;

import com.bombinggames.wurfelengine.core.Controller;
import com.bombinggames.wurfelengine.core.GameplayScreen;
import com.bombinggames.wurfelengine.core.Map.AbstractMap;
import com.bombinggames.wurfelengine.core.WorkingDirectory;
import java.io.File;
import java.util.StringTokenizer;

/**
 *
 * @author Benedikt Vogler
 */
public class LoadMapCommand implements ConsoleCommand {

	@Override
	public boolean perform(StringTokenizer parameters, GameplayScreen gameplay) {
		if (!parameters.hasMoreElements()) return false;

		String mapname = parameters.nextToken();
		if (mapname.length()>0) {
			int slot = AbstractMap.newSaveSlot(new File(WorkingDirectory.getMapsFolder()+"/"+mapname));
			return Controller.loadMap(new File(WorkingDirectory.getMapsFolder()+"/"+mapname), slot);
		}
		return true;
	}

	@Override
	public String getCommandName() {
		return "loadmap";
	}
	
}