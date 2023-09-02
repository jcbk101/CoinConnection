package com.genesyseast.coinconnection.CustomControls;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.BlurEffect;


public class ImageEffect
        extends AppCompatImageView
{
    
    private int    imageEffect = 0;
    private float  blur_radius = 0;
    private Bitmap bitmap;
    private int imageResource;
    
    private int color;
    private int layer_used;
    
    /**
     * constructors
     *
     * @param context N/A
     */
    public ImageEffect( Context context )
    {
        super( context );
    }
    
    public ImageEffect( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
    }
    
    public ImageEffect( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        readAttr( context, attrs );
    }
    
    
    /**
     * //###############################
     * //
     * // Set effect to Image
     * //
     * //###############################
     */
    private void setEffect( int effect )
    {
        imageEffect = effect;
        
        if ( getDrawable() != null && effect == 1 )
        {
            bitmap = null;
            bitmap = (( BitmapDrawable ) getDrawable()).getBitmap();
            setImageBitmap( new BlurEffect().blur( getContext(), bitmap, blur_radius ) );
        }
    }
    
    
    @Override
    public void setImageDrawable( @Nullable Drawable drawable )
    {
        super.setImageDrawable( drawable );
//        setEffect( imageEffect );
    }
    
    
    @Override
    public void setImageResource( int resId )
    {
        super.setImageResource( resId );
        
        imageResource = resId;
        setEffect( imageEffect );
    }
    
    
    public int getImageEffect()
    {
        return imageEffect;
    }
    
    public void setImageEffect( int imageEffect )
    {
        this.imageEffect = imageEffect;
        setEffect( imageEffect );
    }
    
    public float getBlur_radius()
    {
        return blur_radius;
    }
    
    public void setBlur_radius( float blur_radius )
    {
        this.blur_radius = blur_radius;
        setEffect( imageEffect );
    }
    
    public int getColor()
    {
        return color;
    }
    
    
    
    /**
     * //###############################
     * //
     * // Load custom attributes
     * //
     * //###############################
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ImageEffect );
        
        
        //
        if ( a.hasValue( R.styleable.ImageEffect_srcCompat ) )
        {
            imageResource = a.getResourceId( R.styleable.ImageEffect_srcCompat, 0 );
            setImageResource( imageResource );
        }
        
        if ( a.hasValue( R.styleable.ImageEffect_color ) )
        {
            
/*
            DisplayMetrics displayMetrics = new DisplayMetrics();
            (( Activity ) getContext()).getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
            int height = displayMetrics.heightPixels;
            int width  = displayMetrics.widthPixels;
*/
            
            color = a.getInt( R.styleable.ImageEffect_color, 0 );
            
            try
            {
                Bitmap bitmap = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                
                Canvas canvas = new Canvas( bitmap );
                canvas.drawColor( color );
                Drawable newDrawable = new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( bitmap, getWidth(),getHeight(), true ) );
                //
                setImageDrawable( newDrawable );
            }
            catch ( Exception ex )
            {
                ex.printStackTrace();
            }
        }
        
        if ( a.hasValue( R.styleable.ImageEffect_layer ) )
        {
            layer_used = a.getResourceId( R.styleable.ImageEffect_layer, 0 );
        }
        
        
        if ( a.hasValue( R.styleable.ImageEffect_effect ) )
        {
            int effect = a.getInt( R.styleable.ImageEffect_effect, 0 );
            
            if ( effect == 1 && a.hasValue( R.styleable.ImageEffect_blur_radius ) )
            {
                blur_radius = a.getFloat( R.styleable.ImageEffect_blur_radius, 0.0f );
                setEffect( effect );
            }
        }
        
        //
        a.recycle();
    }
    
    
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        
        if ( bitmap != null )
        {
            bitmap.recycle();
        }
        
        bitmap = null;
    }
}
