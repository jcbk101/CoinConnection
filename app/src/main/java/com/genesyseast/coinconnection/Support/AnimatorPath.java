package com.genesyseast.coinconnection.Support;

import java.util.ArrayList;
import java.util.Collection;

public class AnimatorPath
{
    // The points in the path
    ArrayList<PointLocations> pathPoints = new ArrayList<PointLocations>();
    
    
    public void moveTo( float x, float y )
    {
        pathPoints.add( PointLocations.moveTo( x, y ) );
    }
    public void lineTo( float x, float y )
    {
        pathPoints.add( PointLocations.lineTo( x, y ) );
    }
    
    public void curveTo( float c0X, float c0Y, float c1X, float c1Y, float x, float y )
    {
        pathPoints.add( PointLocations.curveTo( c0X, c0Y, c1X, c1Y, x, y ) );
    }
    
    public Collection<PointLocations> getPoints()
    {
        return pathPoints;
    }
}
