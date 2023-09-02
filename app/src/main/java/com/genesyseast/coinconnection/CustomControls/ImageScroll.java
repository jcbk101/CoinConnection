package com.genesyseast.coinconnection.CustomControls;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

import com.genesyseast.coinconnection.R;


public class ImageScroll
        extends AppCompatImageView
{
    
    public  int           currentY;
    public  int           scrollPosition  = 0;
    //
    private Context       context;
    private Bitmap        bitmap;
    private Drawable      bGround;
    private Rect          imgRect;
    private Rect          bgRect;
    public  ValueAnimator scrollAnimator;
    private float         ratio           = -1;
    private float         startY;
    private boolean       fitBottom       = false;
    private int           scrollDirection = 0;
    private int           scrollBgResId   = 0;
    
    
    /**
     * constructors
     *
     * @param context N/A
     */
    public ImageScroll( Context context )
    {
        super( context );
        
        this.context = context;
        this.currentY = 0;
        this.scrollPosition = 0;
    }
    
    public ImageScroll( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.context = context;
        this.currentY = 0;
        this.scrollPosition = 0;
        
        readAttr( context, attrs );
        
    }
    
    public ImageScroll( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        
        this.context = context;
        this.currentY = 0;
        this.scrollPosition = 0;
        
        readAttr( context, attrs );
        
    }
    
    
    /**
     * @param scrollBgResId
     */
    private void setScrollingBg( int scrollBgResId )
    {
        bitmap = null;
        bitmap = BitmapFactory.decodeResource( getContext().getResources(), scrollBgResId );
    }
    
    
    /**
     * Update the reel spinning animation according to the ValueAnimator
     *
     * @param canvas
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        //        super.onDraw( canvas );
        
        if ( bitmap != null )
        {
            // NOTE: To understand what is happening here, the X and Y
            //       variables are where the canvas is being drawn TO.
            //       These values point to the CANVAS
            //       Destination here is always X: X-value, Y: Y-value
            
            // Ratio using width because the width of Bitmap and ImageView holder are different
            // But the CopyMap and ImageView heights are the same
            // This allows us to retain the aspect ratio
            if ( ratio == -1 )
            {
                ratio = ( float ) getWidth() / bitmap.getWidth();
            }
            
            if ( fitBottom )
            {
                if ( bgRect == null )
                {
                    // Must scale the Destination size to fit the incoming
                    // Source size. Therefor the source is indexed properly into the
                    // Texture
                    int startY = Math.max( bitmap.getHeight() - (int)(getHeight() / ratio), 0 );
                    
                    bgRect = new Rect( 0, 0, getWidth(), getHeight() );
                    imgRect = new Rect( 0, startY, bitmap.getWidth(), bitmap.getHeight() );
                }
                
                //
                canvas.drawBitmap( bitmap, imgRect, bgRect, null );
                
                
                if ( scrollAnimator != null && scrollAnimator.isRunning() )
                {
                    scrollAnimator.end();
                    scrollAnimator = null;
                }
                
                return;
            }
            
            
            // This makes the image loop back to the
            // start to simulate a reel spinning
            currentY %= bitmap.getHeight();
            
            // Save canvas state
            canvas.save();
            
            canvas.scale( ratio, ratio );
            canvas.drawBitmap( bitmap, 0, currentY, null );
            
            
            // Draw the specified bitmap, with its top/left corner at (x,y),
            // using the specified paint, transformed by the current matrix.
            //
            // Copy the section we need that was possibly clipped
            // due to the image position being adjusted
            // Typically, his is drawing a portion of the reel close to
            if ( currentY > 0 )
            {
                canvas.drawBitmap( bitmap, 0, currentY - bitmap.getHeight(), null );
            }
            
            // Restore canvas state
            canvas.restore();
        }
    }
    
    
    /**
     * //#############################
     * <p>
     * Animate the BG scrolling
     * <p>
     * //#############################
     *
     * @param duration
     * @param direction
     * @param repeat
     */
    public void scrollTheImage( long duration, int direction, int repeat )
    {
        if ( bitmap != null && context != null )
        {
            currentY = 0;
            scrollDirection = direction;
            
            if ( scrollDirection == 0 )
            {
                scrollAnimator = ValueAnimator.ofInt( bitmap.getHeight(), 0 );
            }
            else
            {
                scrollAnimator = ValueAnimator.ofInt( 0, bitmap.getHeight() );
            }
            
            scrollAnimator.setDuration( duration );
            scrollAnimator.setRepeatCount( repeat );
            scrollAnimator.setInterpolator( new LinearInterpolator() );
            scrollAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {   // Scroll the image
                    currentY = ( int ) animation.getAnimatedValue();
                    
                    // Update the image onto the ImageView
                    invalidate();
                }
            } );
            
            scrollAnimator.start();
        }
    }
    
    
    /**
     * Load custom attributes
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageScroll );
        
        // Read the colors
        currentY = a.getInt( R.styleable.ImageScroll_startY, 0 );
        
        // If we want the image to be top justified
        fitBottom = a.getBoolean( R.styleable.ImageScroll_fitBottom, false );
        
        // Select a scrolling direction
        scrollDirection = a.getInt( R.styleable.ImageScroll_scrollDirection, 0 );
        
        // The scrolling layer
        scrollBgResId = a.getResourceId( R.styleable.ImageScroll_scrollingBg, 0 );
        setScrollingBg( scrollBgResId );
        
        //
        a.recycle();
    }
    
}
