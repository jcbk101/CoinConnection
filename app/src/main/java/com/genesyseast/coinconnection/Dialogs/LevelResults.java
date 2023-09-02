package com.genesyseast.coinconnection.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.plattysoft.leonids.ParticleSystem;
import com.plattysoft.leonids.modifiers.ScaleModifier;

import java.security.SecureRandom;
import java.util.Locale;

public class LevelResults
        extends DialogFragment
{
    
    private int              stopper;
    private GameEngine       gameEngine;
    private GradientTextView tv;
    private View             view_dialog;
    private int              timeRemaining;
    private int              starCount       = 0;
    private int              levelScore;
    private int              connections;
    private int              boardMoves;
    private boolean          animateStar;
    private int[]            start           = { 0, 0, 0, 0 };
    private int[]            end             = { 0, 0, 0, 0 };
    private ValueAnimator    animator;
    private boolean          dismissAnimator = false;
    
    
    /**
     * //#####################################
     * <p>
     * Interface method declarations
     * <p>
     * //#####################################
     */
    public interface OnLevelUpListener
    {
        void onLevelUpResult( int code );
    }
    
    private OnLevelUpListener onLevelUpListener;
    
    
    /**
     * //#####################################
     * <p>
     * Interface: Input listener
     * <p>
     * //#####################################
     *
     * @param context N/A
     */
    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );
        
        try
        {
            onLevelUpListener = ( OnLevelUpListener ) getTargetFragment();
        }
        catch ( ClassCastException e )
        {
            e.getMessage();
        }
    }
    
    
    /**
     * //#####################################
     * <p>
     * All other instantiations
     * <p>
     * //#####################################
     */
    public LevelResults()
    {
    }
    
    
    @Override
    public void onCancel( @NonNull DialogInterface dialog )
    {
        super.onCancel( dialog );
        
        dismiss();
    }
    
    
    @Override
    public void dismiss()
    {
        super.dismiss();
        
        if ( onLevelUpListener != null )
        {
            onLevelUpListener.onLevelUpResult( getTargetRequestCode() );
        }
        
    }
    
    
    /**
     * //#####################################
     * <p>
     * View builder
     * <p>
     * //#####################################
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
        final String           levelDataString;
        final int              msgType;
        final ConstraintLayout parent;
        GradientTextView       view;
        
        
        try
        {
            if ( getArguments() != null && getDialog() != null && getDialog().getWindow() != null )
            {
                //                getDialog().getWindow().clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                view_dialog = View.inflate( getContext(), R.layout.popup_level_results_layout, null );
                gameEngine = GameEngine.getInstance( getContext() );
                
                // Get the message
                starCount = getArguments().getInt( "star_count", 0 );
                timeRemaining = getArguments().getInt( "time_left", 0 );
                levelScore = getArguments().getInt( "level_score", 0 );
                connections = getArguments().getInt( "connections", 0 );
                boardMoves = getArguments().getInt( "moves", 0 );
                
                //
                if ( connections == -1 )
                {
                    view = view_dialog.findViewById( R.id.connectionLabel );
                    view.setText( getString( R.string.matches_label ) );
                    connections = boardMoves;
                }
                
                
                levelDataString = getArguments().getString( "level_msg", null );
                animateStar = getArguments().getBoolean( "new_star", false );
                
                if ( starCount < 0 )
                {
                    starCount = 0;
                }
                
                setCancelable( false );
                view_dialog.setClickable( false );
                //                giveLevelDetails( levelDataString );
                
                
                // Get the message
                // Simple click
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.playBgSfx( PlaySound.SPECIAL_MATCHED );
                }
                
                //
                view_dialog.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        if ( view_dialog.getTag() == null )
                        {
                            gameEngine.animatorList.add( animation );
                            view_dialog.setTag( 1 );
                        }
                    }
                } );
                view_dialog.animate().scaleX( 1 ).scaleY( 1 ).setInterpolator( new AnticipateOvershootInterpolator() );
                view_dialog.animate().setDuration( 500 ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        giveLevelDetails( levelDataString );
                        view_dialog.setTag( null );
                        //
                        GradientTextView button = view_dialog.findViewById( R.id.detailsDone );
                        button.setEnabled( true );
                        button.setOnClickListener( new View.OnClickListener()
                        {
                            @Override
                            public void onClick( View v )
                            {
                                //@@@@@@@@@@@@@@@@@@@@@ Add points ticker
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.playBgSfx( PlaySound.DIALOG_CLICK );
                                }
                                
                                //
                                // User didn't want to wait for auto close
                                //
                                if ( animator != null )
                                {
                                    dismissAnimator = false;
                                    animator.cancel();
                                }
                                // Get the message
                                dismiss();
                            }
                        } );
                    }
                } ).start();
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
        }
        
        //
        return view_dialog;
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
        
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout( width, height );
        }
    }
    
    
    @Override
    public void onResume()
    {
/*
        if ( animator != null )
        {
            long dur = animator.getDuration();
            
            dismissAnimator = false;
            animator.cancel();
            
            dismissAnimator = true;
            animator = ValueAnimator.ofInt( 0, 100 );
            animator.setDuration( dur );
            animator.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    if ( dismissAnimator )
                    {
                        dismiss();
                    }
                    
                    super.onAnimationEnd( animation );
                }
            } );
            //
            animator.start();
        }
        else
        {
            dismissAnimator = false;
        }
        
*/
        super.onResume();
    }
    
    
    /**
     * //#####################################
     * <p>
     * Specifically for the Level Completed
     * <p>
     * //#####################################
     */
    private void giveLevelDetails( String levelDataString )
    {
        SecureRandom r = new SecureRandom();
        int[] colors = {
                R.array.white_on_white, R.array.bronze_reflection, R.array.silver_reflection, R.array.gold_reflection
        };
        int[] stars      = { R.id.levelLeftStar, R.id.levelRightStar, R.id.levelCenterStar };
        int   scoreDelay = 1000;
        int   DURATION   = 500;
        
        tv = view_dialog.findViewById( R.id.levelCompleteTitle );
        tv.setText( levelDataString );
        tv.setColorList( colors[ starCount % colors.length ] );
        
        //
        if ( starCount < 0 )
        {
            starCount = 0;
        }
        if ( starCount > 3 )
        {
            starCount = 3;
        }
        
        
        //##################################
        //
        //
        //
        //##################################
        if ( animateStar )
        {
            for ( int i = 0; i < starCount; i++ )
            {
                // Show the spinning star
                // Get the correct star
                final ImageView star = view_dialog.findViewById( stars[ i ] );
                
                // Zoom from out to in,  Fade in with Bounce
                // Execute the animation
                final float          x = star.getScaleX();
                final float          y = star.getScaleY();
                ObjectAnimator       obj;
                PropertyValuesHolder sx, sy;
                
                sx = PropertyValuesHolder.ofFloat( "scaleX", 0, 1.5f, x );
                sy = PropertyValuesHolder.ofFloat( "scaleY", 0, 1.5f, y );
                
                //
                star.setPivotX( star.getWidth() / 2f );
                star.setPivotY( star.getHeight() / 2f );
                // Hide dummy star
                //                dummyStar.setImageResource( 0 );
                
                //
                // Display each active star
                //
                obj = ObjectAnimator.ofPropertyValuesHolder( star, sx, sy );
                obj.setDuration( DURATION ).setStartDelay( i * (DURATION * 2) );
                obj.setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                obj.addListener( new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart( Animator animation )
                    {
                        star.setImageResource( R.drawable.star_2_on );
                        // Particle effect
                        if ( getActivity() != null )
                        {
                            ParticleSystem burst = new ParticleSystem( ( ViewGroup ) view_dialog, 20,
                                                                       ResourcesCompat.getDrawable( getResources(), R.drawable.star_particle_gold, null ), 2000
                            );
                            burst.setSpeedRange( 0.05f, 0.1f );
                            burst.setRotationSpeed( .5f );
//                            burst./*setFadeOut( 500 ).*/setScaleRange( 1, 1.5f );
                            burst.addModifier( new ScaleModifier( 0.25f, 0f, 0, 1000) );
//                            burst.emit( star, 20, 400 );
                            burst.oneShot( star, 20 );
                        }
                    }
                    
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        star.setScaleX( x );
                        star.setScaleY( y );
                        
                        //@@@@@@@@@@@@@@@@@@@@@ Star has landed
                        if ( gameEngine.soundPlayer != null )
                        {
                            gameEngine.soundPlayer.play( PlaySound.STAR_SHOOT );
                        }
                        
                        //
                        obj.removeAllListeners();
                    }
                    
                    @Override
                    public void onAnimationCancel( Animator animation )
                    {
                    
                    }
                    
                    @Override
                    public void onAnimationRepeat( Animator animation )
                    {
                    
                    }
                } );
                
                obj.start();
/*
                newStar = ObjectAnimator.ofFloat( stars, "alpha", 0, 1 );
                newStar.addListener( new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart( Animator animation )
                    {
                        final float x            = star.getScaleX();
                        final float y            = star.getScaleY();
                        final float starRotation = star.getRotation();
                        
                        //@@@@@@@@@@@@@@@@@@@@@ Landing star: 1 second
                        if ( gameEngine.soundPlayer != null )
                        {
                            gameEngine.soundPlayer.playBgSfx( PlaySound.STAR_LEVEL_UP );
                        }
                        
                        //
                        ObjectAnimator       zoom;
                        PropertyValuesHolder rot, sx, sy;
                        
                        sx = PropertyValuesHolder.ofFloat( "scaleX", 5, x );
                        sy = PropertyValuesHolder.ofFloat( "scaleY", 5, y );
                        rot = PropertyValuesHolder.ofFloat( "rotation", -360, starRotation );
                        zoom = ObjectAnimator.ofPropertyValuesHolder( star, rot, sx, sy );
                        
                        //                    star.setRotation( starRotation - 360 );
                        star.setPivotX( star.getWidth() / 2f );
                        star.setPivotY( star.getHeight() / 2f );
                        //
                        // Used to change the image when ready to fly
                        //
                        star.animate().alpha( 1 ).withStartAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                star.setImageResource( R.drawable.star_holder );
                            }
                        } ).start();
                        
                        
                        //
                        // Display each active star
                        //
                        zoom.setStartDelay( currentStar * 750 );
                        zoom.setDuration( 750 );
                        zoom.addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                super.onAnimationEnd( animation );
                                
                                star.setScaleX( x );
                                star.setScaleY( y );
                                star.setRotation( starRotation );
                                star.setImageResource( R.drawable.gold_star );
                                
                                //@@@@@@@@@@@@@@@@@@@@@ Star has landed
                                if ( gameEngine.soundPlayer != null )
                                {
                                    gameEngine.soundPlayer.play( PlaySound.STAR_SHOOT );
                                }
                                
                                // Animate the star landing
                                animateStarLanded( view_dialog, currentStar );
                                
                                // Particle effect
                                if ( getActivity() != null )
                                {
                                    ParticleSystem burst = new ParticleSystem( ( ViewGroup ) view_dialog, 10,
                                                                               ResourcesCompat.getDrawable( getResources(), R.drawable.particle_special, null ), 750
                                    );
                                    burst.setSpeedRange( 0.05f, 0.1f );
                                    burst.setRotationSpeed( .5f );
                                    burst.setFadeOut( 750 );
                                    burst.oneShot( star, 10 );
                                }
                            }
                        } );
                        //
                        zoom.start();
                    }
                    
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                    
                    }
                    
                    @Override
                    public void onAnimationCancel( Animator animation )
                    {
                    
                    }
                    
                    @Override
                    public void onAnimationRepeat( Animator animation )
                    {
                    
                    }
                } );
                
                //
                newStar.setDuration( 10 ).setInterpolator( new AccelerateInterpolator() );
                gameEngine.animatorList.add( newStar );
                newStar.start();
*/
            }
        }
        
        
        //#####################################
        //
        // Show the scores updating
        //
        //#####################################
        ValueAnimator[] nums     = new ValueAnimator[ 4 ];
        final TextView  score    = view_dialog.findViewById( R.id.levelScoreText );
        final int       duration = 1000;
        
        //
        scoreDelay += (starCount * DURATION);
        
        // Main score goes up
        //        start[ 0 ] = gameEngine.Scores[ gameEngine.currentLevel ];
        start[ 0 ] = 0;
        //        end[ 0 ] = start[ 0 ] + ((timeRemaining / 60000) * 60) + levelScore + (connections * 5);
        end[ 0 ] = start[ 0 ] + (timeRemaining / 1000) + levelScore + (connections * 5);
        
        
        // Finalize the score change
        //        gameEngine.Scores[ gameEngine.currentLevel ] = end[ 0 ];
        
        //
        //-----------------------
        //
        nums[ 0 ] = ValueAnimator.ofInt( start[ 0 ], end[ 0 ] );
        nums[ 0 ].setStartDelay( scoreDelay );
        nums[ 0 ].setDuration( duration ).setInterpolator( new LinearInterpolator() );
        nums[ 0 ].addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                int value = ( int ) animation.getAnimatedValue();
                score.setText( String.format( Locale.getDefault(), " %d ", value ) );
                
                //@@@@@@@@@@@@@@@@@@@@@ Add points ticker
                //                gameEngine.soundPlayer.play( PlaySound.POINTS_ADD );
            }
        } );
        nums[ 0 ].addListener( new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart( Animator animation )
            {
                //@@@@@@@@@@@@@@@@@@@@@ Add points ticker
/*
                if ( gameEngine.soundPlayer != null )
                {
                    stopper = gameEngine.soundPlayer.play( PlaySound.POINTS_ADD_LOOP, PlaySound.LOOP );
                }
*/
            }
            
            @Override
            public void onAnimationEnd( Animator animation )
            {
                score.setText( String.format( Locale.getDefault(), " %d", end[ 0 ] ) );
                
                GradientTextView button = view_dialog.findViewById( R.id.detailsDone );
                button.setVisibility( View.VISIBLE );
                button.setAlpha( 0f );
                button.animate().alpha( 1 ).setDuration( 500 ).start();
                
                //@@@@@@@@@@@@@@@@@@@@@ Add points ticker
/*
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.POINTS_ADD_LOOP, PlaySound.STOP, stopper );
                }
                // End sound with full echo
                if ( gameEngine.soundPlayer != null )
                {
                    gameEngine.soundPlayer.play( PlaySound.POINTS_ADD_LOOP );
                }
*/
                
                //###############################
                //
                // Auto close in 3 seconds
                //
                //###############################
                dismissAnimator = true;
                animator = ValueAnimator.ofInt( 0, 100 );
                animator.setDuration( 3000 );
                animator.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        if ( dismissAnimator )
                        {
                            dismiss();
                        }
                        
                        super.onAnimationEnd( animation );
                    }
                } );
                //
                animator.start();
                gameEngine.animatorList.add( animator );
            }
            
            @Override
            public void onAnimationCancel( Animator animation )
            {
            
            }
            
            @Override
            public void onAnimationRepeat( Animator animation )
            {
            
            }
        } );
        //
        gameEngine.animatorList.add( nums[ 0 ] );
        nums[ 0 ].start();
        
        
        //#############################
        //
        // All other numbers go down
        //
        //#############################
        //        start[ 1 ] = (timeRemaining / 60000) * 60;
        start[ 1 ] = (timeRemaining / 1000);
        start[ 2 ] = levelScore;
        start[ 3 ] = (connections * 5);
        int[] tv = { 0, R.id.timeBonusText, R.id.levelBonusText, R.id.connectionText };
        
        
        for ( int i = 1; i < nums.length; i++ )
        {
            final TextView tView = view_dialog.findViewById( tv[ i ] );
            
            nums[ i ] = ValueAnimator.ofInt( start[ i ], 0 );
            nums[ i ].setStartDelay( scoreDelay );
            nums[ i ].setDuration( duration ).setInterpolator( new LinearInterpolator() );
            nums[ i ].addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    int value = ( int ) animation.getAnimatedValue();
                    tView.setText( String.format( Locale.getDefault(), " %d ", value ) );
                }
            } );
            nums[ i ].addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    tView.setText( String.format( Locale.getDefault(), " %d", 0 ) );
                }
            } );
            
            //
            gameEngine.animatorList.add( nums[ i ] );
            nums[ i ].start();
        }
    }
    
    
    /**
     * //#####################################
     * <p>
     * Shake the view upon star landing
     * <p>
     * //#####################################
     *
     * @param view
     * @param showStar
     */
    private void animateStarLanded( View view, int showStar )
    {
        view.setPivotX( view.getWidth() / 2f );
        view.setPivotY( view.getHeight() / 2f );
        
        // Select the side to bounce
        if ( showStar == 1 )
        {
            view.setRotationY( -15 );
            view.animate().rotationY( 0 );
        }
        else if ( showStar == 2 )
        {
            view.setRotationY( 15 );
            view.animate().rotationY( 0 );
        }
        else
        {
            // Last star
            view.setScaleX( .90f );
            view.setScaleY( .90f );
            view.animate().scaleY( 1f ).scaleX( 1f );
        }
        
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
        view.animate().setDuration( 1000 ).setInterpolator( new CustomBounceInterpolator( .2f, 20 ) );
        view.animate().withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                view.setTag( null );
            }
        } ).start();
    }
    
    
    @Override
    public void onDestroy()
    {
        gameEngine = null;
        tv = null;
        view_dialog = null;
        
        super.onDestroy();
    }
}
