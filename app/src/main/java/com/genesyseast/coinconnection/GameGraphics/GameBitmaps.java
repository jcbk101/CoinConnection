package com.genesyseast.coinconnection.GameGraphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Variables.PointXYZ;


public class GameBitmaps
{
    /**
     * //###############################
     * <p>
     * Split an image into usable
     * tile form
     * <p>
     * //###############################
     *
     * @param resID       N/A
     * @param gridRows    N/A
     * @param gridColumns N/A
     *
     * @return N/A
     */
    public static Bitmap[] splitImage( Context context, int resID, int gridRows, int gridColumns )
    {
        Bitmap[] bitmaps;
        Bitmap   loadBmp;
        int      width;
        int      height;
        int      index = 0;
        
        // Try to split the image
        try
        {
            loadBmp = BitmapFactory.decodeResource( context.getResources(), resID );
            height = (loadBmp.getHeight() / gridRows);
            width = (loadBmp.getWidth() / gridColumns);
            
            
            // Allocate space for the image split
            bitmaps = new Bitmap[ (gridRows * gridColumns) ];
            
            // Split the image into tile sized sections if possible
            for ( int y = 0; y < gridRows; y++ )
            {
                for ( int x = 0; x < gridColumns; x++ )
                {
                    bitmaps[ index++ ] = Bitmap.createBitmap( loadBmp, (x * width), (y * height), width, height );
                }
            }
            return bitmaps;
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        // Failed
        return null;
    }
    
    
    /**
     * //###############################
     * <p>
     * Return the drawable in Bitmap
     * form
     * <p>
     * //###############################
     *
     * @param resID N/A
     *
     * @return N/A
     */
    public static Bitmap getBitmapImage( Context context, int resID )
    {
        Bitmap loadBmp;
        int    width;
        int    height;
        
        try
        {
            loadBmp = BitmapFactory.decodeResource( context.getResources(), resID );
            height = loadBmp.getHeight();
            width = loadBmp.getWidth();
            
            return Bitmap.createBitmap( loadBmp, 0, 0, width, height );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        // Failed
        return null;
    }
    
    
    /**
     * //###############################
     * <p>
     * Change the size of a drawable
     * <p>
     * //###############################
     *
     * @param resID N/A
     *
     * @return N/A
     */
    public static Drawable resizeDrawable( Context context, int resID, int width, int height )
    {
        Bitmap loadBmp;
        
        try
        {
            loadBmp = BitmapFactory.decodeResource( context.getResources(), resID );
            
            // Scale it to your size
            return new BitmapDrawable( context.getResources(), Bitmap.createScaledBitmap( loadBmp, width, height, true ) );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        // Failed
        return null;
    }
    
    
    /**
     * //###############################
     * <p>
     * Convert a Drawable to a Bitmap
     * <p>
     * //###############################
     *
     * @param drawable
     *
     * @return
     */
    public static Bitmap drawableToBitmap( Drawable drawable, boolean needMutable )
    {
        Bitmap   bitmap = null;
        Drawable d;
        
        if ( drawable != null )
        {
            if ( drawable instanceof BitmapDrawable && !needMutable )
            {
                BitmapDrawable bitmapDrawable = ( BitmapDrawable ) drawable;
                if ( bitmapDrawable.getBitmap() != null )
                {
                    return bitmapDrawable.getBitmap();
                }
            }
            
            
            if ( drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0 )
            {
                d = drawable;
                bitmap = Bitmap.createBitmap( 1, 1, Bitmap.Config.ARGB_8888 ); // Single color bitmap will be created of 1x1 pixel
            }
            else
            {
                d = drawable;
                bitmap = Bitmap.createBitmap( drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
            }
            
            Canvas canvas = new Canvas( bitmap );
            d.setBounds( 0, 0, canvas.getWidth(), canvas.getHeight() );
            d.draw( canvas );
            
            return bitmap;
        }
        
        return null;
    }
    
    
    public static PointXYZ getImageSize( Context context, int resId )
    {
        Bitmap loadBmp;
        int    width;
        int    height;
        int    index = 0;
        
        // Try to split the image
        try
        {
            loadBmp = BitmapFactory.decodeResource( context.getResources(), resId );
            height = loadBmp.getHeight();
            width = loadBmp.getWidth();
            
            return new PointXYZ( width, height, 0 );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        
        // Failed
        return new PointXYZ( 0, 0, 0 );
    }
    
    
    /**
     * //###############################
     * <p>
     * Retrieve the resources from a
     * Typed Array list
     * <p>
     * //###############################
     *
     * @param context
     * @param resArrayId
     *
     * @return
     */
    public static int[] getResourceArray( Context context, int resArrayId )
    {
        TypedArray arrayList;
        int[]      array;
        
        //
        // Attributes
        //
        arrayList = context.getResources().obtainTypedArray( resArrayId );
        array = new int[ arrayList.length() ];
        
        //
        for ( int i = 0; i < arrayList.length(); i++ )
        {
            array[ i ] = arrayList.getResourceId( i, 0 );
        }
        
        // Apply the images
        arrayList.recycle();
        
        return array;
    }
}
