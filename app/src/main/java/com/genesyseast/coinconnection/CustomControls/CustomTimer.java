package com.genesyseast.coinconnection.CustomControls;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class CustomTimer
        extends ValueAnimator
        implements ValueAnimator.AnimatorListener, ValueAnimator.AnimatorUpdateListener
{
    public static final int TIMER_INACTIVE = -1;
    public static final int TIMER_RUNNING  = 1;
    public static final int TIMER_DONE     = 2;
    //
    public static final int NORMAL_CANCEL  = 0;
    public static final int USER_CANCEL    = 1;
    public static final int PAUSE_CANCEL   = 2;
    
    public static long                       currentTime;
    public static int                        timerStatus;
    private       long                       lastTime      = 0;
    public static View                       target;
    private       Context                    context;
    private       OnTimerCommunicateListener communicator;
    private       AnimatorSet                animSet;
    private       int                        userCancel    = 0;
    public        long                       savedTime     = 0;
    private       Activity                   activity;
    public        boolean                    videoAccepted = false;
    //
    private       GameEngine                 gameEngine;
    private       boolean                    timeAsked     = false;
    
    /**
     * Reset all variables
     */
    public void clear()
    {
        CustomTimer.target = null;
        CustomTimer.currentTime = 0;
        this.context = null;
        videoAccepted = false;
    }
    
    
    /**
     * Needed interface to communicate with the parent caller
     */
    public interface OnTimerCommunicateListener
    {
        void onTimeExpired( int status );
    }
    
    
    /**
     * Interface communicator
     *
     * @param communicator N/A
     */
    public void setOnCommunicator( OnTimerCommunicateListener communicator )
    {
        this.communicator = communicator;
    }
    
    
    /**
     * Get to check status
     *
     * @return
     */
    public OnTimerCommunicateListener getCommunicator()
    {
        return communicator;
    }
    
    /**
     * Helper to know if Time's Up! messag is to be shown
     *
     * @param userCancel
     */
    public void setUserCancel( int userCancel )
    {
        this.userCancel = userCancel;
    }
    
    /**
     * Constructor
     *
     * @param millisInFuture N/A
     * @param view           N/A
     */
    public CustomTimer( long millisInFuture, Context context, View view )
    {
        this.animSet = ( AnimatorSet ) AnimatorInflater.loadAnimator( context, R.animator.time_pulse );
        CustomTimer.target = view;
        CustomTimer.currentTime = millisInFuture;
        this.context = context;
        
        //
        timeAsked = false;
        userCancel = 0;
        lastTime = (currentTime / 1000) - 1;
        if ( lastTime < 0 )
        {
            lastTime = 0;
        }
        
        //
        setIntValues( ( int ) millisInFuture, 0 );
        setInterpolator( new LinearInterpolator() );
        setDuration( currentTime );
        addListener( this );
        addUpdateListener( this );
        
        //
        if ( gameEngine == null )
        {
            gameEngine = GameEngine.getInstance( context );
        }
    }
    
    
    /**
     * Used to change the timer count time
     *
     * @param time
     */
    public void updateTime( float time )
    {
        currentTime = ( long ) (getDuration() + (time * 1000f));
        
        setIntValues( ( int ) currentTime, 0 );
        super.setDuration( currentTime );
    }
    
    
    /**
     * Used to change the timer count time
     *
     * @param time
     */
    public void resumeTime( long time )
    {
        currentTime = time;
        
        setIntValues( ( int ) currentTime, 0 );
        super.setDuration( currentTime );
    }
    
    
    @Override
    public void resume()
    {
/*
        SimpleDateFormat fmt      = new SimpleDateFormat( "mm : ss", Locale.getDefault() );
        String           datetime = fmt.format( currentTime );
        
        Toast.makeText( context, "Timer restarted: " + datetime, Toast.LENGTH_SHORT ).show();
*/
        super.resume();
    }
    
    /**
     * Test to see if layer said yes to watching a video after time expired
     *
     * @return
     */
    public boolean isVideoAccepted()
    {
        return videoAccepted;
    }
    
    @Override
    public void onAnimationStart( Animator animation )
    {
        timerStatus = TIMER_RUNNING;
    }
    
    @Override
    public void onAnimationEnd( Animator animation )
    {
        //SecureRandom r = new SecureRandom();
        
        if ( userCancel == 0 )
        {
            // Let the system know it's a REAL timer up!
            communicator.onTimeExpired( NORMAL_CANCEL );
            
            // Just exit this level!
            timerStatus = TIMER_DONE;
            target = null;
            currentTime = 0;
            this.context = null;
            videoAccepted = false;
        }
        else if ( userCancel == 1 )
        {
            // Just exit this level
            timerStatus = TIMER_DONE;
            userCancel = 0;
            target = null;
            currentTime = 0;
            this.context = null;
            videoAccepted = false;
        }
        else
        {
            // Resuming a timer from a user function
            userCancel = 0;
        }
    }
    
    
    @Override
    public void onAnimationCancel( Animator animation )
    {
        timerStatus = TIMER_INACTIVE;
    }
    
    @Override
    public void onAnimationRepeat( Animator animation )
    {
    
    }
    
    @Override
    public void onAnimationUpdate( ValueAnimator animation )
    {
        currentTime = ( int ) animation.getAnimatedValue();
        
        if ( target != null )
        {
            SimpleDateFormat fmt      = new SimpleDateFormat( "mm : ss", Locale.getDefault() );
            String           datetime = fmt.format( currentTime );
            TextView         textView = ( TextView ) target;
            
            //
            textView.setText( datetime );
            
            //            if ( (currentTime % 1000) == 0 && currentTime < 7000 && !animSet.isRunning() )
            if ( (currentTime / 1000) == lastTime && currentTime < 10000 && !animSet.isRunning() )
            {
                animSet.setTarget( target );
                animSet.start();
            }
            
            lastTime = (currentTime / 1000) - 1;
            if ( lastTime < 0 )
            {
                SecureRandom r = new SecureRandom();
                lastTime = 0;
                
            }
        }
    }
}