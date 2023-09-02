package com.genesyseast.coinconnection.Dialogs;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;

public class RewardDialog
        extends DialogFragment
        implements View.OnClickListener
{
    private GameEngine     gameEngine;
    private View           view_dialog = null;
    private ObjectAnimator obj;
    
    
    /**
     * //#################################
     * <p>
     * All other instantiations
     * <p>
     * //#################################
     */
    public RewardDialog()
    {
    }
    
    @Override
    public void dismiss()
    {
        super.dismiss();
    }
    
    
    /**
     * //#################################
     * <p>
     * Create the view
     * <p>
     * //#################################
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
        GradientTextView text;
        View             view;
        
        try
        {
            if ( getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_reward_layout, null );
                
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
                
                //
                // Coins received
                //
                text = view_dialog.findViewById( R.id.rewardMessage );
                text.setText( getString( R.string.reward_text ) );
                
                view = view_dialog.findViewById( R.id.rewardSparkle );
                PropertyValuesHolder sx  = PropertyValuesHolder.ofFloat( "scaleX",  0, 0, 1.2f, 0 );
                PropertyValuesHolder sy  = PropertyValuesHolder.ofFloat( "scaleY",  0, 0, 1.2f, 0 );
                PropertyValuesHolder rot = PropertyValuesHolder.ofFloat( "rotation",  0, 90 );
                
                obj = ObjectAnimator.ofPropertyValuesHolder( view, sx, sy, rot );
                obj.setDuration( 2500 ).setInterpolator( new LinearInterpolator() );
                obj.setRepeatCount( ValueAnimator.INFINITE );
                obj.start();
                
                view_dialog.setOnClickListener( this );
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
     * //#################################
     * <p>
     * Click listener for the button on
     * rom info form
     * <p>
     * //#################################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        if ( getDialog() != null )
        {
            getDialog().dismiss();
        }
    }
    
    
    @Override
    public void onDestroyView()
    {
        view_dialog = null;
        gameEngine = null;
        
        if ( obj != null )
        {
            obj.end();
        }
        
        obj = null;
        
        super.onDestroyView();
    }
}
