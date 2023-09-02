package com.genesyseast.coinconnection.GameGraphics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.gridlayout.widget.GridLayout;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.CustomControls.GridLayoutView;
import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.CustomControls.OverlayView;
import com.genesyseast.coinconnection.CustomControls.StarsEarnedBar;
import com.genesyseast.coinconnection.Dialogs.LevelCompleted;
import com.genesyseast.coinconnection.Fragments.ConnectionsFragment;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.Variables.Mapper;
import com.genesyseast.coinconnection.Variables.PointXYZ;
import com.plattysoft.leonids.ParticleSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class ConnectionsGridLayout
        extends GridLayout
        implements View.OnTouchListener
{
    private static class tileGroup
    {
        int                tileNum;
        int                count;
        ArrayList<Integer> position;
    }
    
    public static final int MIN_FOR_SPECIAL = 6;
    final public static int SCORE_UPDATE    = 0;
    final public static int BONUS_SPLASH    = 1;
    final public static int MOVES_UPDATE    = 2;
    
    
    private       Context              context;
    public static int                  gMapWidth;
    public static int                  gMapHeight;
    public        int                  mapWidth;
    public        int                  mapHeight;
    private       View                 mainView;
    private       GameBoard            gameBoard;
    private       GameEngine           gameEngine;
    private       LogicThread          logicThread;
    //
    private       ArrayList<BoardTile> boardTiles;
    public        ArrayList<View>      targetViews;
    public        ArrayList<Integer>   tilePoints;
    public        ArrayList<Integer>   gemsToMatch;
    public        ArrayList<Integer>   blockersToMatch;
    //
    private       int[]                coinSet;
    private       int[]                coinGlow;
    private       int[]                coinBombIcons;
    private       int[]                coinBombGlow;
    private       int[]                glowColors;
    private       int                  maxColors;
    // Symbols mapping
    public        int[][]              boardMap;
    public        int[][]              matchList;
    // Used for hints AND to determine if any matches exist
    public        PointXYZ             hintTile;
    //
    public        ArrayList<Mapper>    specials;
    private       OnGridUpdateListener onGridUpdateListener;
    //
    private       OnBoosterListener    onBoosterListener;
    private       int                  boosterItem      = -1;
    //
    private       Random               r;
    public        int                  specialMerge     = 0;
    /*    private       int                  boardMoves       = 0;*/
    private       int                  loopPosition     = 0;
    private       CustomTimer          timer;
    private       long                 SUPER_TIME_VALUE = 45000;
    public        boolean              noMatchesTrigger = false;
    // Should only be false when a game is completed!
    private       boolean              inHelper         = false;
    public        boolean              canSolveMatches  = true;
    private       int                  matchColor       = -1;
    private       int                  matchSpecial     = -1;
    private       int                  size;
    private       LinearGradient       linearGradient;
    private       Paint                paint;
    private       Paint                subPaint;
    public        boolean              levelCompleted   = false;
    private       boolean              canTouch         = true;
    //
    public        Bitmap               lineBuffer;
    public        Canvas               bufCanvas;
    private       int[]                tileTypes;
    private       int                  DIV_NUM          = 2;
    private       int                  TOUCH_NUM        = 3;
    private       int                  DEBUG_ANIM_START = 000;
    public        boolean              objStillDropping = true;
    public        boolean              isInActive       = false;
    private       int                  firstPass        = 0;
    private       int                  totalGemsLeft    = 0;
    
    
    public interface OnGridUpdateListener
    {
        void onGridUpdated( int gridFunction, int gridData );
        
        void onNoMoreMatches();
        
        void onLevelComplete();
    }
    
    public interface OnBoosterListener
    {
        void boosterLocationSet( int location );
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
    public ConnectionsGridLayout( Context context )
    {
        super( context );
        
        this.context = context;
        this.inHelper = false;
        this.boosterItem = -1;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        specials = new ArrayList<>();
        r = new Random( 10293846 );
        tileTypes = new int[ BoardTile.YELLOW_GEM ];
        
        // List of names for items
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.TILE_SIZE );
        tilePoints = new ArrayList<>();
        gemsToMatch = new ArrayList<>();
        blockersToMatch = new ArrayList<>();
        
        //
        setOnTouchListener( this );
        
        // Shader
        int[] colorList;
        
        // Regular gradient
        String[] nums2str = getResources().getStringArray( R.array.white_on_white );
        colorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        linearGradient = new LinearGradient( 0, 0, size, 0, colorList, null, Shader.TileMode.MIRROR );
        paint = new Paint();
        subPaint = new Paint();
        
        //        paint.setColor( 0xFFe36dd0 );
        //paint.setShader( linearGradient );
        paint.setStrokeWidth( 4 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        //
        subPaint.setColor( Color.BLACK );
        subPaint.setStrokeWidth( 6 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        
        setUseDefaultMargins( false );
        
        // Glowing color beam
        glowColors = getResources().getIntArray( R.array.glow_colors );
    }
    
    public ConnectionsGridLayout( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        
        this.context = context;
        this.inHelper = false;
        this.boosterItem = -1;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        specials = new ArrayList<>();
        r = new Random( 10293846 );
        tileTypes = new int[ BoardTile.YELLOW_GEM ];
        
        // List of names for items
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.TILE_SIZE );
        tilePoints = new ArrayList<>();
        gemsToMatch = new ArrayList<>();
        blockersToMatch = new ArrayList<>();
        
        //
        setOnTouchListener( this );
        
        // Shader
        int[] colorList;
        // Regular gradient
        //        String[] nums2str = getResources().getStringArray( R.array.silver_reflection );
        String[] nums2str = getResources().getStringArray( R.array.white_on_white );
        colorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        linearGradient = new LinearGradient( 0, 0, size, 0, colorList, null, Shader.TileMode.MIRROR );
        paint = new Paint();
        subPaint = new Paint();
        
        //        paint.setColor( 0xFFe36dd0 );
        //paint.setShader( linearGradient );
        paint.setStrokeWidth( 4 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        //
        subPaint.setColor( Color.BLACK );
        subPaint.setStrokeWidth( 6 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        
        setUseDefaultMargins( false );
        
        // Glowing color beam
        glowColors = getResources().getIntArray( R.array.glow_colors );
    }
    
    public ConnectionsGridLayout( Context context, AttributeSet attrs, int defStyle )
    {
        super( context, attrs, defStyle );
        
        this.context = context;
        this.inHelper = false;
        this.boosterItem = -1;
        
        //###############################
        //
        // Items to be freed for GC
        //
        //###############################
        targetViews = new ArrayList<>();
        specials = new ArrayList<>();
        r = new Random( 10293846 );
        tileTypes = new int[ BoardTile.YELLOW_GEM ];
        
        // List of names for items
        gameBoard = GameBoard.getInstance( context );
        gameEngine = GameEngine.getInstance( context );
        
        // Get the distances we will use for testing
        size = getResources().getDimensionPixelSize( R.dimen.TILE_SIZE );
        tilePoints = new ArrayList<>();
        gemsToMatch = new ArrayList<>();
        blockersToMatch = new ArrayList<>();
        
        //
        setOnTouchListener( this );
        
        // Shader
        int[] colorList;
        // Regular gradient
        //        String[] nums2str = getResources().getStringArray( R.array.silver_reflection );
        String[] nums2str = getResources().getStringArray( R.array.white_on_white );
        colorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        linearGradient = new LinearGradient( 0, 0, size, 0, colorList, null, Shader.TileMode.MIRROR );
        paint = new Paint();
        subPaint = new Paint();
        
        //        paint.setColor( 0xFFe36dd0 );
        //paint.setShader( linearGradient );
        paint.setStrokeWidth( 4 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        //
        subPaint.setColor( Color.BLACK );
        subPaint.setStrokeWidth( 6 * getResources().getDimensionPixelSize( R.dimen._1sdp ) );
        
        setUseDefaultMargins( false );
        
        // Glowing color beam
        glowColors = getResources().getIntArray( R.array.glow_colors );
    }
    
    
    public void setBoardTiles( ArrayList<BoardTile> boardTiles )
    {
        this.boardTiles = boardTiles;
    }
    
    
    /**
     * //###############################
     * <p>
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
        this.inHelper = false;
        this.mapWidth = mapWidth;
        setRowCount( mapWidth );
        
        if ( this.mapWidth > 0 && this.mapHeight > 0 )
        {
            matchList = new int[ this.mapHeight ][ this.mapWidth ];
            //
            boardTiles = gameBoard.getBoardTiles();
            boardMap = gameBoard.getBoardMap();
            coinSet = gameBoard.getCoinSet();
            coinGlow = gameBoard.getCoinGlow();
            coinBombGlow = gameBoard.getCoinBombGlow();
            coinBombIcons = gameBoard.getCoinBombIcons();
            maxColors = gameBoard.getMaxColors();
            //
            gMapHeight = mapHeight;
            gMapWidth = mapWidth;
            
            if ( lineBuffer == null )
            {
                lineBuffer = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                bufCanvas = new Canvas( lineBuffer );
            }
        }
    }
    
    public void setMapHeight( int mapHeight )
    {
        this.inHelper = false;
        this.mapHeight = mapHeight;
        setColumnCount( mapHeight );
        
        if ( this.mapWidth > 0 && this.mapHeight > 0 )
        {
            matchList = new int[ this.mapHeight ][ this.mapWidth ];
            //
            boardTiles = gameBoard.getBoardTiles();
            boardMap = gameBoard.getBoardMap();
            coinSet = gameBoard.getCoinSet();
            coinGlow = gameBoard.getCoinGlow();
            coinBombGlow = gameBoard.getCoinBombGlow();
            coinBombIcons = gameBoard.getCoinBombIcons();
            maxColors = gameBoard.getMaxColors();
            //
            gMapHeight = mapHeight;
            gMapWidth = mapWidth;
            
            if ( lineBuffer == null )
            {
                lineBuffer = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                bufCanvas = new Canvas( lineBuffer );
            }
        }
    }
    
    public void setMainView( View mainView )
    {
        this.mainView = mainView;
        
        // Need to get the target views and do it ONCE!!!
        ConstraintLayout layout = mainView.findViewById( R.id.boardTargetHolder );
        
        if ( layout != null )
        {
            this.inHelper = false;
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
        
        //
        // Stop reloading this in onTouch
        //
/*
        matchHintLayout = mainView.findViewById( R.id.matchHintLayout );
        matchHintIcon = mainView.findViewById( R.id.matchHintIcon );
*/
        //        matchHintText = mainView.findViewById( R.id.matchHintText );
    }
    
    
    public void setTimer( CustomTimer timer )
    {
        this.timer = timer;
    }
    
    public void setLevelCompleted( boolean levelCompleted )
    {
        this.levelCompleted = levelCompleted;
    }
    
    public boolean isLevelCompleted()
    {
        return levelCompleted;
    }
    
    
    /**
     * //###############################
     * <p>
     * Setters for map sizes
     * <p>
     * //###############################
     */
    public void setHelperGrid( ArrayList<BoardTile> boardTiles, int mapWidth, int mapHeight, boolean inHelper )
    {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.inHelper = inHelper;
        
        if ( this.mapWidth > 0 && this.mapHeight > 0 )
        {
            this.boardTiles = boardTiles;
            coinSet = gameBoard.getCoinSet();
            coinGlow = gameBoard.getCoinGlow();
            maxColors = BoardTile.MAX_COINS;
            //
            gMapHeight = mapHeight;
            gMapWidth = mapWidth;
            
            matchList = new int[ this.mapHeight ][ this.mapWidth ];
            boardMap = new int[ mapHeight ][ mapWidth ];
            
            setColumnCount( gMapWidth );
            setRowCount( gMapHeight );
            
            if ( lineBuffer == null && getWidth() > 0 && getHeight() > 0 )
            {
                lineBuffer = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                bufCanvas = new Canvas( lineBuffer );
            }
        }
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
     * Draw the connecting line
     * <p>
     * //###############################
     *
     * @param canvas
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        if ( lineBuffer != null )
        {
            canvas.drawBitmap( lineBuffer, 0, 0, paint );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Restore all lines after a reset
     * <p>
     * //###############################
     *
     * @param bufCanvas
     */
    private void drawAllLines( Canvas bufCanvas )
    {
        if ( lineBuffer != null )
        {
            if ( tilePoints.size() > 1 )
            {
                for ( int i = 0; i < tilePoints.size() - 1; i++ )
                {
                    // Set the line we wish to use
                    BoardTile tile  = boardTiles.get( tilePoints.get( i ) );
                    PointXYZ  start = new PointXYZ();
                    PointXYZ  end   = new PointXYZ();
                    
                    //
                    start.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    start.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    tile = boardTiles.get( tilePoints.get( i + 1 ) );
                    end.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    end.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    subPaint.setColor( darkenColor( glowColors[ tile.tileNum % glowColors.length ], 1 ) );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
                    
                    paint.setColor( glowColors[ tile.tileNum % glowColors.length ] );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, paint );
                }
                
                // Show the lines
                invalidate();
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Default caller
     * <p>
     * //###############################
     */
    public void onDrawGrid()
    {
        onDrawGrid( inHelper );
    }
    
    
    /**
     * //###############################
     * <p>
     * My adapter, custom made
     * <p>
     * //###############################
     *
     * @param isHelper
     */
    public void onDrawGrid( boolean isHelper )
    {
        int                   size    = context.getResources().getDimensionPixelSize( R.dimen.TILE_SIZE );
        int                   padding = 0;//context.getResources().getDimensionPixelSize( R.dimen._1sdp );
        GridView.LayoutParams params;
        
        
        //##############################
        // Some times the user pauses
        // the game exit. Leave if exit
        // was too early!
        //##############################
        if ( boardTiles == null )
        {
            //            GameEngine.gamePaused = false;
            //            MainActivity.getActivity().getFragmentManager().popBackStack();
            return;
        }
        
        
        // Assist help function by NOT crashing!
        inHelper = isHelper;
        if ( !isHelper )
        {
            // Original board mapping
            Arrays.fill( tileTypes, 0 );
            
            // Clear gem count
            gemsToMatch.clear();
            blockersToMatch.clear();
            
            //
            GameEngine.arrayCopy( boardMap, matchList, mapHeight, mapWidth );
            
/*
            // Reset hints
            logicThread.setHintTimer( CustomTimer.currentTime );
*/
        }
        else
        {
            size = context.getResources().getDimensionPixelSize( R.dimen._32sdp );
        }
        
        
        // Set parameters
        params = new GridView.LayoutParams( size, size );
        //
        tilePoints.clear();
        totalGemsLeft = 0;
        
        
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
        
        //
        //###########################
        //
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
            
            //            image.cancelAnimationAndListeners();
            //            image.setOnTouchListener( this );
            //            image.setOnClickListener( this );
            //            image.setOnLongClickListener( this );
            
            // Anything swapped, fix it here
            image.setTranslationX( 0 );
            image.setTranslationY( 0 );
            image.setSoundEfx( -1 );
            
            
            //###################################
            //
            // Find and record all active gems
            //
            //###################################
            if ( image.getBlockerType() > 0 )
            {
                // Solve for blockers using this list
                // Takes precedence over GEMS since
                // Gems can be inside a blocker
                blockersToMatch.add( image.getPosition() );
                
                // Just in case a Gem is hiding in a blocker
                if ( image.tileNum >= BoardTile.PURPLE_GEM && image.tileNum <= BoardTile.YELLOW_GEM )
                {
                    gemsToMatch.add( image.getPosition() );
                }
            }
            else if ( image.tileNum >= BoardTile.PURPLE_GEM && image.tileNum <= BoardTile.YELLOW_GEM )
            {
                gemsToMatch.add( image.getPosition() );
            }
            
            
            //
            // Handle the initial board spawning
            //
            if ( image.getState() == BoardTile.STATE_INACTIVE )
            {
                // Blank image requested
                if ( image.tileNum < 0 )
                {
                    image.setVisibility( INVISIBLE );
                    // Need to use the custom image for "Overlay"
                    //                    image.setBackground( image.overlayImage );
                }
                else
                {
                    // If the tile is under a Blocker and cannot
                    // be re-spawned, replaced, keep active but also hidden
                    image.setImageResource( 0 );
                    image.setBackgroundResource( 0 );
                    //                image.clearAnimation();
                }
                
                image.setTag( null );
            }
            else
            {   // Helper data for Error checks also
                image.setScaleY( 1 );
                image.setScaleX( 1 );
                image.setRotation( 0 );
                image.setAlpha( 1f );
                //                image.clearAnimation();
                
                //
                image.setTag( null );
                image.setVisibility( VISIBLE );
                imageHelper( position );
                image.setState( BoardTile.STATE_ACTIVE );
                image.setSpecialItem( image.getSpecialItem() );
                
                // Keep count of standard coins
                if ( image.tileNum > -1 && image.tileNum < BoardTile.MAX_COINS )
                {
                    tileTypes[ image.tileNum ]++;
                }
            }
            
            // Now add the image to the GridLayout
            image.setId( position );
            image.setPosition( position );
            image.pointPosi = -1;
            
            // Padding to Image view to keep BG image
            // Visible behind Foreground image
            image.setPadding( padding, padding, padding, padding );
            addView( image, params );
            
            //
            if ( position == 0 )
            {
                image.requestLayout();
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Default caller
     * <p>
     * //###############################
     */
    public void onUpdateGrid()
    {
        onUpdateGrid( inHelper );
    }
    
    
    /**
     * //###############################
     * <p>
     * Update the grid
     * <p>
     * //###############################
     */
    public void onUpdateGrid( boolean isHelper )
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
        int[]   targets      = new int[ coinSet.length ];
        
        
        // Will crash if helper displaying
        if ( !isHelper )
        {
            // Calculate the score to send to the parent view group
            for ( int i = 0; i < coinSet.length; i++ )
            {
                targets[ i ] = 0;
            }
            
/*
            // Reset hints
            logicThread.setHintTimer( CustomTimer.currentTime );
*/
        }
        
        
        //####################################
        //
        // Draw each image
        //
        //####################################
        for ( int position = 0; position < boardTiles.size() && !GameEngine.isKilled; position++ )
        {
            final BoardTile image;
            final int       index = position;
            Animation       anim;
            
            
            // Is the game completed already??
            if ( levelCompleted )
            {
                return;
            }
            
            //
            image = boardTiles.get( position );
            
            //######################################
            //
            // Handle the board re-spawning
            //
            //######################################
            if ( image.getState() == BoardTile.CREATE_ITEM || image.getState() == BoardTile.MASTER_ITEM )
            {   //
                image.animate().setInterpolator( new LinearInterpolator() );
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                
                //                image.endAnimation();
                image.animate().withLayer().setInterpolator( new LinearInterpolator() );
                image.animate().translationX( image.swapTO.x ).translationY( image.swapTO.y ).setDuration( AnimationValues.SWAP_SPEED );
                image.animate().withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        // Standard
                        if ( image.getState() == BoardTile.CREATE_ITEM )
                        {
                            image.setSpecialItem( -1 );
                            image.specialTile = -1;
                            // Child: Still needs to drop
                            image.setVisibility( INVISIBLE );
                            image.setState( BoardTile.STATE_INACTIVE );
                        }
                        else
                        {
                            // Master: Still needs to drop
                            image.setVisibility( VISIBLE );
                            image.setState( BoardTile.STATE_ACTIVE );
                            
                            // Announce its arrival!
                            //                            image.specialTile = image.swapTO.data;
                            imageHelper( image.getPosition() );
                            image.setScaleX( 0f );
                            image.setScaleY( 0f );
                            image.animate().scaleY( 1f ).scaleX( 1f ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                            image.animate().setDuration( 500 ).start();
                            //                            image.setTag( anim );
                            // Create Special Sound
                            if ( gameEngine.soundPlayer != null && !isHelper )
                            {
                                gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_CREATED );
                            }
                        }
                        //
                        image.swapTO.x = 0;
                        image.swapTO.y = 0;
                        image.setTranslationX( 0 );
                        image.setTranslationY( 0 );
                        image.setPivotX( image.getWidth() / 2f );
                        image.setPivotY( image.getHeight() / 2f );
                        //
                        image.animator = null;
                        //image.clearAnimation();
                        //
                        
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {   // ALWAYS create special items OR process matches after REAL SWAP
                                logicThread.animationsRunning = 0;
                                
                                // This can be called from STATE_MATCHED also
                                // but not together. One or the other
                                logicThread.addToStack( LogicThread.CMD_DROP );
                            }
                        }
                    }
                } ).start();
                //
                
                logicThread.animationsRunning++;
                //
                if ( !isHelper )
                {
                    // Add to the score
                    runningScore += (GameEngine.POINTS_PER_TILE * GameEngine.pointsMultiplier);
                    
                    if ( image.tileNum < BoardTile.MAX_COINS )
                    {
                        adjustTargetCounts( targets, image.tileNum );
                    }
                    
                    //
                    matchMade = true;
                }
            }
            else if ( image.getState() == BoardTile.MASTER_ITEM_TO_GIVE )
            {   //
                image.animate().setInterpolator( new LinearInterpolator() );
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                
                //                image.endAnimation();
                image.animate().withLayer().setInterpolator( new LinearInterpolator() );
                image.animate().translationX( image.swapTO.x ).translationY( image.swapTO.y ).setDuration( AnimationValues.SWAP_SPEED );
                image.animate().withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
/*
                        ViewGroup       group         = mainView.findViewById( R.id.specialParent );
                        int             index         = (image.tileNum - BoardTile.BOMB);
                        float           transY;
                        float           transX;
                        int             count         = group.getChildCount();
                        int             currentView   = 0;
                        ImageTextView[] imageTextView = new ImageTextView[ count ];
                        
                        
                        // Get all the special item views
                        for ( int i = 0; i < count; i++ )
                        {
                            View v = group.getChildAt( i );
                            
                            if ( v instanceof ImageTextView )
                            {
                                imageTextView[ currentView ] = ( ImageTextView ) v;
                                currentView++;
                            }
                        }
                        
                        //
                        final ImageTextView temp = imageTextView[ index ];
                        image.setPivotX( image.getWidth() / 2f );
                        image.setPivotY( image.getHeight() / 2f );
                        
                        // Animate the item going to where it is housed
                        int[] vLoc     = new int[ 2 ];
                        int[] imageLoc = new int[ 2 ];
                        
                        temp.getLocationOnScreen( vLoc );
                        image.getLocationOnScreen( imageLoc );
                        
                        transX = vLoc[ 0 ] - imageLoc[ 0 ];
                        transY = vLoc[ 1 ] - imageLoc[ 1 ];
                        
                        // Show the special's symbol
                        imageHelper( image.getPosition() );
                        
                        //#################################
                        //
                        // Move the item to the player
                        //
                        //#################################
                        image.animate().translationX( transX ).scaleX( .5f );
                        image.animate().translationY( transY ).scaleY( .5f );
                        image.animate().setDuration( 350 ).setInterpolator( new AccelerateInterpolator( 1f ) );
                        image.animate().withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                image.setImageResource( 0 );
                                
                                //@@@@@@@@@@@@@@@@@@@ Add to the stash
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.play( PlaySound.POINTS_ADD );
                                }
                                
                                
                                //#################################
                                //
                                // Master: Still needs to drop
                                //
                                //#################################
                                image.setVisibility( VISIBLE );
                                image.setState( BoardTile.STATE_ACTIVE );
                                
                                
                                //
                                int i = r.nextInt( maxColors );
                                image.tileNum = i;
                                image.specialItem = 0;
                                image.specialTile = -1;
                                
                                image.swapTO.x = 0;
                                image.swapTO.y = 0;
                                image.setTranslationX( 0 );
                                image.setTranslationY( 0 );
                                image.setPivotX( image.getWidth() / 2f );
                                image.setPivotY( image.getHeight() / 2f );
                                //
                                image.animator = null;
                                //image.clearAnimation();
                                //
                                
                                if ( logicThread != null )
                                {
                                    logicThread.animationsRunning--;
                                    
                                    if ( logicThread.animationsRunning <= 0 )
                                    {   // ALWAYS create special items OR process matches after REAL SWAP
                                        logicThread.animationsRunning = 0;
                                        
                                        // This can be called from STATE_MATCHED also
                                        // but not together. One or the other
                                        logicThread.addToStack( LogicThread.CMD_DROP );
                                    }
                                }
                                
                                //
                                gameEngine.Boosters[ index ]++;
                                
                                // Add the numbers to the special and make it respond
                                temp.setText( String.format( Locale.getDefault(), "%d", gameEngine.Boosters[ index ] ) );
                                temp.setScaleX( 0 );
                                temp.setScaleY( 0 );
                                temp.setAlpha( 0f );
                                temp.animate().alpha( 1 ).scaleX( 1 ).scaleY( 1 );
                                temp.animate().setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
                                temp.animate().setDuration( 250 ).start();
                            }
                        } ).start();
*/


/*
                        //#################################
                        // Master: Still needs to drop
                        //#################################
                        image.setVisibility( VISIBLE );
                        image.setState( BoardTile.STATE_ACTIVE );
                        
                        // Announce its arrival!
                        imageHelper( image.getPosition() );
                        //
                        image.swapTO.x = 0;
                        image.swapTO.y = 0;
                        image.setTranslationX( 0 );
                        image.setTranslationY( 0 );
                        image.setPivotX( image.getWidth() / 2f );
                        image.setPivotY( image.getHeight() / 2f );
                        //
                        image.animator = null;
                        //image.clearAnimation();
                        //
                        
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {   // ALWAYS create special items OR process matches after REAL SWAP
                                logicThread.animationsRunning = 0;
                                
                                // This can be called from STATE_MATCHED also
                                // but not together. One or the other
                                logicThread.addToStack( LogicThread.CMD_DROP );
                            }
                        }
*/
                    }
                } ).start();
                //
                
                logicThread.animationsRunning++;
                //
                if ( !isHelper )
                {
                    // Add to the score
                    runningScore += (GameEngine.POINTS_PER_TILE * GameEngine.pointsMultiplier);
                    
                    if ( image.tileNum >= BoardTile.BOMB && image.specialTile > -1 )
                    {
                        adjustTargetCounts( targets, image.specialTile );
                    }
                    else
                    {
                        adjustTargetCounts( targets, image.tileNum );
                    }
                    //
                    matchMade = true;
                }
            }
            else if ( image.getState() == BoardTile.ANNOUNCE_PRESENCE )
            {
                if ( image.getSoundEfx() == -1 && !isHelper )
                {
                    //@@@@@@@@@@@@@@@@@ Create Special Sound
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_CREATED );
                    }
                }
                //
                image.setSoundEfx( -1 );
                
                //
                image.setState( BoardTile.STATE_ACTIVE );
                // Announce its arrival!
                imageHelper( image.getPosition() );
                //
                image.setScaleX( .5f );
                image.setScaleY( .5f );
                image.setAlpha( 0.5f );
                image.animate().alpha( 1f ).scaleX( 1f ).scaleY( 1f ).setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
                image.animate().setDuration( 500 ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.setScaleY( 1 );
                        image.setScaleX( 1 );
                        image.animDelay = 0;
                        image.animate().setStartDelay( 0 );
                    }
                } ).start();
            }
            else if ( image.getState() == BoardTile.USE_SPECIAL_ITEM )
            {
                boolean canMatch = true;
                
                switch ( image.getSpecialItem() )
                {
                    // TODO: Clock support needed
                    //                    case BoardTile.
                    
                    // TODO: Rainbow coin support needed
                    case BoardTile.SWEEPER_COIN_VERTICAL:
                    case BoardTile.SWEEPER_COIN_HORIZONTAL:
                        useABolt( image, image.animDelay, (image.getSpecialItem() - BoardTile.SWEEPER_COIN_VERTICAL), image.tileNum );
                        break;
                    case BoardTile.RAINBOW_SEEKER:
                        useChargeBeam( image );
                        break;
                    default:
                        Toast.makeText( getContext(), "Special not supported: " + image.tileNum, Toast.LENGTH_SHORT ).show();
                        canMatch = false;
                        
                        break;
                }
                
                // Animate the 'SIGNAL_MATCHES' with whatever the special did
                if ( canMatch )
                {
                    // MUST RESTART THIS METHOD NOW OR ALL TILES AFTER
                    // INITIATOR TILE WILL FIRE OUT OF SYNC; IE: multiple specials
                    //
                    // DOES NOT APPLY TO CLOCK Because it is auto activated
                    // And matched on drop is possible which messes up
                    // logicThread.animationRunning
                    // Reset to start messes with coins awaiting DROP
                    position = -1;
                    
                    // Kill the other possible match animations and restart that as well
                    logicThread.animationsRunning = 0;
                    continue;
                }
            }
            else if ( image.getState() == BoardTile.USE_SPECIAL_ITEM_NEXT )
            {
                image.setState( BoardTile.STATE_ACTIVE );
            }
            else if ( image.getState() == BoardTile.STATE_RESPAWN )
            {
                // Drop from the top
                // Now in dropping state
                image.setState( BoardTile.STATE_DROPPING );
                
                
                //###############################
                //
                // If no Gems to match are
                // present, Add some!
                //
                //###############################
                if ( gemsToMatch.size() == 0 && /*gameBoard.maxGems > 0*/ getTotalGemsLeft() > 0 )
                {
                    for ( View view : targetViews )
                    {
                        final PointXYZ temp = ( PointXYZ ) view.getTag();
                        
                        //
                        // Make sure the Gem we want is active in game
                        //
                        if ( view.getVisibility() == VISIBLE && temp.getTag() == null && (temp.y >= BoardTile.PURPLE_GEM && temp.y <= BoardTile.YELLOW_GEM) && temp.x > 0 )
                        {
                            // Gem creation: Make this newly spawn tile a Jewel!
                            image.tileNum = temp.y;
                            // Set image
                            image.setBackgroundResource( GameBoard.coinSet[ temp.y ] );
                            // Designate it a Jewel
                            image.setSpecialItem( BoardTile.GEM_COIN );
                            // Add to the list that needs to be solved!
                            gemsToMatch.add( image.getPosition() );
                        }
                        
/*
                        if ( gemsToMatch.size() == 0 )
                        {
                            gameBoard.maxGems = 0;
                        }
*/
                    }
                }
                else
                {
                    //##################################
                    //
                    // Need to favor the tiles that need
                    // to be solved the most
                    //
                    //##################################
                    int     i     = r.nextInt( maxColors );
                    boolean found = false;
                    Point   high  = new Point( 0, 0 );
                    
                    //
                    // Don't do every time down.
                    // keep some randomness
                    //
                    if ( r.nextInt( 3 ) == 1 )
                    {
                        int c = 0;
                        
                        for ( View v : targetViews )
                        {
                            //
                            if ( v.getVisibility() == VISIBLE && c < BoardTile.MAX_COINS && v.getTag() != null )
                            {
                                final PointXYZ target = ( PointXYZ ) v.getTag();
                                
                                int difference = (target.x - tileTypes[ c ]);
                                
                                // MUST be a color that has a high target range
                                // MUST be a color with a high range, but less
                                // colors on board
                                //                        if ( target.x > high.x &&  tileTypes[ c ] < onBoardLow )
                                if ( high.x < difference )
                                {
                                    high.x = target.x;
                                    high.y = c;
                                    found = true;
                                }
                            }
                            
                            //
                            c++;
                        }
                    }
                    
                    // Do we have a target that needs attention?
                    // Fixed duplicate color issue
                    if ( found )
                    {
                        i = high.y;
                    }
                    
                    //#####################################
                    //
                    // Must add this color to the stack
                    //
                    //#####################################
                    if ( image.tileNum < BoardTile.MAX_COINS )
                    {
                        tileTypes[ i ]++;
                    }
                    
                    image.tileNum = i;
                    image.setSpecialItem( 0 );
                    image.specialTile = -1;
                    image.resetCoin();
                }
                
                //
                imageHelper( image.getPosition() );
                image.setVisibility( VISIBLE );
                //
                image.animate().withLayer().setInterpolator( new LinearInterpolator() );
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                //
                //                                image.setTranslationY( -image.getBottom() );
                //                image.setTranslationY( -image.getHeight() );
                //                image.setTranslationY( -getHeight() );
                image.setTranslationY( -(getTop() + image.getHeight()) );
                //                image.animate().setDuration( AnimationValues.DROP_TIME / 2 );
                image.animate().setDuration( AnimationValues.DROP_TIME );
                image.animate().translationY( 0 ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.setVisibility( VISIBLE );
                        image.setState( BoardTile.STATE_ACTIVE );
                        image.animator = null;
                        image.animDelay = 0;
                        image.animate().setStartDelay( 0 );
                        //                        Log.d( "Grid Update", "SPAWNED: " + image.getPosition() );
                        
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {   // ALWAYS test again after DROP / RESPAWN complete
                                logicThread.animationsRunning = 0;
                                //                            logicThread.addToStack( LogicThread.CMD_TEST_AGAIN );
                                logicThread.addToStack( LogicThread.CMD_IDLE );
                            }
                        }
                    }
                } ).start();
                
                //                Log.d( "Grid Update", "SPAWNING: " + image.getPosition() );
                // Add to the animation counter
                logicThread.animationsRunning++;
            }
            else if ( image.getState() == BoardTile.STATE_DROP )
            {
                // Drop from the current spot
                // Items with extra BG effect start them NOW
                imageHelper( image.getPosition() );
                
                // Offsets tile will animate TO
                final float toX = (image.getX() - image.swapTO.x);
                final float toY = (image.getY() - image.swapTO.y);
                
                // The offsets tile will animate FROM
                image.setTranslationX( -toX );
                image.setTranslationY( -toY );
                //                image.endAnimation();
                image.resetCoin();
                
                // Now in dropping state
                image.setState( BoardTile.STATE_DROPPING );
                image.setVisibility( VISIBLE );
                //
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                //
                //                image.animate().withLayer();//.setStartDelay( image.getDelayTime() );
                image.animate().translationX( 0 ).translationY( 0 ).setInterpolator( new LinearInterpolator() );
                image.animate().setDuration( AnimationValues.DROP_TIME ).withEndAction( new Runnable()
                        //                image.animate().setDuration( AnimationValues.DROP_TIME / 2 ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.animDelay = 0;
                        image.animate().setStartDelay( 0 );
                        image.setState( BoardTile.STATE_ACTIVE );
                        image.setVisibility( VISIBLE );
                        //
                        //Log.d( "Grid Update", "DROPPED: " + image.getPosition() );
                        
                        if ( logicThread != null )
                        {
                            
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {   // ALWAYS test again after DROP / RESPAWN complete
                                logicThread.animationsRunning = 0;
                                //                            logicThread.addToStack( LogicThread.CMD_TEST_AGAIN );
                                logicThread.addToStack( LogicThread.CMD_IDLE );
                            }
                        }
                    }
                } ).start();
                
                logicThread.animationsRunning++;
                //Log.d( "Grid Update", "DROPPING: " + image.getPosition() );
            }
            // Draw standard board symbol
            else if ( image.getState() == BoardTile.STATE_ACTIVE )
            {
                //                image.endAnimation();
                imageHelper( image.getPosition() );
            }
            else if ( image.getState() == BoardTile.STATE_MATCHED )
            {
                View     v;
                PointXYZ p;
                
                //
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                
                
                //#################################
                //
                // Do not run this in Help mode
                //
                //#################################
                if ( !isHelper )
                {
                    v = targetViews.get( image.tileNum );
                    p = ( PointXYZ ) v.getTag();
                }
                else
                {
                    v = null;
                    p = null;
                }
                
                
                // Slide to the coin??
                if ( p != null && p.x > 0 )
                {
                    int[] vLoc     = new int[ 2 ];
                    int[] imageLoc = new int[ 2 ];
                    
                    v.getLocationOnScreen( vLoc );
                    image.getLocationOnScreen( imageLoc );
                    
                    //SHOULD THIS BE KEPT??
                    //##################################
                    //
                    // Slide to the target
                    //
                    //##################################
                    //                    image.animate().setStartDelay( image.getTileY() * 100 );
                    BoardTile                tile      = new BoardTile( getContext() );
                    FrameLayout.LayoutParams params;
                    FrameLayout              container = mainView.findViewById( R.id.boardGridSpecials );
                    
                    // Create a TEMP new tile to animate while real tile
                    // is respawning
                    tile.setBackground( image.getBackground() );
                    tile.setVisibility( View.VISIBLE );
                    
                    // Move the tile to the new location
                    // then animate a move from old location
                    params = new FrameLayout.LayoutParams( image.getWidth(), image.getHeight() );
                    container.addView( tile, params );
                    
                    // Position over target item
                    int l = vLoc[ 0 ] - getLeft();
                    int t = vLoc[ 1 ] - getTop();
                    params.setMargins( l, t, l + image.getWidth(), t + image.getHeight() );
                    tile.setLayoutParams( params );
                    tile.setScaleX( 1.5f );
                    tile.setScaleY( 1.5f );
                    
                    // Translate to old position
                    tile.setTranslationX( -(l - image.getLeft()) );
                    tile.setTranslationY( -(t - image.getTop()) );
                    
                    tile.gotoTarget( AnimationValues.SWOOP_SPEED, image.gotoTargetDelay, new AnticipateInterpolator( 1 ) ).addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            
                            container.removeView( tile );
                        }
                    } );
                }
                
                
                //
                // Animate with a particle effect
                //
                // Use the particles, not an image.
                image.animate().setStartDelay( image.animDelay );
                image.animate().withLayer().setDuration( AnimationValues.DROP_TIME ).scaleY( 0 ).scaleX( 0 );
                image.animate().withStartAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ParticleSystem parts;
                        // Activating tile
                        if ( image.getTag() != null )
                        {
                            parts = new ParticleSystem( ( Activity ) getContext(), 10, R.drawable.particle_special, 750 ).setSpeedRange( 0.01f, 0.05f );
                            parts.setRotationSpeed( .5f );
                            //                            image.setTag( null );
                        }
                        else
                        {
                            parts = new ParticleSystem( ( Activity ) getContext(), 10, GameBoard.coinParticles[ image.tileNum ], 750 ).setSpeedRange( 0.01f, 0.05f );
                        }
                        
                        parts.setFadeOut( 750 );
                        parts.oneShot( image, 10 );
                        
                        // Remove the tile as particles display
                        image.setImageResource( 0 );
                    }
                } );
                image.animate().setInterpolator( new AnticipateOvershootInterpolator( 1f ) ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.animDelay = 0;
                        //
                        image.setVisibility( INVISIBLE );
                        image.setState( BoardTile.STATE_INACTIVE );
                        image.animator = null;
                        image.setSpecialItem( -1 );
                        image.specialTile = -1;
                        image.clearAnimation();
                        //                        image.cancelAnimationAndListeners();
                        image.setTag( null );
                        
                        //
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {
                                // ALWAYS call DROP / RESPAWN after MATCHES CLEARED complete
                                logicThread.animationsRunning = 0;
                                logicThread.addToStack( LogicThread.CMD_DROP );
                            }
                        }
                    }
                } ).start();
                
                
                //##################################
                //
                // Add to the animation counter
                //
                //##################################
                if ( gameEngine.soundPlayer != null && !isHelper && logicThread.animationsRunning == 0 )
                {
                    if ( image.getTag() != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                    }
                    else
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.COIN_MATCH );
                    }
                    
                    //
                    //                    image.setTag( null );
                }
                
                //
                logicThread.animationsRunning++;
                
                if ( !isHelper )
                {
                    // Add to the score
                    runningScore += (GameEngine.POINTS_PER_TILE * GameEngine.pointsMultiplier);
                    
                    // Adjust targets
                    adjustTargetCounts( targets, image.tileNum );
                    matchMade = true;
                }
            }
            else if ( image.getState() == BoardTile.GEM_MATCHED )
            {
                View     v;
                PointXYZ p;
                
                //
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                
                //#################################
                //
                // Do not run this in Help mode
                //
                //#################################
                if ( !isHelper )
                {
                    v = targetViews.get( image.tileNum );
                    p = ( PointXYZ ) v.getTag();
                }
                else
                {
                    v = null;
                    p = null;
                }
                
                
                // Slide to the coin??
                if ( p != null && p.x > 0 )
                {
                    int[] vLoc     = new int[ 2 ];
                    int[] imageLoc = new int[ 2 ];
                    
                    v.getLocationOnScreen( vLoc );
                    image.getLocationOnScreen( imageLoc );
                    
                    //SHOULD THIS BE KEPT??
                    //##################################
                    //
                    // Slide to the target
                    //
                    //##################################
                    //                    image.animate().setStartDelay( image.getTileY() * 100 );
                    BoardTile                tile      = new BoardTile( getContext() );
                    FrameLayout.LayoutParams params;
                    FrameLayout              container = mainView.findViewById( R.id.boardGridSpecials );
                    
                    // Create a TEMP new tile to animate while real tile
                    // is respawning
                    tile.setBackground( image.getBackground() );
                    tile.setVisibility( View.VISIBLE );
                    
                    // Move the tile to the new location
                    // then animate a move from old location
                    params = new FrameLayout.LayoutParams( image.getWidth(), image.getHeight() );
                    container.addView( tile, params );
                    
                    // Position over target item
                    int l = vLoc[ 0 ] - getLeft();
                    int t = vLoc[ 1 ] - getTop();
                    params.setMargins( l, t, l + image.getWidth(), t + image.getHeight() );
                    tile.setLayoutParams( params );
                    tile.setScaleX( 1.5f );
                    tile.setScaleY( 1.5f );
                    
                    // Translate to old position
                    tile.setTranslationX( -(l - image.getLeft()) );
                    tile.setTranslationY( -(t - image.getTop()) );
                    
                    tile.gotoTarget( AnimationValues.SWOOP_SPEED, image.gotoTargetDelay, new AnticipateInterpolator( 1 ) ).addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            
                            container.removeView( tile );
                        }
                    } );
                }
                
                //
                // Animate with a particle effect
                //
                // Use the particles, not an image.
                image.animate().setStartDelay( image.animDelay );
                image.animate().withLayer().setDuration( AnimationValues.DROP_TIME ).scaleY( 0 ).scaleX( 0 );
                image.animate().withStartAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ParticleSystem parts = new ParticleSystem( ( Activity ) getContext(), 10, GameBoard.coinParticles[ image.tileNum ], 750 ).setSpeedRange( 0.01f, 0.05f );
                        parts.setFadeOut( 750 );
                        parts.oneShot( image, 10 );
                        
                        // Remove the tile as particles display
                        image.setImageResource( 0 );
                    }
                } );
                image.animate().setInterpolator( new AnticipateOvershootInterpolator( 1f ) ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.animDelay = 0;
                        //
                        image.setVisibility( INVISIBLE );
                        image.setState( BoardTile.STATE_INACTIVE );
                        image.animator = null;
                        image.setSpecialItem( -1 );
                        image.specialTile = -1;
                        image.clearAnimation();
                        //                        image.cancelAnimationAndListeners();
                        image.setTag( null );
                        
                        //
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {
                                // ALWAYS call DROP / RESPAWN after MATCHES CLEARED complete
                                logicThread.animationsRunning = 0;
                                logicThread.addToStack( LogicThread.CMD_DROP );
                            }
                        }
                    }
                } ).start();
                
                
                //##################################
                //
                // Add to the animation counter
                //
                //##################################
                if ( gameEngine.soundPlayer != null && !isHelper )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                }
                
                
                //##################################
                //
                // Add to the animation counter
                //
                //##################################
                logicThread.animationsRunning++;
                
                if ( !isHelper )
                {
                    // Add to the score
                    runningScore += (GameEngine.POINTS_PER_GEM * GameEngine.pointsMultiplier);
                    
                    // Adjust targets
                    adjustTargetCounts( targets, image.tileNum );
                    matchMade = true;
                }
            }
            else if ( image.getState() == BoardTile.BLOCKER_MATCHED )
            {
                //
                // No damn double hits!
                //
                if ( image.blockerAdjusted )
                {
                    continue;
                }
                
                //
                image.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( image.animator == null )
                        {
                            image.animator = animation;
                        }
                    }
                } );
                // Animate with a particle effect
                //
                // Use the particles, not an image.
                image.animate().setStartDelay( image.animDelay );
                image.animate().withLayer().setDuration( AnimationValues.DROP_TIME ).scaleY( 1 );
                image.animate().withStartAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Select the blocker particle effect
                        if ( image.getBlockerType() == BoardTile.COIN_STACK || image.getBlockerType() == BoardTile.CHEST_BLOCKER )
                        {
                            // Using the stacks!
                            int color = image.getBlockerColor();
                            
                            ParticleSystem parts;
                            parts = new ParticleSystem( ( Activity ) getContext(), 16, GameBoard.coinParticles[ color ], 750 ).setSpeedRange( 0.05f, 0.1f );
                            parts.setFadeOut( 750 );
                            parts.oneShot( image, 10 );
                        }
                        else if ( image.getBlockerType() != BoardTile.BARREL_BLOCKER )
                        {
                            int[]            partArrays = new int[]{ R.array.shell_particles, R.array.rock_particles, R.array.ice_particles };
                            TypedArray       a          = getResources().obtainTypedArray( partArrays[ image.getBlockerType() - BoardTile.SHELL_BLOCKER ] );
                            int              count      = a.length();
                            ParticleSystem[] parts      = new ParticleSystem[ a.length() ];
                            
                            for ( int i = 0; i < count; i++ )
                            {
                                parts[ i ] = new ParticleSystem( ( Activity ) getContext(), 1, a.getResourceId( i, 0 ), 500 ).setSpeedRange( 0.05f, 0.1f );
                                parts[ i ].setFadeOut( 500 );
                                parts[ i ].setRotationSpeed( .5f );
                                parts[ i ].oneShot( image, 1 );
                            }
                            
                            a.recycle();
                            
                            // Remove 2 of 3 images only
                            if ( image.getBlockerType() == BoardTile.SHELL_BLOCKER || image.getBlockerType() == BoardTile.ROCK_BLOCKER )
                            {
                                image.clearCoin();
                            }
                        }
                        else if ( image.getBlockerType() == BoardTile.BARREL_BLOCKER )
                        {
                            if ( image.getBlockerCount() == 1 )
                            {
                                ParticleSystem parts;
                                
                                // TODO: Make the barrel parts into particles
                                //                                 Currently the third barrel does not adjust properly!
                                // When using a multi hit special?
                                parts = new ParticleSystem( ( Activity ) getContext(), 16, R.drawable.particle_special, 750 ).setSpeedRange( 0.05f, 0.1f );
                                parts.setFadeOut( 750 );
                                parts.oneShot( image, 10 );
/*
                                int[]                array  = GameEngine.arrayFromResource( getContext(), R.array.barrel_particles );
                                final ObjectAnimator barrel = ObjectAnimator.ofInt( image, "backgroundResource", array );
                                
                                barrel.setDuration( 500 ).setInterpolator( new LinearInterpolator() );
                                barrel.setEvaluator( new CustomEvaluator() );
                                barrel.addListener( new AnimatorListenerAdapter()
                                {
                                    @Override
                                    public void onAnimationEnd( Animator animation )
                                    {
                                        barrel.removeAllListeners();
                                        gameEngine.animatorList.remove( barrel );
    
                                        super.onAnimationEnd( animation );
                                    }
                                } );
                                
                                //
                                gameEngine.animatorList.add( barrel );
                                barrel.start();
*/
                            }
                        }
                        
                        // Decrease the counter
                        image.adjustBlocker();
                        
                        // Signal a hit already!
                        image.blockerAdjusted = true;
                    }
                } );
                image.animate().setInterpolator( new LinearInterpolator() ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if ( GameEngine.isKilled )
                        {
                            return;
                        }
                        
                        image.animDelay = 0;
                        
                        // Blocker with hidden items will
                        // return "-1" flag
                        if ( image.getBlockerType() == 0 )
                        {
                            image.setVisibility( INVISIBLE );
                            image.setState( BoardTile.STATE_INACTIVE );
                        }
                        else if ( image.getBlockerType() < 0 )
                        {
                            image.setVisibility( VISIBLE );
                            image.setState( BoardTile.STATE_ACTIVE );
                            image.setBlockerType( 0 );
                        }
                        else
                        {
                            image.setState( BoardTile.STATE_ACTIVE );
                        }
                        
                        image.animator = null;
                        image.setSpecialItem( -1 );
                        image.specialTile = -1;
                        image.clearAnimation();
                        //                        image.cancelAnimationAndListeners();
                        image.setTag( null );
                        image.blockerAdjusted = false;
                        
                        //
                        if ( logicThread != null )
                        {
                            logicThread.animationsRunning--;
                            
                            if ( logicThread.animationsRunning <= 0 )
                            {
                                // ALWAYS call DROP / RESPAWN after MATCHES CLEARED complete
                                logicThread.animationsRunning = 0;
                                logicThread.addToStack( LogicThread.CMD_DROP );
                            }
                        }
                    }
                } ).start();
                
                
                //##################################
                //
                // Add to the animation counter
                //
                //##################################
                if ( gameEngine.soundPlayer != null && !isHelper )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                }
                
                
                //##################################
                //
                // Add to the animation counter
                //
                //##################################
                logicThread.animationsRunning++;
                
                if ( !isHelper )
                {
                    // Add to the score
                    runningScore += (GameEngine.POINTS_PER_GEM * GameEngine.pointsMultiplier);
                    matchMade = true;
                }
            }
            else if ( image.getState() == BoardTile.SPECIAL_ANIMATE )
            {
                if ( image.getTag() != null )
                {
                    animatorHelper( position );
                    
                    if ( !isHelper )
                    {
                        // Add to the score
                        runningScore += (GameEngine.POINTS_PER_TILE * GameEngine.pointsMultiplier);
                        
                        // Adjust targets
                        adjustTargetCounts( targets, image.tileNum );
                        matchMade = true;
                    }
                }
            }
            // Blank image requested
            else if ( image.getState() == BoardTile.STATE_INACTIVE )
            {
                if ( image.tileNum < 0 )
                {
                    //                image.setImageResource( 0 );
                    //                    image.setBackgroundResource( 0 );
                    image.setTag( null );
                    image.setSpecialItem( -1 );
                    image.specialTile = -1;
                    image.setBackground( image.overlayImage );
                }
                else //if ( !objStillDropping )
                {
                    // Do nothing but return to idle
                    // Show no tile!
                    image.animate().translationX( 0 ).translationY( 0 ).setInterpolator( new LinearInterpolator() );
                    image.animate().setDuration( AnimationValues.DROP_TIME / 2 ).withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( GameEngine.isKilled )
                            {
                                return;
                            }
                            
                            image.animDelay = 0;
                            image.animate().setStartDelay( 0 );
                            //                            image.setState( BoardTile.STATE_ACTIVE );
                            image.setVisibility( VISIBLE );
                            image.setTag( null );
                            
                            //
                            //Log.d( "Grid Update", "DROPPED: " + image.getPosition() );
                            
                            if ( logicThread != null )
                            {
                                logicThread.animationsRunning--;
                                
                                if ( logicThread.animationsRunning <= 0 )
                                {   // ALWAYS test again after DROP / RESPAWN complete
                                    logicThread.animationsRunning = 0;
                                    //                            logicThread.addToStack( LogicThread.CMD_TEST_AGAIN );
                                    if ( objStillDropping )
                                    {
                                        logicThread.addToStack( LogicThread.CMD_DROP );
                                    }
                                    else
                                    {
                                        logicThread.addToStack( LogicThread.CMD_IDLE );
                                    }
                                }
                            }
                        }
                    } ).start();
                    
                    logicThread.animationsRunning++;
                    //                    objStillDropping = true;
                    // If the tile is under a Blocker and cannot
                    // be re-spawned, replaced, keep active but also hidden
                    image.setBackgroundResource( 0 );
                }
                
                image.setImageResource( 0 );
            }
            
            //
            // Now add the image to the GridLayout
            //
            if ( position == 0 )
            {
                image.requestLayout();
            }
        }
        
        // Update score and make sounds!
        if ( runningScore > 0 && onGridUpdateListener != null && !isHelper )
        {
            GameEngine.pointsMultiplier = tilePoints.size();
            
            onGridUpdateListener.onGridUpdated( SCORE_UPDATE, runningScore );
            //            onGridUpdateListener.onGridUpdated( BONUS_SPLASH, GameEngine.pointsMultiplier );
            onGridUpdateListener.onGridUpdated( MOVES_UPDATE, GameEngine.boardMoves );
        }
        
        // To use for "target reached" checking
        if ( matchMade && !isHelper )
        {
            adjustTargets( targets );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Get the total gem count remaining
     * <p>
     * //###############################
     *
     * @return
     */
    private int getTotalGemsLeft()
    {
        int count = 0;
        
        for ( int i = BoardTile.MAX_COINS; i < targetViews.size(); i++ )
        {
            ImageTextView  view = ( ImageTextView ) targetViews.get( i );
            final PointXYZ temp = ( PointXYZ ) view.getTag();
            
            if ( view.getVisibility() == VISIBLE && temp != null )
            {
                // Get the amounts remaining
                count += temp.x;
            }
        }
        
        return count;
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
        
        //##################################
        //
        // Standard Tile
        //
        //##################################
        if ( image.getBlockerType() < 1 )
        {
            // Specials: Sweep layer masking
            if ( image.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && image.getSpecialItem() < BoardTile.RAINBOW_SEEKER )
            {
                image.setBackgroundResource( coinBombIcons[ image.tileNum % coinBombIcons.length ] );
                // Arrows to use
                image.setImageResource( (image.getSpecialItem() == BoardTile.SWEEPER_COIN_HORIZONTAL ? R.drawable.arrow_hori : R.drawable.arrow_vert) );
            }
            else
            {
                image.setBackgroundResource( coinSet[ image.tileNum % coinSet.length ] );
                image.setImageResource( 0 );
            }
        }
        else
        {
            //
            // Blockers
            //
            if ( image.getBlockerType() == BoardTile.COIN_STACK )
            {
                // Replacement Blocker:
                //     Draw each stack level if their is a count
                //     If it's a single item, draw in BG Resource
                image.setImageResource( 0 );
                image.setBackgroundResource( 0 );
            }
            else if ( image.getBlockerType() == BoardTile.SHELL_BLOCKER )
            {
                // Replacement Blocker
                image.setBackgroundResource( R.drawable.shell_blocker );
            }
            else if ( image.getBlockerType() == BoardTile.ROCK_BLOCKER )
            {
                // Replacement Blocker
                image.setBackgroundResource( R.drawable.rock_blocker );
            }
            //            else if ( image.getBlockerType() == BoardTile.ICE_BLOCKER || image.getBlockerType() == BoardTile.BARREL_BLOCKER )
            else if ( image.getBlockerType() == BoardTile.ICE_BLOCKER )
            {
                // Overlay Blocker
                image.setBackgroundResource( coinSet[ image.tileNum % coinSet.length ] );
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * <p>
     * Helper for special animator calls
     * <p>
     * //###############################
     */
    public void animatorHelper( int position )
    {
        final BoardTile image = boardTiles.get( position );
        
        // Special case animation that is different from the child animations
        //      Star: Star rotates 360 a few times them shoots off other stars
        //      Bomb: Expand then explode
        //      Bolt: Lighting Vertically or Horizontally
        //      Spark: Branching bolt, affected coins jump and spin
        //      Question Mark: Either of the above...Randomly
        if ( image.getTag() != null && image.getTag() instanceof ObjectAnimator[] )
        {
            // Can hold array of animations
            ObjectAnimator[] bg = ( ObjectAnimator[] ) image.getTag();
            
            //
            for ( int i = 0; i < bg.length; i++ )
            {
                if ( bg[ i ] != null )
                {
                    //                    gameEngine.animatorList.add( bg[ i ] );
                    if ( bg[ i ].getListeners() == null )
                    {
                        final ObjectAnimator obj = bg[ i ];
                        
                        bg[ i ].addListener( new Animator.AnimatorListener()
                        {
                            @Override
                            public void onAnimationStart( Animator animation )
                            {
                            }
                            
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                if ( GameEngine.isKilled )
                                {
                                    return;
                                }
                                
                                // Only play the sound once!
                                if ( logicThread != null && logicThread.animationsRunning > 1 )
                                {
                                    image.setSoundEfx( -1 );
                                }
                                
                                if ( logicThread != null )
                                {
                                    logicThread.animationsRunning--;
                                }
                                
                                if ( obj != null )
                                {
                                    gameEngine.animatorList.remove( obj );
                                }
                                
                                image.setScaleY( 1 );
                                image.setScaleX( 1 );
                                image.setRotation( 0 );
                                image.setVisibility( INVISIBLE );
                                image.setState( BoardTile.STATE_INACTIVE );
                                //
                                image.clearAnimation();
                                image.setTag( null );
                                image.animator = null;
                                image.setSpecialItem( -1 );
                                image.specialTile = -1;
                                image.animDelay = 0;
                                
                                // Undo caught status
                                image.isCaught = false;
                                
                                //
                                if ( logicThread != null && logicThread.animationsRunning <= 0 )
                                {   // ALWAYS call DROP / RESPAWN after MATCHES CLEARED complete
                                    // Stored sound
                                    if ( image.getSoundEfx() != -1 )
                                    {
                                        if ( gameEngine.soundPlayer != null && !inHelper )
                                        {
                                            gameEngine.soundPlayer.playBgSfx( image.getSoundEfx() );
                                        }
                                    }
                                    else
                                    {
                                        //
                                        gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                                    }
                                    
                                    //
                                    logicThread.animationsRunning = 0;
                                    logicThread.addToStack( LogicThread.CMD_DROP );
                                }
                            }
                            
                            @Override
                            public void onAnimationCancel( Animator animation )
                            {
                            
                            }
                            
                            @Override
                            public void onAnimationRepeat( Animator animation )
                            {
                            
                            }
                        } );
                        
                        // Inform thread that we are animating
                        logicThread.animationsRunning++;
                        
                        // Enable pausing
                        gameEngine.animatorList.add( bg[ i ] );
                    }
                    
                    bg[ i ].start();
                }
                else
                {
                    bg[ i ] = null;
                }
            }
            //
            image.setTag( null );
        }
        else if ( image.getTag() != null && image.getTag() instanceof ObjectAnimator )
        {
            // Single ObjectAnimator call
            final ObjectAnimator bg = ( ObjectAnimator ) image.getTag();
            
            bg.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    if ( GameEngine.isKilled )
                    {
                        return;
                    }
                    
                    // Only play the sound once!
                    if ( logicThread.animationsRunning > 1 )
                    {
                        image.setSoundEfx( -1 );
                    }
                    //
                    logicThread.animationsRunning--;
                    
                    gameEngine.animatorList.remove( bg );
                    
                    //
                    image.setScaleY( 1 );
                    image.setScaleX( 1 );
                    image.setRotation( 0 );
                    image.setVisibility( INVISIBLE );
                    image.setState( BoardTile.STATE_INACTIVE );
                    //
                    image.clearAnimation();
                    image.setTag( null );
                    image.animator = null;
                    image.setSpecialItem( -1 );
                    image.specialTile = -1;
                    image.animDelay = 0;
                    
                    // Undo caught status
                    image.isCaught = false;
                    //
                    if ( logicThread.animationsRunning <= 0 )
                    {   // ALWAYS call DROP / RESPAWN after MATCHES CLEARED complete
                        // Stored sound
                        if ( image.getSoundEfx() != -1 )
                        {
                            if ( gameEngine.soundPlayer != null && !inHelper )
                            {
                                gameEngine.soundPlayer.playBgSfx( image.getSoundEfx() );
                            }
                        }
                        //
                        image.setSoundEfx( -1 );
                        //
                        logicThread.animationsRunning = 0;
                        logicThread.addToStack( LogicThread.CMD_DROP );
                    }
                }
                
                @Override
                public void onAnimationCancel( Animator animation )
                {
                    
                }
                
                @Override
                public void onAnimationRepeat( Animator animation )
                {
                    
                }
            } );
            
            // Inform thread that we are animating
            logicThread.animationsRunning++;
            
            bg.start();
            gameEngine.animatorList.add( bg );
            image.setTag( null );
        }
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
        int       x          = ( int ) event.getX() / size;
        int       y          = ( int ) event.getY() / size;
        int       position   = (y * getColumnCount() + x);
        BoardTile tile;
        PointXYZ  start      = new PointXYZ();
        PointXYZ  end        = new PointXYZ();
        boolean   canMatch   = false;
        boolean   hasSweeper = false;
        
        //
        // Exit until We can touch again
        // Don't even bother if in helper mode
        //
        if ( inHelper || !canTouch )
        {
            return false;
        }
        
        
        //####################################
        //
        //
        //
        //####################################
        if ( event.getActionMasked() == MotionEvent.ACTION_UP )
        {
            // Pause the hint timer
            if ( logicThread != null )
            {
                logicThread.setHintTimer( LogicThread.RESUME_HINT );
            }
            
            
            // Only cases where we can match coins
            // This includes Special Rainbow coin
            if ( tilePoints.size() > 2 || (tilePoints.size() == 2 && matchColor > BoardTile.MAX_COINS) )
            {
                canMatch = true;
            }
            
            //
            //
            //
            if ( tilePoints.size() == 0 )
            {
                //#################################
                //
                // Reset coins to normal state
                //
                //#################################
                for ( BoardTile t : boardTiles )
                {
                    //                t.clearColorFilter();
                    t.pointPosi = -1;
                    
                    // Show the proper image
                    if ( t.tileNum > -1 && t.getState() == BoardTile.STATE_ACTIVE )
                    {
                        t.resetCoin();
                    }
                }
                
                return true;
            }
            
            //
            // Create a rainbow coin
            //
            if ( tilePoints.size() == 2 && matchColor < BoardTile.MAX_COINS )
            {
                BoardTile t1, t2;
                
                t1 = boardTiles.get( tilePoints.get( 0 ) );
                t2 = boardTiles.get( tilePoints.get( 1 ) );
                
                if ( t1.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && t1.getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL && t2.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && t2
                        .getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL )
                {
                    canMatch = true;
                }
            }
            
            //
            // Find board created boosters
            // Sort by tile that is up the highest ( lowest num to highest )
            Collections.sort( tilePoints, new Comparator<Integer>()
            {
                @Override
                public int compare( Integer o1, Integer o2 )
                {
                    if ( o1 < o2 )
                    {
                        return -1;
                    }
                    else if ( o1 > o2 )
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            } );
            
            int delay  = 0;
            int startY = tilePoints.get( 0 ) / mapHeight;
            int startX = 0;
            
            //
            for ( int i = 0; i < tilePoints.size(); i++ )
            {
                BoardTile t = boardTiles.get( tilePoints.get( i ) );
                
                if ( canMatch )
                {
                    matchList[ t.getTileY() ][ t.getTileX() ] = 0;
                    //
                    if ( t.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL )
                    {
                        hasSweeper = true;
                    }
                    
                    // Deternmine the "goto target delay"
                    if ( (t.getPosition() / mapHeight) > startY )
                    {
                        startY++;
                        startX = 0;
                        delay += 100;
                    }
                    else
                    {
                        delay += startX;
                        //                        startX += (100 / mapWidth);
                        startX += 50;
                    }
                    
                    // Add delay where needed
                    t.gotoTargetDelay = delay;
                }
            }
            
            
            //#################################
            //
            // Reset coins to normal state
            //
            //#################################
            for ( BoardTile t : boardTiles )
            {
                //                t.clearColorFilter();
                t.pointPosi = -1;
                
                // Show the proper image
                if ( t.tileNum > -1 && t.getState() == BoardTile.STATE_ACTIVE )
                {
                    t.resetCoin();
                }
            }
            
            
            //#################################
            //
            // Any matches?
            //
            //#################################
            if ( canMatch )
            {
                GameEngine.boardMoves++;
                
                // If we have a Rainbow coin, create it
                if ( tilePoints.size() == 2 && hasSweeper && matchColor < BoardTile.MAX_COINS )
                {
                    // Add to the correct booster's meter for creation
                    logicThread.addToStack( LogicThread.CMD_CREATE_SPECIALS );
                }
                // If we have a special, create it!
                else if ( tilePoints.size() >= MIN_FOR_SPECIAL && !hasSweeper )
                {
                    if ( (CustomTimer.currentTime / 1000) > 45 && tilePoints.size() == MIN_FOR_SPECIAL )
                    {
                        // Clock isn't enabled yet
                        logicThread.addToStack( LogicThread.CMD_PROCESS_MATCHES );
                    }
                    else
                    {
                        // Add to the correct booster's meter for creation
                        logicThread.addToStack( LogicThread.CMD_CREATE_SPECIALS );
                    }
                }
                else
                {
                    logicThread.addToStack( LogicThread.CMD_PROCESS_MATCHES );
                }
                
                //
                canTouch = false;
            }
            else
            {
                tilePoints.clear();
            }
            
            // Erase the lines
            lineBuffer.eraseColor( Color.TRANSPARENT );
            invalidate();
            matchColor = -1;
            matchSpecial = -1;
            
            return false;
        }
        
        
        // Pause the hint timer
        if ( logicThread != null )
        {
            // Showing arrows
            if ( logicThread.hintIsDisplayed == 1 )
            {
                FrameLayout layout = mainView.findViewById( R.id.boardGridSpecials );
                layout.removeAllViews();
                logicThread.hintIsDisplayed = 0;
            }
            // Moving coins
            else if ( logicThread.hintIsDisplayed == 2 )
            {
                int[] hints = new int[]{ hintTile.x, hintTile.y, hintTile.z };
                
                for ( int hint : hints )
                {
                    BoardTile      t   = boardTiles.get( hint );
                    ObjectAnimator obj = ( ObjectAnimator ) t.getTag();
                    
                    obj.end();
                    obj = null;
                    t.setTag( null );
                }
                
                logicThread.hintIsDisplayed = 0;
            }
            else
            {
                logicThread.setHintTimer( LogicThread.PAUSE_HINT );
            }
        }
        
        
        //###################################
        //
        // We do not allow touches out
        // of bounds
        //
        //###################################
        if ( position < 0 || position >= boardTiles.size() || (y < 0 || y >= mapHeight) || (x < 0 || x >= mapWidth) )
        {
            return true;
        }
        
        
        //####################################
        //
        // We do not allow touching
        // Jewels and Blockers
        // A tile sitting inactive under
        // a blocker row
        // We can touch sweepers and rainbow
        //
        //####################################
        tile = boardTiles.get( position );
        if ( tile.getState() == BoardTile.STATE_INACTIVE || (tile.getBlockerType() > 0 || tile.tileNum >= BoardTile.PURPLE_GEM) && tile.tileNum != BoardTile.RAINBOW_COIN )
        {
            return true;
        }
        
        
        //####################################
        //
        // Player setting a booster??
        //
        //####################################
        if ( boosterItem != -1 )
        {
            // Do not touch outside of the grid
            // Must be a valid tile
            tile = boardTiles.get( position );
            
            if ( tile.tileNum < 0 || tile.getSpecialItem() > 1 )
            {
                if ( gameEngine.soundPlayer != null )
                {
                    //@@@@@@@@@@@@@@@@@@@@
                    gameEngine.soundPlayer.play( PlaySound.INVALID );
                }
                
                return false;
            }
            
            // Reset it
            boosterItem = -1;
            
            // We have a good one
            // Now run the animation for this booster
            onBoosterListener.boosterLocationSet( position );
            
            return false;
        }
        
        
        //
        // Valid tile to test against
        //
        tile = boardTiles.get( position );
        
        if ( tile.tileNum < 0 )
        {
            return false;
        }
        
        
        //##################################
        //
        // Make sure this is valid before we
        // proceed to process the touched
        // tile.
        //
        //##################################
        if ( tilePoints.size() > 0 )
        {
            //
            // We have this selected already and we wish
            // to remove the tile after this one from the list
            //
            if ( tile.pointPosi != -1 && tile.pointPosi == tilePoints.size() - 2 )
            {
                // Tile to remove
                int       index = tilePoints.get( tilePoints.size() - 1 );
                BoardTile tile2;
                
                // Reset state info
                tile = boardTiles.get( index );
                tile.pointPosi = -1;
                //                tile.setImageResource( coinSet[ tile.tileNum] );
                tile.resetCoin();
                
                
                // Remove the line from the screen
                // Get the line we wish to erase
                tile = boardTiles.get( tilePoints.get( tilePoints.size() - 2 ) );
                start.y = (tile.getTileY() * size) + (size / DIV_NUM);
                start.x = (tile.getTileX() * size) + (size / DIV_NUM);
                
                // Do we show the Specials?
                //                handleSpecialDisplay( tilePoints.size() - 1, tile );
                
                //
                tile2 = boardTiles.get( tilePoints.get( tilePoints.size() - 1 ) );
                end.y = (tile2.getTileY() * size) + (size / DIV_NUM);
                end.x = (tile2.getTileX() * size) + (size / DIV_NUM);
                
                // Set paint to draw transparent line
                Xfermode mode = subPaint.getXfermode();
                subPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
                
                // clear the line
                bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
                
                // Restore normal drawing
                subPaint.setXfermode( mode );
                
                //
                // Remove it
                //
                tilePoints.remove( tilePoints.size() - 1 );
                
                // Because the removal can erase an existing line
                // that happens to be set in a "X" pattern, re need
                // to redraw ALL lines on an erase function!
                drawAllLines( bufCanvas );
                
                return false;
            }
            
            // Is this already in the list??
            if ( tile.pointPosi != -1 )
            {
                return false;
            }
        }
        
        
        //####################################
        //
        //
        //
        //####################################
        if ( event.getActionMasked() == MotionEvent.ACTION_DOWN )
        {
            // Specials or standard icons
            matchColor = boardTiles.get( position ).tileNum;
            matchSpecial = boardTiles.get( position ).getSpecialItem();
            
            // Set Pressed state
            //            if ( matchColor >= BoardTile.MAX_COINS )
            if ( matchSpecial >= BoardTile.SWEEPER_COIN_VERTICAL && matchSpecial <= BoardTile.SWEEPER_COIN_HORIZONTAL )
            {
                // Specials sound
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.SPECIAL_TOUCH );
                }
                
                tile.setBackgroundResource( coinBombGlow[ tile.tileNum % coinBombGlow.length ] );
            }
            else
            {
                // Coin sound
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.COIN );
                }
                
                tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
            }
            
            // Main selected coin
            //            tile.clearColorFilter();
            //            tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
            tile.growCoin();
            // First in the list
            tile.pointPosi = 0;
            // Add to list for lines
            tilePoints.add( tile.getPosition() );
            
            return true;
        }
        //####################################
        //
        //
        //
        //####################################
        else if ( event.getActionMasked() == MotionEvent.ACTION_MOVE )
        {
            int     test_value;
            boolean isValid   = false;
            int     last_posi = tilePoints.get( tilePoints.size() - 1 );
            int     bound     = (size / TOUCH_NUM);
            //            float     bound     = (size / 2.5f);
            
            int xx    = ( int ) event.getX();
            int yy    = ( int ) event.getY();
            int testX = ((xx / size) * size) + (size / DIV_NUM);
            int testY = ((yy / size) * size) + (size / DIV_NUM);
            
            
            //            Log.d( "Bound","Box: " + bound + "\n\tX: " + xx + ", Y: " + yy + "\n\ttX: " + testX + ", tY: " + testY+ "\n\ttX+B: " + (testX + bound) + ", tY+B: " + (testY + bound) );
            
            
            // Make sure it's within a touchable point
            // for a new tile: X
            if ( xx < (testX - bound) || xx > (testX + bound) )
            {
                return true;
            }
            // tile: Y
            if ( yy < (testY - bound) || yy > (testY + bound) )
            {
                return true;
            }
            
            
            //
            // Can only be 4 values!
            //
            test_value = Math.abs( last_posi - position );
            
            //
            // Is this  valid tile selection?
            //
            if ( (test_value == 1 || test_value == 7 || test_value == 6 || test_value == 8) && tile.tileNum == matchColor )
            {
                isValid = true;
            }
            
            
            //################################
            //
            // We have a match! It's a Non
            // special coin
            //
            //################################
            if ( isValid && !tilePoints.contains( position ) )
            {
                // Next up in the list
                tile.pointPosi = tilePoints.size();
                
                // Add to the buffer
                tilePoints.add( tile.getPosition() );
                
                
                if ( tilePoints.size() > 1 )
                {
                    // Set the line we wish to use
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 2 ) );
                    start.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    start.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 1 ) );
                    end.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    end.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    subPaint.setColor( darkenColor( glowColors[ tile.tileNum % glowColors.length ], 1 ) );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
                    
                    //                    paint.setMaskFilter( new BlurMaskFilter( 4, BlurMaskFilter.Blur.SOLID ) );
                    paint.setColor( glowColors[ tile.tileNum % glowColors.length ] );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, paint );
                    
                    // Show the lines
                    invalidate();
                }
                
                // Set Pressed state
                //            if ( matchColor >= BoardTile.MAX_COINS )
                if ( tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && tile.getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL )
                {
                    // Specials sound
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.play( PlaySound.SPECIAL_TOUCH );
                    }
                    
                    tile.setBackgroundResource( coinBombGlow[ tile.tileNum % coinBombGlow.length ] );
                }
                else
                {
                    // Coin sound
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.play( PlaySound.COIN );
                    }
                    
                    tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
                }
/*
                
                // Play SFX
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.COIN );
                }
                
                // Register it's activeness
                if ( matchSpecial >= BoardTile.SWEEPER_COIN_VERTICAL && matchSpecial <= BoardTile.SWEEPER_COIN_HORIZONTAL )
                
                {
                    tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
                }
                
*/
                
                tile.growCoin();
                
                //                tile.clearColorFilter();
                
                return true;
            }
            //            else if ( tilePoints.size() == 1 && matchColor == BoardTile.RAINBOW_COIN && matchSpecial < BoardTile.SWEEPER_COIN_VERTICAL )
            else if ( tilePoints.size() == 1 )
            {
                if ( matchColor == BoardTile.RAINBOW_COIN && tile.getSpecialItem() < 1 )
                {
                    // For selected specials ONLY
                    // Add support here for any other board created
                    // special here.
                    //
                    tile.pointPosi = tilePoints.size();
                    
                    // Add to the buffer
                    tilePoints.add( tile.getPosition() );
                    
                    // Set the line we wish to use
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 2 ) );
                    start.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    start.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 1 ) );
                    end.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    end.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    subPaint.setColor( darkenColor( glowColors[ tile.tileNum % glowColors.length ], 1 ) );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
                    
                    paint.setColor( glowColors[ tile.tileNum % glowColors.length ] );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, paint );
                    
                    // Show the lines
                    invalidate();
                    
                    
                    // Play SFX
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.play( PlaySound.SPECIAL_TOUCH );
                    }
                    
                    // Register it's activeness
                    tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
                    tile.growCoin();
                    
                    return true;
                }
                else if ( (matchSpecial >= BoardTile.SWEEPER_COIN_VERTICAL && matchSpecial <= BoardTile.SWEEPER_COIN_HORIZONTAL) && (tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && tile
                        .getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL) )
                {
                    //
                    // For Rainbow coin creation using ANY striped coins
                    //
                    tile.pointPosi = tilePoints.size();
                    
                    // Add to the buffer
                    tilePoints.add( tile.getPosition() );
                    
                    // Set the line we wish to use
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 2 ) );
                    start.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    start.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    tile = boardTiles.get( tilePoints.get( tilePoints.size() - 1 ) );
                    end.y = (tile.getTileY() * size) + (size / DIV_NUM);
                    end.x = (tile.getTileX() * size) + (size / DIV_NUM);
                    
                    //
                    subPaint.setColor( darkenColor( glowColors[ tile.tileNum % glowColors.length ], 1 ) );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
                    
                    paint.setColor( glowColors[ tile.tileNum % glowColors.length ] );
                    bufCanvas.drawLine( start.x, start.y, end.x, end.y, paint );
                    
                    // Show the lines
                    invalidate();
                    
                    
                    // Play SFX
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.play( PlaySound.SPECIAL_TOUCH );
                    }
                    
                    // Register it's activeness
                    tile.setBackgroundResource( coinBombGlow[ tile.tileNum % coinBombGlow.length ] );
                    //                    tile.setBackgroundResource( coinGlow[ tile.tileNum ] );
                    tile.growCoin();
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * //#############################
     * <p>
     * Make a color darker
     * <p>
     * //#############################
     *
     * @param Color
     * @param shiftLevel
     *
     * @return
     */
    private int darkenColor( int Color, int shiftLevel )
    {
        int r, g, b, a;
        
        a = Color & 0xFF000000;
        r = ((Color & 0x00FF0000) >> shiftLevel) & 0x00FF0000;
        g = ((Color & 0x0000FF00) >> shiftLevel) & 0x0000FF00;
        b = ((Color & 0x000000FF) >> shiftLevel) & 0x000000FF;
        
        return a | r | g | b;
    }
    
    
    /**
     * //######################################
     * <p>
     * Place a booster where the player wants
     * <p>
     * //######################################
     *
     * @param onBoosterLitener
     * @param boosterItem
     */
    public void setBoosterListener( OnBoosterListener onBoosterLitener, int boosterItem )
    {
        this.onBoosterListener = onBoosterLitener;
        this.boosterItem = boosterItem;
    }
    
    
    /**
     * //#######################################
     * <p>
     * Used to place a gem in the highest spot
     * possible, and with minimum spaces below
     * it to maximum difficulty dropping the gem
     * <p>
     * //#######################################
     *
     * @param gemCount N/A
     */
    public ArrayList<Integer> findGemSlot( int gemCount )
    {
        ArrayList<Integer> list     = new ArrayList<>();
        int                i;
        boolean            invalid;
        int                maxCount = boardTiles.size();
        
        
        //################################
        //
        // MUST be able to drop. It's the
        // only way to break the gems
        //
        //################################
        for ( int c = 0; c < gemCount; c++ )
        {
            i = r.nextInt( boardTiles.size() );
            
            while ( i == -1 || boardTiles.get( i ).tileNum < 0 || boardTiles.get( i ).getSpecialItem() > 1 )
            {
                i = r.nextInt( boardTiles.size() );
            }
            
            // Transform the coin into a Gem
            if ( gemCount < 4 )
            {
                int index = r.nextInt( 4 );
                
                while ( list.contains( index ) )
                {
                    index = r.nextInt( 4 );
                }
                
                //
                boardTiles.get( i ).tileNum = index + BoardTile.PURPLE_GEM;
                list.add( index );
            }
            else
            {
                boardTiles.get( i ).tileNum = BoardTile.PURPLE_GEM + c;
                list.add( c );
            }
            
            //
            boardTiles.get( i ).setSpecialItem( BoardTile.GEM_COIN );
            boardTiles.get( i ).specialTile = -1;
            
            PointXYZ p = new PointXYZ( 1, 0 );
            boardTiles.get( i ).setTag( p );
        }
        
        return list;
    }
    
    
    /**
     * //##############################
     * <p>
     * Used for help system
     * <p>
     * //##############################
     */
    public void drawBufferLineHelper( int size )
    {
        PointXYZ  start = new PointXYZ();
        PointXYZ  end   = new PointXYZ();
        BoardTile tile;
        
        
        for ( int i = 0; i < (tilePoints.size() - 1); i++ )
        {
            tile = boardTiles.get( tilePoints.get( i ) );
            start.y = (tile.getTileY() * size) + (size / DIV_NUM);
            start.x = (tile.getTileX() * size) + (size / DIV_NUM);
            
            //
            tile = boardTiles.get( tilePoints.get( i + 1 ) );
            end.y = (tile.getTileY() * size) + (size / DIV_NUM);
            end.x = (tile.getTileX() * size) + (size / DIV_NUM);
            
            //
            bufCanvas.drawLine( start.x, start.y, end.x, end.y, subPaint );
            bufCanvas.drawLine( start.x, start.y, end.x, end.y, paint );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Get the direction of the swipe
     * <p>
     * //###############################
     *
     * @param pressed
     * @param next
     *
     * @return
     */
    private int getDirection( PointXYZ pressed, PointXYZ next )
    {
        return pressed.z - next.z;
    }
    
    
    //########################################
    //
    // Match making methods: User initiated
    //
    //########################################
    
    /**
     * ########################################
     * <p>
     * Solve any matches
     * This is where most of the magic happens
     * <p>
     * ########################################
     */
    public void signalMatchesFound()
    {
        BoardTile tile;
        
        //###########################################
        //
        // Matches found
        //
        //###########################################
        for ( int i = 0; i < boardTiles.size(); i++ )
        {
            tile = boardTiles.get( i );
            
            // Cancel any spinning, etc
            //            tile.endAnimation();
        }
        
        
        //###############################
        //
        // Normal matches
        //
        //###############################
        for ( int i = 0; i < tilePoints.size(); i++ )
        {
            tile = boardTiles.get( tilePoints.get( i ) );
            
            // Did we match a special tile?
            // ONLY test here for a CLOCK
            if ( tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && tile.getState() == BoardTile.STATE_ACTIVE )
            {
                tile.setState( BoardTile.USE_SPECIAL_ITEM );
            }
            else if ( tile.getState() != BoardTile.CREATE_ITEM && tile.getState() != BoardTile.MASTER_ITEM && tile.getState() != BoardTile.SPECIAL_ANIMATE )
            {
                tile.setState( BoardTile.STATE_MATCHED );
            }
        }
        
        
        //#################################
        //
        // Check for Gems and Blockers
        //
        //#################################
        for ( int i = 0; i < gemsToMatch.size(); i++ )
        {
            // Get the Gem
            tile = boardTiles.get( gemsToMatch.get( i ) );
            
            int x = tile.getTileX();
            int y = tile.getTileY();
            // U, UR, R, DR, D, DL, L, UL
            int[][] dirc = {
                    { 0, -1 },  // U
                    { 1, 0 },   // R
                    { 0, 1 },   // D
                    { -1, 0 },  // L
            };
            
            // Did we match a special tile?
            // ONLY test here for a CLOCK
            for ( int[] ints : dirc )
            {
                int xx = x + ints[ 0 ];
                int yy = y + ints[ 1 ];
                
                if ( tile.getState() != BoardTile.GEM_MATCHED && xx > -1 && xx < mapWidth && yy > -1 && yy < mapHeight )
                {
                    BoardTile test = boardTiles.get( yy * mapWidth + xx );
                    
                    if ( test.getState() == BoardTile.STATE_MATCHED )
                    {
                        tile.setState( BoardTile.GEM_MATCHED );
                        break;
                    }
                }
            }
        }
        
        //
        // Blockers
        //
        for ( int i = 0; i < blockersToMatch.size(); i++ )
        {
            // Get the Gem
            tile = boardTiles.get( blockersToMatch.get( i ) );
            
            int x = tile.getTileX();
            int y = tile.getTileY();
            // U, UR, R, DR, D, DL, L, UL
            int[][] dirc = {
                    { 0, -1 },  // U
                    { 1, 0 },   // R
                    { 0, 1 },   // D
                    { -1, 0 },  // L
            };
            
            // Did we match a special tile?
            // ONLY test here for a CLOCK
            for ( int[] ints : dirc )
            {
                int xx = x + ints[ 0 ];
                int yy = y + ints[ 1 ];
                
                if ( tile.getState() != BoardTile.BLOCKER_MATCHED && xx > -1 && xx < mapWidth && yy > -1 && yy < mapHeight )
                {
                    BoardTile test = boardTiles.get( yy * mapWidth + xx );
                    
                    if ( test.getState() == BoardTile.STATE_MATCHED )
                    {
                        tile.setState( BoardTile.BLOCKER_MATCHED );
                        break;
                    }
                }
            }
        }
    }
    
    
    /**
     * ########################################
     * <p>
     * Create a special item for the board
     * <p>
     * ########################################
     */
    public void createSpecialItems()
    {
        BoardTile tile;
        BoardTile master = null;
        int       toX;
        int       toY;
        
        //##################################
        //
        // Determine if we have a special
        // item spawning
        //
        //##################################
        // Depending on the count, MAKE A SPECIAL!!
        int count = 0;
        int index;
        
        
        //#####################################
        //
        // Set the master tile
        //
        //#####################################
        for ( int i = 0; i < tilePoints.size(); i++ )
        {
            tile = boardTiles.get( tilePoints.get( i ) );
            
            // Check to see if we matched a SWEEPER / RAINBOW tile
            if ( tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL )
            {
                continue;
            }
            
            //
            count++;
        }
        
        
        if ( tilePoints.size() == 2 )
        {
            // Select the random master tile
            master = boardTiles.get( tilePoints.get( r.nextInt( tilePoints.size() ) ) );
            
            
            //#################################
            //
            // Coins: Rainbow
            //
            //#################################
            if ( master != null )
            {
                master.setSpecialItem( BoardTile.RAINBOW_SEEKER );
                master.tileNum = BoardTile.RAINBOW_COIN;
            }
        }
        else
        {
            // Set the master
            int search = r.nextInt( tilePoints.size() );
            while ( boardTiles.get( tilePoints.get( search ) ).getSpecialItem() == -1 || boardTiles.get( tilePoints.get( search ) ).getSpecialItem() > 0 )
            {
                search = r.nextInt( tilePoints.size() );
            }
            
/*
            // Select the random master tile
            if ( tilePoints.contains( 33 ) )
            {
                search = tilePoints.indexOf( 33 );
            }
            
*/
            master = boardTiles.get( tilePoints.get( search ) );
            
            
            //#################################
            //
            // Coins: Vertical and Horizontal
            // sweepers beams
            //
            //#################################
            if ( master != null )
            {
                master.setSpecialItem( r.nextInt( 2 ) + BoardTile.SWEEPER_COIN_VERTICAL );
            }
        }
        
        
        //#############################
        //
        // Common code
        //
        //#############################
        if ( master != null )
        {
            master.setState( BoardTile.MASTER_ITEM );
            master.swapTO = new PointXYZ( 0, 0, master.tileNum, master.getSpecialItem() );
            
            //
            //##################################
            //
            for ( int c = 0; c < tilePoints.size(); c++ )
            {
                tile = boardTiles.get( tilePoints.get( c ) );
                
                //
                if ( tile.tileNum < 0 )
                {
                    continue;
                }
                
                // Animate all tiles to master tile
                if ( tile.getPosition() != master.getPosition() && (tile.getSpecialItem() < 1 || (tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && tile.getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL)) )
                {
                    matchList[ tile.getTileY() ][ tile.getTileX() ] = 0;
                    //
                    tile.setState( BoardTile.CREATE_ITEM );
                    toX = ( int ) (master.getX() - tile.getX());
                    toY = ( int ) (master.getY() - tile.getY());
                    //
                    tile.swapTO = new PointXYZ( toX, toY, tile.tileNum, master.getPosition() );
                }
            }
        }
        
        
        //#################################
        //
        // Check for Gems
        //
        //#################################
        for ( int i = 0; i < gemsToMatch.size(); i++ )
        {
            // Get the Gem
            tile = boardTiles.get( gemsToMatch.get( i ) );
            
            int x = tile.getTileX();
            int y = tile.getTileY();
            
            // U, UR, R, DR, D, DL, L, UL
            int[][] dirc = {
                    { 0, -1 },  // U
                    { 1, 0 },   // R
                    { 0, 1 },   // D
                    { -1, 0 },  // L
            };
            
            //
            // Did we match a special tile?
            //
            for ( int[] ints : dirc )
            {
                int xx = x + ints[ 0 ];
                int yy = y + ints[ 1 ];
                
                if ( tile.getState() != BoardTile.GEM_MATCHED && xx > -1 && xx < mapWidth && yy > -1 && yy < mapHeight )
                {
                    BoardTile test = boardTiles.get( yy * mapWidth + xx );
                    
                    if ( test.getState() == BoardTile.CREATE_ITEM || test.getState() == BoardTile.MASTER_ITEM )
                    {
                        tile.setState( BoardTile.GEM_MATCHED );
                        break;
                    }
                }
            }
        }
        
        //
        // Blockers
        //
        for ( int i = 0; i < blockersToMatch.size(); i++ )
        {
            // Get the Gem
            tile = boardTiles.get( blockersToMatch.get( i ) );
            
            int x = tile.getTileX();
            int y = tile.getTileY();
            // U, UR, R, DR, D, DL, L, UL
            int[][] dirc = {
                    { 0, -1 },  // U
                    { 1, 0 },   // R
                    { 0, 1 },   // D
                    { -1, 0 },  // L
            };
            
            // Did we match a special tile?
            // ONLY test here for a CLOCK
            for ( int[] ints : dirc )
            {
                int xx = x + ints[ 0 ];
                int yy = y + ints[ 1 ];
                
                if ( tile.getState() != BoardTile.BLOCKER_MATCHED && xx > -1 && xx < mapWidth && yy > -1 && yy < mapHeight )
                {
                    BoardTile test = boardTiles.get( yy * mapWidth + xx );
                    
                    if ( test.getState() == BoardTile.CREATE_ITEM || test.getState() == BoardTile.MASTER_ITEM )
                    {
                        tile.setState( BoardTile.BLOCKER_MATCHED );
                        break;
                    }
                }
            }
        }
    }
    
    
    /**
     * ########################################
     * <p>
     * After all tiles have disappeared,
     * handle dropping old tiles
     * handle re-spawning tiles with no
     * replacements
     * <p>
     * ########################################
     */
    public void signalDroppingTiles()
    {
        int       index     = 0;
        int       delayTime = 0;
        int       baseY     = -1;
        BoardTile image;
        
        //
        // Scan from bottom up
        // Once we are here, all matching is done!
        //
        objStillDropping = false;
        isInActive = false;
        
        for ( int y = (mapHeight - 1); y > -1; y-- )
        {
            // Check if tile under is empty so this can drop
            for ( int x = 0; x < mapWidth; x++ )
            {
                index = (y * mapWidth) + x;
                image = boardTiles.get( index );
                //
                if ( image.getState() == BoardTile.STATE_INACTIVE && image.tileNum > -1 )
                //                    if ( image.getState() != BoardTile.STATE_DROP && image.getState() != BoardTile.STATE_DROPPING && image.tileNum != -1 )
                {
                    int temp = checkForReplacement( index, delayTime, (firstPass == 1) );
                    //                    checkForReplacement( index, delayTime );
                    
                    if ( temp == 0 )
                    {
                        // Only need this set once when a tile is dropping
                        objStillDropping = true;
                    }
                    
                    if ( image.getState() == BoardTile.STATE_INACTIVE )
                    {
                        isInActive = true;
                    }
                    
                    // We can start delaying now
                    if ( baseY == -1 )
                    {
                        baseY = y;
                    }
                }
            }
            
            //
            if ( y <= baseY )
            {
                //                delayTime += AnimationValues.DROP_TIME/2;
                delayTime = 0;
            }
        }
        
        
        //
        // Final checks
        //
        if ( !objStillDropping && isInActive && firstPass == 0 )
        {
            firstPass = 1;
            objStillDropping = true;
        }
        else
        {
            firstPass = 0;
        }
    }
    
    
    /**
     * ########################################
     * <p>
     * Find the lowest available tile to drop
     * down
     * <p>
     * ########################################
     *
     * @param matchedPosi N/A
     *
     * @return N/A
     */
/*
    private void checkForReplacement( int matchedPosi, int delayTime )
    {
        int       replacedPosi = matchedPosi;
        boolean   valid        = false;
        BoardTile matched;
        BoardTile replaced;
        int       x, y;
        
        //#################################
        //
        // If the tiles are already at the
        // top, then respawn
        //
        //#################################
        if ( (matchedPosi - mapWidth) < 0 )
        {
            // Just needs to respawn. View already
            // invisible and as high as it is going to be
            replaced = boardTiles.get( replacedPosi );
            replaced.setState( BoardTile.STATE_RESPAWN );
            replaced.setSpecialItem( -1);
            replaced.specialTile = -1;
        }
        else
        {
            // Get to the next tile
            replacedPosi -= mapWidth;
            
            while ( replacedPosi > -1 && !valid )
            {
                //
                // Is there a replacement??
                //
                if ( boardTiles.get( replacedPosi ).getState() == BoardTile.STATE_ACTIVE )
                {   // Found a new home
                    replaced = boardTiles.get( replacedPosi ); // Replacement tile
                    matched = boardTiles.get( matchedPosi );  // Matched tile
                    
                    // Go to the same location as replacement tile and take its color
                    // and take its location for now
                    // These are the values to animate from
                    //
                    // Make this old tile drop from
                    // replacement tile's position
                    // Make sure the matched tile is visible
                    matched.setVisibility( VISIBLE );
                    matched.swapTO = new PointXYZ( ( int ) replaced.getX(), ( int ) replaced.getY(), boardTiles.get( replacedPosi ).tileNum );
                    
                    //
                    matched.tileNum = replaced.tileNum;
                    matched.setSpecialItem( replaced.getSpecialItem());
                    matched.specialTile = replaced.specialTile;
                    matched.setState( BoardTile.STATE_DROP );
                    matched.endAnimation();
                    matched.setBackgroundResource( 0 );
                    matched.setImageResource( 0 );
                    //
                    // Certain Objects in Tag need to transfer also
                    matched.setTag( replaced.getTag() );
                    
                    
                    // Make replaced tile require a replacement now
                    replaced.setVisibility( INVISIBLE );
                    // Save replacement's position
                    replaced.setState( BoardTile.STATE_INACTIVE );
                    
                    // Reset immediately for effect that the dropping
                    // tile is now the floating tile
                    // erase this completely to be respawn or to find a replacement
                    //                    replaced.setImageResource( 0 );
                    //                    replaced.setBackgroundResource( 0 );
                    replaced.setSpecialItem( -1);
                    replaced.specialTile = -1;
                    //                    replaced.specialItem = 0;
                    // In case of special animations
                    replaced.endAnimation();
                    replaced.setTag( null );
                    //
                    valid = true;
                }
                
                // Check the next one up
                replacedPosi -= mapWidth;
            }
            
            //
            // No replacement was found
            // Guess it needs to respawn instead
            //
            if ( !valid )
            {
                matched = ( BoardTile ) getChildAt( matchedPosi );
                matched.setState( BoardTile.STATE_RESPAWN );
                matched.setSpecialItem( -1);
                matched.specialTile = -1;
                //                matched.specialItem = 0;
            }
        }
    }
*/
    private int checkForReplacement( int matchedPosi, int delayTime, boolean canCheckDiagonal )
    {
        int       replacedPosi = matchedPosi;
        boolean   valid        = false;
        BoardTile matched;
        BoardTile replaced     = null;
        boolean   hasShifted   = false;
        boolean   hasMovedUp   = false;
        
        //
        // Not done matching this blocker
        //
        if ( boardTiles.get( matchedPosi ).getBlockerType() > 0 )
        {
            return -1;
        }
        
        //#################################
        //
        // If the tiles are already at the
        // top, then respawn
        //
        //#################################
        if ( (matchedPosi - mapWidth) < 0 )
        {
            // Just needs to respawn. View already
            // invisible and as high as it is going to be
            replaced = boardTiles.get( replacedPosi );
            replaced.setState( BoardTile.STATE_RESPAWN );
            replaced.setSpecialItem( -1 );
            replaced.specialTile = -1;
            replaced.clearCoin();
            //
            //replaced.animDelay = delayTime;
            replaced.animDelay = Math.max( 1, matchedPosi / mapWidth );
            
            return 0;
        }
        else
        {
            //
            // Get to the next tile
            //
            replacedPosi -= mapWidth;
            
            //
            // Is there a replacement??
            // CANNOT BE A BLOCKER! They do not move
            //
            
            while ( replacedPosi > -1 )
            {
                // If it's a blocker, check to the left and
                // right of the blocker
                //                if ( replacedPosi > -1 )
                {
                    // Original test says this is valid
                    replaced = boardTiles.get( replacedPosi );
                    
                    if ( replaced.getBlockerType() > 0 || /*replaced.tileNum == BoardTile.NON_GENERATOR || */canCheckDiagonal )
                    {
                        if ( hasMovedUp )
                        {
                            // Reached a blocker, but we are passed the shift point
                            // Just leave for now
                            return -1;
                        }
                        else
                        {
                            // Check to the left first, then
                            // check the right if left fails
                            //                            BoardTile test     = boardTiles.get( replacedPosi );
                            int[] checkDir = new int[]{ (replacedPosi % mapWidth) - 1, replacedPosi - 1, (replacedPosi % mapWidth) + 1, replacedPosi + 1 };
                            
                            // We can test left first to see if it's valid
                            for ( int i = 0; i < checkDir.length; i += 2 )
                            {
                                // Both directions are covered
                                if ( checkDir[ i ] > -1 && checkDir[ i ] < mapWidth )
                                {
                                    replaced = boardTiles.get( checkDir[ i + 1 ] );
                                    
/*
                                    if ( test.getBlockerType() > 0 || test.tileNum == BoardTile.NON_GENERATOR )
                                    {
*/
                                    if ( replaced.getState() == BoardTile.STATE_ACTIVE && replaced.getBlockerType() == 0 )
                                    {
                                        // We have a good tile
                                        hasShifted = true;
                                        valid = true;
                                        break;
                                    }
                                    //                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        if ( replaced.getState() == BoardTile.STATE_ACTIVE && replaced.getBlockerType() == 0 /*&& replaced.tileNum != -1*/ )
                        //if ( replaced.getState() != BoardTile.STATE_DROP && replaced.getState() != BoardTile.STATE_DROPPING && replaced.getBlockerType() == 0 && replaced.tileNum != -1 )
                        {
                            // We have a good tile
                            valid = true;
                        }
                    }
                }
                
                
                //################################
                //
                // Only do this is we have a find
                // Covers top, top-left, top-right
                //
                //################################
                if ( valid )
                {
                    // Found a new home
                    matched = boardTiles.get( matchedPosi );  // Matched tile
                    
                    // Go to the same location as replacement tile and take its color
                    // and take its location for now
                    // These are the values to animate from
                    //
                    // Make this old tile drop from
                    // replacement tile's position
                    // Make sure the matched tile is visible
                    matched.setVisibility( VISIBLE );
                    matched.swapTO = new PointXYZ( ( int ) replaced.getX(), ( int ) replaced.getY(), boardTiles.get( replacedPosi ).tileNum );
                    
                    //
                    matched.tileNum = replaced.tileNum;
                    matched.setSpecialItem( replaced.getSpecialItem() );
                    matched.specialTile = replaced.specialTile;
                    matched.setState( BoardTile.STATE_DROP );
                    //                matched.endAnimation();
                    matched.clearCoin();
                    //                    matched.animDelay = delayTime;
                    //                    matched.animDelay = delayTime;
                    matched.animDelay = 1;//((matchedPosi - replacedPosi) / mapWidth);
                    //
                    // Certain Objects in Tag need to transfer also
                    matched.setTag( replaced.getTag() );
                    
                    
                    // Make replaced tile require a replacement now
                    replaced.setVisibility( INVISIBLE );
                    // Save replacement's position
                    replaced.setState( BoardTile.STATE_INACTIVE );
                    
                    // Reset immediately for effect that the dropping
                    // tile is now the floating tile
                    // erase this completely to be respawn or to find a replacement
                    //                    replaced.setImageResource( 0 );
                    //                    replaced.setBackgroundResource( 0 );
                    replaced.setSpecialItem( -1 );
                    replaced.specialTile = -1;
                    replaced.clearCoin();
                    //                    replaced.specialItem = 0;
                    // In case of special animations
                    //                replaced.endAnimation();
                    replaced.setTag( null );
                    
                    return 0;
                }
                
                //
                // Test the current replacement test tile to see if it's
                // a blocker tile
                //
                replaced = boardTiles.get( replacedPosi );
                
                if ( replaced.getBlockerType() == 0 )
                {
                    // Not a blocker. Move passed it and
                    // Signal the upward movement.
                    // Shifting now disabled
                    hasMovedUp = true;
                    
                    // Test first if at the top of the grid
                    replacedPosi -= mapWidth;
                    
                    // Reached the top?
                    if ( replacedPosi < 0 )
                    {
                        // Just needs to respawn. View already
                        // invisible and as high as it is going to be
                        replaced = boardTiles.get( matchedPosi );
                        replaced.setState( BoardTile.STATE_RESPAWN );
                        replaced.setSpecialItem( -1 );
                        replaced.specialTile = -1;
                        replaced.clearCoin();
                        //
                        //                        replaced.animDelay = delayTime;
                        replaced.animDelay = Math.max( 1, matchedPosi / mapWidth );
                        
                        return 0;
                    }
                }
                else
                {
                    return -1;
                }
            }
        }
        
        return -1;
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
        // No more bonus points
        GameEngine.pointsMultiplier = 1;
        
        // Stop Hint system just in case it will trigger before thread reset
        logicThread.setHintTimer( LogicThread.STOP_HINT );
        
        // Restore original board mapping
        GameEngine.arrayCopy( boardMap, matchList, mapHeight, mapWidth );
        
        // No specials
        specials.clear();
        
        //
        tilePoints.clear();
        invalidate();
        
        // Refresh screen tiles
        onDrawGrid();
        
        // Fill the hint system
        checkIfMatchesExist( false );
        
        // Save the player some time
        if ( timer != null )
        {
            timer.resume();
            if ( (CustomTimer.currentTime / 1000) < timer.savedTime / 1000 )
            {
                timer.updateTime( (timer.savedTime - CustomTimer.currentTime) / 1000f );
            }
        }
        
        // Enable touching again
        canTouch = true;
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
    public boolean checkTargetsReached()
    {
        int total_targets = 0;
        
        //
        for ( View view : targetViews )
        {
            final PointXYZ temp = ( PointXYZ ) view.getTag();
            
            if ( view.getVisibility() == VISIBLE && temp.getTag() == null )
            {
                // Get the amounts remaining
                total_targets += temp.x;
            }
        }
        
        // Test for the score reached
        if ( ConnectionsFragment.levelScore < gameBoard.neededPoints )
        {
            total_targets = 1;
        }
        
        
        // Test for stars earned requirement
        StarsEarnedBar bar = mainView.findViewById( R.id.starsToEarn );
        if ( bar != null && gameBoard.neededStars > 0 && bar.getStarsOn() < gameBoard.neededStars )
        {
            total_targets = 1;
        }
        
        return total_targets == 0;
        
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
    private void adjustTargets( int[] targets )
    {
        for ( int i = 0; i < targetViews.size(); i++ )
        {
            final ImageTextView view = ( ImageTextView ) targetViews.get( i );
            final PointXYZ      temp = ( PointXYZ ) view.getTag();
            
            if ( temp != null && view.getVisibility() == VISIBLE && temp.getTag() == null )
            {
                if ( targets[ temp.y % targets.length ] > 0 && temp.x > 0 )
                {
                    PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.5f, 1 );
                    PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.5f, 1 );
                    ObjectAnimator       obj = ObjectAnimator.ofPropertyValuesHolder( view, sx, sy );
                    //                    final int            number = (temp.x - targets[ i ]) < 0 ? 0 : (temp.x - targets[ i ]);
                    //                    final int number = Math.max( (temp.x - targets[ i ]), 0 );
                    obj.setDuration( 500 ).setInterpolator( new AnticipateOvershootInterpolator( 1f ) );
                    temp.setTag( obj );
                    
                    //
                    temp.x = Math.max( (temp.x - targets[ temp.y ]), 0 );
                    
                    // If target has some thing left, subtract it
                    // But...If the player solves a target with nothing left
                    // penalize them!
                    if ( temp.x > 0 )
                    {
                        obj.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                super.onAnimationEnd( animation );
                                temp.setTag( null );
                                view.setText( String.format( Locale.getDefault(), "%d", temp.x ) );
                                view.setImageResource( 0 );
                            }
                        } );
                        obj.start();
                    }
                    else
                    {
                        obj.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                super.onAnimationEnd( animation );
                                view.setText( "" );
                                view.setImageResource( R.drawable.white_checkmark );
                                temp.setTag( null );
                            }
                        } );
                    }
                    
                    // Run the animation
                    obj.start();
                    
/*
                    else if ( GameEngine.penaltyMode )
                    {
                        // Penalized the player for solving a finished target
                        // Possible Adjustment: Last level only!
                        temp.x = 1;
                        //
                        view.setText( String.format( Locale.getDefault(), "%d", temp.x ) );
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
*/
                }
            }
        }
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
        if ( mainView != null && !inHelper )
        {
            final View view = mainView.findViewById( R.id.boardGridLayout );
            
            // Kill the TIMER!!!!!!!!!!!!
            if ( timer != null )
            {
                timer.pause();
            }
            
            
            //###############################
            //
            // Clear all tile statuses
            //
            //###############################
            for ( BoardTile tile : boardTiles )
            {
                tile.cardFlipped = false;
                //                tile.endAnimation();
                //                tile.setImageResource( 0 );
                //                tile.clearCoin();
            }
            
            
            // Clear all lines
            tilePoints.clear();
            lineBuffer.eraseColor( Color.TRANSPARENT );
            invalidate();
            
            
            //
            // Clear masking
            //
            OverlayView gridMask = mainView.findViewById( R.id.gridMask );
            gridMask.clearMask();
            
            
            //###################################
            //
            //
            //
            //###################################
            specialMerge = 0;
            logicThread.clearData();
            logicThread.setHintTimer( LogicThread.STOP_HINT );
            canTouch = false;
            
            
            // Need this
            gameEngine.specialRunning = true;
            
            
            //###################################
            //
            // Bonus from moves remaining
            //
            //###################################
            if ( GameEngine.boardMoves > 0 )
            {
                LevelCompleted completed = new LevelCompleted( getContext(), gameBoard.xmlBackgrounds[ GameEngine.currentBackground ] );
                
                completed.setOnDismissListener( new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss( DialogInterface dialog )
                    {
                        
                        int         delay    = 0;
                        int         DURATION = 750;
                        FrameLayout moves    = mainView.findViewById( R.id.timeMovesFrame );
                        //                FrameLayout container = mainView.findViewById( R.id.boardGridSpecials );
                        
                        // count the animations that need to happen
                        logicThread.animationsRunning = 0;
                        
                        //
                        // Fire off all the existing
                        //
                        for ( BoardTile tile : boardTiles )
                        {
                            if ( tile.getSpecialItem() == BoardTile.SWEEPER_COIN_VERTICAL || tile.getSpecialItem() == BoardTile.SWEEPER_COIN_HORIZONTAL )
                            {
                                // Fire it off!
                                tile.setState( BoardTile.USE_SPECIAL_ITEM );
                            }
                        }
                        
                        
                        //
                        // Transfer random coins into sweepers
                        int maxMoves = Math.min( 6, GameEngine.boardMoves );
                        //
                        for ( int c = 0; c < maxMoves; c++ )
                        //                for ( int c = 0; c < 4; c++ )
                        {
                            boolean   haveSweeper = false;
                            BoardTile ex          = boardTiles.get( r.nextInt( boardTiles.size() ) );
                            
                            while ( !haveSweeper )
                            {
                                // Already a sweeper
                                if ( ex.tileNum != -1 && ex.getSpecialItem() < BoardTile.SWEEPER_COIN_VERTICAL && ex.getState() != BoardTile.USE_SPECIAL_ITEM )
                                {
                                    // Fire it off!
                                    haveSweeper = true;
                                    break;
                                }
                                
                                //
                                ex = boardTiles.get( r.nextInt( boardTiles.size() ) );
                            }
                            
                            
                            // Fire it off!
                            ex.setState( BoardTile.USE_SPECIAL_ITEM );
                            
                            
                            //###############################
                            //
                            // Animate the projectiles
                            //
                            //###############################
                            final ImageView          sweeper = new ImageView( getContext() );
                            FrameLayout.LayoutParams params;// = new ConstraintLayout.LayoutParams( size, size );
                            
                            sweeper.setImageResource( R.drawable.blue_projectile );
                            //                    sweeper.setBackgroundColor( 0xFF000000 );
                            sweeper.setId( ex.getPosition() + 1000 );
                            sweeper.setVisibility( View.INVISIBLE );
                            
                            int   width    = getResources().getDimensionPixelSize( R.dimen._16sdp );
                            int   height   = getResources().getDimensionPixelSize( R.dimen._52sdp );
                            int[] vLoc     = new int[ 2 ];
                            int[] imageLoc = new int[ 2 ];
                            int   angle;
                            
                            
                            // From position
                            moves.getLocationOnScreen( vLoc );
                            // To position
                            ex.getLocationOnScreen( imageLoc );
                            
                            
                            // Move the tile to the new location
                            // then animate a move from old location
                            params = new FrameLayout.LayoutParams( width, height );
                            gridMask.addView( sweeper, params );
                            
                            // Center for distance from Moves tile
                            vLoc[ 0 ] += ((moves.getWidth() / 2f) - (width / 2f));
                            vLoc[ 1 ] -= height - (moves.getHeight() / 2);
                            
                            // Center for distance to Coin
                            imageLoc[ 0 ] += ((ex.getWidth() / 2f) - (sweeper.getWidth() / 2f));
                            imageLoc[ 1 ] -= height - (ex.getHeight() / 2);
                            
                            
                            // Place on "Moves" icon
                            params.setMargins( vLoc[ 0 ], vLoc[ 1 ], vLoc[ 0 ] + width, vLoc[ 1 ] + height );
                            sweeper.setLayoutParams( params );
                            
                            // Center the two tiles to each other
                            sweeper.setPivotX( width / 2f );
                            // Pivot on the bottom of the projectile
                            sweeper.setPivotY( height );
                            
                            // Final rotation angle
                            angle = ( int ) PointXYZ.angleOfTwoPoints( new Point( vLoc[ 0 ], vLoc[ 1 ] ), new Point( imageLoc[ 0 ], imageLoc[ 1 ] ) ) - 180;
                            sweeper.setRotation( angle );
                            
                            //
                            // Save the target to swap at the end of animation
                            //
                            Point t = new Point( ex.getPosition(), delay );
                            sweeper.setTag( t );
                            
                            
                            //###############################
                            //
                            //
                            //
                            //###############################
                            sweeper.getViewTreeObserver().addOnPreDrawListener( new ViewTreeObserver.OnPreDrawListener()
                            {
                                @Override
                                public boolean onPreDraw()
                                {
                                    sweeper.getViewTreeObserver().removeOnPreDrawListener( this );
                                    
                                    int[]            vLoc2     = new int[ 2 ];
                                    int[]            imageLoc2 = new int[ 2 ];
                                    Point            t         = ( Point ) sweeper.getTag();
                                    BoardTile        ex        = boardTiles.get( t.x );
                                    GradientTextView moveText  = moves.findViewById( R.id.boardMovesText );
                                    
                                    
                                    // From position
                                    moves.getLocationOnScreen( vLoc2 );
                                    // To position
                                    ex.getLocationOnScreen( imageLoc2 );
                                    
                                    // Center for distance from Moves tile
                                    vLoc2[ 0 ] += ((moves.getWidth() / 2f) - (width / 2f));
                                    vLoc2[ 1 ] -= height - (moves.getHeight() / 2);
                                    // Center for distance to Coin
                                    imageLoc2[ 0 ] += ((ex.getWidth() / 2f) - (sweeper.getWidth() / 2f));
                                    imageLoc2[ 1 ] -= sweeper.getHeight() - (ex.getHeight() / 2);
                                    
                                    float transX = (imageLoc2[ 0 ] - vLoc2[ 0 ]);
                                    float transY = (imageLoc2[ 1 ] - vLoc2[ 1 ]);
                                    
                                    // Clear data
                                    sweeper.setTag( null );
                                    
                                    //
                                    sweeper.animate().setDuration( DURATION ).setStartDelay( t.y );
                                    sweeper.animate().translationY( transY ).translationX( transX ).setInterpolator( new AccelerateDecelerateInterpolator() );
                                    sweeper.animate().withStartAction( new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            sweeper.setVisibility( View.VISIBLE );
                                            // Decrease Moves
                                            GameEngine.boardMoves--;
                                            GameEngine.boardMoves = Math.max( GameEngine.boardMoves, 0 );
                                            moveText.setText( String.format( Locale.getDefault(), "%d", GameEngine.boardMoves ) );
                                            moveText.invalidate();
                                        }
                                    } );
                                    sweeper.animate().start();
                                    
                                    
                                    // Animate stuff
                                    PropertyValuesHolder sx    = PropertyValuesHolder.ofFloat( "scaleX", 1, 2f, 1 );
                                    PropertyValuesHolder sy    = PropertyValuesHolder.ofFloat( "scaleY", 1, 2f, 1 );
                                    PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat( "alpha", 1, 1, 0 );
                                    
                                    ObjectAnimator ang = ObjectAnimator.ofPropertyValuesHolder( sweeper, sx, sy, alpha );
                                    //
                                    ang.setDuration( DURATION );
                                    ang.setStartDelay( t.y );
                                    //
                                    ang.setInterpolator( new LinearInterpolator() );
                                    ang.addListener( new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd( Animator animation )
                                        {
                                            super.onAnimationEnd( animation );
                                            
                                            BoardTile tile = boardTiles.get( t.x );
                                            tile.setSpecialItem( r.nextInt( 2 ) + BoardTile.SWEEPER_COIN_VERTICAL );
                                            // Transform to the new image
                                            imageHelper( tile.getPosition() );
                                            tile.invalidate();
                                            
                                            //
                                            sweeper.setTag( null );
                                            gridMask.removeView( sweeper );
                                            
                                            // Remove from waiting list
                                            logicThread.animationsRunning--;
                                            if ( logicThread.animationsRunning <= 0 )
                                            {
                                                // Level completed Dialog already displayed!
                                                GameEngine.boardMoves = -1;
                                                
                                                moveText.setText( String.format( Locale.getDefault(), "%d", GameEngine.boardMoves ) );
                                                moveText.invalidate();
                                                onUpdateGrid( false );
                                            }
                                        }
                                    } );
                                    ang.start();
                                    
                                    return false;
                                }
                            } );
                            
                            // Add to the list
                            logicThread.animationsRunning++;
                            delay += (DURATION / 2);
                        }
                    }
                } );
                
                completed.show();
            }
            else
            {
                if ( GameEngine.boardMoves == 0 )
                {
                    LevelCompleted completed = new LevelCompleted( getContext(), gameBoard.xmlBackgrounds[ GameEngine.currentBackground ] );
                    
                    completed.setOnDismissListener( new DialogInterface.OnDismissListener()
                    {
                        @Override
                        public void onDismiss( DialogInterface dialog )
                        {
                            onSignalCompleteHelper( view );
                        }
                    } );
                    
                    completed.show();
                }
                else
                {
                    onSignalCompleteHelper( view );
                }
                
                levelCompleted = true;
            }
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
        
        //############################
        //
        // Hide all the targets
        //
        //############################
        for ( int i = 0; i < targetViews.size(); i++ )
        {
            View v = targetViews.get( i );
            
            if ( v.getVisibility() == VISIBLE )
            {
                int width = mainView.getWidth();
                
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
        //
        //
        //############################
        GridLayoutView gridView = mainView.findViewById( R.id.boardGrid );
        
        // Exit animation for the board
        gridView.animate().scaleX( 0 ).scaleY( 0 ).setDuration( 350 );
        gridView.animate().setStartDelay( 1000 );
        
        //
        animate().scaleX( 0 ).scaleY( 0 ).setDuration( 350 );
        animate().setStartDelay( 1000 );
        animate().setInterpolator( new AccelerateInterpolator() ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                removeAllViews();
                
                // Kill blockers
                for ( BoardTile tile : boardTiles )
                {
                    tile.clearCoin();
                }
                
                if ( onGridUpdateListener != null )
                {
                    onGridUpdateListener.onLevelComplete();
                }
                //
                setScaleX( 1 );
                setScaleY( 1 );
                setVisibility( INVISIBLE );
                
                
                //
                // Clear Board
                //
                gridView.clearMask();
                gridView.setScaleX( 1 );
                gridView.setScaleY( 1 );
                
                
                // Re-enable touching
                canTouch = true;
            }
        } ).start();
    }
    
    
    /**
     * //###############################
     * <p>
     * <p>
     * //###############################
     */
    public void signalLevelFailed()
    {
    }
    
    
    /**
     * ########################################
     * <p>
     * Check to see if any matches remain. Also
     * fills the list for hints. Should be called
     * after board is created and when the board
     * goes IDLE in the thread
     * <p>
     * For hints, specials get the dancing arrows
     * For matches, each tile rocks back and forth
     * <p>
     * ########################################
     */
    public boolean checkIfMatchesExist( boolean isShuffling )
    {
        // Make sure no shuffling during help!
        if ( inHelper )
        {
            return true;
        }
        
        
        // Save the player some time
        if ( timer != null && !timer.isPaused() && !isShuffling )
        {
            timer.pause();
        }
        
        // All checks Kills the counter
        logicThread.setHintTimer( LogicThread.STOP_HINT );
        hintTile = null;
        
        
        //#####################################
        //
        // No specials, keep testing
        //
        //#####################################
        ArrayList<tileGroup> groups = new ArrayList<>();
        
        //
        //        for ( int y = 0; y < maxColors; y++ )
        for ( int y = 0; y < BoardTile.MAX_COINS; y++ )
        {
            groups.add( new tileGroup() );
            groups.get( y ).count = 0;
            groups.get( y ).position = new ArrayList<>();
        }
        
        
        //####################################
        //
        // Find the coins with counts greater
        // than 3
        //
        //####################################
        int holder;
        //
        for ( BoardTile tile : boardTiles )
        {
            holder = tile.tileNum;
            //
            if ( holder < 0 || holder >= BoardTile.MAX_COINS || tile.getBlockerType() > 0 )
            {
                continue;
            }
            
            // Build a color list for optimal efficiency
            //
            groups.get( holder ).count++;
            groups.get( holder ).tileNum = holder;
            // Add to group pointer list for easier access in testing
            groups.get( holder ).position.add( tile.getPosition() );
        }
        
        // Eliminate any small count tiles
        for ( int i = 0; i < maxColors; i++ )
        {
            if ( groups.get( i ).count < 3 )
            {
                // Not used for testing
                groups.get( i ).tileNum = -1;
                groups.get( i ).count = -1;
            }
        }
        
        // Sort the list according to count. High count first
        // Must create comparator.
        Collections.sort( groups, new Comparator<tileGroup>()
        {
            @Override
            public int compare( tileGroup o1, tileGroup o2 )
            {
                return ( int ) (o2.count - o1.count);
            }
        } );
        
        
        // Search using the list element with the most colors first!
        for ( int i = 0; i < groups.size(); i++ )
        {
            // Testing color
            int color = groups.get( i ).tileNum;
            
            for ( int c = 0; c < groups.get( i ).position.size(); c++ )
            {
                // Test each valid color in this group at their position
                int   yPosi = groups.get( i ).position.get( c ) / getRowCount();
                int   xPosi = (groups.get( i ).position.get( c ) % getColumnCount());
                int[] hints = new int[ 3 ];
                
                
                //################################
                //
                // Recursive loop test
                //
                //################################
                hints[ 0 ] = groups.get( i ).position.get( c );
                if ( testForMatch( xPosi, yPosi, 1, new int[]{ 0, 0 }, color, hints ) >= 3 )
                {
                    // Set a tile to highlight
                    //                    hintTile = new PointXYZ( (yPosi * getRowCount() + xPosi), -1 );
                    hintTile = new PointXYZ( hints[ 0 ], hints[ 1 ], hints[ 2 ] );
                    break;
                }
            }
            
            // We are done searching
            if ( hintTile != null )
            {
                break;
            }
        }
        
        
        //
        // Any SUPER specials?
        //
        if ( hintTile == null )
        {
            for ( BoardTile tile : boardTiles )
            {
                if ( tile.tileNum == BoardTile.RAINBOW_COIN /*|| tile.getSpecialItem() == BoardTile.SWEEPER_COIN_VERTICAL || tile.getSpecialItem() == BoardTile.SWEEPER_COIN_HORIZONTAL*/ )
                {
                    hintTile = new PointXYZ( tile.getPosition(), -1, -1 );
                    break;
                }
            }
        }
        
        
        //###############################
        //
        // All failed!
        // No matches AND no shuffle or
        // random power bomb
        //
        //###############################
        if ( hintTile == null )
        {
            // Give the player a free shuffle
            // Or give the player a rainbow power bomb!
            animateShuffling();
            //            Toast.makeText( getContext(), "Game locked. Reshuffling", Toast.LENGTH_SHORT ).show();
            //            useShuffler();
            
/*
            if ( logicThread != null )
            {
                // Reset hint timer
                logicThread.setHintTimer( LogicThread.PLAY_HINT );
            }
*/
            
            return false;
        }
        
        //
        // Save the player some time
        //
        if ( timer != null && timer.isPaused() && !isShuffling )
        {
            timer.resume();
        }
        
        
/*
        if ( logicThread != null )
        {
            // Reset hint timer
            logicThread.setHintTimer( LogicThread.PLAY_HINT );
        }
*/
        
        return true;
    }
    
    
    /**
     * //###############################
     * <p>
     * Communication between the thread
     * and this allows for a hint system
     * when the player is IDLE / stuck
     * <p>
     * //###############################
     */
    public void showHint()
    {
        if ( (GameEngine.systemFlags & GameEngine.HINT_OFF) == GameEngine.HINT_OFF || inHelper )
        {
            logicThread.setHintTimer( LogicThread.STOP_HINT );
            return;
        }
        
        //
        //
        //
        if ( !GameEngine.isKilled && hintTile != null && tilePoints.size() == 0 )
        {
            final View[]                hintPointer = new View[ 4 ];
            final ConnectionsGridLayout grid        = mainView.findViewById( R.id.boardGridLayout );
            final FrameLayout           layout      = mainView.findViewById( R.id.boardGridSpecials );
            
            //
            // Display the Arrows
            //
            if ( hintTile.x >= 0 && hintTile.y == -1 && hintTile.z == -1 )
            {
                // Display arrows for matches or specials under a hint
                BoardTile tile       = boardTiles.get( hintTile.x );
                int       size       = getResources().getDimensionPixelSize( R.dimen.TILE_SIZE );
                float     moveAmount = (size / 4f);
                
                //
                for ( int i = 0; i < 4; i++ )
                {
                    ObjectAnimator           mover;
                    FrameLayout.LayoutParams params;
                    
                    hintPointer[ i ] = new View( getContext() );
                    hintPointer[ i ].setPivotY( size / 2f );
                    hintPointer[ i ].setPivotX( size / 2f );
                    hintPointer[ i ].setId( 3000 + i );
                    hintPointer[ i ].setVisibility( VISIBLE );
                    hintPointer[ i ].setBackgroundResource( R.drawable.hint_arrow );
                    //                    hintPointer[ i ].setLayoutParams( new ConstraintLayout.LayoutParams( size, size ) );
                    //
                    params = new FrameLayout.LayoutParams( size, size );
                    params.setMargins( tile.getLeft(), tile.getTop(), tile.getRight(), tile.getBottom() );
                    layout.addView( hintPointer[ i ], params );
                    hintPointer[ i ].requestLayout();
                    
                    //
                    //-------------------------------
                    //
                    switch ( i )
                    {   // Right
                        case 1:
                            hintPointer[ i ].setRotation( 90 );
                            hintPointer[ i ].setTranslationX( size );
                            //
                            mover = ObjectAnimator.ofFloat( hintPointer[ i ], "translationX", size, size - moveAmount, size, size - moveAmount );
                            break;
                        // Down
                        case 2:
                            hintPointer[ i ].setRotation( 180 );
                            hintPointer[ i ].setTranslationX( 0 );
                            hintPointer[ i ].setTranslationY( size );
                            //
                            mover = ObjectAnimator.ofFloat( hintPointer[ i ], "translationY", size, size - moveAmount, size, size - moveAmount );
                            break;
                        // Left
                        case 3:
                            hintPointer[ i ].setRotation( -90 );
                            hintPointer[ i ].setTranslationX( -size );
                            //
                            mover = ObjectAnimator.ofFloat( hintPointer[ i ], "translationX", -size, -size + moveAmount, -size, -size + moveAmount );
                            break;
                        // Up
                        case 0:
                        default:
                            hintPointer[ i ].setRotation( 0 );
                            hintPointer[ i ].setTranslationX( 0 );
                            hintPointer[ i ].setTranslationY( size );
                            //
                            mover = ObjectAnimator.ofFloat( hintPointer[ i ], "translationY", -size, -size + moveAmount, -size, -size + moveAmount );
                            break;
                    }
                    
                    final int index = i;
                    
                    mover.setDuration( 2000 ).setRepeatMode( ValueAnimator.REVERSE );
                    //                mover.setRepeatCount( 2 );
                    mover.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            layout.removeView( hintPointer[ index ] );
                            hintPointer[ index ].setTranslationX( 0 );
                            hintPointer[ index ].setTranslationY( 0 );
                            
                            if ( index == 3 )
                            {
                                logicThread.setHintTimer( LogicThread.PLAY_HINT );
                                logicThread.hintIsDisplayed = 0;
                            }
                        }
                    } );
                    
                    gameEngine.animatorList.add( mover );
                    mover.start();
                }
                
                // Showing the hints
                logicThread.hintIsDisplayed = 1;
            }
            //
            // Rock the tiles back and forth
            //
            else if ( hintTile.z > -1 && hintTile.y > -1 && hintTile.x > -1 )
            {
                int[] hints     = new int[]{ hintTile.x, hintTile.y, hintTile.z };
                int[] arrayPtrs = GameEngine.arrayFromResource( getContext(), R.array.coin_shine_list );
                
                
                //##################################
                //
                //
                //
                //##################################
                for ( int hint : hints )
                {
                    BoardTile tile     = boardTiles.get( hint );
                    int       image_ID = tile.getImageResource();
                    int       bg_ID    = tile.getBackgroundResource();
                    
                    int[] arrays = GameEngine.arrayFromResource( getContext(), arrayPtrs[ tile.tileNum ] );
                    
                    // Use a shuffle / Random, fool!
                    ObjectAnimator mover = ObjectAnimator.ofInt( tile, "imageResource", arrays );
                    
                    mover.setEvaluator( new CustomEvaluator() );
                    mover.setDuration( 500 ).setInterpolator( new LinearInterpolator() );
                    mover.setRepeatCount( 1 );
                    mover.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            logicThread.setHintTimer( LogicThread.PLAY_HINT );
                            
                            // Showing the hints
                            logicThread.hintIsDisplayed = 0;
                            
                            tile.setImageResource( image_ID );
                            tile.setBackgroundResource( bg_ID );
                        }
                    } );
                    
                    //
                    tile.setTag( mover );
                    gameEngine.animatorList.add( mover );
                    mover.start();
/*
                    // Use a shuffle / Random, fool!
                    ObjectAnimator mover = ObjectAnimator.ofFloat( tile, "rotation", 0, 10, -10, 10, -10, 10, 0 );
                    
                    // Pivot on bottom of the tile
                    tile.setPivotY( tile.getHeight() );
                    
                    //
                    mover.setDuration( 2000 ).setInterpolator( new LinearInterpolator() );
                    mover.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            logicThread.setHintTimer( LogicThread.PLAY_HINT );
                            
                            // Showing the hints
                            logicThread.hintIsDisplayed = 0;
                            //
                            tile.setPivotY( tile.getHeight() / 2f );
                        }
                    } );
                    
                    //
                    tile.setTag( mover );
                    gameEngine.animatorList.add( mover );
                    mover.start();
*/
                }
                
                // Showing the hints
                logicThread.hintIsDisplayed = 2;
            }
        }
        else
        {
            logicThread.setHintTimer( LogicThread.PLAY_HINT );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Animate the shuffle process
     * <p>
     * //###############################
     */
    private void animateShuffling()
    {
        useShuffler();
    }
    
    
    /**
     * //###############################
     * <p>
     * Search for potential matches
     * <p>
     * //###############################
     *
     * @return
     */
    private int testForMatch( int startX, int startY, int count, int[] skipDirection, int color, int[] hintTiles )
    {
        // Each direction set is an X, Y offset
        final int[][] moveDirection = {
                { 0, -1 }, // Up
                { -1, -1 }, // Up / Left
                { -1, 0 }, // Left
                { -1, 1 }, // Left / Down
                { 0, 1 }, // Down
                { 1, 1 }, // Right / Down
                { 1, 0 }, //Right
                { 1, -1 }, // Up / Right
        };
        
        //
        int       result = 0;
        BoardTile tile;
        
        
        // Test the current tile in each direction
        // A match count of 2 is all that is needed because
        // the test tile is one already
        for ( int[] moves : moveDirection )
        {
            int x = moves[ 0 ];
            int y = moves[ 1 ];
            
            //
            // Need to skip testing this direction
            //
            if ( skipDirection[ 0 ] == x && skipDirection[ 1 ] == y )
            {
                continue;
            }
            
            // Only valid directions
            if ( (startX + x) < 0 || (startY + y) < 0 || (startX + x) >= getColumnCount() || (startY + y) >= getRowCount() )
            {
                continue;
            }
            
            //
            // Does this tested tile match?
            //
            tile = boardTiles.get( (startY + y) * getColumnCount() + (startX + x) );
            
            // Coins under blockers or
            // blockers don't count
            if ( tile.getBlockerType() == 0 && tile.tileNum == color && tile.getSpecialItem() < 1 )
            {
                // Save all good hint finds in a list
                hintTiles[ count ] = tile.getPosition();
                
                count++;
                
                // Have three matches yet?
                if ( count > 2 )
                {
                    result = 3;
                    break;
                }
                else
                {
                    result = testForMatch( startX + x, startY + y, count, new int[]{ -x, -y }, color, hintTiles );
                    
                    if ( result >= 2 )
                    {
                        result = 3;
                        break;
                    }
                    else
                    {
                        // Keep checking!
                        count--;
                    }
                }
            }
        }
        
        //
        return result;
    }
    
    
    //########################################################################
    //
    // Specials processing formula:
    //      Swap initiates the matching process. Should either of the swapped
    //      tiles be a 'special' tile, match making is canceled and ONLY the
    //      special tile is matched. If two specials are swapped together then
    //      the special with the highest value takes precedence.
    //
    //      Next: USE_SPECIAL_ITEM is called, and if it is NOT a clock, then
    //      handle creating matches inside the SPECIAL_CALL.
    //      If possible, attach an 'AnimateBG' call to the affected BoardTile's
    //      'animator' property and play that animation instead of normal one
    //      if a special exit is requested
    //
    //      Consider adding buttons for special again. Instead of stacking the
    //      board, add duplicates to the players stack for later use.
    //########################################################################
    
    /**
     * ########################################
     * <p>
     * Clock special use method
     * <p>
     * ########################################
     *
     * @param image N/A
     */
    private void useAClock( final BoardTile image )
    {
/*
        // If it is > 45 seconds, don't bother!
*/
/*        if ( (CustomTimer.currentTime / 1000) > 45 )
        {
            return;
        }
        else*//*

        if ( timer != null )
        {   // Add 30 or more seconds!
            timer.pause();
            timer.updateTime( 30f );
            
            final TextView tv           = mainView.findViewById( R.id.boardTimeTextAnimated );
            int[]          xyPosi;
            final int      toX;
            final int      toY;
            int            center       = 0;
            final int      mainDuration = AnimationValues.DROP_TIME;
            
            // Clock tile's grid position
            xyPosi = getTileXY( image );
            
            toX = ( int ) tv.getX();
            toY = ( int ) tv.getY();
            
            // Set it to move
            tv.setX( xyPosi[ 0 ] );
            center = AnimationValues.getCenter( image.getHeight(), (tv.getHeight() / 2) );
            tv.setY( xyPosi[ 1 ] + center );
            tv.setRotation( 0 );
            
            
            //###############################
            //
            //
            //
            //###############################
            ObjectAnimator[]     clock  = new ObjectAnimator[ 3 ];
            PropertyValuesHolder rotate = PropertyValuesHolder.ofFloat( "rotation", 360 );
            PropertyValuesHolder sx     = PropertyValuesHolder.ofFloat( "scaleX", 1f, 0 );
            PropertyValuesHolder sy     = PropertyValuesHolder.ofFloat( "scaleY", 1f, 0 );
            
            clock[ 0 ] = ObjectAnimator.ofPropertyValuesHolder( image, rotate, sx, sy );
            clock[ 0 ].setDuration( mainDuration );
            clock[ 0 ].setInterpolator( new AnticipateOvershootInterpolator( 1f ) );
            
            //
            PropertyValuesHolder px      = PropertyValuesHolder.ofFloat( "x", toX );
            PropertyValuesHolder py      = PropertyValuesHolder.ofFloat( "y", toY );
            PropertyValuesHolder rotate2 = PropertyValuesHolder.ofFloat( "rotation", -720 );
            PropertyValuesHolder color   = PropertyValuesHolder.ofInt( "textColor", 0xFFFFFFFF, 0xFFFF0000 );
            
            // Move the "+30" text, change color
            clock[ 1 ] = ObjectAnimator.ofPropertyValuesHolder( tv, px, py, rotate2, color ).setDuration( mainDuration );
            clock[ 1 ].setInterpolator( new AccelerateInterpolator() );
            clock[ 1 ].setStartDelay( mainDuration );
            clock[ 1 ].addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                    //@@@@@@@@@@@@@@@@@@ Star landing sound
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.STAR_LEVEL_UP );
                    }
                    
                    tv.setTextColor( Color.WHITE );
                    tv.setVisibility( View.VISIBLE );
                    tv.setText( "+30" );
                    // Setup the moving text
                    tv.setPivotX( tv.getWidth() / 2f );
                    tv.setPivotY( tv.getHeight() / 2f );
                    tv.setScaleX( 1 );
                    tv.setScaleY( 1 );
                    tv.setX( toX );
                    tv.setY( toY );
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                
                }
                
                @Override
                public void onAnimationCancel( Animator animation )
                {
                
                }
                
                @Override
                public void onAnimationRepeat( Animator animation )
                {
                
                }
            } );
            
            
            //##################################
            //
            // Set up the final scale animation
            //
            //##################################s
            PropertyValuesHolder sx2 = PropertyValuesHolder.ofFloat( "scaleX", 1f, 2f );
            PropertyValuesHolder sy2 = PropertyValuesHolder.ofFloat( "scaleY", 1f, 2f );
            
            clock[ 2 ] = ObjectAnimator.ofPropertyValuesHolder( tv, sx2, sy2 );
            clock[ 2 ].setDuration( mainDuration );
            clock[ 2 ].setStartDelay( mainDuration * 2 );
            clock[ 2 ].setInterpolator( new CustomBounceInterpolator( 0.2f, 20 ) );
            clock[ 2 ].addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    tv.setText( "" );
                    
                    //@@@@@@@@@@@@@@@@@@ Star shoot sound for landing
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.STAR_SHOOT );
                    }
                    
                    int[] resArray = gameEngine.arrayFromResource( R.array.star_burst );
                    
                    ObjectAnimator bg = ObjectAnimator.ofInt( tv, "backgroundResource", resArray );
                    bg.setEvaluator( new CustomEvaluator() );
                    bg.setInterpolator( new LinearInterpolator() );
                    bg.setDuration( (resArray.length - 1) * 25 );
                    bg.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            tv.setVisibility( INVISIBLE );
                            tv.setBackgroundResource( 0 );
                        }
                    } );
                    bg.start();
                }
            } );
            
            // Eliminate any infinite loops caused by solving
            // special tiles withing a special call
            image.setTag( clock );
            image.specialItem = -1;
            image.setState( BoardTile.SPECIAL_ANIMATE );
            
        }
        else
        {
            matchList[ image.getTileY() ][ image.getTileX() ] = 0;
            image.setState( BoardTile.STATE_MATCHED );
        }
*/
    }
    
    
    /**
     * ########################################
     * <p>
     * Allows the player to shuffle the board
     * with a bomb affect.
     * Coins near explosision shake a little
     * Master bomb expands then explodes
     * <p>
     * ########################################
     */
    private void useABomb( final BoardTile image )
    {
        // Save the player some time
        if ( timer != null )
        {
            timer.pause();
        }
        
        // For recursive special calls
        final ArrayList<Point> specials = new ArrayList<>();
        
        
        //#######################################
        //
        // Master bomb grows then explodes
        //
        //#######################################
        final FrameLayout    frame      = mainView.findViewById( R.id.boardGridSpecials );
        int                  startDelay = 0;
        int                  baseDelay  = 350;
        int[]                resIds;
        int[]                xyPosi     = new int[ 2 ];
        final BoardTile      master     = new BoardTile( getContext() );
        ObjectAnimator[]     bomb       = new ObjectAnimator[ 2 ];
        PropertyValuesHolder sx         = PropertyValuesHolder.ofFloat( "scaleX", 1, 3 );
        PropertyValuesHolder sy         = PropertyValuesHolder.ofFloat( "scaleY", 1, 3 );
        
        
        // I want the master bomb to be above all other coins
        master.setVisibility( VISIBLE );
        master.setId( image.getId() );
        master.setPivotX( image.getWidth() / 2f );
        master.setPivotY( image.getHeight() / 2f );
        //
        //        GameBoardFragment.makeConnection( master, frame, .5f, .5f );
        frame.addView( master, image.getWidth(), image.getHeight() );
        
        master.setX( image.getX() );
        master.setY( image.getY() );
        master.setScaleType( ImageView.ScaleType.FIT_CENTER );
        master.setBackgroundResource( GameBoard.coinSet[ image.tileNum ] );
        
        
        bomb[ 0 ] = ObjectAnimator.ofPropertyValuesHolder( master, sx, sy );
        bomb[ 0 ].setInterpolator( new CustomBounceInterpolator( 0.2f, 20 ) );
        bomb[ 0 ].setDuration( baseDelay + DEBUG_ANIM_START );
        bomb[ 0 ].setStartDelay( startDelay );
        bomb[ 0 ].addListener( new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart( Animator animation )
            {
                //@@@@@@@@@@@@@@@@@@ Bomb sound
                if ( gameEngine.soundPlayer != null && !inHelper )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.BOMB_EXPAND );
                }
            }
            
            @Override
            public void onAnimationEnd( Animator animation )
            {
                //@@@@@@@@@@@@@@@@@@ Explode Sound
                if ( gameEngine.soundPlayer != null && !inHelper )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.BOMB_SPECIAL );
                }
                
                int[]         resIds = GameEngine.arrayFromResource( getContext(), R.array.bomb_explode );
                ValueAnimator dummy  = ValueAnimator.ofInt( 0, 1 );
                dummy.setDuration( resIds.length * 50 ).addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        frame.removeView( master );
                    }
                } );
                dummy.start();
            }
            
            @Override
            public void onAnimationCancel( Animator animation )
            {
            
            }
            
            @Override
            public void onAnimationRepeat( Animator animation )
            {
            
            }
        } );
        
        
        //##########################################
        //
        // Extra delay time from "special Capture"
        //
        //##########################################
        resIds = GameEngine.arrayFromResource( getContext(), R.array.bomb_explode );
        //
        bomb[ 1 ] = ObjectAnimator.ofInt( master, "backgroundResource", resIds );
        bomb[ 1 ].setEvaluator( new CustomEvaluator() );
        bomb[ 1 ].setDuration( resIds.length * 50 );
        bomb[ 1 ].setStartDelay( baseDelay );
        bomb[ 1 ].setInterpolator( new LinearInterpolator() );
        //
        image.setBackgroundResource( 0 );
        
        // Eliminate any infinite loops caused by solving
        // special tiles withing a special call
        image.setState( BoardTile.SPECIAL_ANIMATE );
        image.setTag( bomb );
        image.setSpecialItem( -1 );
        image.isCaught = true;
        
        
        //##################################
        //
        // All other bomb coins
        //
        //##################################
        // Start the proceeding animation AFTER the
        // master tile's initial animation
        int     tileX;
        int     tileY;
        int[]   grid  = { -1, 0, 1 };
        int[][] shake = new int[ mapHeight ][ mapWidth ];
        
        
        //########################################
        //
        // Make a super bomb when time running out
        //
        //########################################
        if ( timer != null && CustomTimer.currentTime < SUPER_TIME_VALUE && !inHelper )
        {
            grid = new int[]{ -2, -1, 0, 1, 2 };
        }
        
        //##############################
        // Match affected tiles, and
        // Attach a special animation
        // to exit
        //##############################
        for ( int value : grid )
        {
            tileY = image.getTileY() + value;
            
            //
            if ( tileY > -1 && tileY < getRowCount() )
            {
                for ( int i : grid )
                {
                    tileX = image.getTileX() + i;
                    if ( tileX > -1 && tileX < getColumnCount() )
                    {
                        final BoardTile tile = boardTiles.get( tileY * mapWidth + tileX );
                        ObjectAnimator  anim;
                        
                        if ( tile.tileNum == -1 || matchList[ tileY ][ tileX ] == 0 || tile.getPosition() == image.getPosition() || tile.isCaught )
                        {
                            continue;
                        }
                        
                        // If we are affecting another special
                        // item, then run that item's special code
                        if ( tile.getBlockerType() > 0 )
                        {
                            tile.setState( BoardTile.BLOCKER_MATCHED );
                            tile.animDelay = baseDelay;
                            tile.setTag( null );
                        }
                        else if ( tile.getSpecialItem() > 0 )
                        {
                            if ( tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL )
                            {
                                // After the bomb explodes
                                resIds = GameEngine.arrayFromResource( getContext(), R.array.bomb_explode );
                                
                                specials.add( new Point( tile.getPosition(), baseDelay /*+ (resIds.length * 50)*/ ) );
                            }
                            else if ( tile.tileNum >= BoardTile.PURPLE_GEM && tile.tileNum <= BoardTile.YELLOW_GEM )
                            {
                                tile.setState( BoardTile.GEM_MATCHED );
                                tile.animDelay = baseDelay;
                                // Clear tile in Match List
                                matchList[ tile.getTileY() ][ tile.getTileX() ] = 2;
                            }
                        }
                        else
                        {
                            // Setup the Bomb animation for this tile
                            tile.setPivotX( tile.getWidth() / 2f );
                            tile.setPivotY( tile.getHeight() / 2f );
                            //
                            resIds = GameEngine.arrayFromResource( getContext(), R.array.bomb_explode );
                            //
                            anim = ObjectAnimator.ofInt( tile, "imageResource", resIds );
                            anim.setEvaluator( new CustomEvaluator() );
                            anim.setDuration( resIds.length * 50 );
                            anim.setInterpolator( new LinearInterpolator() );
                            anim.setStartDelay( baseDelay );
                            
                            // Clear tile in Match List
                            matchList[ tile.getTileY() ][ tile.getTileX() ] = 2;
                            
                            // Attach it to the view
                            //                            tile.endAnimation();
                            tile.setTag( anim );
                            tile.setState( BoardTile.SPECIAL_ANIMATE );
                        }
                    }
                }
            }
        }
        
        
        //#################################
        //
        // Prepare the shake-ables grid
        //
        //#################################
        GameEngine.arrayCopy( matchList, shake, mapHeight, mapWidth );
        
        int[][]             direction = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
        ArrayList<PointXYZ> shakeList = new ArrayList<>();
        
        
        // Find the tiles that can shake
        for ( int y = 0; y < mapHeight; y++ )
        {
            for ( int x = 0; x < mapWidth; x++ )
            {
                if ( shake[ y ][ x ] == 2 )
                {
                    matchList[ y ][ x ] = 0;
                    
                    for ( int i = 0; i < 4; i++ )
                    {
                        int baseX = x + direction[ i ][ 0 ];
                        int baseY = y + direction[ i ][ 1 ];
                        
                        
                        if ( baseX < mapWidth && baseX > -1 && baseY < mapHeight && baseY > -1 && (shake[ baseY ][ baseX ] != 2 && shake[ baseY ][ baseX ] != -1 && shake[ baseY ][ baseX ] != 0) )
                        {
                            BoardTile tile    = boardTiles.get( baseY * mapWidth + baseX );
                            int       offsetX = image.getTileX() - tile.getTileX();
                            int       offsetY = image.getTileY() - tile.getTileY();
                            
                            if ( tile.getPosition() == image.getPosition() || tile.isCaught )
                            {
                                continue;
                            }
                            
                            // If X is negative, shake favors the right, else the left
                            // If Y is positive, shake favors the top, else the bottom
                            shakeList.add( new PointXYZ( (offsetX < 0) ? 1 : -1, (offsetY < 0) ? 1 : -1, (baseY * mapWidth + baseX) ) );
                        }
                    }
                    
                }
            }
        }
        
        
        //##############################
        //
        // Activate the shaker!
        //
        //##############################
        for ( int i = 0; i < shakeList.size(); i++ )
        {
            PointXYZ             index = shakeList.get( i );
            final BoardTile      tile  = boardTiles.get( index.z );
            ObjectAnimator       shaker;
            PropertyValuesHolder px    = PropertyValuesHolder.ofFloat( "translationX", index.x * 4 );
            PropertyValuesHolder py    = PropertyValuesHolder.ofFloat( "translationY", index.y * 4 );
            //
            sx = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.25f, 0.75f, 1.15f, 1 );
            sy = PropertyValuesHolder.ofFloat( "scaleY", 1, 0.75f, 1.25f, 0.85f, 1 );
            
            shaker = ObjectAnimator.ofPropertyValuesHolder( tile, px, py, sx, sy );
            shaker.setInterpolator( new LinearInterpolator() );
            shaker.setDuration( 4 * 50 );
            shaker.setRepeatCount( 2 );
            shaker.setStartDelay( baseDelay );
            shaker.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    // Dummy listener so one is not added
                    tile.setTranslationY( 0 );
                    tile.setTranslationX( 0 );
                    tile.setState( BoardTile.STATE_ACTIVE );
                    tile.setTag( null );
                }
            } );
            
            //
            shaker.start();
            //
            //            tile.setState( BoardTile.SPECIAL_ANIMATE );
            //            tile.setTag( shaker );
        }
        
        
        // Capture more specials if found
        if ( specials.size() > 0 )
        {
            captureSweepers( specials );
        }
        
        
        // Save the player some time
        if ( timer != null )
        {
            timer.resume();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Vertical AND Horizontal bolt
     * clears coins
     * <p>
     * //###############################
     */
    private void useABolt( final BoardTile image )
    {
        useABolt( image, 0, 2, -1 );
    }
    
    private void useABolt( final BoardTile image, int delay, int direction, int color_index )
    {
        // Save the player some time
        if ( timer != null )
        {
            timer.pause();
        }
        
        
        //#######################################
        //
        // Master bomb grows then explodes
        //
        //#######################################
        final FrameLayout frame       = mainView.findViewById( R.id.boardGridSpecials );
        int               half_width  = image.getWidth() / 2;
        int               half_height = image.getHeight() / 2;
        int               width       = image.getWidth();
        int               height      = image.getHeight();
        int               xx;
        int               yy;
        int[]             resId;
        boolean           hasLayout   = false;
        
        //        frame.addView( spark );
        final int          DURATION      = 350;
        ArrayList<Integer> affectedTiles = new ArrayList<>();
        
        // For recursive special calls
        final ArrayList<Point> specials = new ArrayList<>();
        
        
        // Center-of-it-all tile
        Point[] start = new Point[ 2 ];
        Point[] end   = new Point[ 2 ];
        int[][] bolts = new int[ 2 ][];
        
        
        // Get the ACTIVE height and width for the current board
        // from where the initiator tile is
        int startX = -1, endX = -1;
        int startY = -1, endY = -1;
        int incX, incY;
        
        int[][] grid = {
                // incX, incY, xStart, yStart
                { 0, 1, image.getTileX(), 0 },  // Up to Down
                { 1, 0, 0, image.getTileY() } // Left to Right:
        };
        
        
        //#################################
        //
        // Get the sweeps positioned
        //
        //#################################
        for ( int i = 0; i < grid.length; i++ )
        {
            int[] index = grid[ i ];
            
            // Choose the shortest tile count in
            // each direction as a starting point
            // First is for "|" bolt pattern
            // Second is for "-" bolt pattern
            incX = index[ 0 ];
            incY = index[ 1 ];
            startX = index[ 2 ];
            startY = index[ 3 ];
            
            
            // 0 and 90 degree
            // Get the start
            while ( boardMap[ startY ][ startX ] == -1 )
            {
                startX += incX;
                startY += incY;
            }
            
            // Get the end
            int realY = startY, realX = startX;
            endY = startY;
            endX = startX;
            
            while ( endY < mapHeight && endX < mapWidth )
            {
                // Null tile
                if ( boardMap[ endY ][ endX ] != -1 )
                {
                    affectedTiles.add( endY * mapWidth + endX );
                    realY = endY;
                    realX = endX;
                }
                //
                endX += incX;
                endY += incY;
            }
            
            // Use the REAL values
            endX = realX;
            endY = realY;
            
            
            // Transfer over bolt match info
            bolts[ i ] = new int[ affectedTiles.size() ];
            for ( int c = 0; c < affectedTiles.size(); c++ )
            {
                bolts[ i ][ c ] = affectedTiles.get( c );
            }
            
            // Clear for the next round
            affectedTiles.clear();
            
            BoardTile startTile = boardTiles.get( startY * mapWidth + startX );
            BoardTile endTile   = boardTiles.get( endY * mapWidth + endX );
            
            start[ i ] = new Point( ((( int ) startTile.getX() / width) * width), ((( int ) startTile.getY() / height) * height) );
            end[ i ] = new Point( ((( int ) endTile.getX() / width) * width), ((( int ) endTile.getY() / height) * height) );
        }
        
        
        //##################################
        //
        // Loop variables
        //
        //##################################
        int loopStart;
        int loopMax;
        
        // Determine the direction
        if ( direction == 0 )
        {
            // Vertical
            loopStart = 0;
            loopMax = bolts.length - 1;
        }
        else if ( direction == 1 )
        {
            // Horizontal
            loopStart = 1;
            loopMax = bolts.length;
        }
        else
        {
            loopStart = 0;
            loopMax = bolts.length;
        }
        
        //
        //
        //
        for ( int i = loopStart; i < loopMax; i++ )
        {
            //####################################
            //
            // Loop through affected tiles
            //
            //####################################
            for ( int c = 0; c < bolts[ i ].length; c++ )
            {
                BoardTile nextTile = boardTiles.get( bolts[ i ][ c ] );
                
                if ( nextTile.getPosition() != image.getPosition() && nextTile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL )
                {
                    // Capture more specials if found
                    specials.add( new Point( nextTile.getPosition(), delay ) );
                    continue;
                }
                
                //
                //---------------------------------
                //
                if ( nextTile.getTag() == null )
                {
                    // Must do the first tile as well
                    // Burst the tile at the end
                    //
                    
                    // Clear this tile
                    matchList[ nextTile.getTileY() ][ nextTile.getTileX() ] = 0;
                    
                    
                    //##############################
                    //
                    //
                    //
                    //##############################
                    if ( nextTile.getBlockerType() > 0 )
                    {
                        nextTile.setState( BoardTile.BLOCKER_MATCHED );
                        nextTile.setTag( null );
                    }
                    else if ( nextTile.tileNum >= BoardTile.PURPLE_GEM && nextTile.tileNum <= BoardTile.YELLOW_GEM )
                    {
                        nextTile.setState( BoardTile.GEM_MATCHED );
                        nextTile.animDelay = DURATION + delay;
                    }
                    else
                    {
                        nextTile.setState( BoardTile.STATE_MATCHED );
                        nextTile.setTag( 1 );
                        nextTile.animDelay = delay;
                        
                        //
                        if ( nextTile.getPosition() == image.getPosition() )
                        {
                            nextTile.setBackgroundResource( 0 );
                        }
                    }
                }
            }
            
            
            // Don't even bother if a length is a single match
            if ( bolts[ i ].length <= 1 )
            {
                continue;
            }
            
            //####################################
            // Match affected tiles, and Attach a
            // special animation to exit
            // Only the start and end tiles get
            // bolt contact
            //####################################
            float                      xDist;
            float                      yDist;
            double                     length;
            final ImageView            lightning;
            final ObjectAnimator       flasher;
            final PropertyValuesHolder sy, alpha;
            final View                 layout = View.inflate( getContext(), R.layout.ring_special_layout, null );
            FrameLayout.LayoutParams   params;
            
            
            // Get the length
            xDist = (end[ i ].x - start[ i ].x);
            yDist = (end[ i ].y - start[ i ].y);
            length = Math.sqrt( (xDist * xDist) + (yDist * yDist) );
            
            
            // Create the linked lightning
            lightning = new ImageView( getContext() );
            lightning.setId( 1000 + 1 );
            lightning.setVisibility( INVISIBLE );
            lightning.setScaleType( ImageView.ScaleType.FIT_CENTER );
            
            //
            if ( color_index > -1 )
            {
                lightning.setColorFilter( glowColors[ color_index ], PorterDuff.Mode.MULTIPLY );
            }
            
            //
            if ( xDist > yDist )
            {
                lightning.setBackgroundResource( R.drawable.light_sweep_single_hori );
                lightning.setImageResource( R.drawable.light_sweep_triple_hori );
                lightning.setPivotY( height / 2f );
                lightning.setPivotX( ( float ) length / 2f );
                
                xx = start[ i ].x + half_width;
                yy = start[ i ].y;
                params = new FrameLayout.LayoutParams( ( int ) length, height );
            }
            else
            {
                lightning.setBackgroundResource( R.drawable.light_sweep_single_vert );
                lightning.setImageResource( R.drawable.light_sweep_triple_vert );
                lightning.setPivotX( width / 2f );
                lightning.setPivotY( ( float ) length / 2f );
                
                xx = start[ i ].x;
                yy = start[ i ].y + half_height;
                params = new FrameLayout.LayoutParams( width, ( int ) length );
            }
            
            //
            params.setMargins( xx, yy, width, ( int ) length );
            frame.addView( lightning, params );
            
            
            // Add he ring view
            if ( !hasLayout )
            {
                layout.setX( image.getX() );
                layout.setY( image.getY() );
                frame.addView( layout, image.getWidth(), image.getHeight() );
                
                layout.setScaleY( .6f );
                layout.setScaleX( .6f );
                //
                layout.animate().setStartDelay( delay );
                //
                layout.setVisibility( INVISIBLE );
                
                layout.animate().setDuration( DURATION ).setInterpolator( new AccelerateInterpolator() );
                layout.animate().scaleY( 1f ).scaleX( 1f ).start();
                hasLayout = true;
            }
            
            
            //####################################
            //
            // Animate each lighting bolt
            //
            //####################################
            if ( xDist > yDist )
            {
                sy = PropertyValuesHolder.ofFloat( "scaleX", 1f, 2.5f );
            }
            else
            {
                sy = PropertyValuesHolder.ofFloat( "scaleY", 1f, 2.5f );
            }
            
            //            alpha = PropertyValuesHolder.ofFloat( "alpha", 1, 0 );
            flasher = ObjectAnimator.ofPropertyValuesHolder( lightning, sy );
            flasher.setDuration( DURATION + DEBUG_ANIM_START );
            flasher.setStartDelay( delay );
            
            
            // Start these flashes now!
            flasher.setInterpolator( new LinearInterpolator() );
            flasher.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                    lightning.setVisibility( VISIBLE );
                    //
                    if ( layout.getVisibility() == INVISIBLE )
                    {
                        layout.setVisibility( VISIBLE );
                    }
                    
                    //@@@@@@@@@@@@@@@@@@ Spark sound
                    if ( gameEngine.soundPlayer != null && !inHelper )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.LIGHTNING_SPECIAL );
                    }
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    gameEngine.animatorList.remove( flasher );
                    frame.removeAllViews();
                    
                    //@@@@@@@@@@@@@@@@@@ Spark sound
                    if ( gameEngine.soundPlayer != null && !inHelper )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                    }
                }
                
                @Override
                public void onAnimationCancel( Animator animation )
                {
                
                }
                
                @Override
                public void onAnimationRepeat( Animator animation )
                {
                
                }
            } );
            
            //
            gameEngine.animatorList.add( flasher );
            flasher.start();
        }
        
        // If bombs caught in the crossfire
        if ( specials.size() > 0 )
        {
            captureSweepers( specials );
        }
        
        
        // Eliminate any infinite loops caused by solving
        // special tiles within a special call
        image.setSpecialItem( -1 );
        image.setSoundEfx( PlaySound.SPECIAL_MATCHED );
        
        // Save the player some time
        if ( timer != null )
        {
            timer.resume();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Use the star booster
     * <p>
     * //###############################
     */
    private void useAStar( final BoardTile image )
    {
        // Save the player some time
        if ( timer != null )
        {
            timer.pause();
        }
        
        // Init the rotating star animation and play until it is ready to shoot!
        final FrameLayout      frame     = mainView.findViewById( R.id.boardGridSpecials );
        final int              size      = image.getWidth();
        final BoardTile[]      tv        = new BoardTile[ 5 ];
        final int              baseDelay = 1000;
        final ArrayList<Point> specials  = new ArrayList<>();         // For recursive special call
        //final View               layout    = View.inflate( getContext(), R.layout.ring_special_layout, null );
        
        
        //#######################################
        //
        // Now, set the projectiles to shoot off
        // when the master tile's initial
        // animation ends!
        //
        //#######################################
        // Spark sound; 2 seconds
        if ( gameEngine.soundPlayer != null && !inHelper )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.CHARGE_BEAM_SPECIAL );
        }
        
        //
        // Rotate the main star
        //
        image.setBackgroundResource( R.drawable.star_2_on );
        image.setPivotX( image.getWidth() / 2f );
        image.setPivotY( image.getHeight() / 2f );
        //
        ObjectAnimator star = ObjectAnimator.ofFloat( image, "rotation", 0, 720 );
        
        star.setInterpolator( new AnticipateOvershootInterpolator() );
        star.setDuration( baseDelay + DEBUG_ANIM_START );
        star.addListener( new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart( Animator animation )
            {
                image.specialTile = -1;
                image.setRotation( 0 );
                
                // Spark sound; 2 seconds
                if ( gameEngine.soundPlayer != null && !inHelper )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.STAR_SHOOT );
                }
            }
            
            @Override
            public void onAnimationEnd( Animator animation )
            {
                ParticleSystem parts;
                
                // Activating tile
                parts = new ParticleSystem( ( Activity ) getContext(), 15, R.drawable.particle_gem_yellow, 750 ).setSpeedRange( 0.05f, 0.1f );
                parts.setRotationSpeed( .5f );
                parts.setFadeOut( 750 );
                parts.oneShot( image, 15 );
                
                // Remove the tile as particles display
                //                image.setImageResource( 0 );
            }
            
            @Override
            public void onAnimationCancel( Animator animation )
            {
            
            }
            
            @Override
            public void onAnimationRepeat( Animator animation )
            {
            
            }
        } );
        
        //
        // Add he ring view
        //
/*
        layout.setX( image.getX() );
        layout.setY( image.getY() );
        frame.addView( layout, image.getWidth(), image.getHeight() );
*/
        
        
        //############################
        //
        // Get boundaries
        //
        //############################
        int maxWidth  = mapWidth;
        int maxHeight = mapHeight;
        int minHeight = -1;
        int minWidth  = -1;
        
        // Up, Right, Down-right, Down-left, left
        int[][] destination = new int[][]{
                //  Up
                { 0, -(frame.getHeight() + size) },
                // Right
                { frame.getWidth() + size, 0 },
                // Down Right,
                { frame.getHeight() + size, frame.getWidth() + size },
                // Down Left
                { -(frame.getWidth() + size), frame.getHeight() + size },
                // Left
                { -(frame.getWidth() + size), 0 }
                /*
                                //  Up
                                { 0, -1 },
                                // Right
                                { 1, 0 },
                                // Down Right,
                                { 1, 1 },
                                // Down Left
                                { -1, 1 },
                                // Left
                                { -1, 0 }
                */
        };
        
        
        //##################################
        //
        // Shoot off the stars after MASTER
        // star spin.
        // Create 5 empty views to hold star
        // beams for shooting animation
        //
        //##################################
        for ( int v = 0; v < 5; v++ )
        {
            final int index = v;
            tv[ v ] = new BoardTile( getContext() );
            //
            tv[ v ].setId( 1000 + v );
            tv[ v ].setVisibility( INVISIBLE );
            tv[ v ].setBackgroundResource( R.drawable.star_2_on );
            tv[ v ].setPivotX( size / 2f );
            tv[ v ].setPivotY( size / 2f );
            
            //
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( size, size );
            params.setMargins( image.getLeft(), image.getTop(), image.getRight(), image.getBottom() );
            
            //
            // Add each star to the Frame layout!
            //
            tv[ v ].setLayoutParams( params );
            tv[ v ].setAlpha( 1f );
            tv[ v ].setTranslationX( 0 );
            tv[ v ].setTranslationY( 0 );
            // Place it in the layout
            
            frame.addView( tv[ v ] );
            //
            tv[ v ].animate().translationX( destination[ v ][ 0 ] ).translationY( destination[ v ][ 1 ] );
            tv[ v ].animate().rotation( 360 * 4 );
            tv[ v ].animate().alpha( 0 );
            tv[ v ].animate().setStartDelay( baseDelay );
            tv[ v ].animate().setDuration( baseDelay + DEBUG_ANIM_START ).setInterpolator( new LinearInterpolator() );
            // Allow support for pausing
            tv[ v ].animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( tv[ index ] != null && tv[ index ].animator == null )
                    {
                        tv[ index ].animator = animation;
                    }
                }
            } );
            
            // Clean up code!
            tv[ v ].animate().withStartAction( new Runnable()
            {
                @Override
                public void run()
                {
                    tv[ index ].setVisibility( VISIBLE );
                }
            } );
            tv[ v ].animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    frame.removeView( tv[ index ] );
                    tv[ index ].clearAnimation();
                    tv[ index ].setVisibility( INVISIBLE );
                    
                    //
                    gameEngine.animatorList.remove( tv[ index ].animator );
                    tv[ index ].animator = null;
                    tv[ index ] = null;
                }
            } );
            
            //
            tv[ v ].animate().start();
        }
        
        
        //#######################################
        //
        // Star animation main code for all the
        // regular coins to be matched
        //
        //#######################################
        int tileY = image.getTileY();
        int tileX = image.getTileX();
        // Up, Right, Down-Right, Down-Left, Left
        int[][] direction = { { 0, -1 }, { 1, 0 }, { 1, 1 }, { -1, 1 }, { -1, 0 }, };
        
        
        // Match affected tiles, and Attach a
        // special animation to exit
        // Star has 5 prongs, check each direction
        for ( int i = 0; i < 5; i++ )
        {
            int t_x        = tileX;
            int t_y        = tileY;
            int delayValue = 1;
            
            // Do we have a good tile??
            while ( (t_x < maxWidth && t_x > minWidth) && (t_y < maxHeight && t_y > minHeight) )
            {
                final BoardTile tile = boardTiles.get( t_y * mapWidth + t_x );
                
                //
                if ( tile.tileNum == -1 )
                {
                    // Get the next tile
                    t_x += direction[ i ][ 0 ];
                    t_y += direction[ i ][ 1 ];
                    continue;
                }
                
                // If we are affecting another special
                // item, then run that item's special code
                if ( (tile.getBlockerType() > 0 || tile.getSpecialItem() > 0) && tile.getPosition() != image.getPosition() )
                {
                    // Striped Coins!
                    if ( tile.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && tile.getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL )
                    {
                        specials.add( new Point( tile.getPosition(), baseDelay + delayValue ) );
                    }
                    else if ( tile.tileNum >= BoardTile.PURPLE_GEM && tile.tileNum <= BoardTile.YELLOW_GEM )
                    {
                        tile.setState( BoardTile.GEM_MATCHED );
                        tile.animDelay = baseDelay + delayValue;
                    }
                    else if ( tile.getBlockerType() > 0 )
                    {
                        tile.setState( BoardTile.BLOCKER_MATCHED );
                        tile.animDelay = baseDelay + delayValue;
                        tile.setTag( null );
                    }
                }
                else
                {
                    // Set the coin in a MATCHED state
                    matchList[ t_y ][ t_x ] = 0;
                    
                    // All following animations need to start as the projectile passes over them
                    if ( tile.getPosition() != image.getPosition() )
                    {
                        //
                        tile.setPivotX( tile.getWidth() / 2f );
                        tile.setPivotY( tile.getHeight() / 2f );
                        tile.setSpecialItem( -1 );
                        
                        // Designate this a special particle effect
                        tile.setTag( 1 );
                        // Need a delay
                        tile.animDelay = baseDelay + delayValue;
                        tile.setState( BoardTile.STATE_MATCHED );
                        tile.setSoundEfx( PlaySound.SPECIAL_MATCHED );
                    }
                }
                
                // Get the next tile
                t_x += direction[ i ][ 0 ];
                t_y += direction[ i ][ 1 ];
                delayValue += (baseDelay / mapWidth);
            }
        }
        
        
        //
        // If bombs caught in the crossfire
        //
        if ( specials.size() > 0 )
        {
            captureSweepers( specials );
        }
        
        
        //########################################
        //
        // Eliminate any infinite loops
        // caused by solving
        // special tiles withing a special call
        //
        //########################################
        image.setSpecialItem( -1 );
        image.setState( BoardTile.SPECIAL_ANIMATE );
        //        image.setState( BoardTile.STATE_MATCHED );
        image.setTag( star );
        
        // Save the player some time
        if ( timer != null )
        {
            timer.resume();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Strikes the swapped coin, bounces
     * the coins in the air
     * ( while spinning ) and drops them
     * to a particle effect
     * and matches them all
     * <p>
     * //###############################
     */
    private void useChargeBeam( final BoardTile image )
    {
        // Save the player some time
        if ( timer != null )
        {
            timer.pause();
        }
        
        //#######################################
        //
        // Beam from Rainbow coin
        //
        //#######################################
        FrameLayout layout      = mainView.findViewById( R.id.boardGridSpecials );
        int         half_width  = image.getWidth() / 2;
        int         half_height = image.getHeight() / 2;
        int         width       = image.getWidth();
        int         height      = image.getHeight();
        int         xx;
        int         yy;
        int         colorToMatch;
        int         duration    = 0;
        
        
        //##################################
        //
        // Center-of-it-all tile
        //
        //##################################
        Point start = new Point( ((( int ) image.getX() / width) * width), ((( int ) image.getY() / height) * height) );
        
        
        //@@@@@@@@@@@@@@@@@@ Spark sound
        if ( gameEngine.soundPlayer != null && !inHelper )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.LIGHTNING_SPECIAL );
        }
        
        //
        if ( boardTiles.get( tilePoints.get( 0 ) ).getSpecialItem() == BoardTile.RAINBOW_SEEKER )
        {
            colorToMatch = boardTiles.get( tilePoints.get( 1 ) ).tileNum;
        }
        else
        {
            colorToMatch = boardTiles.get( tilePoints.get( 0 ) ).tileNum;
        }
        
        //TODO: Kill the delay code and redo
        // messing up too much
        
        //###################################
        //
        // Find all tiles of the same color
        // Match affected tiles, and Attach
        //
        //###################################
        for ( int y = 0; y < boardTiles.size(); y++ )
        {
            final BoardTile      tile = boardTiles.get( y );
            float                xDist;
            float                yDist;
            double               length;
            double               angle;
            int                  center;
            final ImageView      coin_beam;
            final ObjectAnimator flasher;
            
            //
            // All except initiator
            //
            if ( tile.tileNum == colorToMatch && tile.getPosition() != image.getPosition() )
            //                    if ( tile.tileNum == colorToMatch )
            {
                // Get the end points for the CONNECTED TO tiles
                FrameLayout.LayoutParams params;
                Point                    end = new Point( ((( int ) tile.getX() / width) * width), ((( int ) tile.getY() / height) * height) );
                
                //
                // Get the length
                //
                xDist = (end.x - start.x);
                yDist = (end.y - start.y);
                length = Math.sqrt( (xDist * xDist) + (yDist * yDist) );
                angle = PointXYZ.angleOfTwoPoints( start, end ) - 180;
                
                // Create the linked beam
                coin_beam = new ImageView( getContext() );
                coin_beam.setId( 1000 + y );
                coin_beam.setVisibility( VISIBLE );
                coin_beam.setScaleType( ImageView.ScaleType.FIT_XY );
                
                int adjustWidth = width / 2;
                
                // Gonna be half the size of a tile
                coin_beam.setPivotX( adjustWidth / 2f );
                coin_beam.setPivotY( 0 );
                //
                // Adjust the offsets on a grid to center on the tiles
                //
                params = new FrameLayout.LayoutParams( adjustWidth, ( int ) length );
                params.setMargins( start.x, start.y, start.x + width, start.y + ( int ) length );
                //                xx = start.x;
                //                yy = start.y;
                
                // Beam centered with the tile
                coin_beam.setTranslationX( (width - adjustWidth) / 2f );
                coin_beam.setTranslationY( height / 2f );
                coin_beam.setRotation( ( int ) angle );
                
                //
                // Add it to Specials layer
                //
                //                layout.addView( coin_beam, width / 3, ( int ) length );
                layout.addView( coin_beam, params );
                
                
                //####################################
                //
                // Animate each Beam ray
                //
                //####################################
                final int[] resId    = GameEngine.arrayFromResource( getContext(), R.array.coin_beam );
                final int   DURATION = resId.length * 50;
                
                flasher = ObjectAnimator.ofInt( coin_beam, "backgroundResource", resId );
                flasher.setEvaluator( new CustomEvaluator() );
                flasher.setInterpolator( new LinearInterpolator() );
                flasher.setDuration( DURATION );
                //                flasher.setStartDelay( (y / mapHeight) * 75 );
                
                // Save this value
                duration = DURATION;
                
                // Start these flashes now!
                flasher.addListener( new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart( Animator animation )
                    {
                        //@@@@@@@@@@@@@@@@@@ Spark sound
                        if ( gameEngine.soundPlayer != null && tile.getPosition() == image.getPosition() )
                        {
                            gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                        }
                        
                        //
                        // Begin explode process
                        //
                        ValueAnimator valueAnimator;
                        valueAnimator = ValueAnimator.ofInt( 0, 15 );
                        valueAnimator.setInterpolator( new LinearInterpolator() );
                        valueAnimator.setStartDelay( DURATION / 2 );
                        valueAnimator.setDuration( 100 );
                        valueAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate( ValueAnimator animation )
                            {
                                int color = ( int ) animation.getAnimatedValue();
                                
                                color = (color << 28) | 0x0FFFFFFF;
                                
                                ViewCompat.setBackgroundTintMode( tile, PorterDuff.Mode.SRC_ATOP );
                                ViewCompat.setBackgroundTintList( tile, ColorStateList.valueOf( color ) );
                                
                                //                                ImageViewCompat.setImageTintList( image, ColorStateList.valueOf( color ) );
                            }
                        } );
                        valueAnimator.start();
                    }
                    
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        gameEngine.animatorList.remove( flasher );
                        
                        //                        ViewCompat.setBackgroundTintList( tile, null );
                        layout.removeView( coin_beam );
                    }
                    
                    @Override
                    public void onAnimationCancel( Animator animation )
                    {
                    
                    }
                    
                    @Override
                    public void onAnimationRepeat( Animator animation )
                    {
                    
                    }
                } );
                
                //
                // For pausing purposes
                //
                gameEngine.animatorList.add( flasher );
                flasher.start();
                
                // Clear this tile
                matchList[ tile.getTileY() ][ tile.getTileX() ] = 0;
                
                // Standard particle effect
                tile.setTag( null );
                tile.animDelay = 0;//DURATION;
                tile.setState( BoardTile.STATE_MATCHED );
                
                //                break;
            }
        }
        
        
        // Clear this tile
        matchList[ image.getTileY() ][ image.getTileX() ] = 0;
        
        
        //###################################
        //
        // Set up the initiator coin
        //
        //###################################
        final int lastDur = duration;
        ImageView tile    = new ImageView( getContext() );
        
        tile.setY( image.getY() );
        tile.setX( image.getX() );
        tile.setBackgroundResource( coinSet[ image.tileNum ] );
        
        // Place on top of rays
        layout.addView( tile, image.getWidth(), image.getHeight() );
        
        tile.setPivotX( image.getWidth() / 2f );
        tile.setPivotY( image.getHeight() / 2f );
        tile.animate().setDuration( duration );
        tile.animate().rotationY( 1080 ).setInterpolator( new AccelerateInterpolator() );
        tile.animate().withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                //
                // Begin explode process
                //
                ValueAnimator valueAnimator;
                valueAnimator = ValueAnimator.ofInt( 0, 15 );
                valueAnimator.setInterpolator( new LinearInterpolator() );
                valueAnimator.setDuration( lastDur );
                valueAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        int color = ( int ) animation.getAnimatedValue();
                        
                        color = (color << 28) | 0x0FFFFFFF;
                        
                        ViewCompat.setBackgroundTintMode( tile, PorterDuff.Mode.SRC_ATOP );
                        ViewCompat.setBackgroundTintList( tile, ColorStateList.valueOf( color ) );
                    }
                } );
                
                //
                valueAnimator.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        layout.removeView( tile );
                    }
                } );
                valueAnimator.start();
            }
        } ).start();
        
        // Exit animations setting
        image.animDelay = duration;
        image.setBackgroundResource( 0 );
        //                image.setState( BoardTile.STATE_MATCHED );
        image.setState( BoardTile.SPECIAL_ANIMATE );
        image.setTag( 1 );
        
        // Save the player some time
        if ( timer != null )
        
        {
            timer.resume();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Shuffles all tiles on the board.
     * All tiles spring to one location
     * in the air
     * then fall to a new location
     * <p>
     * //###############################
     */
    public void useShuffler()
    {
        int       shuffleCount = 0;
        int       index        = 0;
        BoardTile tile;
        boolean   activeTiles  = false;
        
        
        //###############################################
        // NEED TO GIVE PLAYER BACK SPECIALS AFTER
        // SHUFFLE
        // Take this opportunity to save any specials
        //###############################################
        for ( int y = 0; y < boardTiles.size(); y++ )
        {
            tile = boardTiles.get( y );
            
            if ( tile.tileNum != -1 )
            {
                activeTiles = true;
                break;
            }
        }
        
        
        // No need staying if nothing is active
        if ( !activeTiles )
        {
            return;
        }
        
        
        //#############################################
        //
        //  FIX THE DAMN SHUFFLER AND OTHER BUTTON ISSUES
        //
        //#############################################
        while ( shuffleCount < 4 )
        {
            // Build the main board
            // Animate the coins falling and bouncing into place
            for ( int y = 0; y < mapHeight; y++ )
            {
                for ( int x = 0; x < mapWidth; x++ )
                {
                    tile = boardTiles.get( y * mapWidth + x );
                    
                    //
                    // Do not touch Blockers and Gems!
                    //
                    if ( tile.tileNum != -1 && tile.getBlockerType() == 0 && tile.tileNum < BoardTile.PURPLE_GEM )
                    {
                        int maxLoops = 0;
                        index = r.nextInt( maxColors );
                        //
                        while ( gameBoard.DuplicateAt( x, y, index ) && maxLoops < 50 )
                        {
                            index = r.nextInt( maxColors );
                            maxLoops++;
                        }
                        
                        // Assign a color to the new tile
                        tile.tileNum = index;
                        //                        tile.specialItem = 0;
                        
                        // Announce it's presence
                        tile.setState( BoardTile.ANNOUNCE_PRESENCE );
                        tile.setSoundEfx( -2 );
                    }
                }
            }
            
            // Make sure the board has solvable tiles
            if ( checkIfMatchesExist( true ) )
            {
                break;
            }
            //
            shuffleCount++;
        }
        
        
        //
        // If the above fails, give the player a RAINBOW COIN
        // special for their troubles!
        //
        if ( shuffleCount > 3 )
        {
            Random r = new Random( 412837465 );
            int    i;
            
            i = r.nextInt( boardTiles.size() );
            while ( boardTiles.get( i ).tileNum == -1 || boardTiles.get( i ).tileNum >= BoardTile.PURPLE_GEM || boardTiles.get( i ).getBlockerType() > 0 )
            {
                i = r.nextInt( boardTiles.size() );
            }
            
            // Beam special, basically
            hintTile = new PointXYZ( i, -1, -1 );
            boardTiles.get( i ).tileNum = BoardTile.RAINBOW_COIN;
            boardTiles.get( i ).setSpecialItem( BoardTile.RAINBOW_SEEKER );
            boardTiles.get( i ).specialTile = -1;
        }
        
        // Display the changes
        onUpdateGrid();
    }
    
    
    /**
     * //###############################
     * <p>
     * Run the selected booster effect
     * <p>
     * //###############################
     *
     * @param setBooster
     */
    public void activateBooster( final BoardTile image, int setBooster, boolean isRandom )
    {
        // Random special Sound
        if ( isRandom && gameEngine.soundPlayer != null && !inHelper )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.RANDOM_SPECIAL );
        }
        
        //
        switch ( setBooster )
        {
            case BoardTile.BOLT:
                useABolt( image );
                break;
            case BoardTile.STAR:
                useAStar( image );
                break;
            case 0:
            default:
                useABomb( image );
                break;
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Capture recursive specials
     * <p>
     * //###############################
     */
    public void captureSweepers( ArrayList<Point> specials )
    {
        // Check for recursive special calls
        for ( int i = 0; i < specials.size(); i++ )
        {
            BoardTile image = boardTiles.get( specials.get( i ).x );
            
            // Recursive call
            if ( image.getSpecialItem() >= BoardTile.SWEEPER_COIN_VERTICAL && image.getSpecialItem() <= BoardTile.SWEEPER_COIN_HORIZONTAL && !image.isCaught )
            {
                // Classify this as a "caught" special so an extra
                // Thread call is NOT made
                image.isCaught = true;
                useABolt( image, specials.get( i ).y, (image.getSpecialItem() - BoardTile.SWEEPER_COIN_VERTICAL), image.tileNum );
            }
        }
    }
    
    
    @Override
    protected void onDetachedFromWindow()
    {
        mainView = null;
        gameBoard = null;
        gameEngine = null;
        
        // Kill the thread
        if ( logicThread != null )
        {
            //            logicThread.killThread();
            logicThread = null;
        }
        //
        
        boardTiles = null;
        targetViews = null;
        tilePoints = null;
        coinSet = null;
        coinBombIcons = null;
        glowColors = null;
        
        boardMap = null;
        matchList = null;
        
        specials = null;
        onGridUpdateListener = null;
        onBoosterListener = null;
        
        r = null;
        timer = null;
        linearGradient = null;
        lineBuffer = null;
        bufCanvas = null;
        tileTypes = null;
        
        super.onDetachedFromWindow();
    }
}
