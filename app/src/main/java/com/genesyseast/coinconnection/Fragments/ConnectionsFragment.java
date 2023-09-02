package com.genesyseast.coinconnection.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.CustomControls.HighLighter;
import com.genesyseast.coinconnection.CustomControls.ImageEffect;
import com.genesyseast.coinconnection.CustomControls.OverlayView;
import com.genesyseast.coinconnection.CustomControls.StarsEarnedBar;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.Dialogs.GamePaused;
import com.genesyseast.coinconnection.Dialogs.LevelResults;
import com.genesyseast.coinconnection.Dialogs.ObjectivesDialog;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.CustomControls.GridLayoutView;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.ShowCase.ShowClass;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.BlurEffect;
import com.genesyseast.coinconnection.Support.GameState;
import com.google.android.gms.ads.AdListener;


import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static com.genesyseast.coinconnection.GameEngine.GameEngine.mInterstitialAd;


public class ConnectionsFragment
        extends Fragment
        implements View.OnClickListener, GamePaused.OnResult, CustomTimer.OnTimerCommunicateListener, ConnectionsGridLayout.OnGridUpdateListener, LevelResults.OnLevelUpListener,
                   ConnectionsGridLayout.OnBoosterListener
{
    
    /**
     * Game's count down time
     */
    //    public static         CustomTimer          timer;
    //
    private        GameEngine           gameEngine;
    private        GameBoard            gameBoard;
    private        LogicThread          logicThread;
    // Specific for the praising text. No duplicate splashes
    private        int                  connections  = -1;
    //
    public static  ArrayList<BoardTile> boardTiles;
    private        ValueAnimator        pointsAnimator;
    private        ValueAnimator        praiseAnimator;
    private        ValueAnimator        scoreAnimator;
    //
    // Inflate the layout for this fragment
    private        Bitmap               loadBmp      = null;
    public static  View                 view_main;
    private        int                  targetScore  = 0;
    public static  int                  levelScore   = 0;
    private static boolean              PENALTY_MODE = false;
    private        int                  setBooster   = -1;
    private        StarsEarnedBar       bar;
    private        FrameLayout          boosterView;
    
    // This will be applied to each menu layout for the menu navagation
    public ConnectionsFragment()
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
        view_main = inflater.inflate( R.layout.game_connections_fragment, container, false );
        
        //
        boardTiles = new ArrayList<>();
        
        //
        // Get everything rolling!
        //
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
                        //
                        // Player attempting to set a booster?
                        //
                        if ( setBooster > -1 )
                        {
                            ConnectionsGridLayout grid = view_main.findViewById( R.id.boardGridLayout );
                            
                            setBooster = -1;
                            // Erase setting booster state
                            grid.setBoosterListener( null, -1 );
                            HighLighter h = view_main.findViewById( R.id.highLighter );
                            h.endHighlight();
                            //
                            loadSpecialItemData();
                        }
                        else
                        {
                            GradientTextView imageButton = getView().findViewById( R.id.pauseButton );
                            
                            if ( imageButton.isClickable() )
                            {
                                imageButton.callOnClick();
                            }
                        }
                    }
                    
                    return true;
                }
                
                return false;
            }
        } );
        
        //
        connections = 0;
        
        return view_main;
    }
    
    
    @Override
    public void onPause()
    {
        super.onPause();
        setBooster = -1;
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
        
        if ( GameEngine.timer != null )
        {
            if ( GameEngine.timer.getCommunicator() == null )
            {
                // Listener fails upon return from app leave. so if player fails a level, game will crash
                GameEngine.timer.setOnCommunicator( this );
            }
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
            gameEngine.animatorList.remove( GameEngine.timer );
            //            timer.removeAllListeners();
            GameEngine.timer.setUserCancel( killStatus );
            GameEngine.timer.cancel();
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
        
        if ( praiseAnimator != null )
        {
            praiseAnimator.removeAllListeners();
            praiseAnimator.cancel();
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
        
        //
        GameEngine.isKilled = true;
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
            ConnectionsGridLayout grid = getView().findViewById( R.id.boardGridLayout );
            
            // Resume it all
            GameEngine.gamePaused = false;
            
            // ALWAYS create a new instance
            if ( logicThread == null )
            {
                logicThread = new LogicThread( getActivity() );
                logicThread.setGridLayout( grid );
                //                logicThread.restoreLogic();
            }
            
            //
            logicThread.restoreLogic();
            
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
        }
    }
    
    
    /**
     * //###################################
     * <p>
     * This triggered means no more matches
     * available
     * <p>
     * //###################################
     */
    @Override
    public void onNoMoreMatches()
    {
        if ( getView() != null )
        {
            final ConnectionsGridLayout grid   = getView().findViewById( R.id.boardGridLayout );
            final ErrorDialog           dialog = new ErrorDialog();
            
            
            //@@@@@@@@@@@@@@@@@@@@@ Play game start sound
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.ERROR );
                //                gameEngine.sndPlayer.soundPool.unload( sound );
            }
            
            //
            // Show coin count, and how much each move cost
            // per coin. Give option to purchase moves / coins
            // Or watch an Ad
            //
            // Shuffle the board
            grid.useShuffler();
            //
            grid.noMatchesTrigger = false;
            
/*
            dialog.setTitle( "No moves left..." );
            dialog.setMessage( "Uh oh! There are no more moves. Would you like to watch an Ad to re-shuffle the game board?" );
            dialog.setYesText( "Sure" );
            dialog.setNoText( "I'll pass" );
            //
            dialog.setTimer( GameEngine.timer );
            dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON );
            dialog.setAlertIcon( android.R.drawable.ic_dialog_alert );
            dialog.setFormImage( R.drawable.neutral_face_emoji );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( int buttonClicked )
                {
                    if ( buttonClicked == ErrorDialog.NO_BUTTON )
                    {
                        // Player gave up
                        final View view = getView();
                        GameEngine.gamePaused = false;
                        killGameStatus( CustomTimer.NORMAL_CANCEL );
                        
                        // Exit dialog
                        dialog.setFormImage( R.drawable.sad_face_emoji );
                        dialog.animateShake( false );
                        dialog.setOnShakeAnimationListener( new ErrorDialog.OnAnimationListener()
                        {
                            @Override
                            public void onAnimationEnd()
                            {
                                dialog.dismiss();
                                
                                // Transition back to caller
                                if ( view != null && getActivity() != null )
                                {
                                    view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            getActivity().onBackPressed();
                                        }
                                    } ).start();
                                }
                            }
                        } );
                    }
                    else if ( buttonClicked == ErrorDialog.CLOSE_BUTTON )
                    {
                        // If the user has to click this, they
                        // watched the whole video but had to click X
                        if ( GameEngine.rewardGiven )
                        {
                            Toast.makeText( getContext(), "Error occurred. Reward given", Toast.LENGTH_SHORT ).show();
                        }
                        
                        // Shuffle the board
                        grid.useShuffler( null );
                        
                        //
                        GameEngine.rewardGiven = false;
                        //
                        grid.noMatchesTrigger = false;
                        dialog.dismiss();
                    }
                    else
                    {
                        // Exit dialog
                        dialog.setFormImage( R.drawable.happy_face_emoji );
                        dialog.animateShake( true );
                        dialog.setOnShakeAnimationListener( new ErrorDialog.OnAnimationListener()
                        {
                            @Override
                            public void onAnimationEnd()
                            {
                                // Set a close button in case of errors
                                dialog.setButtons( ErrorDialog.CLOSE_BUTTON );
                                
                                // Show the video
                                if ( GameEngine.mRewardedVideoAd.isLoaded() )
                                {
                                    gameEngine.setOnAdReturnListener( new GameEngine.OnAdReturnListener()
                                    {
                                        @Override
                                        public void onAdReturn( int status, int errorFlag )
                                        {
                                            if ( status == GameEngine.AD_REWARDED )
                                            {
                                                GameEngine.rewardGiven = false;
                                            }
                                            else if ( status == GameEngine.AD_CLOSED )
                                            {
                                                if ( GameEngine.rewardGiven )
                                                {
                                                    // Shuffle the board
                                                    grid.useShuffler( null );
                                                }
                                                
                                                //
                                                GameEngine.rewardGiven = false;
                                                //
                                                grid.noMatchesTrigger = false;
                                            }
                                        }
                                    } );
                                    //
                                    GameEngine.mRewardedVideoAd.show();
                                    GameEngine.savedDialog = dialog;
                                }
                                else
                                {
                                    // Exit dialog
                                    dialog.dismiss();
                                    
                                    // Shuffle the board
                                    grid.useShuffler( null );
                                    //
                                    grid.noMatchesTrigger = false;
                                }
                            }
                        } );
                    }
                }
            } );
            
            //
            dialog.setCancelable( false );
            if ( getActivity() != null )
            {
                dialog.show( getActivity().getSupportFragmentManager(), "Error" );
            }
*/
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
        final ImageEffect           background;
        final ConnectionsGridLayout connectionsGridLayout;
        Random                      r = new Random();
        
        // Pause Button
        GradientTextView pauseButton = view_main.findViewById( R.id.pauseButton );
        pauseButton.setOnClickListener( this );
        
        // Init Game Local data: Special Item stuff, Start time, Update score and moves
        levelScore = 0;
        loadSpecialItemData();
        setGameData( view_main );
        gameEngine.specialRunning = false;
        connections = 0;
        
        
        //############################################
        //
        // Do not select the same board just played
        //
        //############################################
        if ( getContext() != null && !GameEngine.reloadSameGame )
        {
            GameEngine.currentBoardImage = GameBoard.getBoardImage( getContext(), GameEngine.currentBoardImage, true );
        }
        
        
        // TODO: Change on release GameBoard
        // DEBUG PURPOSES
        //GameEngine.currentBoardImage = 0;
        
        
        //################################################
        //
        // CONSIDER ADDING ANIMATIONS TO BG
        //      IE: Fish for water bg, snow for ice bg
        //          Add Blur effect
        //
        //################################################
        background = view_main.findViewById( R.id.boardBackground );
        
        if ( !GameEngine.reloadSameGame )
        {
            // Start it off!
            GameEngine.currentBackground = r.nextInt( gameBoard.xmlBackgrounds.length );
        }
        
        
        //#########################################
        //
        // Any save data that jas been resumed gets
        // loaded here
        //
        //#########################################
        bar = view_main.findViewById( R.id.starsToEarn );
        
        
        // TODO: This data should by handled by CC editor
        //  And all loaded from XML or JSON
        if ( GameEngine.resumeGame )
        {
            GameState.loadGameState( getContext() );
            
            // Reflect the correct information
            setGameData( view_main );
            
            bar.setMax( 5000 );
            bar.setProgress( 0 );
            bar.setStarsPosi( "300,2000,4000" );
        }
        else
        {
            // This will be where the game data is
            // Loaded form the new XML format file
            bar.setMax( 5000 );
            bar.setProgress( 0 );
            bar.setStarsPosi( "300,2000,4000" );
        }
        
        
        // Music
        gameEngine.currentBGMusic = GameEngine.currentBackground;
        
        // Set the Background image
        background.setImageResource( gameBoard.xmlBackgrounds[ GameEngine.currentBackground ] );
        
        
        // Get the GridView ready
        connectionsGridLayout = view_main.findViewById( R.id.boardGridLayout );
        connectionsGridLayout.setBackgroundResource( 0 );
        connectionsGridLayout.canSolveMatches = true;
        connectionsGridLayout.noMatchesTrigger = false;
        
        // Set grid update listener
        connectionsGridLayout.setOnGridUpdateListener( this );
        connectionsGridLayout.setLevelCompleted( false );
        
        
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
        connectionsGridLayout.setLogicThread( logicThread );
        logicThread.setGridLayout( connectionsGridLayout );
        
        //
        GameEngine.isKilled = false;
        
        
        //#########################################
        //
        // Clear Board moves
        //
        //#########################################
        TextView moves = view_main.findViewById( R.id.boardMovesText );
        moves.setText( String.format( Locale.getDefault(), "%d", 0 ) );
        moves = view_main.findViewById( R.id.boardMovesLabel );
        moves.setText( R.string.moves_text );
        
        
        //#########################################
        //
        // Get this game going!
        //
        //#########################################
        startTheGame();
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
        final LevelResults dialog        = new LevelResults();
        Bundle             args          = new Bundle();
        int                resultCode    = -1;
        int                starsOn       = bar.getStarsOn();
        long               bonusTimeLeft = 0;
        int                dummyLevel    = GameEngine.currentLevel;
        
        // Get the amount of stars met
        GameEngine.setStarCountFromLevel( GameEngine.currentLevel, starsOn );
        
        // Increase star level, and add the stars
        GameEngine.currentLevel++;
        
        
        // Return to normal level if reloaded an old level
        if ( GameEngine.reloadedLevel > -1 )
        {
            GameEngine.currentLevel = GameEngine.reloadedLevel;
            GameEngine.reloadedLevel = -1;
        }
        
        
        //TODO Timer not enabled
        // Kill the timer if active
        if ( GameEngine.timer != null )
        {
            bonusTimeLeft = CustomTimer.currentTime;
            GameEngine.timer.setUserCancel( CustomTimer.USER_CANCEL );
            GameEngine.timer.cancel();
        }
        
        // No more resuming previous game
        GameState.eraseSavedGame( getContext() );
        
        // Save current game data
        gameEngine.savePrefData();
        
        // Standard completion
        resultCode = 0;
        
        
        // Kill BG Music
        //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
        if ( gameEngine.soundPlayer != null )
        {
            //            gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.STOP );
            gameEngine.soundPlayer.fadeOutMusic();
        }
        
        
        //################################
        //
        // Determine is stars earned
        // celebration happens
        //
        //################################
        args.putString( "level_msg", String.format( Locale.getDefault(), "Level %02d completed!\nNice skills", dummyLevel + 1 ) );
        
        if ( starsOn > 0 )
        {
            args.putBoolean( "new_star", true );
            resultCode |= 2;
        }
        
        // Standard level message
        args.putInt( "time_left", ( int ) bonusTimeLeft );
        args.putInt( "star_count", starsOn );
        args.putInt( "level_score", levelScore );
        args.putInt( "connections", connections );
        
        
        dialog.setCancelable( true );
        dialog.setArguments( args );
        dialog.setTargetFragment( ConnectionsFragment.this, resultCode );
        
        //################################
        //
        // Dim the screen and show the
        // dialog
        //
        //################################
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
        //        final SecureRandom r = new SecureRandom();
        
        exitToSelectScreen();
        
        // Runs after the player clicks out of the level up dialog
        // And the player has completed an area
        if ( code == 1 || code == 3 )
        {
/*
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
*/
/*
                                FragmentManager fm    = getActivity().getSupportFragmentManager();
                                int             count = fm.getBackStackEntryCount();
                                
                                for ( int i = 0; i < count; ++i )
                                {
                                    fm.popBackStackImmediate();
                                }
*//*

                            
                            view.setTag( null );
                            FragmentLoader.SwapFragment( getActivity(), getContext(), new GameCompleted(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        }
                    }
                } ).start();
            }
*/
        }
        //############################################################
        // Runs after the player clicks out of the level up dialog
        // And a star was earned. This gives the player 3 free spins
        // on the slot machine or 1 try on chests!
        //############################################################
        else if ( code == 2 )
        {
/*
            View view = getView();
            
            // On to the next level
            // Show Video Ad if ready
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
                        exitToSelectScreen();
                    }
                } ).start();
            }
*/
        }
        else
        {
/*
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
*/
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Universal exit call
     * <p>
     * //###########################
     */
    public void exitToSelectScreen()
    {
        //
        // Show mandatory Ad if it's ready
        // Only show after 24 levels completed, and every 6th
        // level played there after
        //
        // TODO: Remove debug when more users on full release
        if ( !GameEngine.noMoreAds && mInterstitialAd.isLoaded() && GameEngine.currentLevel > 24 && (GameEngine.currentLevel % 6) == 0 /* && GameEngine.debugMode*/ )
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
    
    
    /**
     * //####################################
     * <p>
     * Decide which game should be played
     * <p>
     * //####################################
     */
    public void transitionToNextLevel()
    {
        //#############################
        //
        // Kill BG Music
        //
        //#############################
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.STOP );
        }
        
        //
        // Fade transition to next level
        //
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

/*
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
*/
                
                if ( (GameEngine.currentLevel % GameEngine.MAX_LEVEL_PER_SELECTOR) == 0 && getActivity() != null )
                {
                    FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                    
                    // Return to the level selector for
                    // bonuses, prizes, etc
                    //                    getActivity().onBackPressed();
                }
/*
                else if ( (GameEngine.currentLevel % GameEngine.CARD_GAME_LEVEL) == 0 && getActivity() != null )
                {
                    getActivity().onBackPressed();
                    FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                }
*/
                else
                {
                    // Load the next level area
                    loadLevelData();
                }
            }
        } ).start();
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
            numText.setText( String.format( Locale.getDefault(), " %d ", GameEngine.currentLevel + 1 ) );
            
            numText = view.findViewById( R.id.boardScoreText );
            numText.setText( String.format( Locale.getDefault(), " %d ", levelScore ) );
            
            numText = view.findViewById( R.id.boardMovesText );
            numText.setText( String.format( Locale.getDefault(), "%d", GameEngine.boardMoves ) );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Create the BG dimmed images
     * <p>
     * //###############################
     *
     * @param view
     * @param bgTileResId
     *
     * @return
     */
    public void createBoardImage( ConnectionsGridLayout view, int bgTileResId )
    {
        view.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                Bitmap                      bgTile;
                Drawable                    boardImage;
                Canvas                      canvas;
                Canvas                      board;
                Rect                        dstRect    = new Rect();
                final ConnectionsGridLayout gridLayout = view_main.findViewById( R.id.boardGridLayout );
                int                         padding    = getResources().getDimensionPixelSize( R.dimen._1sdp );
                
                // Try to detect the mapping
                try
                {
                    loadBmp = Bitmap.createBitmap( gridLayout.getWidth(), gridLayout.getHeight(), Bitmap.Config.ARGB_8888 );
                    boardImage = view.getBackground();
                    boardImage.setBounds( 0, 0, view.getWidth(), view.getHeight() );
                    
                    bgTile = BitmapFactory.decodeResource( getResources(), bgTileResId );
                    canvas = new Canvas( loadBmp );
                    // Copy the board image to the new BMP
                    boardImage.draw( canvas );
                    // Again to make it darker
                    boardImage.draw( canvas );
                    
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
                                
                                dstRect.left = ex.getLeft() + padding;
                                dstRect.top = ex.getTop() + padding;
                                dstRect.right = ex.getRight() - padding;
                                dstRect.bottom = ex.getBottom() - padding;
                                //
                                canvas.drawBitmap( bgTile, null, dstRect, null );
                            }
                        }
                    }
                    
                    //
                    // Set the tiled image
                    
                    view.setBackground( new BitmapDrawable( getResources(), loadBmp ) );
                    bgTile.recycle();
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                }
                
                // Kill the listener
                view.getViewTreeObserver().removeOnGlobalLayoutListener( this );
            }
        } );
    }
    
    
    /**
     * //#############################
     * <p>
     * Load the special item boxes
     * <p>
     * //#############################
     */
    private void loadSpecialItemData()
    {
        ViewGroup group    = view_main.findViewById( R.id.specialFooter );
        int[]     frames   = { R.id.bombFrame, R.id.boltFrame, R.id.starFrame, R.id.randomFrame };
        int[]     counters = { R.id.bombCounter, R.id.boltCounter, R.id.starCounter, R.id.randomCounter };
        int[]     holders  = { R.id.bombHolder, R.id.boltHolder, R.id.starHolder, R.id.randomHolder };
        int[] images = {
                R.drawable.bomb_on,
                R.drawable.bomb_off,
                R.drawable.bolt_on,
                R.drawable.bolt_off,
                R.drawable.star_on,
                R.drawable.star_off,
                R.drawable.random_on,
                R.drawable.random_off
        };
        
        //##############################
        //
        //
        //
        //##############################
        for ( int i = 0; i < frames.length; i++ )
        {
            FrameLayout      layout = group.findViewById( frames[ i ] );
            GradientTextView tv     = layout.findViewById( counters[ i ] );
            ImageView        img    = layout.findViewById( holders[ i ] );
            
            // Give it a clicker!
            layout.setOnClickListener( this );
            layout.setTag( i );
            
            // Have boosters
            if ( gameEngine.Boosters[ i ] > 0 )
            {
                img.setImageResource( images[ i + i ] );
                tv.setText( String.format( Locale.getDefault(), "%d", gameEngine.Boosters[ i ] ) );
            }
            else
            {
                img.setImageResource( images[ i + i + 1 ] );
                tv.setText( "+" );
            }
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
            final SecureRandom          r               = new SecureRandom();
            final ConnectionsGridLayout grid            = getView().findViewById( R.id.boardGridLayout );
            final GradientTextView      timesUp         = new GradientTextView( getContext() );
            Typeface                    typeface        = ResourcesCompat.getFont( getContext(), R.font.badaga );
            final ConstraintLayout      conLayout       = getView().findViewById( R.id.boardGridFrame );
            LinearLayout                matchHintLayout = getView().findViewById( R.id.matchHintLayout );
            //            ImageView                   matchHintIcon   = getView().findViewById( R.id.matchHintIcon );
            
            
            //###############################
            //
            // Clear all tile statuses
            //
            //###############################
            for ( BoardTile tile : boardTiles )
            {
                tile.cardFlipped = false;
                tile.endAnimation();
                tile.clearColorFilter();
                tile.setBackgroundColor( 0 );
            }
            
            
            // Hide the coin counter bubble
            matchHintLayout.setVisibility( View.INVISIBLE );
            //            matchHintIcon.setVisibility( View.GONE );
            // Clear all lines
            grid.tilePoints.clear();
            grid.lineBuffer.eraseColor( Color.TRANSPARENT );
            grid.invalidate();
            
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
            timesUp.setGravity( Gravity.CENTER_HORIZONTAL );
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
                                            
                                            if ( gameEngine.soundPlayer != null )
                                            {
                                                //@@@@@@@@@@@@@@@@@@@
                                                gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                                                gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
                                            }
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
                    // Drop all the tiles randomly
                    //
                    //#################################
                    int i     = 0;
                    int delay = 0;
                    
                    for ( int y = 0; y < grid.mapHeight; y++ )
                    {
                        for ( int x = 0; x < grid.mapWidth; x++ )
                        {
                            final BoardTile tile   = boardTiles.get( i++ );
                            final int       delay2 = delay;
                            
                            float xx = tile.getWidth();
                            float yy = tile.getHeight();
                            
                            tile.setPivotX( xx / 2 );
                            tile.setPivotY( yy );
                            
                            PropertyValuesHolder rot    = PropertyValuesHolder.ofFloat( "rotation", 0, 225, 140, 215, 150, 200, 165, 180 );
                            PropertyValuesHolder transY = PropertyValuesHolder.ofFloat( "translationY", 0, 0, 0, 0, 0, grid.getHeight() * 2 );
                            ObjectAnimator       obj    = ObjectAnimator.ofPropertyValuesHolder( tile, rot, transY );
                            //
                            obj.setDuration( 2000 ).setStartDelay( delay );
                            obj.start();
                            gameEngine.animatorList.add( obj );
                        }
                        
                        //
                        delay += 50;
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
        //###################################
        //
        // Update the score
        //
        //###################################
        if ( gridFunction == ConnectionsGridLayout.SCORE_UPDATE )
        {
            // Update the score
            //            int currentScore = gameEngine.Scores[ gameEngine.loadedArea ];
            int currentScore = levelScore;
            
            // Update the score and make it final
            levelScore = (currentScore + gridData);
            if ( bar != null )
            {
                bar.setProgress( levelScore );
            }
            
            
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
            
        }
        else if ( gridFunction == ConnectionsGridLayout.BONUS_SPLASH )
        {
            float[] bonusTextSize = { 1.0f, 1.1f, 1.15f, 1.2f, 1.25f, 1.3f, 1.35f, 1.35f, 1.35f };
            // White, Black, Red, Green, Blue, Orange, Yellow, Purple, Brown
            int[] endColor = {
                    0xFFFFFFFF, 0xFF4F4F4F, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFF7F00, 0xFFFFFF00, 0xFFFF00FF, 0xFF8F5C00
            };
            String[] praiseMessage = {
                    "Nice Connection!", "Cool!", "Awesome!!", "Super!", "Excellent!", "Wild!!", "Extreme!", "Ridiculous!!", "Epic Connection!!"
            };
            
            //
            if ( gridData > 5 )
            {
/*
                final GradientTextView text = view_main.findViewById( R.id.comboUpdate );
                // Modify attributes
                float transY = -(text.getTextSize() * 1.5f);
                // Fix indexing
                int index;
                
                //
                connections++;
                gridData -= 6;
                index = Math.min( gridData, 8 );
                
                // Get the text view
                text.setText( String.format( Locale.getDefault(), "%s\nPoints x %d", praiseMessage[ index ], praiseMultiplier[ index ] ) );
                text.setVisibility( View.VISIBLE );
                text.setStrokeColor( Color.BLACK );
                text.setStrokeWidth( getResources().getDimensionPixelSize( R.dimen._3sdp ) );
                text.setTextSize( getResources().getDimensionPixelSize( R.dimen._12ssp ) * bonusTextSize[ index ] );
                text.setColorList( new int[]{ 0xFFFFFFFF, endColor[ index ] } );
                
                
                // Remove so no duplicates
                if ( praiseAnimator != null )
                {
                    gameEngine.animatorList.remove( praiseAnimator );
                }
                //
                praiseAnimator = ObjectAnimator.ofFloat( text, "TranslationY", transY );
                praiseAnimator.setDuration( 750 ).setInterpolator( new LinearInterpolator() );
                praiseAnimator.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        text.setScaleX( 1.3f );
                        text.setScaleY( .76f );
                        text.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate( ValueAnimator animation )
                            {
                                if ( text.getTag() == null )
                                {
                                    text.setTag( 1 );
                                    gameEngine.animatorList.add( animation );
                                }
                            }
                        } );
                        text.animate().setDuration( 750 );
                        text.animate().scaleY( 1f ).scaleX( 1f ).setInterpolator( new BounceInterpolator() );
                        text.animate().withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                text.setVisibility( View.GONE );
                                text.setTranslationY( 0 );
                            }
                        } ).start();
                    }
                } );
                //
                praiseAnimator.start();
                gameEngine.animatorList.add( praiseAnimator );
*/
            }
        }
        else if ( gridFunction == ConnectionsGridLayout.MOVES_UPDATE )
        {
            // Report the current move count
            if ( getView() != null )
            {
                TextView moves = getView().findViewById( R.id.boardMovesText );
                
                GameEngine.boardMoves = gridData;
                moves.setText( String.format( Locale.getDefault(), "%d", Math.max( gridData, 0 ) ) );
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Load the game after the
     * <p>
     * //###############################
     */
    private void startTheGame()
    {
        //##############################
        //
        // Show what we built
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
                
                final ConnectionsGridLayout connectionsGridLayout = view_main.findViewById( R.id.boardGridLayout );
                ImageEffect                 background            = view_main.findViewById( R.id.boardBackground );
                boolean                     gameResumed;
                //                final ImageView             view                  = view_main.findViewById( R.id.boardGridSlots );
                
                
                if ( GameEngine.timer != null )
                {
                    GameEngine.timer.setUserCancel( CustomTimer.USER_CANCEL );
                    GameEngine.timer.cancel();
                    GameEngine.timer = null;
                }
        
/*
        GameEngine.timer = new CustomTimer( gameEngine.timeAllowed, getContext(), view_main.findViewById( R.id.boardTimeText ) );
        GameEngine.timer.setOnCommunicator( ConnectionsFragment.this );
        GameEngine.timer.addListener( new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart( Animator animation )
            {
                logicThread.clearData();
                logicThread.setHintTimer( CustomTimer.currentTime );
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
*/

/*
        GameEngine.timer.start();
        gameEngine.animatorList.add( GameEngine.timer );
*/
                try
                {
                    //################################
                    //
                    // After Drawing the symbols, animate
                    // it all onto the screen.
                    //
                    //################################
                    
                    // Reset Board moves
                    GameEngine.boardMoves = 0;
                    
                    // Need to have board pieces
                    connectionsGridLayout.setBoardTiles( boardTiles );
                    
                    // This gets modified by the help system. restore it!
                    ConnectionsGridLayout.gMapHeight = gameBoard.getMapHeight();
                    ConnectionsGridLayout.gMapWidth = gameBoard.getMapWidth();
                    
                    
                    // Display the initial symbols
                    if ( getContext() != null )
                    {
                        gameBoard.BuildConnectionsBoard( getContext(), GameEngine.currentBoardImage, boardTiles, view_main );
                        
                        StarsEarnedBar bar = view_main.findViewById( R.id.starsToEarn );
                        
                        // TODO: Testing only!!! No gems or coins. Just stars
                        if ( gameBoard.neededStars > 0 && bar != null )
                        {
                            bar.setMax( 20000 );
                            bar.setProgress( 0 );
                            bar.setStarsPosi( "3000,12000,19000" );
                        }
                        else if ( bar != null )
                        {
                            if ( gameBoard.neededPoints > 0 )
                            {
                                int value = gameBoard.neededPoints * 2;
                                
                                bar.setMax( value );
                                bar.setProgress( 0 );
                                bar.setStarsPosi( String.format( Locale.US, "%d,%d,%d", value / 4, value / 2, (value - value / 8) ) );
                            }
                            else
                            {
                                bar.setMax( 4000 );
                                bar.setProgress( 0 );
                                bar.setStarsPosi( "800,1200,3500" );
                            }
                        }
                    }
                    
                    
                    //#########################################
                    //
                    // Any save data that has been resumed gets
                    // loaded here
                    //
                    //#########################################
                    if ( GameEngine.resumeGame )
                    {
/*
                        gameResumed = true;
                        GameEngine.resumeGame = false;
*/
                        GameState.loadGameState( getContext() );
                        
                        //
                        // Set the Background image
                        //
                        background.setImageResource( gameBoard.xmlBackgrounds[ GameEngine.currentBackground ] );
                        
                        gameEngine.timeAllowed = CustomTimer.currentTime;
                        
                        // Check for Match exist
                        connectionsGridLayout.checkIfMatchesExist( false );
                        
                        //
                        connectionsGridLayout.onDrawGrid();
                        
                        // Reflect the correct information
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
                        gameEngine.timeAllowed = 250000;
                    }
                    
                    //
                    // Board moves
                    //
/*
            TextView moves = view_main.findViewById( R.id.boardMovesText );
            moves.setText( String.format( Locale.getDefault(), " %d ", GameEngine.boardMoves ) );
*/
                    
                    //
                    connectionsGridLayout.setBackgroundColor( 0x00000000 );
                    connectionsGridLayout.setVisibility( View.VISIBLE );
                    connectionsGridLayout.setScaleY( 1f );
                    connectionsGridLayout.setScaleX( 1f );
                    
                    //################################
                    //
                    // Board grid map
                    //
                    //################################
                    GridLayoutView gridLayoutView = view_main.findViewById( R.id.boardGrid );
                    gridLayoutView.setRowCount( connectionsGridLayout.getRowCount() );
                    gridLayoutView.setColumnCount( connectionsGridLayout.getColumnCount() );
                    gridLayoutView.setBoardTiles( boardTiles );
                    // Build it
                    gridLayoutView.createBoardImage( connectionsGridLayout );
                    gridLayoutView.setScaleY( 1f );
                    gridLayoutView.setScaleX( 1f );
                    
                    
                    // Animate the grid and tiles into scene
                    gridLayoutView.setAlpha( 0 );
                    connectionsGridLayout.setAlpha( 0 );
                    
                    //
                    gridLayoutView.animate().alpha( 1 ).setDuration( 700 ).start();
                    connectionsGridLayout.animate().alpha( 1 ).setDuration( 700 ).start();
                    
                    
                    // Create the masking image
                    OverlayView overlayView = view_main.findViewById( R.id.gridMask );
                    overlayView.createMaskImage( background, connectionsGridLayout, boardTiles );
                    
                    // Reset the moves for the game
                    //                    connectionsGridLayout.setBoardMoves( 0 );
                    GameEngine.boardMoves = 0;
                    
                    
/*
            
            //################################
            //
            // Help System
            //
            //################################
            if ( getActivity() != null && getContext() != null && !GameEngine.wasHelpReceived( getContext() ) )
            {
                try
                {
                    //###########################
                    //
                    // Run the help system
                    //
                    //###########################
*/
/*
                                HelpDialog helpDialog = new HelpDialog( true );
                                helpDialog.setCancelable( false );
                                helpDialog.show( getActivity().getSupportFragmentManager(), "Help" );
                                
                                helpDialog.setOnDismisser( new HelpDialog.OnDismisser()
                                {
                                    @Override
                                    public void onDismiss()
                                    {
                                        //                                        startTheGame();
                                        showObjectives( level, gameResumed );
                                    }
                                } );
*/
/*

                    
                    
                    showObjectives( GameEngine.currentLevel, gameResumed );
                }
                catch ( NullPointerException npe )
                {
                    npe.printStackTrace();
                    showObjectives( GameEngine.currentLevel, gameResumed );
                }
            }
            else
            {
                showObjectives( GameEngine.currentLevel, gameResumed );
            }
*/
                    
                    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                    //
                    // Start the associated BG Music
                    //
                    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgMusic( (gameEngine.currentBGMusic >> 2), PlaySound.LOOP );
                    }
                }
                catch ( NullPointerException npe )
                {
                    npe.getMessage();
                }
                
                //
                //        connectionsGridLayout.setTimer( GameEngine.timer );
                //        connectionsGridLayout.checkIfMatchesExist( true, true );
                
                //
                GameEngine.reloadSameGame = false;
                
                // Display the objectives
                showObjectives( GameEngine.currentLevel, GameEngine.resumeGame );
                GameEngine.resumeGame = false;
            }
        } ).start();
    }
    
    
    /**
     * //###############################
     * <p>
     * Show the objectives and goals
     * <p>
     * //###############################
     */
    private void showObjectives( int level, boolean gameResumed )
    {
        //################################
        //
        // Objectives System
        //
        //################################
        if ( !gameResumed )
        {
            ObjectivesDialog dialog;
            int              msg = 0;
            
            dialog = new ObjectivesDialog( getContext(), gameBoard.objMessage, GameBoard.targetViewsList, msg );
            // Do not allow cancelable closing
            dialog.setCancelable( false );
            dialog.setOnDismissListener( new DialogInterface.OnDismissListener()
            {
                @Override
                public void onDismiss( DialogInterface dialog )
                {
                    // Start hint timer
                    logicThread.setHintTimer( LogicThread.PLAY_HINT );
                }
            } );
            dialog.show();
        }
        else
        {
            // Start hint timer
            logicThread.setHintTimer( LogicThread.PLAY_HINT );
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
        View view = getView();
        
        if ( code == GamePaused.MENU_REQUEST || code == GamePaused.QUIT_REQUEST )
        {
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
                                        //                                        getActivity().onBackPressed();
                                        GameEngine.reloadSameGame = true;
                                        
                                        if ( gameEngine.soundPlayer != null )
                                        {
                                            //@@@@@@@@@@@@@@@@@@@
                                            gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                                            gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
                                        }
                                        
                                        //
                                        if ( GameEngine.reloadedLevel > -1 )
                                        {
                                            GameEngine.currentLevel = GameEngine.reloadedLevel;
                                            GameEngine.reloadedLevel = -1;
                                        }
                                        //
                                        FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                                    }
                                    else
                                    {
                                        // Create a save state
                                        GameEngine.reloadSameGame = false;
                                        CustomTimer.currentTime = tempTime;
                                        GameState.saveGameState( getContext(), 0, view_main );
                                        //
                                        FragmentLoader.SwapFragment( getActivity(), getContext(), new GameStartFragment(), FragmentLoader.REPLACE_FRAGMENT );
                                    }
                                    
                                    // Memory leak??
                                    view_main = null;
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
                                
                                GameState.saveGameState( getContext(), 0, view_main );
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
     * /####################################
     * <p>
     * Let the player choose where to place
     * the booster. If a booster exists, swap
     * new booster for the old booster
     * <p>
     * //###################################
     *
     * @param index
     */
    public void selectBoosterLocation( View view, int index )
    {
        ConnectionsGridLayout grid = view_main.findViewById( R.id.boardGridLayout );
        
        
        // Need context
        if ( getContext() == null )
        {
            return;
        }
        
        
        //
        // User wants a different special
        //
        if ( setBooster != -1 )
        {
            setBooster = -1;
            
            HighLighter highLighter = view_main.findViewById( R.id.highLighter );
            
            for ( int i = 0; i < highLighter.getTargetsCount(); i++ )
            {
                View target = highLighter.getTarget( i );
                if ( target instanceof ConnectionsGridLayout )
                {
                    continue;
                }
                
                highLighter.removeTarget( i );
            }
            
            // Erase setting booster state
            grid.setBoosterListener( null, -1 );
        }
        
        
        //##############################
        //
        // Get the Booster we want
        //
        //##############################
        setBooster = index + BoardTile.BOMB;
        
        // Animate the booster holder
        boosterView = ( FrameLayout ) view;
        
        
        //##############################
        //
        //
        //
        //##############################
        if ( !GameEngine.getBoosterHelper( getContext() ) && getActivity() != null )
        {
            ShowClass showClass = new ShowClass( getContext() );
            showClass.addTarget( grid, true, 500, 0, new LinearInterpolator(), ShowClass.ROUNDED_RECT, ShowClass.CENTER_OUT )
                     .addTargetTip( showClass.getTargetCount(), grid, "Booster Placement", "Tap a coin to swap for this booster!", R.drawable.bubble_center_blue_a,
                                    ShowClass.TIP_CENTER
                                  )
                     .setOnCloseType( ShowClass.ON_TIMER, 1000 )
                     .setOnShowCaseCloseListener( new ShowClass.OnShowCaseCloseListener()
                     {
                         @Override
                         public void onClosed()
                         {
                             HighLighter highLighter = view_main.findViewById( R.id.highLighter );
                    
                             highLighter.addTarget( boosterView, HighLighter.ROUNDED_RECT, HighLighter.CENTER_OUT )
                                        .addTarget( grid, HighLighter.ROUNDED_RECT, HighLighter.CENTER_OUT )
                                        .setCloseOnTouch( true )
                                        .run();
                             //
                             grid.setBoosterListener( ConnectionsFragment.this, setBooster );
                         }
                     } )
                     .showCaseTargets();
            //
            GameEngine.setBoosterHelper( getContext(), true );
        }
        else
        {
            SecureRandom r           = new SecureRandom();
            HighLighter  highLighter = view_main.findViewById( R.id.highLighter );
            
            /*            highLighter.addTarget( grid, HighLighter.ROUNDED_RECT, HighLighter.CENTER_OUT )*/
            highLighter.addTarget( grid, HighLighter.ROUNDED_RECT, r.nextInt( HighLighter.CENTER_OUT_HORZ + 1 ) )
                       .addTarget( boosterView, HighLighter.ROUNDED_RECT, HighLighter.CENTER_OUT )
                       .setCloseOnTouch( true )
                       .run();
            
            // Make the selection
            grid.setBoosterListener( this, setBooster );
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Wait and listen for the grid to
     * return a location
     * <p>
     * //###############################
     *
     * @param i
     */
    @Override
    public void boosterLocationSet( int i )
    {
        ConnectionsGridLayout grid     = view_main.findViewById( R.id.boardGridLayout );
        boolean               isRandom = false;
        
        //##############################
        //
        // Replace what ever was there
        //
        //##############################
        gameEngine.Boosters[ setBooster - BoardTile.BOMB ]--;
        //
        
        if ( setBooster == BoardTile.RANDOM )
        {
            // Random selected booster
            SecureRandom r = new SecureRandom();
            
            setBooster = r.nextInt( 3 ) + BoardTile.BOMB;
            isRandom = true;
        }
        
        //
        boardTiles.get( i ).tileNum = setBooster;
        boardTiles.get( i ).setSpecialItem( 2 );
        // This may need to change if special uses a BG
        boardTiles.get( i ).specialTile = -1;
        
        //
        loadSpecialItemData();
        
        // Show the changed tile
        grid.onDrawGrid();
        // Activate the special
        grid.activateBooster( boardTiles.get( i ), setBooster, isRandom );
        // Show it
        grid.onUpdateGrid();
        
        //
        HighLighter highLighter = view_main.findViewById( R.id.highLighter );
        highLighter.endHighlight();
        
        setBooster = -1;
        boosterView = null;
        
        // Erase setting booster state
        grid.setBoosterListener( null, -1 );
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
        int                   id   = v.getId();
        ConnectionsGridLayout grid = view_main.findViewById( R.id.boardGridLayout );
        
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
                                dialog.setTargetFragment( ConnectionsFragment.this, 1 );
                                
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
        else if ( v instanceof FrameLayout )
        {
            final int index = Integer.parseInt( v.getTag().toString() );
            
            //###########################################
            //
            //
            //
            //###########################################
            if ( gameEngine.Boosters[ index ] > 0 )
            {
                // All touches resets the counter
                logicThread.setHintTimer( LogicThread.PLAY_HINT );
                
/*
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
                        selectBoosterLocation( v, index );
                        //
                        v.setClickable( true );
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
*/
                if ( boosterView != null && boosterView == v )
                {
                    // User wants to cancel booster set
                    HighLighter h = view_main.findViewById( R.id.highLighter );
                    h.endHighlight();
                    boosterView = null;
                    
                    setBooster = -1;
                    // Erase setting booster state
                    if ( grid != null )
                    {
                        grid.setBoosterListener( null, -1 );
                    }
                    //
                    loadSpecialItemData();
                }
                else
                {
                    //@@@@@@@@@@@@@@@@ Button Click
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.play( PlaySound.BUTTON_CLICK );
                    }
                    
                    selectBoosterLocation( v, index );
                }
                
                // Disable clicking on the same button that called THIS
                //                v.setClickable( false );
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
        praiseAnimator = null;
        scoreAnimator = null;
        
        // Inflate the layout for this fragment
        view_main = null;
        
        super.onDestroy();
    }
}