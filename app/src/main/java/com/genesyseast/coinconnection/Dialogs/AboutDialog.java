package com.genesyseast.coinconnection.Dialogs;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.CustomControls.ImageEffect;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

public class AboutDialog
        extends DialogFragment
        implements View.OnClickListener
{
    private GameEngine gameEngine;
    View view_dialog = null;
    private Drawable          blurredBg;
    private OnDismissListener onDismissListener;
    
    
    /**
     * All other instantiations
     */
    public AboutDialog()
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
    public void onStart()
    {
        super.onStart();
        
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        int width  = ViewGroup.LayoutParams.MATCH_PARENT;
        
        if ( getDialog() != null && getDialog().getWindow() != null )
        {
            getDialog().getWindow().setLayout( width, height );
        }
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
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        GradientTextView button;
        TextView         tv;
        
        try
        {
            if ( getActivity() != null && getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_about_layout, null );
                
                // Privacy Policy
                tv = view_dialog.findViewById( R.id.privacyPolicy );
                //            tv.setText( R.string.privacy_link_text );
                tv.setMovementMethod( LinkMovementMethod.getInstance() );
                
                // Exit button clicker assignment
                button = view_dialog.findViewById( R.id.aboutButtonDone );
                button.setOnClickListener( this );
                
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
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
     * Click listener for the button on
     * rom info form
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
    
    
    @Override
    public void onDestroyView()
    {
        view_dialog = null;
        gameEngine = null;
        onDismissListener = null;
        
        super.onDestroyView();
    }
}
