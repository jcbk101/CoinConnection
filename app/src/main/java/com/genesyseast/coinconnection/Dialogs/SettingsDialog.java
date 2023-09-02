package com.genesyseast.coinconnection.Dialogs;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.CustomControls.CustomSeekBar;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.Fragments.LevelSelector;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

import static android.content.Context.AUDIO_SERVICE;

public class SettingsDialog
        extends DialogFragment
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, DialogInterface.OnKeyListener
{
    private GameEngine        gameEngine;
    private View              view_dialog = null;
    private int[]             checkBoxes  = { R.id.sfxCheckbox, R.id.musicCheckbox, R.id.hintCheckbox };
    private int[]             states      = { GameEngine.SFX_OFF, GameEngine.MUSIC_OFF, GameEngine.HINT_OFF };
    private OnDismissListener onDismissListener;
    
    
    /**
     * //############################
     * <p>
     * All other instantiations
     * <p>
     * //############################
     */
    public SettingsDialog()
    {
    
    }
    
    
    public interface OnDismissListener
    {
        void onDismiss();
    }
    
    public void setOnDismissListener( OnDismissListener onDismissListener )
    {
        this.onDismissListener = onDismissListener;
    }
    
    
    @Override
    public void dismiss()
    {
        super.dismiss();
        
        if ( onDismissListener != null )
        {
            onDismissListener.onDismiss();
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Build the view
     * <p>
     * * //############################
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
        GradientTextView button;
        TextView         tv;
        
        try
        {
            if ( getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_settings_layout, null );
                
                // Exit button clicker assignment
                button = view_dialog.findViewById( R.id.settingsDone );
                button.setOnClickListener( this );
                
                SeekBar seekBar = view_dialog.findViewById( R.id.volumeSeekBar );
                seekBar.setOnSeekBarChangeListener( this );
                seekBar.setEnabled( (GameEngine.systemFlags & (GameEngine.SFX_OFF | GameEngine.MUSIC_OFF)) != 3 );
                
                // Status of the seekbar
                ImageView musicNote = view_dialog.findViewById( R.id.musicNoteImage );
                musicNote.setEnabled( (GameEngine.systemFlags & (GameEngine.SFX_OFF | GameEngine.MUSIC_OFF)) != 3 );
                
                
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
                
                getDialog().setOnKeyListener( this );
                //
                handleSettingsWindow();
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.printStackTrace();
            dismiss();
            return null;
        }
        
        //
        return view_dialog;
    }
    
    
    /**
     * //############################
     * <p>
     * Click listener for the button on
     * rom info form
     * <p>
     * //############################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        if ( getDialog() != null )
        {
            //@@@@@@@@@@@@@@@@@@ Close dialog box
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
            }
            
            dismiss();
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Options menu
     * <p>
     * //###########################
     */
    public void handleSettingsWindow()
    {
        // Give the back button some thing to do
        ImageView    musicNote = view_dialog.findViewById( R.id.musicNoteImage );
        SeekBar      musicBar  = view_dialog.findViewById( R.id.volumeSeekBar );
        SwitchCompat checkbox;
        
        //
        // Music setting
        //
        musicBar.setOnSeekBarChangeListener( this );
        musicBar.setProgress( GameEngine.musicVolume );
        musicBar.setMax( GameEngine.maxVolume );
        
        //
        //############################
        //
        for ( int i = 0; i < checkBoxes.length; i++ )
        {
            int curState = states[ i ];
            
            checkbox = view_dialog.findViewById( checkBoxes[ i ] );
            checkbox.setChecked( (GameEngine.systemFlags & curState) == curState );
            checkbox.setTag( i );
            
            
            // Set up a clicker
            checkbox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                {
                    int index = ( int ) buttonView.getTag();
                    
                    // Animate if the box is being checked!
                    if ( isChecked )
                    {
                        LevelSelector.bounceButtonClick( getActivity(), buttonView, false ).start();
                    }
                    
                    
                    GameEngine.systemFlags &= ~states[ index ];
                    GameEngine.systemFlags |= (isChecked ? states[ index ] : 0);
                    
                    // Status of the seekbar
                    musicNote.setEnabled( (GameEngine.systemFlags & (GameEngine.SFX_OFF | GameEngine.MUSIC_OFF)) != 3 );
                    if ( !musicNote.isEnabled() )
                    {
                        LevelSelector.bounceButtonClick( getActivity(), musicNote, false ).start();
                    }
                    
                    musicBar.setEnabled( (GameEngine.systemFlags & (GameEngine.SFX_OFF | GameEngine.MUSIC_OFF)) != 3 );
                    
                    //
                    if ( index == 1 )
                    {
                        // stop the music
                        if ( gameEngine.soundPlayer != null )
                        {
                            if ( !isChecked && gameEngine.currentBGMusic > -1 )
                            {
                                gameEngine.soundPlayer.playBgMusic( gameEngine.currentBGMusic >> 2, PlaySound.LOOP );
                            }
                            else
                            {
                                gameEngine.soundPlayer.playBgMusic( gameEngine.currentBGMusic >> 2, PlaySound.STOP );
                            }
                        }
                    }
                }
            } );
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Handle seek bar changes
     * <p>
     * //###########################
     *
     * @param seekBar  N/A
     * @param progress N/A
     * @param fromUser N/A
     */
    @Override
    public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
    {
        if ( view_dialog != null && getContext() != null )
        {
            AudioManager am = ( AudioManager ) getContext().getSystemService( AUDIO_SERVICE );
            GameEngine.musicVolume = seekBar.getProgress();
            
            if ( am != null )
            {
                am.setStreamVolume( AudioManager.STREAM_MUSIC, GameEngine.musicVolume, 0 );
            }
        }
    }
    
    @Override
    public void onStartTrackingTouch( SeekBar seekBar )
    {
    }
    
    @Override
    public void onStopTrackingTouch( SeekBar seekBar )
    {
        if ( view_dialog != null )
        {
            GameEngine.musicVolume = seekBar.getProgress();
            
            
            if ( getContext() != null )
            {
                AudioManager am = ( AudioManager ) getContext().getSystemService( AUDIO_SERVICE );
                
                if ( am != null )
                {
                    am.setStreamVolume( AudioManager.STREAM_MUSIC, GameEngine.musicVolume, 0 );
                }
            }
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Volume controls, etc
     * <p>
     * //############################
     *
     * @param dialogInterface
     * @param keyCode
     * @param event
     *
     * @return
     */
    @Override
    public boolean onKey( DialogInterface dialogInterface, int keyCode, KeyEvent event )
    {
        if ( getContext() != null && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || (keyCode == KeyEvent.KEYCODE_VOLUME_UP) )
        {
            try
            {
                AudioManager am = ( AudioManager ) getContext().getSystemService( AUDIO_SERVICE );
                SeekBar      seeker;
                
                if ( am != null )
                {
                    int volume_level = am.getStreamVolume( AudioManager.STREAM_MUSIC );
                    
                    GameEngine.musicVolume = volume_level;
                    GameEngine.sfxVolume = volume_level;
                    
                    if ( view_dialog != null )
                    {
                        seeker = view_dialog.findViewById( R.id.volumeSeekBar );
                        seeker.setProgress( GameEngine.musicVolume );
                        am.setStreamVolume( AudioManager.STREAM_MUSIC, GameEngine.musicVolume, 0 );
                    }
                }
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        
        return false;
    }
    
    
    /**
     * //############################
     * <p>
     * Help to prevent memory leaks
     * <p>
     * //############################
     */
    @Override
    public void onDestroyView()
    {
        view_dialog = null;
        gameEngine = null;
        
        super.onDestroyView();
    }
}
