package com.genesyseast.coinconnection.GameEngine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.GameGraphics.CardsGridLayout;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.GameGraphics.GameBitmaps;
import com.genesyseast.coinconnection.CustomControls.GridLayoutView;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class GameBoard
{
    public        ArrayList<BoardTile>  boardTiles;
    public        int[][]               boardMap;
    //
    private       int                   mapDataIndex;
    private       int                   mapWidth;
    private       int                   mapHeight;
    private       int                   maxTiles;
    private       int                   mapColors;
    //
    public        ConnectionsGridLayout connectionsGrid;
    public        CardsGridLayout       cardsGrid;
    public        GridLayoutView        gridLayoutView;
    //
    public static GameBoard             instance;
    //
    private       GameEngine            gameEngine;
    private       Context               context;
    // Mapping data for the board images
    public        int[]                 gemsPerLevel;
    public        int[]                 xmlMapBgs;
    public        int[]                 xmlMapColors;
    public        int[]                 xmlTargets;
    // Background images for the main playing area
    public        int[]                 xmlBackgrounds;
    //
    public static int[]                 coinBombIcons;
    public static int[]                 coinBombGlow;
    public        int[]                 cardSet;
    public static int[]                 coinSet;
    public static int[]                 coinGlow;
    public static int[]                 coinBg;
    public static int[]                 coinParticles;
    public        int                   maxColors;
    public        int                   maxGems;
    public        int                   maxTargets;
    public        int                   neededPoints;
    public        int                   neededStars;
    public        String                objMessage;
    private       int[][]               test = {
            { 9, 5, 5, 3, 0, 1, 5 }, // 0 - No matches
            { 0, 3, 8, 4, 6, 2, 4 }, // 1
            { 1, 3, 7, 11, 2, 0, 3 },// 2
            { 0, 5, 2, 5, 4, 1, 5 }, // 3
            { 6, 1, 6, 0, 2, 3, 1 }, // 4
            { 3, 2, 4, 5, 0, 4, 2 }, // 5
            { 2, 0, 1, 3, 2, 5, 3 } // 6
    };
    
    //
    public static       ArrayList<TextView> targetViewsList = new ArrayList<>();
    public static final int                 BASE_CARDS      = 4;
    int[] targetIds = {
            R.id.redTarget,
            R.id.greenTarget,
            R.id.blueTarget,
            R.id.orangeTarget,
            R.id.pinkTarget,
            R.id.purpleTarget,
            R.id.yellowTarget,
            R.id.purpleGemTarget,
            R.id.pinkGemTarget,
            R.id.tealGemTarget,
            R.id.yellowGemTarget
    };
    
    
    /**
     * Getters for current map Width, Height, and Max Color
     *
     * @return
     */
    public int getMapWidth()
    {
        return mapWidth;
    }
    
    public int getMapHeight()
    {
        return mapHeight;
    }
    
    public int getMapColors()
    {
        return mapColors;
    }
    
    public int getMaxTiles()
    {
        return maxTiles;
    }
    
    public ArrayList<BoardTile> getBoardTiles()
    {
        return boardTiles;
    }
    
    public int[][] getBoardMap()
    {
        return boardMap;
    }
    
    public int[] getCoinSet()
    {
        return coinSet;
    }
    
    public int[] getCoinGlow()
    {
        return coinGlow;
    }
    
    public int getMaxColors()
    {
        return maxColors;
    }
    
    public int[] getCoinBg()
    {
        return coinBg;
    }
    
    public int[] getCardSet()
    {
        return cardSet;
    }
    
    public int[] getCoinBombIcons()
    {
        return coinBombIcons;
    }
    
    public int[] getCoinBombGlow()
    {
        return coinBombGlow;
    }
    
    /**
     * //####################################
     * <p>
     * No instantiated allowed!
     * <p>
     * //####################################
     */
    private GameBoard( Context context )
    {
        this.context = context;
        gameEngine = GameEngine.getInstance( context );
        
        // Determine the max colors by the current level
        xmlMapColors = context.getResources().getIntArray( R.array.colors_per_level );
        
        // Determine the max coin match targets by the current level
        xmlTargets = context.getResources().getIntArray( R.array.targets_per_level );
        
        
        //############################
        //
        // Map Board images
        //
        //############################
        xmlMapBgs = GameBitmaps.getResourceArray( context, R.array.xmlMapBG );
        
        
        //############################
        //
        // Board icons: Coins
        //
        //############################
        coinSet = GameBitmaps.getResourceArray( context, R.array.coin_icon_list );
        coinGlow = GameBitmaps.getResourceArray( context, R.array.coin_glow_list );
        coinBombIcons = GameBitmaps.getResourceArray( context, R.array.coin_bomb_list );
        coinBombGlow = GameBitmaps.getResourceArray( context, R.array.coin_bomb_glow );
        
        
        //############################
        //
        // Board icons: Particles
        //
        //############################
        coinParticles = GameBitmaps.getResourceArray( context, R.array.coin_particles );
        
        //############################
        //
        // Board icons: Cards
        //
        //############################
        cardSet = GameBitmaps.getResourceArray( context, R.array.card_icon_list );
        
        //############################
        //
        // Game Play field Backgrounds
        //
        //############################
        xmlBackgrounds = GameBitmaps.getResourceArray( context, R.array.game_bgs );
    }
    
    
    /**
     * //####################################
     * <p>
     * Create a single instance only
     * <p>
     * //####################################
     *
     * @return
     */
    public static synchronized GameBoard getInstance( Context context )
    {
        if ( instance == null )
        {
            instance = new GameBoard( context );
        }
        
        return instance;
    }
    
    
    /**
     * //####################################
     * <p>
     * Build the board based on the
     * loaded map data for Connections Game
     * <p>
     * //####################################
     *
     * @param context N/A
     * @param view    N/A
     */
    public void BuildConnectionsBoard( Context context, int currentBoard, @NonNull ArrayList<BoardTile> boardTiles, View view )
    {
        Random    r     = new Random();
        BoardTile tile;
        int[][]   mapData;
        int       index = currentBoard;
        int       level;
        int       maxCoins;
        int       coinCountTotal;
        int       gemCountTotal;
        
        
        //
        // Need the dimensions
        //
        mapWidth = 7;
        mapHeight = 7;
        maxTiles = (mapHeight * mapWidth);
        
        
        // Mapping index for the board
        this.mapDataIndex = currentBoard;
        
        // Add data to the board tiles by filling
        // the ArrayList with Mapping data
        this.boardTiles = boardTiles;
        
        // Place the map in 2D Array for for easier access
        boardMap = new int[ mapHeight ][ mapWidth ];
        boardTiles.clear();
        
        // Get the current level for the current area
        level = GameEngine.currentLevel;
        
        
        //########################################
        // (32 Areas / 4) == 8 advanced starting
        // levels for coin colors
        //########################################
        index = level;
        index += (GameEngine.currentLevel / 4);
        //
        if ( index >= GameEngine.MAX_LEVEL_COLORS )
        {
            // Can't be higher than MAX_LEVEL_COLORS
            index = (GameEngine.MAX_LEVEL_COLORS - 1);
        }
        
        
        //#################################
        //
        // Make it a little harder
        //
        //#################################
        //        int[]    adjust    = context.getResources().getIntArray( R.array.area_adjust );
        String[] adjust = context.getResources().getStringArray( R.array.area_adjust );
        String[] list   = context.getResources().getStringArray( R.array.connections_targets );
        
        String[] targetSet = list[ level % 18 ].split( "," );
        
        // TODO: remove this code. all will be XML
        String[] adjustSet = adjust[ GameEngine.currentLevel % 32 ].split( "," );
        int      min, max, adj, bg_type;
        
        // Coin targets
        min = Integer.parseInt( targetSet[ 0 ].trim() );
        // Jewel targets
        max = Integer.parseInt( targetSet[ 1 ].trim() );
        
        // Area adjustments
        adj = Integer.parseInt( adjustSet[ 0 ].trim() );
        bg_type = Integer.parseInt( adjustSet[ 1 ].trim() );
        
        
        // Value is inclusive
        maxCoins = r.nextInt( min + adj ) + 1;
        
        // Value is inclusive
        maxGems = r.nextInt( max + adj ) + 1;
        
        
        //##############################
        //
        // Do not give gems if not ready
        //
        //##############################
        if ( max == 0 && GameEngine.currentLevel == 0 )
        {
            maxGems = 0;
        }
        
        // Set a universal target count for both types
        if ( maxGems > 4 )
        {
            maxGems = 4;
        }
        
        // TODO : DEBUG ONLY
        maxGems = 0;
        maxCoins = 1;
        
        
        //##############################
        //
        // Get the max colors we can use
        //
        //##############################
        // TODO fix all this!
        maxColors = xmlMapColors[ index % xmlMapColors.length ];
        
        // Sometimes, we don't want gems
        if ( maxCoins > 3 )
        {
            if ( r.nextBoolean() && maxGems >= 4 )
            {
                maxGems = r.nextInt( 4 );
            }
        }
        
        //
        if ( maxGems > 0 )
        {
            if ( maxCoins >= BoardTile.MAX_COINS )
            {
                // Remove the gold coin. Game will be too hard
                maxCoins = BoardTile.MAX_COINS - 1;
            }
            
            //
            if ( maxColors >= BoardTile.MAX_COINS )
            {
                maxColors = BoardTile.MAX_COINS - 1;
            }
        }
        else
        {
            // No gems were loaded
            if ( maxCoins >= BoardTile.MAX_COINS )
            {
                // Remove the gold coin. Game will be too hard
                maxCoins = BoardTile.MAX_COINS;
            }
            
            //
            if ( maxColors >= BoardTile.MAX_COINS )
            {
                maxColors = BoardTile.MAX_COINS;
            }
        }
        
        //
        maxTargets = (maxCoins + maxGems);
        
        // TODO: Make this part of XML or json file
        // DEBUG
/*
        if ( r.nextBoolean() )
        {
            maxTargets = maxGems = 0;
            neededStars = 3;
        }
        else
        {
            neededStars = 0;
        }
*/
        
        
        //############################
        //
        // Max time allowed
        //
        //############################
        TypedArray arrays = context.getResources().obtainTypedArray( R.array.time_allowed );
        
        int[] timeList = new int[ arrays.length() ];
        //
        for ( int i = 0; i < arrays.length(); i++ )
        {
            String value = arrays.getString( i );
            
            if ( value != null )
            {
                timeList[ i ] = ( int ) (Double.parseDouble( value ) * 60000f);
            }
        }
        
        // Add a few seconds for harder levels
        // TODO remove this code. in json
        gameEngine.timeAllowed = timeList[ GameEngine.currentLevel % 18 ];
        //        gameEngine.timeAllowed += (adjust[ gameEngine.loadedArea ] * 15000);
        gameEngine.timeAllowed += (Integer.parseInt( adjustSet[ 0 ] ) * 15000);
        //        gameEngine.timeAllowed = 20000;
        arrays.recycle();
        
        
        //#############################################
        //
        // Build the main board
        //
        //#############################################
        // DEBUG: Create an XML file containing
        // all the mapping data for each level
        //        createMappingXml();
        
        // Get the mapping data set
        mapData = getCoinMapFormat( xmlMapBgs[ currentBoard ], mapWidth, mapHeight );
        
        //
        for ( int y = 0; y < mapHeight; y++ )
        {
            for ( int x = 0; x < mapWidth; x++ )
            {
                // Each entry in each column of the current row
                index = mapData[ y ][ x ];
                
                //
                // VALID Tile type
                //
                if ( index != 0 )
                {
                    // Display the tile immediately
                    tile = new BoardTile( context, BoardTile.STATE_ACTIVE, (y * mapWidth) + x );
                    tile.setScaleType( ImageView.ScaleType.FIT_CENTER );
                    
                    //
                    index = r.nextInt( maxColors );
                    
                    // Assign a color to the new tile
                    tile.tileNum = index;
                    
                    // TODO: Debug only use: Board filler
                    //                    tile.tileNum = test[ y ][ x ];
                    //                    tile.tileNum = 3;
                    
                    tile.setSpecialItem( 0 );
                    boardTiles.add( tile );
                    boardMap[ y ][ x ] = 1;
                    
                    // The value is good
                    continue;
                }
                
                //
                // Default to here: INVALID Tile type
                //
                tile = new BoardTile( context, BoardTile.STATE_INACTIVE, (y * mapWidth) + x );
                tile.tileNum = -1;
                
                tile.setSpecialItem( 0 );
                boardMap[ y ][ x ] = -1;
                
                // Add the processed tile
                boardTiles.add( tile );
            }
        }
        
        
        // TODO: finish creating new BG image engine
        gridLayoutView = view.findViewById( R.id.boardGrid );
        gridLayoutView.setBoardTiles( boardTiles );
        gridLayoutView.setXyGrid( mapData );
        
        
        //
        connectionsGrid = view.findViewById( R.id.boardGridLayout );
        connectionsGrid.removeAllViews();
        connectionsGrid.setMapHeight( mapHeight );
        connectionsGrid.setMapWidth( mapWidth );
        connectionsGrid.setMainView( view );
        
        // TODO : Remove all of debug tile testing data for specials
///*
//        //
//        boardTiles.get( 17 ).tileNum = BoardTile.RAINBOW_COIN;
//        boardTiles.get( 17 ).setSpecialItem( BoardTile.RAINBOW_SEEKER );
//*/
//
///*
//        boardTiles.get( 13 ).setBlockerType( BoardTile.CHEST_BLOCKER, new int[]{ 0, 1 } );
//
//        boardTiles.get( 11 ).setBlockerType( BoardTile.CHEST_BLOCKER, new int[]{ 0, 2 } );
//
//
//        boardTiles.get( 27 ).setBlockerType( BoardTile.ROCK_BLOCKER );
//        //      boardTiles.get( 27 ).setBlockerImages( new int[]{ R.drawable.rock_blocker } );
//
//        boardTiles.get( 30 ).setBlockerType( BoardTile.BARREL_BLOCKER, new int[]{ 0, 1 } );
//*/
//        //        boardTiles.get( 30 ).setBlockerImages( new int[]{ R.drawable.shell_blocker } );
//
//        //        boardTiles.get( 7 ).setBlockerType( BoardTile.ICE_BLOCKER, new int[]{ 0, 1, 2 } );
//
//
//
///*
//        int[] coins = GameEngine.arrayFromResource( context, R.array.coin_stack );
//        // To fix bugs
//        for ( int i = 21; i < 28; i += 2 )
//        {
//            boardTiles.get( i ).setBlockerType( BoardTile.BARREL_BLOCKER, new int[]{ 0, 1 } );
////            boardTiles.get( i ).setBlockerType( BoardTile.ICE_BLOCKER );
//
//                //                boardTiles.get( i ).setBlockerImages( new int[]{ R.drawable.chest_blocker_closed, R.drawable.chest_blocker_full } );
//        }
//*/
//
//
//
///*
//        boardTiles.get( 3 ).setSpecialItem( BoardTile.SWEEPER_COIN_HORIZONTAL );
//        boardTiles.get( 3 ).tileNum = 3;
//        boardTiles.get( 10 ).setSpecialItem( BoardTile.SWEEPER_COIN_VERTICAL );
//        boardTiles.get( 10 ).tileNum = 3;
//
//        boardTiles.get( 33 ).setBlockerType( BoardTile.GLASS_PANEL_BLOCKER );
//        boardTiles.get( 33 ).setBlockerImages( new int[]{ R.drawable.blocker_glass_1, R.drawable.blocker_glass_2, R.drawable.blocker_glass_3, R.drawable.blocker_glass_4 } );
//
//
//        boardTiles.get( 16 ).tileNum = BoardTile.RAINBOW_COIN;
//        boardTiles.get( 16 ).specialItem = BoardTile.BEAM_SEEKER;
//
//        boardTiles.get( 26 ).tileNum = BoardTile.RAINBOW_COIN;
//        boardTiles.get( 26 ).specialItem = BoardTile.BEAM_SEEKER;
//
//        boardTiles.get( 3 ).setBlockerType( BoardTile.CHEST_BLOCKER );
//        boardTiles.get( 3 ).setBlockerImages( new int[]{ R.drawable.chest_blocker_closed, R.drawable.chest_blocker_full } );
//
//        boardTiles.get( 13 ).setBlockerType( BoardTile.CHEST_BLOCKER );
//        boardTiles.get( 13 ).setBlockerImages( new int[]{ R.drawable.chest_blocker_closed, R.drawable.chest_blocker_empty } );
//
//        boardTiles.get( 8 ).tileNum = 3;
//        boardTiles.get( 8 ).specialItem = BoardTile.SWEEPER_COIN_VERTICAL;
//
//        boardTiles.get( 20 ).tileNum = 3;
//        boardTiles.get( 20 ).specialItem = BoardTile.SWEEPER_COIN_HORIZONTAL;
//
//        boardTiles.get( 5 ).tileNum = 3;
//        boardTiles.get( 13 ).tileNum = 3;
//
//        boardTiles.get( 6 ).setBlockerType( BoardTile.COIN_STACK );
//        boardTiles.get( 6 ).setBlockerImages( new int[]{ R.drawable.coin_stack_gold, R.drawable.coin_stack_green, R.drawable.coin_stack_pink } );
//
//        boardTiles.get( 27 ).setBlockerType( BoardTile.ROCK_BLOCKER );
//        boardTiles.get( 27 ).setBlockerImages( new int[]{ R.drawable.rock_blocker } );
//
//        boardTiles.get( 30 ).setBlockerType( BoardTile.SHELL_BLOCKER );
//        boardTiles.get( 30 ).setBlockerImages( new int[]{ R.drawable.shell_blocker } );
//
//        boardTiles.get( 23 ).setBlockerType( BoardTile.ICE_BLOCKER );
//        boardTiles.get( 23 ).setBlockerImages( new int[]{ R.drawable.ice_1, R.drawable.ice_2, R.drawable.ice_3 } );
//*/



/*
        ###################################
         IMPORTANT!!!!!!
         Make sure we have at least a
         single match!
         NEVER include specials here
        ###################################
*/
        boolean valid = connectionsGrid.checkIfMatchesExist( true );
        if ( !valid )
        {
            connectionsGrid.useShuffler();
        }
        
        
        //##################################
        //
        // Set up the "Target" coins
        //
        //##################################
        ViewGroup group = view.findViewById( R.id.boardTargetHolder );
        
        // Clear this list of targets
        targetViewsList.clear();
        
        for ( int targetId : targetIds )
        {
            View v = group.findViewById( targetId );
            
            if ( v != null )
            {
                (( ImageTextView ) v).setImageResource( 0 );
                targetViewsList.add( ( TextView ) v );
                v.setVisibility( View.GONE );
                v.setTag( null );
            }
        }
        
        
        //####################################
        //
        // Choose the colors we wish to use
        //
        //####################################
        ArrayList<Integer> coinsList = new ArrayList<>();
        
        //
        coinsList.clear();
        // If Less than what we will display
        // then randomly select colors to target
        if ( maxCoins < maxColors )
        {
            int color;
            
            while ( coinsList.size() < maxCoins )
            {
                color = r.nextInt( maxColors );
                if ( coinsList.contains( color ) )
                {
                    continue;
                }
                
                // Add it to the buffer
                coinsList.add( color );
            }
        }
        else
        {
            // Add all colors to the list
            for ( int i = 0; i < maxColors; i++ )
            {
                coinsList.add( i );
            }
        }
        
        
        //####################################
        //
        // Set status of each target and the
        // amount needed for completion
        //
        //####################################
        int count;
        coinCountTotal = 0;
        
        if ( neededStars == 0 )
        {
            // ONLY if we are not solving for stars
            for ( int i = 0; i < coinsList.size(); i++ )
            {
                TextView v     = targetViewsList.get( coinsList.get( i ) );
                PointXYZ point = new PointXYZ();
                
                //            count = r.nextInt( 10 + (gameEngine.loadedArea / 2) ) + xmlTargets[ index ];
                count = r.nextInt( 10 + (GameEngine.currentLevel * 2) ) + xmlTargets[ index ];
                
                if ( GameEngine.debugMode )
                {
                    count = 1;
                }
                
                // Use this to set a points requirement number
                coinCountTotal += count;
                
                // Count
                point.x = count;
                // Index into Targets Array
                point.y = coinsList.get( i );
                //
                v.setTag( point );
                v.setVisibility( View.INVISIBLE );
                v.setText( String.format( Locale.getDefault(), "%d", count ) );
            }
        }
        
        
        //####################################
        //
        // Because "Specials" follow, Gems
        // must be added here
        //
        //####################################
        gemCountTotal = 0;
        if ( maxGems > 0 )
        {
            coinsList.clear();
            coinsList = connectionsGrid.findGemSlot( maxGems );
            
            for ( int i = 0; i < coinsList.size(); i++ )
            {
                //CRASHING due to indexing of Gems n the list is now wrong
                TextView v     = targetViewsList.get( coinsList.get( i ) + BoardTile.PURPLE_GEM );
                PointXYZ point = new PointXYZ();
                
                //                count = r.nextInt( 1 + adjust[ gameEngine.loadedArea ] ) + 1;
                count = r.nextInt( 1 + Integer.parseInt( adjustSet[ 0 ] ) ) + 1;
                
                if ( GameEngine.debugMode )
                {
                    count = 2;
                }
                
                // Gems per slot
                gemCountTotal += count;
                
                // Count
                point.x = count;
                // Index into Targets Array
                //                point.y = (coinsList.get( i ) - BoardTile.MAX_COINS) + BoardTile.RED_GEM;
                point.y = (coinsList.get( i ) + BoardTile.PURPLE_GEM);
                
                //
                v.setTag( point );
                v.setVisibility( View.INVISIBLE );
                v.setText( String.format( Locale.getDefault(), "%d", count ) );
            }
        }
        
        
        //#################################
        //
        // Select the Objective Message
        //
        //#################################
        
        // Should we add point requirement
        String[] messages  = context.getResources().getStringArray( R.array.connections_messages );
        String[] panelsStr = new String[]{ "Remove back panels", "Remove all gem bags;", "Remove all chests;", "Remove all chests and gem bags;" };
        String   barf;
        boolean  pointsReq = r.nextBoolean();
        boolean  panelsReq = false;
        
        
/*
        GameEngine.penaltyMode = false;
        if ( maxCoins > 1 )
        {
            // Can't do penalty with only one coin
            // Do we want a penalty mode?
            GameEngine.penaltyMode = (r.nextInt( 3 ) == 1);
            if ( GameEngine.penaltyMode )
            {   // Never require points with penalty mode!
                pointsReq = false;
            }
        }
*/
        
        //
/*
        // Build the string. Include back panels
        if ( bg_type > 0 )
        {
            panelsReq = r.nextBoolean();
            
            // Disable for now
            panelsReq = false;
        }
*/
        //
        bg_type = -1;
        
        //
        // Coins only??
        if ( maxGems == 0 && coinCountTotal > 0 )
        {
            if ( !pointsReq )
            {
                neededPoints = 0;
                objMessage = String.format( Locale.getDefault(), messages[ 0 ], maxCoins, "" );
            }
            else
            {
                // Minimum 3 per match!
                coinCountTotal *= 2;
                neededPoints = (coinCountTotal * GameEngine.POINTS_PER_TILE);
                objMessage = String.format( Locale.getDefault(), messages[ 1 ], maxCoins, neededPoints, "" );
            }
        }
        else if ( maxGems > 0 )
        {
            if ( !pointsReq )
            {
                neededPoints = 0;
                objMessage = String.format( Locale.getDefault(), messages[ 2 ], maxCoins, maxGems, "" );
            }
            else
            {
                // Minimum 3 per match!
                coinCountTotal *= 2;
                neededPoints = (coinCountTotal * GameEngine.POINTS_PER_TILE);
                neededPoints += (gemCountTotal * GameEngine.POINTS_PER_GEM);
                //
                objMessage = String.format( Locale.getDefault(), messages[ 3 ], maxCoins, neededPoints, maxGems, "" );
            }
        }
        else if ( neededStars > 0 )
        {
            neededPoints = 0;
            objMessage = messages[ 4 ];
        }
        
        //
        // If it's a HARDER level, then inform the player of it
        //
/*
        if ( (level + 1) == 16 )
        {
            objMessage = messages[ 4 ] + objMessage;
        }
*/
        
        
        //############################
        //
        // Update the grid
        //
        //############################
        connectionsGrid.onDrawGrid();
    }
    
    
    private void createMappingXml()
    {
        if ( context != null )
        {
            SharedPreferences        prefs;
            SharedPreferences.Editor editor;
            StringBuilder            board = new StringBuilder();
            int[][]                  mapData;
            
            //
            prefs = context.getSharedPreferences( "level_mapping", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            for ( int i = 0; i < xmlMapBgs.length; i++ )
            {
                mapData = getCoinMapFormat( xmlMapBgs[ i ], mapWidth, mapHeight );
                
                board.delete( 0, board.length() );
                
                
                // Export the data
                for ( int y = 0; y < mapHeight; y++ )
                {
                    for ( int x = 0; x < mapWidth; x++ )
                    {
                        if ( ((y * mapWidth) + x + 1) < (mapWidth * mapWidth) )
                        {
                            board.append( mapData[ y ][ x ] );
                            board.append( "," );
                        }
                        else
                        {
                            board.append( mapData[ y ][ x ] );
                        }
                    }
                }
                
                // Save the mapping data
                editor.putString( String.format( Locale.US, "level_%02d", i ), board.toString() );
            }
            
            //
            editor.commit();
        }
    }
    
    
    /**
     * //####################################
     * <p>
     * Build the board based on the
     * loaded map data for Cards Game
     * <p>
     * //####################################
     *
     * @param context N/A
     * @param view    N/A
     */
    public void BuildCardsBoard( Context context, int currentBoard, ArrayList<BoardTile> boardTiles, View view )
    {
        Random     r        = new Random();
        BoardTile  tile;
        int[][]    mapData;
        int        index    = currentBoard;
        int        level;
        TypedArray arrays;
        String[]   messages = context.getResources().getStringArray( R.array.cards_messages );
        
        
        //#################################
        //
        // Need the dimensions
        //
        //#################################
        mapWidth = 8;
        mapHeight = 8;
        maxTiles = (mapHeight * mapWidth);
        neededPoints = 0;
        
        // Add data to the board tiles by filling
        // the ArrayList with Mapping data
        this.boardTiles = boardTiles;
        
        
        // Place the map in 2D Array for for easier access
        boardMap = new int[ mapHeight ][ mapWidth ];
        boardTiles.clear();
        
        
        // Get the current level for the current area
        level = GameEngine.currentLevel;
        if ( level < 0 )
        {
            level = 0;
        }
        
        
        //########################################
        //
        // (32 Areas / 4) == 8 advanced starting
        // levels for coin colors
        //
        //########################################
        index = level;
        index += (GameEngine.currentLevel / 4);
        //
        if ( index >= GameEngine.MAX_LEVEL_COLORS )
        {
            // Can't be higher than MAX_LEVEL_COLORS
            index = (GameEngine.MAX_LEVEL_COLORS - 1);
        }
        
        
        //#################################
        //
        // Make it a little harder
        //
        //#################################
        String[] adjust    = context.getResources().getStringArray( R.array.area_adjust );
        String[] list      = context.getResources().getStringArray( R.array.cards_targets );
        String[] targetSet = list[ level ].split( "," );
        String[] adjustSet = adjust[ GameEngine.currentLevel ].split( "," );
        
        int min, max;
        
        // Coin targets
        max = Integer.parseInt( targetSet[ 0 ].trim() );
        maxColors = r.nextInt( max + Integer.parseInt( adjustSet[ 0 ] ) ) + 1;
        
        // Gem targets
        max = Integer.parseInt( targetSet[ 1 ].trim() );
        maxGems = r.nextInt( max + Integer.parseInt( adjustSet[ 0 ] ) ) + 1;
        
        //
        if ( maxColors > BoardTile.MAX_COINS )
        {
            maxColors = BoardTile.MAX_COINS;
        }
        //
        if ( maxGems > 4 )
        {
            maxGems = 4;
        }
        
        
        // Sometimes, we don't want gems
        if ( maxColors > 3 )
        {
            if ( r.nextBoolean() && maxGems >= 4 )
            {
                maxGems = r.nextInt( 4 );
            }
        }
        
        // Set a universal target count for both types
        maxTargets = (maxColors + maxGems);
        
        
        //#############################################
        //
        // Build the main board
        //
        //#############################################
        // Get the mapping data set
        arrays = context.getResources().obtainTypedArray( R.array.cards_map_data );
        TypedArray mapSet = context.getResources().obtainTypedArray( arrays.getResourceId( (currentBoard % arrays.length()), R.array.square_map ) );
        
        mapData = new int[ mapHeight ][ mapWidth ];
        
        //
        for ( int i = 0; i < mapSet.length(); i++ )
        {
            String value = mapSet.getString( i );
            
            if ( value != null )
            {
                String[] items = value.split( "," );
                
                for ( int x = 0; x < items.length; x++ )
                {
                    mapData[ i ][ x ] = Integer.parseInt( items[ x ] );
                }
            }
        }
        
        // Code clean up
        arrays.recycle();
        mapSet.recycle();
        
        //##################################
        //
        // Available tiles need to be
        // assigned
        //
        //##################################
        int           availableTiles = 0;
        List<Integer> cardList       = new ArrayList<>();
        
        for ( int y = 0; y < mapHeight; y++ )
        {
            for ( int x = 0; x < mapWidth; x++ )
            {
                // Each entry in each column of the current row
                index = mapData[ y ][ x ];
                
                //
                // VALID Tile type
                //
                if ( index != 0 )
                {
                    boardMap[ y ][ x ] = 1;
                    
                    availableTiles++;
                }
                else
                {
                    boardMap[ y ][ x ] = -1;
                }
            }
        }
        
        
        //###################################
        //
        // Do we have more targets than
        // available tiles
        //
        //###################################
        if ( availableTiles < (maxTargets * 2) )
        {
            while ( availableTiles < (maxTargets * 2) && maxTargets > 1 )
            {
                if ( maxColors >= maxGems )
                {
                    if ( maxColors > 0 )
                    {
                        maxColors--;
                    }
                }
                else
                {
                    if ( maxGems > 0 )
                    {
                        maxTargets--;
                    }
                }
                
                maxTargets = (maxColors + maxGems);
            }
        }
        
        //
        // Create random tiles
        int           colors  = 0;
        int           jewels  = 0;
        List<Integer> targets = new ArrayList<>();
        
        //
        for ( int i = 0; i < maxColors; i++ )
        {
            // Base cards "?", "Sad face", Hint card, ???-Card
            // Skip those
            cardList.add( BASE_CARDS + colors );
            cardList.add( BASE_CARDS + colors );
            
            //
            targets.add( colors );
            colors++;
        }
        
        //
        for ( int i = 0; i < maxGems; i++ )
        {
            // Base cards "?" and "Sad face"
            // Skip those
            cardList.add( BASE_CARDS + jewels + BoardTile.MAX_COINS );
            cardList.add( BASE_CARDS + jewels + BoardTile.MAX_COINS );
            
            //
            targets.add( jewels + BoardTile.MAX_COINS );
            jewels++;
        }
        
        
        // If we are giving a hint card to add hints
        if ( r.nextBoolean() )
        {
            // Hint card: Adds 1
            cardList.add( 2 );
            cardList.add( 2 );
        }
        
        // If we are giving a moves card to add moves
        if ( r.nextBoolean() )
        {
            // Move card: Adds 1 + what was used
            cardList.add( 3 );
            cardList.add( 3 );
        }
        
        //
        for ( int i = cardList.size(); i < availableTiles; i += 2 )
        {
            // Base cards "?", "Sad face", Hint, and ???-Card
            // We want to use sad faces for all unused tiles
            cardList.add( 1 );
            cardList.add( 1 );
        }
        
        //
        // Now shuffle the list and then place them
        //
        for ( int i = 0; i < 4; i++ )
        {
            Collections.shuffle( cardList );
            Collections.shuffle( cardList );
        }
        
        
        //#################################
        //
        // Build the map
        //
        //#################################
        int currentCard = 0;
        
        for ( int y = 0; y < mapHeight; y++ )
        {
            for ( int x = 0; x < mapWidth; x++ )
            {
                // Each entry in each column of the current row
                index = mapData[ y ][ x ];
                
                //
                // VALID Tile type
                //
                if ( index != 0 )
                {
                    // Display the tile immediately
                    tile = new BoardTile( context, BoardTile.STATE_ACTIVE, (y * mapWidth) + x );
                    tile.setScaleType( ImageView.ScaleType.FIT_CENTER );
                    
                    // Question mark box
                    tile.tileNum = cardSet[ 0 ];
                    // Array indexing. Subtract 2 for "?" and ":(" cards
                    tile.specialTile = cardList.get( currentCard );
                    tile.setSpecialItem( -1 );
                    // Flipped card images
                    tile.card[ 0 ] = cardSet[ 0 ];
                    tile.card[ 1 ] = cardSet[ cardList.get( currentCard ) ];
                    
                    //
                    if ( GameEngine.debugMode && tile.specialTile > 1 )
                    {
                        tile.card[ 0 ] = R.drawable.happy_face_emoji;
                        tile.tileNum = tile.card[ 0 ];
                    }
                    
                    //
                    if ( GameEngine.debugMode && (tile.card[ 1 ] == R.drawable.card_hint_glass_gold || tile.card[ 1 ] == R.drawable.card_free_moves_gold) )
                    {
                        tile.card[ 0 ] = R.drawable.sad_face_emoji;
                        tile.tileNum = tile.card[ 0 ];
                    }
                    
                    
                    currentCard++;
                    //
                    boardTiles.add( tile );
                    boardMap[ y ][ x ] = 1;
                    
                    // The value is good
                    continue;
                }
                
                //
                // Default to here: INVALID Tile type
                //
                tile = new BoardTile( context, BoardTile.STATE_INACTIVE, (y * mapWidth) + x );
                tile.tileNum = -1;
                tile.specialTile = -1;
                tile.setSpecialItem( 1 );
                tile.card[ 0 ] = -1;
                tile.card[ 1 ] = -1;
                //
                boardMap[ y ][ x ] = -1;
                
                // Add the processed tile
                boardTiles.add( tile );
            }
        }
        
        
        //
        // Send out a few messages
        //
        cardsGrid = view.findViewById( R.id.boardGridLayout );
        cardsGrid.removeAllViews();
        cardsGrid.setMapHeight( mapHeight );
        cardsGrid.setMapWidth( mapWidth );
        cardsGrid.setView_main( view );
        
        
        //##################################
        //
        // Set up the "Target" coins
        //
        //##################################
        ViewGroup group = view.findViewById( R.id.boardTargetHolder );
        int       count = group.getChildCount();
        
        // Clear this list of targets
        targetViewsList.clear();
        
        for ( int i = 0; i < count; i++ )
        {
            TextView v = ( TextView ) group.getChildAt( i );
            
            if ( v != null && v.getId() != R.id.targetText )
            {
                targetViewsList.add( v );
                v.setVisibility( View.GONE );
            }
        }
        
        
        //####################################
        //
        // Set status of each target and the
        // amount needed for completion
        //
        //####################################
        for ( int i = 0; i < targets.size(); i++ )
        {
            int c = targets.get( i );
            
            TextView v     = targetViewsList.get( c );
            PointXYZ point = new PointXYZ();
            
            count = 1;
            
            //
            point.x = count;
            v.setTag( point );
            v.setVisibility( View.INVISIBLE );
            v.setText( String.format( Locale.getDefault(), "%2d", count ) );
        }
        
        
        //#####################################
        //
        //
        //
        //#####################################
        int moveCount = 0;
        for ( BoardTile tiles : boardTiles )
        {
            if ( tiles.tileNum != -1 )
            {
                moveCount++;
            }
        }
        
        //
        // Time calculations used to make moves per level
        // a little more complex
        int[] timeDeduct = { 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
        //        int[] timeDeduct = { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
        
        // Set the maximum move amount for this level
        GameEngine.boardMoves = (moveCount - (moveCount / timeDeduct[ level ]));
        
        
        //#################################
        //
        // Select the Objective Message
        //
        //#################################
        if ( maxGems > 0 && maxColors > 0 )
        {
            objMessage = String.format( Locale.getDefault(), messages[ 0 ], GameEngine.boardMoves, maxColors, maxGems );
        }
        else if ( maxColors == 0 && maxGems > 0 )
        {
            objMessage = String.format( Locale.getDefault(), messages[ 1 ], GameEngine.boardMoves, maxGems );
        }
        else if ( maxColors > 0 && maxGems == 0 )
        {
            objMessage = String.format( Locale.getDefault(), messages[ 2 ], GameEngine.boardMoves, maxColors );
        }
        
        
        //############################
        //
        // Update the grid
        //
        //############################
        cardsGrid.onDrawGrid();
    }
    
    
    /**
     * //####################################
     * <p>
     * Prevent start game generated matches
     * When checking game matches, be sure to check both
     * game pieces being swapped
     * <p>
     * //####################################
     *
     * @param columnX   N/A
     * @param rowY      N/A
     * @param boardTile N/A
     *
     * @return N/A
     */
    public boolean DuplicateAt( int columnX, int rowY, int boardTile )
    {
        // Test to the left
        if ( columnX > 1 )
        {
            if ( boardTiles.get( (rowY * mapWidth) + (columnX - 1) ).tileNum == boardTile && boardTiles.get( (rowY * mapWidth) + (columnX - 2) ).tileNum == boardTile )
            {
                return true;
            }
        }
        
        // Test to the right
        if ( (columnX + 2) < mapWidth )
        {
            if ( ((rowY * mapWidth) + (columnX + 2)) < boardTiles.size() )
            {
                if ( boardTiles.get( (rowY * mapWidth) + (columnX + 1) ).tileNum == boardTile && boardTiles.get( (rowY * mapWidth) + (columnX + 2) ).tileNum == boardTile )
                {
                    return true;
                }
            }
        }
        
        // Test up
        if ( rowY > 1 )
        {
            
            if ( boardTiles.get( ((rowY - 1) * mapWidth) + columnX ).tileNum == boardTile && boardTiles.get( ((rowY - 2) * mapWidth) + columnX ).tileNum == boardTile )
            {
                return true;
            }
        }
        
        // Test down
        if ( (rowY + 2) < mapHeight )
        {
            
            if ( (((rowY + 2) * mapWidth) + columnX) < boardTiles.size() )
            {
                if ( boardTiles.get( ((rowY + 1) * mapWidth) + columnX ).tileNum == boardTile && boardTiles.get( ((rowY + 2) * mapWidth) + columnX ).tileNum == boardTile )
                {
                    return true;
                }
            }
        }
        
        
        return false;
    }
    
    
    /**
     * //####################################
     * <p>
     * Return a pixel from an image view at x/y location
     * <p>
     * //####################################
     *
     * @param resID
     * @param gridX
     * @param gridY
     *
     * @return
     */
    private int[][] getCoinMapFormat( int resID, int gridX, int gridY )
    {
        Bitmap  loadBmp;
        int     width;
        int     height;
        int     pixel  = 0;
        int[][] mapper = new int[ gridY ][ gridX ];
        
        
        // TODO: To be loaded with xml data
        //  now that  "drop" shifts left and right and
        //  does not allow dropping through DISABLED
        //  tiles. Must map areas where portals and generators are
        // Try to detect the mapping
        try
        {
            loadBmp = BitmapFactory.decodeResource( context.getResources(), resID );
            height = (loadBmp.getHeight() / gridY);
            width = (loadBmp.getWidth() / gridX);
            
            // Split the image into tile sized sections if possible
            //            bitmaps = Bitmap.createBitmap( loadBmp, (x * width), (y * height), width, height );
            
            for ( int y = 0; y < gridY; y++ )
            {
                for ( int x = 0; x < gridX; x++ )
                {
                    pixel = loadBmp.getPixel( (x * width) + (width / 2), (y * height) + (height / 2) );
                    
                    // Inactive tile
                    if ( (pixel & 0xFF000000) == 0 )
                    {
                        mapper[ y ][ x ] = 0;
                    }
                    else
                    {
                        mapper[ y ][ x ] = 1;
                    }
                }
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        
        return mapper;
    }
    
    
    /**
     * //####################################
     * <p>
     * Choose a board image to use
     * <p>
     * //####################################
     *
     * @param context
     *
     * @return
     */
    public static int getBoardImage( Context context, int currentBoardImage, boolean noDuplicate )
    {
        TypedArray   arrays = context.getResources().obtainTypedArray( R.array.xmlMapBG );
        SecureRandom r      = new SecureRandom();
        int          adjust;
        // TODO fix this mess. Use XML
        int level  = GameEngine.currentLevel;
        int length = arrays.length();
        
        
        // We don't need anymore
        arrays.recycle();
        
        // Get the range that we can use
        if ( level > 7 && level < 16 )
        {
            // Using levels 9 to 16, start at images 18 - 35
            adjust = 18;
        }
        // Gold border, final board image for this area
        else if ( level == 16 )
        {
            // Using final level. There are 36 lower levels images
            // I made 32 Final Board images for 32 Areas
            // MOD the AREA value with 32 to not cause a crash
            return 36;
        }
        else
        {
            // Using levels 1 to 8, start at images 0 to 17
            adjust = 0;
        }
        
        
        return r.nextInt( 18 ) + adjust;
    }
    
    
    /**
     * //####################################
     * <p>
     * Test all board tiles for this tileNum
     * <p>
     * //####################################
     *
     * @param tileNum
     *
     * @return
     */
    public boolean hasBoardTile( int tileNum )
    {
        for ( BoardTile tile : boardTiles )
        {
            if ( tile.tileNum == tileNum )
            {
                return true;
            }
        }
        
        return false;
    }
}
