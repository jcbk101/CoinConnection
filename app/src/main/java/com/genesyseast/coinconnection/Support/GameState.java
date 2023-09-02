package com.genesyseast.coinconnection.Support;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.Fragments.CardsFragment;
import com.genesyseast.coinconnection.Fragments.ConnectionsFragment;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.util.ArrayList;
import java.util.Locale;

public class GameState
{
    private static GameEngine gameEngine;
    private static GameBoard  gameBoard;
    private static int[]      targetIds = {
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
     * Check if we have a save state
     *
     * @param context
     *
     * @return
     */
    public static boolean getSaveStatus( Context context )
    {
        if ( context != null )
        {
            SharedPreferences prefs = context.getSharedPreferences( "game_state", Context.MODE_PRIVATE );
            
            return prefs.getBoolean( "save_exist", false );
        }
        else
        {
            return false;
        }
    }
    
    
    /**
     * /#######################################
     * <p>
     * Detect the same game format
     * <p>
     * //#######################################
     */
    public static int getSaveGameType( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( "game_state", Context.MODE_PRIVATE );
        
        return prefs.getInt( "game_type", 0 );
    }
    
    
    /**
     * //##################################
     * <p>
     * Save all game data to the Shared
     * Preferences
     * <p>
     * //##################################
     */
    public static void saveGameState( Context context, int gameType, View view )
    {
        if ( context != null )
        {
            SharedPreferences        prefs;
            SharedPreferences.Editor editor;
            StringBuilder            board   = new StringBuilder();
            StringBuilder            blocker = new StringBuilder();
            
            //
            prefs = context.getSharedPreferences( "game_state", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            gameBoard = GameBoard.getInstance( context );
            gameEngine = GameEngine.getInstance( context );
            
            
            //################################
            //
            //
            //
            //################################
            editor.putBoolean( "save_exist", true );
            //
            editor.putLong( "current_time", CustomTimer.currentTime );
            editor.putInt( "current_bgm", gameEngine.currentBGMusic );
            editor.putInt( "current_bg", GameEngine.currentBackground );
            editor.putInt( "current_board", GameEngine.currentBoardImage );
            editor.putInt( "current_moves", GameEngine.boardMoves );
            editor.putInt( "current_level", GameEngine.currentLevel );
            editor.putInt( "reloaded_level", GameEngine.reloadedLevel );
            
            
            //##################################
            //
            // Matching Card Game
            //
            //##################################
            editor.putInt( "game_type", gameType );
            
            if ( gameType == 1 )
            {
                editor.putInt( "card_hints_left", CardsFragment.cardHintCount );
                editor.putInt( "current_score", CardsFragment.levelScore );
                editor.putInt( "coin_count", gameBoard.maxColors );
                editor.putInt( "jewel_count", gameBoard.maxGems );
                
                // Board layout
                for ( BoardTile tile : gameBoard.boardTiles )
                {
                    int value;
                    
                    value = tile.getState();
                    value |= (tile.specialTile & 0xFF) << 8;
                    
                    board.append( value );
                    
                    if ( tile.getPosition() < (gameBoard.boardTiles.size() - 1) )
                    {
                        board.append( "," );
                    }
                }
                
                // Save the board tiles
                editor.putString( "board_tiles", board.toString() );
                board.setLength( 0 );
                
                
                //##################################
                //
                // Get the views for the current
                // scene
                //
                //##################################
                ViewGroup group = view.findViewById( R.id.boardTargetHolder );
                
                for ( int i = 0; i < targetIds.length; i++ )
                {
                    View v = group.findViewById( targetIds[ i ] );
                    
                    if ( v != null )
                    {
                        if ( v.getTag() != null )
                        {
                            PointXYZ point = ( PointXYZ ) v.getTag();
                            board.append( point.x );
                        }
                        else
                        {
                            board.append( "-1" );
                        }
                        
                        if ( i < (targetIds.length - 1) )
                        {
                            board.append( "," );
                        }
                    }
                }
                
                // Save the board targets
            }
            //##############################################
            //
            // Standard connection game
            //
            //##############################################
            else
            {
                editor.putInt( "current_score", ConnectionsFragment.levelScore );
                editor.putInt( "coin_count", gameBoard.maxColors );
                editor.putInt( "jewel_count", gameBoard.maxGems );
                
                // Board layout
                for ( BoardTile tile : gameBoard.boardTiles )
                {
                    board.append( tile.tileNum );
                    board.append( "," );
                    board.append( tile.getSpecialItem() );
                    
                    if ( tile.getPosition() < (gameBoard.boardTiles.size() - 1) )
                    {
                        board.append( "," );
                    }
                    
                    //#################################
                    //
                    // Have to support Blocker data
                    // save and load
                    //
                    //#################################
                    int blockerType = tile.getBlockerType();
                    
                    if ( blockerType > 0 )
                    {
                        // Set the tile that is a blocker
                        blocker.append( tile.getPosition() );
                        blocker.append( "," );
                        // Set the tile blocker type
                        blocker.append( tile.getBlockerType() );
                        blocker.append( "," );
                        
                        // Get the blocker image indexes
                        if ( blockerType == BoardTile.COIN_STACK || blockerType == BoardTile.ICE_BLOCKER || blockerType == BoardTile.CHEST_BLOCKER || blockerType == BoardTile.BARREL_BLOCKER )
                        {
                            // Set the tile blocker image count for reading purposes
                            blocker.append( tile.blockerImages.size() );
                            blocker.append( "," );
                            
                            
                            for ( int i = 0; i < tile.blockerImages.size(); i++ )
                            {
                                // Get the blocker image index
                                blocker.append( tile.getBlockerIndex( tile.blockerImages.get( i ).id ) );
                                blocker.append( "," );
                            }
                        }
                        else
                        {
                            // Rock, Shell, all other singles
                            // Set the tile that is a blocker
                            blocker.append( tile.getPosition() );
                            blocker.append( "," );
                            // Set the tile blocker type
                            blocker.append( tile.getBlockerType() );
                            blocker.append( "," );
                            // Set the tile blocker image count for reading purposes
                            blocker.append( 0 );
                            blocker.append( "," );
                        }
                    }
                }
                
                // Save the board tiles
                editor.putString( "board_tiles", board.toString() );
                board.setLength( 0 );
                
                // Blockers if the exist
                if ( blocker.length() > 0 )
                {
                    if ( blocker.charAt( blocker.length() - 1 ) == ',' )
                    {
                        blocker.deleteCharAt( blocker.length() - 1 );
                    }
                    
                    editor.putString( "blocker_data", blocker.toString() );
                    blocker = null;
                }
                
                
                //##################################
                //
                // Get the views for the current
                // scene
                //
                //##################################
                ViewGroup group = view.findViewById( R.id.boardTargetHolder );
                
                for ( int i = 0; i < targetIds.length; i++ )
                {
                    View v = group.findViewById( targetIds[ i ] );
                    
                    if ( v != null )
                    {
                        if ( v.getTag() != null )
                        {
                            PointXYZ point = ( PointXYZ ) v.getTag();
                            
                            board.append( point.x );
                            board.append( "," );
                            
                            // Target Index
                            board.append( point.y );
                        }
                        else
                        {
                            board.append( "-1" );
                            board.append( "," );
                            
                            // Target Index
                            board.append( "-1" );
                        }
                        
                        if ( i < (targetIds.length - 1) )
                        {
                            board.append( "," );
                        }
                    }
                }
            }
            
            // Save the board targets
            editor.putString( "targets", board.toString() );
            board.setLength( 0 );
            
            //
            editor.commit();
        }
    }
    
    
    /**
     * /#######################################
     * <p>
     * Load all shared pref data
     * <p>
     * //#######################################
     */
    public static void loadGameState( Context context )
    {
        if ( context != null )
        {
            SharedPreferences        prefs    = context.getSharedPreferences( "game_state", Context.MODE_PRIVATE );
            SharedPreferences.Editor editor   = prefs.edit();
            boolean                  saveExist;
            int                      gameType = 0;
            
            gameBoard = GameBoard.getInstance( context );
            gameEngine = GameEngine.getInstance( context );
            
            
            //################################
            //
            //
            //
            //################################
            saveExist = prefs.getBoolean( "save_exist", false );
            
            //
            if ( saveExist )
            {
                CustomTimer.currentTime = prefs.getLong( "current_time", 0 );
                gameEngine.currentBGMusic = prefs.getInt( "current_bgm", 0 );
                GameEngine.currentBackground = prefs.getInt( "current_bg", 0 );
                GameEngine.currentBoardImage = prefs.getInt( "current_board", 0 );
                GameEngine.boardMoves = prefs.getInt( "current_moves", 0 );
                
                // Has it been converted yet?
                if ( !prefs.getBoolean( "level_converted", false ) )
                {
                    GameEngine.currentLevel = prefs.getInt( "current_area", 0 ) * 18;
                    // This will reset the current area BACK to this level. Must be careful
                    GameEngine.currentLevel += prefs.getInt( "current_level", 0 );
                    GameEngine.reloadedLevel = -1;
                    
                    // Remove this. No longer used
                    editor.remove( "current_area" );
                    
                    //Save converted data
                    editor.putInt( "current_level", GameEngine.currentLevel );
                    editor.putBoolean( "level_converted", true );
                    
                    //
                    editor.commit();
                }
                else
                {
                    GameEngine.currentLevel = prefs.getInt( "current_level", 0 );
                    GameEngine.reloadedLevel = prefs.getInt( "reloaded_level", -1 );
                }
                
                // Get the game type
                gameType = prefs.getInt( "game_type", 0 );
                
                //
                String boardTiles = prefs.getString( "board_tiles", "" );
                String targets    = prefs.getString( "targets", "" );
                String blockers   = prefs.getString( "blocker_data", "" );
                
                String[] bt = boardTiles.split( "," );
                String[] tg = targets.split( "," );
                String[] bd = blockers.split( "," );
                
                
                //####################################
                //
                //
                //
                //####################################
                if ( gameType == 0 )
                {
                    // Connections game info
                    ConnectionsFragment.levelScore = prefs.getInt( "current_score", 0 );
                    
                    // Load Board Tiles
                    if ( ConnectionsFragment.boardTiles != null && ConnectionsFragment.boardTiles.size() > 0 )
                    {
                        for ( int i = 0; i < bt.length; i += 2 )
                        {
                            BoardTile tile        = ConnectionsFragment.boardTiles.get( (i / 2) );
                            String    tileNum     = bt[ i ];
                            String    specialItem = bt[ i + 1 ];
                            
                            tile.tileNum = Integer.parseInt( tileNum );
                            tile.setSpecialItem( Integer.parseInt( specialItem ) );
                        }
                        
                        //
                        // Blocker data
                        //
                        if ( bd.length >= 3 )
                        {
                            for ( int i = 0; i < bd.length; i += 3 )
                            {
                                // Rock, Shell, all other singles
                                // Set the tile that is a blocker
                                int   position   = Integer.parseInt( bd[ i ] );
                                int   type       = Integer.parseInt( bd[ i + 1 ] );
                                int   count      = Integer.parseInt( bd[ i + 2 ] );
                                int[] images     = new int[ count ];
                                int   imageCount = 0;
                                
                                //
                                for ( int c = 0; c < count; c++, i++ )
                                {
                                    // Get the blocker image index
                                    images[ c ] = Integer.parseInt( bd[ i + 3 ] );
                                    imageCount++;
                                }
                                
                                //
                                BoardTile tile = ConnectionsFragment.boardTiles.get( position );
                                //
                                if ( imageCount > 0 )
                                {
                                    tile.setBlockerType( type, images );
                                    images = null;
                                }
                                else
                                {
                                    tile.setBlockerType( type );
                                }
                            }
                        }
                    }
                    
                    //
                    // Load Target Data
                    //
                    if ( ConnectionsFragment.view_main != null )
                    {
                        ConstraintLayout    layout      = ConnectionsFragment.view_main.findViewById( R.id.boardTargetHolder );
                        int                 children    = layout.getChildCount();
                        ArrayList<TextView> targetViews = new ArrayList<>();
                        
                        // Get those views
                        for ( int c = 0; c < children; c++ )
                        {
                            View view = layout.getChildAt( c );
                            
                            if ( view instanceof TextView && view.getId() != R.id.targetText )
                            {
                                targetViews.add( ( TextView ) view );
                            }
                        }
                        
                        
                        for ( int i = 0; i < targetViews.size(); i++ )
                        {
                            if ( i >= (tg.length / 2) )
                            {
                                break;
                            }
                            
                            int           value;
                            int           index;
                            ImageTextView v    = ( ImageTextView ) targetViews.get( i );
                            String        text = tg[ i + i ];
                            
                            value = Integer.parseInt( text );
                            text = tg[ i + i + 1 ];
                            index = Integer.parseInt( text );
                            
                            if ( value < 0 )
                            {
                                v.setVisibility( View.GONE );
                                continue;
                            }
                            
                            //
                            v.setTag( new PointXYZ( value, index ) );
                            if ( value > 0 )
                            {
                                v.setText( String.format( Locale.getDefault(), "%d", value ) );
                            }
                            else
                            {
                                v.setText( "" );
                                v.setImageResource( R.drawable.white_checkmark );
                            }
                            v.setVisibility( View.VISIBLE );
                        }
                    }
                    
                    // Memory cleaning
                    bt = null;
                    tg = null;
                    bd = null;
                }
                else
                {
                    // Cards game info
                    CardsFragment.levelScore = prefs.getInt( "current_score", 0 );
                    CardsFragment.cardHintCount = prefs.getInt( "card_hints_left", 0 );
                    
                    // Load Board Tiles
                    if ( CardsFragment.boardTiles != null && CardsFragment.boardTiles.size() > 0 )
                    {
                        for ( int i = 0; i < bt.length; i++ )
                        {
                            BoardTile tile   = CardsFragment.boardTiles.get( i );
                            String    text   = bt[ i ];
                            int       decode = Integer.parseInt( text );
                            
                            // Inactive tile??
                            if ( (decode & 0xFF00) == 0xFF00 )
                            {
                                continue;
                            }
                            
                            // Read in the aggregate data and decode
                            tile.setState( decode & 0x00FF );
                            tile.specialTile = (decode & 0xFF00) >> 8;
                            //
                            tile.card[ 0 ] = gameBoard.cardSet[ 0 ];
                            tile.card[ 1 ] = gameBoard.cardSet[ tile.specialTile ];
                            //
                            switch ( tile.getState() )
                            {
                                case BoardTile.STATE_ACTIVE:
                                    tile.tileNum = gameBoard.cardSet[ 0 ];
                                    break;
                                case BoardTile.FLIP_CARD:
                                case BoardTile.STATE_FLIPPED:
                                    tile.tileNum = gameBoard.cardSet[ 1 ];
                                    break;
                                case BoardTile.STATE_INACTIVE:
                                default:
                                    tile.tileNum = -1;
                                    break;
                            }
                        }
                    }
                    
                    // Load Target Data
                    if ( CardsFragment.view_main != null )
                    {
                        ConstraintLayout    layout      = CardsFragment.view_main.findViewById( R.id.boardTargetHolder );
                        int                 children    = layout.getChildCount();
                        ArrayList<TextView> targetViews = new ArrayList<>();
                        
                        // Get those views
                        for ( int c = 0; c < children; c++ )
                        {
                            View view = layout.getChildAt( c );
                            
                            if ( view instanceof TextView && view.getId() != R.id.targetText )
                            {
                                targetViews.add( ( TextView ) view );
                            }
                        }
                        
                        
                        for ( int i = 0; i < targetViews.size(); i++ )
                        {
                            if ( i >= tg.length )
                            {
                                break;
                            }
                            
                            TextView v    = targetViews.get( i );
                            String   text = tg[ i ];
                            int      value;
                            
                            value = Integer.parseInt( text );
                            
                            if ( value < 0 )
                            {
                                v.setVisibility( View.GONE );
                                continue;
                            }
                            v.setTag( new PointXYZ( value, 0 ) );
                            v.setText( String.format( Locale.getDefault(), "%d", value ) );
                            v.setVisibility( View.VISIBLE );
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Player did not want the saved game
     * <p>
     * //###################################
     */
    public static void eraseSavedGame( Context context )
    {
        if ( context != null )
        {
            SharedPreferences        prefs;
            SharedPreferences.Editor editor;
            
            //
            prefs = context.getSharedPreferences( "game_state", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            
            //################################
            //
            //
            //
            //################################
            /*        editor.putBoolean( "save_exist", false );*/
            editor.clear();
            //
            editor.commit();
        }
    }
}
