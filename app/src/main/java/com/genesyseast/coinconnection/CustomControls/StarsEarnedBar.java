package com.genesyseast.coinconnection.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.genesyseast.coinconnection.GameGraphics.GameBitmaps;
import com.genesyseast.coinconnection.R;

public class StarsEarnedBar
        extends View
{
    private Drawable tickOn;
    private Drawable tickOff;
    private Drawable progressDrawable;
    private float[]  starsPosi;
    private int      tickWidth;
    private int      tickHeight;
    private int      max;
    private int      min;
    private int      progress;
    private int      starsOn               = 0;
    //
    private boolean  hasResizedProgressBar = false;
    private boolean  hasResizeTickOff      = false;
    private boolean  hasResizeTickOn       = false;
    
    
    /**
     * //#########################################
     * <p>
     * Getters and Setters
     * <p>
     * //#########################################
     *
     * @return
     */
    public Drawable getTickOn()
    {
        return tickOn;
    }
    
    public void setTickOn( Drawable tickOn )
    {
        Bitmap bitmap = GameBitmaps.drawableToBitmap( tickOn, false );
        
        if ( getHeight() > 0 && bitmap != null )
        {
            tickWidth = tickHeight = (getHeight() + getHeight() / 2);
            this.tickOn = new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( bitmap, tickWidth, tickHeight, true ) );
            
            hasResizeTickOn = true;
        }
        else
        {
            this.tickOn = tickOn;
            hasResizeTickOn = false;
        }
    }
    
    public Drawable getTickOff()
    {
        return tickOff;
    }
    
    public void setTickOff( Drawable tickOff )
    {
        Bitmap bitmap = GameBitmaps.drawableToBitmap( tickOff, false );
        
        if ( getHeight() > 0 && bitmap != null )
        {
            //            Bitmap   bitmap;
            Drawable newDrawable;
            
            tickWidth = tickHeight = (getHeight() + getHeight() / 2);
            this.tickOff = new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( bitmap, tickWidth, tickHeight, true ) );
            
            hasResizeTickOff = true;
        }
        else
        {
            this.tickOff = tickOff;
            hasResizeTickOff = false;
        }
    }
    
    public float[] getStarsPosi()
    {
        return starsPosi;
    }
    
    public void setStarsPosi( float[] starsPosi )
    {
        this.starsPosi = starsPosi;
    }
    
    public void setStarsPosi( String starsList )
    {
        if ( starsList != null )
        {
            String[] list      = starsList.split( "," );
            float[]  starsPosi = new float[ list.length ];
            
            //
            //#########################
            //
            for ( int i = 0; i < list.length; i++ )
            {
                String num   = list[ i ];
                int    value = Integer.parseInt( num );
                
                starsPosi[ i ] = Math.max( value, 1 );
            }
            
            this.starsPosi = starsPosi;
            
            return;
        }
        
        this.starsPosi = new float[]{ 0, 0, 0 };
    }
    
    
    public void setProgress( int progress )
    {
        this.progress = progress;
        invalidate();
    }
    
    
    public int getTickWidth()
    {
        return tickWidth;
    }
    
    public void setTickWidth( int tickWidth )
    {
        this.tickWidth = tickWidth;
        invalidate();
    }
    
    public int getTickHeight()
    {
        return tickHeight;
    }
    
    public void setTickHeight( int tickHeight )
    {
        this.tickHeight = tickHeight;
        invalidate();
    }
    
    public int getMax()
    {
        return max;
    }
    
    public void setMax( int max )
    {
        this.max = max;
        invalidate();
    }
    
    public int getMin()
    {
        return min;
    }
    
    public void setMin( int min )
    {
        this.min = min;
        invalidate();
    }
    
    public int getProgress()
    {
        return progress;
    }
    
    public Drawable getProgressDrawable()
    {
        return progressDrawable;
    }
    
    public int getStarsOn()
    {
        return starsOn;
    }
    
    
    /**
     * //###############################
     * <p>
     * Adjust the height according to
     * the view's height, not the PNG
     * <p>
     * //###############################
     *
     * @param d
     */
    public void setProgressDrawable( Drawable d )
    {
        Bitmap bitmap = GameBitmaps.drawableToBitmap( d, false );
        
        if ( getHeight() > 0 && bitmap != null )
        {
            int paddingSides  = getPaddingLeft() + getPaddingRight();
            int paddingTopBot = getPaddingTop() + getPaddingBottom();
            
            progressDrawable = new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( bitmap, d.getIntrinsicWidth() - paddingSides, getHeight() - paddingTopBot, true ) );
            
            //
            hasResizedProgressBar = true;
            invalidate();
        }
        else
        {
            progressDrawable = d;
            hasResizedProgressBar = false;
        }
    }
    
    
    
    /**
     * //#########################################
     * <p>
     * Constructors
     * <p>
     * //#########################################
     *
     * @param context
     */
    public StarsEarnedBar( Context context )
    {
        super( context );
    }
    
    public StarsEarnedBar( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
    }
    
    public StarsEarnedBar( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        readAttr( context, attrs );
    }
    
    
    /**
     * //##############################
     * <p>
     * Some Drawables need to be
     * resized
     * <p>
     * //##############################
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw( Canvas canvas )
    {
        if ( !hasResizedProgressBar )
        {
            setProgressDrawable( getProgressDrawable() );
        }
        
        if ( !hasResizeTickOn )
        {
            setTickOn( getTickOn() );
        }
        
        if ( !hasResizeTickOff )
        {
            setTickOff( getTickOff() );
        }
        
        super.onDraw( canvas );
        
        drawProgressBar( canvas );
        drawTickMarks( canvas );
    }
    
    
    /**
     * //############################
     * <p>
     * Class denies access to method
     * <p>
     * //############################
     */
    private void drawProgressBar( Canvas canvas )
    {
        if ( progressDrawable != null )
        {
            final int saveCount = canvas.save();
            float     adjust    = ( float ) getProgress() / ( float ) getMax();
            float     clipX     = (getWidth() * adjust);
            
            // Set up the clip
            canvas.clipRect( 0, 0, clipX, getHeight() );
            
            // Set up the bar
            progressDrawable.setBounds( getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom() );
            
            // Draw the bar
            progressDrawable.draw( canvas );
            
            canvas.restoreToCount( saveCount );
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Class denies access to method
     * <p>
     * //############################
     */
    private void drawTickMarks( Canvas canvas )
    {
        if ( tickOn != null && tickOff != null && starsPosi != null )
        {
            final int count = starsPosi.length;
            
            if ( count > 1 )
            {
                final int saveCount = canvas.save();
                float     xOffset;
                float     xDiv;
                Drawable  tickMark;
                
                
                //###############################
                //
                //
                //
                //###############################
                starsOn = 0;
                
                for ( float v : starsPosi )
                {
                    if ( getProgress() >= v )
                    {
                        tickMark = tickOn;
                        starsOn++;
                    }
                    else
                    {
                        tickMark = tickOff;
                    }
                    
                    //
                    final int w     = tickMark.getIntrinsicWidth();
                    final int h     = tickMark.getIntrinsicHeight();
                    final int halfW = w >= 0 ? w / 2 : 1;
                    final int halfH = h >= 0 ? h / 2 : 1;
                    
                    tickMark.setBounds( -halfW, -halfH, halfW, halfH );
                    
                    // Place the star where it should be on the line
                    xDiv = (( float ) getMax() / v);
                    xOffset = (getWidth() - getPaddingLeft() - getPaddingRight()) / xDiv;
                    
                    // Move to the X position for THIS star
                    canvas.translate( getPaddingLeft() + xOffset + (halfW / 2f), getHeight() / 2f );
                    
                    // Draw the star
                    tickMark.draw( canvas );
                    
                    // Reset the Pen's position back to the start
                    // Of the draw location
                    canvas.translate( -(getPaddingLeft() + xOffset + (halfW / 2f)), -(getHeight() / 2f) );
                }
                
                canvas.restoreToCount( saveCount );
            }
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Process atributes
     * <p>
     * //############################
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.StarsEarnedBar );
        int        index;
        Bitmap     bitmap;
        Drawable   newDrawable;
        
        
        //##############################
        //
        // Get the number values
        //
        //##############################
        setTickWidth( a.getDimensionPixelSize( R.styleable.StarsEarnedBar_tickWidth, 0 ) );
        setTickHeight( a.getDimensionPixelSize( R.styleable.StarsEarnedBar_tickHeight, 0 ) );
        
        //
        setMin( a.getInt( R.styleable.StarsEarnedBar_min, 0 ) );
        setMax( a.getInt( R.styleable.StarsEarnedBar_max, 0 ) );
        setProgress( a.getInt( R.styleable.StarsEarnedBar_progress, 0 ) );
        
        
        //##############################
        //
        // Get the progress drawable to
        // use for progress indicator
        //
        //##############################
        index = a.getResourceId( R.styleable.StarsEarnedBar_progressDrawable, 0 );
        if ( index > 0 )
        {
            setProgressDrawable( ResourcesCompat.getDrawable( getResources(), index, null ) );
        }
        
        
        //##############################
        //
        // Get the tick images
        //
        //##############################
        index = a.getResourceId( R.styleable.StarsEarnedBar_tickMarkOn, 0 );
        if ( index > 0 )
        {
            setTickOn( ResourcesCompat.getDrawable( getResources(), index, null ) );
        }
        
        
        index = a.getResourceId( R.styleable.StarsEarnedBar_tickMarkOff, 0 );
        if ( index > 0 )
        {
            setTickOff( ResourcesCompat.getDrawable( getResources(), index, null ) );
        }
        
        
        //##############################
        //
        // Read the title and set
        // it if any
        //
        //##############################
        String starsList = a.getString( R.styleable.StarsEarnedBar_starPositions );
        
        if ( starsList != null )
        {
            String[] list = starsList.split( "," );
            
            starsPosi = new float[ list.length ];
            
            //
            //#########################
            //
            for ( int i = 0; i < list.length; i++ )
            {
                String num   = list[ i ];
                int    value = Integer.parseInt( num );
                
                if ( value > 0 )
                {
                    // Holds the percentage to divide the
                    // progress width by.
                    // Then the value is the star's X position!
                    starsPosi[ i ] = value;
                }
                else
                {
                    starsPosi[ i ] = 1;
                }
            }
        }
        
        a.recycle();
    }
}
