package com.genesyseast.coinconnection.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.R;

public class ShuffleBoard
        extends Dialog
{
    
    private GradientTextView tv;
    private View             view_dialog;
    private int              bgImage;
    
    public ShuffleBoard( @NonNull Context context, int bgImage )
    {
        super( context );
        this.bgImage = bgImage;
    }
    
    public ShuffleBoard( @NonNull Context context, int themeResId, int bgImage )
    {
        super( context, themeResId );
        this.bgImage = bgImage;
    }
    
    
    @Override
    public void dismiss()
    {
        super.dismiss();
    }
    
    
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        try
        {
            view_dialog = View.inflate( getContext(), R.layout.popup_shuffle_board_layout, null );
            final ConstraintLayout frame = view_dialog.findViewById( R.id.levelCompleteFrame );
            final GradientTextView text  = view_dialog.findViewById( R.id.levelCompleteText );
            
            if ( view_dialog != null && getWindow() != null )
            {
                //                getDialog().getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
                getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                getWindow().setDimAmount( .8f );
                
                // Set the BG Image to use
                ImageView image = frame.findViewById( R.id.bgImage );
                image.setImageResource( bgImage );
                
                
                //################################
                //
                // Touch and leave now
                //
                //################################
                setCancelable( false );
                view_dialog.setClickable( true );
                view_dialog.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick( View v )
                    {
                        final GradientTextView text  = view_dialog.findViewById( R.id.levelCompleteText );
                        final ConstraintLayout frame = view_dialog.findViewById( R.id.levelCompleteFrame );
                        
                        view_dialog.clearAnimation();
                        
                        //
                        view_dialog.animate().setStartDelay( 300 );
                        view_dialog.animate().translationY( -frame.getBottom() ).setDuration( 500 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                        view_dialog.animate().withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                dismiss();
                            }
                        } );
                        view_dialog.animate().start();
                        
                        // Text slide
                        text.animate().setStartDelay( 0 );
                        text.animate().translationX( frame.getWidth() * 2 ).setDuration( 300 ).setInterpolator( new AccelerateDecelerateInterpolator() ).start();
                    }
                } );
                
                //###############################
                //
                // Or auto close in 3 seconds
                //
                //###############################
                view_dialog.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
                {
                    @Override
                    public void onGlobalLayout()
                    {
                        view_dialog.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                        
                        //
                        view_dialog.animate().setStartDelay( 0 );
                        view_dialog.setTranslationY( -frame.getBottom() );
                        view_dialog.animate().translationY( 0 ).setDuration( 500 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                        view_dialog.animate().withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                view_dialog.animate().setDuration( 2000 ).withEndAction( new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        final GradientTextView text  = view_dialog.findViewById( R.id.levelCompleteText );
                                        final ConstraintLayout frame = view_dialog.findViewById( R.id.levelCompleteFrame );
                                        
                                        view_dialog.clearAnimation();
                                        
                                        //
                                        view_dialog.animate().setStartDelay( 300 );
                                        view_dialog.animate().translationY( -frame.getBottom() ).setDuration( 500 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                                        view_dialog.animate().withEndAction( new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                dismiss();
                                            }
                                        } );
                                        view_dialog.animate().start();
                                        
                                        // Text slide
                                        text.animate().setStartDelay( 0 );
                                        text.animate().translationX( frame.getWidth() * 2 ).setDuration( 300 ).setInterpolator( new AccelerateDecelerateInterpolator() ).start();
                                        
                                        //                                        dismiss();
                                    }
                                } ).start();
                            }
                        } );
                        view_dialog.animate().start();
                        
                        // Text slide
                        text.setTranslationX( -text.getRight() );
                        text.animate().setStartDelay( 500 );
                        text.animate().translationX( 0 ).setDuration( 300 ).setInterpolator( new AccelerateDecelerateInterpolator() ).start();
                    }
                } );
                
                //
                setContentView( view_dialog );
            }
            else
            {
                setContentView( new View( getContext() ) );
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
        }
    }
    
    
    /**
     * //#####################################
     * <p>
     * Used to set the screen into fullscreen
     * mode
     * <p>
     * //#####################################
     */
    @Override
    public void onStart()
    {
        super.onStart();
        
        Dialog dialog = this;
        
        if ( dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout( width, height );
        }
    }
    
    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        
        tv = null;
        view_dialog = null;
    }
}
