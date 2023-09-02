package com.genesyseast.coinconnection;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.gridlayout.widget.GridLayout;

import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.Dialogs.AgeVerify;
import com.genesyseast.coinconnection.Fragments.FragmentLoader;
import com.genesyseast.coinconnection.Fragments.GameStartFragment;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.GameGraphics.CardsGridLayout;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.PlaySound.PlaySound;


public class MainActivity
        extends AppCompatActivity
{
    private Activity                      activity;
    private GameEngine                    gameEngine;
    private GameBoard                     gameBoard;
    private LogicThread                   logicThread;
    private long                          backPressed;
    private boolean                       savedMusicStatus = false;
    static  FragmentManager               fm;
    public  GameEngine.OnAdReturnListener onAdReturnListener;
    
    
    /**
     * Main entry point
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        
        //###############################
        //
        // Get fackebook App KeyHash
        //
        //###############################
/*
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo( GameStartFragment.APP_PNAME, PackageManager.GET_SIGNATURES );
    
            for ( Signature sig: info.signatures )
            {
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                md.update( sig.toByteArray() );
                Log.d( "KeyHash", Base64.encodeToString( md.digest(), Base64.DEFAULT ) );
            }
        }
        catch ( PackageManager.NameNotFoundException | NoSuchAlgorithmException nnfe )
        {
            nnfe.printStackTrace();
        }
*/
        
        
        //###############################
        //
        // Set in Release when ready!
        //
        //###############################
        
        GameEngine.debugMode = true;
        //GameEngine.debugMode = false;
        
        
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        //        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.main_activity_start );
        
        // Set Media control for volume
        setVolumeControlStream( AudioManager.STREAM_MUSIC );
        
        activity = this;
        fm = getSupportFragmentManager();
        
        
        // No need to continue if animations are disabled
        if ( checkSystemAnimationsDuration( this ) == 0 )
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder( this );
            
            dialog.setMessage( getString( R.string.anims_disabled ) );
            dialog.setPositiveButton( "Exit", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick( DialogInterface dialog, int which )
                {
                    finish();
                    System.exit( 1 );
                }
            } );
            
            //
            dialog.create().show();
            return;
        }
        
        
        //#################################
        //
        // Get the current system volume
        //
        //#################################
        AudioManager am = ( AudioManager ) getSystemService( AUDIO_SERVICE );
        if ( am != null )
        {
            int volume_level     = am.getStreamVolume( AudioManager.STREAM_MUSIC );
            int max_volume_level = am.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
            
            GameEngine.musicVolume = volume_level;
            GameEngine.sfxVolume = volume_level;
            GameEngine.maxVolume = max_volume_level;
        }
        
        // Create game engine instance, init Thread: MainThread
        gameEngine = GameEngine.getInstance( this );
        gameBoard = GameBoard.getInstance( this );
        
        //
        // Load saved game data, but do not reload it
        //
        if ( !gameEngine.prefsLoaded )
        {
            gameEngine.loadPrefData();
            gameEngine.prefsLoaded = true;
        }
        
        // Start the game
        SharedPreferences        prefs      = getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor     = prefs.edit();
        boolean                  showSplash = prefs.getBoolean( "show_splash", true );
        
        //############################
        //
        //
        //
        //############################
        if ( gameEngine.soundPlayer == null )
        {
            gameEngine.soundPlayer = new PlaySound( this, GameEngine.MAX_SFX_STREAMS );
        }
        
        // Load the game sounds to start
        gameEngine.soundPlayer.setMusic( R.array.game_music );
        gameEngine.soundPlayer.setSounds( R.array.game_start_efx );
        
        //
        //------------------------
        //
        // TODO: redelete for release
        showSplash = false;
        if ( !showSplash )
        {
            View logo     = findViewById( R.id.logoBg );
            View star     = findViewById( R.id.logoStar );
            View presents = findViewById( R.id.presenstText );
            
            logo.setAlpha( 0 );
            star.setAlpha( 0 );
            presents.setAlpha( 0 );
            
            if ( GameEngine.getPlayerAge( this ) == 0 )
            {
                processAgeCheck();
            }
            else
            {
                startGameMenu();
            }
        }
        else
        {
            View frame    = findViewById( R.id.fragment_container );
            View logo     = findViewById( R.id.logoBg );
            View star     = findViewById( R.id.logoStar );
            View presents = findViewById( R.id.presenstText );
            
            // Start the engine display the splash page
            frame.setAlpha( 0f );
            
            //
            //###########################
            //
            /*            presents.animate().alpha( 1f ).setDuration( 2000 ).start();*/
            star.animate().alpha( 1f ).rotation( 720 ).setDuration( 20000 ).start();
            frame.animate().alpha( 1f ).setDuration( 2000 ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    //
                    logo.setVisibility( View.VISIBLE );
                    
                    star.animate().setDuration( 1000 ).setStartDelay( 2000 ).alpha( 0f );
                    presents.animate().setDuration( 1000 ).setStartDelay( 2000 ).alpha( 0f );
                    logo.animate().setDuration( 1000 ).setStartDelay( 2000 ).alpha( 0f );
                    logo.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( GameEngine.getPlayerAge( MainActivity.this ) == 0 )
                            {
                                processAgeCheck();
/*
                                AgeVerify ager = new AgeVerify();
                                ager.setCancelable( false );
                                ager.show( MainActivity.this.getSupportFragmentManager(), "Age Verify" );
                                ager.setOnDismisser( new AgeVerify.OnDismisser()
                                {
                                    @Override
                                    public void onDismiss( int playerAge, boolean response )
                                    {
                                        GameEngine.setPlayerAge( MainActivity.this, playerAge );
                                        
                                        if ( response && playerAge > 8 )
                                        {
                                            startGameMenu();
                                        }
                                        else if ( response )
                                        {
                                            GameEngine.setPlayerAge( MainActivity.this, 0 );
                                            Toast.makeText( activity, "Sorry, but you need to get parent permission.", Toast.LENGTH_SHORT ).show();
                                        }
                                        else
                                        {
                                            finish();
                                        }
                                    }
                                } );
*/
                            }
                            else
                            {
                                startGameMenu();
                            }
                        }
                    } ).start();
                }
            } ).start();
        }
        
        // No more banner after the first showing
        //        editor.putBoolean( "show_splash", false );
        //editor.putBoolean( "show_splash", true );
        editor.commit();
    }
    
    
    /**
     * //############################
     * <p>
     * Get age check and process if
     * the user can play
     * <p>
     * //############################
     */
    private void processAgeCheck()
    {
        AgeVerify ager = new AgeVerify();
        ager.setCancelable( false );
        ager.show( getSupportFragmentManager(), "Age Verify" );
        ager.setOnDismisser( new AgeVerify.OnDismisser()
        {
            @Override
            public void onDismiss( int playerAge, boolean response )
            {
                GameEngine.setPlayerAge( MainActivity.this, playerAge );
                //GameEngine.setPlayerAge( MainActivity.this, 0 );
                
                if ( response && playerAge > 8 )
                {
                    startGameMenu();
                }
                else if ( response )
                {
                    GameEngine.setPlayerAge( MainActivity.this, 0 );
                    Toast.makeText( activity, "Sorry, but you need to get parent permission to play.", Toast.LENGTH_SHORT ).show();
                    finish();
                }
                else
                {
                    finish();
                }
            }
        } );
    }
    
    
    public Activity getActivity()
    {
        return activity;
    }
    
    /**
     * //############################
     * <p>
     * Check to see if animations
     * are disabled
     * <p>
     * //############################
     *
     * @return
     */
    public static float checkSystemAnimationsDuration( Context context )
    {
        return Settings.Global.getFloat( context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1 );
    }
    
    
    /**
     * //############################
     * <p>
     * Main Menu initiation
     * Assumes screen is dark black
     * <p>
     * //############################
     */
    private void startGameMenu()
    {
        // Transition to Game Menu
        FragmentLoader.SwapFragment( this, this, new GameStartFragment(), FragmentLoader.REPLACE_FRAGMENT );
    }
    
    
    /**
     * //############################
     * <p>
     * Volume controls, etc
     * <p>
     * //############################
     *
     * @param keyCode
     * @param event
     *
     * @return
     */
    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        if ( (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP) )
        {
            AudioManager am = ( AudioManager ) getSystemService( AUDIO_SERVICE );
            
            if ( am != null )
            {
                int volume_level = am.getStreamVolume( AudioManager.STREAM_MUSIC );
                
                GameEngine.musicVolume = volume_level;
                GameEngine.sfxVolume = volume_level;
            }
        }
        else
        {
            return super.onKeyUp( keyCode, event );
        }
        
        return true;
    }
    
    
    /**
     * //############################
     * <p>
     * Back track
     * <p>
     * //############################
     */
    @Override
    public void onBackPressed()
    {
        if ( getSupportFragmentManager().getBackStackEntryCount() == 0 )
        {
            if ( (backPressed + 2000) > System.currentTimeMillis() )
            {
/*
                // Kill the sound engine
                gameEngine.soundPlayer.destroyAll();
                GameEngine.mRewardedVideoAd.destroy(this);
*/
                //
                finish();
                System.exit( 1 );
            }
            else
            {
                Toast.makeText( getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT ).show();
            }
            //
            backPressed = System.currentTimeMillis();
        }
        else
        {
            getSupportFragmentManager().popBackStack();
        }
    }
    
    
    /**
     * //#################################
     * <p>
     * Close the app and exit
     * <p>
     * //#################################
     */
    @Override
    public void finish()
    {
        super.finish();
        
        if ( gameEngine != null )
        {
            gameEngine.savePrefData();
        }
    }
    
    
    @Override
    protected void onRestart()
    {
        resumeGameStatus();
        super.onRestart();
    }
    
    
    @Override
    protected void onStop()
    {
        pauseGameStatus();
        super.onStop();
    }
    
    
    @Override
    protected void onDestroy()
    {
        // Kill the sound engine
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.destroyAll();
        }
        
        //
        GameEngine.mRewardedVideoAd.destroy( this );
        //
        super.onDestroy();
    }
    
    
    /**
     * //#################################
     * <p>
     * Handle pausing the game
     * <p>
     * //#################################
     */
    private void pauseGameStatus()
    {
        // Save existing data
        if ( logicThread != null )
        {
            logicThread.saveLogic();
        }
        
        
        //##############################
        //
        // Kill the timer
        //
        //##############################
        if ( GameEngine.timer != null )
        {
            // Clean up
            gameEngine.animatorList.remove( GameEngine.timer );
            //            timer.removeAllListeners();
            GameEngine.timer.setUserCancel( CustomTimer.PAUSE_CANCEL );
            GameEngine.timer.cancel();
            GameEngine.timer = null;
            
/*
            if ( GameEngine.lastGamePlayed == 0 )
            {
                try
                {
                    ConnectionsGridLayout grid = findViewById( R.id.boardGridLayout );
                    grid.setTimer( null );
                }
                catch ( Exception ex )
                {
                    ex.printStackTrace();
                    
                    try
                    {
                        CardsGridLayout grid = findViewById( R.id.boardGridLayout );
                        GameEngine.lastGamePlayed = 1;
                    }
                    catch ( Exception exc )
                    {
                        ex.printStackTrace();
                        Toast.makeText( activity, "Memory Error", Toast.LENGTH_SHORT ).show();
                        finish();
                        return;
                    }
                }
            }
*/
        }
        
        
        //#################################
        //
        // Pause all animations
        //
        //#################################
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
        
        
        //#################################
        //
        // Kill the thread
        //
        //#################################
        if ( logicThread != null )
        {
            logicThread.killThread();
            logicThread = null;
        }
        
        //
        if ( gameEngine.soundPlayer != null )
        {
            // Get current play status
            //            GameEngine.musicIsPlaying = gameEngine.soundPlayer.music.isPlaying();
            savedMusicStatus = GameEngine.musicIsPlaying;
            if ( savedMusicStatus )
            {
                GameEngine.musicPosition = gameEngine.soundPlayer.music.getCurrentPosition();
            }
            
            // Kill BG Music
            //@@@@@@@@@@@@@@@@@@@@@@ Start the associated BG Music
            if ( gameEngine.soundPlayer.music != null )
            {
                gameEngine.soundPlayer.playBgMusic( -1, PlaySound.STOP );
            }
            
            // MUST release sound resources
            gameEngine.soundPlayer.destroyAll();
            gameEngine.soundPlayer = null;
        }
        
        
        // Pause the Video Ad
        GameEngine.mRewardedVideoAd.pause( this );
    }
    
    
    /**
     * //#############################
     * <p>
     * Resume a currently played game
     * <p>
     * //#############################
     */
    private void resumeGameStatus()
    {
        boolean    gridType = (GameEngine.lastGamePlayed == 1);
        GridLayout grid     = findViewById( R.id.boardGridLayout );
        
        
        // ALWAYS create a new instance
        if ( logicThread == null )
        {
            logicThread = new LogicThread( getActivity() );
            
            if ( grid != null )
            {
                if ( grid instanceof CardsGridLayout )
                {
                    gridType = true;
                    GameEngine.lastGamePlayed = 1;
                }
                else
                {
                    gridType = false;
                    GameEngine.lastGamePlayed = 0;
                }
                
                logicThread.setGridLayout( grid );
            }
        }
        
        //
        // Start a new time to resume with
        //
        if ( GameEngine.timer == null && CustomTimer.currentTime > 0 )
        {
            // because it's save for static purpose only. once restore, removed
            // static refs to prevent leaks.
            GameEngine.timer = new CustomTimer( CustomTimer.currentTime, this, CustomTimer.target );
            GameEngine.timer.setOnCommunicator( null );
            GameEngine.timer.addListener( new Animator.AnimatorListener()
            {
                @Override
                public void onAnimationStart( Animator animation )
                {
                    logicThread.clearData();
                    logicThread.setHintTimer( LogicThread.PLAY_HINT );
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
            
            GameEngine.timer.start();
            
            // Place in pause status if the game was
            // paused at the time user left App
            if ( GameEngine.gamePaused )
            {
                GameEngine.timer.pause();
            }
            
            gameEngine.animatorList.add( GameEngine.timer );
        }
        
        //
        // Share the ref
        //
        if ( grid != null )
        {
            if ( gridType )
            {
                (( CardsGridLayout ) grid).setLogicThread( logicThread );
            }
            else
            {
                (( ConnectionsGridLayout ) grid).setLogicThread( logicThread );
                (( ConnectionsGridLayout ) grid).setTimer( GameEngine.timer );
            }
            //
            //            grid.setLogicThread( logicThread );
            logicThread.restoreLogic();
            // TODO: Remove arg
            logicThread.setHintTimer( LogicThread.PLAY_HINT );
        }
        
        
        
        //###########################
        //
        // Resume ONLY if game not in
        // paused status
        //
        //###########################
        if ( !GameEngine.gamePaused )
        {
            // Resume all tile animations
            if ( gameBoard != null && gameBoard.boardTiles != null && grid != null )
            {
                for ( int i = 0; i < gameBoard.boardTiles.size(); i++ )
                {
                    gameBoard.boardTiles.get( i ).pauseAnimation( false );
                }
            }
            
            // Resume all animations
            if ( gameEngine.animatorList != null )
            {
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
            }
        }
        
        
        //###########################
        //
        // Resume all Ads
        //
        //###########################
/*
        AdView adView = findViewById( R.id.adView );
        
        if ( adView != null )
        {
            adView.resume();
        }
*/
        
        //
        // SoundPool: Only on TRUE resume
        //
        if ( gameEngine.soundPlayer == null )
        {
            gameEngine.soundPlayer = new PlaySound( this, GameEngine.MAX_SFX_STREAMS, R.array.game_start_efx );
            gameEngine.soundPlayer.setMusic( R.array.game_music );
            //
            if ( savedMusicStatus )
            {
                gameEngine.soundPlayer.playBgMusic( gameEngine.currentBGMusic >> 2, PlaySound.LOOP );
                gameEngine.soundPlayer.music.seekTo( GameEngine.musicPosition );
            }
        }
        
        // Get the Video Ad back
        GameEngine.mRewardedVideoAd.resume( this );
    }
    
    
    /**
     * Cleat back stack
     */
    public static void removeBackStack()
    {
/*
        if ( fm != null )
        {
            int count = fm.getBackStackEntryCount();
            
            GameEngine.stackClearing = true;
            
            for ( int i = 0; i < count; ++i )
            {
                fm.popBackStackImmediate();
            }
            
            GameEngine.stackClearing = false;
        }
*/
    }
}
