package com.genesyseast.coinconnection.CustomControls;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.DrawableCompat;

import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;


public class ImageTextView
        extends AppCompatTextView
        implements View.OnTouchListener
{
    //    public  View    textView = null;
    //    private int     textViewId;
    //
    private Typeface typeface;
    private int      strokeColor;
    private int      strokeWidth;
    private int      imageResource;
    private int      backgroundResource;
    //
    private Bitmap   srcImage = null;
    private Bitmap   bgImage  = null;
    //
    private Rect     imgRect  = null;
    private Rect     bgRect   = null;
    private int      pLeft    = 0;
    private int      pRight   = 0;
    private int      pTop     = 0;
    private int      pBottom  = 0;
    private int      drawableHeight;
    private int      drawableWidth;
    private boolean  isButton = false;
    
    /**
     * Constructors
     *
     * @param context
     */
    public ImageTextView( Context context )
    {
        super( context );
        readAttr( context, null );
        setOnTouchListener( this );
    }
    
    public ImageTextView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
        setOnTouchListener( this );
    }
    
    
    public void setImageResource( int imageResource )
    {
        this.imageResource = imageResource;
        
        if ( imageResource != 0 )
        {
            srcImage = BitmapFactory.decodeResource( getContext().getResources(), imageResource );
            if ( srcImage == null )
            {
                srcImage = getBitmapFromVectorDrawable( getContext(), imageResource );
            }
            
            imgRect = null;
        }
    }
    
    
    @Override
    public void setCompoundDrawablesRelative( @Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom )
    {
        //        int size = getResources().getDimensionPixelSize( R.dimen._24sdp );
        
        if ( drawableWidth > 0 && drawableHeight > 0 )
        {
            if ( start != null )
            {
                start.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( end != null )
            {
                end.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( top != null )
            {
                top.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( bottom != null )
            {
                bottom.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
        }
        
        super.setCompoundDrawablesRelative( start, top, end, bottom );
    }
    
    
    /**
     * convert Vectors to Bitmap
     *
     * @param context
     * @param drawableId
     *
     * @return
     */
    public static Bitmap getBitmapFromVectorDrawable( Context context, int drawableId )
    {
        Drawable drawable = AppCompatResources.getDrawable( context, drawableId );
        
        if ( drawable != null )
        {
            if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP )
            {
                drawable = (DrawableCompat.wrap( drawable )).mutate();
            }
            
            
            Bitmap bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
            Canvas canvas = new Canvas( bitmap );
            drawable.setBounds( 0, 0, canvas.getWidth(), canvas.getHeight() );
            drawable.draw( canvas );
            
            return bitmap;
        }
        
        return null;
    }
    
    @Override
    public void setBackgroundResource( int resId )
    {
        this.backgroundResource = resId;
        
        if ( resId != 0 )
        {
            bgImage = BitmapFactory.decodeResource( getContext().getResources(), resId );
            bgRect = null;
        }
        
        //        super.setBackgroundResource( resId );
    }
    
    
    public int getStrokeColor()
    {
        return strokeColor;
    }
    
    public void setStrokeColor( int strokeColor )
    {
        this.strokeColor = strokeColor;
        invalidate();
    }
    
    public int getStrokeWidth()
    {
        return strokeWidth;
    }
    
    public void setStrokeWidth( int strokeWidth )
    {
        this.strokeWidth = strokeWidth;
        invalidate();
    }
    
    public void setButton( boolean button )
    {
        isButton = button;
    }
    
    /**
     * //#################################
     * <p>
     * Used mainly for button press
     * animations
     * <p>
     * //#################################
     *
     * @param v
     * @param event
     *
     * @return
     */
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        if ( isButton )
        {
            switch ( event.getAction() )
            {
                case MotionEvent.ACTION_DOWN:
                {
                    // Overlay is black with transparency of 0x77 (119)
                    setScaleX( .8f );
                    setScaleY( .8f );
                    //                    getBackground().setColorFilter( 0xFF000000, PorterDuff.Mode.DARKEN );
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                {
                    // Clear the overlay
                    animate().setDuration( 350 );
                    animate().scaleX( 1f ).scaleY( 1f ).setInterpolator( new CustomBounceInterpolator( .2,20 ) ).start();
/*
                    setScaleX( 1f );
                    setScaleY( 1f );
*/
//                    getBackground().clearColorFilter();
                    invalidate();
                    break;
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * Overridden onDraw for custom drawing
     *
     * @param canvas N/A
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        int    textColor = getCurrentTextColor();
        Shader shader    = getPaint().getShader();
        
        
        // Self controlled BG
        if ( backgroundResource != 0 )
        {
            if ( bgRect == null )
            {
                bgRect = new Rect( 0, 0, getWidth(), getHeight() );
            }
            
            //
            canvas.drawBitmap( bgImage, null, bgRect, null );
        }
        
        // Self controlled FG, Always draw centered
        if ( imageResource != 0 )
        {
            if ( imgRect == null )
            {
                imgRect = new Rect( getPaddingStart() + pLeft, getPaddingTop() + pTop, getWidth() - (getPaddingEnd() + pRight), getHeight() - (getPaddingBottom() + pBottom) );
            }
            
            canvas.drawBitmap( srcImage, null, imgRect, null );
        }
        
        
        if ( strokeWidth > 0 )
        {
            getPaint().clearShadowLayer();
            
            getPaint().setStrokeWidth( strokeWidth );
            getPaint().setStrokeJoin( Paint.Join.BEVEL );
            getPaint().setStyle( Paint.Style.STROKE );
            setTextColor( strokeColor );
            super.onDraw( canvas );
        }
        
        //
        getPaint().setStyle( Paint.Style.FILL );
        getPaint().setShader( shader );
        setTextColor( textColor );
        super.onDraw( canvas );
    }
    
    
    /**
     * Load custom attributes
     *
     * @param context N/A
     * @param attrs   N/A
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageTextView );
        
        // Read the colors
        strokeColor = a.getColor( R.styleable.ImageTextView_strokeColor, 0 );
        strokeWidth = a.getDimensionPixelSize( R.styleable.ImageTextView_strokeWidth, 0 );
        
        //
        drawableHeight = a.getDimensionPixelSize( R.styleable.ImageTextView_drawableHeight, 0 );
        drawableWidth = a.getDimensionPixelSize( R.styleable.ImageTextView_drawableWidth, 0 );
        
        //
        isButton = a.getBoolean( R.styleable.ImageTextView_isButton, false );
        
        //
        if ( drawableHeight > 0 && drawableWidth > 0 )
        {
            Drawable[] drawables = getCompoundDrawablesRelative();
            setCompoundDrawablesRelative( drawables[ 0 ], drawables[ 1 ], drawables[ 2 ], drawables[ 3 ] );
        }
        
        
        int i = a.getResourceId( R.styleable.ImageTextView_srcCompat, 0 );
        setImageResource( i );
        
        int c = a.getDimensionPixelSize( R.styleable.ImageTextView_srcCompatPadding, 0 );
        
        if ( c == 0 )
        {
            pLeft = a.getDimensionPixelSize( R.styleable.ImageTextView_srcCompatPaddingLeft, 0 );
            pTop = a.getDimensionPixelSize( R.styleable.ImageTextView_srcCompatPaddingTop, 0 );
            pRight = a.getDimensionPixelSize( R.styleable.ImageTextView_srcCompatPaddingRight, 0 );
            pBottom = a.getDimensionPixelSize( R.styleable.ImageTextView_srcCompatPaddingBottom, 0 );
        }
        else
        {
            pLeft = pTop = pRight = pBottom = c;
        }
        
        a.recycle();
    }
}
