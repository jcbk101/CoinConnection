package com.genesyseast.coinconnection.Support;

import android.animation.TypeEvaluator;

public class CustomEvaluator
        implements TypeEvaluator
{
    @Override
    public Object evaluate( float fraction, Object startValue, Object endValue )
    {
        if ( fraction >= .99f )
        {
            return ( int ) endValue;
        }
        else
        {
            return ( int ) startValue;
        }
    }
}
