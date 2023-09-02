package com.genesyseast.coinconnection.GameEngine;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.widget.Toast;

import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.Variables.PointXYZ;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class GameEngine
        implements RewardedVideoAdListener
{
    // Set for Debug / Clear for Release
    public static boolean debugMode;
    
    // Data for SYSTEM_FLAGS
    public static final int                      MUSIC_OFF              = 1;
    public static final int                      SFX_OFF                = 2;
    public static final int                      SWIPE_DOWN             = 4;
    public static final int                      HINT_OFF               = 8;
    public static final int                      MATCH_NUM_OFF          = 16;
    public static final int                      SELECT_CHEST           = 32;
    // Data for Level select data
    public static final int                      MAX_LEVEL_PER_SELECTOR = 12;
    public static final int                      CARD_GAME_LEVEL        = 10;
    public static final int                      MAX_LEVELS             = 1500;
    public static final int                      MAX_STAR_LEVELS        = MAX_LEVELS >> 2;
    public static final int                      MAX_BOOSTERS           = 5;
    public static final int                      BOOSTERS_CAPACITY      = 99;
    public static final int                      MAX_LEVEL_COLORS       = MAX_LEVELS;
    public static final int                      POINTS_PER_TILE        = 50;
    public static final int                      POINTS_PER_GEM         = 100;
    public static       boolean                  reloadSameGame         = false;
    public static       boolean                  penaltyMode            = false;
    public static       int                      lastGamePlayed         = 0;
    // Main instance
    private static      GameEngine               instance;
    public static       long                     systemFlags;
    public static       boolean                  gamePaused             = false;
    public static       boolean                  isKilled               = false;
    public static       int                      maxVolume              = 0;
    public static       int                      musicPosition          = 0;
    //
    public static       int                      boardChange            = 0;
    //
    public static       int                      pointsMultiplier       = 1;
    public static       int                      sfxVolume              = 0;
    public static       int                      musicVolume            = 0;
    // Values stored in SharedPrefs
    public static       int                      currentLevel           = 0;
    public static       int                      reloadedLevel          = -1;
    public static       int                      currentBoardImage      = 0;
    public static       int                      currentBackground      = 0;
    public static       int                      boardMoves             = 0;
    //
    public static       boolean                  rewardGiven            = false;
    //
    public static       boolean                  resumeGame;
    //
    // Each level holds 32-Bits.
    // (32 / 3 = 10.6) or 10 star segments per INTEGER with 2 bits remaining
    // to be used for the next star segment
    // 10 INTS x 32 BITS = 320. 320 / 3 bits for stars = 106.6 compressed (star) level bits
    public static       byte[]                   Level                  = new byte[ MAX_STAR_LEVELS + 1 ];
    public static       byte[]                   prizeStatus            = new byte[ (MAX_LEVELS / MAX_LEVEL_PER_SELECTOR) + 1 ];
    //
    public              byte[]                   Boosters               = new byte[ MAX_BOOSTERS ];
    // Default: one minute
    //public  long    timeAllowed    = 900000;
    public              long                     timeAllowed            = (5 * 60000); // 5 minutes
    public              boolean                  specialRunning         = false;
    public              boolean                  prefsLoaded            = false;
    // Common variables
    private             Context                  context;
    // Holds a list of animations to pause / unpause that are NOT BoardTiles
    public              ArrayList<ValueAnimator> animatorList;
    // Add a TAG to the class for whatever
    private static      Object                   tag;
    // Sound API is !!!!!!!!
    public              PlaySound                soundPlayer;
    public static final int                      MAX_SFX_STREAMS        = 10;
    //
    public              int                      currentBGMusic         = 0;
    public static       boolean                  musicIsPlaying         = false;
    //
    // Video Ad support
    // TODO: find replacement for this
    public static       RewardedVideoAd          mRewardedVideoAd;
    public static       InterstitialAd           mInterstitialAd;
    public static       boolean                  noMoreAds;
    // Ad return codes
    public static final int                      AD_LOADED              = 0;
    public static final int                      AD_OPENED              = 1;
    public static final int                      AD_STARTED             = 2;
    public static final int                      AD_CLOSED              = 3;
    public static final int                      AD_REWARDED            = 4;
    public static final int                      AD_LEFT_APP            = 5;
    public static final int                      AD_FAILED_LOAD         = 6;
    public static final int                      AD_COMPLETED           = 7;
    //
    private             OnAdReturnListener       onAdReturnListener;
    public static       ErrorDialog              savedDialog;
    public static       CustomTimer              timer;
    //
    // TODO reset all for real game run. Loaded from SharedPrefs XML
    public static       int                      bonusCount             = 5;
    public static       int                      moneyOnHand            = 0;
    public static       int                      mailOnHand             = 3;
    public static       int                      movesOnHand            = 5;
    public static       boolean                  giftWaiting            = true;
    
    
    public interface OnAdReturnListener
    {
        void onAdReturn( int status, int errorFlag );
    }
    
    /**
     * Must callback for simplified listening
     *
     * @param onAdReturnListener
     */
    public void setOnAdReturnListener( OnAdReturnListener onAdReturnListener )
    {
        this.onAdReturnListener = onAdReturnListener;
    }
    
    
    /**
     * Enable a single instance only
     *
     * @param context
     */
    private GameEngine( Context context )
    {
        this.context = context;
        animatorList = new ArrayList<>();
        soundPlayer = new PlaySound( context, MAX_SFX_STREAMS );
        
        try
        {
            // Get the video ads going
            // Currently using DEBUG code
            if ( !GameEngine.debugMode )
            {
                // Supports for children
                RequestConfiguration conf;
                conf = new RequestConfiguration.Builder().setTagForChildDirectedTreatment( TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE ).build();
                MobileAds.setRequestConfiguration( conf );
                
                // Actual Ads
                MobileAds.initialize( context, new OnInitializationCompleteListener()
                {
                    @Override
                    public void onInitializationComplete( InitializationStatus initializationStatus )
                    {
                    }
                } );
                
                mInterstitialAd = new InterstitialAd( context );
                mInterstitialAd.setAdUnitId( "ca-app-pub-7978336361271355/1417424924" );
            }
            else
            {
                List<String> testDevices = new ArrayList<>();
                testDevices.add( AdRequest.DEVICE_ID_EMULATOR );
                
                RequestConfiguration requestConfiguration = new RequestConfiguration.Builder().setTestDeviceIds( testDevices ).build();
                
                //            MobileAds.initialize( context, "ca-app-pub-3940256099942544~3347511713" );
                MobileAds.initialize( context, new OnInitializationCompleteListener()
                {
                    @Override
                    public void onInitializationComplete( InitializationStatus initializationStatus )
                    {
                    }
                } );
                
                mInterstitialAd = new InterstitialAd( context );
                mInterstitialAd.setAdUnitId( "ca-app-pub-3940256099942544/1033173712" );
            }
            
            //FIND A WAY TO SUPPORT INTERSTITIAL ADS WITH A ONCLOSE LITTSENER
            
            // Use an activity context to get the rewarded video instance.
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance( context );
            mRewardedVideoAd.setRewardedVideoAdListener( this );
            
            // Get an Ad loaded
            loadRewardedVideoAd( context );
            loadInterstitialAd( context );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * ################################
     * <p>
     * Strictly for the video Ad format
     * <p>
     * ################################
     */
    /**
     * Load the Ads
     */
    public static void loadRewardedVideoAd( Context context )
    {
        AdRequest adRequest;
        Bundle    extras = new Bundle();
        extras.putString( "max_ad_content_rating", "G" );
        
        // Currently using DEBUG code
        if ( !GameEngine.debugMode )
        {
            // Actual Ads
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
            mRewardedVideoAd.loadAd( "ca-app-pub-7978336361271355/2123328513", adRequest );
        }
        else
        {
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
            mRewardedVideoAd.loadAd( "ca-app-pub-3940256099942544/5224354917", adRequest );
        }
    }
    
    
    /**
     * @param context
     */
    public static void loadInterstitialAd( Context context )
    {
        AdRequest adRequest;
        Bundle    extras = new Bundle();
        extras.putString( "max_ad_content_rating", "G" );
        
        // Currently using DEBUG code
        if ( !GameEngine.debugMode )
        {
            // Actual Ads
            if ( !mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded() )
            {
                adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
                mInterstitialAd.loadAd( adRequest );
            }
        }
        else
        {
            if ( !mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded() )
            {
                adRequest = new AdRequest.Builder().addNetworkExtrasBundle( AdMobAdapter.class, extras ).build();
                mInterstitialAd.loadAd( adRequest );
            }
        }
    }
    
    /**
     * Return the current instance if it exist
     *
     * @param context
     *
     * @return
     */
    public static synchronized GameEngine getInstance( Context context )
    {
        if ( instance == null )
        {
            instance = new GameEngine( context );
        }
        
        return instance;
    }
    
    public static void setTag( Object tag )
    {
        GameEngine.tag = tag;
    }
    
    public static Object getTag()
    {
        return tag;
    }
    
    
    /**
     * //##############################
     * <p>
     * Save all game data to the
     * Shared Preferences
     * <p>
     * //##############################
     */
    public void savePrefData()
    {
        SharedPreferences        prefs;
        SharedPreferences.Editor editor;
        
        if ( context != null )
        {
            prefs = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            // System flags
            editor.putLong( "system_flags", systemFlags );
            
            //
            StringBuilder special_items = new StringBuilder();
            StringBuilder levels        = new StringBuilder();
            StringBuilder prize         = new StringBuilder();
            
            //
            // Every 2 Bits = 1 level ( 2 bits = 3 stars )
            //
            for ( int i = 0; i < MAX_STAR_LEVELS; i++ )
            {
                if ( (i + 1) < MAX_STAR_LEVELS )
                {
                    levels.append( Level[ i ] ).append( "," );
                }
                else
                {
                    levels.append( Level[ i ] );
                }
            }
            
            // Prize status: bits set for each prize  claimed
            // Total of 12 bits per section for now
            for ( int i = 0; i < prizeStatus.length; i++ )
            {
                if ( (i + 1) < prizeStatus.length )
                {
                    prize.append( prizeStatus[ i ] ).append( "," );
                }
                else
                {
                    prize.append( prizeStatus[ i ] );
                }
            }
            
            // Boosters
            for ( int i = 0; i < MAX_BOOSTERS; i++ )
            {
                if ( (i + 1) < MAX_BOOSTERS )
                {
                    special_items.append( Boosters[ i ] ).append( "," );
                }
                else
                {
                    special_items.append( Boosters[ i ] );
                }
            }
            
            //
            editor.putString( "special_items", special_items.toString() );
            editor.putString( "zone_levels", levels.toString() );
            editor.putString( "prize_status", prize.toString() );
            editor.putInt( "current_level", currentLevel );
            editor.putInt( "reloaded_level", reloadedLevel );
            editor.putInt( "skrilla", moneyOnHand );
            editor.putBoolean( "picnic_basket", giftWaiting );
            editor.putBoolean( "got_skrilla", noMoreAds );
            
            
            //Remove old stuff
            editor.remove( "high_scores" );
            editor.remove( "specials_capacity" );
            editor.remove( "upgrade_days_to_wait" );
            editor.remove( "boosters_upgrade" );
            editor.remove( "received_help_screen" );
            
            //
            editor.commit();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Load all shared pref data
     * <p>
     * //##############################
     */
    public void loadPrefData()
    {
        int                      DAYS_UNTIL_PROMPT = 1;
        long                     launch_count;
        long                     date_firstLaunch;
        long                     time_to_wait;
        SharedPreferences        prefs             = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor            = prefs.edit();
        int                      i                 = 0;
        Random                   r                 = new Random();
        
        
        // Load system flags
        systemFlags = prefs.getLong( "system_flags", 0 );
        
        //
        moneyOnHand = prefs.getInt( "skrilla", 0 );
        //        giftWaiting = prefs.getBoolean( "picnic_basket", false );
        giftWaiting = prefs.getBoolean( "picnic_basket", true );
        noMoreAds = prefs.getBoolean( "got_skrilla", false );
        
        //
        String special_items = prefs.getString( "special_items", "" );
        String level_data    = prefs.getString( "zone_levels", "" );
        String prize_data    = prefs.getString( "prize_status", "" );
        
        
        String[] levels       = level_data.split( "," );
        String[] specialItems = special_items.split( "," );
        String[] prizes       = prize_data.split( "," );
        
        //
        // Each bit represents a prize claimed for that level
        //
        for ( i = 0; i < prizeStatus.length; i++ )
        {
            if ( prizes.length > i && prizes[ i ] != null && !prizes[ i ].equals( "" ) )
            {
                prizeStatus[ i ] = Byte.parseByte( prizes[ i ].trim() );
            }
            else
            {
                prizeStatus[ i ] = 0;
            }
        }
        
        
        // Special items
        i = 0;
        for ( String nums : specialItems )
        {
            if ( i >= MAX_BOOSTERS )
            {
                break;
            }
            
            //
            if ( nums != null && !nums.equals( "" ) )
            {
                Boosters[ i ] = Byte.parseByte( nums.trim() );
                
                if ( Boosters[ i ] > BOOSTERS_CAPACITY )
                {
                    Boosters[ i ] = BOOSTERS_CAPACITY;
                }
                else if ( Boosters[ i ] < 0 )
                {
                    Boosters[ i ] = 0;
                }
                
                i++;
            }
            else
            {
                Boosters[ i++ ] = 0;
            }
        }
        
        
        //##############################
        //
        // Get the proper level and
        // convert if not done already
        //
        //##############################
        if ( !prefs.getBoolean( "level_converted", false ) )
        {
            byte[] temp  = new byte[ levels.length ];
            int    level = 0;
            
            // Each level is actually a Zone of 18 levels
            for ( i = 0; i < levels.length; i++ )
            {
                if ( levels[ i ] != null && !levels[ i ].equals( "" ) )
                {
                    temp[ i ] = Byte.parseByte( levels[ i ].trim() );
                }
                else
                {
                    temp[ i ] = 0;
                }
            }
            
            // Ever 4 levels is a New level block of 4 -> 2-Bit sets
            currentLevel = 0;
            
            for ( i = 0; i < Level.length; i++ )
            {
                // Example : Area 1 = 18 levels completed
                // So 18 levels to convert
                level = temp[ i ];
                
                if ( level > 0 )
                {
                    for ( int c = 0; c < level; c++ )
                    {
                        setStarCountFromLevel( currentLevel, 3 );
                        currentLevel++;
                        
                        // Can't begreater than the max!
                        if ( currentLevel >= Level.length )
                        {
                            break;
                        }
                    }
                }
                else
                {
                    break;
                }
            }
            
            //
            // Save info
            //
            editor.putBoolean( "level_converted", true );
            editor.commit();
            
            //
            temp = null;
            savePrefData();
        }
        else
        {
            // Each level is actually a Block of 4 -> 2-Bit sets
            for ( i = 0; i < levels.length; i++ )
            {
                if ( i >= Level.length )
                {
                    break;
                }
                
                if ( levels[ i ] != null && !levels[ i ].equals( "" ) )
                {
                    Level[ i ] = Byte.parseByte( levels[ i ].trim() );
                }
                else
                {
                    Level[ i ] = 0;
                }
            }
            
            // Just starting the game if Level = 0
            currentLevel = prefs.getInt( "current_level", 0 );
            reloadedLevel = prefs.getInt( "reloaded_level", -1 );
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the stars earn for a
     * particular level
     * <p>
     * //##############################
     *
     * @param level
     *
     * @return
     */
    public static int getStarCountFromLevel( int level )
    {
        // GameEngine.Level[ 0 ] = 8 bits
        // 11 01 10 00 { 3, 1, 2, 0 stars earned: levels 1 - 4 }
        
        // Each index into GE.Level is 8 bits. Divide level
        // by 4 to get the Base level
        int levelIndex = (level >> 2);
        
        // Each level holds 4 -> 2-Bit patterns
        int bitIndex = (level & 0x03) << 1;
        
        // Return the bits requested
        return (Level[ levelIndex ] >> bitIndex) & 0x03;
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the stars earn for a
     * particular level
     * <p>
     * //##############################
     *
     * @param level
     *
     * @return
     */
    public static void setStarCountFromLevel( int level, int starCount )
    {
        // GameEngine.Level[ 0 ] = 8 bits
        // 11 01 10 00 { 3, 1, 2, 0 stars earned: levels 1 - 4 }
        
        // Each index into GE.Level is 8 bits. Divide level
        // by 4 to get the Base level
        int levelIndex = (level >> 2);
        
        // Each level holds 4 -> 2-Bit patterns
        int bitIndex = (level & 0x03) << 1;
        
        if ( starCount < 0 )
        {
            starCount = 0;
        }
        
        // Store the bits requested
        Level[ levelIndex ] |= (starCount << bitIndex);
    }
    
    
    /**
     * //##############################
     * <p>
     * Set booster move help
     * <p>
     * //##############################
     *
     * @param context
     * @param boosterMove
     */
    public static void setBoosterHelper( Context context, boolean boosterMove )
    {
        SharedPreferences        prefs  = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = prefs.edit();
        
        //
        editor.putBoolean( "show_booster_move", boosterMove );
        
        editor.commit();
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the booster move help
     * <p>
     * //##############################
     *
     * @param context
     */
    public static boolean getBoosterHelper( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        
        //
        return prefs.getBoolean( "show_booster_move", false );
    }
    
    
    /**
     * //##############################
     * <p>
     * Set resume button help
     * <p>
     * //##############################
     *
     * @param context
     * @param resumeShown
     */
    public static void setResumeHelper( Context context, boolean resumeShown )
    {
        SharedPreferences        prefs  = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = prefs.edit();
        
        //
        editor.putBoolean( "show_resume_help", resumeShown );
        
        editor.commit();
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the resume button help
     * <p>
     * //##############################
     *
     * @param context
     */
    public static boolean getResumeHelper( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        
        //
        return prefs.getBoolean( "show_resume_help", false );
    }
    
    
    /**
     * //##############################
     * <p>
     * Test to see if the player
     * has received help yet
     * <p>
     * //##############################
     *
     * @param context
     *
     * @return
     */
    public static boolean wasHelpReceived( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        
        // Save system flags
        return prefs.getBoolean( "received_help_screen", false );
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the player's age
     * <p>
     * //##############################
     *
     * @param context
     *
     * @return
     */
    public static int getPlayerAge( Context context )
    {
        SharedPreferences prefs = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        
        // Save system flags
        return prefs.getInt( "player_age", 0 );
    }
    
    
    /**
     * //##############################
     * <p>
     * Set the player's age
     * <p>
     * //##############################
     *
     * @return
     */
    public static void setPlayerAge( Context context, int age )
    {
        SharedPreferences        prefs  = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save system flags
        editor.putInt( "player_age", age );
        
        editor.commit();
    }
    
    
    /**
     * //##############################
     * <p>
     * Show that help was received
     * <p>
     * //##############################
     *
     * @return
     */
    public void helpWasReceived()
    {
        SharedPreferences        prefs  = context.getSharedPreferences( "app_data_block", Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = prefs.edit();
        
        // Save system flags
        editor.putBoolean( "received_help_screen", true );
        
        editor.commit();
    }
    
    
    /**
     * Copy data from one array to the next
     *
     * @param arrayFrom
     * @param arrayTo
     * @param y
     * @param x
     */
    public static void arrayCopy( int[][] arrayFrom, int[][] arrayTo, int y, int x )
    {
        for ( int i = 0; i < y; i++ )
        {
            for ( int j = 0; j < x; j++ )
            {
                arrayTo[ i ][ j ] = arrayFrom[ i ][ j ];
            }
        }
    }
    
    
    /**
     * @param array
     * @param y
     * @param x
     */
    public static void arraySetData( int[][] array, int y, int x, int data )
    {
        for ( int i = 0; i < y; i++ )
        {
            for ( int j = 0; j < x; j++ )
            {
                array[ i ][ j ] = data;
            }
        }
    }
    
    
    /**
     * Returns an array of ints from a resorce array
     *
     * @param arrayResId
     *
     * @return
     */
/*
    public int[] arrayFromResource( int arrayResId )
    {
        int[]      array;
        TypedArray arrays;
        
        arrays = context.getResources().obtainTypedArray( arrayResId );
        array = new int[ arrays.length() ];
        //
        for ( int i = 0; i < arrays.length(); i++ )
        {
            array[ i ] = arrays.getResourceId( i, 0 );
        }
        
        // Clean up man
        arrays.recycle();
        
        return array;
    }
*/
    public static int[] arrayFromResource( Context context, int arrayResId )
    {
        int[]      array;
        TypedArray arrays;
        
        arrays = context.getResources().obtainTypedArray( arrayResId );
        array = new int[ arrays.length() ];
        //
        for ( int i = 0; i < arrays.length(); i++ )
        {
            array[ i ] = arrays.getResourceId( i, 0 );
        }
        
        // Clean up man
        arrays.recycle();
        
        return array;
    }
    
    
    /**
     * Related to the callback
     */
    @Override
    public void onRewardedVideoAdLoaded()
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_LOADED, 0 );
        }
    }
    
    @Override
    public void onRewardedVideoAdOpened()
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_OPENED, 0 );
        }
    }
    
    @Override
    public void onRewardedVideoStarted()
    {
        if ( musicIsPlaying )
        {
            soundPlayer.music.pause();
        }
        
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_STARTED, 0 );
        }
    }
    
    @Override
    public void onRewardedVideoAdClosed()
    {
        if ( !GameEngine.rewardGiven )
        {
            Toast.makeText( context, "No reward was given.", Toast.LENGTH_SHORT ).show();
        }
        
        
        // Load the next rewarded video ad.
        loadRewardedVideoAd( context );
        
        if ( musicIsPlaying && soundPlayer != null && soundPlayer.music != null )
        {
            soundPlayer.music.start();
        }
        
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_CLOSED, 0 );
        }
        
        // MUST come after the dialog was informed of the changes
        // to be processed
        if ( savedDialog != null )
        {
            savedDialog.dismiss();
            savedDialog = null;
        }
    }
    
    @Override
    public void onRewarded( RewardItem rewardItem )
    {
/*
        Toast.makeText( context, "onRewarded! currency: " + rewardItem.getType() + "  amount: " + rewardItem.getAmount(),
                        Toast.LENGTH_SHORT
                      ).show();
*/
        // Reward the user
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_REWARDED, 0 );
        }
        
    }
    
    @Override
    public void onRewardedVideoAdLeftApplication()
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_LEFT_APP, 0 );
        }
    }
    
    @Override
    public void onRewardedVideoAdFailedToLoad( int i )
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_FAILED_LOAD, i );
        }
    }
    
    @Override
    public void onRewardedVideoCompleted()
    {
        if ( onAdReturnListener != null )
        {
            onAdReturnListener.onAdReturn( AD_COMPLETED, 0 );
        }
    }
}


