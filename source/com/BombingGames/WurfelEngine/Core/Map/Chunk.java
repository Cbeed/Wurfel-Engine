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
package com.BombingGames.WurfelEngine.Core.Map;

import com.BombingGames.WurfelEngine.Core.Controller;
import com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractEntity;
import com.BombingGames.WurfelEngine.Core.Gameobjects.AbstractGameObject;
import com.BombingGames.WurfelEngine.Core.Gameobjects.Block;
import com.BombingGames.WurfelEngine.WE;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * A Chunk is filled with many Blocks and is a part of the map.
 * @author Benedikt
 */
public class Chunk {
    /**The suffix of a chunk files.*/
    protected static final String CHUNKFILESUFFIX = "wec";
    /**The suffix of the metafile */
    protected static final String METAFILESUFFIX = "wem";
    
    private static int blocksX = 10;
    private static int blocksY = 40;//blocksY must be even number
    private static int blocksZ = 10;

    /**
     *
     * @param meta
     */
    protected static void setDimensions(MapMetaData meta) {
        blocksX = meta.getChunkBlocksX();
        blocksY = meta.getChunkBlocksY();
        blocksZ = meta.getChunkBlocksZ();
    }
    
    private Block data[][][];
  
    /**
     * Creates a Chunk filled with empty cells (likely air).
     */
    public Chunk() {
        data = new Block[blocksX][blocksY][blocksZ];
        
        for (int x=0; x < blocksX; x++)
            for (int y=0; y < blocksY; y++)
                for (int z=0; z < blocksZ; z++)
                    data[x][y][z] = Block.getInstance(0);
    }
    
    /**
    *Creates a chunk by trying to load and if this fails it generates a new one.
    * @param coordX the chunk coordinate
    * @param coordY the chunk coordinate
     * @param mapname filename
     * @param generator
    */
    public Chunk(final String mapname, final int coordX, final int coordY, final Generator generator){
        this();
        Gdx.app.debug("Chunk", "Creating chunk"+coordX+","+coordY);
        if (!load(mapname, coordX, coordY))
            fill(coordX, coordY, generator);
    }
    
    /**
    *Creates a chunk by generating a new one.
    * @param coordX the chunk coordinate
    * @param coordY the chunk coordinate
    * @param generator
    */
    public Chunk(final int coordX, final int coordY, final Generator generator){
        this();
        fill(coordX, coordY, generator);
    }
    
    /**
     * 
     * @param coordX
     * @param coordY
     * @param generator 
     */
    private void fill(final int coordX, final int coordY, final Generator generator){
        WE.getConsole().add("Creating new chunk: "+coordX+", "+ coordY);
        for (int x=0; x < blocksX; x++)
            for (int y=0; y < blocksY; y++)
                for (int z=0; z < blocksZ; z++)
                    Block.getInstance(
                        generator.generate(blocksX*coordX+x, blocksY*coordY+y, z),
                        0
					).spawn(new Coordinate(blocksX*coordX+x, blocksY*coordY+y, z, false));
    }
    
    /**
     * Tries to load a chunk from disk.
     */
    private boolean load(final String fileName, int coordX, int coordY){

		//FileHandle path = Gdx.files.internal("/map/chunk"+coordX+","+coordY+"."+CHUNKFILESUFFIX);
		FileHandle path = Gdx.files.absolute(
			WE.getWorkingDirectory().getAbsolutePath()
				+ "/maps/"+fileName+"/chunk"+coordX+","+coordY+"."+CHUNKFILESUFFIX
		);

		Gdx.app.debug("Chunk","Loading Chunk: "+ coordX + ", "+ coordY + "\"");

		if (path.exists()) {
			//Reading map files test
			try {
				//FileReader input = new FileReader("map/chunk"+coordX+","+coordY+".otmc");
				//BufferedReader bufRead = new BufferedReader(input);
				BufferedReader bufRead = path.reader(30000);//normal chunk file is around 17.000 byte

				//jump over first line to prevent problems with length byte
				bufRead.readLine();

				int z = 0;
				int x;
				int y;
				boolean loadingEntities = false;//flag if entitie part has begun
				
				String bufLine = bufRead.readLine();
				
				//read a line
				while (bufLine != null) {
					if (bufLine.length()>0){
						StringBuilder line = new StringBuilder(1);
						line.append(bufLine);

						//jump over optional comment line
						if (
							line.length() > 0
							&& line.charAt(0) == '/'
							&& line.charAt(1) == '/'
						){
							Gdx.app.debug("Chunk",line.toString());
							line = new StringBuilder(1);
							line.append(bufRead.readLine());//read next row
						}
						
						if (loadingEntities==false && line.toString().startsWith("entities") ){
							loadingEntities=true;
							Gdx.app.debug("Chunk","loading entities");
						}

						if (loadingEntities){
							//ignore entities at the moment
						} else {
							//if layer is empty, fill with air
							if (line.charAt(0) == 'l' ){
								for (int elx = 0; elx < blocksX; elx++) {
									for (int ely = 0; ely < blocksY; ely++) {
										data[elx][ely][z] = Block.getInstance(0);
									}
								}
							} else {
								//fill layer block by block
								y = 0;
								do{
									x = 0;

									do{
										int posdots = line.indexOf(":");

										int posend = 1;
										while ((posend < -1+line.length()) && (line.charAt(posend)!= ' '))  {
											posend++;
										}

										data[x][y][z] = Block.getInstance(
											Integer.parseInt(line.substring(0,posdots)),
											Integer.parseInt(line.substring(posdots+1, posend))
										);
										x++;
										line.delete(0,posend+1);
									} while (x < blocksX);

									line = new StringBuilder(1);
									line.append(bufRead.readLine());//read next row

									y++;
								} while (y < blocksY);
							}
							z++;
						}
					}
					//read next line
					bufLine = bufRead.readLine();
				}
				return true;
			} catch (IOException ex) {
				Gdx.app.error("Chunk","Loading of chunk "+coordX+","+coordY + " failed: "+ex);
			} catch (StringIndexOutOfBoundsException ex) {
				Gdx.app.error("Chunk","Loading of chunk "+coordX+","+coordY + " failed. Map file corrupt: "+ex);
			} catch (ArrayIndexOutOfBoundsException ex){
				Gdx.app.error("Chunk","Loading of chunk "+coordX+","+coordY + " failed.Chunk or meta file corrupt: "+ex);
			}
		} else {
			Gdx.app.log("Chunk",coordX + ","+ coordY +" could not be found.");
		}
        
        return false;
    }
    
    /**
     * 
     * @param fileName the map name on disk

	 * @param pos position on map in memory
     * @return 
     * @throws java.io.IOException 
     */
    public boolean save(String fileName, int pos) throws IOException {
        if ("".equals(fileName)) return false;
		int coords[] = Controller.getMap().getChunkCoords(pos);
        Gdx.app.log("Chunk","Saving "+coords[0] + ","+ coords[1] +".");
        FileHandle path = new FileHandle(WE.getWorkingDirectory().getAbsolutePath() + "/maps/"+fileName+"/chunk"+coords[0]+","+coords[1]+"."+CHUNKFILESUFFIX);
        String lineFeed = System.getProperty("line.separator");
        
        path.file().createNewFile();
        try (Writer writer = path.writer(false, "UTF8")) {		
            for (int z = 0; z < blocksZ; z++) {
				//check if layer is empty
				boolean dirty = false;
				for (int x = 0; x < blocksX; x++) {
					for (int y = 0; y < blocksY; y++) {
						if (data[x][y][z].getId() != 0)
							dirty=true;
					}
				}
				
				writer.write("//"+z+lineFeed);
				if (dirty)
					for (int y = 0; y < blocksY; y++) {
						String line = "";
						for (int x = 0; x < blocksX; x++) {
							line +=data[x][y][z].getId()+":"+data[x][y][z].getValue()+" ";  
						}
						writer.write(line+lineFeed);
					}
				else {
					writer.write('l'+lineFeed);
				}
				writer.write(lineFeed);
            }
		
			//save entities
			writer.write("entities"+lineFeed);
			ArrayList<AbstractEntity> entities = Controller.getMap().getEntitysOnChunk(pos);
			for (AbstractEntity ent : entities){
				ent.save(writer);
			}
		} catch (IOException ex){
            throw ex;
        }
        return true;
    }
        /**
     * The amount of blocks in X direction
     * @return 
     */
    public static int getBlocksX() {
        return blocksX;
    }

    /**
     * The amount of blocks in Y direction
     * @return 
     */
    public static int getBlocksY() {
        return blocksY;
    }

   /**
     * The amount of blocks in Z direction
     * @return 
     */
    public static int getBlocksZ() {
        return blocksZ;
    }
    

    /**
     * Returns the data of the chunk
     * @return 
     */
    public Block[][][] getData() {
        return data;
    }

    /**
     * 
     * @param data
     */
    public void setData(Block[][][] data) {
        this.data = data;
    }

    /**
     *
     * @return
     */
    public static int getScreenWidth(){
        return blocksX*AbstractGameObject.SCREEN_WIDTH;
    }
    
    /**
     *
     * @return
     */
    public static int getScreenDepth() {
        return blocksY*AbstractGameObject.SCREEN_DEPTH/4;
    }
    
    /**
     *
     * @return
     */
    public static int getGameWidth(){
        return blocksX*AbstractGameObject.GAME_DIAGLENGTH;
    }
    
    /**
     *
     * @return
     */
    public static int getGameDepth() {
        return blocksY*AbstractGameObject.GAME_DIAGLENGTH2;
    }
    
        /**
     * The height of the map.
     * @return in game size
     */
    public static int getGameHeight(){
        return blocksZ*AbstractGameObject.GAME_EDGELENGTH;
    }
	
	/**
	 * print the chunk to console
	 */
	public void print() {
		for (int z = 0; z < blocksZ; z++) {
			for (int y = 0; y < blocksY; y++) {
				for (int x = 0; x < blocksX; x++) {
					if (data[x][y][z].getId()==0)
						System.out.print("  ");
					else
						System.out.print(data[x][y][z].getId() + " ");
				}
				System.out.print("\n");
			}
				System.out.print("\n\n");
		}
	}
}