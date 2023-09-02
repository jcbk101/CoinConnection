package com.genesyseast.coinconnection.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ObjectivesDialog
        extends AlertDialog
{
    private GameEngine gameEngine;
    private View       view_dialog;
    private String     targetMsg;
    private boolean    isFailed    = false;
    private boolean    isPaused    = false;
    private Activity   activity;
    private int        animCounter = 0;
    private int        borders     = 0;
    private int[]      goalLabels  = { R.id.objectiveMessage, R.id.goaLabel1, R.id.goaLabel2, R.id.goaLabel3, R.id.goaLabel4, R.id.goaLabel5, R.id.goaLabel6 };
    private int[]      targetIds   = {
            R.id.redTargetObj,
            R.id.greenTargetObj,
            R.id.blueTargetObj,
            R.id.orangeTargetObj,
            R.id.pinkTargetObj,
            R.id.purpleTargetObj,
            R.id.yellowTargetObj,
            R.id.purpleGemTarget,
            R.id.pinkGemTarget,
            R.id.tealGemTarget,
            R.id.yellowGemTarget
    };
    
    private int[] borderImgIds = {
            R.drawable.container_goals_blue, R.drawable.container_goals_red, R.drawable.container_goals_gold
    };
    
    private ArrayList<TextView> targetViews;
    
    
    /**
     * //##############################
     * <p>
     * Display Pause parts
     * <p>
     * //##############################
     *
     * @param paused
     */
    public void setPaused( boolean paused )
    {
        isPaused = paused;
    }
    
    
    /**
     * //##############################
     * <p>
     * All other instantiations
     * <p>
     * //##############################
     */
    public ObjectivesDialog( Context context, String targetMsg, ArrayList<TextView> targetViews, int borders )
    {
        super( context );
        
        this.targetMsg = targetMsg;
        this.targetViews = targetViews;
        this.borders = borders;
    }
    
    
    @Override
    public void cancel()
    {
        dismiss();
        
        super.cancel();
    }
    
    /**
     * //############################
     * <p>
     * Dismiss
     * <p>
     * //############################
     */
    @Override
    public void dismiss()
    {
        super.dismiss();
    }
    
    
    /**
     * //############################
     * <p>
     * Create the view
     * <p>
     * //############################
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        setContentView( R.layout.popup_objectives_layout );
        
        try
        {
            if ( getWindow() != null & targetViews != null )
            {
                //
                // Set the dialog in full screen mode
                //
                /*                getWindow().requestFeature( Window.FEATURE_NO_TITLE );*/
                getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                /*                getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;*/
                //
                getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                // Need this instance
                gameEngine = GameEngine.getInstance( getContext() );
                
                //###############################
                //
                // Choose the correct borders
                //
                //###############################
                View v = findViewById( R.id.objFrame );
                v.setBackgroundResource( borderImgIds[ borders ] );
                
                
                //###############################
                //
                // Hide the coins
                //
                //###############################
                for ( int i = 0; i < targetIds.length; i++ )
                {
                    View target = findViewById( targetIds[ i ] );
                    v = targetViews.get( i );
                    PointXYZ temp = ( PointXYZ ) v.getTag();
                    
                    // Get the counts
                    //                    if ( v.getVisibility() == VISIBLE )
                    if ( temp != null && temp.x > 0 )
                    {
                        target.setVisibility( INVISIBLE );
                        //                        v.setVisibility( INVISIBLE );
                    }
                    else
                    {
                        target.setVisibility( View.GONE );
                        //                      v.setVisibility( View.GONE );
                    }
                }
                
                // Capture the back key and process the result
                setOnKeyListener( new DialogInterface.OnKeyListener()
                {
                    @Override
                    public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event )
                    {
                        if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                        {
                            transferTargets();
                            return true;
                        }
                        
                        return false;
                    }
                } );
                
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            isFailed = true;
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Find a number in the string
     * <p>
     * //############################
     */
    public static String extractNumber( final String str )
    {
        
        if ( str == null || str.isEmpty() )
        {
            return "";
        }
        
        StringBuilder sb    = new StringBuilder();
        boolean       found = false;
        for ( char c : str.toCharArray() )
        {
            if ( Character.isDigit( c ) )
            {
                sb.append( c );
                found = true;
            }
            else if ( found )
            {
                // If we already found a digit before and this char is not a digit, stop looping
                break;
            }
        }
        
        return sb.toString();
    }
    
    
    /**
     * //############################
     * <p>
     * Full screen mode
     * <p>
     * //############################
     */
    @Override
    protected void onStart()
    {
        Dialog dialog = this;
        int    width  = ViewGroup.LayoutParams.MATCH_PARENT;
        int    height = ViewGroup.LayoutParams.MATCH_PARENT;
        
        
        if ( dialog.getWindow() != null )
        {
            dialog.getWindow().setLayout( width, height );
            
            //
            view_dialog = dialog.findViewById( R.id.objectiveParent );
            
            view_dialog.setAlpha( 0f );
            
            view_dialog.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    if ( view_dialog.getTag() == null )
                    {
                        view_dialog.setTag( animation );
                    }
                }
            } );
            view_dialog.animate().alpha( 1f );
            view_dialog.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    ValueAnimator anim = ( ValueAnimator ) view_dialog.getTag();
                    gameEngine.animatorList.remove( anim );
                    view_dialog.setTag( null );
                    
                    //                    GradientTextView target;
                    GradientTextView message;
                    
                    
                    //###############################
                    //
                    // Split messages and post them
                    //
                    //###############################
                    String[] splitMsg = targetMsg.split( ";" );
                    
                    if ( splitMsg != null )
                    {
                        for ( int i = 0; i < splitMsg.length; i++ )
                        {
                            GradientTextView tv = findViewById( goalLabels[ i ] );
                            
                            if ( i == 0 )
                            {
                                tv.setText( splitMsg[ i ] );
                                tv.setVisibility( INVISIBLE );
                            }
                            else
                            {
                                tv.setVisibility( VISIBLE );
                                
                                // Do we need to add an "s"?
                                String num = extractNumber( splitMsg[ i ] );
                                
                                if ( num != null && num != "" && Integer.parseInt( num ) > 1 )
                                {
                                    tv.setText( String.format( Locale.getDefault(), "%ss", splitMsg[ i ] ) );
                                }
                                else
                                {
                                    tv.setText( splitMsg[ i ] );
                                }
                                
                                //
                                tv.setAlpha( 0 );
                                tv.animate().alpha( 1f ).setDuration( 750 ).start();
                            }
                        }
                    }
                    
                    //
                    //#############################
                    //
                    message = view_dialog.findViewById( R.id.objectiveMessage );
                    message.setVisibility( VISIBLE );
                    message.setTranslationX( -message.getWidth() );
                    
                    //
                    message.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            if ( message.getTag() == null )
                            {
                                message.setTag( animation );
                            }
                        }
                    } );
                    
                    message.animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ValueAnimator anim = ( ValueAnimator ) message.getTag();
                            gameEngine.animatorList.remove( anim );
                            message.setTag( null );
                            
                            View objParent = findViewById( R.id.objectiveParent );
                            objParent.setOnClickListener( new View.OnClickListener()
                            {
                                @Override
                                public void onClick( View v )
                                {
                                    transferTargets();
                                    //                                    dismiss();
                                }
                            } );
                            
                            dialog.setCancelable( true );
                        }
                    } );
                    message.animate().translationX( 0 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                    message.animate().setDuration( 750 ).start();
                    
                    
                    //#############################
                    //
                    // Animate targets
                    //
                    //#############################
                    for ( int i = 0; i < targetViews.size(); i++ )
                    {
                        PointXYZ p;
                        TextView v = targetViews.get( i );
                        
                        if ( i >= targetIds.length )
                        {
                            break;
                        }
                        
                        final GradientTextView target = view_dialog.findViewById( targetIds[ i ] );
                        
                        // Get the counts
                        if ( target.getVisibility() == INVISIBLE )
                        {
                            float transX = target.getX() + target.getWidth();
                            
                            target.setVisibility( VISIBLE );
                            
                            p = ( PointXYZ ) v.getTag();
                            target.setText( String.format( Locale.getDefault(), "%d", p.x ) );
                            
                            target.setTranslationX( -transX );
                            
                            // Set All off screen. then roll them in one at a time
                            target.setRotation( -transX );
                            target.animate().translationX( 0 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                            target.animate().rotation( 0 );
                            target.animate().setDuration( 750 ).setStartDelay( ((targetViews.size() - 1) - i) * 25 );
                            
                            // Pausible animation
                            target.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                            {
                                @Override
                                public void onAnimationUpdate( ValueAnimator animation )
                                {
                                    if ( target.getTag() == null )
                                    {
                                        target.setTag( animation );
                                        gameEngine.animatorList.add( animation );
                                    }
                                }
                            } );
                            
                            target.animate().withEndAction( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    ValueAnimator anim = ( ValueAnimator ) target.getTag();
                                    gameEngine.animatorList.remove( anim );
                                    target.setTag( null );
                                }
                            } );
                            target.animate().start();
                        }
                    }
                }
            } ).start();
        }
        
        super.onStart();
    }
    
    
    /**
     * ########################################
     * <p>
     * Check to see if any or all of the
     * targets have been reached. If so, update
     * the player on current status
     * <p>
     * ########################################
     */
    public void transferTargets()
    {
        ConstraintLayout parent = findViewById( R.id.objectiveParent );
        int[]            tars   = new int[ 2 ];
        int[]            objs   = new int[ 2 ];
        
        animCounter = 0;
        
        //#############################
        //
        //
        //
        //#############################
        if ( parent != null )
        {
            boolean needToDismiss = true;
            
            //
            for ( int i = 0; i < targetViews.size(); i++ )
            {
                View target = view_dialog.findViewById( targetIds[ i ] );
                View v      = targetViews.get( i );
                
                
                if ( target.getVisibility() == VISIBLE )
                {
                    needToDismiss = false;
                    
                    // Set the final location where the tile is
                    target.getLocationOnScreen( tars );
                    // Get the objective's info
                    v.getLocationOnScreen( objs );
                    
                    //
                    TextView mover = new TextView( getContext() );
                    mover.setId( i + 1001 );
                    mover.setLayoutParams( new ConstraintLayout.LayoutParams( v.getWidth(), v.getHeight() ) );
                    mover.setVisibility( VISIBLE );
                    mover.setBackground( target.getBackground() );
                    mover.setPivotY( v.getHeight() / 2f );
                    mover.setPivotX( v.getWidth() / 2f );
                    parent.addView( mover );
                    
                    
                    // Animate it back onto the board
                    ObjectAnimator       obj;
                    PropertyValuesHolder sx, sy, xMove, yMove;
                    sx = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.5f, 1 );
                    sy = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.5f, 1 );
                    xMove = PropertyValuesHolder.ofFloat( "x", tars[ 0 ], objs[ 0 ] );
                    yMove = PropertyValuesHolder.ofFloat( "y", tars[ 1 ], objs[ 1 ] );
                    
                    //
                    obj = ObjectAnimator.ofPropertyValuesHolder( mover, sx, sy, xMove, yMove );
                    obj.setInterpolator( new AccelerateInterpolator( 1f ) );
                    obj.setDuration( 350 );
                    obj.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            //
                            if ( GameEngine.isKilled )
                            {
                                return;
                            }
                            
                            animCounter--;
                            parent.removeView( mover );
                            v.setVisibility( VISIBLE );
                            
                            // Seems to fail at times
                            if ( gameEngine != null && gameEngine.animatorList != null )
                            {
                                gameEngine.animatorList.remove( obj );
                            }
                            
                            if ( animCounter <= 0 )
                            {
                                dismiss();
                            }
                            super.onAnimationEnd( animation );
                        }
                    } );
                    obj.start();
                    
                    // Seems to fail at times
                    if ( gameEngine.animatorList != null )
                    {
                        gameEngine.animatorList.add( obj );
                    }
                    
                    animCounter++;
                    //
                    target.setVisibility( INVISIBLE );
                }
            }
    
            if ( needToDismiss )
            {
                dismiss();
            }
        }
        else
        {
            for ( int i = 0; i < targetViews.size(); i++ )
            {
                View target = view_dialog.findViewById( targetIds[ i ] );
                View v      = targetViews.get( i );
                
                
                if ( target.getVisibility() == VISIBLE )
                {
                    v.setVisibility( VISIBLE );
                }
            }
            
            dismiss();
        }
    }
    
    
    @Override
    public void onDetachedFromWindow()
    {
        gameEngine = null;
        view_dialog = null;
        targetMsg = null;
        targetViews = null;
        
        super.onDetachedFromWindow();
    }
}
