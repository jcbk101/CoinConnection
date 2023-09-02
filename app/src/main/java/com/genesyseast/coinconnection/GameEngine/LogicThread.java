package com.genesyseast.coinconnection.GameEngine;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.gridlayout.widget.GridLayout;

import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.GameGraphics.CardsGridLayout;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;

import java.util.ArrayList;

public class LogicThread
{
    public static int CMD_IDLE            = 0;
    public static int CMD_UPDATE_GRID     = 1;
    public static int CMD_SWAP            = 2;
    public static int CMD_FAKE_SWAP       = 3;
    public static int CMD_CREATE_SPECIALS = 4;
    public static int CMD_PROCESS_MATCHES = 5;
    public static int CMD_DROP            = 6;
    public static int CMD_SHOW_HINT       = 7;
    public static int CMD_LEVEL_COMPLETE  = 8;
    public static int CMD_LEVEL_FAILED    = 9;
    public static int CMD_FLIP            = 10;
    public static int CMD_GEM_DROP        = 11;
    public static int CMD_EXIT_THREAD     = 12;
    //
    public static int PLAY_HINT           = 0;
    public static int STOP_HINT           = -1;
    public static int PAUSE_HINT          = 1;
    public static int RESUME_HINT         = 2;
    
    private String[] commands = {
            "CMD_IDLE",
            "CMD_UPDATE_GRID",
            "CMD_SWAP",
            "CMD_FAKE_SWAP",
            "CMD_CREATE_SPECIALS",
            "CMD_PROCESS_MATCHES",
            "CMD_DROP",
            "CMD_SHOW_HINT",
            "CMD_LEVEL_COMPLETE",
            "CMD_LEVEL_FAILED",
            "CMD_FLIP",
            "CMD_GEM_DROP",
            "CMD_EXIT_THREAD"
    };
    
    //
    private        int                currentCmd        = 0;
    private        boolean            isRunning         = false;
    public         int                animationsRunning = 0;
    public         ArrayList<Integer> cmdStack;
    //
    private        Activity           activity;
    private        GridLayout         gridLayout;
    //
    private static int                saveCurrentCmd;
    private static int                saveAniRun;
    //
    private        long               hintTimer         = -1;
    private        int                hintDelay         = 5000;
    private        boolean            hintCanceled;
    public         int                hintIsDisplayed   = 0;
    //
    public         ValueAnimator      hintAnim;
    
    
    /**
     * refresh data for a new game
     */
    public void clearData()
    {
        hintTimer = -1;
        currentCmd = CMD_IDLE;
        saveCurrentCmd = currentCmd;
        animationsRunning = 0;
        saveAniRun = animationsRunning;
        //
        isRunning = false;
        
        if ( cmdStack != null )
        {
            cmdStack.clear();
        }
    }
    
    public void saveLogic()
    {
        saveCurrentCmd = currentCmd;
        saveAniRun = animationsRunning;
        if ( hintAnim != null && hintAnim.isRunning() )
        {
            hintAnim.pause();
        }
    }
    
    public void restoreLogic()
    {
        if ( cmdStack != null )
        {
            if ( saveCurrentCmd != CMD_IDLE )
            {
                cmdStack.add( saveCurrentCmd );
                animationsRunning = saveAniRun;
            }
            else
            {
                cmdStack.clear();
                animationsRunning = 0;
            }
        }
        
        //
        if ( hintAnim != null && hintAnim.isPaused() )
        {
            hintAnim.resume();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Start up the hint system
     * <p>
     * //##############################
     *
     * @param hintTimer
     */
    public void setHintTimer( int hintTimer )
    {
        //
        // No hints wanted
        //
        if ( (GameEngine.systemFlags & GameEngine.HINT_OFF) == GameEngine.HINT_OFF || (( ConnectionsGridLayout ) gridLayout).levelCompleted )
        {
            if ( hintAnim != null )
            {
                hintCanceled = true;
                hintAnim.cancel();
            }
            
            // Start fresh
            hintAnim = null;
            
            return;
        }
        
        // Pause the Hint timer
        if ( hintTimer == PAUSE_HINT )
        {
            if ( hintAnim != null && hintAnim.isRunning() )
            {
                hintAnim.pause();
            }
            
            return;
        }
        else if ( hintTimer == RESUME_HINT )
        {
            // Resume the Hint timer
            if ( hintAnim != null && hintAnim.isPaused() )
            {
                hintAnim.resume();
            }
            
            return;
        }
        else if ( hintTimer == STOP_HINT )
        {
            // Kill the Hint timer
            if ( hintAnim != null && hintAnim.isRunning() )
            {
                hintCanceled = true;
                hintAnim.cancel();
            }
            
            // Start fresh
            hintAnim = null;
            return;
        }
        else
        {
            // Default: Run / Restart the hint system
            if ( hintAnim != null )
            {
                if ( hintAnim.isRunning() )
                {
                    hintCanceled = true;
                    hintAnim.cancel();
                }
            }
            
            // Start fresh
            hintAnim = null;
            // Default value
            this.hintTimer = hintDelay;
            
            //
            
            hintAnim = ValueAnimator.ofInt( 0, ( int ) this.hintTimer );
            hintAnim.setDuration( ( int ) this.hintTimer ).setStartDelay( 0 );
            hintAnim.setInterpolator( new LinearInterpolator() );
            hintAnim.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                
                }
                
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    if ( gridLayout instanceof ConnectionsGridLayout && !hintCanceled )
                    {
                        (( ConnectionsGridLayout ) gridLayout).showHint();
                    }
                    
                    hintCanceled = false;
                }
                
                @Override
                public void onAnimationCancel( Animator animation )
                {
                    hintCanceled = true;
                }
                
                @Override
                public void onAnimationRepeat( Animator animation )
                {
                
                }
            } );
            
            hintAnim.start();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Get an instance of the
     * grid class
     * <p>
     * //##############################
     *
     * @param gridLayout
     */
    public void setGridLayout( GridLayout gridLayout )
    {
        this.gridLayout = gridLayout;
    }
    
    
    /**
     * Current command being run
     *
     * @return
     */
    public int getCurrentCmd()
    {
        return currentCmd;
    }
    
    
    /**
     * Constructor
     *
     * @param activity
     */
    public LogicThread( Activity activity )
    {
        this.activity = activity;
        
        // When this is called, set game variables
        //        isRunning = true;
        cmdStack = new ArrayList<>();
        //        currentCmd = CMD_IDLE;
        currentCmd = -1;
    }
    
    
    /**
     * //#####################################
     * <p>
     * Main game loop. Calls needed features
     * SYNCHRONOUSLY and handled in
     * a separate thread as needed.
     * <p>
     * //#####################################
     */
    public void processCommand()
    {
        isRunning = true;
        
        
        //####################################
        //
        // Purpose of this thread is to allow
        // animations_bg to run to completion and
        // allow each needed function to run
        // synchronously without interruption
        //
        //####################################
        while ( isRunning )
        {
            // All executed animations_bg must finish first
            // And there MUST be a command on the stack
            if ( cmdStack != null && animationsRunning <= 0 && cmdStack.size() > 0 )
            {
                //
                // Update the main grid if a
                // request was made
                //
                try
                {
                    currentCmd = cmdStack.get( 0 );
                    cmdStack.remove( 0 );
                    
                    //################################
                    // Do we need to update the grid?
                    // Simple, basic call
                    //################################
                    if ( currentCmd == CMD_UPDATE_GRID && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                        }
                        else
                        {
                            (( CardsGridLayout ) gridLayout).onUpdateGrid();
                        }
                    }
                    //################################
                    //
                    //
                    //################################
                    else if ( (currentCmd == CMD_SWAP || currentCmd == CMD_FLIP) && gridLayout != null )
                    {
                        // Swap
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                        }
                        else
                        {
                            (( CardsGridLayout ) gridLayout).onUpdateGrid();
                        }
                    }
                    //################################
                    //
                    //
                    //################################
                    else if ( currentCmd == CMD_FAKE_SWAP && gridLayout != null )
                    {
                        // Swap
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                        }
                        else
                        {
                            (( CardsGridLayout ) gridLayout).onUpdateGrid();
                        }
                    }
                    //################################
                    // Do we need to create special
                    // items and have them drop??
                    //################################
                    else if ( currentCmd == CMD_CREATE_SPECIALS && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            // Create special items
                            (( ConnectionsGridLayout ) gridLayout).createSpecialItems();
                            
                            // Put tiles to MATCHED state
                            //                                gridLayout.signalMatchesFound();
                            
                            // Start animating changes
                            (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                        }
                    }
                    //################################
                    // Do we need to animate the tiles
                    // being removed?
                    //################################
                    else if ( currentCmd == CMD_PROCESS_MATCHES && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            // Put tiles to MATCHED state
                            (( ConnectionsGridLayout ) gridLayout).signalMatchesFound();
                            
                            // Start animating them [ Remove from board visually ]
                            (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                        }
                        else
                        {
                            // Start animating them [ Remove from board visually ]
                            (( CardsGridLayout ) gridLayout).onUpdateGrid();
                        }
                    }
                    //################################
                    // Do we need to animate the
                    // floating tiles being dropped??
                    //################################
                    else if ( currentCmd == CMD_DROP && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            // Put tiles to MATCHED state
                            (( ConnectionsGridLayout ) gridLayout).signalDroppingTiles();
                            
                            if ( (( ConnectionsGridLayout ) gridLayout).objStillDropping || (( ConnectionsGridLayout ) gridLayout).isInActive )
                            {
                                (( ConnectionsGridLayout ) gridLayout).onUpdateGrid();
                            }
                            else if ( !(( ConnectionsGridLayout ) gridLayout).isInActive )
                            {
                                addToStack( LogicThread.CMD_IDLE );
                            }
                        }
                    }
                    else if ( currentCmd == CMD_IDLE && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            if ( (( ConnectionsGridLayout ) gridLayout).objStillDropping )
                            {
                                addToStack( LogicThread.CMD_DROP );
                            }
                            else
                            {
                                (( ConnectionsGridLayout ) gridLayout).checkForErrors();
                                
                                // To use for "target reached" checking
                                if ( (( ConnectionsGridLayout ) gridLayout).checkTargetsReached() )
                                {
                                    addToStack( LogicThread.CMD_LEVEL_COMPLETE );
                                }
                                else
                                {
                                    // Prepare the hint time
                                    (( ConnectionsGridLayout ) gridLayout).checkIfMatchesExist( false );
                                    
                                    //
                                    //hintTimer = (CustomTimer.currentTime - hintDelay);
                                    setHintTimer( PLAY_HINT );
                                }
                            }
                        }
                        else
                        {
                            //#################################
                            //
                            // Clear any errors on the board
                            //
                            //#################################
                            (( CardsGridLayout ) gridLayout).checkForErrors();
                            
                            // To use for "target reached" checking
                            if ( (( CardsGridLayout ) gridLayout).checkTargetsReached() == 0 )
                            {
                                addToStack( LogicThread.CMD_LEVEL_COMPLETE );
                            }
                            else if ( (( CardsGridLayout ) gridLayout).checkMovesExhausted() )
                            {
                                addToStack( LogicThread.CMD_LEVEL_FAILED );
                            }
                            else
                            {
                                if ( (( CardsGridLayout ) gridLayout).getFlippedTiles() == 2 )
                                {
                                    (( CardsGridLayout ) gridLayout).setFlippedTiles( 0 );
                                }
                            }
                        }
                    }
                    else if ( currentCmd == CMD_SHOW_HINT && gridLayout != null )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).showHint();
                        }
                        
                        cmdStack.add( CMD_IDLE );
                        //                        hintTimer = (CustomTimer.currentTime - hintDelay);
                    }
                    //################################
                    // Game is complete
                    //################################
                    else if ( currentCmd == CMD_LEVEL_COMPLETE )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).signalLevelComplete();
                        }
                        else
                        {
                            (( CardsGridLayout ) gridLayout).signalLevelComplete();
                        }
                    }
                    //################################
                    // Game is a failure
                    //################################
                    else if ( currentCmd == CMD_LEVEL_FAILED )
                    {
                        if ( gridLayout instanceof ConnectionsGridLayout )
                        {
                            (( ConnectionsGridLayout ) gridLayout).signalLevelFailed();
                        }
                        else
                        {
                            (( CardsGridLayout ) gridLayout).signalLevelFailed();
                        }
                    }
                }
                catch ( NullPointerException npe )
                {
                    npe.printStackTrace();
                }
                
                
                //#####################################
                //
                // Exit if the stack is clear
                //
                //#####################################
                if ( cmdStack.size() == 0 )
                {
                    isRunning = false;
                    return;
                }
            }
        }
    }
    
    
    /**
     * //#####################################
     * <p>
     * Hope to prevent errors after killing
     * the process
     * <p>
     * //#####################################
     *
     * @param cmdCode
     */
    public void addToStack( int cmdCode )
    {
        if ( cmdStack != null )
        {
            cmdStack.add( cmdCode );
            
            if ( animationsRunning != 0 )
            {
                Toast.makeText( activity, "Animations Exist, running Command[" + currentCmd + "]: " + commands[ currentCmd ], Toast.LENGTH_SHORT ).show();
            }
            
            // Run the command if it's not running already
            if ( !isRunning )
            {
                processCommand();
            }
/*
            else
            {
                Toast.makeText( activity, "Already running Command[" + currentCmd + "]: " + commands[ currentCmd ], Toast.LENGTH_SHORT ).show();
            }
*/
        }
    }
    
    
    /**
     * ######################
     * <p>
     * Kill memory leaks
     * <p>
     * ######################
     */
    public void killThread()
    {
        cmdStack = null;
        activity = null;
        gridLayout = null;
        if ( hintAnim != null )
        {
            hintAnim.cancel();
        }
        
        hintAnim = null;
    }
    
}