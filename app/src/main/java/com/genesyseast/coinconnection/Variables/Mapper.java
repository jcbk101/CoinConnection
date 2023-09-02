package com.genesyseast.coinconnection.Variables;


import java.util.ArrayList;

public class Mapper

{
    public static final int LEFT  = 0;
    public static final int UP    = 1;
    public static final int RIGHT = 2;
    public static final int DOWN  = 3;
    
    /*
        private int                startX;
        private int                startY;
        private int                endX;
        private int                endY;
        private int                direction;
        private int                count;
        private int                index;
    */
    public int                index;
    public int                special;
    public  ArrayList<Integer> entry;
    
    public Mapper()
    {
        entry = new ArrayList<>();
    }
    
    /*
     */
    /**
     * Constructors
     *
     * @param startX    N/A
     * @param startY    N/A
     * @param endX      N/A
     * @param endY      N/A
     * @param direction N/A
     * @param count     N/A
     * @param index     N/A
     *//*

    public Mapper( int startX, int startY, int endX, int endY, int direction, int count, int index )
    {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.direction = direction;
        this.count = count;
        this.index = index;
        this.entry = new ArrayList<>();
    }
    
    public Mapper( int startX, int startY, int direction )
    {
        this.startX = startX;
        this.startY = startY;
        this.endX = 0;
        this.endY = 0;
        this.direction = direction;
        this.count = 0;
        this.index = 0;
        this.entry = new ArrayList<>();
    }
    
    public Mapper()
    {
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.direction = 0;
        this.count = 0;
        this.index = 0;
        this.entry = new ArrayList<>();
    }
*/

/*
    public int getIndex()
    {
        return index;
    }
    
    public void setIndex( int index )
    {
        this.index = index;
    }
    
    public int getStartX()
    {
        return startX;
    }
    
    public void setStartX( int startX )
    {
        this.startX = startX;
    }
    
    public int getStartY()
    {
        return startY;
    }
    
    public void setStartY( int startY )
    {
        this.startY = startY;
    }
    
    public int getEndX()
    {
        return endX;
    }
    
    public void setEndX( int endX )
    {
        this.endX = endX;
    }
    
    public int getEndY()
    {
        return endY;
    }
    
    public void setEndY( int endY )
    {
        this.endY = endY;
    }
    
    public int getDirection()
    {
        return direction;
    }
    
    public void setDirection( int direction )
    {
        this.direction = direction;
    }
    
    public int getCount()
    {
        return count;
    }
    
    public void setCount( int count )
    {
        this.count = count;
    }
    
    
    */
    /**
     * Clear all variables
     *//*

    public void clear()
    {
        this.startX = 0;
        this.startY = 0;
        this.endX = 0;
        this.endY = 0;
        this.direction = 0;
        this.count = 0;
        this.index = 0;
    }
*/
}
