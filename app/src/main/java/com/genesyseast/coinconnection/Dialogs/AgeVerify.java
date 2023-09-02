package com.genesyseast.coinconnection.Dialogs;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

import java.util.Locale;

public class AgeVerify
        extends DialogFragment
        implements View.OnClickListener
{
    private GameEngine   gameEngine;
    private NumberPicker np;
    private int          age;
    private OnDismisser  onDismisser;
    private View         view_dialog = null;
    
    public interface OnDismisser
    {
        void onDismiss( int playerAge, boolean response );
    }
    
    
    public void setOnDismisser( OnDismisser onDismisser )
    {
        this.onDismisser = onDismisser;
    }
    
    
    /**
     * All other instantiations
     */
    public AgeVerify()
    {
    }
    
    
    @Override
    public void dismiss()
    {
        if ( onDismisser != null )
        {
            onDismisser.onDismiss( age, (age != 0) );
        }
        
        super.dismiss();
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        GradientTextView button;
        TextView         tv;
        String[]         values = new String[ 100 ];
        
        
        try
        {
            if ( getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_age_verify_layout, null );
                /*                view = View.inflate( getContext(), R.layout.popup_about_layout, null );*/
                
                // Privacy Policy
                tv = view_dialog.findViewById( R.id.privacyPolicy );
                Spanned sp = Html.fromHtml( getString( R.string.age_privacy_link_text ) );
                tv.setText( sp );
                //tv.setText( R.string.privacy_link_text );
                tv.setMovementMethod( LinkMovementMethod.getInstance() );
                
                // Privacy Policy
                np = view_dialog.findViewById( R.id.numPicker );
                np.setOnValueChangedListener( new NumberPicker.OnValueChangeListener()
                {
                    @Override
                    public void onValueChange( NumberPicker picker, int oldVal, int newVal )
                    {
                        age = newVal;
                        
                        GradientTextView button = view_dialog.findViewById( R.id.ageVerifyButton );
                        button.setEnabled( (age > 0) );
                        button.setClickable( (age > 0) );
                        button.setFocusable( (age > 0) );
                    }
                } );
                
                // Exit button clicker assignment
                button = view_dialog.findViewById( R.id.ageVerifyButton );
                button.setOnClickListener( this );
                button.setEnabled( (age > 0) );
                button.setClickable( (age > 0) );
                button.setFocusable( (age > 0) );
                //
                button = view_dialog.findViewById( R.id.ageVerifyDecline );
                button.setOnClickListener( this );
                
                //
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
                
                // Fill the string array
                for ( int i = 0; i < values.length; i++ )
                {
                    if ( i == 0 )
                    {
                        values[ i ] = "Select your age";
                    }
                    else
                    {
                        values[ i ] = String.format( Locale.getDefault(), "%02d", i );
                    }
                }
                
                //Populate NumberPicker values from minimum and maximum value range
                //Set the minimum value of NumberPicker
                np.setMinValue( 0 );
                //Specify the maximum value/number of NumberPicker
                np.setMaxValue( values.length - 1 );
                //
                np.setDisplayedValues( values );
                //Gets whether the selector wheel wraps when reaching the min/max value.
                np.setWrapSelectorWheel( true );
            }
            else
            {
                return null;
            }
            ;
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
     * Click listener for the button on
     * rom info form
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.ageVerifyDecline )
        {
            age = 0;
        }
        
        
        //@@@@@@@@@@@@@@@@@@ Close dialog box
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
        }
        
        dismiss();
    }
    
    
    @Override
    public void onDestroyView()
    {
        gameEngine = null;
        np = null;
        onDismisser = null;
        view_dialog = null;
        
        super.onDestroyView();
    }
}
