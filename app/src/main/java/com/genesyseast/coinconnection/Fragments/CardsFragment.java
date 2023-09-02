package com.genesyseast.coinconnection.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.Dialogs.GamePaused;
import com.genesyseast.coinconnection.Dialogs.LevelResults;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.GameGraphics.CardsGridLayout;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.GameState;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static com.genesyseast.coinconnection.GameEngine.GameEngine.mInterstitialAd;


public class CardsFragment
        extends Fragment
        implements View.OnClickListener, GamePaused.OnResult, CustomTimer.OnTimerCommunicateListener, CardsGridLayout.OnGridUpdateListener, LevelResults.OnLevelUpListener
{
    private       GameEngine           gameEngine;
    private       GameBoard            gameBoard;
    private       LogicThread          logicThread;
    // Specific for the praising text. No duplicate splashes
    private       int                  cardMatches   = -1;
    //
    public static ArrayList<BoardTile> boardTiles;
    private       ValueAnimator        pointsAnimator;
    private       ValueAnimator        scoreAnimator;
    //
    private       AdView               adView;
    // Inflate the layout for this fragment
    public static View                 view_main;
    public static int                  levelScore    = 0;
    public static int                  cardHintCount = 0;
    private       Bitmap               loadBmp       = null;
    
    // This will be applied to each menu layout for the menu navagation
    public CardsFragment()
    {
        // Required empty public constructor
    }
    
    
    /**
     * //###################################
     * <p>
     * Create the main view for this class
     * <p>
     * //###################################
     *
     * @param inflater           N/A
     * @param container          N/A
     * @param savedInstanceState N/A
     *
     * @return N/A
     */
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        Random r = new Random();
        
        
        // Get instances of needed classes
        gameEngine = GameEngine.getInstance( getContext() );
        gameBoard = GameBoard.getInstance( getContext() );
        
        // Inflate the layout for this fragment
        view_main = inflater.inflate( R.layout.game_cards_fragment, container, false );
        
        //
        boardTiles = new ArrayList<>();
        
        // Get everything rolling!
        loadLevelData();
        
        //#######################################
        //
        // Intercept the back key
        //
        //#######################################s
        view_main.setFocusableInTouchMode( true );
        view_main.requestFocus();
        view_main.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    if ( getView() != null )
                    {
                        GradientTextView imageButton = getView().findViewById( R.id.pauseButton );
                        
                        //
                        if ( imageButton.isClickable() )
                        {
                            imageButton.callOnClick();
                        }
                    }
                    
                    return true;
                }
                
                return false;
            }
        } );
        
        
        //######################################
        //
        // Display those Ads
        //
        //######################################
        AdRequest adRequest;
        Bundle    extras = new Bundle();
        extras.putString( "max_ad_content_rating", "G" );
        
        
/*
        if ( !GameEngine.debugMode )
        {
            // Actual Ads
            adView = view_main.findViewById( R.id.adView );
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
        }
        else
        {
            adView = view_main.findViewById( R.id.adViewTest );
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
        }
        
        // Show either Ad
        
        adView.loadAd( adRequest );
        adView.setVisibility( View.VISIBLE );
*/
        
        
        //##############################
        //
        // Load the required sounds
        //
        //##############################
        view_main.setAlpha( 0f );
        view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                if ( view_main.getTag() == null )
                {
                    gameEngine.animatorList.add( animation );
                }
            }
        } );
        view_main.animate().setDuration( AnimationValues.FADE_TIME ).alpha( 1f );
        view_main.animate().withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                view_main.setTag( null );
            }
        } ).start();
        
        //
        cardMatches = 0;
        
        return view_main;
    }
    
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if ( view_main != null )
        {
            view_main.setFocusableInTouchMode( true );
            view_main.requestFocus();
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle pausing the game
     * <p>
     * //###################################
     */
    private void killGameStatus( int killStatus )
    {
        if ( GameEngine.timer != null )
        {
            // Clean up
/*
            gameEngine.animatorList.remove( GameEngine.timer );
            //            timer.removeAllListeners();
            GameEngine.timer.setUserCancel( killStatus );
            GameEngine.timer.cancel();
*/
        }
        
        // Kill BG Music
        //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgMusic( -1, PlaySound.STOP );
        }
        
        
        // Pause all tile animations
        for ( int i = 0; i < boardTiles.size(); i++ )
        {
            boardTiles.get( i ).cancelAnimation();
        }
        
        if ( scoreAnimator != null )
        {
            //          scoreAnimator.removeAllListeners();
            scoreAnimator.cancel();
        }
        
        if ( pointsAnimator != null )
        {
            //            pointsAnimator.removeAllListeners();
            pointsAnimator.cancel();
        }
        
        
        // Kill all animations
        for ( int i = (gameEngine.animatorList.size() - 1); i > -1; i-- )
        {
            if ( !(gameEngine.animatorList.get( i ) instanceof ObjectAnimator) )
            {
                ValueAnimator anim = gameEngine.animatorList.get( i );
                if ( anim != null )
                {
                    anim.removeAllListeners();
                    anim.cancel();
                    anim = null;
                }
            }
            else
            {
                ObjectAnimator anim = ( ObjectAnimator ) gameEngine.animatorList.get( i );
                if ( anim != null )
                {
                    anim.removeAllListeners();
                    anim.cancel();
                    anim = null;
                }
            }
        }
        
        
        // Kill the pseudo thread
        logicThread = null;
        
        // Kill the Ads for now
        if ( adView != null )
        {
            adView.destroy();
        }
        
        GameEngine.isKilled = true;
        /*        gameBoard = null;*/
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle pausing the game
     * <p>
     * //###################################
     */
    private void pauseGameStatus()
    {
        // Save existing data
        logicThread.saveLogic();
        
        // Pause all animations
        for ( int i = (gameEngine.animatorList.size() - 1); i > -1; i-- )
        {
            if ( !(gameEngine.animatorList.get( i ) instanceof ObjectAnimator) )
            {
                ValueAnimator anim = gameEngine.animatorList.get( i );
                if ( anim != null )
                {
                    anim.pause();
                }
                else
                {   // Not being used, remove it
                    gameEngine.animatorList.remove( i );
                }
            }
            else
            {
                ObjectAnimator anim = ( ObjectAnimator ) gameEngine.animatorList.get( i );
                if ( anim != null )
                {
                    anim.pause();
                }
                else
                {   // Not being used, remove it
                    gameEngine.animatorList.remove( i );
                }
            }
        }
        
        
        // Pause BoardTiles
        for ( int i = 0; i < boardTiles.size(); i++ )
        {
            boardTiles.get( i ).pauseAnimation( true );
        }
        
        // Pause the Ads for now
        if ( adView != null )
        {
            adView.pause();
        }
        
        
        // Pause it all
        GameEngine.gamePaused = true;
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle pausing the game
     * <p>
     * //###################################
     */
    private void resumeGameStatus()
    {
        if ( getView() != null )
        {
            CardsGridLayout grid = getView().findViewById( R.id.boardGridLayout );
            
            // Resume it all
            GameEngine.gamePaused = false;
            
            // ALWAYS create a new instance
            if ( logicThread == null )
            {
                logicThread = new LogicThread( getActivity() );
                logicThread.setGridLayout( grid );
            }
            
            // Share the ref
            grid.setLogicThread( logicThread );
            
            
            // Pause all animations
            for ( int i = (gameEngine.animatorList.size() - 1); i > -1; i-- )
            {
                if ( !(gameEngine.animatorList.get( i ) instanceof ObjectAnimator) )
                {
                    ValueAnimator anim = gameEngine.animatorList.get( i );
                    if ( anim != null )
                    {
                        anim.resume();
                    }
                    else
                    {   // Not being used, remove it
                        gameEngine.animatorList.remove( i );
                    }
                }
                else
                {
                    ObjectAnimator anim = ( ObjectAnimator ) gameEngine.animatorList.get( i );
                    if ( anim != null )
                    {
                        anim.resume();
                    }
                    else
                    {   // Not being used, remove it
                        gameEngine.animatorList.remove( i );
                    }
                }
            }
            
            
            //#############################
            //
            // Redo the timer
            //
            //#############################
            if ( GameEngine.timer != null )
            {
                if ( GameEngine.timer.getCommunicator() == null )
                {
                    GameEngine.timer.setOnCommunicator( this );
                }
            }
            
            
            // Resume all tile animations
            for ( int i = 0; i < boardTiles.size(); i++ )
            {
                boardTiles.get( i ).pauseAnimation( false );
            }
            
            // Kill the Ads for now
            if ( adView != null )
            {
                adView.resume();
            }
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Universal method to load the current
     * game instance with level data
     * <p>
     * //###################################
     */
    private void loadLevelData()
    {
        final ImageView       background;
        final CardsGridLayout cardsGridLayout;
        Random                r = new Random();
        
        // Pause Button
        GradientTextView pauseButton = view_main.findViewById( R.id.pauseButton );
        pauseButton.setOnClickListener( this );
        
        // Init Game Local data: Special Item stuff, Start time, Update score and moves
        levelScore = 0;
        setGameData( view_main );
        gameEngine.specialRunning = false;
        cardMatches = 0;
        
        
        //############################################
        //
        // Do not select the same board just played
        //
        //############################################
        if ( getContext() != null && !GameEngine.reloadSameGame )
        {
            gameEngine.currentBoardImage = GameBoard.getBoardImage( getContext(), gameEngine.currentBoardImage, true );
        }
        
        
        // DEBUG PURPOSES
        //gameEngine.currentBoardImage = 42;
        
        
        //################################################
        //
        // CONSIDER ADDING ANIMATIONS TO BG
        //      IE: Fish for water bg, snow for ice bg
        //
        //################################################
        background = view_main.findViewById( R.id.boardBackground );
        
        // Start it off!
        GameEngine.currentBackground = r.nextInt( gameBoard.xmlBackgrounds.length );
        
        
        //#########################################
        //
        // Any save data that jas been resumed gets
        // loaded here
        //
        //#########################################
        if ( GameEngine.resumeGame )
        {
            GameState.loadGameState( getContext() );
            
            // Reflect the correct information
            setGameData( view_main );
        }
        
        // Music
        gameEngine.currentBGMusic = GameEngine.currentBackground;
        
        // Set the Background image
        background.setImageResource( gameBoard.xmlBackgrounds[ GameEngine.currentBackground ] );
        
        
        // Get the GridView ready
        cardsGridLayout = view_main.findViewById( R.id.boardGridLayout );
        cardsGridLayout.setBackgroundResource( 0 );
        cardsGridLayout.canSolveMatches = true;
        
        // Set grid update listener
        cardsGridLayout.setOnGridUpdateListener( this );
        cardsGridLayout.setLevelCompleted( false );
        
        
        //#################################
        //
        // Start the thread
        //
        //#################################
        //        if ( logicThread == null || logicThread.getState() == Thread.State.TERMINATED || logicThread.getState() == Thread.State.NEW )
        if ( logicThread == null )
        {
            logicThread = new LogicThread( getActivity() );
        }
        //
        logicThread.clearData();
        
        
        // Create the Logic Thread
        //        logicThread = LogicThread.getInstance( getActivity() );
        // Pass THIS grid layout to the Thread
        cardsGridLayout.setLogicThread( logicThread );
        logicThread.setGridLayout( cardsGridLayout );
        
        //
        GameEngine.isKilled = false;
        
        
        //#########################################
        //
        // Clear Board moves
        //
        //#########################################
        TextView moves = view_main.findViewById( R.id.boardMovesText );
        moves.setText( String.format( Locale.getDefault(), "%3d", 0 ) );
        
        
        // Default Card hint count message
        cardHintCount = 0;
        loadCardHint();
        
        
        //#########################################
        //
        // Animate the Area / Level to the screen
        //
        //#########################################
/*
        TwoWordSlider dialog = new TwoWordSlider();
        Bundle        args   = new Bundle();
        
        args.putString( "left_image", String.format( Locale.getDefault(), "Level: %02d", GameEngine.currentLevel + 1 ) );
        args.putString( "right_image", "" );
        
        dialog.setCancelable( false );
        dialog.setArguments( args );
        dialog.setTargetFragment( CardsFragment.this, 1 );
        //
        if ( getActivity() != null )
        {
            dialog.show( getActivity().getSupportFragmentManager(), "Two Words" );
        }
*/
    }
    
    
    /**
     * //###################################
     * <p>
     * Triggered when a level is completed
     * <p>
     * //###################################
     */
    @Override
    public void onLevelComplete()
    {
        // Handle all "level complete" transitions
        // Animate the Area / Level to the screen
        //        LevelUpDialog dialog     = new LevelUpDialog();
        final LevelResults dialog     = new LevelResults();
        Bundle             args       = new Bundle();
        int                resultCode = -1;
        // todo remove this code. Fix with stars earned for current level
        int  last_star = 3;
        long bonusTimeLeft;
        
        
        // Increase star level
        GameEngine.currentLevel++;
        
        // todo create code to handle new star data format
        
        // Kill the timer
        bonusTimeLeft = gameEngine.boardMoves * 5;
        
        
        // No more resuming previous game
        if ( getContext() != null )
        {
            GameState.eraseSavedGame( getContext() );
        }
        
        // Standard completion
        resultCode = 0;
        
        
        // Kill BG Music
        //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
        if ( gameEngine.soundPlayer != null )
        {
            //            gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.STOP );
            gameEngine.soundPlayer.fadeOutMusic();
        }
        
        // Star burst animation ( spinning )
        // You've earned another star message
        // Big Shiny, spinning star ,opposite star burst spin
        // Request OKAY click to continue
        args.putString( "level_msg", String.format( Locale.getDefault(), "Level %02d completed!\nNice skills", GameEngine.currentLevel ) );
        
        // Earned a new star! Give them slots!
        args.putBoolean( "new_star", true );
        resultCode |= 2;
        
        
        // Standard level message
        args.putInt( "time_left", ( int ) bonusTimeLeft );
        args.putInt( "star_count", last_star );
        args.putInt( "level_score", levelScore );
        args.putInt( "moves", gameEngine.boardMoves );
        args.putInt( "connections", -1 );
        
        
        dialog.setCancelable( true );
        dialog.setArguments( args );
        dialog.setTargetFragment( CardsFragment.this, resultCode );
        
        //
        if ( getActivity() != null )
        {
            view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( view_main.getTag() == null )
                    {
                        view_main.setTag( 1 );
                        gameEngine.animatorList.add( animation );
                    }
                }
            } );
            view_main.setAlpha( .90f );
            view_main.animate().setDuration( 2000 ).alpha( 1f );
            view_main.animate().withStartAction( new Runnable()
            {
                @Override
                public void run()
                {
                    //@@@@@@@@@@@@@@@@@@@@@ Level complete: 3 seconds
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.LEVEL_COMPLETE_PART_1, PlaySound.PLAY );
                        //                        gameEngine.soundPlayer.playBgMusic( PlaySound.LEVEL_COMPLETE_PART_1, PlaySound.PLAY );
                    }
                }
            } );
            view_main.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    view_main.setTag( null );
                    if ( getActivity() != null )
                    {
                        dialog.show( getActivity().getSupportFragmentManager(), "Level Result" );
                    }
                }
            } ).start();
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Triggered when a level is failed
     * <p>
     * //###################################
     */
    @Override
    public void onLevelFailed( String msg )
    {
        if ( getView() != null && getContext() != null )
        {
/*
            final SecureRandom     r         = new SecureRandom();
            final CardsGridLayout  grid      = getView().findViewById( R.id.boardGridLayout );
            final GradientTextView levelFail = new GradientTextView( getContext() );
            Typeface               typeface  = ResourcesCompat.getFont( getContext(), R.font.badaga );
            final ConstraintLayout conLayout = getView().findViewById( R.id.boardGridFrame );
            
            
            // If Tiles being animated, end it!
            if ( logicThread.animationsRunning > 0 )
            {
                for ( BoardTile tile : boardTiles )
                {
                    tile.endAnimation();
                }
            }
            
            
            // Erase old timer save data
*/
/*
            GameEngine.timer.clear();
            CustomTimer.timerStatus = CustomTimer.TIMER_DONE;
*//*

            
            //
            levelFail.setId( 1000 + 1 );
            levelFail.setVisibility( View.VISIBLE );
            
            //#########################
            //
            // Exit message
            //
            //#########################
            levelFail.setText( msg );
            
            //
            levelFail.setTextSize( getResources().getDimensionPixelSize( R.dimen._20ssp ) );
            levelFail.setTranslationY( -getResources().getDimensionPixelSize( R.dimen._32ssp ) * 2 );
            levelFail.setTextColor( Color.WHITE );
            levelFail.setStrokeColor( Color.BLACK );
            levelFail.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen._4sdp ) );
            levelFail.setColorList( R.array.red_reflection );
            levelFail.setGradient( true );
            levelFail.setGradientDirection( GradientTextView.HORIZONTAL );
            levelFail.setTypeface( typeface );
            levelFail.setLayoutParams( new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT ) );
            levelFail.setGravity( Gravity.CENTER_HORIZONTAL );
            
            //
            makeConnection( levelFail, getView().findViewById( R.id.boardGridFrame ), -1, -1 );
            
            Layout layout = levelFail.getLayout();
            
            //
            final ObjectAnimator objTime = ObjectAnimator.ofFloat( levelFail, "translationY", -getResources().getDimensionPixelSize( R.dimen._32ssp ) * 2, 0 );
            objTime.setDuration( 750 ).setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
            objTime.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                    //@@@@@@@@@@@@@@@@@@@ Level Failed
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.LEVEL_FAIL, PlaySound.PLAY );
                    }
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    gameEngine.animatorList.remove( objTime );
                    
                    final ObjectAnimator objTime2 = ObjectAnimator.ofFloat( levelFail, "translationY", 0, grid.getHeight() );
                    
                    gameEngine.animatorList.add( objTime2 );
                    objTime2.setDuration( 2000 ).setInterpolator( new AccelerateInterpolator() );
                    objTime2.setStartDelay( 500 );
                    objTime2.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            
                            //
                            gameEngine.animatorList.remove( objTime2 );
                            
                            View view = getView();
                            GameEngine.gamePaused = false;
                            killGameStatus( CustomTimer.USER_CANCEL );
                            
                            // Remove the "Times Up" view
                            conLayout.removeView( levelFail );
                            
                            
                            if ( view != null )
                            {
                                // Transition back to caller
                                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                                {
                                    @Override
                                    public void onAnimationUpdate( ValueAnimator animation )
                                    {
                                        if ( view.getTag() == null )
                                        {
                                            view.setTag( 1 );
                                            gameEngine.animatorList.add( animation );
                                        }
                                    }
                                } );
                                view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        view.setTag( null );
                                        if ( getActivity() != null )
                                        {
                                            getActivity().onBackPressed();
                                        }
                                    }
                                } ).start();
                            }
                        }
                    } );
                    //
                    gameEngine.animatorList.add( objTime2 );
                    objTime2.start();
                    
                    
                    //#################################
                    //
                    // Display all tiles not solved but
                    // should have been
                    //
                    //#################################
                    int i     = 0;
                    int delay = 0;
                    
                    for ( int c = 0; c < boardTiles.size(); c++ )
                    {
                        BoardTile tile = boardTiles.get( c );
                        
                        if ( tile.specialTile > -1 && tile.getState() != BoardTile.STATE_FLIPPED )
                        {
                            tile.flipCoin( false );
                        }
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
            gameEngine.animatorList.add( objTime );
            objTime.start();
            
            
            //################################
            //
            // Do not allow any saved games
            //
            //################################
            if ( getContext() != null )
            {
                GameState.eraseSavedGame( getContext() );
            }
*/
        }
        
    }
    
    
    /**
     * //###################################
     * <p>
     * Response from calls made by the above
     * method
     * <p>
     * //###################################
     *
     * @param code
     */
    @Override
    public void onLevelUpResult( int code )
    {
        // Runs after the player clicks out of the level up dialog
        // And the player has completed an area
        if ( code == 1 || code == 3 )
        {
            // Is all levels in this area complete?
            // If so, leave here and transition back to the menu
            View view = getView();
            GameEngine.gamePaused = false;
            killGameStatus( CustomTimer.NORMAL_CANCEL );
            
            
            //########################################
            //
            // Make sure to enable the next area
            // If that area exist
            //
            //########################################
            // Transition to Game Completed
            if ( view != null )
            {
                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view.getTag() == null )
                        {
                            view.setTag( 1 );
                            gameEngine.animatorList.add( animation );
                        }
                    }
                } );
                view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        view.setTag( null );
                        if ( getActivity() != null )
                        {
/*
                                FragmentManager fm    = getActivity().getSupportFragmentManager();
                                int             count = fm.getBackStackEntryCount();
                                
                                for ( int i = 0; i < count; ++i )
                                {
                                    fm.popBackStackImmediate();
                                }
*/
                            
                            view.setTag( null );
                            FragmentLoader.SwapFragment( getActivity(), getContext(), new GameCompleted(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        }
                    }
                } ).start();
            }
        }
        //############################################################
        // Runs after the player clicks out of the level up dialog
        // And a star was earned. This gives the player 3 free spins
        // on the slot machine or 1 try on chests!
        //############################################################
        else if ( code == 2 )
        {
            View view = getView();
            
            // Transition to Play Slots
            // Then on to the next level
            if ( view != null )
            {
                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view.getTag() == null )
                        {
                            view.setTag( 1 );
                            gameEngine.animatorList.add( animation );
                        }
                    }
                } );
                view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        view.setTag( null );
                        
                        //
                        // Show mandatory Ad if it's ready
                        // Only show for first bonus and end of area
                        //
                        // TODO: Remove debug and Area > 2 when more users on full release
                        boolean showAd = (GameEngine.currentLevel % 6) == 0;
                        if ( mInterstitialAd.isLoaded() && GameEngine.currentLevel > 20 && showAd )
                        {
                            mInterstitialAd.setAdListener( new AdListener()
                            {
                                @Override
                                public void onAdClosed()
                                {
                                    if ( GameEngine.musicIsPlaying && gameEngine.soundPlayer != null && gameEngine.soundPlayer.music != null )
                                    {
                                        gameEngine.soundPlayer.music.start();
                                    }
                                    
                                    // Get another Ad
                                    GameEngine.loadInterstitialAd( getContext() );
                                    
                                    // Move on
                                    transitionToNextLevel();
                                    
                                    super.onAdClosed();
                                }
                                
                                @Override
                                public void onAdOpened()
                                {
                                    if ( GameEngine.musicIsPlaying )
                                    {
                                        gameEngine.soundPlayer.music.pause();
                                    }
                                    
                                    super.onAdOpened();
                                }
                            } );
                            mInterstitialAd.show();
                        }
                        else
                        {
                            // No Ad shown
                            transitionToNextLevel();
                        }
                    }
                } ).start();
            }
        }
        else
        {
            // Transition to the next area:
            //      Call the Fragment that
            //      Displays the Level Box info transition to complete
            //      status.
            
            // Kill BG Music
            //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.STOP );
            }
            
            // Fade transition to next level
            view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( view_main.getTag() == null )
                    {
                        view_main.setTag( 1 );
                        gameEngine.animatorList.add( animation );
                    }
                }
            } );
            view_main.animate().setDuration( AnimationValues.FADE_TIME ).alpha( 0f );
            view_main.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    view_main.setTag( null );
                    view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            if ( view_main != null && view_main.getTag() == null )
                            {
                                view_main.setTag( 1 );
                                gameEngine.animatorList.add( animation );
                            }
                        }
                    } );
                    view_main.animate().setDuration( AnimationValues.FADE_TIME ).alpha( 1f );
                    view_main.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( view_main != null )
                            {
                                view_main.setTag( null );
                                view_main.animate().setUpdateListener( null );
                            }
                        }
                    } ).start();
                    
                    
                    //####################################
                    //
                    // Decide which game should be played
                    //
                    //####################################
                    if ( (GameEngine.currentLevel % GameEngine.MAX_LEVEL_PER_SELECTOR) == 0 && getActivity() != null )
                    {
                        // Return to the level selector for
                        // bonuses, prizes, etc
                        getActivity().onBackPressed();
                    }
                    else if ( (GameEngine.currentLevel % GameEngine.CARD_GAME_LEVEL) == 0 && getActivity() != null )
                    {
                        getActivity().onBackPressed();
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                    }
                    else
                    {
                        // Load the next level area
                        loadLevelData();
                    }
                }
            } ).start();
        }
    }
    
    
    /**
     *
     */
    public void exitToSelectScreen()
    {
        //
        // Show mandatory Ad if it's ready
        // Only show for first bonus and end of area
        //
        if ( mInterstitialAd.isLoaded() && gameEngine.currentLevel > 2 && GameEngine.debugMode )
        {
            mInterstitialAd.setAdListener( new AdListener()
            {
                @Override
                public void onAdClosed()
                {
                    if ( GameEngine.musicIsPlaying && gameEngine.soundPlayer != null && gameEngine.soundPlayer.music != null )
                    {
                        gameEngine.soundPlayer.music.start();
                    }
                    
                    // Get another Ad
                    GameEngine.loadInterstitialAd( getContext() );
                    
                    // Move on
                    if ( getActivity() != null )
                    {
                        getActivity().onBackPressed();
                    }
                    
                    super.onAdClosed();
                }
                
                @Override
                public void onAdOpened()
                {
                    if ( GameEngine.musicIsPlaying )
                    {
                        gameEngine.soundPlayer.music.pause();
                    }
                    
                    super.onAdOpened();
                }
            } );
            mInterstitialAd.show();
        }
        else
        {
            // No Ad shown
            if ( getActivity() != null )
            {
                getActivity().onBackPressed();
            }
        }
    }
    
    
    /**
     * //####################################
     * <p>
     * Decide which game should be played
     * <p>
     * //####################################
     */
    public void transitionToNextLevel()
    {
        if ( (GameEngine.currentLevel % GameEngine.MAX_LEVEL_PER_SELECTOR) == 0 && getActivity() != null )
        {
            // Return to the level selector for
            // bonuses, prizes, etc
            getActivity().onBackPressed();
        }
        else if ( (GameEngine.currentLevel % GameEngine.CARD_GAME_LEVEL) == 0 && getActivity() != null )
        {
            getActivity().onBackPressed();
            FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
        }
        else
        {
            // Load the next level area
            loadLevelData();
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle score, Level, Moves
     * <p>
     * //###################################
     */
    private void setGameData( View view )
    {
        if ( view != null )
        {
            TextView numText;
            
            
            numText = view.findViewById( R.id.boardLevelText );
            numText.setText( String.format( Locale.getDefault(), " %02d ", GameEngine.currentLevel + 1 ) );
            
            numText = view.findViewById( R.id.boardScoreText );
            //            numText.setText( String.format( Locale.getDefault(), "%d", gameEngine.Scores[ gameEngine.loadedArea ] ) );
            numText.setText( String.format( Locale.getDefault(), " %d ", levelScore ) );
            
            numText = view.findViewById( R.id.boardMovesText );
            numText.setText( String.format( Locale.getDefault(), " %d ", gameEngine.boardMoves ) );
            
            // Set initial Timer
            if ( numText.getTag() == null )
            {
/*
                numText = view.findViewById( R.id.boardTimeText );
                numText.setText( "00 : 00" );
                numText.setTag( 1 );
*/
            }
            
            // Reset praise flag
            cardMatches = -1;
        }
    }
    
    
    /**
     * Create the BG dimmed images
     *
     * @param view
     * @param bgTileResId
     *
     * @return
     */
    public void createBoardImage( View view, int bgTileResId )
    {
        Bitmap                bgTile;
        Canvas                canvas;
        Rect                  dstRect    = new Rect();
        final CardsGridLayout gridLayout = view_main.findViewById( R.id.boardGridLayout );
        
        
        // Try to detect the mapping
        try
        {
            loadBmp = Bitmap.createBitmap( gridLayout.getWidth(), gridLayout.getHeight(), Bitmap.Config.ARGB_8888 );
            bgTile = BitmapFactory.decodeResource( getResources(), bgTileResId );
            canvas = new Canvas( loadBmp );
            
            //
            // Split the image into tile sized sections if possible
            //            bitmaps = Bitmap.createBitmap( loadBmp, (x * width), (y * height), width, height );
            for ( int y = 0; y < gridLayout.getRowCount(); y++ )
            {
                for ( int x = 0; x < gridLayout.getColumnCount(); x++ )
                {
                    // Disabled tile
                    if ( gameBoard.boardMap[ y ][ x ] > -1 )
                    {
                        BoardTile ex = boardTiles.get( y * gridLayout.getColumnCount() + x );
                        
                        dstRect.left = ex.getLeft();
                        dstRect.top = ex.getTop();
                        dstRect.right = ex.getRight();
                        dstRect.bottom = ex.getBottom();
                        //
                        canvas.drawBitmap( bgTile, null, dstRect, null );
                    }
                }
            }
            
            //
            // Set the tiled image
            //
            view.setBackground( new BitmapDrawable( getResources(), loadBmp ) );
            bgTile.recycle();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Load the special item boxes
     * <p>
     * //###################################
     */
    private void loadCardHint()
    {
        // Hint box
        FrameLayout      frame = view_main.findViewById( R.id.cardsHintFrame );
        GradientTextView tv    = frame.findViewById( R.id.cardsHintCounter );
        
        // Give it a clicker!
        frame.setOnClickListener( this );
        
/*
        if ( cardHintCount <= 0 )
        {
            tv.setText( "None" );
        }
        else
*/
        {
            tv.setText( String.format( Locale.getDefault(), "%d", cardHintCount ) );
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Whatever CustomTimer sends me ends
     * up here
     * <p>
     * //###################################
     *
     * @param status
     */
    @Override
    public void onTimeExpired( int status )
    {
        if ( getView() != null && getContext() != null )
        {
/*
            final SecureRandom     r         = new SecureRandom();
            final CardsGridLayout  grid      = getView().findViewById( R.id.boardGridLayout );
            final GradientTextView timesUp   = new GradientTextView( getContext() );
            Typeface               typeface  = ResourcesCompat.getFont( getContext(), R.font.badaga );
            final ConstraintLayout conLayout = getView().findViewById( R.id.boardGridFrame );
            
            
            // If Tiles being animated, end it!
            if ( logicThread.animationsRunning > 0 )
            {
                for ( BoardTile tile : boardTiles )
                {
                    tile.endAnimation();
                }
            }
            
            
            // Erase old timer save data
            GameEngine.timer.clear();
            CustomTimer.timerStatus = CustomTimer.TIMER_DONE;
            
            //
            timesUp.setId( 1000 + 1 );
            timesUp.setVisibility( View.VISIBLE );
            
            //#########################
            //
            // Exit message
            //
            //#########################
            if ( status == CustomTimer.USER_CANCEL )
            {
                timesUp.setText( R.string.see_you_later );
            }
            else
            {
                timesUp.setText( R.string.time_is_up );
            }
            
            
            timesUp.setTextSize( getResources().getDimensionPixelSize( R.dimen._18ssp ) );
            timesUp.setTranslationY( -getResources().getDimensionPixelSize( R.dimen._32ssp ) * 2 );
            timesUp.setTextColor( Color.WHITE );
            timesUp.setStrokeColor( Color.BLACK );
            timesUp.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen._4sdp ) );
            timesUp.setColorList( R.array.red_reflection );
            timesUp.setGradient( true );
            timesUp.setGradientDirection( GradientTextView.HORIZONTAL );
            timesUp.setTypeface( typeface );
            timesUp.setLayoutParams( new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT ) );
            
            //
            makeConnection( timesUp, getView().findViewById( R.id.boardGridFrame ), -1, -1 );
            
            Layout layout = timesUp.getLayout();
            
            //
            final ObjectAnimator objTime = ObjectAnimator.ofFloat( timesUp, "translationY", -getResources().getDimensionPixelSize( R.dimen._32ssp ) * 2, 0 );
            objTime.setDuration( 750 ).setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
            objTime.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                    //@@@@@@@@@@@@@@@@@@@ Level Failed
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.LEVEL_FAIL, PlaySound.PLAY );
                    }
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    gameEngine.animatorList.remove( objTime );
                    
                    final ObjectAnimator objTime2 = ObjectAnimator.ofFloat( timesUp, "translationY", 0, grid.getHeight() );
                    
                    gameEngine.animatorList.add( objTime2 );
                    objTime2.setDuration( 2000 ).setInterpolator( new AccelerateInterpolator() );
                    objTime2.setStartDelay( 500 );
                    objTime2.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            
                            //
                            gameEngine.animatorList.remove( objTime2 );
                            
                            View view = getView();
                            GameEngine.gamePaused = false;
                            killGameStatus( CustomTimer.NORMAL_CANCEL );
                            
                            // Remove the "Times Up" view
                            conLayout.removeView( timesUp );
                            
                            
                            if ( view != null )
                            {
                                // Transition back to caller
                                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                                {
                                    @Override
                                    public void onAnimationUpdate( ValueAnimator animation )
                                    {
                                        if ( view.getTag() == null )
                                        {
                                            view.setTag( 1 );
                                            gameEngine.animatorList.add( animation );
                                        }
                                    }
                                } );
                                view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        view.setTag( null );
                                        if ( getActivity() != null )
                                        {
                                            getActivity().onBackPressed();
                                        }
                                    }
                                } ).start();
                            }
                        }
                    } );
                    //
                    gameEngine.animatorList.add( objTime2 );
                    objTime2.start();
                    
                    
                    //#################################
                    //
                    // Display all tiles not solved but
                    // should have been
                    //
                    //#################################
                    int i     = 0;
                    int delay = 0;
                    
                    for ( int c = 0; c < boardTiles.size(); c++ )
                    {
                        BoardTile tile = boardTiles.get( c );
                        
                        if ( tile.specialTile > -1 && tile.getState() != BoardTile.STATE_FLIPPED )
                        {
                            tile.flipCoin( false );
                        }
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
            gameEngine.animatorList.add( objTime );
            objTime.start();
            
            
            //################################
            //
            // Do not allow any saved games
            //
            //################################
            if ( getContext() != null )
            {
                GameState.eraseSavedGame( getContext() );
            }
*/
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Receive communication from the grid
     * layout IE: score changes
     * <p>
     * //###################################
     *
     * @param gridFunction
     * @param gridData
     */
    @Override
    public void onGridUpdated( int gridFunction, int gridData )
    {
        // Update the hint counter
        loadCardHint();
        
        //###################################
        //
        // Update the score
        //
        //###################################
        if ( gridFunction == CardsGridLayout.SCORE_UPDATE )
        {
            // Update the score
            //            int currentScore = gameEngine.Scores[ gameEngine.loadedArea ];
            int currentScore = levelScore;
            
            // Update the score and make it final
            //            gameEngine.Scores[ gameEngine.loadedArea ] = (currentScore + gridData);
            levelScore = (currentScore + gridData);
            
            
            // Show the user the score changing
            if ( scoreAnimator != null && scoreAnimator.isRunning() )
            {   // Stop running, complete score change, and start a new one
                scoreAnimator.end();
            }
            
            // Remove so no duplicates
            if ( scoreAnimator != null )
            {
                gameEngine.animatorList.remove( scoreAnimator );
            }
            
            //
            // Animate the score numbers flipping
            //
            scoreAnimator = ValueAnimator.ofInt( currentScore, (currentScore + gridData) );
            scoreAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( getView() != null )
                    {
                        int      newScore = ( int ) animation.getAnimatedValue();
                        TextView score    = getView().findViewById( R.id.boardScoreText );
                        
                        // Update the score
                        score.setText( String.format( Locale.getDefault(), " %d ", newScore ) );
                    }
                }
            } );
            //
            scoreAnimator.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    if ( getView() != null )
                    {
                        TextView score = getView().findViewById( R.id.boardScoreText );
                        
                        // Update the score
                        score.setText( String.format( Locale.getDefault(), " %d ", levelScore ) );
                    }
                }
            } );
            //
            scoreAnimator.setDuration( 1000 ).setInterpolator( new LinearInterpolator() );
            scoreAnimator.start();
            gameEngine.animatorList.add( scoreAnimator );
            
            
            //##############################
            // Animate the text
            //##############################
            if ( gridData > 1 )
            {
/*
                final GradientTextView text = view_main.findViewById( R.id.pointsUpdate );
                // Move to the new location
                //                text.setText( String.format( Locale.getDefault(), "Points x %d", gridData ) );
                text.setText( String.format( Locale.getDefault(), "%d Points", gridData ) );
                text.setVisibility( View.VISIBLE );
                //
                
                // Remove so no duplicates
                if ( pointsAnimator != null )
                {
                    gameEngine.animatorList.remove( pointsAnimator );
                }
                //
                pointsAnimator = ObjectAnimator.ofFloat( text, "TranslationY", -text.getTextSize() );
                pointsAnimator.setDuration( 750 ).setInterpolator( new LinearInterpolator() );
                pointsAnimator.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        text.setVisibility( View.GONE );
                        text.setTranslationY( 0 );
                    }
                } );
                //
                pointsAnimator.start();
                gameEngine.animatorList.add( pointsAnimator );
*/
            }
        }
        else if ( gridFunction == CardsGridLayout.MOVES_UPDATE )
        {   // Report the current move count
            if ( getView() != null )
            {
                TextView moves = getView().findViewById( R.id.boardMovesText );
                
                gameEngine.boardMoves = gridData;
                moves.setText( String.format( Locale.getDefault(), "%3d", gridData ) );
                
                if ( gameEngine.boardMoves < 6 )
                {
                    moves.setScaleY( 1.3f );
                    moves.setScaleX( 1.3f );
                    moves.animate().scaleY( 1f ).scaleX( 1f ).setInterpolator( new CustomBounceInterpolator( .2f, 20 ) );
                    moves.animate().start();
                }
            }
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Method used to build a set and attach
     * it to the parent
     * <p>
     * //###################################
     *
     * @param target
     * @param parent
     */
    public static void makeConnection( View target, View parent, float verticalBias, float horizontalBias )
    {
        ConstraintLayout constraintLayout = ( ConstraintLayout ) parent;
        ConstraintSet    constraintSet    = new ConstraintSet();
        
        // Process
        constraintLayout.addView( target );
        constraintSet.clone( constraintLayout );
        constraintSet.connect( target.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP );
        constraintSet.connect( target.getId(), ConstraintSet.BOTTOM, constraintLayout.getId(), ConstraintSet.BOTTOM );
        constraintSet.connect( target.getId(), ConstraintSet.END, constraintLayout.getId(), ConstraintSet.END );
        constraintSet.connect( target.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START );
        
        // Bias
        if ( verticalBias != -1 )
        {
            constraintSet.setVerticalBias( target.getId(), verticalBias );
        }
        if ( verticalBias != -1 )
        {
            constraintSet.setHorizontalBias( target.getId(), horizontalBias );
        }
        
        constraintSet.applyTo( constraintLayout );
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle callback / result from
     * "Tow Word" dialog call
     * <p>
     * //###################################
     *
     * @param code N/A
     */
/*
    @Override
    public void onTwoWordResult( int code )
    {
        if ( code == TwoWordSlider.GAME_START && getView() != null )
        {
            try
            {
                SecureRandom r = new SecureRandom();
                Animation    anim;
                int[] anims_list = {
                        R.anim.spring_slide_in_from_bottom, R.anim.spring_slide_in_from_top, R.anim.spring_slide_in_from_left, R.anim.spring_slide_in_from_right
                };
                
                
                //################################
                //
                // After Drawing the symbols, animate
                // it all onto the screen.
                //
                //################################
                final CardsGridLayout cardsGridLayout = getView().findViewById( R.id.boardGridLayout );
                ImageView             background      = getView().findViewById( R.id.boardBackground );
                int                   index           = r.nextInt( 4 ) + 1;
                int                   c               = 0;
                final boolean         gameResumed;
                final View            view            = view_main;//.findViewById( R.id.boardGridSlots );
                
                
                for ( int x = 0; x < index; x++ )
                {
                    c = r.nextInt( anims_list.length );
                }
                
                
                // Load up the game tile data
                //            boardTiles = gameBoard.loadBoardTileMap( gameEngine.currentBoardImage );
                // Display the initial symbols
                if ( getContext() != null )
                {
                    gameBoard.BuildCardsBoard( getContext(), gameEngine.currentBoardImage, boardTiles, getView() );
                    cardHintCount = 0;
                    
                    // If the targets are a lot, give one more hint
                    if ( gameBoard.maxTargets > 8 )
                    {
                        cardHintCount = 3;
                    }
                    else if ( gameBoard.maxTargets > 5 )
                    {
                        cardHintCount = 2;
                    }
                    else if ( gameBoard.maxTargets > 2 )
                    {
                        cardHintCount = 1;
                    }
                    
                    // Safety valve
                    cardHintCount -= (GameEngine.currentLevel / 16);
                    if ( cardHintCount < 0 )
                    {
                        cardHintCount = 0;
                    }
                    
                    loadCardHint();
                }
                
                
                //#########################################
                //
                // Any save data that jas been resumed gets
                // loaded here
                //
                //#########################################
                if ( gameEngine.resumeGame )
                {
                    gameResumed = true;
                    gameEngine.resumeGame = false;
                    GameState.loadGameState( getContext() );
                    
                    // Set the Background image
                    background.setImageResource( gameBoard.xmlBackgrounds[ gameEngine.currentBackground ] );
                    gameEngine.timeAllowed = CustomTimer.currentTime;
                    cardsGridLayout.onDrawGrid();
                    
                    // Reflect resume status
                    setGameData( view_main );
                }
                else
                {
                    gameResumed = false;
                }
                
                
                //##########################
                // DEBUG
                //##########################
                if ( GameEngine.debugMode )
                {
                    // TESTING
                    gameEngine.boardMoves = 6;
                }
                
                
                // Board moves
                TextView moves = view_main.findViewById( R.id.boardMovesText );
                moves.setText( String.format( Locale.getDefault(), "%3d", gameEngine.boardMoves ) );
                
                // Show the correct Hint count
                loadCardHint();
                //
                //                connectionsGridLayout.setBackgroundResource( gameBoard.xmlMapBgs[ gameEngine.currentBoardImage ] );
                //                cardsGridLayout.setBackgroundColor( 0x7F000000 );
                cardsGridLayout.setVisibility( View.VISIBLE );
                cardsGridLayout.setScaleY( 1f );
                cardsGridLayout.setScaleX( 1f );
                
                //
                anim = AnimationUtils.loadAnimation( getContext(), anims_list[ c ] );
                //                anim.setDuration( AnimationValues.SPRING_SLIDE_TIME );
                anim.setDuration( AnimationValues.SPRING_SLIDE_TIME );
                anim.setInterpolator( new AccelerateDecelerateInterpolator() );
                anim.setStartOffset( 500 );
                anim.setAnimationListener( new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart( Animation animation )
                    {
                        createBoardImage( view, R.drawable.black_bg_tile );
                        // Hide the dimmed tiles
                        view.setVisibility( View.INVISIBLE );
                    }
                    
                    @Override
                    public void onAnimationEnd( Animation animation )
                    {
                        int level = GameEngine.currentLevel;
                        
                        //################################
                        //
                        // Objectives System
                        //
                        //################################
                        if ( !gameResumed )
                        {
                            ObjectivesDialog dialog;
                            
                            //##############################
                            //
                            //
                            //
                            //##############################
                            dialog = new ObjectivesDialog( getContext(), gameBoard.objMessage, GameBoard.targetViewsList, 0 );
                            dialog.setOnDismissListener( new DialogInterface.OnDismissListener()
                            {
                                @Override
                                public void onDismiss( DialogInterface dialog )
                                {
                                    startTheGame();
                                }
                            } );
                            //
                            
                            // Do not allow cancelable closing
                            dialog.setCancelable( false );
                            dialog.show();
                        }
                        else
                        {
                            startTheGame();
                        }
                        
                        // Show the dimmed tiles
                        view.setVisibility( View.VISIBLE );
                    }
                    
                    @Override
                    public void onAnimationRepeat( Animation animation )
                    {
                    
                    }
                } );
                
                //
                cardsGridLayout.startAnimation( anim );
                
                // Reset the moves for the game
                cardsGridLayout.setBoardMoves( gameEngine.boardMoves );
                //
                cardsGridLayout.setFlippedTiles( 0 );
                
                //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.LOOP );
                }
            }
            catch ( NullPointerException npe )
            {
                npe.getMessage();
            }
        }
    }
*/
    
    
    /**
     * //###############################
     * <p>
     * Load the game after the
     * "Two Word" animation
     * <p>
     * //###############################
     */
    private void startTheGame()
    {
        final CardsGridLayout cardsGridLayout = view_main.findViewById( R.id.boardGridLayout );
        
        if ( GameEngine.timer != null )
        {
            GameEngine.timer.setUserCancel( CustomTimer.USER_CANCEL );
            GameEngine.timer.cancel();
            GameEngine.timer = null;
        }
        
        //
        GameEngine.reloadSameGame = false;
        
        // This gets modified by the help system. restore it!
        CardsGridLayout.gMapHeight = gameBoard.getMapHeight();
        CardsGridLayout.gMapWidth = gameBoard.getMapWidth();
        
        
        //##################################
        //
        // Set Target Text to include
        // "Penalty" depending on level
        //
        //##################################
        if ( getContext() != null )
        {
            final GradientTextView target = view_main.findViewById( R.id.targetText );
            
            target.setText( getContext().getString( R.string.targets_text ) );
            target.setClickable( false );
            target.setOnClickListener( null );
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Handle callback / result from
     * "Paused Game" dialog call
     * <p>
     * //###################################
     *
     * @param code
     */
    @Override
    public void onPausedGameResult( int code )
    {
        if ( getView() == null )
        {
            Toast.makeText( getContext(), "Error processing pause feature.", Toast.LENGTH_SHORT ).show();
            return;
        }
        
        
        //##########################
        //
        // Exit the current game
        //
        //##########################
        if ( code == GamePaused.MENU_REQUEST || code == GamePaused.QUIT_REQUEST )
        {
            View view = getView();
            
            // Kill current song
            if ( gameEngine.soundPlayer != null )
            {
                //@@@@@@@@@@@@@@@@@@@
                gameEngine.soundPlayer.fadeOutMusic().addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        GameEngine.gamePaused = false;
                        
                        // NEED TO SAVE CURRENT TIME SOMEWHERE
                        final long tempTime = CustomTimer.currentTime;
                        
                        killGameStatus( CustomTimer.USER_CANCEL );
                        
                        // Transition back to caller
                        view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate( ValueAnimator animation )
                            {
                                if ( view.getTag() == null )
                                {
                                    view.setTag( 1 );
                                    gameEngine.animatorList.add( animation );
                                }
                            }
                        } );
                        view.animate().alpha( 0f ).setDuration( 750 + AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                view.setTag( null );
                                
                                if ( getActivity() != null )
                                {
                                    if ( code == GamePaused.MENU_REQUEST )
                                    {
                                        getActivity().onBackPressed();
                                        GameEngine.reloadSameGame = true;
                                        
                                        if ( gameEngine.soundPlayer != null )
                                        {
                                            //@@@@@@@@@@@@@@@@@@@
                                            gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                                            gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
                                        }
                                        
                                        // Memory leak??
                                        view_main = null;
                                    }
                                    else
                                    {
                                        // Create a save state
                                        GameEngine.reloadSameGame = false;
                                        CustomTimer.currentTime = tempTime;
                                        GameState.saveGameState( getContext(), 1, view_main );
                                        MainActivity.removeBackStack();
                                        
                                        // Memory leak??
                                        view_main = null;
                                    }
                                }
                            }
                        } ).start();
                    }
                } );
            }
            else
            {
                GameEngine.gamePaused = false;
                
                // NEED TO SAVE CURRENT TIME SOMEWHERE
                final long tempTime = CustomTimer.currentTime;
                
                killGameStatus( CustomTimer.USER_CANCEL );
                
                // Transition back to caller
                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view.getTag() == null )
                        {
                            view.setTag( 1 );
                            gameEngine.animatorList.add( animation );
                        }
                    }
                } );
                view.animate().alpha( 0f ).setDuration( 750 + AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        view.setTag( null );
                        
                        if ( gameEngine.soundPlayer != null )
                        {
                            //@@@@@@@@@@@@@@@@@@@
                            gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                            gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
                        }
                        
                        if ( getActivity() != null )
                        {
                            if ( code == GamePaused.MENU_REQUEST )
                            {
                                getActivity().onBackPressed();
                                
                                if ( gameEngine.soundPlayer != null )
                                {
                                    //@@@@@@@@@@@@@@@@@@@
                                    gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                                    gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
                                }
                                
                                // Memory leak??
                                view_main = null;
                            }
                            else
                            {
                                // Create a save state
                                CustomTimer.currentTime = tempTime;
                                
                                GameState.saveGameState( getContext(), 1, view_main );
                                MainActivity.removeBackStack();
                                
                                // Memory leak??
                                view_main = null;
                            }
                        }
                    }
                } ).start();
            }
        }
        else
        {
            View view = getView();
            
            view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( view.getTag() == null )
                    {
                        view.setTag( 1 );
                        gameEngine.animatorList.add( animation );
                    }
                }
            } );
            view.animate().setDuration( 100 ).alpha( 1f ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {   // Must restart the timer
                    view.setTag( null );
                    
                    resumeGameStatus();
                    GameEngine.gamePaused = false;
                    
                    // Re-enable Pause clicking
                    GradientTextView image = view.findViewById( R.id.pauseButton );
                    image.setClickable( true );
                }
            } ).start();
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * Clicker for buttons and Selectors
     * <p>
     * //###################################
     *
     * @param v N/A
     */
    @Override
    public void onClick( final View v )
    {
        int             id   = v.getId();
        SecureRandom    r    = new SecureRandom();
        CardsGridLayout grid = view_main.findViewById( R.id.boardGridLayout );
        
        // cut out double dialog displays
        if ( ErrorDialog.isShowing || (GameEngine.timer != null && CustomTimer.currentTime < 2000) )
        {
            return;
        }
        
        // If the level is complete, accept NO clicks!
        if ( grid != null && grid.isLevelCompleted() )
        {
            return;
        }
        
        if ( id == R.id.pauseButton )
        {
            //@@@@@@@@@@@@@@@@ Button Click
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
            }
            
            // Pause everything!
            pauseGameStatus();
            
            // Disable clicking on the same button that called THIS
            v.setClickable( false );
            v.setScaleX( .5f );
            v.setScaleY( .5f );
            v.animate().scaleX( 1f ).scaleY( 1f ).setDuration( AnimationValues.DROP_TIME );
            v.animate().setInterpolator( new BounceInterpolator() );
            v.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    final GamePaused dialog = new GamePaused();
                    dialog.setCancelable( false );
                    
                    if ( getView() != null )
                    {
                        View view = getView();
                        
                        view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate( ValueAnimator animation )
                            {
                                if ( view.getTag() == null )
                                {
                                    view.setTag( 1 );
                                    gameEngine.animatorList.add( animation );
                                }
                            }
                        } );
                        view.animate().alpha( 0.50f ).setDuration( 100 ).withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                view.setTag( null );
                                
                                // This is the requestCode that you are sending.
                                dialog.setTargetFragment( CardsFragment.this, 1 );
                                
                                //@@@@@@@@@@@@@@@@ Button Click
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.playBgSfx( PlaySound.PAUSE_GAME, PlaySound.PLAY );
                                }
                                
                                // This is the tag, "dialog" being sent.
                                if ( getActivity() != null )
                                {
                                    dialog.show( getActivity().getSupportFragmentManager(), "Pause" );
                                }
                            }
                        } ).start();
                    }
                }
            } ).start();
        }
        else if ( id == R.id.cardsHintFrame )
        {
            //###########################################
            //
            //
            //
            //###########################################
            if ( cardHintCount > 0 && (grid != null && grid.getFlippedTiles() == 0) )
            {
                ObjectAnimator       button;
                PropertyValuesHolder sx = PropertyValuesHolder.ofFloat( "scaleX", 1, 0.5f, 1 );
                PropertyValuesHolder sy = PropertyValuesHolder.ofFloat( "scaleY", 1, 0.5f, 1 );
                
                button = ObjectAnimator.ofPropertyValuesHolder( v, sx, sy );
                button.setDuration( 350 ).setInterpolator( new BounceInterpolator() );
                button.addListener( new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart( Animator animation )
                    {
                        //@@@@@@@@@@@@@@@@ Button Click
                        if ( gameEngine.soundPlayer != null )
                        {
                            gameEngine.soundPlayer.play( PlaySound.BUTTON_CLICK );
                        }
                    }
                    
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        int             i;
                        int             match   = -1;
                        boolean         invalid = true;
                        BoardTile[]     tile    = new BoardTile[ 2 ];
                        CardsGridLayout grid    = view_main.findViewById( R.id.boardGridLayout );
                        
                        //#############################
                        //
                        // Solve a match for the player
                        //
                        //#############################
                        for ( int c = 0; c < 2; c++ )
                        {
                            // Start at a random spot
                            i = r.nextInt( boardTiles.size() );
                            
                            for ( int s = 0; s < boardTiles.size(); s++ )
                            {
                                if ( match == -1 )
                                {
                                    tile[ c ] = boardTiles.get( (s + i) % boardTiles.size() );
                                }
                                else
                                {
                                    tile[ c ] = boardTiles.get( s );
                                }
                                
                                //
                                if ( c == 1 && tile[ 0 ].getPosition() == tile[ 1 ].getPosition() )
                                {
                                    continue;
                                }
                                
                                //##################################
                                //
                                //
                                //
                                //##################################
                                if ( match != -1 )
                                {
                                    if ( tile[ c ].getPosition() != tile[ 0 ].getPosition() && tile[ c ].card[ 1 ] == tile[ 0 ].card[ 1 ] )
                                    {
                                        match = -2;
                                        break;
                                    }
                                }
                                else if ( tile[ c ].getState() == BoardTile.STATE_ACTIVE && tile[ c ].card[ 1 ] != R.drawable.card_emoji_sad && tile[ c ].card[ 1 ] != R.drawable.card_hint_glass_gold && tile[ c ].card[ 1 ] != R.drawable.card_free_moves_gold )
                                {
                                    // Need to find this
                                    match = tile[ c ].card[ 1 ];
                                    break;
                                }
                            }
                        }
                        
                        // Only if we have a good card
                        //                        if ( (tile[ 0 ].getState() == BoardTile.STATE_ACTIVE && tile[ 0 ].specialTile == 1) && (tile[ 1 ].getState() == BoardTile.STATE_ACTIVE && tile[ 1 ].specialTile == 1) )
                        if ( match == -2 )
                        {
                            invalid = false;
                        }
                        else
                        {
                            v.setClickable( true );
                        }
                        
                        
                        //############################
                        //
                        // Emulate a match
                        //
                        //############################
                        if ( !invalid )
                        {
                            grid.setFlippedTiles( 2 );
                            //
                            tile[ 0 ].setState( BoardTile.FLIP_CARD );
                            tile[ 1 ].setState( BoardTile.FLIP_CARD );
                            //
                            tile[ 0 ].flipCoin( false );
                            tile[ 1 ].flipCoin( false ).addListener( new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    super.onAnimationEnd( animation );
                                    
                                    ValueAnimator delay = ValueAnimator.ofInt( 0, 5 ).setDuration( 350 );
                                    delay.addListener( new AnimatorListenerAdapter()
                                    {
                                        @Override
                                        public void onAnimationEnd( Animator animation )
                                        {
                                            super.onAnimationEnd( animation );
                                            tile[ 0 ].setState( BoardTile.STATE_MATCHED );
                                            tile[ 1 ].setState( BoardTile.STATE_MATCHED );
                                            
                                            tile[ 0 ].setSpecialItem( 0 );
                                            tile[ 1 ].setSpecialItem( 0 );
                                            
                                            //
                                            logicThread.addToStack( LogicThread.CMD_PROCESS_MATCHES );
                                            cardHintCount--;
                                            GameEngine.boardMoves--;
                                            loadCardHint();
                                            
                                            //@@@@@@@@@@@@@@@@
                                            if ( gameEngine.soundPlayer != null )
                                            {
                                                gameEngine.soundPlayer.play( PlaySound.SLOTS_PRIZE_ADD );
                                            }
                                            
                                            //
                                            v.setClickable( true );
                                        }
                                    } );
                                    
                                    delay.start();
                                }
                            } );
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
                button.start();
                
                // Disable clicking on the same button that called THIS
                v.setClickable( false );
            }
            else
            {
                //@@@@@@@@@@@@@@@@ Button Click
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.INVALID );
                }
                
            }
        }
        
    }
    
    
    /**
     * //###################################
     * <p>
     * Help stop memory leaks
     * <p>
     * //###################################
     */
    @Override
    public void onDestroy()
    {
        if ( loadBmp != null )
        {
            loadBmp.recycle();
            loadBmp = null;
        }
        
        gameEngine = null;
        gameBoard = null;
        // Kill the thread
        if ( logicThread != null )
        {
            logicThread.killThread();
            logicThread = null;
        }
        
        //
        boardTiles = null;
        pointsAnimator = null;
        scoreAnimator = null;
        //
        if ( adView != null )
        {
            adView.destroy();
        }
        
        adView = null;
        // Inflate the layout for this fragment
        view_main = null;
        
        super.onDestroy();
    }
}