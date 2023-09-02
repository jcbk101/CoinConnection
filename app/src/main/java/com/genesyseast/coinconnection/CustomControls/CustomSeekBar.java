package com.genesyseast.coinconnection.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;


import com.genesyseast.coinconnection.R;

public class CustomSeekBar
        extends AppCompatSeekBar
{
    private int    thumbWidth;
    private int    thumbHeight;
    private Bitmap bitmap;
    
    public CustomSeekBar( Context context )
    {
        super( context );
    }
    
    public CustomSeekBar( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
        
    }
    
    public CustomSeekBar( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        readAttr( context, attrs );
        
    }
    
    
    @Override
    public void setEnabled( boolean enabled )
    {
        super.setEnabled( enabled );
        
        if ( isEnabled() )
        {
            getThumb().clearColorFilter();
        }
        else
        {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation( 0 );  //0 means grayscale
            ColorMatrixColorFilter cf = new ColorMatrixColorFilter( matrix );
            getThumb().setColorFilter( cf );
        }
    }
    
    
    @Override
    public void setThumb( Drawable thumb )
    {
        if ( thumbHeight != 0 && thumbWidth != 0 )
        {
            if ( bitmap != null )
            {
                bitmap = null;
            }
            
            bitmap = (( BitmapDrawable ) thumb.getCurrent()).getBitmap();
            
            // Scale it to your size
            Drawable newDrawable = new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( bitmap, thumbWidth, thumbHeight, true ) );
            
            super.setThumb( newDrawable );
        }
        else
        {
            super.setThumb( thumb );
        }
    }
    
    
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.CustomSeekBar );
        
        // Read the title and set it if any
        thumbHeight = a.getDimensionPixelSize( R.styleable.CustomSeekBar_thumbHeight, 0 );
        thumbWidth = a.getDimensionPixelSize( R.styleable.CustomSeekBar_thumbWidth, 0 );
        
        setThumb( getThumb() );
        a.recycle();
    }
}
