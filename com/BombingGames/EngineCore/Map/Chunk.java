package com.BombingGames.EngineCore.Map;

import com.BombingGames.EngineCore.GameplayScreen;
import com.BombingGames.Game.Gameobjects.Block;
import com.BombingGames.Wurfelengine;
import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * A Chunk is filled with many Blocks and is a part of the map.
 * @author Benedikt
 */
public class Chunk {
    /**The number of the mapgenerator used.*/
    public static final int GENERATOR = 8;
    /**The suffix of a chunk file.*/
    protected static final String CHUNKFILESUFFIX = "wec";
    /**The suffix of the metafile */
    protected static final String METAFILESUFFIX = "wem";
    
    private static int blocksX = 10;
    private static int blocksY = 40;//blocksY must be even number
    private static int blocksZ = 10;
    
    private Block data[][][] = new Block[blocksX][blocksY][blocksZ];
  
    /**
     * Creates a Chunk filled with air
     */
    public Chunk() {
        for (int x=0; x < blocksX; x++)
            for (int y=0; y < blocksY; y++)
                for (int z=0; z < blocksZ; z++)
                    data[x][y][z] = Block.getInstance(0);
    }
    
    /**
    *Creates a chunk.
    * @param pos the position of the chunk. Value between 0-8
    * @param coordX 
    * @param coordY 
    * @param newMap load from HD(true) or generate new (false)?
    */
    public Chunk(int pos, int coordX, int coordY, boolean newMap){
        this();

        if (newMap)
            generate(pos, coordX, coordY);
            else load(pos, coordX, coordY);
    }
    
    /**
     * Generates new content for a chunk.
     */  
    private void generate(int pos, int coordX, int coordY){
        //chunkdata will contain the blocks and objects
        //alternative to chunkdata.length ChunkBlocks
        Gdx.app.log("DEBUG","Creating new chunk: "+ coordX + ", "+ coordY);
        GameplayScreen.msgSystem().add("Creating new chunk: "+coordX+", "+ coordY);
        switch (GENERATOR){
            case 0:{//random pillars
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        int height = (int) (Math.random()*blocksZ-1)+1;
                        for (int z=0; z < height; z++){
                            data[x][y][z] = Block.getInstance(2);
                            }
                        data[x][y][height] = Block.getInstance(1);
                    }
                break;
            }
                
            case 1: {//islands
                //water
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        data[x][y][0] = Block.getInstance(8);
                        data[x][y][1] = Block.getInstance(9);
                        data[x][y][2] = Block.getInstance(9);
                    }
                
                //mountain
                int mountainx = (int) (Math.random()*blocksX-1);
                int mountainy = (int) (Math.random()*blocksY-1);
                
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        int height = blocksZ-1- Math.abs(mountainy-y)- Math.abs(mountainx-x);
                        if (height>0){
                            for (int z=0; z < height; z++) {
                                if (height > 2)
                                    data[x][y][z] = Block.getInstance(2);
                                else
                                    data[x][y][z] = Block.getInstance(8);
                                    
                            }
                            if (height > 2)
                                    data[x][y][height] = Block.getInstance(1);
                                else
                                    data[x][y][height] = Block.getInstance(8);
                            
                            if (Math.random() < 0.15f && height < getBlocksZ()-1 && height > 2) data[x][y][height+1] = Block.getInstance(34);
                            if (Math.random() < 0.15f && height < getBlocksZ()-1 && height > 2) data[x][y][height+1] = Block.getInstance(35);
                        }
                    }
                break;
            }
                
            case 2: {//flat block (grass?)
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        if (blocksZ>1){
                            int z;
                            for (z=0; z < blocksZ/2; z++){
                                data[x][y][z] = Block.getInstance(2);
                            }
                            data[x][y][z-1] = Block.getInstance(1);
                        }else data[x][y][0] = Block.getInstance(2);
                    }
                break;
            }
                
            case 3: {//flat gras with one random pillar per chunk
                int pillarx = (int) (Math.random()*blocksX-1);
                int pillary = (int) (Math.random()*blocksY-1);
                //pillar
                for (int z=0; z < blocksZ; z++) data[pillarx][pillary][z] = Block.getInstance(1);
                
                //flat grass
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        data[x][y][0] = Block.getInstance(2);
                        data[x][y][1] = Block.getInstance(3);
                    }
                break;
            }
                
            case 4: {//explosive barrel test
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++)
                        for (int z=0; z < blocksZ-1; z++){
                            if (z!=blocksZ-2)
                                 data[x][y][z] = Block.getInstance(2);
                            else data[x][y][z] = Block.getInstance(1);
                    }
            }
                
            case 5: {//animation test                
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        data[x][y][0] = Block.getInstance(72);
                    }
                //data[blocksX/2][blocksY/2][2] = Block.getInstance(72);//animation test
                //data[blocksX/2][blocksY/2][1] = Block.getInstance(2);
                
                break;
            } 
            case 6: {//every block                
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        data[x][y][0] = Block.getInstance(y, 0, new Coordinate(x + pos % 3 * blocksX, y + pos / 3 * blocksY, 0, true));
                    }
                break;
            }
             case 7: {//flat grass
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        if (blocksZ>1){
                            int z;
                            for (z=0; z < blocksZ/2; z++){
                                data[x][y][z] = Block.getInstance(9);
                            }
                        }else data[x][y][0] = Block.getInstance(9);
                    }
                break;
            }
            case 8: {//flat gras with one special block
                //flat grass
                for (int x=0; x < blocksX; x++)
                    for (int y=0; y < blocksY; y++){
                        data[x][y][0] = Block.getInstance(2);
                        data[x][y][1] = Block.getInstance(3);
                    }
                
                int specialx = (int) (Math.random()*blocksX-1);
                int specialy = (int) (Math.random()*blocksY-1);
                //pillar
                data[specialx][specialy][1] = Block.getInstance(40, 0, new Coordinate(specialx + pos % 3 * blocksX, specialy + pos / 3 * blocksY, 1, true));
                break;
            }
        }
    }
    
    /**
     * loads a chunk from memory
     */
    private void load(int pos, int coordX, int coordY){
        //Reading map files test
        try {
            // if (new File("map/chunk"+coordX+","+coordY+".otmc").exists()) {
            File path = new File(Wurfelengine.getWorkingDirectory().getAbsolutePath() + "/map/chunk"+coordX+","+coordY+"."+CHUNKFILESUFFIX);
            Gdx.app.log("DEBUG","Trying to load Chunk: "+ coordX + ", "+ coordY + " from \"" + path.getAbsolutePath() + "\"");
            GameplayScreen.msgSystem().add("Load: "+coordX+","+coordY);
            
            if (path.isFile()) {
                //FileReader input = new FileReader("map/chunk"+coordX+","+coordY+".otmc");
                //BufferedReader bufRead = new BufferedReader(input);
                BufferedReader bufRead = new BufferedReader(new FileReader(path));

                StringBuilder line;
                //jump over first line to prevent problems with length byte
                bufRead.readLine();


                int z = 0;
                int x;
                int y;
                String lastline;

                //finish a layer
                do {
                    line = new StringBuilder();
                    line.append(bufRead.readLine());
                    //optionale Kommentarzeile überspringen
                    if ((line.charAt(1) == '/') && (line.charAt(2) == '/')){
                        line = new StringBuilder();
                        line.append(bufRead.readLine());
                    }

                    //Ebene
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
                                        Integer.parseInt(line.substring(posdots+1, posend)),
                                        new Coordinate(x + pos % 3 * blocksX, y + pos / 3 * blocksY, z, true)
                                        );
                            x++;
                            line.delete(0,posend+1);
                        } while (x < blocksX);

                        line = new StringBuilder();
                        line.append(bufRead.readLine());

                        y++;
                    } while (y < blocksY);
                    lastline = bufRead.readLine();
                    z++;
                } while (lastline != null);
            } else {
                Gdx.app.log("DEBUG","...but it could not be found. Creating new.");
                generate(pos, coordX, coordY);
            }
        } catch (IOException ex) {
            Gdx.app.log("ERROR","Loading of chunk "+coordX+","+coordY + "failed: "+ex);
        }
    }
    
    /**
     * reads the map info file and sets the size of the chunk
     */
    public static void readMapInfo(){
        BufferedReader bufRead = null;
        try {
            File path = new File(Wurfelengine.getWorkingDirectory().getAbsolutePath() + "/map/map."+METAFILESUFFIX);
            Gdx.app.log("DEBUG","Trying to load Map Info from \"" + path.getAbsolutePath() + "\"");
            bufRead = new BufferedReader(new FileReader(path));
            String mapname = bufRead.readLine();
            mapname = mapname.substring(2, mapname.length());
            Gdx.app.log("INFO","Loading map: "+mapname);
            GameplayScreen.msgSystem().add("Loading map: "+mapname);   
            
            String mapversion = bufRead.readLine(); 
            mapversion = mapversion.substring(2, mapversion.length());
            Gdx.app.log("DEBUG","Map Version:"+mapversion);
            
            String blocksXString = bufRead.readLine();
            Gdx.app.log("DEBUG","sizeX:"+blocksXString);
            blocksXString = blocksXString.substring(2, blocksXString.length());
            blocksX = Integer.parseInt(blocksXString);
            
            String blocksYString = bufRead.readLine();
            Gdx.app.log("DEBUG","sizeY:"+blocksYString);
            blocksYString = blocksYString.substring(2, blocksYString.length());
            blocksY = Integer.parseInt(blocksYString);
            
            String blocksZString = bufRead.readLine();
            Gdx.app.log("DEBUG","sizeZ:"+blocksZString);
            blocksZString = blocksZString.substring(2, blocksZString.length());
            blocksZ = Integer.parseInt(blocksZString);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                null,
                "The meta file could not be read. It must be named 'map."+ Chunk.METAFILESUFFIX + "' and must be at the maps directory:"+ Wurfelengine.getWorkingDirectory().getAbsolutePath() + "/map/",
                "Loading error",
                 JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bufRead.close();
            } catch (IOException ex) {
                Logger.getLogger(Chunk.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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


}
