package com.genesyseast.coinconnection.CustomControls;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.AnimateOnPath;
import com.genesyseast.coinconnection.Support.AnimatorPath;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.PointLocations;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.util.ArrayList;
import java.util.Arrays;

import static com.genesyseast.coinconnection.GameEngine.GameBoard.coinBombIcons;
import static com.genesyseast.coinconnection.GameEngine.GameBoard.coinSet;

public class BoardTile
        extends AppCompatImageView
        implements ValueAnimator.AnimatorListener
{
    //##################################
    // State Codes
    //##################################
    // Not active or usable on the board
    public static final int STATE_INACTIVE        = 0;
    // Active and movable
    public static final int STATE_ACTIVE          = 1;
    // Spawning an tag onto the board for the first time
    public static final int STATE_RESPAWN         = 2;
    // Start drop process
    public static final int STATE_DROP            = 3;
    // Spawning an tag onto the board after a math
    public static final int STATE_DROPPING        = 4;
    // Was matched, needs to be re-spawned
    public static final int STATE_MATCHED         = 5;
    // Gem was matched
    public static final int GEM_MATCHED           = 6;
    // Maintain the swap state
    public static final int BLOCKER_MATCHED       = 7;
    //
    public static final int STATE_UNSWAP          = 8;
    // For special item creation
    public static final int CREATE_ITEM           = 9;
    public static final int MASTER_ITEM           = 10;
    // Activate a special item
    public static final int USE_SPECIAL_ITEM      = 11;
    // Activate a special item after a current special
    public static final int USE_SPECIAL_ITEM_NEXT = 12;
    // Perform animation attached to setTag() after SPECIAL used
    public static final int SPECIAL_ANIMATE       = 13;
    //
    public static final int ANNOUNCE_PRESENCE     = 14;
    //
    public static final int FLIP_CARD             = 15;
    //
    public static final int STATE_FLIPPED         = 16;
    //
    public static final int MASTER_ITEM_TO_GIVE   = 17;
    
    
    //##################################
    // Special tiles
    //##################################
    final public static int MAX_COINS      = 7;
    // GEMS
    public static final int PURPLE_GEM     = MAX_COINS;
    public static final int GREEN_GEM      = MAX_COINS + 1;
    public static final int TEAL_GEM       = MAX_COINS + 2;
    public static final int YELLOW_GEM     = MAX_COINS + 3;
    //
    public static final int RAINBOW_COIN   = MAX_COINS + 4;
    // Explodes 3x3, 5x5 grid of coins
    public static final int BOMB           = MAX_COINS + 5;
    // Clears all colors vertically, Horizontally, or both
    public static final int BOLT           = MAX_COINS + 6;
    // Clears all colors spreading out from prongs
    public static final int STAR           = MAX_COINS + 7;
    // Random special
    public static final int RANDOM         = MAX_COINS + 8;
    //#################################
    // Blockers
    //#################################
    public static final int COIN_STACK     = MAX_COINS + 9;
    // Create a particle set for this to use in the particle engine
    public static final int SHELL_BLOCKER  = MAX_COINS + 10;
    // Create a particle set for this to use in the particle engine
    public static final int ROCK_BLOCKER   = MAX_COINS + 11;
    // This is 3 stages
    public static final int ICE_BLOCKER    = MAX_COINS + 12;
    // This is 4 stages
    public static final int BARREL_BLOCKER = MAX_COINS + 13;
    // This is 2 stages
    public static final int CHEST_BLOCKER  = MAX_COINS + 14;
    //#################################
    // Non Generators
    //#################################
    public static final int NON_GENERATOR  = -2;
    
    
    /**
     * //#################################
     * // Striped and other special coins
     * // Vertical or Horizontal
     * // Used with: Special Tile
     * //#################################
     */
    public static final int GEM_COIN                = 3;
    public static final int SWEEPER_COIN_VERTICAL   = 4;
    public static final int SWEEPER_COIN_HORIZONTAL = 5;
    public static final int RAINBOW_SEEKER          = 6;
    
    public static class BlockerList
    {
        Drawable drawable;
        public int id;
        
        public BlockerList( Drawable drawable, int id )
        {
            this.drawable = drawable;
            this.id = id;
        }
    }
    
    
    // For tile swapping statuses
    public  int                         soundEfx        = 0;
    public  int                         animDelay       = 0;
    public  int                         gotoTargetDelay = 0;
    public  int                         specialTile     = -1;
    private int                         state;
    private int                         position;
    public  int                         tileNum;
    private int                         specialItem;
    public  int[]                       card            = { 0, 0 };
    public  PointXYZ                    swapTO;
    public  boolean                     isCaught        = false;
    public  int                         pointPosi;
    // DEBUG
    private int                         blockerType;
    private int                         blockerCount;
    private int                         blockerColor;
    public  int[]                       blockerResIds;
    public  ArrayList<BlockerList>      blockerImages;
    private Drawable[]                  specialLayer;
    private int                         specialsIndex;
    private int                         imageResource;
    private int                         backgroundResource;
    //*/
    private Context                     context;
    //
    public  BitmapDrawable              overlayImage;
    public  ValueAnimator               valueAnimator;
    public  ValueAnimator               animator        = null;
    private ObjectAnimator              rot;
    private OnAnimationCompleteListener onAnimationCompleteListener;
    private boolean                     debugVisiblity  = false;
    public  boolean                     cardFlipped     = false;
    public  boolean                     blockerAdjusted = false;
    private int[]                       stackIndex      = new int[]{ 0, 1, 2 };
    private int[]                       blockerStack    = new int[]{
            R.drawable.coin_stack_red,
            R.drawable.coin_stack_green,
            R.drawable.coin_stack_blue,
            R.drawable.coin_stack_orange,
            R.drawable.coin_stack_pink,
            R.drawable.coin_stack_purple,
            R.drawable.coin_stack_gold
    };
    
    private int[] iceLayer = new int[]{
            R.drawable.ice_1, R.drawable.ice_2, R.drawable.ice_3
    };
    
    private int[] treasuresChest = new int[]{
            R.drawable.chest_blocker_closed, R.drawable.chest_blocker_empty, R.drawable.chest_blocker_full
    };
    private int[] barrels        = new int[]{
            R.drawable.barrel_00, R.drawable.barrel_01,
            };
    
    //
    // TODO: Remove for release
    private int   xAdj;
    private int   yAdj;
    //
    private Paint paint;
    
    public interface OnAnimationCompleteListener
    {
        void onAnimationComplete();
    }
    
    
    /**
     * //##################################
     * <p>
     * Return the call to the calling
     * image in
     * <p>
     * //##################################
     *
     * @param onAnimationCompleteListener
     */
    public void setOnAnimationCompleteListener( OnAnimationCompleteListener onAnimationCompleteListener )
    {
        this.onAnimationCompleteListener = onAnimationCompleteListener;
    }
    
    
    /**
     * //##################################
     * <p>
     * Constructors
     * <p>
     * //##################################
     *
     * @param context
     */
    public BoardTile( Context context )
    {
        super( context );
        this.context = context;
        
        readAttr( context, null );
        
        paint = new Paint();
        paint.setStrokeWidth( 2 );
        paint.setColor( 0xFF00FF00 );
        paint.setStyle( Paint.Style.STROKE );
    }
    
    
    public BoardTile( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.context = context;
        readAttr( context, attrs );
        
        paint = new Paint();
        paint.setStrokeWidth( 2 );
        paint.setColor( 0xFF00FF00 );
        paint.setStyle( Paint.Style.STROKE );
        
    }
    
    public BoardTile( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        this.context = context;
        
        readAttr( context, attrs );
        
        paint = new Paint();
        paint.setStrokeWidth( 2 );
        paint.setColor( 0xFF00FF00 );
        paint.setStyle( Paint.Style.STROKE );
        
    }
    
    
    /**
     * //##################################
     * <p>
     * Constructor for map tile array
     * <p>
     * //##################################
     *
     * @param context
     * @param state
     * @param position
     */
    public BoardTile( Context context, int state, int position )
    {
        super( context );
        this.state = state;
        this.position = position;
    }
    
    
    public int getTileX()
    {
        return position % ConnectionsGridLayout.getMapWidth();
    }
    
    public int getTileY()
    {
        return position / ConnectionsGridLayout.getMapHeight();
    }
    
    
    /**
     * //##################################
     * <p>
     * Setter for Object's special item
     * <p>
     * //##################################
     *
     * @param specialItem
     */
    public void setSpecialItem( int specialItem )
    {
        this.specialItem = specialItem;
        
        // Kill and restart if needed
        if ( valueAnimator != null )
        {
            valueAnimator.end();
            valueAnimator.removeAllListeners();
            valueAnimator = null;
        }
        
        if ( specialItem == SWEEPER_COIN_VERTICAL || specialItem == SWEEPER_COIN_HORIZONTAL )
        {
            int[] bomb_sparkle = GameEngine.arrayFromResource( getContext(), R.array.bomb_sparkle );
            
            specialsIndex = 0;
            setSpecialLayer( bomb_sparkle );
            
            valueAnimator = ValueAnimator.ofInt( 0, bomb_sparkle.length );
            valueAnimator.setDuration( 300 ).setRepeatCount( ValueAnimator.INFINITE );
            valueAnimator.setInterpolator( new LinearInterpolator() );
            valueAnimator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate( ValueAnimator animation )
                {
                    specialsIndex = ( int ) animation.getAnimatedValue();
                    
                    if ( specialsIndex >= bomb_sparkle.length )
                    {
                        specialsIndex = bomb_sparkle.length - 1;
                    }
                    
                    invalidate();
                }
            } );
            
            valueAnimator.start();
        }
    }
    
    
    public int getSpecialItem()
    {
        return specialItem;
    }
    
    
    public void setSpecialLayer( int[] resIds )
    {
        if ( this.specialLayer != null )
        {
            this.specialLayer = null;
        }
        
        if ( resIds.length > 0 )
        {
            this.specialLayer = new Drawable[ resIds.length ];
            
            for ( int i = 0; i < resIds.length; i++ )
            {
                this.specialLayer[ i ] = ResourcesCompat.getDrawable( getResources(), resIds[ i ], null );
            }
        }
    }
    
    /**
     * //##################################
     * <p>
     * Setter for Object's state
     * <p>
     * //##################################
     *
     * @param state
     */
    public void setState( int state )
    {
        this.state = state;
    }
    
    
    /**
     * //##################################
     * <p>
     * Getter for Object's state
     * <p>
     * //##################################
     *
     * @return
     */
    public int getState()
    {
        return state;
    }
    
    
    /**
     * Get the
     *
     * @return
     */
    public int getImageResource()
    {
        return imageResource;
    }
    
    public int getBackgroundResource()
    {
        return backgroundResource;
    }
    
    
    @Override
    public void setImageResource( int resId )
    {
        super.setImageResource( resId );
        imageResource = resId;
    }
    
    
    @Override
    public void setBackgroundResource( int resId )
    {
        super.setBackgroundResource( resId );
        backgroundResource = resId;
    }
    
    /**
     * //##################################
     * <p>
     * Position setter
     * <p>
     * //##################################
     *
     * @param position
     */
    public void setPosition( int position )
    {
        this.position = position;
    }
    
    public int getPosition()
    {
        return position;
    }
    
    
    /**
     * //##################################
     * <p>
     * Use a different sound efx for
     * matching
     * <p>
     * //##################################
     *
     * @param soundEfx
     */
    public void setSoundEfx( int soundEfx )
    {
        this.soundEfx = soundEfx;
    }
    
    
    /**
     * //##################################
     * <p>
     * Use to get the sound effect
     * <p>
     * //##################################
     *
     * @return
     */
    public int getSoundEfx()
    {
        return soundEfx;
    }
    
    
    /**
     * //##################################
     * <p>
     * Return current blocker
     * <p>
     * //##################################
     *
     * @return
     */
    public int getBlockerType()
    {
        return blockerType;
    }
    
    
    /**
     * //##################################
     * <p>
     * Return amount of images used
     * <p>
     * //##################################
     *
     * @return
     */
    public int getBlockerCount()
    {
        return blockerCount;
    }
    
    
    /**
     * //#####################################
     * <p>
     * Helper method to decrease the blocker
     * image count so that memory does not
     * have to be adjusted when decreasing
     * blocker stacks and progressives
     * <p>
     * //#####################################
     */
    public void adjustBlocker()
    {
        if ( blockerImages != null )
        {
            if ( blockerImages.size() > 0 )
            {
                blockerImages.remove( 0 );
                blockerCount = blockerImages.size();
            }
            
            //
            // Reset if blockers are done
            //
            if ( blockerImages.size() == 0 )
            {
                blockerImages = null;
                blockerResIds = null;
                
                // Only for hidden coins to stay active
                // Any blocker that requires this will be supported
                if ( blockerType == ICE_BLOCKER )
                {
                    blockerType = -1;
                }
                else
                {
                    blockerType = 0;
                }
                blockerCount = 0;
            }
        }
    }
    
    
    /**
     * //###############################
     * <p>
     * Set the blocker type
     * <p>
     * //###############################
     *
     * @param blockerType
     */
    public void setBlockerType( int blockerType )
    {
        this.blockerType = blockerType;
    }
    
    
    public void setBlockerType( int blockerType, int[] blockerImage )
    {
        this.blockerType = blockerType;
        setBlockerImages( blockerImage );
    }
    
    
    /**
     * //##################################
     * <p>
     * Get the images to use
     * <p>
     * //##################################
     *
     * @param blockerImage
     */
    protected void setBlockerImages( int[] blockerImage )
    {
        int resDrawableInt = 0;
        
        if ( this.blockerImages == null )
        {
            this.blockerImages = new ArrayList<>();
        }
        
        //
        this.blockerImages.clear();
        this.blockerResIds = blockerImage;
        
        if ( blockerImage != null )
        {
            for ( int value : blockerImage )
            {
                // TODO: ALL NEEDS TESTING
                if ( blockerType == COIN_STACK )
                {
                    resDrawableInt = blockerStack[ value ];
                }
                else if ( blockerType == ICE_BLOCKER )
                {
                    resDrawableInt = iceLayer[ value ];
                }
                else if ( blockerType == CHEST_BLOCKER )
                {
                    resDrawableInt = treasuresChest[ value ];
                }
                else if ( blockerType == BARREL_BLOCKER )
                {
                    resDrawableInt = barrels[ value ];
                }
                
                //
                this.blockerImages.add( new BlockerList( ResourcesCompat.getDrawable( getResources(), resDrawableInt, null ), resDrawableInt ) );
            }
            
            //
            this.blockerCount = blockerImage.length;
            setImageResource( 0 );
        }
        else
        {
            this.blockerCount = 0;
        }
        
    }
    
    
    /**
     * //###############################
     * <p>
     * Strictly used for coin stacks
     * <p>
     * //###############################
     *
     * @return
     */
    public int getBlockerColor()
    {
        if ( blockerType == COIN_STACK )
        {
            for ( int i = 0; i < blockerStack.length; i++ )
            {
                if ( blockerImages.get( 0 ).id == blockerStack[ i ] )
                {
                    return i;
                }
            }
        }
        
        // Gold by default
        return 6;
    }
    
    
    /**
     * //###############################
     * <p>
     * Strictly used for coin stacks
     * <p>
     * //###############################
     *
     * @return
     */
    public int getBlockerIndex( int resId )
    {   //
        if ( blockerType == COIN_STACK )
        {
            for ( int i = 0; i < blockerStack.length; i++ )
            {
                if ( resId == blockerStack[ i ] )
                {
                    return i;
                }
            }
        }
        else if ( blockerType == ICE_BLOCKER )
        {
            for ( int i = 0; i < iceLayer.length; i++ )
            {
                if ( resId == iceLayer[ i ] )
                {
                    return i;
                }
            }
        }
        else if ( blockerType == CHEST_BLOCKER )
        {
            for ( int i = 0; i < treasuresChest.length; i++ )
            {
                if ( resId == treasuresChest[ i ] )
                {
                    return i;
                }
            }
        }
        else if ( blockerType == BARREL_BLOCKER )
        {
            for ( int i = 0; i < barrels.length; i++ )
            {
                if ( resId == barrels[ i ] )
                {
                    return i;
                }
            }
        }
        
        // Gold by default
        return 0;
    }
    
    
    /**
     * //##################################
     * <p>
     * Handle all special case drawing:
     * Blockers, etc
     * <p>
     * //##################################
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw( Canvas canvas )
    {
        //###################################
        //
        // Draw the blockers for stacked
        // coins
        //
        //###################################
        if ( blockerType == COIN_STACK )
        {
            final int saveCount   = canvas.save();
            float     yOffset;
            int       stackAdjust = getResources().getDimensionPixelSize( R.dimen._4sdp );
            int       sIndex      = 0;
            
            //###############################
            //
            //
            //
            //###############################
            for ( int i = blockerImages.size() - 1, s = 0; i > -1; i--, s++ )
            {
                Drawable coin = blockerImages.get( i ).drawable;
                
                yOffset = getPaddingTop() + stackAdjust;
                yOffset -= (stackAdjust * stackIndex[ s ]);
                
                // Move to the X position for THIS star
                canvas.translate( 0, yOffset );
                
                // Adjust bounds for padding
                coin.setBounds( getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom() );
                
                // Draw the coin
                //canvas.drawBitmap( (( BitmapDrawable ) coin).getBitmap(), 0, 0, null );
                coin.draw( canvas );
                
                // Reset the Pen's position back to the start
                // Of the draw location
                canvas.translate( 0, -yOffset );
            }
            
            canvas.restoreToCount( saveCount );
        }
        else if ( blockerType == ICE_BLOCKER || blockerType == CHEST_BLOCKER || blockerType == BARREL_BLOCKER )
        {
            final int saveCount  = canvas.save();
            int       sizeAdjust = 0;
            Drawable  drawable   = blockerImages.get( 0 ).drawable;
            
            
            if ( blockerType == ICE_BLOCKER )
            {
                sizeAdjust = getResources().getDimensionPixelSize( R.dimen._2sdp );
                
                // Draw the coin
                super.onDraw( canvas );
            }
            
            
            //###############################
            //
            //
            //
            //###############################
            // Move to the X position for THIS star
            //            canvas.scale( 1.5f, 1.5f );
            
            // Adjust bounds for padding
            drawable.setBounds( getPaddingLeft() - sizeAdjust, getPaddingTop() - sizeAdjust, (sizeAdjust + getWidth()) - getPaddingRight(),
                                (sizeAdjust + getHeight()) - getPaddingBottom()
                              );
            
            
            // Draw the coin
            //canvas.drawBitmap( (( BitmapDrawable ) coin).getBitmap(), 0, 0, null );
            drawable.draw( canvas );
            
            canvas.restoreToCount( saveCount );
        }
        else if ( specialItem == SWEEPER_COIN_VERTICAL || specialItem == SWEEPER_COIN_HORIZONTAL )
        {
            if ( specialLayer != null )
            {
                final int saveCount = canvas.save();
                float     yOffset;
                float     xOffset;
                int       xAdj      = getResources().getDimensionPixelSize( R.dimen._6sdp );
                int       yAdj      = getResources().getDimensionPixelSize( R.dimen._12sdp );
                float     size      = getWidth() / 1.5f;
                
                // Draw primary BG
                super.onDraw( canvas );
                
                if ( this.xAdj > 0 )
                {
                    xAdj = this.xAdj;
                }
                
                if ( this.yAdj > 0 )
                {
                    yAdj = this.yAdj;
                }
                
                //###############################
                //
                //
                //
                //###############################
                Drawable coin = specialLayer[ specialsIndex ];
                
                yOffset = getPaddingTop() - yAdj;
                xOffset = getPaddingLeft() - xAdj;
                
                // Move to the X position for THIS star
                canvas.translate( xOffset, yOffset );
                
                // Adjust bounds for padding
                coin.setBounds( getPaddingLeft(), getPaddingTop(), ( int ) size - getPaddingRight(), ( int ) size - getPaddingBottom() );
                
                // Draw the coin
                //canvas.drawBitmap( (( BitmapDrawable ) coin).getBitmap(), 0, 0, null );
                coin.draw( canvas );
                
                // Reset the Pen's position back to the start
                // Of the draw location
                canvas.translate( -xOffset, -yOffset );
                
                canvas.restoreToCount( saveCount );
            }
        }
        else
        {
            super.onDraw( canvas );
        }
        
/*
        // Show touch zone: Debug only!
        if ( getWidth() > 0 )
        {
            int bound = (getWidth() / 4);
            int half  = getWidth() / 2;
            
            canvas.drawRect( (getLeft() + half) - bound, (getTop() + half) - bound, (getRight() - half) + bound, (getBottom() - half) + bound, paint );
            super.onDraw( canvas );
        }
*/
    }
    
    
    /**
     * //##################################
     * <p>
     * interface listener-- valueAnimator
     * ONLY
     * <p>
     * //##################################
     *
     * @param animation
     */
    @Override
    public void onAnimationStart( Animator animation )
    {
    }
    
    @Override
    public void onAnimationEnd( Animator animation )
    {
        valueAnimator = null;
    }
    
    
    /**
     * //##################################
     * <p>
     * Handle cancels
     * <p>
     * //##################################
     *
     * @param animation
     */
    @Override
    public void onAnimationCancel( Animator animation )
    {
    }
    
    @Override
    public void onAnimationRepeat( Animator animation )
    {
    
    }
    
    
    /**
     * //##################################
     * <p>
     * Depending on animation type, cancel
     * it
     * <p>
     * //##################################
     */
    public void endAnimation()
    {
        if ( getTag() != null )
        {
            if ( getTag() instanceof ObjectAnimator )
            {
                // Clear it up
                (( ObjectAnimator ) getTag()).end();
                setTag( null );
            }
            else if ( getTag() instanceof ObjectAnimator[] || getTag() instanceof ValueAnimator[] )
            {
                ValueAnimator[] obj = ( ValueAnimator[] ) getTag();
                
                for ( int i = 0; i < obj.length; i++ )
                {
                    if ( obj[ i ] != null )
                    {
                        obj[ i ].end();
                    }
                    //
                    setTag( null );
                }
            }
            else if ( getTag() instanceof ValueAnimator )
            {
                // Clear it up
                (( ValueAnimator ) getTag()).end();
                setTag( null );
            }
        }
        
        //
        if ( valueAnimator != null && valueAnimator.isRunning() )
        {
            valueAnimator.end();
        }
        
        // Clear anything running
        clearAnimation();
        
        // Fix scaling
        setScaleX( 1 );
        setScaleY( 1 );
    }
    
    
    /**
     * //##################################
     * <p>
     * Depending on animation type, cancel
     * it
     * <p>
     * //##################################
     */
    public void cancelAnimation()
    {
        if ( getTag() != null )
        {
            if ( getTag() instanceof ObjectAnimator )
            {
                // Clear it up
                (( ObjectAnimator ) getTag()).cancel();
                setTag( null );
            }
            else if ( getTag() instanceof ObjectAnimator[] || getTag() instanceof ValueAnimator[] )
            {
                ValueAnimator[] obj = ( ValueAnimator[] ) getTag();
                
                for ( int i = 0; i < obj.length; i++ )
                {
                    if ( obj[ i ] != null )
                    {
                        obj[ i ].cancel();
                    }
                }
                //
                setTag( null );
            }
            else if ( getTag() instanceof ValueAnimator )
            {
                // Clear it up
                (( ValueAnimator ) getTag()).cancel();
                setTag( null );
            }
        }
        
        //
        if ( valueAnimator != null && valueAnimator.isRunning() )
        {
            valueAnimator.cancel();
        }
        
        // Clear anything running
        clearAnimation();
        
        // Fix scaling
        setScaleX( 1 );
        setScaleY( 1 );
    }
    
    
    /**
     * //##################################
     * <p>
     * We want this tile to do and / or
     * finish NO animations
     * <p>
     * //##################################
     */
    public void cancelAnimationAndListeners()
    {
        if ( getTag() != null )
        {
            if ( getTag() instanceof ObjectAnimator )
            {
                // Clear it up
                (( ObjectAnimator ) getTag()).removeAllListeners();
                (( ObjectAnimator ) getTag()).cancel();
                setTag( null );
            }
            else if ( getTag() instanceof ObjectAnimator[] || getTag() instanceof ValueAnimator[] )
            {
                ValueAnimator[] obj = ( ValueAnimator[] ) getTag();
                
                for ( int i = 0; i < obj.length; i++ )
                {
                    if ( obj[ i ] != null )
                    {
                        obj[ i ].removeAllListeners();
                        obj[ i ].cancel();
                    }
                }
                //
                setTag( null );
            }
            else if ( getTag() instanceof ValueAnimator )
            {
                // Clear it up
                (( ValueAnimator ) getTag()).removeAllListeners();
                (( ValueAnimator ) getTag()).cancel();
                setTag( null );
            }
        }
        
        //
        if ( valueAnimator != null && valueAnimator.isRunning() )
        {
            valueAnimator.removeAllListeners();
            valueAnimator.cancel();
        }
        
        // Clear anything running
        clearAnimation();
        
        // Fix scaling
        setScaleX( 1 );
        setScaleY( 1 );
        
        // Reset Start Delay
        animDelay = 0;
        animate().setStartDelay( 0 );
    }
    
    
    /**
     * //##################################
     * <p>
     * Pause / unpause animation
     * <p>
     * //##################################
     *
     * @param pauseStatus N/A
     */
    public void pauseAnimation( boolean pauseStatus )
    {
        if ( getTag() != null )
        {
            if ( getTag() instanceof ObjectAnimator )
            {
                ObjectAnimator obj = ( ObjectAnimator ) getTag();
                
                if ( pauseStatus )
                {
                    obj.pause();
                }
                else
                {
                    obj.resume();
                }
            }
            else if ( getTag() instanceof ObjectAnimator[] || getTag() instanceof ValueAnimator[] )
            {
                ValueAnimator[] obj = ( ValueAnimator[] ) getTag();
                
                for ( int i = 0; i < obj.length; i++ )
                {
                    if ( obj[ i ] != null )
                    {
                        if ( pauseStatus )
                        {
                            obj[ i ].pause();
                        }
                        else
                        {
                            obj[ i ].resume();
                        }
                    }
                }
            }
            else if ( getTag() instanceof ValueAnimator )
            {
                ValueAnimator obj = ( ValueAnimator ) getTag();
                
                if ( pauseStatus )
                {
                    obj.pause();
                }
                else
                {
                    obj.resume();
                }
            }
        }
        
        
        // Foreground animations for extra layer
        if ( valueAnimator != null )
        {
            if ( pauseStatus )
            {
                valueAnimator.pause();
            }
            else
            {
                valueAnimator.resume();
            }
        }
        
        // Board movements
        if ( animator != null )
        {
            if ( pauseStatus )
            {
                animator.pause();
            }
            else
            {
                animator.resume();
            }
        }
    }
    
    
    /**
     * //####################################
     * <p>
     * Simple image reset
     * <p>
     * //####################################
     */
    public void resetCoin()
    {
        setScaleX( 1f );
        setScaleY( 1f );
        ViewCompat.setBackgroundTintList( this, null );
        //        setImageResource( 0 );
        
        if ( getBlockerCount() < 1 )
        {
            if ( specialItem >= SWEEPER_COIN_VERTICAL && specialItem <= SWEEPER_COIN_HORIZONTAL )
            {
                setBackgroundResource( coinBombIcons[ tileNum % coinBombIcons.length ] );
            }
            else if ( getBlockerType() > 0 )
            {
                //setBackgroundResource( coinSet[ tileNum % coinSet.length ] );
            }
            else
            {
                setBackgroundResource( coinSet[ tileNum % coinSet.length ] );
            }
            
            if ( specialItem < 1 )
            {
                setImageResource( 0 );
            }
        }
    }
    
    
    /**
     * //####################################
     * <p>
     * Simple erase all statuses
     * <p>
     * //####################################
     */
    public void clearCoin()
    {
        setScaleX( 1f );
        setScaleY( 1f );
        setImageResource( 0 );
        setBackgroundResource( 0 );
        setBlockerImages( null );
        setBlockerType( 0 );
    }
    
    
    /**
     * //####################################
     * <p>
     * Animate the board icon when clicked
     * <p>
     * //####################################
     */
    public void growCoin()
    {
        // End all currently running animations
/*
        if ( getTag() != null )
        {
            endAnimation();
        }
*/
        
        setScaleX( .5f );
        setScaleY( .5f );
        animate().scaleX( 1f ).scaleY( 1f ).setDuration( 350 ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
        animate().start();
    }
    
    
    /**
     * //##############################
     * <p>
     * Flip the coin
     * <p>
     * //##############################
     */
    public ObjectAnimator flipCoin( boolean delay )
    {
        final int DURATION   = 250;
        int       startDelay = (delay ? DURATION / 2 : 0);
        BoardTile tile       = this;
        
        // TODO: fix the flip with new code
        //  from Mail notification ( code below )
/*
                obj[ 0 ].addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator valueAnimator )
                    {
                        float num = Math.abs( ( float ) valueAnimator.getAnimatedValue() );
                        
                        if ( (num >= 0 && num < 90) || (num >= 270 && num <= 360) )
                        {
                            mail.setImageResource( R.drawable.mail_back_gold );
                        }
                        else
                        {
                            mail.setImageResource( R.drawable.mail_front_gold );
                        }
                    }
                } );
*/
        
        //
        setPivotX( getWidth() / 2f );
        setPivotY( getHeight() / 2f );
        
        //##################################
        //
        // Put the flip back to normal
        //
        //##################################
        if ( cardFlipped )
        {
            // Flip back to normaL
            // Show the image we want
            // Halfway through, the views will flip
            rot = ObjectAnimator.ofFloat( tile, "alpha", .99f, 1f );
            rot.setDuration( DURATION );
            
            setScaleX( 1 );
            setRotationY( 0 );
            animate().rotationY( -90 ).setStartDelay( startDelay );
            animate().setDuration( DURATION / 2 ).setInterpolator( new LinearInterpolator() );
            animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    // swap the views
                    cardFlipped = false;
                    tileNum = card[ 0 ];
                    // Reverse the image so it looks correct
                    setBackgroundResource( tileNum );
                    
                    //
                    setRotationY( -90 );
                    setScaleX( -1 );
                    animate().rotationY( -180 );
                    animate().setDuration( DURATION / 2 ).setInterpolator( new LinearInterpolator() );
                    animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            if ( getScaleX() != -1 )
                            {
                                setScaleX( -1 );
                            }
                        }
                    } );
                    animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setScaleX( 1 );
                            setRotationY( 0 );
                            animate().setUpdateListener( null );
                        }
                    } ).start();
                }
            } ).start();
        }
        //##################################
        //
        // Show the image we want
        // Halfway through, the views
        // will flip
        //
        //##################################/
        else
        {
            rot = ObjectAnimator.ofFloat( tile, "alpha", .99f, 1f );
            rot.setDuration( DURATION );
            
            setScaleX( 1 );
            setRotationY( 0 );
            animate().rotationY( 90 ).setStartDelay( startDelay );
            animate().setDuration( DURATION / 2 ).setInterpolator( new LinearInterpolator() );
            animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    // swap the views
                    cardFlipped = true;
                    tileNum = card[ 1 ];
                    // Reverse the image so it looks correct
                    setBackgroundResource( tileNum );
                    
                    //
                    setRotationY( 90 );
                    setScaleX( -1 );
                    animate().rotationY( 180 );
                    animate().setDuration( DURATION / 2 ).setInterpolator( new LinearInterpolator() );
                    animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            if ( getScaleX() != -1 )
                            {
                                setScaleX( -1 );
                            }
                        }
                    } );
                    animate().withEndAction( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setScaleX( 1 );
                            setRotationY( 0 );
                            animate().setUpdateListener( null );
                        }
                    } ).start();
                }
            } ).start();
        }
        
        //
        rot.start();
        
        return rot;
    }
    
    
    /**
     * //##############################
     * <p>
     * Flip the coin slightly
     * <p>
     * //##############################
     */
    public ObjectAnimator slideTo( float y, float x, int DURATION, TimeInterpolator interpolator )
    {
        ObjectAnimator obj;
        float          transY;
        float          transX;
        
        //
        transX = x;
        transY = y;
        
        setPivotX( getWidth() / 2f );
        setPivotY( getHeight() / 2f );
        
        //
        obj = ObjectAnimator.ofFloat( this, "alpha", .99f, 1f );
        obj.setDuration( DURATION );
        
        
        // Animate the item going to where it is housed
        animate().translationX( transX ).scaleX( .1f );
        animate().translationY( transY ).scaleY( .1f );
        
        if ( interpolator == null )
        {
            animate().setDuration( DURATION ).setInterpolator( new AccelerateInterpolator() );
        }
        else
        {
            animate().setDuration( DURATION ).setInterpolator( interpolator );
        }
        
        animate().start();
        
        obj.start();
        
        return obj;
    }
    
    
    /**
     * //##############################
     * <p>
     * Take tile to it's target holder
     * <p>
     * //##############################
     *
     * @param DURATION
     * @param interpolator
     *
     * @return
     */
    public ObjectAnimator gotoTarget( int DURATION, int DELAY, TimeInterpolator interpolator )
    {
        ObjectAnimator obj;
        
        setPivotX( getWidth() / 2f );
        setPivotY( getHeight() / 2f );
        
        //
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.2f, .5f );
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.2f, .5f );
        obj = ObjectAnimator.ofPropertyValuesHolder( this, sx, sy );
        obj.setInterpolator( new LinearInterpolator() );
        
        //
        obj.setDuration( DURATION );
        obj.setStartDelay( DELAY );
        
        if ( interpolator == null )
        {
            animate().setInterpolator( new LinearInterpolator() );
        }
        else
        {
            animate().setInterpolator( interpolator );
        }
        
        animate().setStartDelay( DELAY );
        animate().setDuration( DURATION ).translationX( 0 ).translationY( 0 ).start();
        obj.start();
        
        return obj;
    }
    
    
    /**
     * //############################
     * <p>
     * A custom setter for swoopIn
     * <p>
     * //############################
     *
     * @param newLoc
     */
    public void setLocation( PointLocations newLoc )
    {
        setTranslationX( newLoc.pointX );
        setTranslationY( newLoc.pointY );
    }
    
    
    /**
     * //############################
     * <p>
     * Have tiles swoop with a curve
     * to where they are collected
     * <p>
     * //############################
     *
     * @return
     */
    public ObjectAnimator swoopIn( int DURATION, int deltaX, int deltaY, int swoopAmount )
    {
        PropertyValuesHolder sx, sy;
        final ObjectAnimator anim;
        final ObjectAnimator scale;
        
        // Set up path to new location using a Bï¿½zier spline curve
        AnimatorPath path = new AnimatorPath();
        path.moveTo( -deltaX, -deltaY );
        path.curveTo( -(deltaX / 2f), -deltaY, 0, -deltaY / 2f, 0, 0 );
        
        //
        sx = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.2f, .25f );
        sy = PropertyValuesHolder.ofFloat( "scaleY", 1, 1.2f, .25f );
        scale = ObjectAnimator.ofPropertyValuesHolder( this, sx, sy );
        scale.setDuration( DURATION ).setInterpolator( new LinearInterpolator() );
        scale.start();
        
        //
        anim = ObjectAnimator.ofObject( this, "location", new AnimateOnPath( swoopAmount ), path.getPoints().toArray() );
        anim.setInterpolator( new AccelerateInterpolator() );
        anim.setDuration( DURATION );
        anim.start();
        
        return anim;
    }
    
    
    /**
     * //##############################
     * <p>
     * Free up memory
     * <p>
     * //##############################
     */
    @Override
    protected void onDetachedFromWindow()
    {
        context = null;
        valueAnimator = null;
        animator = null;
        onAnimationCompleteListener = null;
        //
        if ( specialLayer != null )
        {
            Arrays.fill( specialLayer, null );
        }
        
        specialLayer = null;
/*
        blockerCount = 0;
        blockerType = 0;
        blockerImages = null;
        blockerResIds = null;
*/
        
        super.onDetachedFromWindow();
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
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.BoardTile );
        TypedArray arrayList;
        int[]      array;
        
        //
        // Attributes
        //
        int id = a.getResourceId( R.styleable.BoardTile_blockerList, 0 );
        if ( id != 0 )
        {
            arrayList = getResources().obtainTypedArray( id );
            array = new int[ arrayList.length() ];
            
            //
            for ( int i = 0; i < arrayList.length(); i++ )
            {
                array[ i ] = arrayList.getResourceId( i, 0 );
            }
            
            // Apply the images
            setBlockerImages( array );
            arrayList.recycle();
        }
        
        
        //
        // Attributes: debug
        //
        id = a.getResourceId( R.styleable.BoardTile_specialList, 0 );
        if ( id != 0 )
        {
            arrayList = getResources().obtainTypedArray( id );
            array = new int[ arrayList.length() ];
            
            //
            for ( int i = 0; i < arrayList.length(); i++ )
            {
                array[ i ] = arrayList.getResourceId( i, 0 );
            }
            
            // Apply the images
            setSpecialLayer( array );
            arrayList.recycle();
        }
        
        //
        // Get the type
        //
        if ( a.hasValue( R.styleable.BoardTile_blockerType ) )
        {
            setBlockerType( a.getInt( R.styleable.BoardTile_blockerType, 0 ) );
        }
        
        if ( a.hasValue( R.styleable.BoardTile_xAdj ) )
        {
            xAdj = a.getDimensionPixelSize( R.styleable.BoardTile_xAdj, 0 );
        }
        if ( a.hasValue( R.styleable.BoardTile_yAdj ) )
        {
            yAdj = a.getDimensionPixelSize( R.styleable.BoardTile_yAdj, 0 );
        }
        
        //
        // Get the sweeper
        //
        if ( a.hasValue( R.styleable.BoardTile_sweeper ) )
        {
            specialItem = a.getInt( R.styleable.BoardTile_sweeper, 0 );
        }
        
        // Clean up
        a.recycle();
    }
    
}
