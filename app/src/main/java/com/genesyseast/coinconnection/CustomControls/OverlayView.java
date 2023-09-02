package com.genesyseast.coinconnection.CustomControls;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.R;

import java.util.ArrayList;

public class OverlayView
        extends FrameLayout
{
    //    private Bitmap                newBg;
    //    private int[][]               mapData;
    //    private ImageView             backGroundHolder;
    //    private ConnectionsGridLayout gridHolder;
    private boolean isLocal = false;
    
    
    public OverlayView( Context context )
    {
        super( context );
    }
    
    public OverlayView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
    }
    
    
/*
    public void setBackGroundHolder( ImageView backGroundHolder )
    {
        this.backGroundHolder = backGroundHolder;
    }
    
    public void setGridHolder( ConnectionsGridLayout gridHolder )
    {
        this.gridHolder = gridHolder;
    }
    
    public void setMapData( int[][] mapData )
    {
        this.mapData = mapData;
    }
*/
    
    @Override
    public void setBackgroundResource( int resid )
    {
        //        super.setBackgroundResource( resid );
        //        setBackground( ResourcesCompat.getDrawable( getResources(), resid, null ) );
    }
    
    @Override
    public void setBackground( Drawable background )
    {
        // Simply to prevent outside changes
        if ( !isLocal )
        {
            return;
        }
        
        isLocal = false;
        super.setBackground( background );
    }
    
    
    /**
     * //#############################
     * <p>
     * Create an illusion for tiles
     * dropping through a portal
     * <p>
     * //#############################
     *
     * @param backGroundHolder
     * @param gridHolder
     */
    //    public void createMaskImage( ImageView backGroundHolder, ConnectionsGridLayout gridHolder, int[][] mapData )
    public void createMaskImage( ImageView backGroundHolder, ConnectionsGridLayout gridHolder, ArrayList<BoardTile> boardTiles )
    {
        //        if ( backGroundHolder != null && gridHolder != null && mapData != null )
        if ( backGroundHolder != null && gridHolder != null && boardTiles != null )
        {
            Bitmap newBg;
            Bitmap backTemp;
            Bitmap grid;
            Canvas backCanvas;
            Canvas newBgCanvas;
            Canvas gridCanvas;
            Rect   dst = new Rect();
            Rect   src = new Rect();
            
            //
            // Size of each tile to copy over
            //
            int h = gridHolder.getHeight() / gridHolder.getRowCount();
            int w = gridHolder.getWidth() / gridHolder.getColumnCount();
            
            // Create a TEMP background bitmap to use for BG tile copying
            backTemp = Bitmap.createBitmap( backGroundHolder.getWidth(), backGroundHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            // This will be the FINAL new BG
            newBg = Bitmap.createBitmap( backGroundHolder.getWidth(), backGroundHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            // Create a TEMP gridLayout bitmap to use for BG tile copying
            grid = Bitmap.createBitmap( gridHolder.getWidth(), gridHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            //
            // Assign a canvas for each bitmap
            //
            backCanvas = new Canvas( backTemp );
            newBgCanvas = new Canvas( newBg );
            gridCanvas = new Canvas( grid );
            
            
            gridHolder.draw( gridCanvas );
            backGroundHolder.draw( backCanvas );
            
            // Copy all content from the top
            // of BG down to the top of the grid
            newBgCanvas.drawBitmap( backTemp, new Rect( 0, 0, backGroundHolder.getWidth(), gridHolder.getTop() - (h / 5) ),
                                    new Rect( 0, 0, backGroundHolder.getWidth(), gridHolder.getTop() - (h / 5) ), null
                                  );
            
            
            //#################################
            //
            // Find any blank spots and copy
            // the Game's BG to that spot
            // Retaining the Board data as well
            //
            //#################################
/*
            for ( int y = 0; y < gridHolder.getRowCount(); y++ )
            {
                for ( int x = 0; x < gridHolder.getColumnCount(); x++ )
                {
                    BoardTile tile = boardTiles.get( y * gridHolder.getColumnCount() + x );
                    
                    // Always free this memory
                    tile.overlayImage = null;
                    
                    // Using boardTiles
                    // -1 = INACTIVE
                    if ( tile.tileNum == -1 )
                    {
                        // Create the new image
                        tile.overlayImage = new BitmapDrawable( getResources(), Bitmap.createBitmap( tileSize, tileSize, Bitmap.Config.ARGB_8888 ) );
                        //                        tile.overlayImage.setBounds( 0, 0, tileSize, tileSize );
                        Canvas tileCanvas = new Canvas( tile.overlayImage.getBitmap() );
                        
                        // Copy the BG tile data to newBG
                        dst.left = 0;
                        dst.top = 0;
                        dst.right = tileSize;
                        dst.bottom = tileSize;
                        
                        // Copy the BG tile data to newBG
                        src.left = gridHolder.getLeft() + (x * w);
                        src.top = gridHolder.getTop() + (y * h);
                        src.right = gridHolder.getLeft() + (x * w) + w;
                        src.bottom = gridHolder.getTop() + (y * h) + h;
                        // The Background picture's tile
                        tileCanvas.drawBitmap( backTemp, src, dst, null );
                        
                        // The Foreground uses the current Grid Image
                        src.left = (x * w);
                        src.top = (y * h);
                        src.right = (x * w) + w;
                        src.bottom = (y * h) + h;
                        // The grid's tile
                        tileCanvas.drawBitmap( grid, src, dst, null );
                        tileCanvas.drawBitmap( grid, src, dst, null );
                        tile.setZ( 10 );
                        
                        //
                        tileCanvas = null;
                    }
                }
            }
*/

/*
            for ( int y = 0; y < gridHolder.getRowCount(); y++ )
            {
                for ( int x = 0; x < gridHolder.getColumnCount(); x++ )
                {
                    // Using gameboard.boardMap[][]
                    // -1 = INACTIVE
                    if ( mapData[ y ][ x ] == -1 )
                    {
                        // Copy the BG tile data to newBG
                        dst.left = (x * w) + padding;
                        dst.top = (y * h) + padding;
                        dst.right = (x * w) + w - padding;
                        dst.bottom = (y * h) + h - padding;
                        
                        // Copy the BG tile data to newBG
                        src.left = gridHolder.getLeft() + (x * w) + padding;
                        src.top = gridHolder.getTop() + (y * h) + padding;
                        src.right = gridHolder.getLeft() + (x * w) + w - padding;
                        src.bottom = gridHolder.getTop() + (y * h) + h - padding;
                        
                        // The Background picture's tile
                        newBgCanvas.drawBitmap( backTemp, src, src, null );
                        
                        // The Foreground uses the current Grid Image
                        newBgCanvas.drawBitmap( grid, dst, src, null );
                    }
                }
            }
*/
            
            //################################
            //
            // save the Overlay Masking BG
            //
            //################################
            isLocal = true;
            setBackground( new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( newBg, getWidth(), getHeight(), true ) ) );
            
            // Clean up for Garbage collector
            newBg.recycle();
            grid.recycle();
            backTemp.recycle();
            gridCanvas = null;
            backCanvas = null;
        }
    }

    
    /*
    @Override
    public void setBackground( Drawable background )
    {
        super.setBackground( background );
        
        if ( backGroundHolder != null && gridHolder != null )
        {
            Bitmap backTemp;
            Bitmap grid;
            Canvas backCanvas;
            Canvas newBgCanvas;
            Canvas gridCanvas;
            Rect   dst = new Rect();
            Rect   src = new Rect();
            
            //
            // Size of each tile to copy over
            //
            int h = gridHolder.getHeight() / gridHolder.getRowCount();
            int w = gridHolder.getWidth() / gridHolder.getColumnCount();
            
            
            // Create a TEMP background bitmap to use for BG tile copying
            backTemp = Bitmap.createBitmap( backGroundHolder.getWidth(), backGroundHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            // This will be the FINAL new BG
            newBg = Bitmap.createBitmap( backGroundHolder.getWidth(), backGroundHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            // Create a TEMP gridLayout bitmap to use for BG tile copying
            grid = Bitmap.createBitmap( gridHolder.getWidth(), gridHolder.getHeight(), Bitmap.Config.ARGB_8888 );
            
            //
            // Assign a canvas for each bitmap
            //
            newBgCanvas = new Canvas( newBg );
            backCanvas = new Canvas( backTemp );
            gridCanvas = new Canvas( grid );
            
            
            gridHolder.draw( gridCanvas );
            backGroundHolder.draw( backCanvas );
            
            // Copy all content from the top
            // of BG down to the top of the grid
            newBgCanvas.drawBitmap( backTemp, new Rect( 0, 0, backGroundHolder.getWidth(), gridHolder.getTop() ), new Rect( 0, 0, backGroundHolder.getWidth(), gridHolder.getTop() ), null );
            
            
            //#################################
            //
            // Find any blank spots and copy
            // the Game's BG to that spot
            // Retaining the Board data as well
            //
            //#################################
            for ( int y = 0; y < gridHolder.getRowCount(); y++ )
            {
                for ( int x = 0; x < gridHolder.getColumnCount(); x++ )
                {
                    if ( mapData[ y ][ x ] == 0 )
                    {
                        // Copy the BG tile data to newBG
                        dst.left = (x * w);
                        dst.top = (y * h);
                        dst.right = (x * w) + w;
                        dst.bottom = (y * h) + h;
                        
                        // Copy the BG tile data to newBG
                        src.left = gridHolder.getLeft() + (x * w);
                        src.top = gridHolder.getTop() + (y * h);
                        src.right = gridHolder.getLeft() + (x * w) + w;
                        src.bottom = gridHolder.getTop() + (y * h) + h;
                        
                        // The Background picture's tile
                        newBgCanvas.drawBitmap( backTemp, src, src, null );
                        
                        // The Foreground uses the current Grid Image
                        newBgCanvas.drawBitmap( grid, dst, src, null );
                    }
                }
            }
            
            //################################
            //
            // save the Overlay Masking BG
            //
            //################################
            super.setBackground( new BitmapDrawable( getResources(), Bitmap.createScaledBitmap( newBg, getWidth(), getHeight(), true ) ) );
            
            // Clean up for Garbage collector
            grid.recycle();
            backTemp.recycle();
            gridCanvas = null;
            backCanvas = null;
        }
    }
*/
    
    
    public void clearMask()
    {
        isLocal = true;
        setBackground( null );
    }
    
    
    @Override
    protected void onDetachedFromWindow()
    {
        isLocal = true;
        setBackground( null );
        super.onDetachedFromWindow();
    }
}
