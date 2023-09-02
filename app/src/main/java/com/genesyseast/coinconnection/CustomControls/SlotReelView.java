package com.genesyseast.coinconnection.CustomControls;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.genesyseast.coinconnection.Fragments.PlaySlots;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameGraphics.GameBitmaps;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;


public class SlotReelView
        extends AppCompatImageView
{
    
    public         int                currentY;
    public         int                reelPosition  = 0;
    //
    private        int                reelImageResId;
    private        Context            context;
    private        Bitmap             bitmap;
    public         ValueAnimator      reelAnimator;
    private        float              scaleRatio    = -1.0f;
    private        OnReelSpinListener onReelSpinListener;
    private static int                reelsRunning  = 0;
    private        GameEngine         gameEngine;
    private        int                spinDirection = 0;
    
    /**
     * constructors
     *
     * @param context N/A
     */
    public SlotReelView( Context context )
    {
        super( context );
        
        this.context = context;
        this.currentY = 0;
        this.reelPosition = 0;
        this.gameEngine = GameEngine.getInstance( getContext() );
        
        readAttr( context, null );
    }
    
    public SlotReelView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        
        this.context = context;
        this.currentY = 0;
        this.reelPosition = 0;
        this.gameEngine = GameEngine.getInstance( getContext() );
        
        readAttr( context, attrs );
    }
    
    public SlotReelView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        
        this.context = context;
        this.currentY = 0;
        this.reelPosition = 0;
        this.gameEngine = GameEngine.getInstance( getContext() );
        
        readAttr( context, attrs );
    }
    
    public interface OnReelSpinListener
    {
        void onReelStoppedSpinning();
    }
    
    /**
     * //###########################
     * <p>
     * Get the interface listener
     * <p>
     * //###########################
     *
     * @param onReelSpinListener
     */
    public void setOnReelSpinListener( OnReelSpinListener onReelSpinListener )
    {
        this.onReelSpinListener = onReelSpinListener;
    }
    
    
    /**
     * //###########################
     * <p>
     * Getters and Setters
     * <p>
     * //###########################
     *
     * @return
     */
    public int getCurrentY()
    {
        return currentY;
    }
    
    public void setCurrentY( int currentY )
    {
        this.currentY = currentY;
    }
    
    public int getReelImageResId()
    {
        return reelImageResId;
    }
    
    public void setReelImageResId( int reelImageResId )
    {
        this.reelImageResId = reelImageResId;
        this.bitmap = GameBitmaps.getBitmapImage( context, reelImageResId );
    }
    
    
    /**
     * //###########################
     * <p>
     * Update the reel spinning
     * animation according to
     * the ValueAnimator
     * <p>
     * //###########################
     *
     * @param canvas
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        super.onDraw( canvas );
        
        if ( bitmap != null )
        {
            // NOTE: To understand what is happening here, the X and Y
            //       variables are where the canvas is being drawn TO.
            //       These values point to the CANVAS
            //       Destination here is always X: X-value, Y: Y-value
            
            // Ratio using width because the width of Bitmap and ImageView holder are different
            // But the CopyMap and ImageView heights are the same
            // This allows us to retain the aspect ratio
            if ( scaleRatio == -1 )
            {
                scaleRatio = (( float ) getWidth() / ( float ) bitmap.getWidth());
            }
            
            
            // This makes the image loop back to the
            // start to simulate a reel spinning
            currentY %= bitmap.getHeight();
            
            // Save canvas state
            canvas.save();
            
            canvas.scale( scaleRatio, scaleRatio );
            canvas.drawBitmap( bitmap, 0, currentY, null );
            
            // Draw the specified bitmap, with its top/left corner at (x,y),
            // using the specified paint, transformed by the current matrix.
            //
            // Copy the section we need that was possibly clipped
            // due to the image position being adjusted
            // Typically, his is drawing a portion of the reel close to
            if ( currentY > 0 )
            {
                canvas.drawBitmap( bitmap, 0, (currentY - bitmap.getHeight()), null );
            }
            
            // Restore canvas state
            canvas.restore();
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Start the reel spinner
     * <p>
     * //###########################
     *
     * @param runDuration N/A
     * @param stopPoint   N/A
     */
    public void spinTheReel( int runDuration, int stopPoint, int spinDirection )
    {
        if ( reelImageResId != -1 && context != null )
        // Split the BG image and apply to each layout needed
        {
            this.bitmap = GameBitmaps.getBitmapImage( context, reelImageResId );
            
            // Set the reel to spin around a mandatory 8 times
            // Variable used to control page overlapping from excess spinning
            if ( bitmap != null )
            {
                reelPosition = (currentY % bitmap.getHeight());
                
                //
                if ( spinDirection == 0 )
                {
                    reelAnimator = ValueAnimator.ofInt( ((runDuration + 2) * bitmap.getHeight()) + stopPoint, reelPosition );
                }
                else
                {
                    reelAnimator = ValueAnimator.ofInt( reelPosition, ((runDuration + 2) * bitmap.getHeight()) + stopPoint );
                }
                
                reelAnimator.setDuration( (runDuration + 3) * 500 );                  // Duration in millisecond
                reelAnimator.setInterpolator( new AccelerateDecelerateInterpolator() );  // E.g. Linear, Accelerate, Decelerate
                reelAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {   // Scroll the image
                        currentY = ( int ) animation.getAnimatedValue();
                        
                        // Play the reel spinning sound
                        //                        gameEngine.soundPlayer.play( PlaySound.SLOTS_REEL_TICK, PlaySound.PLAY );
                        // Update the image onto the ImageView
                        invalidate();
                    }
                } );
                
                // Reel Spinning will be finally false on last reel spinning
                reelAnimator.addListener( new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart( Animator animation )
                    {
                        PlaySlots.reelIsSpinning = true;
                        reelsRunning++;
                    }
                    
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        int[] winSound = { PlaySound.REEL_STOP_WIN_3, PlaySound.REEL_STOP_2, PlaySound.REEL_STOP_1 };
                        
                        //@@@@@@@@@@@@@@@@@@@@@ Level complete: 3 seconds
                        // Play reel stop sound
                        if ( PlaySlots.closeCall && reelsRunning > 1 || (reelsRunning == 1 && PlaySlots.theWinnerIs > -1) )
                        {
                            gameEngine.soundPlayer.playBgSfx( winSound[ reelsRunning - 1 ] );
                        }
                        else
                        {
                            // Random loser
                            gameEngine.soundPlayer.playBgSfx( winSound[ 2 ] );
                        }
                        
                        //
                        reelsRunning--;
                        if ( reelsRunning <= 0 )
                        {
                            PlaySlots.reelIsSpinning = false;
                            
                            //@@@@@@@@@@@@@@@@@@ Stop looping reel sound!
                            gameEngine.soundPlayer.play( PlaySound.SLOTS_REEL_TICK, PlaySound.STOP, PlaySlots.reelTicker );
                            
                            // Delay the winner for a little!
                            ValueAnimator v = ValueAnimator.ofInt( 0, 100 ).setDuration( 750 );
                            v.addListener( new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    // Send the end of reel spinning, pick a winner
                                    if ( onReelSpinListener != null )
                                    {
                                        onReelSpinListener.onReelStoppedSpinning();
                                    }
                                    
                                    // Kill it!
                                    reelAnimator = null;
                                }
                            } );
                            //
                            v.start();
                        }
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
                gameEngine.animatorList.add( reelAnimator );
                reelAnimator.start();
            }
        }
    }
    
    /**
     * //#################################
     * <p>
     * Load custom attributes
     * <p>
     * //#################################
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.SlotReelView );
        
        // Read the colors
        spinDirection = a.getInt( R.styleable.SlotReelView_spinDirection, 0 );
        
        // Reel Image
        setReelImageResId( a.getResourceId( R.styleable.SlotReelView_reelImage, 0 ) );
        
        // Where the reel image starts from the top
        if ( a.hasValue( R.styleable.SlotReelView_startY ) )
        {
            setCurrentY( a.getInt( R.styleable.SlotReelView_startY, 0 ) );
        }
        
        //
        a.recycle();
    }
}
