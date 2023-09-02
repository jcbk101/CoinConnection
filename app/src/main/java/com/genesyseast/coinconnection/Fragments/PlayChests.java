package com.genesyseast.coinconnection.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.genesyseast.coinconnection.ShowCase.ShowClass;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

import java.security.SecureRandom;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayChests
        extends Fragment
        implements View.OnClickListener
{
    
    private       GameEngine   gameEngine;
    private       int          chestVersion  = 0;
    public        int          theWinnerIs   = 0;
    private       boolean      videoWatched  = false;
    private       boolean      processClick  = false;
    private       View         view_main;
    //
    private       int[]        bubbleSide    = { R.drawable.bubble_side_red_a, R.drawable.bubble_side_purple_a };
    private       int[]        bubbleCenter  = { R.drawable.bubble_center_red_a, R.drawable.bubble_center_purple_a };
    private       int[]        chestBoxes    = { R.id.chest1, R.id.chest2, R.id.chest3 };
    private       int[]        itemWon       = { R.id.item1, R.id.item2, R.id.item3 };
    private       int[]        starBurst     = { R.id.chestBurst1, R.id.chestBurst2, R.id.chestBurst3 };
    // TODO : free move card needs to be free move
    private       int[]        winnableItems = {
            R.drawable.bomb_on, R.drawable.bolt_on, R.drawable.star_2_on, R.drawable.random_on, R.drawable.coin_gold, R.drawable.free_moves
    };
    private final String[]     itemNames     = {
            "a Bomb Booster", "a Lightening Booster", "a Star Booster", "a Random Booster", "2 Coins", "5 Free Moves"
    };
    private       int[]        chestLayouts  = { R.layout.play_chests_red_fragment, R.layout.play_chests_purple_fragment };
    private       SecureRandom r;
    
    
    /**
     * Constructor
     */
    public PlayChests()
    {
        // Required empty public constructor
        gameEngine = GameEngine.getInstance( getContext() );
    }
    
    /**
     * Create the required view
     *
     * @param inflater           N/A
     * @param container          N/A
     * @param savedInstanceState N/As
     *
     * @return N/A
     */
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        // Create the random number generator
        r = new SecureRandom();
        
        // Inflate the layout for this fragment
        chestVersion = r.nextInt( 2 );
        view_main = inflater.inflate( chestLayouts[ chestVersion ], container, false );
        
        // Set up the clickers
        for ( int chestBox : chestBoxes )
        {
            View chest = view_main.findViewById( chestBox );
            chest.setOnClickListener( this );
        }
        
        //
        // Set the proper starting message
        //
        TextView tv = view_main.findViewById( R.id.receivedItemText );
        tv.setText( getString( R.string.make_chest_choice ) );
        
        View exit = view_main.findViewById( R.id.exitChestButton );
        exit.setOnClickListener( this );
        
        // Prep for fade in
        view_main.setAlpha( 0f );
        
        //
        loadChestData( view_main );
        
        return view_main;
    }
    
    
    /**
     * //############################
     * <p>
     * Universal call to load
     * fragment data
     * <p>
     * //############################
     */
    private void loadChestData( View view_main )
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
                
                //#############################
                //
                // Should app show the swipe
                // down animation
                //
                //#############################
                if ( (GameEngine.systemFlags & GameEngine.SELECT_CHEST) == 0 )
                {
                    // Show swipe animation on initial run
                    // Flag as seen
                    GameEngine.systemFlags |= GameEngine.SELECT_CHEST;
                    
                    //
                    // Showcase the chest
                    //
                    final ShowClass showClass = new ShowClass( getContext() );
                    // Set up the clickers
                    for ( int i = 0; i < chestBoxes.length; i++ )
                    {
                        View chest = view_main.findViewById( chestBoxes[ i ] );
                        showClass.addTarget( chest, true, 500, i * 100, new LinearInterpolator(), ShowClass.ROUNDED_RECT, ShowClass.CENTER_OUT_VERT );
                        
                        if ( i == 1 )
                        {
                            showClass.addTargetTip( showClass.getTargetCount(), chest, "Select a prize", "Each chest contains a prize, choose one!", bubbleCenter[ chestVersion ],
                                                    ShowClass.TIP_CENTER
                                                  );
                        }
                    }
                    //
                    showClass.setOnCloseType( ShowClass.ON_TIMER, 3500 ).showCaseTargets();
                }
                
                view_main.setTag( null );
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
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    // Close the dialog and exit
                    leaveTheScene();
                    //
                    return true;
                }
                
                return false;
            }
        } );
    }
    
    
    /**
     * //############################
     * <p>
     * Universal Leave caller :(
     * <p>
     * //############################
     */
    private void leaveTheScene()
    {
        // Animate the return
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
        view_main.animate().setStartDelay( 1500 );
        view_main.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                view_main.setTag( null );
                if ( getActivity() != null )
                {
//                    getActivity().onBackPressed();
                    FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                }
            }
        } );
        //
        view_main.animate().start();
        
        
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
        
        if ( processClick )
        {
            return;
        }
        
        processClick = true;
        
        //##################################
        //
        //
        //
        //##################################
        if ( id == R.id.chest1 || id == R.id.chest2 || id == R.id.chest3 )
        {
            // Player selected a box. Check if it won
            //@@@@@@@@@@@@@@@@@ Prize won
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.SLOTS_FREE_SPINS );
            }
            
            //
            for ( int i = 0; i < chestBoxes.length; i++ )
            {
                if ( chestBoxes[ i ] == id )
                {
                    int wonItem = r.nextInt( winnableItems.length );
                    
                    showItemReceived( i, wonItem ).start();
                    givePlayerPrize( winnableItems[ wonItem ] );
                    break;
                }
            }
            
            //
            view_main.animate().alpha( 1 ).setDuration( 3000 ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    leaveTheScene();
                }
            } ).start();
        }
        
        
        // Leave the bonus round
        if ( id == R.id.exitChestButton )
        {
            leaveTheScene();
        }
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
                GameEngine.giftWaiting = false;
                gameEngine.Boosters[ 1 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + 1, 99 );
                break;
            //
            case R.drawable.star_2_on:
                GameEngine.giftWaiting = false;
                gameEngine.Boosters[ 2 ] = ( byte ) Math.min( gameEngine.Boosters[ 2 ] + 1, 99 );
                break;
            //
            case R.drawable.random_on:
                GameEngine.giftWaiting = false;
                gameEngine.Boosters[ 3 ] = ( byte ) Math.min( gameEngine.Boosters[ 3 ] + 1, 99 );
                break;
            //
            case R.drawable.coin_gold:
                GameEngine.giftWaiting = false;
                GameEngine.moneyOnHand = Math.min( GameEngine.moneyOnHand + 2, 9999 );
                break;
            //
            case R.drawable.free_moves:
                GameEngine.giftWaiting = false;
                GameEngine.movesOnHand = Math.min( GameEngine.movesOnHand + 1, 10 );
                break;
            //
            case R.drawable.bomb_on:
            default:
                GameEngine.giftWaiting = false;
                gameEngine.Boosters[ 0 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + 1, 99 );
                break;
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Give the player a nice toy!
     * <p>
     * //############################
     *
     * @param itemReceived N/A
     */
    private ObjectAnimator showItemReceived( final int winningChest, final int itemReceived )
    {
        int[]          colorBoxes  = { R.array.orange_chest, R.array.purple_chest };
        ObjectAnimator winner;
        View           chestWinner = view_main.findViewById( chestBoxes[ winningChest ] );
        
        
        //################################
        //
        //
        //
        //################################
        int[] resId = GameEngine.arrayFromResource( getContext(), colorBoxes[ chestVersion ] );
        winner = ObjectAnimator.ofInt( chestWinner, "imageResource", resId );
        
        winner.setEvaluator( new CustomEvaluator() );
        winner.setDuration( resId.length * 100 );
        winner.setInterpolator( new LinearInterpolator() );
        winner.addListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation )
            {
                View  burst = view_main.findViewById( starBurst[ winningChest ] );
                float sx, sy;
                
                //
                // Star Burst
                //
                sx = burst.getScaleX();
                sy = burst.getScaleY();
/*
                burst.setScaleY( .2f );
                burst.setScaleX( .2f );
*/
                burst.setAlpha( .0f );
                burst.setVisibility( View.VISIBLE );
                burst.animate().alpha( 1f ).setInterpolator( new LinearInterpolator() );
                burst.animate().setDuration( 500 ).start();
                
                //
                // Raise the item received
                //
                ImageView item = view_main.findViewById( itemWon[ winningChest ] );
                item.setImageResource( winnableItems[ itemReceived ] );
                item.setVisibility( View.VISIBLE );
/*
                item.setScaleX( .2f );
                item.setScaleY( .2f );
*/
                item.setTranslationY( 0 );
                item.animate().translationY( -getResources().getDimensionPixelSize( R.dimen._40sdp ) );
                item.animate().setDuration( 500 ).setInterpolator( new LinearInterpolator() );
                item.animate().withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
/*
                        if ( getActivity() != null )
                        {
                            ParticleSystem white = new ParticleSystem( getActivity(), 6, R.drawable.star_on, 1000 );
                            
                            white.setFadeOut( 1000 ).
                        }
*/
                    }
                } );
                item.animate().setStartDelay( 500 ).start();
                
                //
                // Message
                //
                GradientTextView message = view_main.findViewById( R.id.receivedItemText );
                
                message.setText( String.format( Locale.getDefault(), "You received %s!", itemNames[ itemReceived ] ) );
                message.setScaleX( .90f );
                message.setScaleY( .90f );
                message.animate().scaleY( 1f ).scaleX( 1f ).setDuration( 500 );
                message.animate().setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                message.animate().setStartDelay( 500 ).start();
                
                
                //
                super.onAnimationEnd( animation );
            }
        } );
        
        return winner;
    }
    
    
    @Override
    public void onDestroy()
    {
        
        view_main = null;
        gameEngine = null;
        
        super.onDestroy();
    }
}

