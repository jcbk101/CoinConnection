package com.genesyseast.coinconnection.Support;

/**
 * Created by devdeeds.com on 27/9/17.
 */

import android.content.Context;
import android.graphics.Bitmap;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;


public class BlurEffect
{
    
    private static final float BITMAP_SCALE = 0.4f;
    
    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static float BLUR_RADIUS = 10.5f;
    
    public Bitmap blur( Context context, Bitmap image, float blurRadius )
    {
        
        Bitmap outputBitmap = null;
        
        if ( image != null )
        {
            
            if ( blurRadius == 0 )
            {
                return image;
            }
            
            if ( blurRadius < 1 )
            {
                blurRadius = 1;
            }
            
            if ( blurRadius > 25 )
            {
                blurRadius = 25;
            }
            
            BLUR_RADIUS = blurRadius;
            
            int width  = Math.round( image.getWidth() * BITMAP_SCALE );
            int height = Math.round( image.getHeight() * BITMAP_SCALE );
            
            Bitmap inputBitmap = Bitmap.createScaledBitmap( image, width, height, false );
            outputBitmap = Bitmap.createBitmap( inputBitmap );
            
            RenderScript        rs           = RenderScript.create( context );
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
            Allocation          tmpIn        = Allocation.createFromBitmap( rs, inputBitmap );
            Allocation          tmpOut       = Allocation.createFromBitmap( rs, outputBitmap );
            theIntrinsic.setRadius( BLUR_RADIUS );
            theIntrinsic.setInput( tmpIn );
            theIntrinsic.forEach( tmpOut );
            tmpOut.copyTo( outputBitmap );
            
            rs.destroy();
            tmpIn.destroy();
            tmpOut.destroy();
            inputBitmap.recycle();
            
            rs = null;
            tmpIn = null;
            tmpOut = null;
            inputBitmap = null;
        }
        
        return outputBitmap;
    }
    
    
    public static Bitmap createBlur( Context context, Bitmap image, float blurRadius )
    {
        
        Bitmap outputBitmap = null;
        
        if ( image != null )
        {
            
            if ( blurRadius == 0 )
            {
                return image;
            }
            
            if ( blurRadius < 1 )
            {
                blurRadius = 1;
            }
            
            if ( blurRadius > 25 )
            {
                blurRadius = 25;
            }
            
            BLUR_RADIUS = blurRadius;
            
            int width  = Math.round( image.getWidth() * BITMAP_SCALE );
            int height = Math.round( image.getHeight() * BITMAP_SCALE );
            
            Bitmap inputBitmap = Bitmap.createScaledBitmap( image, width, height, false );
            outputBitmap = Bitmap.createBitmap( inputBitmap );
            
            RenderScript        rs           = RenderScript.create( context );
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
            Allocation          tmpIn        = Allocation.createFromBitmap( rs, inputBitmap );
            Allocation          tmpOut       = Allocation.createFromBitmap( rs, outputBitmap );
            theIntrinsic.setRadius( BLUR_RADIUS );
            theIntrinsic.setInput( tmpIn );
            theIntrinsic.forEach( tmpOut );
            tmpOut.copyTo( outputBitmap );
            
            rs.destroy();
            tmpIn.destroy();
            tmpOut.destroy();
            inputBitmap.recycle();
            
            rs = null;
            tmpIn = null;
            tmpOut = null;
            inputBitmap = null;
        }
        
        return outputBitmap;
    }
    
}