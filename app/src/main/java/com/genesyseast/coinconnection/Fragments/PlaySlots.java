package com.genesyseast.coinconnection.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.genesyseast.coinconnection.ShowCase.ShowClass;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.CustomControls.SlotReelView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameGraphics.GameBitmaps;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

import java.security.SecureRandom;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaySlots
        extends Fragment
        implements View.OnLongClickListener, View.OnTouchListener, SlotReelView.OnReelSpinListener, View.OnClickListener
{
    
    public static boolean        reelIsSpinning     = false;
    private       GameEngine     gameEngine;
    private       SlotReelView[] reelView           = new SlotReelView[ 3 ];
    private       int[]          reelImages         = {
            R.id.reel1, R.id.reel2, R.id.reel3,
            };
    private       float          swipeY1            = 0;
    private       int            slotVersion        = 0;
    public static int            reelTicker         = -1;
    //
    final private int            STRIP_ITEMS        = 6;
    //
    private       int            itemHolderPosition = 0;
    private       int[]          item_holder        = { R.id.item1, R.id.item2, R.id.item3 };
    //
    private       boolean        REWARDED           = false;
    public static int            theWinnerIs        = 0;
    public static boolean        closeCall          = false;
    private       boolean        spinEnabled        = true;
    private       boolean        videoWatched       = false;
    private       View           view_main;
    // TODO : free move card needs to be free move
    private       int[]          winnableItems      = {
            R.drawable.bomb_on, R.drawable.bolt_on, R.drawable.star_2_on, R.drawable.random_on, R.drawable.coin_gold, R.drawable.free_moves
    };
    private       int[]          slotsLayouts       = { R.layout.play_slots_red_fragment, R.layout.play_slots_purple_fragment };
    private       int[]          reelStrips         = { R.drawable.slot_reel_red, R.drawable.slot_reel_purple };
    private final String[]       itemNames          = {
            "a Bomb Booster", "a Lightening Booster", "a Star Booster", "a Random Booster", "2 Coins", "5 Free Moves"
    };
    
    
    // This will be applied to each menu layout for the menu navigation
    
    public PlaySlots()
    {
        // Required empty public constructor
        gameEngine = GameEngine.getInstance( getContext() );
    }
    
    /**
     * //##############################
     * <p>
     * Create the required view
     * <p>
     * //##############################
     *
     * @param inflater           N/A
     * @param container          N/A
     * @param savedInstanceState N/A
     *
     * @return N/A
     */
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        GradientTextView tv;
        ImageView        reelArm;
        // Create the random number generator
        SecureRandom r = new SecureRandom();
        
/*
        // DEBUG
        if ( gameBoard == null )
        {
            gameBoard = GameBoard.getInstance( getContext() );
        }
*/
        // Inflate the layout for this fragment
        slotVersion = r.nextInt( 2 );
        view_main = inflater.inflate( slotsLayouts[ slotVersion ], container, false );
        
        // Spins remaining indicator
        tv = view_main.findViewById( R.id.spinsLeftText );
        tv.setText( String.format( Locale.getDefault(), "%d", GameEngine.bonusCount ) );
        
        
        // Exit button support
        View exit = view_main.findViewById( R.id.exitSlotsButton );
        exit.setOnClickListener( this );
        
        // Control Arm animation
        reelArm = view_main.findViewById( R.id.reelArm );
        reelArm.setOnTouchListener( this );
        
        // Prep for fade in
        view_main.setAlpha( 0f );
        //
        //Load the required sounds
        // SoundPool: Only on TRUE resume
        //
        int reelIndex;
        int tileSize;
        
        tileSize = (GameBitmaps.getImageSize( getContext(), reelStrips[ slotVersion ] ).y / STRIP_ITEMS);
        
        //#########################
        //
        // set up the reels
        //
        //#########################
        for ( int i = 0; i < reelImages.length; i++ )
        {
            reelIndex = (r.nextInt( STRIP_ITEMS ) * tileSize) + (tileSize / 2);
            reelView[ i ] = view_main.findViewById( reelImages[ i ] );
            reelView[ i ].setCurrentY( reelIndex );
            reelView[ i ].setReelImageResId( reelStrips[ slotVersion ] );
            // Need to have a "winner" listener
            reelView[ i ].setOnReelSpinListener( PlaySlots.this );
        }
        
        loadSlotsData( view_main );
        
        return view_main;
    }
    
    
    /**
     * //##############################
     * <p>
     * Universal call to load
     * fragment data
     * <p>
     * //##############################
     */
    private void loadSlotsData( View view_main )
    {
        // Previous screen was faded out!
        view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                if ( view_main.getTag() == null )
                {
                    gameEngine.animatorList.add( animation );
                    view_main.setTag( 1 );
                }
            }
        } );
        view_main.setAlpha( 0f );
        view_main.animate().alpha( 1f ).setDuration( AnimationValues.FADE_TIME );
        view_main.animate().withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                if ( gameEngine.soundPlayer != null )
                {
                    if ( GameEngine.musicIsPlaying )
                    {
                        gameEngine.soundPlayer.fadeOutMusic().addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                //@@@@@@@@@@@@@@@@@@@
                                gameEngine.soundPlayer.playBgMusic( PlaySound.SLOTS_BG_SOMEWHERE_IN_THE_ELEVATOR, PlaySound.LOOP );
                                gameEngine.currentBGMusic = PlaySound.SLOTS_BG_SOMEWHERE_IN_THE_ELEVATOR << 2;
                                
                                super.onAnimationEnd( animation );
                            }
                        } );
                    }
                    else
                    {
                        //@@@@@@@@@@@@@@@@@@@
                        gameEngine.soundPlayer.playBgMusic( PlaySound.SLOTS_BG_SOMEWHERE_IN_THE_ELEVATOR, PlaySound.LOOP );
                        gameEngine.currentBGMusic = PlaySound.SLOTS_BG_SOMEWHERE_IN_THE_ELEVATOR << 2;
                    }
                }
                
                view_main.setTag( null );
                
                //#############################
                //
                // Should app show the swipe
                // down animation
                //
                //#############################
                if ( (GameEngine.systemFlags & GameEngine.SWIPE_DOWN) == 0 )
                {
                    final ObjectAnimator swiper;
                    final ImageView      swipeView;
                    final int[]          swipeArray = GameEngine.arrayFromResource( getContext(), R.array.do_swipe );
                    
                    // Show swipe animation on initial run
                    // Flag as seen
                    GameEngine.systemFlags |= GameEngine.SWIPE_DOWN;
                    
                    // Disable spinning until we are ready
                    PlaySlots.reelIsSpinning = true;
                    
                    swipeView = view_main.findViewById( R.id.swipeDownView );
                    swipeView.setVisibility( View.VISIBLE );
                    swiper = ObjectAnimator.ofInt( swipeView, "backgroundResource", swipeArray );
                    
                    // Create a showcase for the swiper view
                    final ShowClass showClass = new ShowClass( getContext() );
                    
                    //
                    // Animate the swipe with a delay
                    int duration = swipeArray.length * 50;
                    //
                    swiper.setDuration( duration );
                    swiper.setStartDelay( 500 );
                    swiper.setInterpolator( new AccelerateInterpolator() );
                    //                    swiper.setRepeatMode( ValueAnimator.REVERSE );
                    swiper.setEvaluator( new CustomEvaluator() );
                    swiper.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            
                            PlaySlots.reelIsSpinning = false;
                            swipeView.setVisibility( View.INVISIBLE );
                        }
                    } );
                    
                    //
                    showClass.addTarget( swipeView, true, 500, 0, new LinearInterpolator(), ShowClass.ROUNDED_RECT, ShowClass.CENTER_OUT_VERT );
                    // Multiply by Repeat Count
                    showClass.setOnCloseType( ShowClass.ON_TIMER, duration + 500 ).showCaseTargets();
                    
                    //
                    gameEngine.animatorList.add( swiper );
                    swiper.start();
                }
            }
        } ).start();
        
        //
        // Intercept the back key
        //
        view_main.setFocusableInTouchMode( true );
        view_main.requestFocus();
        view_main.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if ( !reelIsSpinning && keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    // Animate the return
                    if ( getView() != null )
                    {
                        // Transition back to caller
                        getView().animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if ( getActivity() != null )
                                {
                                    leaveTheScene( view_main );
//                                    getActivity().onBackPressed();
//                                    FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                                }
                            }
                        } );
                        getView().animate().start();
                    }
                    else
                    {
                        // Return with no animation
                        if ( getActivity() != null )
                        {
//                            getActivity().onBackPressed();
//                            FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                            leaveTheScene( view_main );
                        }
                    }
                    
                    //#############################
                    //
                    // Kill current song
                    //
                    //#############################
                    if ( gameEngine.soundPlayer != null )
                    {
                        //@@@@@@@@@@@@@@@@@@@
                        gameEngine.soundPlayer.fadeOutMusic();
                    }
                    
                    return true;
                }
                
                return false;
            }
        } );
    }
    
    
    /**
     * //##############################
     * <p>
     * Reels stopped spinning
     * <p>
     * //##############################
     */
    @Override
    public void onReelStoppedSpinning()
    {
        GameEngine.bonusCount--;
        reelTicker = -1;
        
        
        //##########################################
        //
        //
        //
        //##########################################
        if ( theWinnerIs > -1 )
        {
            // Flashing....LIGHTS!!
            int[]         redFlasher    = new int[]{ R.drawable.red_off, R.drawable.red_on };
            int[]         purpleFlasher = new int[]{ R.drawable.purple_off, R.drawable.purple_on };
            int[]         pointer;
            ValueAnimator flash;
            final View    layout        = view_main.findViewById( R.id.slotMachine );
            final int     bg_image;
            
            //
            // Clock does not exist
            //
            //            theWinnerIs--;
            
            //@@@@@@@@@@@@@@@@@@@ Play Big winner sound: 3 seconds
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.SLOTS_WIN_GAME );
            }
            
            //
            if ( slotVersion == 0 )
            {
                pointer = redFlasher;
            }
            else
            {
                pointer = purpleFlasher;
            }
            
            //
            //##########################
            //
            bg_image = pointer[ 0 ];
            flash = ValueAnimator.ofInt( 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0 );
            flash.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    int index = ( int ) animation.getAnimatedValue();
                    
                    layout.setBackgroundResource( pointer[ index % 2 ] );
                }
            } );
            
            
            // Flash the lights
            gameEngine.animatorList.add( flash );
            flash.setDuration( 2000 ).setInterpolator( new LinearInterpolator() );
            //            flash.setRepeatCount( 16 );
            flash.setEvaluator( new CustomEvaluator() );
            flash.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    ObjectAnimator winner = showItemReceived( theWinnerIs );
                    
                    // Replace the active Slot machine image with "OFF"
                    layout.setBackgroundResource( bg_image );
                    
                    gameEngine.animatorList.add( winner );
                    winner.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            // Give the prize
                            givePlayerPrize( winnableItems[ theWinnerIs ] );
                            
                            // Show the prize won
                            addItemToStack( view_main );
                            
                            // Update spins left
                            GradientTextView spins = view_main.findViewById( R.id.spinsLeftText );
                            spins.setText( String.format( Locale.getDefault(), "%d", GameEngine.bonusCount ) );
                            //
                            super.onAnimationEnd( animation );
                        }
                    } );
                    
                    //
                    winner.start();
                }
            } );
            //
            flash.start();
        }
        else
        {
            //@@@@@@@@@@@@@@@@@@@
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.SLOTS_LOSE_GAME );
            }
            
            // Finish off the magic!
            if ( GameEngine.bonusCount <= 0 )
            {
                leaveTheScene( view_main );
            }
            else
            {
                // re-enable moving the arm
                spinEnabled = true;
            }
        }
        
        //
        // Update spins left
        //
        GradientTextView spins = view_main.findViewById( R.id.spinsLeftText );
        spins.setText( String.format( Locale.getDefault(), "%d", GameEngine.bonusCount ) );
    }
    
    
    /**
     * //############################
     * <p>
     * Give the player the item
     * they won
     * <p>
     * //############################
     *
     * @param wonItem
     */
    private void givePlayerPrize( int wonItem )
    {
        switch ( wonItem )
        {
            case R.drawable.bolt_on:
                gameEngine.Boosters[ 1 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + 1, 99 );
                break;
            case R.drawable.star_2_on:
                gameEngine.Boosters[ 2 ] = ( byte ) Math.min( gameEngine.Boosters[ 2 ] + 1, 99 );
                break;
            case R.drawable.random_on:
                gameEngine.Boosters[ 3 ] = ( byte ) Math.min( gameEngine.Boosters[ 3 ] + 1, 99 );
                break;
            case R.drawable.coin_gold:
                GameEngine.moneyOnHand = Math.min( GameEngine.moneyOnHand + 2, 9999 );
                break;
            case R.drawable.free_moves:
                GameEngine.movesOnHand = Math.min( GameEngine.movesOnHand + 1, 10 );
                break;
            case R.drawable.bomb_on:
            default:
                gameEngine.Boosters[ 0 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + 1, 99 );
                break;
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Add Item to the list
     * <p>
     * //############################
     */
    private void addItemToStack( View mainView )
    {
        ImageView item;
        
        // Add the item to the list
        // THIS NEEDS TESTING
        if ( itemHolderPosition == 3 )
        {
            // Shift everything over
            for ( int i = 0; i < 2; i++ )
            {
                ImageView move_in;
                ImageView move_out;
                
                move_out = mainView.findViewById( item_holder[ i ] );
                move_in = mainView.findViewById( item_holder[ i + 1 ] );
                //
                move_out.setImageDrawable( move_in.getDrawable() );
            }
            //
            item = mainView.findViewById( item_holder[ 2 ] );
            item.setImageResource( 0 );
            itemHolderPosition = 2;
        }
        else
        {
            item = mainView.findViewById( item_holder[ itemHolderPosition ] );
        }
        
        //
        item.setImageResource( winnableItems[ theWinnerIs ] );
        item.setScaleY( .2f );
        item.setScaleX( .2f );
        item.animate().setStartDelay( 500 );
        item.animate().setDuration( 500 ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
        item.animate().withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                // Play Prize added sound: 1 seconds
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.SLOTS_PRIZE_ADD );
                }
            }
        } );
        item.animate().scaleY( 1 ).scaleX( 1 ).start();
        
        // Get to the next item itemHolder
        itemHolderPosition++;
    }
    
    /**
     * //##############################
     * <p>
     * Ask for a second chance for
     * more spins
     * <p>
     * //##############################
     */
    private void leaveTheScene( View mainView )
    {
        SecureRandom r = new SecureRandom();
        
        // Ask the player, RANDOMLY, if they would like
        // to watch a video for more spins
        //@@@@@@@@@@@@@@@@@@
        if ( gameEngine.soundPlayer != null )
        {
            // Fadeout the music
            gameEngine.soundPlayer.fadeOutMusic();
        }
        
        // Transition back to caller
        mainView.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                if ( mainView.getTag() == null )
                {
                    mainView.setTag( 1 );
                    gameEngine.animatorList.add( animation );
                }
            }
        } );
        mainView.animate().alpha( 0f ).setStartDelay( 2000 );
        mainView.animate().setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                mainView.setTag( null );
                if ( getActivity() != null )
                {
//                    getActivity().onBackPressed();
                    FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                }
                
            }
        } );
        mainView.animate().start();
    }
    
    
    /**
     * //##############################
     * <p>
     * Swipe listener for Reel Arm
     * <p>
     * //##############################
     *
     * @param v     N/A
     * @param event N/A
     *
     * @return N/A
     */
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        if ( getView() != null )
        {
            // Do not allow anymore spins!
            if ( GameEngine.bonusCount <= 0 || !spinEnabled )
            {
                return false;
            }
            
            //##############################
            //
            //
            //##############################
            switch ( event.getAction() )
            {
                case MotionEvent.ACTION_DOWN:
                    swipeY1 = event.getY();
                    return true;
                
                case MotionEvent.ACTION_UP:
                    float swipeY2 = event.getY();
                    
                    if ( swipeY2 > swipeY1 && !reelIsSpinning && v.getTag() == null )
                    {
                        //##################################
                        // Downward swipe
                        // Spin the reels, and decide on
                        // a winner
                        //##################################
                        ObjectAnimator reelAnimation;
                        int[]          arm      = { R.array.red_reel_arm, R.array.purple_reel_arm };
                        int[]          armArray = GameEngine.arrayFromResource( getContext(), arm[ slotVersion ] );
                        ImageView      reelArm  = getView().findViewById( R.id.reelArm );
                        
                        
                        //@@@@@@@@@@@@@@@@@@@ Play Free Spins sound
                        if ( gameEngine.soundPlayer != null )
                        {
                            gameEngine.soundPlayer.playBgSfx( PlaySound.SLOT_ARM );
                        }
                        
                        // Disable touching
                        spinEnabled = false;
                        
                        reelAnimation = ObjectAnimator.ofInt( reelArm, "backgroundResource", armArray );
                        reelAnimation.setEvaluator( new CustomEvaluator() );
                        reelAnimation.setDuration( armArray.length * 50 );
                        reelAnimation.setRepeatMode( ValueAnimator.REVERSE );
                        reelAnimation.setInterpolator( new LinearInterpolator() );
                        reelAnimation.setRepeatCount( 1 );
                        reelAnimation.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                //@@@@@@@@@@@@@@@@@@ Play the reel spinning sound
                                if ( gameEngine.soundPlayer != null )
                                {
                                    reelTicker = gameEngine.soundPlayer.play( PlaySound.SLOTS_REEL_TICK, PlaySound.LOOP );
                                }
                                
                                processReelSpin();
                            }
                        } );
                        
                        //
                        reelAnimation.start();
                    }
                    
                    return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * //############################
     * <p>
     * Click listener
     * <p>
     * //############################
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        
        // Leave the bonus round
        if ( id == R.id.exitSlotsButton )
        {
            leaveTheScene( view_main );
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Display Toast for Custom
     * button text
     * <p>
     * //##############################
     *
     * @param v N/A
     *
     * @return N/A
     */
    @Override
    public boolean onLongClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.reelArm )
        {   // Display "NEED TO SWIPE" animation. Create hand / down arrow with inkscape
            Toast.makeText( getContext(), "Swipe Down to spin!", Toast.LENGTH_SHORT ).show();
        }
        
        return true;
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the reelspinning!
     * <p>
     * //##############################
     */
    private void processReelSpin()
    {
        SecureRandom r         = new SecureRandom();
        final int    tileSize  = (GameBitmaps.getImageSize( getContext(), reelStrips[ slotVersion ] ).y / STRIP_ITEMS);
        int          stopPoint = r.nextInt( 10 );
        int[]        points    = new int[ 3 ];
        int          spinSpeed;
        
        //##############################
        //
        // 1 in 10 roll
        // If a 8 was rolled, Get 2 Coins!
        //
        //##############################
        if ( stopPoint == 8 )
        {
            points[ 2 ] = points[ 1 ] = points[ 0 ] = theWinnerIs = 4;
            
            // Pick a spin speed for the winner
            spinSpeed = r.nextInt( 4 );
            //
            closeCall = true;
            
        }
        //
        // If a 9 was rolled, Get 1 Move block == 5 moves!
        //
        else if ( stopPoint == 9 )
        {
            points[ 2 ] = points[ 1 ] = points[ 0 ] = theWinnerIs = 5;
            
            // Pick a spin speed for the winner
            spinSpeed = r.nextInt( 4 );
            //
            closeCall = true;
        }
        else
        {   // A random booster was won
            stopPoint = r.nextInt( 4 );
            //
            points[ 2 ] = points[ 1 ] = points[ 0 ] = theWinnerIs = stopPoint;
            
            // Pick a spin speed for the winner
            spinSpeed = r.nextInt( 4 );
            //
            closeCall = true;
        }
        
        // DEBUG
        //        theWinnerIs = r.nextInt( winnableItems.length );
        
        //
        // set up the reels
        //
        if ( getView() != null )
        {
            int[][] spinner = {
                    { 1, 3, 5 }, // Default
                    { 7, 1, 3 }, // NO YES YES
                    { 1, 6, 3 }, // YES NO YES
                    { 1, 3, 6 }  // YES YES NO
            };
            
            //            offset += (float)(tileSize/2);
            
            for ( int i = 0; i < reelImages.length; i++ )
            {
                // Get their final positions
                points[ i ] = (points[ i ] * tileSize) + (tileSize / 2);
                
                reelView[ i ] = getView().findViewById( reelImages[ i ] );
                reelView[ i ].spinTheReel( spinner[ spinSpeed ][ i ], points[ i ], reelStrips[ slotVersion ] );
            }
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Give the player a nice toy!
     * <p>
     * //##############################
     *
     * @param itemReceived N/A
     */
    private ObjectAnimator showItemReceived( final int itemReceived )
    {
        final View           layout;
        final View           view   = getView();
        final ImageView      img;
        final View           star;
        ObjectAnimator       winner = null;
        PropertyValuesHolder sx     = PropertyValuesHolder.ofFloat( "scaleX", 0, 1 );
        PropertyValuesHolder sy     = PropertyValuesHolder.ofFloat( "scaleY", 0, 1 );
        
        //
        if ( view != null )
        {
            // Display the text
            GradientTextView tv = view.findViewById( R.id.receivedItemText );
            tv.setText( String.format( Locale.getDefault(), "You Received %s!", itemNames[ theWinnerIs ] ) );
            tv.setVisibility( View.VISIBLE );
            
            // What the player gets
            img = view.findViewById( R.id.receivedItemImage );
            img.setVisibility( View.VISIBLE );
            img.setImageResource( winnableItems[ itemReceived ] );
            
            // Star burst
            star = view.findViewById( R.id.spinningItemStar );
            star.setVisibility( View.VISIBLE );
            star.animate().setDuration( 20000 ).setInterpolator( new LinearInterpolator() );
            star.animate().rotation( -360 ).start();
            
            // Complete layout
            layout = view.findViewById( R.id.bigWinner );
            layout.setVisibility( View.VISIBLE );
            
            // Animator
            winner = ObjectAnimator.ofPropertyValuesHolder( layout, sx, sy );
            winner.setDuration( 500 ).setInterpolator( new OvershootInterpolator() );
            winner.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    
                    // Exit the display
                    layout.animate().setStartDelay( 2000 ).alpha( 0 ).setDuration( 1000 ).withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                // re-enable moving the arm
                                star.animate().cancel();
                                layout.setAlpha( 1f );
                                layout.setVisibility( View.INVISIBLE );
                                
                                // Finish off the magic!
                                if ( GameEngine.bonusCount <= 0 )
                                {
                                    leaveTheScene( view_main );
                                }
                                else
                                {
                                    // re-enable moving the arm
                                    spinEnabled = true;
                                }
                            }
                            catch ( Exception ex )
                            {
                                // Strictly for exiting a win too fast on the last spin
                                ex.printStackTrace();
                            }
                        }
                    } ).start();
                }
            } );
        }
        
        return winner;
    }
    
    
    @Override
    public void onDestroy()
    {
        view_main = null;
        gameEngine = null;
        reelView = null;
        reelIsSpinning = false;
        
        super.onDestroy();
    }
}



/*
                if ( !ErrorDialog.isShowing && keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                        {
final ErrorDialog dialog = new ErrorDialog();
        int[]             resIds = gameEngine.arrayFromResource( R.array.star_burst );
        
        // Pause the reels
        for ( SlotReelView slotReelView : reelView )
        {
        if ( slotReelView.reelAnimator != null )
        {
        slotReelView.reelAnimator.pause();
        }
        }
        
        // Pause / stop reel clicking
        if ( reelTicker != -1 )
        {
        if ( gameEngine.soundPlayer != null )
        {
        gameEngine.soundPlayer.play( PlaySound.SLOTS_REEL_TICK, PlaySound.STOP, reelTicker );
        }
        }
        
        // Ask the user if they want to give up their spins
        dialog.setTitle( "Exit: Bonus Round" );
        dialog.setMessage( String.format( Locale.getDefault(), "Are you sure you want to forfeit your %d bonus spin%s?", GameEngine.bonusCount, (GameEngine.bonusCount > 1 ? "s" : "") ) );
        dialog.setButtons( ErrorDialog.MAIN_BUTTONS ).setYesText( "Yes" ).setNoText( "No" ).setCancelText( "Cancel" );
        dialog.animateFormImage( resIds );
        dialog.setCancelable( false );
        dialog.setAlertIcon( R.drawable.ic_help_black_24dp );
        dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
        {
@Override
public void onErrorExitClick( int buttonClicked )
        {
        if ( buttonClicked == ErrorDialog.NO_BUTTON || buttonClicked == ErrorDialog.CANCEL_BUTTON )
        {
        dialog.dismiss();
        
        // resume the reels
        for ( SlotReelView slotReelView : reelView )
        {
        if ( slotReelView.reelAnimator != null )
        {
        slotReelView.reelAnimator.resume();
        }
        }
        
        // Pause / stop reel clicking
        if ( reelTicker != -1 )
        {
        reelTicker = gameEngine.soundPlayer.play( PlaySound.SLOTS_REEL_TICK, PlaySound.LOOP );
        }
        }
        else
        {
        // Close the dialog and exit
        dialog.dismiss();
        
        //
        reelIsSpinning = false;
        
        // cancel the reels
        for ( SlotReelView slotReelView : reelView )
        {
        if ( slotReelView.reelAnimator != null )
        {
        slotReelView.reelAnimator.removeAllListeners();
        slotReelView.reelAnimator.cancel();
        }
        }
        
        // Animate the return
        if ( getView() != null )
        {
        // Transition back to caller
        getView().animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
        {
@Override
public void run()
        {
        if ( getActivity() != null )
        {
        getActivity().onBackPressed();
        }
        }
        } );
        getView().animate().start();
        }
        else
        {
        // Return with no animation
        if ( getActivity() != null )
        {
        getActivity().onBackPressed();
        }
        }
        
        //#############################
        //
        // Kill current song
        //
        //#############################
        if ( gameEngine.soundPlayer != null )
        {
        //@@@@@@@@@@@@@@@@@@@
        gameEngine.soundPlayer.fadeOutMusic();
        }
        }
        }
        } );
        
        // Display the dialog and ask!
        if ( getActivity() != null )
        {
        //@@@@@@@@@@@@@@@@@@@@@ Error sound
        if ( gameEngine.soundPlayer != null )
        {
        gameEngine.soundPlayer.playBgSfx( PlaySound.ERROR );
        }
        dialog.show( getActivity().getSupportFragmentManager(), "Exit" );
        }
        
        return true;
        }
        
        return false;
        }
*/
