package com.genesyseast.coinconnection.CustomControls;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.ShowCase.ShowClass;

import java.util.ArrayList;

public class HighLighter
        extends FrameLayout
        implements View.OnTouchListener
{
    static class HLItem
    {
        HLRectF r;
        View    target;
        int     showType;
        int     expandDir;
        boolean isInflated;
        
        public HLItem( RectF r, View target, int showType, int expandDir )
        {
            this.r = new HLRectF( r );
            this.target = target;
            this.showType = showType;
            this.expandDir = expandDir;
            this.isInflated = false;
        }
    }
    
    
    static class HLRectF
            extends RectF
    {
        public HLRectF()
        {
        }
        
        public HLRectF( float left, float top, float right, float bottom )
        {
            super( left, top, right, bottom );
        }
        
        public HLRectF( @Nullable RectF r )
        {
            super( r );
        }
        
        public HLRectF( @Nullable Rect r )
        {
            super( r );
        }
        
        public void setTop( float top )
        {
            this.top = top;
        }
        
        public void setBottom( float bottom )
        {
            this.bottom = bottom;
        }
        
        public void setRight( float right )
        {
            this.right = right;
        }
        
        public void setLeft( float left )
        {
            this.left = left;
        }
    }
    
    
    private             ArrayList<HLItem> targets;
    private             Bitmap            showCaseBmp;
    private             Canvas            showCaseCanvas;
    private             int               maskColor       = 0xAF000000;
    private             Paint             borderColor;
    private             boolean           isActive        = false;
    private             boolean           closeOnTouch    = true;
    private             boolean           maskSet         = false;
    private             Paint             paint;
    private             int               padding;
    private             int               targetsActive   = 0;
    //
    public static final int               CIRCLE          = 0;
    public static final int               RECTANGLE       = 1;
    public static final int               ROUNDED_RECT    = 2;
    public static final int               BORDER          = 3;
    public static final int               OVAL            = 4;
    //
    public static final int               CENTER_OUT      = 0;
    public static final int               LEFT_DOWN       = 1;
    public static final int               CENTER_OUT_VERT = 2;
    public static final int               CENTER_OUT_HORZ = 3;
    
    
    public HighLighter( Context context )
    {
        super( context );
        init( context );
    }
    
    public HighLighter( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        init( context );
    }
    
    public HighLighter( Context context, @Nullable AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        init( context );
    }
    
    
    /**
     * //##################################
     * <p>
     * Close this crap
     * <p>
     * //##################################
     */
    public void endHighlight()
    {
        showCaseBmp.eraseColor( 0x00000000 );
        isActive = false;
        maskSet = false;
        targets.clear();
        
        //        setVisibility( INVISIBLE );
    }
    
    
    /**
     * //##################################
     * <p>
     * All code in one place
     * <p>
     * //##################################
     *
     * @param context
     */
    private void init( Context context )
    {
        DisplayMetrics metrics = new DisplayMetrics();
        (( Activity ) context).getWindowManager().getDefaultDisplay().getMetrics( metrics );
        
        int height = metrics.heightPixels;
        int width  = metrics.widthPixels;
        
        // Prepare eraser Paint if needed
        this.paint = new Paint();
        this.paint.setColor( 0xFF000000 );
        this.paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
        this.paint.setFlags( Paint.ANTI_ALIAS_FLAG );
        
        //
        this.borderColor = new Paint();
        this.borderColor.setColor( 0xFF00FF00 );
        this.borderColor.setStyle( Paint.Style.STROKE );
        this.borderColor.setStrokeJoin( Paint.Join.ROUND );
        this.borderColor.setStrokeWidth( 4 );
        this.borderColor.setFlags( Paint.ANTI_ALIAS_FLAG );
        
        setBackgroundColor( 0x00000000 );
        showCaseBmp = Bitmap.createBitmap( width, height, Bitmap.Config.ARGB_8888 );
        showCaseCanvas = new Canvas( showCaseBmp );
        
        // Padding to expand
        this.padding = context.getResources().getDimensionPixelSize( R.dimen._3sdp );
        
        // Masking flag
        this.maskSet = false;
        
        // List array
        targets = new ArrayList<>();
    }
    
    
    /**
     * //################################
     * <p>
     * Return the number of targets
     * <p>
     * //################################
     *
     * @return
     */
    public int getTargetsCount()
    {
        if ( targets != null )
        {
            return targets.size();
        }
        
        return 0;
    }
    
    
    /**
     * //################################
     * <p>
     * Return an element
     * <p>
     * //################################
     *
     * @param index
     *
     * @return
     */
    public View getTarget( int index )
    {
        if ( targets != null && index < targets.size() )
        {
            return targets.get( index ).target;
        }
        
        return null;
    }
    
    
    /**
     * //################################
     * <p>
     * Remove the selected target
     * <p>
     * //################################
     *
     * @param index
     */
    public void removeTarget( int index )
    {
        if ( index < targets.size() )
        {
            // Restore the mask color for the removed target
            refillTargetSpace( targets.get( index ) );
            
            // Remove the target
            targets.remove( index );
            
            // Draw the refreshed display
            invalidate();
            
            // Allow mask to refresh and redraw
            //            this.maskSet = false;
        }
    }
    
    
    public HighLighter setCloseOnTouch( boolean closeOnTouch )
    {
        this.closeOnTouch = closeOnTouch;
        
        return this;
    }
    
    
    public HighLighter setMaskColor( int maskColor )
    {
        this.maskColor = 0xAF000000 | (maskColor & 0x00FFFFFF);
        
        // Allow mask to refresh and redraw
        this.maskSet = false;
        
        return this;
    }
    
    
    public HighLighter setBorderColor( int borderColor )
    {
        this.borderColor.setColor( borderColor );
        
        return this;
    }
    
    
    /**
     * //#################################
     * <p>
     * Add a target to the list
     * <p>
     * //#################################
     *
     * @param target
     * @param showType
     * @param expandDirection
     *
     * @return
     */
    public HighLighter addTarget( View target, int showType, int expandDirection )
    {
        if ( targets != null && target != null )
        {
            boolean isPresent = false;
            
            // No duplicates
            for ( HLItem item : targets )
            {
                if ( item.target == target )
                {
                    isPresent = true;
                    break;
                }
            }
            
            
            if ( !isPresent )
            {
                RectF r = new RectF( target.getLeft(), target.getTop(), target.getRight(), target.getBottom() );
                
                targets.add( new HLItem( r, target, showType, expandDirection ) );
                
                // Allow mask to refresh and redraw
                //                this.maskSet = false;
            }
        }
        
        return this;
    }
    
    
    /**
     * //#################################
     * <p>
     * Add a RECT as a target to the list
     * <p>
     * //#################################
     *
     * @param r
     * @param showType
     * @param expandDirection
     *
     * @return
     */
    public HighLighter addTarget( RectF r, int showType, int expandDirection )
    {
        if ( r != null )
        {
            boolean isPresent = false;
            
            // No duplicates
            for ( HLItem item : targets )
            {
                if ( item.r.left == r.left && item.r.right == r.right && item.r.top == r.top && item.r.bottom == r.bottom )
                {
                    isPresent = true;
                    break;
                }
            }
            
            
            if ( !isPresent )
            {
                targets.add( new HLItem( r, null, showType, expandDirection ) );
                
                // Allow mask to refresh and redraw
                //this.maskSet = false;
            }
        }
        
        return this;
    }
    
    
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        if ( closeOnTouch && targetsActive <= 0 )
        {
            endHighlight();
        }
        
        return false;
    }
    
    /**
     * //############################
     * <p>
     * Start the show
     * <p>
     * //############################
     */
    public void run()
    {
        
        for ( final HLItem caseItem : targets )
        {
            ObjectAnimator show;
            int[]          xyPosi = new int[ 2 ];
            
            isActive = true;
            
            
            //########################################
            //
            // Use Rect: Always animated
            //
            //########################################
            if ( caseItem.target == null && caseItem.r != null && !caseItem.isInflated )
            {
                final float w = (caseItem.r.right - caseItem.r.left);
                final float h = (caseItem.r.bottom - caseItem.r.top);
                
                // Let the class know a target is active
                targetsActive++;
                
                show = getExpandDirection( caseItem, w, h );
                //
                
                show.setDuration( 250 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                show.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        // Show the box expanding
                        postInvalidate();
                    }
                } );
                show.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        caseItem.isInflated = true;
                        targetsActive--;
                    }
                } );
                show.start();
                
            }
            //#############################
            //
            // Standard: Always animated
            //
            //#############################
            else if ( caseItem.target != null && caseItem.target.getVisibility() == View.VISIBLE && !caseItem.isInflated )
            {
                final float w = caseItem.target.getWidth();
                final float h = caseItem.target.getHeight();
                
                //
                targetsActive++;
                
                // Get actual screen coordinates
                caseItem.target.getLocationOnScreen( xyPosi );
                
                //
                caseItem.r.left = xyPosi[ 0 ] - padding;
                caseItem.r.right = xyPosi[ 0 ] + w + padding;
                caseItem.r.top = xyPosi[ 1 ] - padding;
                caseItem.r.bottom = xyPosi[ 1 ] + h + padding;
                
                //
                show = getExpandDirection( caseItem, w, h );
                //
                show.setDuration( 250 ).setInterpolator( new AccelerateDecelerateInterpolator() );
                show.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                {
                    @Override
                    public void onAnimationUpdate( ValueAnimator animation )
                    {
                        Log.d( "Poop", "Right: " + caseItem.r.right + ", Bottom: " + caseItem.r.bottom );
                        // Show box expanding
                        postInvalidate();
                    }
                } );
                show.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        caseItem.isInflated = true;
                        targetsActive--;
                    }
                } );
                show.start();
            }
        }
    }
    
    
    /**
     * //############################
     * <p>
     * Determine what expand
     * animation is used
     * <p>
     * //############################
     *
     * @param caseItem
     *
     * @return
     */
    private ObjectAnimator getExpandDirection( HLItem caseItem, float w, float h )
    {
        
        PropertyValuesHolder rl, rt;
        PropertyValuesHolder rr, rb;
        
        if ( caseItem.expandDir == CENTER_OUT_HORZ )
        {
/*
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left + (w / 2f), caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right - (w / 2f), caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top, caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom, caseItem.r.bottom );
*/
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left + (w / 4f), caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right - (w / 4f), caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top, caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom, caseItem.r.bottom );
        }
        else if ( caseItem.expandDir == LEFT_DOWN )
        {
/*
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left, caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.left, caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top, caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.top , caseItem.r.bottom );
*/
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left, caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right - (w / 4f), caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top, caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom - (h / 4f), caseItem.r.bottom );
        }
        else if ( caseItem.expandDir == CENTER_OUT_VERT )
        {
/*
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left, caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right, caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top + (h / 2f), caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom - (h / 2f), caseItem.r.bottom );
*/
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left, caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right, caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top + (h / 4f), caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom - (h / 4f), caseItem.r.bottom );
        }
        else
        {  // CENTER_OUT
/*
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left + (w / 2f), caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right - (w / 2f), caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top + (h / 2f), caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom - (h / 2f), caseItem.r.bottom );
*/
            rl = PropertyValuesHolder.ofFloat( "left", caseItem.r.left + (w / 4f), caseItem.r.left );
            rr = PropertyValuesHolder.ofFloat( "right", caseItem.r.right - (w / 4f), caseItem.r.right );
            rt = PropertyValuesHolder.ofFloat( "top", caseItem.r.top + (h / 4f), caseItem.r.top );
            rb = PropertyValuesHolder.ofFloat( "bottom", caseItem.r.bottom - (h / 4f), caseItem.r.bottom );
            
        }
        
        return ObjectAnimator.ofPropertyValuesHolder( caseItem.r, rl, rt, rr, rb );
    }
    
    
    /**
     * //################################
     * <p>
     * Draw what the user wants
     * <p>
     * //################################
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw( Canvas canvas )
    {
        super.onDraw( canvas );
        
        if ( isActive )
        {
            // Clear the screen back full mask
            if ( !maskSet )
            {
                showCaseBmp.eraseColor( maskColor );
                maskSet = true;
            }
            
            //##################################
            //
            //
            //
            //##################################
            for ( HLItem item : targets )
            {
                // Cutout the RoundRect from THIS view--ShowCase parent
                switch ( item.showType )
                {
                    //
                    case ROUNDED_RECT: showCaseCanvas.drawRoundRect( (item).r, 16, 16, paint );
                        break;
                    //
                    case RECTANGLE: showCaseCanvas.drawRect( item.r, paint );
                        break;
                    //
                    case BORDER:
                        // Adjust the Border so it does not leave dark any parts
                        item.r.left -= padding;
                        item.r.top -= padding;
                        item.r.right += padding;
                        item.r.bottom += padding;
                        //
                        showCaseCanvas.drawRoundRect( item.r, 4, 4, borderColor );
                        break;
                    //
                    case OVAL:
                        //
                        // Expand the region for these two ONLY
                        item.r.left -= padding / 2f;
                        item.r.top -= padding / 2f;
                        item.r.right += padding / 2f;
                        item.r.bottom += padding / 2f;
                        
                        // Adjust the OVAL so it does not leave dark any parts
                        showCaseCanvas.drawOval( item.r, paint );
                        break;
                    // Default is a circle
                    default:
                        float width = item.r.width();
                        float height = item.r.height();
                        float radius = height;
                        
                        if ( width > height )
                        {
                            radius = width;
                        }
                        
                        radius /= 2f;
                        showCaseCanvas.drawCircle( item.r.left + radius, item.r.top + radius, radius, paint );
                        break;
                }
            }
            
            // Draw ONE TIME, IDIOT!!!
            canvas.drawBitmap( showCaseBmp, 0, 0, null );
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Restore the maskcolor for a
     * removed target. Helps with flicker
     * <p>
     * //################################
     *
     * @param item
     */
    private void refillTargetSpace( HLItem item )
    {
        if ( isActive )
        {
            // Save the paint's erase color
            int oldColor = paint.getColor();
            
            // Set the erase color as a mask color
            paint.setColor( maskColor );
            paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.SRC ) );
            
            // Adjust the restore size to
            // prevent missing data
            item.r.left -= padding;
            item.r.top -= padding;
            item.r.right += padding;
            item.r.bottom += padding;
            
            
            //##################################
            //
            //
            //
            //##################################
            // Cutout the RoundRect from THIS view--ShowCase parent
            switch ( item.showType )
            {
                //
                case ROUNDED_RECT: showCaseCanvas.drawRoundRect( item.r, 16, 16, paint );
                    break;
                //
                case RECTANGLE: showCaseCanvas.drawRect( item.r, paint );
                    break;
                //
                case BORDER:
                    // Adjust the Border so it does not leave dark any parts
                    item.r.left -= padding;
                    item.r.top -= padding;
                    item.r.right += padding;
                    item.r.bottom += padding;
                    //
                    showCaseCanvas.drawRoundRect( item.r, 4, 4, borderColor );
                    break;
                //
                case OVAL:
                    //
                    // Expand the region for these two ONLY
                    item.r.left -= padding / 2f;
                    item.r.top -= padding / 2f;
                    item.r.right += padding / 2f;
                    item.r.bottom += padding / 2f;
                    
                    // Adjust the OVAL so it does not leave dark any parts
                    showCaseCanvas.drawOval( item.r, paint );
                    break;
                // Default is a circle
                default:
                    float width = item.r.width();
                    float height = item.r.height();
                    float radius = height;
                    
                    if ( width > height )
                    {
                        radius = width;
                    }
                    
                    radius /= 2f;
                    showCaseCanvas.drawCircle( item.r.left + radius, item.r.top + radius, radius, paint );
                    break;
            }
            
            // Restore the erase color as a mask color
            paint.setColor( oldColor );
            paint.setXfermode( new PorterDuffXfermode( PorterDuff.Mode.CLEAR ) );
        }
    }
    
    
    /**
     * //################################
     * <p>
     * Memory cleaning
     * <p>
     * //################################
     */
    @Override
    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        
        targets.clear();
        targets = null;
        showCaseBmp.recycle();
        showCaseBmp = null;
        showCaseCanvas = null;
        paint = null;
    }
}
    
    
