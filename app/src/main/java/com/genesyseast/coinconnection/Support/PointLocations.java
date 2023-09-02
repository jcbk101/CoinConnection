package com.genesyseast.coinconnection.Support;

public class PointLocations
{
    public static final int pathMove  = 0;
    public static final int pathLine  = 1;
    public static final int pathCurve = 2;
    
    // points location
    public float pointX;
    public float pointY;
    
    float controlX1;
    float controlY1;
    float controlX2;
    float controlY2;
    int   mOperation;
    
    
    private PointLocations( int operation, float x, float y )
    {
        mOperation = operation;
        pointX = x;
        pointY = y;
    }
    
    private PointLocations( float c0X, float c0Y, float c1X, float c1Y, float x, float y )
    {
        controlX1 = c0X;
        controlY1 = c0Y;
        controlX2 = c1X;
        controlY2 = c1Y;
        pointX = x;
        pointY = y;
        mOperation = pathCurve;
    }
    
    public static PointLocations lineTo( float x, float y )
    {
        return new PointLocations( pathLine, x, y );
    }
    
    public static PointLocations curveTo( float c0X, float c0Y, float c1X, float c1Y, float x, float y )
    {
        return new PointLocations( c0X, c0Y, c1X, c1Y, x, y );
    }
    
    public static PointLocations moveTo( float x, float y )
    {
        return new PointLocations( pathMove, x, y );
    }
}
