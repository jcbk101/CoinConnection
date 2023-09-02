package com.genesyseast.coinconnection.Variables;

import android.graphics.Point;

public class PointXYZ
        extends Point
{
    public static final int LEFT  = 0;
    public static final int UP    = 1;
    public static final int RIGHT = 2;
    public static final int DOWN  = 3;
    
    public int    x;
    public int    y;
    public int    z;
    public float  degree;
    public int    data;
    public Object tag;
    
    public Object getTag()
    {
        return tag;
    }
    
    public void setTag( Object tag )
    {
        this.tag = tag;
    }
    
    
    public PointXYZ()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.degree = 0;
        this.data = 0;
        this.tag = null;
    }
    
    public PointXYZ( PointXYZ xyz )
    {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        this.degree = xyz.degree;
        this.data = xyz.data;
        this.tag = xyz.tag;
    }
    
    public PointXYZ( int x )
    {
        this.x = x;
        this.y = 0;
        this.z = 0;
        this.degree = 0;
        this.data = 0;
        this.tag = null;
    }
    
    public PointXYZ( int x, int y )
    {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.degree = 0;
        this.data = 0;
        this.tag = null;
    }
    
    public PointXYZ( int x, int y, int z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.degree = 0;
        this.data = 0;
        this.tag = null;
    }
    
    public PointXYZ( int x, int y, int z, float degree )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.degree = degree;
        this.data = 0;
        this.tag = null;
    }
    
    public PointXYZ( int x, int y, int z, float degree, int data )
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.degree = degree;
        this.data = data;
        this.tag = null;
    }
    
    public void clear()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.degree = 0;
        this.tag = null;
    }
    
/*
    public void test()
    {

        
        if ( !layout.isiHaveStart() )
        {
            layout.setStart( new Point( ((pressed.x / size) * size) + (size / 2), ((pressed.y / size) * size) + (size / 2) ) );
            layout.setiHaveStart( true );
        }
        else
*/

/*
            layout.setEnd( new Point( ((pressed.x / size) * size) + (size / 2), ((pressed.y / size) * size) + (size / 2) ) );
            layout.setiHaveEnd( true );
            layout.invalidate();
*/

/*
            float  xDist  = (layout.getEnd().x - layout.getStart().x);
            float  yDist  = (layout.getEnd().y - layout.getStart().y);
            double length = (xDist * xDist) + (yDist * yDist);
            double angle;
            int    center;
            
            
            //
            length = Math.sqrt( length );
            angle = angleOfTwoPoints( layout.getStart(), layout.getEnd() ) - 180;
            
            //
            message.setText( String.format( Locale.getDefault(), "Line length: %d, Line Angle: %d", ( int ) length, ( int ) angle ) );
            
            bolt.setLayoutParams(
                    new ConstraintLayout.LayoutParams( getResources().getDimensionPixelSize( R.dimen._48sdp ), ( int ) length ) );
            //
            
            bolt.setPivotX( bolt.getWidth() / 2 );
            bolt.setPivotY( 0 );
            
            
            if ( size > bolt.getWidth() )
            {
                center = (size / 2) - (bolt.getWidth() / 2);
            }
            else if ( size < bolt.getWidth() )
            {
                center = (bolt.getWidth() / 2) - (size / 2);
            }
            else
            {
                center = size / 2;
            }
            
            int xx, yy;
            
            xx = ( int ) (layout.getStart().x / size) * size;
            yy = ( int ) (layout.getStart().y / size) * size;
            
            
            xx += layout.getLeft() - center;
            yy += layout.getTop() + (size / 2);
            
            bolt.animate().x( xx );
            bolt.animate().y( yy );
            
            seeker.setProgress( ( int ) angle );
            bolt.animate().rotation( ( int ) angle ).setDuration( 1000 ).start();
            bolt.animate().setDuration( 1000 ).start();
        }
    }
*/
    
    
    /**
     * Return the angle between two points
     *
     * @param s
     * @param e
     *
     * @return
     */
    public static double angleOfTwoPoints( Point s, Point e )
    {
        final double deltaY = (e.y - s.y);
        final double deltaX = (e.x - s.x);
        double       result;
        
        result = Math.atan2( deltaY, deltaX );
        result = Math.toDegrees( result );
        result += 90;
        
        return (result < 0) ? (360d + result) : result;
    }
    
    
    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        
        PointXYZ p = ( PointXYZ ) o;
        
        if ( p.x == x && p.y == y )
        {
            return true;
        }
        
        return super.equals( o );
    }
}
