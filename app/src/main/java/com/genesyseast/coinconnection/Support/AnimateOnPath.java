package com.genesyseast.coinconnection.Support;

import android.animation.TypeEvaluator;

public class AnimateOnPath
        implements TypeEvaluator<PointLocations>
{
    private int swoopValue = 3;
    
    public AnimateOnPath()
    {
    }
    
    public AnimateOnPath( int swoopValue )
    {
        this.swoopValue = swoopValue;
    }
    
    
    @Override
    public PointLocations evaluate( float adjustValue, PointLocations startValue, PointLocations endValue )
    {
        float x, y;
        
        if ( endValue.mOperation == PointLocations.pathCurve )
        {
            float oneMinusT = 1 - adjustValue;
            
            x = oneMinusT * oneMinusT * oneMinusT * startValue.pointX + //
                    swoopValue * oneMinusT * oneMinusT * adjustValue * endValue.controlX1 + //
                    swoopValue * oneMinusT * adjustValue * adjustValue * endValue.controlX2 + //
                    adjustValue * adjustValue * adjustValue * endValue.pointX;
            
            //
            y = oneMinusT * oneMinusT * oneMinusT * startValue.pointY + //
                    swoopValue * oneMinusT * oneMinusT * adjustValue * endValue.controlY1 + //
                    swoopValue * oneMinusT * adjustValue * adjustValue * endValue.controlY2 + //
                    adjustValue * adjustValue * adjustValue * endValue.pointY;
            
        }
        else if ( endValue.mOperation == PointLocations.pathLine )
        {
            x = startValue.pointX + adjustValue * (endValue.pointX - startValue.pointX);
            y = startValue.pointY + adjustValue * (endValue.pointY - startValue.pointY);
        }
        else
        {
            x = endValue.pointX;
            y = endValue.pointY;
        }
        
        return PointLocations.moveTo( x, y );
    }
}
