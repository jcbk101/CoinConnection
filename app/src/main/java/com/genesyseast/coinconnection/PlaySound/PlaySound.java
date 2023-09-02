package com.genesyseast.coinconnection.PlaySound;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.AnimationValues;

import java.io.IOException;
import java.util.ArrayList;

public class PlaySound
        implements MediaPlayer.OnErrorListener
{
    public static final int           BOMB_EXPAND                          = 0;
    public static final int           BOMB_SPECIAL                         = 1;
    public static final int           BUTTON_CLICK                         = 2;
    public static final int           CHARGE_BEAM_PART_1                   = 3;
    public static final int           CHARGE_BEAM_PART_2                   = 4;
    public static final int           CHARGE_BEAM_SPECIAL                  = 5;
    public static final int           COIN                                 = 6;
    public static final int           COIN_MATCH                           = 7;
    public static final int           COIN_REVERSE                         = 8;
    public static final int           DIALOG_CLICK                         = 9;
    public static final int           ERROR                                = 10;
    public static final int           INVALID                              = 11;
    public static final int           LEVEL_COMPLETE_PART_1                = 12;
    public static final int           LEVEL_COMPLETE_PART_2                = 13;
    public static final int           LEVEL_FAIL                           = 14;
    public static final int           LEVEL_SELECT                         = 15;
    public static final int           LIGHTNING_SPECIAL                    = 16;
    public static final int           LOGO_REVEAL                          = 17;
    public static final int           PAUSE_GAME                           = 18;
    public static final int           POINTS_ADD                           = 19;
    public static final int           POINTS_ADD_LOOP                      = 20;
    public static final int           POP                                  = 21;
    public static final int           QUIT_GAME                            = 22;
    public static final int           RANDOM_SPECIAL                       = 23;
    public static final int           REEL_STOP_1                          = 24;
    public static final int           REEL_STOP_2                          = 25;
    public static final int           REEL_STOP_WIN_3                      = 26;
    public static final int           SLOT_ARM                             = 27;
    public static final int           SLOTS_FREE_SPINS                     = 28;
    public static final int           SLOTS_LOSE_GAME                      = 29;
    public static final int           SLOTS_PRIZE_ADD                      = 30;
    public static final int           SLOTS_REEL_TICK                      = 31;
    public static final int           SLOTS_WIN_GAME                       = 32;
    public static final int           SPARK_SPECIAL                        = 33;
    public static final int           SPECIAL_CREATED                      = 34;
    public static final int           SPECIAL_MATCHED                      = 35;
    public static final int           SPECIAL_TOUCH                        = 36;
    public static final int           STAR_LEVEL_UP                        = 37;
    public static final int           STAR_SHOOT                           = 38;
    public static final int           START_GAME                           = 39;
    public static final int           TOAST_ALERT                          = 40;
    public static final int           UNPAUSE_GAME                         = 41;
    //
    public static final int           BEACH_BG_BLACK_DIAMOND               = 0;
    public static final int           FAIRYTALE_BG_SLEEP_TALKING           = 1;
    public static final int           FOREST_BG_MYSTERIOUS_AMBIENCE_SONG21 = 2;
    public static final int           HALLOWEEN_BG_CREEP                   = 3;
    public static final int           NIGHTTIME_BG_EMOTIONAL_DELUGE        = 4;
    public static final int           OCEAN_BG_BLUE_MEADOW_IN_GREEN_SKY    = 5;
    public static final int           WINTER_BG_WINTER_DUST                = 6;
    public static final int           GAME_COMPLETE_BG_HAPPY_ADVETURE      = 7;
    public static final int           MENU_BG_GONE_FISHIN_BY_MEMORAPHILE   = 8;
    public static final int           SLOTS_BG_SOMEWHERE_IN_THE_ELEVATOR   = 9;
    //
    public static final int           PLAY                                 = 0;
    public static final int           LOOP                                 = 1;
    public static final int           STOP                                 = -1;
    //
    private             int           maxStreams;
    public              int[]         soundEffects;
    public              int[]         bgMusic;
    private             Context       context;
    public static       int           currentSoundList                     = 0;
    public static       int[]         sfxStatus;
    private             MediaPlayer[] sfx;
    public              MediaPlayer   music;
    public              int           soundEfxcount                        = 0;
    private             int           SFX_MAX                              = 8;
    
    
    /**
     * Listener interface
     */
    public interface OnSoundsLoadedListener
    {
        void onSoundsLoaded();
    }
    
    /**
     * Main constructor
     */
    public PlaySound( Context context, int maxStreams )
    {
        this.context = context;
        this.maxStreams = maxStreams;
        this.soundEffects = null;
    }
    
    
    /**
     * Second constructor
     *
     * @param maxStreams
     * @param soundEfx
     */
    public PlaySound( Context context, int maxStreams, int soundEfx )
    {
        this.context = context;
        this.maxStreams = maxStreams;
        
        // load the files into memory
        setSounds( soundEfx );
    }
    
    
    /**
     * Get the music play list
     *
     * @param musicList
     *
     * @return
     */
    public int setMusic( int musicList )
    {
        if ( musicList > 0 && context != null )
        {
            // Now load all the goodies
            TypedArray arrays;
            
            
            //############################
            // Map Sizes
            //############################
            arrays = context.getResources().obtainTypedArray( musicList );
            bgMusic = new int[ arrays.length() ];
            
            // Referrences to RAW sound files
            for ( int i = 0; i < arrays.length(); i++ )
            {
                this.bgMusic[ i ] = arrays.getResourceId( i, 0 );
            }
            
            arrays.recycle();
            
            
            return bgMusic.length;
        }
        
        return 0;
    }
    
    
    /**
     * Array of sound effects to load
     *
     * @param soundEfx
     */
    public int setSounds( int soundEfx )
    {
        if ( soundEfx > 0 && context != null )
        {
            // Now load all the goodies
            TypedArray arrays;
            
            
            //############################
            // Map Sizes
            //############################
            arrays = context.getResources().obtainTypedArray( soundEfx );
            soundEffects = new int[ arrays.length() ];
            //
            sfx = new MediaPlayer[ SFX_MAX ];
            sfxStatus = new int[ SFX_MAX ];
            
            
            // Referrences to RAW sound files
            for ( int i = 0; i < arrays.length(); i++ )
            {
                this.soundEffects[ i ] = arrays.getResourceId( i, 0 );
            }
            
            //
            for ( int i = 0; i < sfx.length; i++ )
            {
                sfxStatus[ i ] = -1;
            }
            
            
            arrays.recycle();
            
            // Save for onPause(), onResume()
            currentSoundList = soundEfx;
            
            return soundEffects.length;
        }
        
        return 0;
    }
    
    
    /**
     * Play the sound requested
     *
     * @param soundIndex
     */
    public int play( int soundIndex )
    {
        return play( soundIndex, PLAY, 0 );
    }
    
    
    /**
     * Play the sound requested
     *
     * @param soundIndex
     */
    public int play( int soundIndex, int soundType )
    {
        return play( soundIndex, soundType, 0 );
    }
    
    
    /**
     * Play the sound effect using SoundPool requested
     *
     * @param soundIndex
     */
    public int play( int soundIndex, int soundType, int streamId )
    {
        int index = 0;
        
        if ( (GameEngine.systemFlags & GameEngine.SFX_OFF) == 0 && sfx != null )
        {
            try
            {
                if ( soundType == STOP )
                {
                    if ( sfxStatus[ streamId ] != -1 )
                    {
                        sfx[ streamId ].stop();
                        sfx[ streamId ].release();
                        sfxStatus[ streamId ] = -1;
                    }
                }
                else
                {
                    for ( index = 0; index < sfx.length; index++ )
                    {
                        if ( sfxStatus[ index ] == -1 )
                        {
                            break;
                        }
                    }
                    
                    
                    // If we are maxed out, just leave
                    if ( index >= sfx.length )
                    {
                        return 0;
                    }
                    
                    
/*
                    // All sounds are listed
                    Uri uri = Uri.parse( "android.resource://com.genesyseast.coinconnection/" + soundEffects[ soundIndex ] );
                    sfx[ index ] = new MediaPlayer();
                    sfx[ index ].setAudioStreamType(AudioManager.STREAM_MUSIC);
                    sfx[ index ].setLooping( (soundType == LOOP) );
                    sfx[ index ].setDataSource( uri.pa );
                    sfx[index].setOnPreparedListener( new MediaPlayer.OnPreparedListener()
                    {
                        @Override
                        public void onPrepared( MediaPlayer mp )
                        {
                            mp.start();
                        }
                    } );
                    sfx[ index ].prepareAsync(); // might take long! (for buffering, etc)
*/
                    
                    //                    sfx[ index ] = MediaPlayer.create( context, soundEffects[ soundIndex ] );
                    sfx[ index ] = createAsync( context, soundEffects[ soundIndex ] );
                    if ( sfx[ index ] != null )
                    {
                        sfx[ index ].setLooping( (soundType == LOOP) );
                        sfxStatus[ index ] = sfx[ index ].hashCode();
                        sfx[ index ].setOnPreparedListener( new MediaPlayer.OnPreparedListener()
                        {
                            @Override
                            public void onPrepared( MediaPlayer mp )
                            {
                                mp.start();
                            }
                        } );
                        //                        sfx[ index ].start();
                        sfx[ index ].setOnCompletionListener( mp -> {
                            // Android 6.0 will not loop!
                            if ( mp.isLooping() )
                            {
                                mp.start();
                            }
                            else
                            {
                                mp.release();
                                for ( int i = 0; i < sfxStatus.length; i++ )
                                {
                                    if ( mp.hashCode() == sfxStatus[ i ] )
                                    {
                                        sfxStatus[ i ] = -1;
                                        break;
                                    }
                                }
                            }
                        } );
                        //
                        sfx[ index ].setOnErrorListener( this );
                    }
                }
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
        }
        
        return index;
    }
    
    
    /**
     * Play background music
     *
     * @param soundIndex
     * @param soundType
     *
     * @return
     */
    public int playBgMusic( int soundIndex, int soundType )
    {
        if ( ((GameEngine.systemFlags & GameEngine.MUSIC_OFF) == 0 || soundType == STOP) && bgMusic != null )
        {
            try
            {
                if ( soundType == STOP )
                {
                    if ( music != null )
                    {
                        music.stop();
                        music.release();
                        GameEngine.musicIsPlaying = false;
                    }
                }
                else
                {
                    // All sounds are listed
                    music = MediaPlayer.create( context, bgMusic[ soundIndex ] );
                    //                    music.setLooping( (soundType == LOOP) );
                    music.start();
                    
                    music.setOnCompletionListener( mp -> {
                        // Android 6.0 will not loop!
                        if ( mp.isLooping() )
                        {
                            mp.start();
                        }
                        else
                        {
                            mp.stop();
                            mp.release();
                            GameEngine.musicIsPlaying = false;
                        }
                    } );
                    music.setOnErrorListener( this );
                    music.setLooping( (soundType == LOOP) );
                    
                    GameEngine.musicIsPlaying = true;
                }
            }
            catch ( Exception ex )
            {
                ex.getMessage();
            }
        }
        
        return 0;
    }
    
    
    /**
     * //################################
     * <p>
     * Fade music out method
     * <p>
     * //################################
     */
    public ValueAnimator fadeOutMusic()
    {
        ValueAnimator fadeOut    = ValueAnimator.ofInt( GameEngine.maxVolume, 0 );
        GameEngine    gameEngine = GameEngine.getInstance( context );
        
        //
        if ( music != null && GameEngine.musicIsPlaying )
        {
            fadeOut.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    int volume = ( int ) animation.getAnimatedValue();
                    
                    if ( music != null )
                    {
                        try
                        {
                            music.setVolume( ( float ) volume / ( float ) GameEngine.maxVolume, ( float ) volume / ( float ) GameEngine.maxVolume );
                        }
                        catch ( IllegalStateException ise )
                        {
                            ise.printStackTrace();
                        }
                    }
                }
            } );
            fadeOut.setDuration( AnimationValues.FADE_TIME ).addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    if ( music != null )
                    {
                        float volume = ( float ) (GameEngine.musicVolume / GameEngine.maxVolume);
                        
                        try
                        {
                            music.stop();
                            music.release();
                            GameEngine.musicIsPlaying = false;
                        }
                        catch ( IllegalStateException ise )
                        {
                            ise.printStackTrace();
                        }
                        //                    music.setVolume( volume, volume );
                    }
                }
            } );
        }
        
        //
        fadeOut.start();
        gameEngine.animatorList.add( fadeOut );
        
        return fadeOut;
    }
    
    
    /**
     * For music only
     *
     * @param soundIndex
     *
     * @return
     */
    public int playBgSfx( int soundIndex )
    {
        return playBgSfx( soundIndex, PLAY );
    }
    
    public int playBgSfx( int soundIndex, int soundType )
    {
        return play( soundIndex, soundType, 0 );
    }
    
    
    /**
     * Release to sound player
     */
    public void destroyAll()
    {
        if ( music != null )
        {
            music.release();
            music = null;
        }
        
        if ( sfx != null )
        {
            for ( MediaPlayer mp : sfx )
            {
                if ( mp != null )
                {
                    mp.release();
                }
            }
            
            sfx = null;
        }
    }
    
    
    @Override
    public boolean onError( MediaPlayer mp, int what, int extra )
    {
        return false;
    }
    
    
    /**
     * //#################################
     * <p>
     * Custom creator that allows Async
     * creation
     * <p>
     * //#################################
     *
     * @param context
     * @param resid
     *
     * @return
     */
    public static MediaPlayer createAsync( Context context, int resid )
    {
        try
        {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd( resid );
            if ( afd == null )
            {
                return null;
            }
            
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength() );
            
            afd.close();
            mp.prepareAsync();
            
            return mp;
        }
        catch ( IOException | IllegalArgumentException | SecurityException ex )
        {
            Log.d( "PlaySound", "create failed:", ex );
            // fall through
        }
        
        return null;
    }
}
