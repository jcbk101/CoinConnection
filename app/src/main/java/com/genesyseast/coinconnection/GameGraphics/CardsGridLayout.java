package com.genesyseast.coinconnection.GameGraphics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.Fragments.CardsFragment;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class CardsGridLayout
        extends GridLayout
        implements View.OnTouchListener
{
    final public static int SCORE_UPDATE = 0;
    final public static int BONUS_SPLASH = 1;
    final public static int MOVES_UPDATE = 2;
    
    
    private       Context              context;
    public static int                  gMapWidth;
    public static int                  gMapHeight;
    public        int                  mapWidth;
    public        int                  mapHeight;
    private       View                 view_main;
    private       GameBoard            gameBoard;
    private       GameEngine           gameEngine;
    private       LogicThread          logicThread;
    public        int[]                cardSet;
    //
    private       ArrayList<BoardTile> boardTiles;
    public        ArrayList<TextView>  targetViews;
    private       int                  maxColors;
    // Symbols mapping
    // Used for hints AND to determine if any matches exist
    private       Point                tileOne         = new Point();
    private       Point                tileTwo         = new Point();
    private       int                  flippedTiles    = 0;
    //
    private       OnGridUpdateListener onGridUpdateListener;
    //
    private       Random               r;
    private       int                  boardMoves      = 0;
    private       int                  loopPosition    = 0;
    // Should only be false when a game is completed!
    public        boolean              canSolveMatches = true;
    private       int                  matchColor      = -1;
    private       int                  size;
    public        boolean              levelCompleted  = false;
    private       int                  loopStop;
    
    public interface OnGridUpdateListener
    {
        void onGridUpdated( int gridFunction, int gridData );
        
        void onLevelComplete();
        
        void onLevelFailed( String exitMessage );
    }
    
    
    /**
     * //###############################
     * <p>
     * Class constructor
     * <p>
     * //###############################
     *
     * @param context N/A
     */
    public CardsGridLayout( Context context )
    {
        super( context );
        
        this.context = context;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        r = new Random( 10293846 );
        
        //        setOnTouchListener( this );
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.CARD_SIZE );
        //
        setOnTouchListener( this );
    }
    
    public CardsGridLayout( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.context = context;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        r = new Random( 10293846 );
        
        //        setOnTouchListener( this );
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.CARD_SIZE );
        //
        setOnTouchListener( this );
    }
    
    public CardsGridLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
        
        this.context = context;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        r = new Random( 10293846 );
        
        //        setOnTouchListener( this );
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.CARD_SIZE );
        //
        setOnTouchListener( this );
    }
    
    
    public void setBoardTiles( ArrayList<BoardTile> boardTiles )
    {
        this.boardTiles = boardTiles;
    }
    
    
    /**
     * //###############################
     *
     * <p>
     * //###############################
     *
     * @param logicThread
     */
    public void setLogicThread( LogicThread logicThread )
    {
        this.logicThread = logicThread;
    }
    
    
    public static int getMapWidth()
    {
        return gMapWidth;
    }
    
    public static int getMapHeight()
    {
        return gMapHeight;
    }
    
    
    /**
     * //###############################
     * <p>
     * Setters for map sizes
     * <p>
     * //###############################
     *
     * @param mapWidth N/A
     */
    public void setMapWidth( int mapWidth )
    {
        this.mapWidth = mapWidth;
        setRowCount( mapWidth );
        
        if ( this.mapWidth > 0 && this.mapHeight > 0 )
        {
            boardTiles = gameBoard.getBoardTiles();
            maxColors = gameBoard.getMaxColors();
            cardSet = gameBoard.getCardSet();
            //
            gMapHeight = mapHeight;
            gMapWidth = mapWidth;
        }
    }
    
    public void setMapHeight( int mapHeight )
    {
        this.mapHeight = mapHeight;
        setColumnCount( mapHeight );
        
        if ( this.mapWidth > 0 && this.mapHeight > 0 )
        {
            boardTiles = gameBoard.getBoardTiles();
            maxColors = gameBoard.getMaxColors();
            cardSet = gameBoard.getCardSet();
            //
            gMapHeight = mapHeight;
            gMapWidth = mapWidth;
        }
    }
    
    public void setView_main( View view_main )
    {
        this.view_main = view_main;
        
        // Need to get the target views and do it ONCE!!!
        ConstraintLayout layout = view_main.findViewById( R.id.boardTargetHolder );
        
        if ( layout != null )
        {
            int children = layout.getChildCount();
            
            // Clear crap to be safe
            targetViews.clear();
            
            // Get those views
            for ( int c = 0; c < children; c++ )
            {
                View view = layout.getChildAt( c );
                
                if ( view instanceof TextView && view.getId() != R.id.targetText )
                {
                    targetViews.add( ( TextView ) view );
                }
            }
        }
    }
    
    public void setBoardMoves( int boardMoves )
    {
        this.boardMoves = boardMoves;
    }
    
    public void setLevelCompleted( boolean levelCompleted )
    {
        this.levelCompleted = levelCompleted;
    }
    
    public boolean isLevelCompleted()
    {
        return levelCompleted;
    }
    
    public void setFlippedTiles( int flippedTiles )
    {
        this.flippedTiles = flippedTiles;
    }
    
    public int getFlippedTiles()
    {
        return flippedTiles;
    }
    
    /**
     * ########################################
     * <p>
     * Attach a listener
     * <p>
     * ########################################
     *
     * @param onGridUpdateListener N/A
     */
    public void setOnGridUpdateListener( OnGridUpdateListener onGridUpdateListener )
    {
        this.onGridUpdateListener = onGridUpdateListener;
    }
    
    
    /**
     * //###############################
     * <p>
     * My adapter, custom made
     * <p>
     * //###############################
     */
    public void onDrawGrid()
    {
        int                       pixelSize = context.getResources().getDimensionPixelSize( R.dimen._1sdp );
        int                       size      = context.getResources().getDimensionPixelSize( R.dimen.CARD_SIZE );
        LinearLayout.LayoutParams params;
        
        
        //##############################
        // Some times the user pauses
        // the game exit. Leave if exit
        // was too early!
        //##############################
        if ( boardTiles == null )
        {
            return;
        }
        
        
        // Set parameters
        params = new LinearLayout.LayoutParams( size - (pixelSize * 2), size - (pixelSize * 2) );
        params.setMargins( pixelSize, pixelSize, pixelSize, pixelSize );
        //        params = new LinearLayout.LayoutParams( size, size );
        
        
        //####################################
        //
        // Draw each image
        //
        //####################################
        removeAllViews();
        
        // No need to continue if animations are disabled
        if ( MainActivity.checkSystemAnimationsDuration( getContext() ) == 0 )
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder( getContext() );
            
            dialog.setMessage( getContext().getString( R.string.anims_disabled ) );
            dialog.setPositiveButton( "Exit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    System.exit( 1 );
                }
            } );
            
            //
            dialog.create().show();
            return;
        }
        
        for ( int position = 0; position < boardTiles.size() && !GameEngine.isKilled; position++ )
        {
            final BoardTile image;
            
            // Is the game completed already??
            if ( levelCompleted )
            {
                return;
            }
            
            //
            image = boardTiles.get( position );
            
            image.cancelAnimationAndListeners();
            //            image.setOnTouchListener( this );
            //            image.setOnClickListener( this );
            //            image.setOnLongClickListener( this );
            
            // Anything swapped, fix it here
            image.setTranslationX( 0 );
            image.setTranslationY( 0 );
            image.setSoundEfx( -1 );
            
            //
            // Handle the initial board spawning
            //
            if ( image.getState() == BoardTile.STATE_INACTIVE && image.tileNum < 0 )
            {
                // Blank image requested
                image.setImageResource( 0 );
                image.clearAnimation();
                image.setTag( null );
                image.setVisibility( INVISIBLE );
            }
            //##################################
            //
            // Flipped image requested
            //
            //##################################
            else if ( image.getState() == BoardTile.STATE_FLIPPED )
            {
                image.setTag( null );
                
                if ( image.card[ 1 ] == R.drawable.card_emoji_sad )
                {
                    image.setImageResource( 0 );
                    image.setBackgroundColor( 0 );
                    image.setSpecialItem( 0 );
                }
                else
                {
                    image.setAlpha( 1f );
                    image.clearAnimation();
                    //
                    image.setVisibility( VISIBLE );
                    imageHelper( position );
                }
            }
            // Blank image requested
            else
            {   // Helper data for Error checks also
                //                image.setScaleY( 1 );
                //                image.setScaleX( 1 );
                //                image.setRotation( 0 );
                image.setAlpha( 1f );
                image.clearAnimation();
                
                //
                image.setTag( null );
                image.setVisibility( VISIBLE );
                imageHelper( position );
                
                // Matched is matched!
                if ( image.getState() != BoardTile.STATE_FLIPPED )
                {
                    image.setState( BoardTile.STATE_ACTIVE );
                }
            }
            
            // Now add the image to the GridLayout
            image.setId( position );
            image.setPosition( position );
            
            addView( image, params );
            
            if ( position == 0 )
            {
                image.requestLayout();
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Update the grid
     * <p>
     * //###############################
     */
    public void onUpdateGrid()
    {
        // No need to continue if animations are disabled
        if ( MainActivity.checkSystemAnimationsDuration( getContext() ) == 0 )
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder( getContext() );
            
            dialog.setMessage( context.getString( R.string.anims_disabled ) );
            dialog.setPositiveButton( "Exit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    System.exit( 1 );
                }
            } );
            
            //
            dialog.create().show();
            return;
        }
        
        int     runningScore = 0;
        boolean matchMade    = false;
        int[]   targets      = new int[ cardSet.length ];
        
        
        //####################################
        //
        // Draw each image
        //
        //####################################
        for ( int position = 0; position < boardTiles.size() && !GameEngine.isKilled; position++ )
        {
            final BoardTile image;
            Animation       anim;
            
            // Is the game completed already??
            if ( levelCompleted )
            {
                return;
            }
            
            //
            image = boardTiles.get( position );
            
            
            //##################################
            //
            //
            //
            //##################################
            if ( image.getState() == BoardTile.STATE_ACTIVE )
            {
                //                image.endAnimation();
                imageHelper( image.getPosition() );
            }
            //##################################
            //
            //
            //
            //##################################
            else if ( image.getState() == BoardTile.STATE_MATCHED )
            {
                boolean dance = (image.getSpecialItem() == 0);
                
                //
                // If a sad faced card, DISAPPEAR!!!
                //
                if ( !dance )
                {
                    image.animate().scaleX( 0 ).scaleY( 0 ).setInterpolator( new AnticipateInterpolator() );
                    image.animate().setDuration( AnimationValues.SWAP_SPEED );
                    image.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            image.setImageResource( 0 );
                            image.setBackgroundColor( 0 );
                            image.setSpecialItem( 0 );
                            image.setState( BoardTile.STATE_FLIPPED );
                            
                            logicThread.animationsRunning--;
                            if ( logicThread.animationsRunning <= 0 )
                            {
                                // Check for targets reached
                                logicThread.animationsRunning = 0;
                                logicThread.addToStack( LogicThread.CMD_IDLE );
                            }
                        }
                    } ).start();
                }
                else
                {
                    image.setScaleX( .75f );
                    image.setScaleY( .75f );
                    image.animate().scaleX( 1f ).scaleY( 1f ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                    image.animate().setDuration( AnimationValues.SWAP_SPEED );
                    image.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // No more touching
                            image.setState( BoardTile.STATE_FLIPPED );
                            
                            
                            logicThread.animationsRunning--;
                            if ( logicThread.animationsRunning <= 0 )
                            {
                                // Check for targets reached
                                logicThread.animationsRunning = 0;
                                logicThread.addToStack( LogicThread.CMD_IDLE );
                            }
                        }
                    } ).start();
                }
                
                //
                logicThread.animationsRunning++;
                // Only process Targets
                //
                if ( dance )
                {
                    // Adjust targets
                    adjustTargetCounts( targets, image.specialTile - GameBoard.BASE_CARDS );
                    matchMade = true;
                }
            }
            //##################################
            //
            // Flipped image requested
            //
            //##################################
            else if ( image.getState() == BoardTile.STATE_FLIPPED )
            {
                image.setTag( null );
                
                if ( image.card[ 1 ] == R.drawable.card_emoji_sad )
                {
                    image.setImageResource( 0 );
                    image.setBackgroundColor( 0 );
                    image.setSpecialItem( 0 );
                }
                else
                {
                    image.setAlpha( 1f );
                    image.clearAnimation();
                    //
                    image.setVisibility( VISIBLE );
                    imageHelper( position );
                }
            }
            // Blank image requested
            else if ( image.getState() == BoardTile.STATE_INACTIVE && image.tileNum < 0 )
            {
                image.setImageResource( 0 );
                image.setBackgroundResource( 0 );
                image.setSpecialItem( 0 );
            }
            
            //
            // Now add the image to the GridLayout
            //
            if ( position == 0 )
            {
                image.requestLayout();
            }
        }
        
        
        // To use for "target reached" checking
        if ( matchMade )
        {
            runningScore = adjustTargets( targets );
        }
        
        // Update score and make sounds!
        if ( runningScore > 0 && onGridUpdateListener != null )
        {
            onGridUpdateListener.onGridUpdated( SCORE_UPDATE, runningScore * 25 );
            onGridUpdateListener.onGridUpdated( MOVES_UPDATE, boardMoves );
        }
        //        this.setTag( targets );
    }
    
    
    /**
     * //###############################
     * <p>
     * Help all target adjustments NOT
     * fail due to negative value index
     * <p>
     * //###############################
     *
     * @param targets
     * @param tileNum
     */
    private void adjustTargetCounts( int[] targets, int tileNum )
    {
        if ( tileNum > -1 && tileNum < targets.length )
        {
            targets[ tileNum ]++;
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Helper for image changing with
     * specials attached
     * <p>
     * //###############################
     *
     * @param position N/A
     */
    public void imageHelper( int position )
    {
        BoardTile image = boardTiles.get( position );
        int       padding;
        
        if ( image.tileNum < 0 )
        {
            // Tends to happen after a level is completed, but
            // the game is still matching tiles
            return;
        }
        
        // Items with extra BG effect start them NOW
        image.setBackgroundResource( image.tileNum );
    }
    
    
    /**
     * ########################################
     * <p>
     * Swiping
     * <p>
     * ########################################
     *
     * @param v     N/A
     * @param event N/A
     *
     * @return N/A
     */
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        //
        // No touching when the board is not active
        //        if ( CustomTimer.timerStatus != CustomTimer.TIMER_RUNNING || logicThread.getCurrentCmd() != LogicThread.CMD_IDLE || logicThread.animationsRunning > 0 )
        if ( logicThread.getCurrentCmd() != LogicThread.CMD_IDLE || logicThread.animationsRunning > 0 )
        {
            return false;
        }
        
        //
        // Finish what you are doing first
        //
        if ( flippedTiles == 2 )
        {
            return true;
        }
        
        
        // Do not allow touching if targets are all clear
        int count = 0;
        
        for ( TextView view : targetViews )
        {
            if ( view.getTag() != null )
            {
                count += (( PointXYZ ) view.getTag()).x;
            }
        }
        
        //
        if ( count <= 0 )
        {
            return false;
        }
        
        //##########################################
        //
        //
        //
        //##########################################
        if ( event.getActionMasked() == MotionEvent.ACTION_DOWN )
        {
            int       x = ( int ) event.getX() / size;
            int       y = ( int ) event.getY() / size;
            int       position;
            BoardTile tile;
            
            
            position = (y * mapHeight) + x;
            // MUST BE VALID
            if ( position < 0 || position > boardTiles.size() )
            {
                return true;
            }
            
            
            tile = boardTiles.get( position );
            // MUST BE VALID
            if ( tile.tileNum < 0 )
            {
                return true;
            }
            
            //##############################
            //
            // Can't click a matched tile
            //
            //##############################
            if ( tile.getState() == BoardTile.STATE_FLIPPED )
            {
                //@@@@@@@@@@@@@@@@@
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.INVALID );
                }
                
                return true;
            }
            
            
            //##############################
            //
            // Flip some tiles
            //
            //##############################
            if ( !tile.cardFlipped && flippedTiles == 1 )
            {
                // Flip second card and test
                tileTwo.x = x;
                tileTwo.y = y;
                //
                flippedTiles = 2;
                
                //###############################
                //
                //
                //
                //###############################
                tile.flipCoin( false ).addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        int       position = (tileOne.y * mapHeight) + tileOne.x;
                        BoardTile first    = boardTiles.get( position );
                        
                        
                        // Decrease board moves and prepare test test
                        // if the player is out of moves
                        boardMoves--;
                        
                        //
                        // We have a match
                        //
                        if ( first.card[ 1 ] == tile.card[ 1 ] )
                        {
                            //################################
                            //
                            // Is it a target or a sad face
                            //
                            //################################
                            if ( first.card[ 1 ] == R.drawable.card_emoji_sad )
                            {
                                first.setSpecialItem( -2 );
                                tile.setSpecialItem( -2 );
                                
                                //@@@@@@@@@@@@@@@@@
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.playBgSfx( PlaySound.COIN_MATCH );
                                }
                            }
                            //################################
                            //
                            // Add moves / Hints
                            //
                            //################################
                            else if ( first.card[ 1 ] == R.drawable.card_hint_glass_gold || first.card[ 1 ] == R.drawable.card_free_moves_gold )
                            {
                                first.setSpecialItem( -2 );
                                tile.setSpecialItem( -2 );
                                
                                // Give the player back a move
                                // If the game is getting harder, give one more
                                boardMoves++;
                                
                                if ( first.card[ 1 ] == R.drawable.card_hint_glass_gold )
                                {
                                    CardsFragment.cardHintCount++;
                                }
                                else
                                {
                                    boardMoves++;
                                }
                                
                                //@@@@@@@@@@@@@@@@@
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                                }
                            }
                            else
                            {
                                first.setSpecialItem( 0 );
                                tile.setSpecialItem( 0 );
                                
                                // Give the player back a move
                                // If the game is getting harder, give one more
                                boardMoves++;
                                
                                // If player found one, and they have more than one
                                // card hint remaining, remove it
                                if ( CardsFragment.cardHintCount > 0 && checkTargetsReached() < 4 )
                                {
                                    CardsFragment.cardHintCount = 0;
                                }
                                
                                if ( CardsFragment.cardHintCount < 5 )
                                {
                                    CardsFragment.cardHintCount--;
                                    
                                    if ( CardsFragment.cardHintCount < 0 )
                                    {
                                        CardsFragment.cardHintCount = 0;
                                    }
                                }
                                
                                //@@@@@@@@@@@@@@@@@
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.playBgSfx( PlaySound.SLOTS_PRIZE_ADD );
                                }
                            }
                            
                            //
                            first.setState( BoardTile.STATE_MATCHED );
                            tile.setState( BoardTile.STATE_MATCHED );
                            //                            flippedTiles = 0;
                            logicThread.addToStack( LogicThread.CMD_PROCESS_MATCHES );
                        }
                        else
                        {
                            // No match
                            //@@@@@@@@@@@@@@@@@
                            if ( gameEngine.soundPlayer != null )
                            {
                                gameEngine.soundPlayer.playBgSfx( PlaySound.INVALID );
                            }
                            
                            // Set to unflip
                            tile.flipCoin( true );
                            first.flipCoin( true );
                            
                            //                            tile.setState( BoardTile.STATE_UNSWAP );
                            //                            first.setState( BoardTile.STATE_UNSWAP );
                            //                            flippedTiles = 0;
                            logicThread.addToStack( LogicThread.CMD_IDLE );
                            //                            logicThread.addToStack( LogicThread.CMD_FLIP );
                        }
                        
                        // Update the status for the player
                        onGridUpdateListener.onGridUpdated( MOVES_UPDATE, boardMoves );
                    }
                } );
            }
            else if ( flippedTiles == 0 )
            {
                // Flip the first card visible
                //                tile.flipCoin( 0 );
                tileOne.x = x;
                tileOne.y = y;
                flippedTiles = 1;
                //
                //                tile.setState( BoardTile.FLIP_CARD );
                tile.flipCoin( false );
                tile.setState( BoardTile.FLIP_CARD );
                //                logicThread.addToStack( LogicThread.CMD_FLIP );
            }
        }
        
        return false;
    }
    
    
    /**
     * ########################################
     * <p>
     * Check for Display errors, like Tiles not
     * displayed although status is IDLE
     * <p>
     * ########################################
     */
    public void checkForErrors()
    {
        //        invalidate();
        
        // Refresh screen tiles
        onDrawGrid();
    }
    
    
    /**
     * ########################################
     * <p>
     * Check to see if any or all of the
     * targets have been reached. If so, update
     * the player on current status
     * <p>
     * ########################################
     */
    public int checkTargetsReached()
    {
        int total_targets = 0;
        
        //
        for ( TextView view : targetViews )
        {
            final PointXYZ temp = ( PointXYZ ) view.getTag();
            
            if ( view.getVisibility() == VISIBLE && temp.getTag() == null )
            {
                // Get the amounts remaining
                total_targets += temp.x;
            }
        }
        
        return total_targets;
    }
    
    
    /**
     * //###############################
     * <p>
     * Is the player out of moves?
     * <p>
     * //###############################
     */
    public boolean checkMovesExhausted()
    {
        return boardMoves <= 0;
    }
    
    
    /**
     * //###############################
     * <p>
     * <p>
     * Adjust target numbers
     * <p>
     * //###############################
     *
     * @param targets
     */
    private int adjustTargets( int[] targets )
    {
        int count = 0;
        
        //#################################
        //
        //
        //
        //#################################
        for ( int i = 0; i < targetViews.size(); i++ )
        {
            final TextView view = targetViews.get( i );
            final PointXYZ temp = ( PointXYZ ) view.getTag();
            
            if ( view.getVisibility() == VISIBLE && temp.getTag() == null )
            {
                if ( targets[ i ] > 0 )
                {
                    PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.5f, 1 );
                    PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.5f, 1 );
                    ObjectAnimator       obj = ObjectAnimator.ofPropertyValuesHolder( view, sx, sy );
                    //                    final int            number = (temp.x - targets[ i ]) < 0 ? 0 : (temp.x - targets[ i ]);
                    final int number = Math.max( (temp.x - targets[ i ]), 0 );
                    
                    // If target has something left, subtract it
                    if ( temp.x > 0 )
                    {
                        temp.x = number;
                        //
                        view.setText( String.format( Locale.getDefault(), "%2d", number ) );
                        obj.setDuration( 500 ).setInterpolator( new AnticipateOvershootInterpolator( 1f ) );
                        obj.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                super.onAnimationEnd( animation );
                                temp.setTag( null );
                            }
                        } );
                        obj.start();
                        temp.setTag( obj );
                    }
                    
                    // Get the matched amount for this target
                    count += targets[ i ];
                }
            }
        }
        
        return count;
    }
    
    
    /**
     * //###############################
     * <p>
     * The level is complete!
     * <p>
     * //###############################
     */
    public void signalLevelComplete()
    {
        if ( view_main != null )
        {
/*
            final View       view        = view_main.findViewById( R.id.boardGridLayout );
            ConstraintLayout frame       = view_main.findViewById( R.id.boardGridFrame );
            int              currentView = 0;
            
            
            // Any specials need saving??
            gameEngine.savedSpecials.clear();
            
            // This gets called twice sometimes
            if ( gameEngine.specialRunning )
            {
                gameEngine.specialRunning = false;
                return;
            }
            
            //###################################
            //
            //
            //
            //###################################
            levelCompleted = true;
            logicThread.clearData();
            
            // Need this
            gameEngine.specialRunning = true;
            
            
            // Animate the Cards to full display if over
            onSignalCompleteHelper( view );
            
            // Last animation running has ended!
            //        onGridUpdateListener.onLevelComplete();
*/
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * <p>
     * //###############################
     */
    public void signalLevelFailed()
    {
        if ( view_main != null )
        {
/*
            final View       view        = view_main.findViewById( R.id.boardGridLayout );
            ConstraintLayout frame       = view_main.findViewById( R.id.boardGridFrame );
            int              currentView = 0;
            
            
            // This gets called twice sometimes
            if ( gameEngine.specialRunning )
            {
                gameEngine.specialRunning = false;
                return;
            }
            
            //############################
            //
            // Hide all the targets
            //
            //############################
            for ( int i = 0; i < targetViews.size(); i++ )
            {
                TextView v = targetViews.get( i );
                
                if ( v.getVisibility() == VISIBLE )
                {
                    v.animate().alpha( 0 ).setDuration( 500 );
                    v.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            v.setVisibility( GONE );
                            v.setAlpha( 1f );
                        }
                    } ).start();
                }
            }
            
            //###################################
            //
            //
            //
            //###################################
            levelCompleted = true;
            logicThread.clearData();
            
            // Need this
            gameEngine.specialRunning = true;
            onGridUpdateListener.onLevelFailed( "Out of moves" );
*/
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * <p>
     * //###############################
     */
    public void onSignalCompleteHelper( final View view )
    {
        //@@@@@@@@@@@@@@@@@@@@@ Level complete: 2 seconds
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.STAR_SHOOT );
        }
        
        // For delay purposes
        ValueAnimator val        = ValueAnimator.ofInt( 0, 10 );
        View          scoreNum   = view_main.findViewById( R.id.boardScoreText );
        int[]         scoreLoc   = new int[ 2 ];
        int[]         tileLoc    = new int[ 2 ];
        boolean       soundLoop  = false;
        boolean       valStarted = false;
        
        
        //############################
        //
        // Hide all the targets
        //
        //############################
        for ( int i = 0; i < targetViews.size(); i++ )
        {
            TextView v = targetViews.get( i );
            
            if ( v.getVisibility() == VISIBLE )
            {
                int width = view_main.getWidth();
                
                v.animate().translationX( width + v.getWidth() ).rotation( 360 ).setDuration( 1000 );
                v.animate().setInterpolator( new AnticipateInterpolator( 1f ) );
                v.animate().withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        v.setTranslationX( 0 );
                        v.setVisibility( GONE );
                    }
                } ).start();
            }
            else
            {
                v.setTranslationX( 0 );
            }
        }
        
        
        //############################
        //
        // First, add all remaining
        // cards to score
        //
        //############################
        //
        loopStop = 0;
        scoreNum.getLocationOnScreen( scoreLoc );
        
        //
        for ( int i = 0; i < boardTiles.size(); i++ )
        {
            final BoardTile tile = boardTiles.get( i );
            
            if ( tile.tileNum != -1 && tile.getState() != BoardTile.STATE_FLIPPED )
            {
                float transX;
                float transY;
                
                if ( !soundLoop && gameEngine.soundPlayer != null )
                {
                    //@@@@@@@@@@@@@@@@@@@@@ Level complete: 2 seconds
                    loopStop = gameEngine.soundPlayer.playBgSfx( PlaySound.POINTS_ADD_LOOP, PlaySound.LOOP );
                    soundLoop = true;
                }
                
                //
                valStarted = true;
                
                //
                tile.getLocationOnScreen( tileLoc );
                transX = scoreLoc[ 0 ] - tileLoc[ 0 ];
                transY = scoreLoc[ 1 ] - tileLoc[ 1 ];
                
                //
                tile.cancelAnimationAndListeners();
                //
                tile.animate().setStartDelay( 200 + (i / mapWidth) * 50 + (i % mapHeight) * 50 );
                tile.animate().rotation( 720 );
                tile.animate().translationY( transY ).translationX( transX ).setInterpolator( new AnticipateInterpolator() );
                tile.animate().scaleX( 0.2f ).scaleY( 0.2f );
                tile.animate().setDuration( AnimationValues.DROP_TIME );
                tile.animate().withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tile.setScaleX( 1 );
                        tile.setScaleY( 1 );
                        tile.setBackgroundResource( 0 );
                        tile.setImageResource( 0 );
                        
                        // Make the score react
                        scoreNum.setScaleY( 1.2f + (loopPosition / 100f) );
                        scoreNum.setScaleX( 1.2f + (loopPosition / 100f) );
                        
                        // Show the score change
                        //                        gameEngine.Scores[ gameEngine.Levels[ gameEngine.loadedArea ] ] += 5;
                        CardsFragment.levelScore += 5;
                        (( TextView ) scoreNum).setText( String.format( Locale.getDefault(), "%d", CardsFragment.levelScore ) );
                        
                        
                        loopPosition--;
                        if ( loopPosition <= 0 )
                        {
                            gameEngine.soundPlayer.play( PlaySound.POINTS_ADD_LOOP, PlaySound.STOP, loopStop );
                            //
                            scoreNum.setScaleY( 1f );
                            scoreNum.setScaleX( 1f );
                            
                            //
                            val.start();
                        }
                    }
                } ).start();
                
                // Only count the row just before the last row so some burst
                // play with the "Level Complete" text
                loopPosition++;
                tile.tileNum = -1;
            }
        }
        
        scoreLoc = null;
        tileLoc = null;
        
        
        //############################
        //
        // All remaining Cards,
        // zoom out
        //
        //############################
        val.setDuration( AnimationValues.DROP_TIME );
        val.addListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation )
            {
                loopPosition = 0;
                
                for ( int i = 0; i < boardTiles.size(); i++ )
                {
                    final BoardTile tile = boardTiles.get( i );
                    
                    if ( tile.tileNum != -1 && tile.getState() == BoardTile.STATE_FLIPPED )
                    {
                        //
                        tile.cancelAnimationAndListeners();
                        tile.animate().scaleY( 0 ).scaleX( 0 ).setInterpolator( new AnticipateOvershootInterpolator() );
                        tile.animate().setDuration( AnimationValues.DROP_TIME );
                        tile.animate().withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tile.setScaleX( 1 );
                                tile.setScaleY( 1 );
                                tile.setBackgroundResource( 0 );
                                tile.setImageResource( 0 );
                                
                                loopPosition--;
                                if ( loopPosition <= 0 )
                                {
                                    onGridUpdateListener.onLevelComplete();
                                }
                            }
                        } ).start();
                        
                        // Only count the row just before the last row so some burst
                        // play with the "Level Complete" text
                        loopPosition++;
                        tile.tileNum = -1;
                    }
                }
                
                super.onAnimationEnd( animation );
            }
        } );
        
        //
        // Start it if the nested trigger isn't fired
        //
        if ( !valStarted )
        {
            val.start();
        }
    }
    
    
    @Override
    protected void onDetachedFromWindow()
    {
        view_main = null;
        gameBoard = null;
        gameEngine = null;
        
        // Kill the thread
        if ( logicThread != null )
        {
            logicThread = null;
        }
        //
        boardTiles = null;
        targetViews = null;
        cardSet = null;
        tileOne = null;
        tileTwo = null;
        onGridUpdateListener = null;
        r = null;
        
        super.onDetachedFromWindow();
    }
}
