package com.genesyseast.coinconnection.Support;
/*


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class JSONEngine
{
    public static HashMap<Long, Integer>   hashMap    = new HashMap<>();
    public static HashMap<String, Integer> convertMap = new HashMap<>();
    public static ArrayList<JSONAssetList> list       = new ArrayList<>();
    // Not supported here, only in the editor
    public static JSONArray                dummy      = new JSONArray();
    
    public static void main( String[] args )
    {
        int      name;
        int      type;
        int      path;
        String[] dataTypes = new String[]{ "MonoBehaviour", "Sprite" };
        
        try
        {
            FileReader     reader         = new FileReader( "C:\\AndroidDev\\BF_Data\\myAssets.txt" );
            BufferedReader bufferedReader = new BufferedReader( reader );
            
            String line;
            
            while ( (line = bufferedReader.readLine()) != null )
            {
                // Show what we have
                //                System.out.println( line );
                
                JSONAssetList item = new JSONAssetList();
                
                name = line.indexOf( "NAME:" );
                type = line.indexOf( "TYPE:" );
                path = line.indexOf( "PATH_ID:" );
                
                if ( line.contains( "NAME:" ) )
                {
                    // File's name
                    item.fileName = line.substring( name + 5, type ).trim();
                    // File's type
                    item.dataType = (line.substring( type + 5, path ).trim().contentEquals( "MonoBehaviour" ) ? 0 : 1);
                    // File's resource value
                    item.pathID = Long.parseLong( line.substring( path + 8 ).trim() );
                    
                    // Hashmap. Returns the index to the class data
                    hashMap.put( item.pathID, list.size() );
                    
                    // Add to the list
                    list.add( item );
                }
            }
            reader.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        //
        // Load the convert data
        // These are what I want to replace with
        //
        try
        {
            FileReader     reader         = new FileReader( "C:\\AndroidDev\\BF_Data\\convert.txt" );
            BufferedReader bufferedReader = new BufferedReader( reader );
            JSONAssetList  item           = new JSONAssetList();
            String         line;
            
            //
            while ( (line = bufferedReader.readLine()) != null )
            {
                name = line.indexOf( "NAME:" );
                type = line.indexOf( "TYPE:" );
                path = line.indexOf( "; " );
                
                if ( line.contains( "NAME:" ) )
                {
                    // File's name
                    item.fileName = line.substring( name + 5, type ).trim();
                    // File's type
                    item.dataType = Integer.parseInt( line.substring( type + 5, path ).trim() );
                    
                    // Hashmap. Returns the index to the class data
                    convertMap.put( item.getFileName(), item.getDataType() );
                }
            }
            reader.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        //##################################
        //
        // Now load Level files one-by-one
        //
        //##################################
        int actualLevel = 0;
        
        for ( int i = 0; i < 2000; i++ )
        {
            try
            {
                String inPath = String.format( Locale.getDefault(), "C:\\AndroidDev\\BF_Data\\Levels\\Level %04d.json", i + 1 );
                //                String outPath = String.format( Locale.getDefault(), "C:\\AndroidDev\\BF_Data\\NewLevels\\Level_%04d.json", i+1 );
                String outPath = String.format( Locale.getDefault(), "C:\\AndroidDev\\BF_Data\\NewLevels\\Level_%04d.json", actualLevel + 1 );
                // Writing JSON Object
                JSONObject jsonWriteObj = new JSONObject();
                
                if ( !new File( inPath ).exists() )
                {
                    inPath = null;
                    break;
                }
                //
                // For writing the new level data format
                //
                FileWriter writer = new FileWriter( outPath );
                
                //
                // Grid size
                //
                jsonWriteObj.put( "gridWidth", 7 );
                jsonWriteObj.put( "gridHeight", 7 );
                jsonWriteObj.put( "myName", String.format( Locale.getDefault(), "Level_%04d", actualLevel + 1 ) );
                
                // Actual Level in my code format
                jsonWriteObj.put( "myCode", String.format( Locale.getDefault(), "0xC0%04d", i + 1 ) );
                
                
                // NOTE: Every twelfth board is a card match game
                if ( actualLevel > 1 && (actualLevel % 12) == 0 )
                {
                    // Scripted Movements
                    jsonWriteObj.put( "help_script", dummy );
                    
                    // Card match game
                    jsonWriteObj.put( "isCardMatch", 1 );
                    
                    // Level Objective
                    jsonWriteObj.put( "objective", new String[]{ "Find all required cards with moves allowed." } );
                    
                    // Get the REAL level
                    actualLevel++;
                    
                    writer.write( jsonWriteObj.toString( 2 ) );
                    writer.close();
                    
                    // Get to the level we skipped
                    i--;
                    continue;
                }
                
                
                //
                // Read all the JSON data for processing at once
                //
                // Need "moves" integer
                // Need "levelGoals" array for ElementType files then Icon data ONLY
                // Need "starMilestones" array
                // Need "blockSpawnChances" array for Count ONLY
                // Need "defaultLayout": {
                //                          "gridSize": { "x": 7.0, "y": 7.0 } for grid size match test ONLY
                //                          "grid" array to find board pieces data in BG then BT files ONLY
                String content = new String( Files.readAllBytes( Paths.get( inPath ) ) );
                
                // Reading JSON Object
                JSONObject jsonObject = new JSONObject( content );
                
                int       moves      = jsonObject.getInt( "moves" ); // good
                JSONArray levelGoals = jsonObject.getJSONArray( "levelGoals" ); // good
                
                JSONObject starData = jsonObject.getJSONObject( "starData" ); // good
                //                JSONArray  starMilestones = starData.getJSONArray( "starMilestones" );
                
                // These are typically what can be spawned / re-spawned onto the board
                JSONArray blockSpawnChances = jsonObject.getJSONArray( "blockSpawnChances" ); // good
                //                JSONArray spawns = blockSpawnChances.getJSONArray( "blockSpawnChances" );
                
                JSONObject defaultLayout = jsonObject.getJSONObject( "defaultLayout" );
                //                JSONObject boardSize     = defaultLayout.getJSONObject( "gridSize" );
                //                JSONArray  boardGrid     = defaultLayout.getJSONArray( "grid" );
                
                
                //###############################
                //
                // Save the new data
                //
                //###############################
                // Save the board mapping
                // auto writes to JSON if good
                if ( !jsonWriteBoardData( jsonWriteObj, defaultLayout ) )
                {
                    // Scripted Movements
                    jsonWriteObj.put( "help_script", dummy );
                    
                    // Card match game
                    jsonWriteObj.put( "isCardMatch", 1 );
                    
                    // Level Objective
                    jsonWriteObj.put( "objective", new String[]{ "Find all required cards with moves allowed." } );
                    // Get the REAL level
                    actualLevel++;
                }
                else
                {
                    // Card game: Grid is a bad size
                    jsonWriteObj.put( "isCardMatch", 0 );
                    
                    // Scripted Movements
                    jsonWriteObj.put( "help_script", dummy );
                    // Moves allowed
                    jsonWriteObj.put( "moves", moves );
                    
                    // Level Objective
                    jsonWriteObj.put( "objective", new String[]{ "...with moves allowed." } );
                    
                    // Save Star scores
                    jsonWriteStarsData( jsonWriteObj, starData );
                    // Save Goal data
                    jsonWriteGoalsData( jsonWriteObj, levelGoals );
                    // Save Spawning board pieces
                    jsonWriteSpawnData( jsonWriteObj, blockSpawnChances );
                }
                
                
                //###############################
                //
                // Save the JSON file
                //
                //###############################
                writer.write( jsonWriteObj.toString( 2 ) );
                writer.close();
                
                // Get to the next REAL level
                actualLevel++;
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
    
    
    */
/**
     * //################################
     * <p>
     * Used to locate the conversion num
     * used by Coin Connection
     * <p>
     * //################################
     *
     * @param oldName resource ID
     *
     * @return data
     *//*

    private static String findByConvertNumber( String oldName )
    {
        if ( oldName != null && convertMap.get( oldName ) != null )
        {
            return convertMap.get( oldName ).toString();
        }
        
        return null;
    }
    
    
    */
/**
     * //################################
     * <p>
     * Save the board map data in JSON
     * format
     * <p>
     * //################################
     *
     * @param defaultLayout data to write
     *//*

    private static boolean jsonWriteBoardData( JSONObject j, JSONObject defaultLayout )
    {
        JSONObject boardSize = defaultLayout.getJSONObject( "gridSize" );
        JSONArray  boardGrid = defaultLayout.getJSONArray( "grid" );
        
        int w = boardSize.getInt( "x" );
        int h = boardSize.getInt( "y" );
        
        //
        // We have good data??
        //
        if ( w == 7 && h == 7 )
        {
            JSONArray boardItems = new JSONArray();
            
            for ( int y = 6; y > -1; y-- )
            {
                for ( int x = 0; x < 7; x++ )
                {
                    JSONObject boardItem = boardGrid.getJSONObject( (y * 7) + x );
                    
                    // Test to see if we can find this image
                    */
/**
                     * NOTE: This will check for non special board items ONLY
                     *//*

                    // Create an object to feed a key and value to
                    JSONObject obj    = new JSONObject();
                    long       pathID = boardItem.getLong( "m_PathID" );
                    
                    //#############################
                    //
                    // Zero means active slot, but
                    // the area is empty
                    //
                    //#############################
                    if ( pathID == 0 )
                    {
                        // blocker images
                        obj.put( "blocker_images", dummy );
                        // blocker type
                        obj.put( "blocker_type", -1 );
                        
                        // blocker type = HITS_COUNTER?
                        // Save status
                        obj.put( "isHitsCounter", 0 );
                        obj.put( "hitsCountAmount", 0 );
                        
                        // Write the target amount
                        obj.put( "board_item", "-2" );
                        continue;
                    }
                    
                    //#############################
                    //
                    // Check for a valid entry
                    //
                    //#############################
                    // Will return a BG name. IE: "BG Brk Bush Health 1"
                    String bgName;
                    String text = findByType( pathID );
                    
                    // Get a converted name
                    bgName = findByConvertNumber( text );
                    
                    // If the converted name is valid
                    // continue processing board tile
                    if ( bgName != null )
                    {
                        // blocker images & type
                        obj.put( "blocker_type", -1 );
                        obj.put( "blocker_images", dummy );
                        
                        // blocker type = HITS_COUNTER?
                        // Save status
                        obj.put( "isHitsCounter", 0 );
                        obj.put( "hitsCountAmount", 0 );
                        //
                        obj.put( "board_item", Integer.parseInt( bgName ) );
                    }
                    else
                    {
                        //
                        // See if we can build a blocker from the name
                        //
                        if ( text != null )
                        {
                            if ( text.toUpperCase().contains( " HEALTH " ) )
                            {
                                int                code   = text.toUpperCase().indexOf( " HEALTH " );
                                String             arg    = text.substring( code + 8 ).trim();
                                int                amount = Integer.parseInt( arg );
                                ArrayList<Integer> images = new ArrayList<>();
                                
                                // Fill with dummy images
                                for ( int i = 0; i < amount; i++ )
                                {
                                    images.add( i );
                                }
                                
                                //
                                obj.put( "blocker_type", 1 );
                                obj.put( "blocker_images", images );
                                
                                // blocker type = HITS_COUNTER?
                                // Save status
                                obj.put( "isHitsCounter", 0 );
                                obj.put( "hitsCountAmount", 0 );
                                
                                // Dummy max images count
                                // Fixed in the actual editor
                                obj.put( "board_item", images.size() - 1 );
                            }
                            else
                            {
                                obj.put( "board_item", "Blocker Needed" );
                            }
                        }
                        else
                        {
                            // A blocker will be needed
                            obj.put( "board_item", "NULL" );
                        }
                    }
                    
                    // save object to the array
                    boardItems.put( obj );
                }
            }
            
*/
/*
            for ( int i = 0; i < boardGrid.length(); i++ )
            {
                JSONObject boardItem = boardGrid.getJSONObject( i );
                
                // Test to see if we can find this image
                // NOTE: This will check for non special board items ONLY

                // Create an object to feed a key and value to
                JSONObject obj    = new JSONObject();
                long       pathID = boardItem.getLong( "m_PathID" );
                
                //
                if ( pathID == 0 )
                {
                    // blocker images
                    obj.put( "blocker_images", dummy );
                    // blocker type
                    obj.put( "blocker_type", 0 );
                    
                    // Write the target amount
                    obj.put( "board_item", "Empty" );
                }
                
                //
                // Check for a valid endtry
                //
                String text = findByElementType( pathID );
                
                if ( text != null )
                {
                    // blocker images
                    obj.put( "blocker_images", dummy );
                    // blocker type
                    obj.put( "blocker_type", 0 );
                    
                    // Write the target amount
                    obj.put( "board_item", text );
                }
                else
                {
                    // blocker images
                    obj.put( "blocker_images", dummy );
                    // blocker type
                    obj.put( "blocker_type", 0 );
                    
                    // data not found
                    // NOTE: If thi sreached, use as a blocker
                    // placed in the editor
                    obj.put( "board_item", "Needs Blocker" );
                }
                
                // save object to the array
                boardItems.put( obj );
            }
*//*

            
            // Place the array inside of the
            // Parent Object ( File buffer )
            j.put( "board_items", boardItems );
            
            return true;
        }
        
        return false;
    }
    
    
    */
/**
     * //################################
     * <p>
     * Write the stars Score value to the
     * JSON file in new key / data format
     * <p>
     * //################################
     *
     * @param object data to write
     *//*

    private static void jsonWriteStarsData( JSONObject j, JSONObject object )
    {
        String[]  stars  = new String[]{ "score_max", "score_middle", "score_min" };
        JSONArray amount = new JSONArray();
        JSONArray array  = object.getJSONArray( "starMilestones" );
        
        for ( int i = 0; i < array.length(); i++ )
        {
            JSONObject scoreArray = array.getJSONObject( i );
            int        score      = scoreArray.getInt( "score" );
            
            // Create an object to feed a key and value to
            JSONObject obj = new JSONObject();
            // write to that object
            obj.put( stars[ i ], score );
            // save object to the array
            amount.put( obj );
        }
        
        // Place the array inside of the
        // Parent Object ( File buffer )
        j.put( "star_scores", amount );
    }
    
    
    */
/**
     * //################################
     * <p>
     * Write the stars Score value to the
     * JSON file in new key / data format
     * <p>
     * //################################
     *
     * @param array data to write
     *//*

    private static void jsonWriteGoalsData( JSONObject j, JSONArray array )
    {
        JSONArray targets = new JSONArray();
        
        for ( int i = 0; i < array.length(); i++ )
        {
            JSONObject goalsArray = array.getJSONObject( i );
            int        target     = goalsArray.getInt( "amount" );
            JSONObject element    = goalsArray.getJSONObject( "element" );
            
            // Test to see if we can find this image
            String text    = findByType( element.getLong( "m_PathID" ) );
            String convert = findByConvertNumber( text );
            
            // Create an object to feed a key and value to
            JSONObject obj = new JSONObject();
            
            if ( convert != null )
            {
                // Write the target amount
                obj.put( "target_num", target );
                
                // Write the target amount
                obj.put( "target_img", Integer.parseInt( convert ) );
            }
            else
            {
                // data not found
                obj.put( "target_num", 0 );
                
                if ( text != null )
                {
                    obj.put( "target_img_old", text );
                    obj.put( "target_img", -1 );
                }
                else
                {
                    // Write the target amount
                    obj.put( "target_img", "Blocker Target" );
                }
            }
            
            // save object to the array
            targets.put( obj );
        }
        
        // Place the array inside of the
        // Parent Object ( File buffer )
        j.put( "targets", targets );
    }
    
    
    */
/**
     * //################################
     * <p>
     * Write the stars Score value to the
     * JSON file in new key / data format
     * <p>
     * //################################
     *
     * @param array data to write
     *//*

    private static void jsonWriteSpawnData( JSONObject j, JSONArray array )
    {
        JSONArray spawns = new JSONArray();
        
        for ( int i = 0; i < array.length(); i++ )
        {
            JSONObject spawnItem  = array.getJSONObject( i );
            JSONObject blockGroup = spawnItem.getJSONObject( "blockGroup" );
            
            // Test to see if we can find this image
            String text    = findByType( blockGroup.getLong( "m_PathID" ) );
            String convert = findByConvertNumber( text );
            
            // Create an object to feed a key and value to
            JSONObject obj = new JSONObject();
            
            if ( convert != null )
            {
                // The "BG xxx" name is the same as the filename in "BF_Data\BG". Add .json
                //                long pathID = findByBG( text );
                // Write the target amount
                obj.put( "spawn_img", Integer.parseInt( convert ) );
            }
            else
            {
                if ( text != null )
                {
                    obj.put( "spawn_img_old", text );
                    obj.put( "spawn_img", 0 );
                }
                else
                {
                    // Write the target amount
                    obj.put( "spawn_img", "Blocker Spawn" );
                }
            }
            
            // save object to the array
            spawns.put( obj );
        }
        
        // Place the array inside of the
        // Parent Object ( File buffer )
        j.put( "spawn_tiles", spawns );
    }
    
    
    */
/**
     * //################################
     * <p>
     * Used to locate the elements and
     * retrieve an Icon if it exist
     * <p>
     * //################################
     *
     * @param m_pathID resource ID
     *
     * @return data
     *//*

    private static String findByType( long m_pathID )
    {
        if ( hashMap.get( m_pathID ) != null )
        {
            int listIndex = hashMap.get( m_pathID );
            
            if ( listIndex != 0 )
            {
                return list.get( listIndex ).getFileName();
            }
        }
        
        return null;
    }
    
    
    */
/**
     * //################################
     * <p>
     * Used to locate the elements and
     * retrieve an Icon if it exist
     * <p>
     * //################################
     *
     * @param fileName uses a file path
     *
     * @return pathID to "BT xxxx" file
     *//*

    private static long findByBG( String fileName )
    {
        try
        {
            // Need "blockTypes"
            // Reading JSON Object
            String     inPath     = String.format( Locale.getDefault(), "C:\\AndroidDev\\BF_Data\\BGs\\%s.json", fileName );
            String     content    = new String( Files.readAllBytes( Paths.get( inPath ) ) );
            JSONObject jsonObject = new JSONObject( content );
            JSONArray  blockTypes = jsonObject.getJSONArray( "blockTypes" );
            
            return 0;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return -1;
    }
}
*/
