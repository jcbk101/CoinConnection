package com.genesyseast.coinconnection.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.R;

import java.util.ArrayList;

public class GridLayoutView
        extends FrameLayout
{
    public static class TileMapper
    {
        public static final int OUTER_CORNER          = 0;
        public static final int INNER_CORNER          = 1;
        public static final int HORIZONTAL_OUTER_WALL = 2;
        public static final int VERTICAL_OUTER_WALL   = 3;
        public static final int FLIP_NONE             = 0;
        public static final int FLIP_X                = 1;
        public static final int FLIP_Y                = 2;
        public static final int FLIP_XY               = 3;
        public              int ID, FlipXY, Rotation;
        public int xPosi, yPosi;
        
        public TileMapper( int ID, int FlipXY, int Rotation )
        {
            this.ID = ID;
            this.FlipXY = FlipXY;
            this.Rotation = Rotation;
        }
        
        public TileMapper()
        {
        }
    }
    
    private ArrayList<BoardTile> boardTiles;
    boolean canDraw = false;
    public       Bitmap  loadBmp;
    public       Canvas  bmpCanvas;
    public       int[][] gridData;
    public       int[][] xyGrid;
    private      int     rowCount;
    private      int     columnCount;
    private      int     adjust;
    //
    final public int     TOP    = 1;
    final public int     RIGHT  = 2;
    final public int     BOTTOM = 4;
    final public int     LEFT   = 8;
    final public int     TL     = 16;
    final public int     TR     = 32;
    final public int     BR     = 64;
    final public int     BL     = 128;
    
    
    public void setXyGrid( int[][] xyGrid )
    {
        this.xyGrid = xyGrid;
        this.gridData = new int[ xyGrid.length ][ xyGrid[ 0 ].length ];
    }
    
    public int getRowCount()
    {
        return rowCount;
    }
    
    public void setRowCount( int rowCount )
    {
        this.rowCount = rowCount;
    }
    
    public int getColumnCount()
    {
        return columnCount;
    }
    
    public void setColumnCount( int columnCount )
    {
        this.columnCount = columnCount;
    }
    
    public void setBoardTiles( ArrayList<BoardTile> boardTiles )
    {
        this.boardTiles = boardTiles;
    }
    
    
    public GridLayoutView( Context context )
    {
        super( context );
    }
    
    public GridLayoutView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
    }
    
    
    /**
     * //#######################################
     * //
     * // Construct the game board grid
     * //
     * //#######################################
     *
     * @param grid
     */
    public void createBoardImage( ConnectionsGridLayout grid )
    {
        if ( loadBmp == null && bmpCanvas == null )
        {
            getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
            {
                @Override
                public void onGlobalLayout()
                {
                    // Kill the listener
                    getViewTreeObserver().removeOnGlobalLayoutListener( this );
                    
                    if ( boardTiles != null && boardTiles.size() != 0 )
                    {
                        canDraw = true;
                        
                        // Copy of this Grid canvas
                        loadBmp = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                        bmpCanvas = new Canvas( loadBmp );
                        
                        // Tile size for shifting
                        adjust = getWidth() / (grid.getColumnCount() + 2);
                        
                        // Build the map
                        buildBoardMap();
                        processGridLayout();
                        //                        invalidate();
                    }
                }
            } );
        }
        else
        {
            if ( boardTiles != null && boardTiles.size() != 0 )
            {
                canDraw = true;
                
                // Copy of this Grid canvas
                loadBmp = null;
                bmpCanvas = null;
                
                loadBmp = Bitmap.createBitmap( getWidth(), getHeight(), Bitmap.Config.ARGB_8888 );
                bmpCanvas = new Canvas( loadBmp );
                
                // Tile size for shifting
                adjust = getWidth() / (grid.getColumnCount() + 2);
                
                // Build the map
                buildBoardMap();
                processGridLayout();
                //                invalidate();
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Set the New BG in place
     * <p>
     * //###############################
     */
    private void setImageBG()
    {
        setBackground( new BitmapDrawable( getResources(), loadBmp ) );
    }
    
    
    /**
     * //#######################################
     * //
     * // Build the board grid
     * //
     * //#######################################
     */
    private void buildBoardMap()
    {
        // Build tiles
        for ( int y = 0; y < getRowCount(); y++ )
        {
            for ( int x = 0; x < getColumnCount(); x++ )
            {
                gridData[ y ][ x ] = xyGrid[ y ][ x ] - 1;
            }
        }
        
        // Add them to the grid array in modified form
        for ( int y = 0; y < getRowCount(); y++ )
        {
            for ( int x = 0; x < getColumnCount(); x++ )
            {
                addTileToMap( gridData, x, y, gridData[ y ][ x ] );
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Run the drawing process
     * <p>
     * //###############################
     */
    private void processGridLayout()
    {
        // Place the ceneter tiles and clear blank tiles
        for ( int y = 0; y < getRowCount(); y++ )
        {
            for ( int x = 0; x < getColumnCount(); x++ )
            {
                putTile( bmpCanvas, x, y );
            }
        }
        
        
        // Add the corners where needed
        for ( int y = 0; y < getRowCount(); y++ )
        {
            for ( int x = 0; x < getColumnCount(); x++ )
            {
                addCorners( gridData, x, y, gridData[ y ][ x ] );
            }
        }
        
        
        // Add the corners where needed
        for ( int y = 0; y < getRowCount(); y++ )
        {
            for ( int x = 0; x < getColumnCount(); x++ )
            {
                addWalls( gridData, x, y );
            }
        }
        
        
        setImageBG();
    }
    
    
    /**
     * //######################################
     * //
     * // Method does a full modification to
     * // add corners where needed
     * //
     * //######################################
     *
     * @param grid
     * @param xPosi
     * @param yPosi
     * @param tile_ID
     */
    public void addCorners( int[][] grid, int xPosi, int yPosi, int tile_ID )
    {
        // If this is NOT a blank spot, leave
        // ONLY test blank locations
        // Defaults to the main tile
        //  Blank tile is -1
        int walls_needed   = 0;
        int left           = 0x10000;
        int top            = 0x10000;
        int right          = 0x10000;
        int bottom         = 0x10000;
        int corners_placed = 0;
        //
        
        
        //###############################################
        //
        // Blank ONLY, Use inside walls and corners!
        //
        //###############################################
        if ( tile_ID < 0 )
        {
            // Top
            if ( (yPosi - 1) > -1 )
            {
                top = grid[ yPosi - 1 ][ xPosi ];
                
                // If this is a non blank tile, clear the wall
                if ( top > -1 && (top & BOTTOM) == 0 )
                {
                    walls_needed |= TOP;
                    
                    // Set the wall bit for the blank tile otherwise
                    // Needed for walls next to unoccupied blanks
                    grid[ yPosi ][ xPosi ] &= ~TOP;
                    grid[ yPosi - 1 ][ xPosi ] |= BOTTOM;
                }
            }
            
            
            // Left
            if ( (xPosi - 1) > -1 )
            {
                left = grid[ yPosi ][ xPosi - 1 ];
                
                if ( left > -1 && (left & RIGHT) == 0 )
                {
                    walls_needed |= LEFT;
                    
                    // Set the wall bit for the blank tile otherwise
                    // Needed for walls next to unoccupied blanks
                    grid[ yPosi ][ xPosi ] &= ~LEFT;
                    grid[ yPosi ][ xPosi - 1 ] |= RIGHT;
                }
            }
            
            
            // Bottom
            if ( (yPosi + 1) < 7 )
            {
                bottom = grid[ yPosi + 1 ][ xPosi ];
                
                if ( bottom > -1 && (bottom & TOP) == 0 )
                {
                    walls_needed |= BOTTOM;
                    
                    // Set the wall bit for the blank tile otherwise
                    // Needed for walls next to unoccupied blanks
                    grid[ yPosi ][ xPosi ] &= ~BOTTOM;
                    grid[ yPosi + 1 ][ xPosi ] |= TOP;
                }
            }
            
            
            // Right
            if ( (xPosi + 1) < 7 )
            {
                right = grid[ yPosi ][ xPosi + 1 ];
                
                if ( right > -1 && (right & LEFT) == 0 )
                {
                    walls_needed |= RIGHT;
                    
                    // Set the wall bit for the blank tile otherwise
                    // Needed for walls next to unoccupied blanks
                    grid[ yPosi ][ xPosi ] &= ~RIGHT;
                    grid[ yPosi ][ xPosi + 1 ] |= LEFT;
                }
            }
            
            
            //#################################
            //
            // Top left corner
            //
            //#################################
            if ( (walls_needed & (TOP | LEFT)) == (TOP | LEFT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, TileMapper.INNER_CORNER, R.drawable.inner_corner );
                corners_placed |= TL;
            }
            
            // Top right corner
            if ( (walls_needed & (TOP | RIGHT)) == (TOP | RIGHT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, TileMapper.INNER_CORNER, R.drawable.inner_corner );
                corners_placed |= TR;
            }
            
            // Bottom left corner
            if ( (walls_needed & (BOTTOM | LEFT)) == (BOTTOM | LEFT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, TileMapper.INNER_CORNER, R.drawable.inner_corner );
                corners_placed |= BL;
            }
            
            // Bottom right corner
            if ( (walls_needed & (BOTTOM | RIGHT)) == (BOTTOM | RIGHT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, TileMapper.INNER_CORNER, R.drawable.inner_corner );
                corners_placed |= BR;
            }
            
            // Set the corner bits that are occupied
            // Nothing is cleared if no corner was placed
            if ( corners_placed > 0 )
            {
                grid[ yPosi ][ xPosi ] &= ~(corners_placed);
            }
            else
            {
                if ( top != 0x10000 )
                {
                    grid[ yPosi - 1 ][ xPosi ] = top;
                }
                if ( left != 0x10000 )
                {
                    grid[ yPosi ][ xPosi - 1 ] = left;
                }
                if ( right != 0x10000 )
                {
                    grid[ yPosi ][ xPosi + 1 ] = right;
                }
                if ( bottom != 0x10000 )
                {
                    grid[ yPosi + 1 ][ xPosi ] = bottom;
                }
            }
            
            //
            //            addWalls( xPosi, yPosi, walls_needed, corners_placed, 1 );
        }
        else
        {
            // Outside corners
            
            // Top
            if ( (yPosi - 1) > -1 )
            {
                top = grid[ yPosi - 1 ][ xPosi ];
                
                if ( top < 0 )
                {
                    // Blank spot above
                    walls_needed |= TOP;
                }
            }
            else
            {
                // Default when can't move up
                walls_needed |= TOP;
            }
            
            
            // Left
            if ( (xPosi - 1) > -1 )
            {
                left = grid[ yPosi ][ xPosi - 1 ];
                
                if ( left < 0 )
                {
                    walls_needed |= LEFT;
                }
            }
            else
            {
                // Default when can't move left
                walls_needed |= LEFT;
            }
            
            
            // Bottom
            if ( (yPosi + 1) < 7 )
            {
                bottom = grid[ yPosi + 1 ][ xPosi ];
                
                if ( bottom < 0 )
                {
                    walls_needed |= BOTTOM;
                }
            }
            else
            {
                walls_needed |= BOTTOM;
            }
            
            
            // Right
            if ( (xPosi + 1) < 7 )
            {
                right = grid[ yPosi ][ xPosi + 1 ];
                
                if ( right < 0 )
                {
                    walls_needed |= RIGHT;
                }
            }
            else
            {
                walls_needed |= RIGHT;
            }
            
            
            // Top left corner
            if ( (walls_needed & (TOP | LEFT)) == (TOP | LEFT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, TileMapper.OUTER_CORNER, R.drawable.outer_corner );
                corners_placed |= TL;
            }
            
            // Top right corner
            if ( (walls_needed & (TOP | RIGHT)) == (TOP | RIGHT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, TileMapper.OUTER_CORNER, R.drawable.outer_corner );
                corners_placed |= TR;
            }
            
            // Bottom left corner
            if ( (walls_needed & (BOTTOM | LEFT)) == (BOTTOM | LEFT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, TileMapper.OUTER_CORNER, R.drawable.outer_corner );
                corners_placed |= BL;
            }
            
            // Bottom right corner
            if ( (walls_needed & (BOTTOM | RIGHT)) == (BOTTOM | RIGHT) )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, TileMapper.OUTER_CORNER, R.drawable.outer_corner );
                corners_placed |= BR;
            }
            
            
            // Set the corner bits that are occupied
            // Nothing is cleared if no corner was placed
            grid[ yPosi ][ xPosi ] |= (corners_placed);
            
        }
        // If it's blank, do your thing
        //        if ( tile_ID == -1 )
        
        {
/*
            // Top-Left check: Need Top tile and Left tile
            if ( (yPosi - 1) > -1 && (xPosi - 1) > -1 )
            {
                left_right_tile = grid[ yPosi ][ xPosi - 1 ];
                top_bottom_tile = grid[ yPosi - 1 ][ xPosi ];
                
                // Need wall on the right of the left side tile
                if ( left_right_tile > -1 && (left_right_tile & RIGHT) == 0 )
                {
                    walls_needed |= LEFT;
                }
                
                // Need wall on bottom of the Top tile
                if ( top_bottom_tile > -1 && (top_bottom_tile & BOTTOM) == 0 )
                {
                    walls_needed |= TOP;
                }
                
                
                if ( left_right_tile > -1 && top_bottom_tile > -1 )
                {
                    // These walls NOT set means corner needed
                    if ( (left_right_tile & RIGHT) == 0 && (top_bottom_tile & BOTTOM) == 0 )
                    {
                        putCorner( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1 );
                    }
                }
            }
            
            
            // Top-Right check: Need Top tile and Left tile
            if ( (yPosi - 1) > -1 && (xPosi + 1) < 7 )
            {
                left_right_tile = grid[ yPosi ][ xPosi + 1 ];
                top_bottom_tile = grid[ yPosi - 1 ][ xPosi ];
                
                // Need wall on the left of the right side tile
                if ( left_right_tile > -1 && (left_right_tile & LEFT) == 0 )
                {
                    walls_needed |= RIGHT;
                }
                
                // Need wall on bottom of the Top tile
                if ( top_bottom_tile > -1 && (top_bottom_tile & BOTTOM) == 0 )
                {
                    walls_needed |= TOP;
                }
                
                
                if ( left_right_tile > -1 && top_bottom_tile > -1 )
                {
                    
                    // These walls NOT set means corner needed
                    if ( (left_right_tile & LEFT) == 0 && (top_bottom_tile & BOTTOM) == 0 )
                    {
                        putCorner( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1 );
                    }
                }
            }
            
            
            // Bottom-Right check: Need Bottom tile and Right tile
            if ( (yPosi + 1) < 7 && (xPosi + 1) < 7 )
            {
                left_right_tile = grid[ yPosi ][ xPosi + 1 ];
                top_bottom_tile = grid[ yPosi + 1 ][ xPosi ];
                
                
                // Need wall on the left of the right side tile
                if ( left_right_tile > -1 && (left_right_tile & LEFT) == 0 )
                {
                    walls_needed |= RIGHT;
                }
                
                // Need wall on top of the bottom tile
                if ( top_bottom_tile > -1 && (top_bottom_tile & TOP) == 0 )
                {
                    walls_needed |= BOTTOM;
                }
                
                if ( left_right_tile > -1 && top_bottom_tile > -1 )
                {
                    
                    // These walls NOT set means corner needed
                    if ( (left_right_tile & BOTTOM) == 0 && (top_bottom_tile & RIGHT) == 0 )
                    {
                        putCorner( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, 1 );
                    }
                }
            }
            
            
            // Bottom-Left check: Need Bottom tile and Left tile
            if ( (yPosi + 1) < 7 && (xPosi - 1) > -1 )
            {
                left_right_tile = grid[ yPosi ][ xPosi - 1 ];
                top_bottom_tile = grid[ yPosi + 1 ][ xPosi ];
                
                // Need wall on the right of the left side tile
                if ( left_right_tile > -1 && (left_right_tile & RIGHT) == 0 )
                {
                    walls_needed |= LEFT;
                }
                
                // Need wall on top of the bottom tile
                if ( top_bottom_tile > -1 && (top_bottom_tile & TOP) == 0 )
                {
                    walls_needed |= BOTTOM;
                }
                
                if ( left_right_tile > -1 && top_bottom_tile > -1 )
                {
                    // These walls NOT set means corner needed
                    if ( (left_right_tile & BOTTOM) == 0 && (top_bottom_tile & LEFT) == 0 )
                    {
                        putCorner( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1 );
                    }
                }
            }
*/
        
        }
        
    }
    
    
    /**
     * //#####################################
     * //
     * //
     * //
     * //#####################################
     *
     * @param gridData
     * @param xPosi
     * @param yPosi
     */
    private void addWalls( int[][] gridData, int xPosi, int yPosi )
    {
        //
        /// Get the proper walls
        //
        int walls_needed = gridData[ yPosi ][ xPosi ];
        
        
        //################################
        //
        // Using Clear Space tile
        //
        //################################
        if ( walls_needed < 0 )
        {
            walls_needed ^= -1;
            
            // Use inner walls
            // Only bits we are testing
            switch ( walls_needed & (TL | TR | BL | BR) )
            {
                // Only corner place, needs 2 inner to outer
                // Top: Normal hori
                // Left: Normal vert
                case TL://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case TR://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case BR://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case BL://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case TL | TR://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_multi_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_single_vert );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case BL | BR://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1, R.drawable.inner_wall_multi_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1, R.drawable.inner_wall_single_vert );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, 1, R.drawable.inner_wall_single_vert );
                    break;
                
                case TL | BL://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_wall_multi_vert );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, 1, R.drawable.inner_wall_single_hori );
                    break;
                
                case TR | BR://
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1, R.drawable.inner_wall_single_hori );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, 1, R.drawable.inner_wall_multi_vert );
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_XY, 1, R.drawable.inner_wall_single_hori );
                    break;
                
                // All corners
                case BL | TL | TR | BR:// All inner
                    putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, 1, R.drawable.inner_center );
                    break;
                
                default:
            }
        }
        else
        {
            // Outside walls
            // Top full wall
            if ( (walls_needed & TOP) == 0 )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, TileMapper.HORIZONTAL_OUTER_WALL, R.drawable.outer_wall_hori );
            }
            
            // Left full wall
            if ( (walls_needed & LEFT) == 0 )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_NONE, TileMapper.VERTICAL_OUTER_WALL, R.drawable.outer_wall_vert );
            }
            
            
            // Right full wall
            if ( (walls_needed & RIGHT) == 0 )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_X, TileMapper.VERTICAL_OUTER_WALL, R.drawable.outer_wall_vert );
            }
            
            
            // Bottom full wall
            if ( (walls_needed & BOTTOM) == 0 )
            {
                putCornerOrWall( bmpCanvas, xPosi, yPosi, TileMapper.FLIP_Y, TileMapper.HORIZONTAL_OUTER_WALL, R.drawable.outer_wall_hori );
            }
        }
        
    }
    
    
    /**
     * //######################################
     * //
     * // Method does a 3x3 modification
     * //
     * //######################################
     *
     * @param grid
     * @param xPosi
     * @param yPosi
     * @param tile_ID
     *
     * @return
     */
    private void addTileToMap( int[][] grid, int xPosi, int yPosi, int tile_ID )
    {
        int TOP    = 1;
        int RIGHT  = 2;
        int BOTTOM = 4;
        int LEFT   = 8;
        // Defaults to the main tile
        //  Blank tile is -1
        int newTile = 0;
        
        
        //
        if ( tile_ID < 0 )
        {
            // Blank space: Add walls where needed
            // Top Wall, Good test: Up
            if ( (yPosi - 1) > -1 )
            {
                // Change Upper tile by removing value of bottom BIT
                grid[ yPosi - 1 ][ xPosi ] &= ~BOTTOM;
            }
            
            // Right Wall, Good test: Right
            if ( (xPosi + 1) < 7 )
            {
                // Change Right tile by removing value of left BIT
                grid[ yPosi ][ xPosi + 1 ] &= ~LEFT;
            }
            
            // Bottom Wall, Good test: Down
            if ( (yPosi + 1) < 7 )
            {
                // Change Bottom tile by removing value of upper BIT
                grid[ yPosi + 1 ][ xPosi ] &= ~TOP;
            }
            
            // Left Wall, Good test: Left
            if ( (xPosi - 1) > -1 )
            {
                // Change Left tile by removing value of right BIT
                grid[ yPosi ][ xPosi - 1 ] &= ~RIGHT;
            }
            
            // Save the newly built tile
            grid[ yPosi ][ xPosi ] = -1;
        }
        else
        {
            // Top Wall, Good test: Up
            if ( (yPosi - 1) > -1 && grid[ yPosi - 1 ][ xPosi ] > -1 )
            {
                // Change Upper tile by adding value of bottom BIT
                grid[ yPosi - 1 ][ xPosi ] |= BOTTOM;
                newTile |= TOP;
            }
            
            // Right Wall, Good test: Right
            if ( (xPosi + 1) < 7 && grid[ yPosi ][ xPosi + 1 ] > -1 )
            {
                // Change Right tile by adding value of left BIT
                grid[ yPosi ][ xPosi + 1 ] |= LEFT;
                newTile |= RIGHT;
            }
            
            // Bottom Wall, Good test: Down
            if ( (yPosi + 1) < 7 && grid[ yPosi + 1 ][ xPosi ] > -1 )
            {
                // Change Bottom tile by adding value of upper BIT
                grid[ yPosi + 1 ][ xPosi ] |= TOP;
                newTile |= BOTTOM;
            }
            
            // Left Wall, Good test: Left
            if ( (xPosi - 1) > -1 && grid[ yPosi ][ xPosi - 1 ] > -1 )
            {
                // Change Left tile by adding value of right BIT
                grid[ yPosi ][ xPosi - 1 ] |= RIGHT;
                newTile |= LEFT;
            }
            
            // Save the newly built tile
            grid[ yPosi ][ xPosi ] = newTile;
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Create the BG dimmed images
     * <p>
     * //###############################
     *
     * @param canvas
     *
     * @return
     */
    public void putTile( Canvas canvas, int x, int y )
    {
        Bitmap bgTile;
        Rect   dstRect = new Rect();
        int    padding = adjust;
        
        // Try to detect the mapping
        try
        {
            //
            // Split the image into tile sized sections if possible
            //            bitmaps = Bitmap.createBitmap( loadBmp, (x * width), (y * height), width, height );
            BoardTile ex = boardTiles.get( y * getColumnCount() + x );
            
            // Location
            dstRect.left = (x * adjust) + adjust;
            dstRect.top = (y * adjust) + adjust;
            dstRect.right = (x * adjust) + adjust + adjust;
            dstRect.bottom = (y * adjust) + adjust + adjust;
            
            //
            if ( gridData[ y ][ x ] > -1 )
            {
                //                bgTile = BitmapFactory.decodeResource( getResources(), autotiles[ gridData[ y ][ x ] ] );
                
                // Place the center tile ONLY if a tile is present!
                bgTile = BitmapFactory.decodeResource( getResources(), R.drawable.center );
                
                //
                canvas.drawBitmap( bgTile, null, dstRect, null );
                canvas.drawBitmap( bgTile, null, dstRect, null );
                bgTile.recycle();
            }
            else
            {
                // clear the tile
                //
                Paint clearPaint = new Paint();
                clearPaint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
                canvas.drawRect( dstRect, clearPaint );
            }
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Create the BG dimmed images
     * <p>
     * //###############################
     *
     * @param canvas
     * @param x
     * @param y
     * @param direction
     * @param in_or_out
     * @param resID
     */
    public void putCornerOrWall( Canvas canvas, int x, int y, int direction, int in_or_out, int resID )
    {
        /*
         * Corner direction for rotation purposes
         * 1  2
         * 8  4
         * */
        int    scaleX = (direction & 1);
        int    scaleY = ((direction & 2) >> 1);
        Matrix matrix = new Matrix();
        
        Bitmap bgTile;
        Bitmap scaledImage;
        Rect   dstRect = new Rect();
        int    padding = adjust;//getResources().getDimensionPixelSize( R.dimen._1sdp );
        
        // ONLY FOR OUTER CORNERS
        int xAdj = 0;
        int yAdj = 0;
        
        // Try to detect the mapping
        try
        {
            //
            BoardTile ex = boardTiles.get( y * getColumnCount() + x );
            
            
            if ( scaleX == 0 )
            {
                // Place on the left side
                scaleX = 1;
                //                xAdj = -ex.getWidth();
                xAdj = -adjust;
            }
            else
            {
                // Place on the right side
                scaleX = -scaleX;
                
                //                xAdj = ex.getWidth();
                xAdj = adjust;
            }
            
            if ( scaleY == 0 )
            {
                // Place on the top
                scaleY = 1;
                //                yAdj = -ex.getHeight();
                yAdj = -adjust;
            }
            else
            {
                // Place on the bottom
                scaleY = -scaleY;
                //                yAdj = ex.getHeight();
                yAdj = adjust;
            }
            
            //
            // Is it an inner corner?
            //
            dstRect.left = (x * adjust) + adjust;
            dstRect.top = (y * adjust) + adjust;
            dstRect.right = (x * adjust) + adjust + adjust;
            dstRect.bottom = (y * adjust) + adjust + adjust;
            
            if ( in_or_out == TileMapper.INNER_CORNER )
            {
                // Inner corner
                bgTile = BitmapFactory.decodeResource( getResources(), resID );
                
                matrix.postScale( scaleX, scaleY, bgTile.getWidth() / 2f, bgTile.getHeight() / 2f );
                scaledImage = Bitmap.createBitmap( bgTile, 0, 0, bgTile.getWidth(), bgTile.getHeight(), matrix, true );
            }
            else if ( in_or_out == TileMapper.HORIZONTAL_OUTER_WALL )
            {
                // Outer wall: North / South
                bgTile = BitmapFactory.decodeResource( getResources(), resID );
                
                matrix.postScale( scaleX, scaleY, bgTile.getWidth() / 2f, bgTile.getHeight() / 2f );
                scaledImage = Bitmap.createBitmap( bgTile, 0, 0, bgTile.getWidth(), bgTile.getHeight(), matrix, true );
                
                if ( scaleY < 0 )
                {
                    dstRect.top += adjust;
                    dstRect.bottom += adjust;
                }
                else
                {
                    dstRect.top -= adjust;
                    dstRect.bottom -= adjust;
                }
            }
            else if ( in_or_out == TileMapper.VERTICAL_OUTER_WALL )
            {
                // Outer wall: East / West
                bgTile = BitmapFactory.decodeResource( getResources(), resID );
                
                matrix.postScale( scaleX, scaleY, bgTile.getWidth() / 2f, bgTile.getHeight() / 2f );
                scaledImage = Bitmap.createBitmap( bgTile, 0, 0, bgTile.getWidth(), bgTile.getHeight(), matrix, true );
                
                if ( scaleX < 0 )
                {
                    dstRect.left += adjust;
                    dstRect.right += adjust;
                }
                else
                {
                    dstRect.left -= adjust;
                    dstRect.right -= adjust;
                }
            }
            else
            {
                // Outer corner
                bgTile = BitmapFactory.decodeResource( getResources(), resID );
                
                matrix.postScale( scaleX, scaleY, bgTile.getWidth() / 2f, bgTile.getHeight() / 2f );
                scaledImage = Bitmap.createBitmap( bgTile, 0, 0, bgTile.getWidth(), bgTile.getHeight(), matrix, true );
                
                dstRect.left += xAdj;
                dstRect.top += yAdj;
                dstRect.right += xAdj;
                dstRect.bottom += yAdj;
            }
            
            
            //
            canvas.drawBitmap( scaledImage, null, dstRect, null );
            canvas.drawBitmap( scaledImage, null, dstRect, null );
            
            //
            bgTile.recycle();
            scaledImage.recycle();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * //#############################
     * //
     * // Clear existing map
     * //
     * //#############################
     */
    public void clearMask()
    {
        setBackgroundResource( 0 );
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
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.GridLayoutView );
        TypedArray arrayList;
        int[]      array;
        
        //
        // Attributes
        //
        //
        // Get the type
        //
        if ( a.hasValue( R.styleable.GridLayoutView_android_columnCount ) )
        {
            setColumnCount( a.getInt( R.styleable.GridLayoutView_android_columnCount, 0 ) );
        }
        
        if ( a.hasValue( R.styleable.GridLayoutView_android_rowCount ) )
        {
            setRowCount( a.getInt( R.styleable.GridLayoutView_android_rowCount, 0 ) );
        }
        
        // Clean up
        a.recycle();
    }
    
    
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        
        if ( loadBmp != null )
        {
            loadBmp.recycle();
        }
        loadBmp = null;
        bmpCanvas = null;
        gridData = null;
        xyGrid = null;
        boardTiles = null;
    }
    
}
