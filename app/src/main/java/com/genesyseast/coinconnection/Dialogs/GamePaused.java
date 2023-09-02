package com.genesyseast.coinconnection.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

public class GamePaused
        extends DialogFragment
        implements View.OnClickListener
{
    private             GameEngine gameEngine;
    private             View       view_dialog  = null;
    private             int[]      switchBoxes  = { R.id.sfxButton, R.id.musicButton };
    private             int[]      mainButtons  = { R.id.pauseMenuButton, R.id.pauseQuitButton, R.id.pauseResumeButton };
    private             int[]      switchStates = { GameEngine.SFX_OFF, GameEngine.MUSIC_OFF };
    public static final int        RESUME_GAME  = 0;
    public static final int        MENU_REQUEST = 1;
    public static final int        QUIT_REQUEST = 2;
    
    
    /**
     * //################################
     * <p>
     * Interface method declarations
     * <p>
     * //################################
     */
    public interface OnResult
    {
        void onPausedGameResult( int code );
    }
    
    private OnResult onResult;
    
    
    /**
     * //################################
     * <p>
     * Interface: Input listener
     * <p>
     * //################################
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            onResult = ( OnResult ) getTargetFragment();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    
    /**
     * //################################
     * <p>
     * All other instantiations
     * <p>
     * //################################
     */
    public GamePaused()
    {
    }
    
    @Override
    public void dismiss()
    {
        super.dismiss();
    }
    
    
    /**
     * //################################
     * <p>
     * Create the needed view
     * then return
     * <p>
     * //################################
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        View         button;
        SwitchCompat cb;
        
        try
        {
            view_dialog = View.inflate( getContext(), R.layout.popup_game_paused_layout, null );
            
            //
            gameEngine = GameEngine.getInstance( getContext() );
            
            //
            if ( getDialog() != null && getDialog().getWindow() != null )
            {
                getDialog().getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                getDialog().getWindow().setWindowAnimations( R.style.DialogAnimations );
                
                //
                // Main buttons
                //
                for ( int mainButton : mainButtons )
                {
                    button = view_dialog.findViewById( mainButton );
                    button.setOnClickListener( this );
                }
                
                
                //
                // Sound buttons
                //
                for ( int i = 0; i < switchBoxes.length; i++ )
                {
                    cb = view_dialog.findViewById( switchBoxes[ i ] );
                    cb.setOnClickListener( this );
                    cb.setTag( i );
                    cb.setChecked( (GameEngine.systemFlags & switchStates[ i ]) == switchStates[ i ] );
                    //
                    // Set up a clicker
                    cb.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                        {
                            int state = switchStates[ ( int ) buttonView.getTag() ];
                            
                            GameEngine.systemFlags &= ~state;
                            GameEngine.systemFlags |= (isChecked ? state : 0);
                            
                            // stop the music
                            if ( gameEngine.soundPlayer != null && buttonView.getId() == R.id.musicButton )
                            {
                                if ( !isChecked && gameEngine.currentBGMusic > -1 )
                                {
                                    gameEngine.soundPlayer.playBgMusic( gameEngine.currentBGMusic >> 2, PlaySound.LOOP );
                                }
                                else
                                {
                                    gameEngine.soundPlayer.playBgMusic( -1, PlaySound.STOP );
                                }
                            }
                        }
                    } );
                }
                
                
                // Capture the back key and process the result
                getDialog().setOnKeyListener( new DialogInterface.OnKeyListener()
                {
                    @Override
                    public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event )
                    {
                        if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                        {
                            if ( getView() != null )
                            {
                                View button = getView().findViewById( R.id.pauseResumeButton );
                                button.setPressed( true );
                                button.callOnClick();
                                return true;
                            }
                        }
                        
                        return false;
                    }
                } );
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
            return null;
        }
        //
        return view_dialog;
    }
    
    
    /**
     * //################################
     * <p>
     * Used to set the screen into
     * fullscreen mode
     * <p>
     * //################################
     */
    @Override
    public void onStart()
    {
        super.onStart();
        
/*
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout( width, height );
        }
*/
    }
    
    
    /**
     * //################################
     * <p>
     * Click listener for the button on
     * rom info form
     * <p>
     * //################################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int       id = v.getId();
        final int result;
        
        //
        // Set the images
        //
        if ( getView() != null )
        {
            if ( id == R.id.sfxButton || id == R.id.musicButton )
            {
                //@@@@@@@@@@@@@@@@ Button Click
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
                }
                
                return;
            }
            
            
            //####################################
            //
            //
            //####################################
            switch ( id )
            {
                case R.id.pauseMenuButton: result = MENU_REQUEST;
                    //@@@@@@@@@@@@@@@@ Button Click
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.UNPAUSE_GAME );
                    }
                    break;
                
                case R.id.pauseQuitButton: result = QUIT_REQUEST;
                    //@@@@@@@@@@@@@@@@ Button Click
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.QUIT_GAME );
                    }
                    break;
                
                case R.id.pauseResumeButton:
                default: result = RESUME_GAME;
                    //@@@@@@@@@@@@@@@@ Button Click
                    if ( gameEngine.soundPlayer != null )
                    {
                        gameEngine.soundPlayer.playBgSfx( PlaySound.UNPAUSE_GAME );
                    }
                    break;
            }
            
            //
            dismiss();
            onResult.onPausedGameResult( result );
        }
    }
    
    
    @Override
    public void onDestroyView()
    {
        gameEngine = null;
        view_dialog = null;
        
        super.onDestroyView();
    }
}
