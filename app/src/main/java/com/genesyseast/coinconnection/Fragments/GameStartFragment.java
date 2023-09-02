package com.genesyseast.coinconnection.Fragments;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.genesyseast.coinconnection.CustomControls.ImageScroll;
import com.genesyseast.coinconnection.Dialogs.SettingsDialog;
import com.genesyseast.coinconnection.ShowCase.ShowClass;
import com.genesyseast.coinconnection.Support.AnimationValues;
import com.genesyseast.coinconnection.Support.BlurEffect;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.GameState;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.Dialogs.AboutDialog;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameStartFragment
        extends Fragment
        implements View.OnClickListener
{
    public static String      APP_PNAME = "com.genesyseast.coinconnection";
    private       GameEngine  gameEngine;
    // This will be applied to each menu layout for the menu navagation
    private       View        view_main;
    private       ShareDialog shareDialog;
    private       boolean     resumeShown;
    
    private int[] long_buttons = {
            R.id.playResumeButton, R.id.playButton, R.id.settingsButton, R.id.aboutApp
    };
    
    private int[] square_buttons = {
            R.id.aboutApp, R.id.rateMe, R.id.emailButton, R.id.webpageButton, R.id.devApps, R.id.socialMedia
    };
    
    
    public GameStartFragment()
    {
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
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        view_main = inflater.inflate( R.layout.game_start_fragment, container, false );
        
        // Required empty public constructor
        gameEngine = GameEngine.getInstance( getContext() );
        
        
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
                        getActivity().onBackPressed();
                        return true;
                    }
                }
                
                return false;
            }
        } );
        
        
        //####################################
        // Animate the view
        // Start: Fade in
        // Return from a "pop fragment": previous view was faded out
        //####################################
        view_main.setAlpha( 0f );
        //
        
        initButtons();
        loadStartData( view_main );
        
        // Inflate the layout for this fragment
        return view_main;
    }
    
    
    /**
     * Universal data loader
     *
     * @param view_main
     */
    private void loadStartData( View view_main )
    {
        final View        title    = view_main.findViewById( R.id.mainTitle );
        final ImageScroll scroller = view_main.findViewById( R.id.bgScroller );
        
        
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
        //        scroller.scrollTheImage( 30000, 1, ValueAnimator.INFINITE );
        
        //
        title.setVisibility( View.VISIBLE );
        title.setAlpha( 1 );
        view_main.animate().setDuration( 1000 ).alpha( 1f ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                ImageView bgView = view_main.findViewById( R.id.blurStartScreen );
                bgView.setVisibility( View.INVISIBLE );
                
                if ( getActivity() != null )
                {
                    bgView.setImageDrawable( getBlurredBg( getActivity() ) );
                }
                
                rateMyApp();
            }
        } ).start();
    }
    
    
    /**
     * //##############################
     * <p>
     * Get the button listeners and
     * animations started
     * <p>
     * //##############################
     */
    public void initButtons()
    {
        try
        {
            if ( getContext() != null && getActivity() != null )
            {
                
                final GradientTextView button;
                
                
                //#############################
                //
                // Resume game button
                //
                //#############################
                if ( GameState.getSaveStatus( getContext() ) )
                {
                    button = view_main.findViewById( R.id.playResumeButton );
                    button.setOnClickListener( GameStartFragment.this );
                    
                    //
                    button.setVisibility( View.VISIBLE );
                    button.setScaleX( 0f );
                    button.setScaleY( 0f );
                    button.animate().scaleX( 1f ).scaleY( 1f ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                    button.animate().setDuration( AnimationValues.SLIDE_TIME );
                    button.animate().setStartDelay( (AnimationValues.FADE_TIME << 1) + (AnimationValues.FADE_TIME / 2) );
                    button.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if ( getContext() != null )
                            {
                                resumeShown = GameEngine.getResumeHelper( getContext() );
                                
                                // Provide a showcase for first showing
                                if ( !resumeShown && getActivity() != null )
                                {
                                    ShowClass showClass = new ShowClass( getContext() );
                                    showClass.addTarget( button, true, 500, 0, new LinearInterpolator(), ShowClass.ROUNDED_RECT, ShowClass.CENTER_OUT )
                                             .addTargetTip( showClass.getTargetCount(), button, "Resume Game", "Tap this button to resume your game.",
                                                            R.drawable.bubble_center_purple_a, ShowClass.TIP_CENTER
                                                          )
                                             //                     .setOnCloseType( ShowClass.ON_TOUCH )
                                             /*
                                                                  .setOnShowClassListener( new ShowClass.OnShowClassListener()
                                                                  {
                                                                      @Override
                                                                      public void onComplete()
                                                                      {
                                                                          showClass.endShowCase();
                                                                      }
                                                                  } )
                                             */
                                             .showCaseTargets();
                                    
                                    // Declare it shown!
                                    //                                    GameEngine.setResumeHelper( getContext(), true );
                                }
                            }
                        }
                    } );
                    
                    button.animate().start();
                }
                else
                {
                    button = view_main.findViewById( R.id.playResumeButton );
                    button.setOnClickListener( GameStartFragment.this );
                    button.setVisibility( View.GONE );
                }
                
                
                //#############################
                //
                // Show square buttons
                //
                //#############################
                for ( int square_button : square_buttons )
                {
                    final View imgButton;
                    
                    imgButton = view_main.findViewById( square_button );
                    imgButton.setOnClickListener( GameStartFragment.this );
                }
                
                //#############################
                //
                // Springy buttons
                //
                //#############################
                for ( int i = long_buttons.length - 1; i > -1; i-- )
                {
                    // DO NOT INCLUDE RESUME BUTTON IF SHOWCASING
                    if ( i == 0 && !resumeShown )
                    {
                        continue;
                    }
                    
                    View                 long_button = view_main.findViewById( long_buttons[ i ] );
                    ObjectAnimator       obj;
                    PropertyValuesHolder sx          = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.05f, .95f, 1 );
                    PropertyValuesHolder sy          = PropertyValuesHolder.ofFloat( "scaleY", 1, .95f, 1.05f, 1 );
                    
                    long_button.setOnClickListener( GameStartFragment.this );
                    obj = ObjectAnimator.ofPropertyValuesHolder( long_button, sx, sy );
                    obj.setDuration( 3000 ).setRepeatCount( ValueAnimator.INFINITE );
                    obj.setInterpolator( new LinearInterpolator() );
                    obj.start();
                    
                    // To kill upon exit
                    long_button.setTag( obj );
                }
                
                
                //#############################
                //
                // Spinning star over letter "N"
                //
                //#############################
                View                 star     = view_main.findViewById( R.id.titleSparkle );
                PropertyValuesHolder sx       = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.3f, 1 );
                PropertyValuesHolder sy       = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.3f, 1 );
                PropertyValuesHolder rot      = PropertyValuesHolder.ofFloat( "rotation", 0, 360 );
                ObjectAnimator       starAnim = ObjectAnimator.ofPropertyValuesHolder( star, sx, sy, rot );
                
                //
                starAnim.setDuration( 15000 ).setRepeatCount( ValueAnimator.INFINITE );
                starAnim.setInterpolator( new LinearInterpolator() );
                starAnim.start();
                star.setTag( starAnim );
                
                
                //#############################
                //
                // Spinning Sunshine
                //
                //#############################
/*
                View           sun      = view_main.findViewById( R.id.sunShine );
                ObjectAnimator sunShine = ObjectAnimator.ofFloat( sun, "rotation", 0, 360 );
                
                //
                sunShine.setDuration( 30000 ).setRepeatCount( ValueAnimator.INFINITE );
                sunShine.setInterpolator( new LinearInterpolator() );
                sunShine.start();
                sun.setTag( sunShine );
*/
                
                
                //#############################
                //
                // Exit App button
                //
                //#############################
                View exit = view_main.findViewById( R.id.exitButton );
                exit.setOnClickListener( GameStartFragment.this );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Process all button clicks for the menu system
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        final int id = v.getId();
        
        //#######################################
        //
        // Main menu buttons
        //
        //#######################################
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
        }
        
        
        //
        //        LevelSelector.pressedButtonClick( v ).start();
        
        if ( id == R.id.playResumeButton && getView() != null )
        {
            View view = getView();
            
            //@@@@@@@@@@@@@@@@@@@@@ Play game start sound
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.START_GAME );
            }
            
            if ( gameEngine.soundPlayer != null )
            {
                if ( GameEngine.musicIsPlaying )
                {
                    gameEngine.soundPlayer.fadeOutMusic();
                }
            }
            
            // Transition to LevelSelect
            // Or start a game if this is the first time playing!
            view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    int type = 0;
                    
                    // Skip level select if the game has NEVER been played!
                    GameEngine.resumeGame = true;
                    
                    // Add the level select, but do not show it
                    //                    FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                    
                    if ( getContext() != null )
                    {
                        type = GameState.getSaveGameType( getContext() );
                    }
                    
                    //
                    if ( type == 0 )
                    {
                        // Start a Connections game!
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new ConnectionsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new ConnectionsFragment(), FragmentLoader.REPLACE_FRAGMENT );
                    }
                    else
                    {
                        // Start a Card game!
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new CardsFragment(), FragmentLoader.REPLACE_FRAGMENT );
                    }
                    
                    // Memory Leak?
                    gameEngine = null;
                }
            } ).start();
            
            return;
        }
        else if ( id == R.id.playButton && getView() != null )
        {
            View view = getView();
            
            //@@@@@@@@@@@@@@@@@@@@@ Play game start sound
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.START_GAME );
                //                gameEngine.sndPlayer.soundPool.unload( sound );
            }
            
            //##############################
            //
            // Transition to LevelSelect
            // Or start a game if this is
            // the first time playing!
            //
            //##############################
            view.animate().alpha( 0f ).setDuration( AnimationValues.FADE_TIME ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    // Skip level select if the game has NEVER been played!
                    if ( GameEngine.currentLevel == 0 )
                    {
                        //####################################
                        // No need to continue if animations
                        // are disabled
                        //####################################
                        if ( getContext() != null && MainActivity.checkSystemAnimationsDuration( getContext() ) == 0 )
                        {
                            AlertDialog.Builder dialog = new AlertDialog.Builder( getContext() );
                            
                            dialog.setMessage(
                                    "This device currently has animations disabled. In order to use this app, animation support is required. Please enable device animations, and then restart the app. Thank you." );
                            dialog.setPositiveButton( "Exit", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick( DialogInterface dialog, int which )
                                {
                                    if ( getActivity() != null )
                                    {
                                        getActivity().finish();
                                    }
                                    System.exit( 1 );
                                }
                            } );
                            
                            //
                            dialog.create().show();
                            return;
                        }
                        
                        //
                        // Sound Engine
                        //
                        if ( gameEngine.soundPlayer != null )
                        {
                            if ( GameEngine.musicIsPlaying )
                            {
                                gameEngine.soundPlayer.fadeOutMusic();
                            }
                        }
                        
                        // Memory leak??
                        //                        onDestroy();
                        
                        // Add the level select, but do not show it
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        // Start a game!
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new ConnectionsFragment(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new ConnectionsFragment(), FragmentLoader.REPLACE_FRAGMENT );
                        
                        //
                        if ( getContext() != null )
                        {
                            GameEngine.currentBoardImage = GameBoard.getBoardImage( getContext(), 0, false );
                        }
                    }
                    else
                    {
/*
                        FragementLoader.SwapFragment( getActivity(), getContext(), new GameCompleted(),
                                                      FragementLoader.STACK_REPLACE_FRAGMENT
                                                    );
                        
*/
                        //                        FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.STACK_REPLACE_FRAGMENT );
                        FragmentLoader.SwapFragment( getActivity(), getContext(), new LevelSelector(), FragmentLoader.REPLACE_FRAGMENT );
                    }
                    
                    // Memory Leak?
                    gameEngine = null;
                }
            } ).start();
            
            return;
        }
        
        
        //##################################
        //
        // Load the game sounds to start
        // All others play default click
        //
        //##################################
        if ( id == R.id.exitButton && getActivity() != null )
        {
            // Save data to Shared pref file
            gameEngine.savePrefData();
            getActivity().finish();
            
            return;
        }
        else if ( id == R.id.settingsButton )
        {
            if ( getActivity() != null )
            {
                ImageView bgView = view_main.findViewById( R.id.blurStartScreen );
                bgView.setVisibility( View.VISIBLE );
                
                SettingsDialog dialog = new SettingsDialog();
                dialog.setCancelable( false );
                dialog.show( getActivity().getSupportFragmentManager(), "Settings" );
                dialog.setOnDismissListener( new SettingsDialog.OnDismissListener()
                {
                    @Override
                    public void onDismiss()
                    {
                        bgView.setVisibility( View.INVISIBLE );
                    }
                } );
            }
    
            return;
        }
        
        //################################
        //
        // Bottom row buttons
        //
        //################################
        if ( id == R.id.rateMe && getActivity() != null )
        {
            SharedPreferences        prefs;
            SharedPreferences.Editor editor;
            
            prefs = getActivity().getSharedPreferences( "cc_rate_sys", Context.MODE_PRIVATE );
            // Open the editor
            editor = prefs.edit();
            
            try
            {
                startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "http://play.google.com/store/apps/details?id=" + APP_PNAME ) ) );
            }
            catch ( android.content.ActivityNotFoundException nfe2 )
            {
                Toast.makeText( getContext(), "Could not load Market place", Toast.LENGTH_SHORT ).show();
            }
            //            }
            finally
            {   // At least they clicked the rate button
                editor.putBoolean( "hide_app_rate", true );
                editor.commit();
            }
        }
        else if ( id == R.id.emailButton )
        {
            String mailto = "mailto:genesyseast@gmail.com" + "?cc=" + "" + "&subject=" + Uri.encode( "Regarding your game: " + getString( R.string.app_name ) );
            
            Intent emailIntent = new Intent( Intent.ACTION_SENDTO );
            emailIntent.setData( Uri.parse( mailto ) );
            
            try
            {
                startActivity( emailIntent );
            }
            catch ( ActivityNotFoundException e )
            {
                Toast.makeText( getContext(), "No email app available", Toast.LENGTH_SHORT ).show();
            }
        }
        else if ( id == R.id.webpageButton )
        {
            Intent browserIntent = new Intent( Intent.ACTION_VIEW, Uri.parse( "https://sites.google.com/view/genesyseast-privacy-policy/home" ) );
            startActivity( browserIntent );
        }
        else if ( id == R.id.aboutApp && getActivity() != null )
        {
            AboutDialog dialog = new AboutDialog();
            ImageView   bgView = view_main.findViewById( R.id.blurStartScreen );
            
            bgView.setVisibility( View.VISIBLE );
            
            dialog.setCancelable( false );
            //            dialog.setBlurredBg( getBlurredBg( getActivity() ) );
            dialog.show( getActivity().getSupportFragmentManager(), "About" );
            dialog.setOnDismissListener( new AboutDialog.OnDismissListener()
            {
                @Override
                public void onDismiss()
                {
                    bgView.setVisibility( View.INVISIBLE );
                }
            } );
        }
        else if ( id == R.id.devApps )
        {
            try
            {
                ErrorDialog dialog = new ErrorDialog();
                
                dialog.setTitle( "More Apps" ).setMessage( "You will be redirected to view the developer's collection of Apps. Is this okay?" );
                dialog.setAlertIcon( R.drawable.game_pad );
                dialog.setYesText( "Yes" ).setNoText( "No Thanks" );
                dialog.setButtons( ErrorDialog.YES_BUTTON | ErrorDialog.NO_BUTTON );
                dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
                {
                    @Override
                    public void onErrorExitClick( int buttonClicked )
                    {
                        if ( buttonClicked == ErrorDialog.YES_BUTTON )
                        {
                            try
                            {
                                startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://play.google.com/store/apps/dev?id=8645290608649850542&hl=en_US" ) ) );
                            }
                            catch ( Exception ex )
                            {
                                Toast.makeText( getContext(), "Could not load Dev's collections", Toast.LENGTH_SHORT ).show();
                            }
                        }
                        
                        // Close the box!
                        dialog.dismiss();
                    }
                } );
                
                if ( getActivity() != null )
                {
                    dialog.show( getActivity().getSupportFragmentManager(), null );
                }
            }
            catch ( android.content.ActivityNotFoundException nfe2 )
            {
                Toast.makeText( getContext(), "Could not load Dev's collections", Toast.LENGTH_SHORT ).show();
            }
            //            }
        }
        else if ( id == R.id.socialMedia )
        {
            shareDialog = new ShareDialog( GameStartFragment.this );
            
            if ( ShareDialog.canShow( ShareLinkContent.class ) )
            {
                ShareLinkContent content;
                content = new ShareLinkContent.Builder().setContentTitle( "Look at what I'm playing!" ).setContentDescription( "I've tried Coin Connection, now it's your turn!" )
                                                        //                        .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                                                        .setContentUrl( Uri.parse( "https://play.google.com/store/apps/details?id=" + APP_PNAME ) ).build();
                
                shareDialog.show( content );
                
                ShareLinkContent linkContent = new ShareLinkContent.Builder().setContentUrl( Uri.parse( "https://play.google.com/store/apps/details?id=" + APP_PNAME ) ).build();
                shareDialog.show( linkContent );
            }
            else
            {
                Toast.makeText( getContext(), "Unable to Share...Try again.", Toast.LENGTH_SHORT ).show();
            }
        }
    }
    
    
    private Drawable getBlurredBg( Activity activity )
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled( true );
        view.buildDrawingCache();
        
        Bitmap         b1             = view.getDrawingCache();
        Rect           frame          = new Rect();
        Bitmap         b;
        Canvas         canvas;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        
        
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame( frame );
        
        
        if ( getActivity() != null && getActivity().getWindowManager() != null )
        {
            getActivity().getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
            int statusBarHeight = frame.top;
            int height          = displayMetrics.heightPixels;
            int width           = displayMetrics.widthPixels;
            
            b = Bitmap.createBitmap( b1, 0, statusBarHeight, width, height - statusBarHeight );
            canvas = new Canvas( b );
/*
            canvas.drawColor( 0x00000000, PorterDuff.Mode.CLEAR );
            canvas.drawColor( 0x7FFF0000 );
            
            
*/
            view.destroyDrawingCache();
            
            return new BitmapDrawable( getResources(), BlurEffect.createBlur( getContext(), b, 4 ) );
        }
        
        return null;
    }
    
    
    /**
     * App Rater Toast
     */
    private void rateMyApp()
    {
        if ( getContext() != null )
        {
            int DAYS_UNTIL_PROMPT = 3;
            // Min number of launches
            int LAUNCHES_UNTIL_PROMPT = 3;
            
            long              launch_count;
            long              date_firstLaunch;
            long              days_to_wait;
            SharedPreferences prefs = getContext().getSharedPreferences( "cc_rate_sys", Context.MODE_PRIVATE );
            // Open the editor
            SharedPreferences.Editor editor = prefs.edit();
            
            
            // Does this person wish to see the rate system anymore?
            if ( prefs.getBoolean( "hide_app_rate", false ) )
            {
                return;
            }
            
            // Save the Show / Hide info
            editor.putBoolean( "hide_app_rate", false );
            
            // Increment launch counter
            launch_count = prefs.getLong( "launch_count", 0 ) + 1;
            editor.putLong( "launch_count", launch_count );
            
            // Get date of first launch
            date_firstLaunch = prefs.getLong( "date_first_launch", 0 );
            if ( date_firstLaunch == 0 )
            {
                date_firstLaunch = System.currentTimeMillis();
                editor.putLong( "date_first_launch", date_firstLaunch );
            }
            
            // Get days to wait
            days_to_wait = prefs.getLong( "days_to_wait", DAYS_UNTIL_PROMPT );
            editor.putLong( "days_to_wait", DAYS_UNTIL_PROMPT );
            
            
            //########################################
            //
            // Wait at least n days before opening
            //
            //########################################
            if ( launch_count >= LAUNCHES_UNTIL_PROMPT )
            {
                // Does it meet the time threshold
                if ( System.currentTimeMillis() >= (date_firstLaunch + (days_to_wait * 24 * 60 * 60 * 1000)) )
                {
                    final ErrorDialog dialog = new ErrorDialog();
                    //                    int[]             resIds = gameEngine.arrayFromResource( R.array.star_burst );
                    
                    
                    //@@@@@@@@@@@@@@@@@@@@@ Error sound
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.ERROR );
                    }
                    
                    dialog.setAlertIcon( R.drawable.ic_help_black_24dp );
                    dialog.setTitle( "Rate This App" ).setMessage( getString( R.string.rate_me_plea ) );
                    dialog.setYesText( getString( R.string.rate_yes ) ).setCancelText( getString( R.string.rate_no ) );
                    dialog.setNoText( getString( R.string.rate_remind ) );
                    //
                    //                    dialog.animateFormImage( resIds );
                    dialog.setFormImageBG( R.drawable.box_a_blue ).setCancelable( false );
                    dialog.setButtons( ErrorDialog.MAIN_BUTTONS );
                    //
                    if ( getActivity() != null )
                    {
                        dialog.show( getActivity().getSupportFragmentManager(), "Error" );
                        dialog.setOnErrorListener( new ErrorDialog.OnErrorListener()
                        {
                            @Override
                            public void onErrorExitClick( int buttonClicked )
                            {
                                if ( buttonClicked == ErrorDialog.CANCEL_BUTTON )
                                {
                                    Toast.makeText( getContext(), "Thank you. Will not ask again...", Toast.LENGTH_SHORT ).show();
                                    //
                                    editor.putBoolean( "hide_app_rate", true );
                                    editor.commit();
                                    //
                                    dialog.dismiss();
                                }
                                else if ( buttonClicked == ErrorDialog.NO_BUTTON )
                                {
                                    // Remind the player later
                                    Toast.makeText( getContext(), "Thanks! I'll try.", Toast.LENGTH_SHORT ).show();
                                    editor.putLong( "days_to_wait", 1 );
                                    // Relaunch again after 3 relaunches
                                    editor.putLong( "launch_count", 0 );
                                    editor.commit();
                                    //
                                    dialog.dismiss();
                                }
                                else
                                {
                                    // Yes was clicked
                                    ImageView rateMe = view_main.findViewById( R.id.rateMe );
                                    onClick( rateMe );
                                    //
                                    dialog.dismiss();
                                }
                            }
                        } );
                    }
                }
            }
            
            //
            editor.commit();
        }
    }
    
    
    @Override
    public void onDestroyView()
    {
        View view = view_main.findViewById( R.id.titleSparkle );
        if ( view.getTag() != null )
        {
            ObjectAnimator obj = (( ObjectAnimator ) view.getTag());
            obj.end();
            view.setTag( null );
        }
        
        view = view_main.findViewById( R.id.sunShine );
        if ( view != null && view.getTag() != null )
        {
            ObjectAnimator obj = (( ObjectAnimator ) view.getTag());
            obj.end();
            view.setTag( null );
        }
        
        for ( int i = long_buttons.length - 1; i > -1; i-- )
        {
            View long_button = view_main.findViewById( long_buttons[ i ] );
            if ( long_button.getTag() != null )
            {
                ObjectAnimator obj = (( ObjectAnimator ) long_button.getTag());
                obj.end();
                long_button.setTag( null );
            }
        }
        
        ImageScroll scroller = view_main.findViewById( R.id.bgScroller );
        if ( scroller != null && scroller.scrollAnimator != null )
        {
            scroller.scrollAnimator.end();
            scroller.scrollAnimator = null;
        }
        
        scroller = null;
        view = null;
        view_main = null;
        gameEngine = null;
        shareDialog = null;
        
        super.onDestroyView();
    }
    
}