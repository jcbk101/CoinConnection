package com.genesyseast.coinconnection.Fragments;


import android.Manifest;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.Dialogs.PrizeDialog;
import com.genesyseast.coinconnection.Dialogs.RewardDialog;
import com.genesyseast.coinconnection.Dialogs.SettingsDialog;
import com.genesyseast.coinconnection.LevelSelector.LevelAdapter;
import com.genesyseast.coinconnection.LevelSelector.LevelScene;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.Support.GameState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class LevelSelector
        extends Fragment
        implements View.OnClickListener, LevelAdapter.OnLevelAdapterListener, PrizeDialog.OnPrizeClaimed
{
    private GameEngine gameEngine;
    
    public static int maxMoneyAllowed = 9999;
    
    private ListView              levelView;
    private ArrayList<LevelScene> levelList;
    private LevelAdapter          levelAdapter;
    private View                  view_main;
    private int[]                 scenes;
    
    
    // This will be applied to each menu layout for the menu navagation
    public LevelSelector()
    {
        // Required empty public constructor
        gameEngine = GameEngine.getInstance( getContext() );
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
    
    
    public LevelAdapter getLevelAdapter()
    {
        return levelAdapter;
    }
    
    /**
     * Build the views needed to display
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
        // Inflate the layout for this fragment
        view_main = inflater.inflate( R.layout.level_select_fragment, container, false );
        
        AppCompatDelegate.setCompatVectorFromResourcesEnabled( true );
        
        if ( gameEngine == null )
        {
            gameEngine = GameEngine.getInstance( getContext() );
        }
        
        if ( gameEngine.soundPlayer != null )
        {
            if ( !GameEngine.musicIsPlaying )
            {
                //@@@@@@@@@@@@@@@@@@@
                gameEngine.soundPlayer.playBgMusic( PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE, PlaySound.LOOP );
                gameEngine.currentBGMusic = PlaySound.MENU_BG_GONE_FISHIN_BY_MEMORAPHILE << 2;
            }
        }
        
        //
        TypedArray   array;
        SecureRandom r = new SecureRandom();
        
        array = getResources().obtainTypedArray( R.array.level_scenes );
        scenes = new int[ array.length() ];
        
        //
        for ( int i = 0; i < array.length(); i++ )
        {
            scenes[ i ] = array.getResourceId( i, 0 );
        }
        
        
        //#####################################
        //
        // Get the layouts used for the scenes
        //
        //#####################################
        array = getResources().obtainTypedArray( R.array.scene_layout_list );
        levelList = new ArrayList<>();
        
        //
        int base_level = ((array.length() - 2) * 12);
        
        for ( int i = 0; i < array.length(); i++ )
        {
            int c = array.getInteger( i, 0 );
            
            if ( c == -1 )
            {
                levelList.add( new LevelScene( R.layout.level_section_blank, c, -1, null ) );
                continue;
            }
            
            
            // Adjust accordingly
            int[] stars;
            
            base_level -= 12;
            stars = new int[ 12 ];
            
            //
            for ( int s = 0; s < stars.length; s++ )
            {
                stars[ s ] = GameEngine.getStarCountFromLevel( base_level + s );
                //                stars[ s ] = GameEngine.Level[ base_level + s ];
            }
            
            //
            levelList.add( new LevelScene( scenes[ c ], c, base_level, stars ) );
            stars = null;
        }
        //
        array.recycle();
        
        
        //################################
        //
        // Spin only one star burst
        //
        //################################
        View          frame = view_main.findViewById( R.id.giftFrame );
        View          burst;
        ImageTextView giftBox;
        
        // One for Gift, One for Bonus wheel
        ObjectAnimator obj;
        int            size = getResources().getDimensionPixelSize( R.dimen._24sdp );
        
        if ( GameEngine.giftWaiting )
        {
            burst = view_main.findViewById( R.id.starBurst1 );
            obj = ObjectAnimator.ofFloat( burst, "rotation", 0, -360 );
            
            burst.setPivotX( size );
            burst.setPivotY( size );
            frame.setOnClickListener( this );
            frame.setVisibility( View.VISIBLE );
            
            obj.setDuration( 20000 ).setInterpolator( new LinearInterpolator() );
            obj.setRepeatCount( ValueAnimator.INFINITE );
            obj.start();
            
            //
            // Gift Box animation
            //
            giftBox = view_main.findViewById( R.id.giftBoxHolder );
            ObjectAnimator giftSpin;
            int[]          armArray = GameEngine.arrayFromResource( getContext(), R.array.purple_gift );
            //TODO: fix bug
            giftSpin = ObjectAnimator.ofInt( giftBox, "imageResource", armArray );
            giftSpin.setEvaluator( new CustomEvaluator() );
            giftSpin.setInterpolator( new LinearInterpolator() );
            giftSpin.setDuration( armArray.length * 100 );
            giftSpin.setRepeatCount( ValueAnimator.INFINITE );
            giftSpin.start();
            //
            giftBox.setTag( giftSpin );
        }
        else
        {
            frame.setVisibility( View.GONE );
        }
        
        
        //################################
        //
        // Watch Ad animation
        //
        //################################
        final View watcher = view_main.findViewById( R.id.watchAdsFrame );
        
        watcher.animate().alpha( 1f ).setDuration( 10 ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                PropertyValuesHolder sx       = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.15f, 1 );
                PropertyValuesHolder sy       = PropertyValuesHolder.ofFloat( "scaleY", 1, 0.85f, 1 );
                ObjectAnimator       animator = ObjectAnimator.ofPropertyValuesHolder( watcher, sx, sy );
                animator.setDuration( 3000 ).setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
                animator.setRepeatCount( ValueAnimator.INFINITE );
                animator.start();
                
                watcher.setTag( animator );
            }
        } ).start();
        //
        watcher.setOnClickListener( this );
        
        
        //################################
        //
        // Bonus rounds icon
        //
        //################################
        setBonusStatus();
        
        View currency = view_main.findViewById( R.id.moneyFrame );
        currency.setOnClickListener( this );
        
        //        GameEngine.moneyOnHand = 9990;
        setMoneyStatus();
        setMailStatus();
        
        
        //##############################
        //
        // Player location button
        //
        //##############################
        View playerLoc = view_main.findViewById( R.id.playerLocationFrame );
        playerLoc.setOnClickListener( this );
        playerLoc.setVisibility( View.INVISIBLE );
        
        
        //##############################
        //
        // Bottom buttons
        //
        //##############################
        int[] buttonIDs = { R.id.settingsButton, R.id.homeButton/*, R.id.helpButton*/ };
        
        for ( int buttonID : buttonIDs )
        {
            View view = view_main.findViewById( buttonID );
            view.setOnClickListener( this );
        }
        
        
        //####################################
        //
        // Intercept the back key
        //
        //####################################
        view_main.setFocusableInTouchMode( true );
        view_main.requestFocus();
        view_main.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                    if ( getActivity() != null )
                    {
                        //                        getActivity().onBackPressed();
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new GameStartFragment(), FragmentLoader.REPLACE_FRAGMENT );
                        return true;
                    }
                }
                
                return false;
            }
        } );
        
        /*
        
        //################################
        //
        // Randomly build a map structure
        //
        //################################
        int i    = 0;
        int rand;
        int last = -1;
        
        Log.d( "", String.format( Locale.US, "<item>%d</item>\t <!-- %d -->", 0, i ) );
        
        for ( i = 0; i < (1500 / 12) - 2; i++ )
        {
            rand = r.nextInt( scenes.length - 2 );
            while ( rand == last )
            {
                rand = r.nextInt( scenes.length - 2 );
            }
            
            last = rand;
            Log.d( "", String.format( Locale.US, "<item>%d</item>\t <!-- %d -->", rand + 1, i + 1 ) );
        }
        
        Log.d( "", String.format( Locale.US, "<item>%d</item>\t <!-- %d -->", scenes.length - 1, i ) );
*/
        
        view_main.setVisibility( View.INVISIBLE );
        loadLevelSelectData();
        
        return view_main;
    }
    
    
    /**
     * //##############################
     * <p>
     * Display the goodness
     * <p>
     * //##############################
     */
    public void loadLevelSelectData()
    {
        view_main.setVisibility( View.VISIBLE );
        view_main.setAlpha( 0f );
        view_main.animate().alpha( 1f ).setDuration( 500 );
        view_main.animate().withStartAction( new Runnable()
        {
            @Override
            public void run()
            {
                //##############################
                //
                // List of level maps
                //
                //##############################
                try
                {
                    levelView = view_main.findViewById( R.id.levelLister );
                    levelAdapter = new LevelAdapter( getContext(), 0, levelList, levelView, scenes.length );
                    levelAdapter.setOnLevelAdapterListener( LevelSelector.this );
                    //levelAdapter.setCurrentLevel( currentLevel );
                    
                    //
                    levelView.setAdapter( levelAdapter );
                    levelView.setSelection( (levelList.size() - 2) - (GameEngine.currentLevel / 12) );
                    levelView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
                    {
                        @Override
                        public void onGlobalLayout()
                        {
                            //At this point the layout is complete and the
                            //dimensions of ListView and any child views are known.
                            //Remove listener after changed ListView's height to prevent infinite loop
                            levelView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                            
                            gotoCurrentLevel();
                        }
                    } );
                    
                    // Scrolling listener
                    levelView.setOnScrollListener( new AbsListView.OnScrollListener()
                    {
                        @Override
                        public void onScrollStateChanged( AbsListView absListView, int i )
                        {
                            View playerLocFrame = view_main.findViewById( R.id.playerLocationFrame );
                            
                            //
                            if ( i == SCROLL_STATE_IDLE )
                            {
                                setPlayerGps();
                            }
                            else if ( i == SCROLL_STATE_TOUCH_SCROLL )
                            {
                                playerLocFrame.setVisibility( View.INVISIBLE );
                            }
                        }
                        
                        @Override
                        public void onScroll( AbsListView absListView, int i, int i1, int i2 )
                        {
                        }
                    } );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }
    
    
    /**
     * //############################
     * <p>
     * Scroll to the current level
     * <p>
     * //############################
     */
    private void gotoCurrentLevel()
    {
        int[] itvList = {
                R.id.levelSlot1,
                R.id.levelSlot2,
                R.id.levelSlot3,
                R.id.levelSlot4,
                R.id.levelSlot5,
                R.id.levelSlot6,
                R.id.levelSlot7,
                R.id.levelSlot8,
                R.id.levelSlot9,
                R.id.levelSlot10,
                R.id.levelSlot11,
                R.id.levelSlot12
        };
        int  index        = (GameEngine.currentLevel % 12);
        View selectedItem = null;
        
        //
        for ( int i = 0; i < levelView.getChildCount(); i++ )
        {
            View v = levelView.getChildAt( i );
            
            if ( v.getTag() != null )
            {
                LevelAdapter.ViewHolder holder = ( LevelAdapter.ViewHolder ) v.getTag();
                
                if ( (holder.levelBase / 12) == (GameEngine.currentLevel / 12) )
                {
                    selectedItem = v;
                    break;
                }
            }
        }
        
        //
        if ( selectedItem != null )
        {
            final View selectedLevel = selectedItem.findViewById( itvList[ index ] );
            
            int place = (levelView.getHeight() / 2);
            levelView.setSelectionFromTop( (levelList.size() - 2) - (GameEngine.currentLevel / 12), (place - selectedLevel.getTop()) );
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Click listener
     * <p>
     * //##########################
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        int        id   = v.getId();
        final View view = getView();
        
        
        if ( id == R.id.moneyFrame )
        {
            pressedButtonClick( v ).start();
            
            // Watch Video or Buy coins :(
            GameEngine.moneyOnHand += 1;
            setMoneyStatus();
            
            return;
        }
        else if ( id == R.id.settingsButton )
        {
            //            Toast.makeText( this, "Settings was clicked", Toast.LENGTH_SHORT ).show();
            if ( getActivity() != null )
            {
                SettingsDialog dialog = new SettingsDialog();
                dialog.setCancelable( false );
                dialog.show( getActivity().getSupportFragmentManager(), "Settings" );
            }
            
            return;
        }
        else if ( id == R.id.homeButton )
        {
            if ( getActivity() != null )
            {
                //                        getActivity().onBackPressed();
                FragmentLoader.SwapFragment( getActivity(), getContext(), new GameStartFragment(), FragmentLoader.REPLACE_FRAGMENT );
            }
            
            return;
        }
/*
        else if ( id == R.id.helpButton )
        {
            return;
        }
*/
        
        
        //##########################
        //
        //
        //
        //##########################
        bounceButtonClick( getActivity(), v, true ).start();
        
        //
        if ( id == R.id.playerLocationFrame )
        {
            levelView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
            {
                @Override
                public void onGlobalLayout()
                {
                    //At this point the layout is complete and the
                    //dimensions of ListView and any child views are known.
                    //Remove listener after changed ListView's height to prevent infinite loop
                    levelView.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                    
                    gotoCurrentLevel();
                    
                    // Set Player GPS
                    setPlayerGps();
                }
            } );
            
            levelView.setSelection( (levelList.size() - 2) - (GameEngine.currentLevel / 12) );
        }
        else if ( id == R.id.bonusRoundsFrame )
        {
            if ( GameEngine.bonusCount > 0 && view != null )
            {
                //
                // Transition to Slots Bonus Game
                //
                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view.getTag() == null )
                        {
                            gameEngine.animatorList.add( animation );
                            view.setTag( 1 );
                        }
                    }
                } );
                view.animate().alpha( 0f ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new PlaySlots(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new PlaySlots(), FragmentLoader.REPLACE_FRAGMENT );
                        view.setTag( null );
                    }
                } ).start();
            }
        }
        else if ( id == R.id.mailCheckFrame )
        {
            //            Toast.makeText( this, "Mail was clicked", Toast.LENGTH_SHORT ).show();
            if ( GameEngine.mailOnHand > 0 )
            {
                // Get the message from the service
                ErrorDialog dialog = new ErrorDialog();
                dialog.setCancelable( false );
                dialog.setAlertIcon( R.drawable.mail_front_gold );
                dialog.setTitle( "Mail" );
                
                // Decrease messages
                GameEngine.mailOnHand--;
                
                if ( GameEngine.mailOnHand > 1 )
                {
                    dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON | ErrorDialog.CLOSE_BUTTON );
                    dialog.setYesText( "Next" ).setNoText( "Previous" );
                    dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
                    {
                        @Override
                        public void onErrorExitClick( int buttonClicked )
                        {
                            if ( buttonClicked == ErrorDialog.NO_BUTTON )
                            {
                                //                                dialog.setMessage( getPreviousMessage() );
                            }
                            else
                            {
                                //                                dialog.setMessage( getNextMessage() );
                            }
                        }
                    } );
                }
                else
                {
                    dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.CLOSE_BUTTON );
                    dialog.setYesText( "Done" );
                    //                    dialog.setMessage( getNextMessge() );
                }
                
                //###########################
                //
                // Display what we have
                //
                //###########################
                if ( getActivity() != null )
                {
                    dialog.show( getActivity().getSupportFragmentManager(), "Messages" );
                }
                
                //
                setMailStatus();
            }
        }
        else if ( id == R.id.giftFrame )
        {
            //
            // Transition to Chest Selections
            //
            if ( view != null )
            {
                view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view.getTag() == null )
                        {
                            gameEngine.animatorList.add( animation );
                            view.setTag( 1 );
                        }
                    }
                } );
                view.animate().alpha( 0f ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new PlayChests(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new PlayChests(), FragmentLoader.REPLACE_FRAGMENT );
                        view.setTag( null );
                    }
                } ).start();
            }
        }
        else if ( id == R.id.watchAdsFrame )
        {
            if ( getActivity() != null )
            {
                RewardDialog dialog = new RewardDialog();
                dialog.show( getActivity().getSupportFragmentManager(), "Reward" );
            }
            
            GameEngine.moneyOnHand += 2;
            setMoneyStatus();
        }
    }
    
    
    /**
     * //##############################
     * <p>
     * Determine if the compass
     * needs to be shown
     * <p>
     * //##############################
     */
    private void setPlayerGps()
    {
        View           player         = levelAdapter.getPlayer();
        View           playerLocFrame = view_main.findViewById( R.id.playerLocationFrame );
        View           pointer        = view_main.findViewById( R.id.compassPointer );
        int            screenH        = Resources.getSystem().getDisplayMetrics().heightPixels;
        int[]          pLoc           = new int[ 2 ];
        ObjectAnimator obj;
        
        
        if ( player != null )
        {
            player.getLocationOnScreen( pLoc );
            
            int pHeight = player.getHeight();
            
            // Check the top
            //                                    if ( (pHeight + pLoc[ 1 ]) < header.getHeight() || (pLoc[ 1 ] >= screenH) || !player.isShown() )
            if ( (pHeight + pLoc[ 1 ]) < 0 || (pLoc[ 1 ] >= screenH) || !player.isShown() )
            {
                playerLocFrame.setVisibility( View.VISIBLE );
                
                // Animate the pointer in the
                // direction the player current is in
                if ( player.isShown() )
                {
                    if ( pLoc[ 1 ] >= screenH )
                    {   // Pointer needs to point down
                        obj = ObjectAnimator.ofFloat( pointer, "rotation", 180, 205, 155, 180 );
                    }
                    else
                    {   // Pointer needs to point up
                        obj = ObjectAnimator.ofFloat( pointer, "rotation", 0, -25, 25, 0 );
                    }
                }
                else
                {
                    // Player too far out of listview
                    // Use level base and current level to detrmine direction
                    if ( levelAdapter.getPlayerDirection() == 1 )
                    {   // Pointer needs to point down
                        obj = ObjectAnimator.ofFloat( pointer, "rotation", 180, 205, 155, 180 );
                    }
                    else
                    {   // Pointer needs to point up
                        obj = ObjectAnimator.ofFloat( pointer, "rotation", 0, -25, 25, 0 );
                    }
                }
                
                
                pointer.setPivotX( pointer.getWidth() / 2f );
                pointer.setPivotY( pointer.getHeight() / 2f );
                obj.setDuration( 5000 ).setInterpolator( new LinearInterpolator() );
                obj.setRepeatCount( ValueAnimator.INFINITE );
                obj.start();
                pointer.setTag( obj );
            }
            else
            {
                playerLocFrame.setVisibility( View.INVISIBLE );
                
                if ( pointer.getTag() != null )
                {
                    (( ObjectAnimator ) pointer.getTag()).cancel();
                }
                //
                pointer.setTag( null );
            }
        }
        else
        {
            playerLocFrame.setVisibility( View.VISIBLE );
            
            // Player too far out of listview
            // Use level base and current level to detrmine direction
            if ( levelAdapter.getPlayerDirection() == 1 )
            {   // Pointer needs to point down
                obj = ObjectAnimator.ofFloat( pointer, "rotation", 180, 205, 155, 180 );
            }
            else
            {   // Pointer needs to point up
                obj = ObjectAnimator.ofFloat( pointer, "rotation", 0, -25, 25, 0 );
            }
            
            
            // Animate the pointer in the
            // direction the player current is in
            //            obj = ObjectAnimator.ofFloat( pointer, "rotation", 0, 360 );
            
            pointer.setPivotX( pointer.getWidth() / 2f );
            pointer.setPivotY( pointer.getHeight() / 2f );
            obj.setDuration( 5000 ).setInterpolator( new LinearInterpolator() );
            obj.setRepeatCount( ValueAnimator.INFINITE );
            obj.start();
            pointer.setTag( obj );
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * method to adjust the bonus
     * feature for spins
     * <p>
     * //#############################
     */
    public void setBonusStatus()
    {
        View                   frame;
        final ObjectAnimator[] obj   = new ObjectAnimator[ 2 ];
        final View             wheel = view_main.findViewById( R.id.bonusWheel );
        View                   burst = view_main.findViewById( R.id.starBurst2 );
        frame = view_main.findViewById( R.id.bonusRoundsFrame );
        
        
        if ( GameEngine.bonusCount > 0 )
        {
            // Normal
            burst.setBackgroundResource( R.drawable.small_star_burst );
            burst = view_main.findViewById( R.id.bonusWheelMarker );
            burst.setBackgroundResource( R.drawable.bonus_marker );
            burst = view_main.findViewById( R.id.bonusTextLabel );
            burst.setVisibility( View.VISIBLE );
            
            //
            if ( wheel.getTag() == null )
            {
                wheel.animate().alpha( 1f ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        View burst = view_main.findViewById( R.id.starBurst2 );
                        
                        obj[ 0 ] = ObjectAnimator.ofFloat( wheel, "rotation", 0, 720 );
                        obj[ 1 ] = ObjectAnimator.ofFloat( burst, "rotation", 0, -360 );
                        
                        //
                        wheel.setPivotX( wheel.getWidth() / 2f );
                        wheel.setPivotY( wheel.getHeight() / 2f );
                        burst.setPivotX( burst.getWidth() / 2f );
                        burst.setPivotY( burst.getHeight() / 2f );
                        
                        //
                        obj[ 0 ].setDuration( 10000 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                        obj[ 0 ].setRepeatCount( ValueAnimator.INFINITE );
                        
                        obj[ 1 ].setDuration( 20000 ).setInterpolator( new LinearInterpolator() );
                        obj[ 1 ].setRepeatCount( ValueAnimator.INFINITE );
                        
                        //
                        obj[ 0 ].start();
                        obj[ 1 ].start();
                        
                        //
                        wheel.setTag( obj );
                    }
                } ).start();
            }
            
            
            frame.setOnClickListener( this );
            frame.setVisibility( View.VISIBLE );
            
            TextView tv = frame.findViewById( R.id.bonusRoundsBadge );
            tv.setText( String.format( Locale.getDefault(), "%d", GameEngine.bonusCount ) );
            tv.setVisibility( View.VISIBLE );
            tv.setAlpha( 1f );
        }
        else
        {
            if ( wheel.getTag() != null )
            {
                ObjectAnimator[] objs = (( ObjectAnimator[] ) wheel.getTag());
                
                objs[ 0 ].end();
                objs[ 1 ].end();
                
                wheel.setTag( null );
                GameEngine.bonusCount = 0;
            }
            
            // Disabled
            burst.setBackgroundResource( 0 );
            burst = view_main.findViewById( R.id.bonusWheelMarker );
            burst.setBackgroundResource( R.drawable.bonus_base_gray );
            burst = view_main.findViewById( R.id.bonusTextLabel );
            burst.setVisibility( View.INVISIBLE );
            
            frame.setOnClickListener( null );
            TextView tv = frame.findViewById( R.id.bonusRoundsBadge );
            tv.setVisibility( View.INVISIBLE );
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * Get messages from cloud
     * messages
     * <p>
     * //#############################
     */
    public void setMailStatus()
    {
        View                 frame;
        ObjectAnimator[]     obj  = new ObjectAnimator[ 2 ];
        PropertyValuesHolder flipY;
        PropertyValuesHolder rot;
        PropertyValuesHolder index;
        int                  size = getResources().getDimensionPixelSize( R.dimen._24sdp );
        
        frame = view_main.findViewById( R.id.mailCheckFrame );
        final ImageView mail = view_main.findViewById( R.id.mailCheckButton );
        
        if ( GameEngine.mailOnHand > 0 )
        {
            if ( mail.getTag() == null )
            {
                //                flipY = PropertyValuesHolder.ofFloat( "rotationY", 0, 360 );
                rot = PropertyValuesHolder.ofFloat( "rotation", 0, 15, 0, -15, 0, 15, 0, -15, 0 );
                
                obj[ 0 ] = ObjectAnimator.ofFloat( mail, "rotationY", 0, 360 );
                obj[ 1 ] = ObjectAnimator.ofFloat( mail, "rotation", 0, 10, 0, -10, 0 );
                //
                obj[ 0 ].addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator valueAnimator )
                    {
                        float num = Math.abs( ( float ) valueAnimator.getAnimatedValue() );
                        
                        if ( (num >= 0 && num < 90) || (num >= 270 && num <= 360) )
                        {
                            mail.setImageResource( R.drawable.mail_back_gold );
                        }
                        else
                        {
                            mail.setImageResource( R.drawable.mail_front_gold );
                        }
                    }
                } );
                
                //
                mail.setPivotX( size );
                mail.setPivotY( size );
                obj[ 0 ].setInterpolator( new LinearInterpolator() );
                obj[ 1 ].setInterpolator( new LinearInterpolator() );
                //
                obj[ 0 ].setDuration( 3000 );
                obj[ 1 ].setDuration( 4500 );
                //
                obj[ 0 ].setRepeatCount( ValueAnimator.INFINITE );
                obj[ 1 ].setRepeatCount( ValueAnimator.INFINITE );
                //
                obj[ 0 ].start();
                obj[ 1 ].start();
                //
                mail.setTag( obj );
            }
            
            frame.setOnClickListener( this );
            frame.setVisibility( View.VISIBLE );
            
            TextView tv = frame.findViewById( R.id.mailCheckBadge );
            tv.setText( String.format( Locale.getDefault(), "%d", GameEngine.mailOnHand ) );
            tv.setVisibility( View.VISIBLE );
            tv.setAlpha( 1f );
        }
        else
        {
            if ( mail.getTag() != null )
            {
                obj = ( ObjectAnimator[] ) mail.getTag();
                
                //
                obj[ 0 ].end();
                obj[ 1 ].end();
                obj[ 0 ].removeAllListeners();
                obj[ 1 ].removeAllListeners();
                
                //
                mail.setTag( null );
                GameEngine.mailOnHand = 0;
            }
            
            frame.setOnClickListener( null );
            TextView tv = frame.findViewById( R.id.mailCheckBadge );
            tv.setVisibility( View.INVISIBLE );
            //
            mail.setImageResource( R.drawable.mail_back_off );
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * method to adjust the bonus
     * feature for spins
     * <p>
     * //#############################
     */
    public void setMoneyStatus()
    {
        TextView money;
        
        GameEngine.moneyOnHand = Math.min( GameEngine.moneyOnHand, maxMoneyAllowed );
        
        money = view_main.findViewById( R.id.moneyHolder );
        money.setText( String.format( Locale.getDefault(), "%d", GameEngine.moneyOnHand ) );
    }
    
    
    /**
     * //#############################
     * <p>
     * Communicate the Level selector
     * <p>
     * //############################
     */
    @Override
    public void prizeWasClaimed()
    {
        setMoneyStatus();
    }
    
    
    /**
     * //#############################
     * <p>
     * animate the click of a button
     * with a bounce effect
     * <p>
     * //#############################
     *
     * @param v
     */
    public static ObjectAnimator bounceButtonClick( Activity activity, final View v, boolean showParticles )
    {
/*
        if ( activity != null && showParticles )
        {
            // Particle effect
            //        ParticleSystem red   = new ParticleSystem( MainActivity.this, 33, R.drawable.particle_red, 1000 );
            ParticleSystem white = new ParticleSystem( activity, 33, R.drawable.particle_white, 1000 );
            ParticleSystem black = new ParticleSystem( activity, 33, R.drawable.particle_black, 1000 );
            
            //        red.setSpeedRange( 0.02f, 0.075f ).setFadeOut( 1000 ).oneShot( v, 16 );
            white.setSpeedRange( 0.02f, 0.075f ).setFadeOut( 1000 ).oneShot( v, 16 );
            black.setSpeedRange( 0.02f, 0.075f ).setFadeOut( 1000 ).oneShot( v, 16 );
        }
        
*/
        PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX", .8f, 1 );
        PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY", .8f, 1 );
        ObjectAnimator       obj = ObjectAnimator.ofPropertyValuesHolder( v, sx, sy );
        
        obj.setDuration( 500 ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
        
/*
        v.setScaleX( .8f );
        v.setScaleY( .8f );
        v.animate().scaleX( 1f ).scaleY( 1f ).setDuration( 500 );
        //
        v.animate().setInterpolator( new CustomBounceInterpolator( .2, 20 ) ).start();
*/
        return obj;
    }
    
    
    /**
     * //#############################
     * <p>
     * animate the click of a button
     * with a bounce effect
     * <p>
     * //#############################
     *
     * @param v
     */
    public static ObjectAnimator bounceButtonClick( final View v )
    {
        PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX", .8f, 1 );
        PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY", .8f, 1 );
        ObjectAnimator       obj = ObjectAnimator.ofPropertyValuesHolder( v, sx, sy );
        
        obj.setDuration( 350 ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
        
        return obj;
    }
    
    
    /**
     * //#############################
     * <p>
     * animate the click of a button
     * with a bounce effect
     * <p>
     * //#############################
     *
     * @param v
     */
    public static ObjectAnimator pressedButtonClick( View v )
    {
        PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX", 1f, 0.85f, 1f );
        PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY", 1f, 0.85f, 1f );
        ObjectAnimator       obj = ObjectAnimator.ofPropertyValuesHolder( v, sx, sy );
        
        obj.setDuration( 100 ).setInterpolator( new LinearInterpolator() );
        //        obj.start();
        
        return obj;
    }
    
    
    /**
     * //###############################
     * <p>
     * Request to select an avatar
     * <p>
     * //###############################
     */
    @Override
    public void onAvatarSelector()
    {
        imageSelect();
    }
    
    
    /**
     * //###############################
     * <p>
     * Allow user to view and select
     * prizes earned during play
     * <p>
     * //###############################
     *
     * @param baseLevel
     */
    @Override
    public void onPrizeViewerSelected( int baseLevel )
    {
        if ( getActivity() != null )
        {
            PrizeDialog dialog = new PrizeDialog( baseLevel );
            dialog.setCancelable( false );
            dialog.setOnPrizeClaimed( this );
            dialog.setOnDismisser( new PrizeDialog.OnDismisser()
            {
                @Override
                public void onDismiss()
                {
                    levelAdapter.notifyDataSetChanged();
                }
            } );
            dialog.show( getActivity().getSupportFragmentManager(), "Prize" );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * When a level change has taken
     * place
     * <p>
     * //###############################
     *
     * @param currentLevel
     */
    @Override
    public void onLevelSelected( int currentLevel )
    {
        GameEngine.currentLevel = currentLevel;
        setPlayerGps();
        
        final View   view = getView();
        SecureRandom r    = new SecureRandom();
        
        
        //####################################
        // No need to continue if animations
        // are disabled
        //####################################
        if ( getContext() != null && MainActivity.checkSystemAnimationsDuration( getContext() ) == 0 )
        {
            ErrorDialog dialog = new ErrorDialog();
            
            dialog.setMessage( getString( R.string.anims_disabled ) );
            dialog.setTitle( "Animation Error" );
            dialog.setYesText( "Exit" );
            dialog.setAlertIcon( android.R.drawable.ic_dialog_alert );
            dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
            {
                @Override
                public void onErrorExitClick( int buttonClicked )
                {
                    if ( getActivity() != null )
                    {
                        getActivity().finish();
                    }
                    System.exit( 1 );
                }
            } );
            
            //
            if ( getActivity() != null )
            {
                //@@@@@@@@@@@@ Error Dialog Sound
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.ERROR );
                }
                dialog.show( getActivity().getSupportFragmentManager(), "Error" );
            }
            
            return;
        }
        
        //
        //####################################
        //
        if ( view != null )
        {
            //@@@@@@@@@@@@@@@@@@ Level Select
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.LEVEL_SELECT );
                
                // Fadeout the music
                gameEngine.soundPlayer.fadeOutMusic();
            }
            
            //###################################
            //
            // Erase the resume information
            //
            //###################################
            GameEngine.resumeGame = false;
            //
            if ( getContext() != null )
            {
                GameState.eraseSavedGame( getContext() );
            }
            
            // Only reset if player didn't leave a game via PAUSE->Menu
            if ( !GameEngine.reloadSameGame )
            {
                GameEngine.currentBoardImage = -1;
                GameEngine.currentBackground = -1;
            }
            
            
            // Transition to Game board
            view.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( view.getTag() == null )
                    {
                        gameEngine.animatorList.add( animation );
                        view.setTag( 1 );
                    }
                }
            } );
            view.animate().alpha( 0f ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    //
                    // Testing: Every 10th level is card-matching
                    //
/*
                    if ( (GameEngine.currentLevel % GameEngine.CARD_GAME_LEVEL) == 0 && GameEngine.currentLevel > 0 )
                    {
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.REPLACE_FRAGMENT );
                    }
                    else
*/
                    {
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new ConnectionsFragment(), FragmentLoader.REPLACE_FRAGMENT );
                    }
                    
                    view.setTag( null );
                }
            } ).start();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * When a level reload has taken
     * place
     * <p>
     * //###############################
     *
     * @param currentLevel
     */
    @Override
    public void onExistingLevelSelected( int currentLevel )
    {
        ErrorDialog dialog = new ErrorDialog();
        
        dialog.setMessage( String.format( Locale.getDefault(), "Level %d already completed.\nReplay this level?", currentLevel + 1 ) )
              .setTitle( "Completed level" )
              .setAlertIcon( R.drawable.ic_help_black_24dp );
        dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON );
        dialog.setYesText( "Yes" ).setNoText( "No" ).setOnErrorListener( new ErrorDialog.OnErrorListener()
        {
            @Override
            public void onErrorExitClick( int buttonClicked )
            {
                if ( buttonClicked == ErrorDialog.YES_BUTTON )
                {
                    // This is restored upon level exit / completion
                    GameEngine.reloadedLevel = GameEngine.currentLevel;
                    GameEngine.currentLevel = currentLevel;
                    onLevelSelected( currentLevel );
                }
                
                //
                dialog.dismiss();
            }
        } );
        
        
        if ( getActivity() != null )
        {
            dialog.show( getActivity().getSupportFragmentManager(), "Reload" );
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Results
     * <p>
     * //###############################
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        
        // Check which request we're responding to
        if ( requestCode == 10 )
        {
            // Make sure the request was successful
            if ( resultCode == RESULT_OK )
            {
                // The user picked a image.
                // The Intent's data Uri identifies which item was selected.
                if ( data != null )
                {
                    // This is the key line item, URI specifies the name of the data
                    Uri mImageUri = data.getData();
                    
                    // Make a copy for reloading later
                    OutputStream   outputStream;
                    Bitmap         bitmap;
                    File           filePath;
                    File           dir;
                    File           file;
                    ImageView      dummy;
                    BitmapDrawable drawable;
                    boolean        haveDir = false;
                    
                    //
                    // Greater than API 29
                    //
                    filePath = getContext().getFilesDir();
                    dir = new File( filePath.getAbsoluteFile() + "/avatar/" );
                    
                    if ( !dir.exists() )
                    {
                        haveDir = dir.mkdir();
                    }
                    else
                    {
                        haveDir = true;
                    }
                    
                    //###########################
                    //
                    // Check again
                    //
                    //###########################
                    if ( haveDir )
                    {
                        dummy = new ImageView( getContext() );
                        dummy.setImageURI( mImageUri );
                        
                        drawable = ( BitmapDrawable ) dummy.getDrawable();
                        bitmap = drawable.getBitmap();
                        
                        // Save to file
                        file = new File( dir, "avatar.png" );
                        
                        try
                        {
                            outputStream = new FileOutputStream( file );
                            
                            Bitmap newBitmap = scaleBitmap( bitmap );
                            newBitmap.compress( Bitmap.CompressFormat.PNG, 100, outputStream );
                            
                            //
                            outputStream.flush();
                            outputStream.close();
                            
                            // Set the photo
                            levelAdapter.setBitmap( newBitmap );
                        }
                        catch ( IOException e )
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Toast.makeText( getContext(), "Error: Could not load the image file.", Toast.LENGTH_SHORT ).show();
                    }
/*
                    else
                    {
                        filePath = Environment.getDataDirectory();
                        dir = new File( filePath.getAbsoluteFile() + "/avatar" );
    
                        if ( dir.exists() )
                        {
                            dummy = new ImageView( this );
                            dummy.setImageURI( mImageUri );
        
                            drawable = ( BitmapDrawable ) dummy.getDrawable();
                            bitmap = drawable.getBitmap();
        
                            // Save to file
                            file = new File( dir, "avatar.png" );
        
                            try
                            {
                                outputStream = new FileOutputStream( file );
                                bitmap.compress( Bitmap.CompressFormat.PNG, 100, outputStream );
            
                                //
                                outputStream.flush();
                                outputStream.close();
                            }
                            catch ( IOException e )
                            {
                                e.printStackTrace();
                            }
                        }
                    }
*/
                }
            }
        }
    }
    
    
    /**
     * Resize before saving
     *
     * @param mBitmap
     *
     * @return
     */
    public Bitmap scaleBitmap( Bitmap mBitmap )
    {
        float ScaleSize       = 128;
        float width           = mBitmap.getWidth();
        float height          = mBitmap.getHeight();
        float excessSizeRatio = width > height ? (width / ScaleSize) : (height / ScaleSize);
        
        Bitmap bitmap = Bitmap.createScaledBitmap( mBitmap, ( int ) (width / excessSizeRatio), ( int ) (height / excessSizeRatio), false );
        
        // delete the temp image loaded
        mBitmap.recycle();
        
        return bitmap;
    }
    
    
    /**
     * //#############################
     * <p>
     * Find a photo to use
     * <p>
     * //#############################
     */
    public void imageSelect()
    {
        if ( permissionsCheck() )
        {
            android.content.Intent intent;
            
            intent = new Intent( android.content.Intent.ACTION_OPEN_DOCUMENT );
            intent.addCategory( android.content.Intent.CATEGORY_OPENABLE );
            intent.setType( "image/*" );
            
            startActivityForResult( android.content.Intent.createChooser( intent, "Select Picture" ), 10 );
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * MUST have permission to access
     * such files
     * <p>
     * //#############################
     */
    public boolean permissionsCheck()
    {
        if ( getActivity() != null && getContext() != null )
        {
            if ( ContextCompat.checkSelfPermission( getContext(), Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
            {
                //            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1 );
/*
            if ( ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.READ_EXTERNAL_STORAGE ) )
            {
*/
                ActivityCompat.requestPermissions( getActivity(), new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 10 );
                //            }
            }
            else
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * //##############################
     * <p>
     * Handle File access permission
     * results
     * <p>
     * //##############################
     *
     * @param requestCode  N/A
     * @param permissions  N/A
     * @param grantResults N/A
     */
    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        if ( requestCode == 10 )
        {
            if ( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED )
            {
                Intent intent;
                
                intent = new Intent( android.content.Intent.ACTION_OPEN_DOCUMENT );
                intent.addCategory( android.content.Intent.CATEGORY_OPENABLE );
                intent.setType( "image/*" );
                
                startActivityForResult( android.content.Intent.createChooser( intent, "Select Picture" ), 10 );
            }
            else
            {
                Toast.makeText( getContext(), "Permission DENIED", Toast.LENGTH_SHORT ).show();
            }
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Free up needed memory. No leaks!
     * <p>
     * //################################
     */
    @Override
    public void onDestroyView()
    {
        View giftBox = view_main.findViewById( R.id.giftBoxHolder );
        if ( giftBox.getTag() != null )
        {
            ObjectAnimator obj = (( ObjectAnimator ) giftBox.getTag());
            obj.end();
            giftBox.setTag( null );
        }
        
        View pointer = view_main.findViewById( R.id.compassPointer );
        if ( pointer.getTag() != null )
        {
            ObjectAnimator obj = (( ObjectAnimator ) pointer.getTag());
            obj.end();
            pointer.setTag( null );
        }
        
        
        View wheel = view_main.findViewById( R.id.bonusWheel );
        if ( wheel.getTag() != null )
        {
            ObjectAnimator[] objs = (( ObjectAnimator[] ) wheel.getTag());
            
            objs[ 0 ].end();
            objs[ 1 ].end();
            
            wheel.setTag( null );
        }
        
        ImageView mail = view_main.findViewById( R.id.mailCheckButton );
        if ( mail.getTag() != null )
        {
            ObjectAnimator[] objs = ( ObjectAnimator[] ) mail.getTag();
            
            //
            objs[ 0 ].end();
            objs[ 1 ].end();
            objs[ 0 ].removeAllListeners();
            objs[ 1 ].removeAllListeners();
            
            //
            mail.setTag( null );
        }
        
        View watcher = view_main.findViewById( R.id.watchAdsFrame );
        if ( watcher.getTag() != null )
        {
            ObjectAnimator obj = (( ObjectAnimator ) watcher.getTag());
            obj.end();
            watcher.setTag( null );
        }
        
        levelView = null;
        levelList = null;
        levelAdapter = null;
        view_main = null;
        scenes = null;
        gameEngine = null;
        pointer = null;
        watcher = null;
        mail = null;
        wheel = null;
        giftBox = null;
        
        super.onDestroyView();
    }
}