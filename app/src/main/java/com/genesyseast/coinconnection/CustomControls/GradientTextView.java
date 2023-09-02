package com.genesyseast.coinconnection.CustomControls;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.text.AllCapsTransformationMethod;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.PrecomputedTextCompat;

import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;

import java.util.ArrayList;
import java.util.List;

public class GradientTextView
        extends AppCompatTextView
        implements View.OnTouchListener
{
    public static final int            HORIZONTAL        = 0;
    public static final int            VERTICAL          = 1;
    private             Context        context;
    private             int[]          colorList;
    private             int[]          strokeColorList;
    private             int[]          strokeLayers;
    private             int            gradient;
    private             int            gradientDirection = 0;
    private             int            strokeWidth;
    private             int            strokeColor;
    private             int[]          colors            = new int[ 2 ];
    private             LinearGradient linearGradient;
    private             Rect           bounds;
    private             LinearGradient strokeGradient    = null;
    private             boolean        underLineText     = false;
    private             boolean        isButton          = false;
    private             int            drawableHeight;
    private             int            drawableWidth;
    //
    private             float          shadowRadius;
    private             float          shadowDx;
    private             float          shadowDy;
    private             int            shadowColor;
    
    /**
     * //#################################
     * <p>
     * Constructors
     * <p>
     * //#################################
     *
     * @param context
     */
    public GradientTextView( Context context )
    {
        super( context );
        
        setOnTouchListener( this );
    }
    
    public GradientTextView( Context context, @Nullable AttributeSet attrs )
    {
        super( context, attrs );
        readAttr( context, attrs );
        getGradient();
        getStrokeGradient();
        
        setOnTouchListener( this );
    }
    
    public GradientTextView( Context context, @Nullable AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        readAttr( context, attrs );
        getGradient();
        getStrokeGradient();
        
        setOnTouchListener( this );
    }
    
    
    /**
     * //#################################
     * <p>
     * Setters
     * <p>
     * //#################################
     */
    public void setStartColor( int startColor )
    {
        colors[ 0 ] = startColor;
        getGradient();
    }
    
    public void setEndColor( int endColor )
    {
        colors[ 1 ] = endColor;
        getGradient();
    }
    
    public void setButton( boolean button )
    {
        isButton = button;
    }
    
    /**
     * //#################################
     * <p>
     * For INT arrays
     * <p>
     * //#################################
     *
     * @param colorsArray
     */
    public void setColorList( int[] colorsArray )
    {
        this.colorList = colorsArray;
    }
    
    
    /**
     * //#################################
     * <p>
     * For res ID of string data
     * <p>
     * //#################################
     *
     * @param colorsArrayId
     */
    public void setColorList( int colorsArrayId )
    {
        //        this.colorList = colorList;
        
        String[] nums2str = getResources().getStringArray( colorsArrayId );
        this.colorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        
        if ( colorList != null )
        {
            getGradient();
        }
    }
    
    
    /**
     * //#################################
     * <p>
     * Enable text underlining
     * <p>
     * //#################################
     *
     * @param underLineText
     */
    public void setUnderLineText( boolean underLineText )
    {
        this.underLineText = underLineText;
        
        if ( underLineText )
        {
            setPaintFlags( getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
        }
    }
    
    public void setGradientDirection( int gradientDirection )
    {
        this.gradientDirection = gradientDirection;
    }
    
    public void setGradient( boolean gradient )
    {
        if ( gradient )
        {
            this.gradient = 1;
        }
        else
        {
            this.gradient = 0;
        }
    }
    
    public void setStrokeWidth( int strokeWidth )
    {
        this.strokeWidth = strokeWidth;
    }
    
    
    public void setStrokeColor( int strokeColor )
    {
        this.strokeColor = strokeColor;
    }
    
    
    /**
     * //#################################
     * <p>
     * For font stroke color
     * <p>
     * //#################################
     *
     * @param colorsArray
     */
    public void setStrokeColorList( int[] colorsArray )
    {
        this.strokeColorList = colorsArray;
    }
    
    public void setStrokeColorList( int colorsArrayId )
    {
        
        String[] nums2str = getResources().getStringArray( colorsArrayId );
        
        this.strokeColorList = new int[ nums2str.length ];
        
        for ( int i = 0; i < nums2str.length; i++ )
        {
            strokeColorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
        }
        
        
        if ( strokeColorList != null )
        {
            getStrokeGradient();
        }
    }
    
    
    /**
     * //#################################
     * <p>
     * Set text string data
     * <p>
     * //#################################
     *
     * @param text
     * @param type
     */
    @Override
    public void setText( CharSequence text, BufferType type )
    {
        super.setText( text, type );
        
        getGradient();
    }
    
    
    @Override
    public void setShadowLayer( float radius, float dx, float dy, int color )
    {
        int size = getResources().getDimensionPixelSize( R.dimen._1sdp );
        this.shadowRadius = radius * size;
        this.shadowDx = dx * size;
        this.shadowDy = dy * size;
        this.shadowColor = color;
        
        super.setShadowLayer( this.shadowRadius, this.shadowDx, this.shadowDy, color );
    }
    
    
    @Override
    public void setCompoundDrawablesRelative( @Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom )
    {
        //        int size = getResources().getDimensionPixelSize( R.dimen._24sdp );
        
        if ( drawableWidth > 0 && drawableHeight > 0 )
        {
            if ( start != null )
            {
                start.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( end != null )
            {
                end.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( top != null )
            {
                top.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
            
            if ( bottom != null )
            {
                bottom.setBounds( 0, 0, drawableWidth, drawableHeight );
            }
        }
        
        super.setCompoundDrawablesRelative( start, top, end, bottom );
    }
    
    
    /**
     * //#################################
     * <p>
     * Used mainly for button press
     * animations
     * <p>
     * //#################################
     *
     * @param v
     * @param event
     *
     * @return
     */
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        if ( isButton )
        {
            switch ( event.getAction() )
            {
                case MotionEvent.ACTION_DOWN:
                {
                    // Overlay is black with transparency of 0x77 (119)
                    if ( getTag() != null && getTag() instanceof ObjectAnimator )
                    {
                        ObjectAnimator obj = ( ObjectAnimator ) getTag();
                        obj.pause();
                    }
                    
                    setScaleX( .8f );
                    setScaleY( .8f );
                    //                                        getBackground().setColorFilter( 0x3F000000, PorterDuff.Mode.DARKEN );
                    invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                {
                    // Clear the overlay
                    if ( getTag() != null && getTag() instanceof ObjectAnimator )
                    {
                        ObjectAnimator obj = ( ObjectAnimator ) getTag();
    
                        if ( obj.isPaused() )
                        {
                            obj.resume();
                        }
                    }
                    
                    animate().setDuration( 350 );
                    animate().scaleX( 1f ).scaleY( 1f ).setInterpolator( new CustomBounceInterpolator( .2, 20 ) ).start();
                    
                    //                                        getBackground().clearColorFilter();
                    invalidate();
                    break;
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * //#################################
     * <p>
     * Build gradient color mapping
     * <p>
     * //#################################
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        super.onLayout( changed, left-3, top-3, right+3, bottom+3 );
        
        getGradient();
        getStrokeGradient();
    }
    
    
    /**
     * //#################################
     * <p>
     * Helper method for gradient effect
     * <p>
     * //#################################
     */
    private void getGradient()
    {
        // Get the actual text bounds for gradient drawing
        Paint  p;
        String s = getText().toString();
        int    baseline;
        
        p = this.getPaint();
        bounds = new Rect();
        
        
        //##############################
        //
        // Split the textview strings
        //
        //##############################
        List<CharSequence> lines = new ArrayList<>();
        int                count = getLineCount();
        for ( int line = 0; line < count; line++ )
        {
            int          start     = getLayout().getLineStart( line );
            int          end       = getLayout().getLineEnd( line );
            CharSequence substring = getText().subSequence( start, end );
            lines.add( substring );
            //
            if ( line > 3 )
            {
                break;
            }
        }
        
        // how many lines do we have
        int longest  = 0;
        int curWidth = 0;
        
        for ( int i = 0; i < lines.size(); i++ )
        {
            if ( p.measureText( lines.get( i ).toString() ) > curWidth )
            {
                longest = i;
                curWidth = ( int ) p.measureText( lines.get( i ).toString() );
            }
        }
        
        //
        if ( lines.size() > 0 )
        {
            s = lines.get( longest ).toString();
        }
        
        // Check for ALL CAPS
        TransformationMethod transformationMethod = getTransformationMethod();
        if ( transformationMethod != null )
        {
            if ( transformationMethod.getClass().getSimpleName().equalsIgnoreCase( AllCapsTransformationMethod.class.getSimpleName() ) )
            {
                s = s.toUpperCase();
            }
        }
        
        
        //#########################################
        //
        // Get the pixel width of the text string
        //
        //#########################################
        p.getTextBounds( s, 0, s.length(), bounds );
        baseline = getBaseline();
        
        // Adjust the bound information and translate to canvas coordinates
        bounds.top = baseline + bounds.top;
        bounds.bottom = baseline + bounds.bottom;
        
        int width   = getWidth();
        int gravity = getGravity();//(getGravity() & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK));
        
        // Determine where the gradient starts and ends
        if ( (gravity & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK)) == Gravity.CENTER )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() ));
        }
        else
        {
            bounds.left += getPaddingLeft();
        }
        
        //
        bounds.right = ( int ) p.measureText( s, 0, s.length() );
        bounds.right = (bounds.right + bounds.left);
        
        
        //
        if ( colorList != null )
        {
            if ( gradientDirection == 0 )
            {
                linearGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, colorList, null, Shader.TileMode.CLAMP );
            }
            else
            {
                linearGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, colorList, null, Shader.TileMode.CLAMP );
            }
        }
        else if ( colors != null )
        {
            if ( gradientDirection == 0 )
            {
                linearGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, colors, null, Shader.TileMode.CLAMP );
            }
            else
            {
                linearGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, colors, null, Shader.TileMode.CLAMP );
            }
        }
        
        if ( gradient > 0 )
        {
            getPaint().setShader( linearGradient );
        }
        
    }
    
    
    /**
     * //#################################
     * <p>
     * Helper method for gradient effect
     * <p>
     * //#################################
     */
    private void getStrokeGradient()
    {
        // Get the actual text bounds for gradient drawing
        Paint  p;
        String s = getText().toString();
        int    baseline;
        
        p = this.getPaint();
        bounds = new Rect();
        
        
        //##############################
        //
        // Split the textview strings
        //
        //##############################
        List<CharSequence> lines = new ArrayList<>();
        int                count = getLineCount();
        for ( int line = 0; line < count; line++ )
        {
            int          start     = getLayout().getLineStart( line );
            int          end       = getLayout().getLineEnd( line );
            CharSequence substring = getText().subSequence( start, end );
            lines.add( substring );
            //
            if ( line > 3 )
            {
                break;
            }
        }
        
        // how many lines do we have
        int longest  = 0;
        int curWidth = 0;
        
        for ( int i = 0; i < lines.size(); i++ )
        {
            if ( p.measureText( lines.get( i ).toString() ) > curWidth )
            {
                longest = i;
                curWidth = ( int ) p.measureText( lines.get( i ).toString() );
            }
        }
        
        //
        if ( lines.size() > 0 )
        {
            s = lines.get( longest ).toString();
        }
        
        // Check for ALL CAPS
        TransformationMethod transformationMethod = getTransformationMethod();
        if ( transformationMethod != null )
        {
            if ( transformationMethod.getClass().getSimpleName().equalsIgnoreCase( AllCapsTransformationMethod.class.getSimpleName() ) )
            {
                s = s.toUpperCase();
            }
        }
        
        
        //#########################################
        //
        // Get the pixel width of the text string
        //
        //#########################################
        p.getTextBounds( s, 0, s.length(), bounds );
        baseline = getBaseline();
        
        // Adjust the bound information and translate to canvas coordinates
        bounds.top = baseline + bounds.top + strokeWidth;
        bounds.bottom = baseline + bounds.bottom - strokeWidth;
        
        int width   = getWidth();
        int gravity = getGravity();//(getGravity() & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK));
        
        // Determine where the gradient starts and ends
        if ( (gravity & (Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK)) == Gravity.CENTER )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() )) / 2;
        }
        else if ( (getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT )
        {
            bounds.left += (width - ( int ) p.measureText( s, 0, s.length() ));
        }
        else
        {
            bounds.left += getPaddingLeft();
        }
        
        //
        bounds.left -= +strokeWidth;
        bounds.right = ( int ) p.measureText( s, 0, s.length() );
        bounds.right = (bounds.right + bounds.left) + strokeWidth;
        
        //
        if ( strokeColorList != null )
        {
            if ( gradientDirection == 0 )
            {
                strokeGradient = new LinearGradient( bounds.left, bounds.top, bounds.right, bounds.bottom, strokeColorList, null, Shader.TileMode.CLAMP );
            }
            else
            {
                strokeGradient = new LinearGradient( 0, bounds.top, 0, bounds.bottom, strokeColorList, null, Shader.TileMode.CLAMP );
            }
        }
    }
    
    
    /**
     * //#################################
     * <p>
     * Overridden onDraw for custom drawing
     * <p>
     * //#################################
     *
     * @param canvas
     */
    @Override
    protected void onDraw( Canvas canvas )
    {
        int    textColor = getCurrentTextColor();
        Shader shader    = getPaint().getShader();
        
        //
        // Clear shader for stroke and shadow
        //
        getPaint().setShader( null );
        
        // Handle shadow layer
        getPaint().setShadowLayer( shadowRadius, shadowDx, shadowDy + (strokeWidth * 1.5f), shadowColor );
        super.onDraw( canvas );
        //        getPaint().clearShadowLayer();
        
        
        // GET THE SHADOW LAYER VALUES TO SAVE
        if ( strokeWidth > 0 )
        {
            getPaint().clearShadowLayer();
            
            // Universal settings
            getPaint().setStrokeJoin( Paint.Join.ROUND );
            getPaint().setStyle( Paint.Style.STROKE );
            
            // If second stroke layer, show here
            if ( strokeLayers != null )
            {
                getPaint().setStrokeWidth( strokeWidth * 1.75f );
                setTextColor( strokeLayers[ 0 ] );
                super.onDraw( canvas );
                
                // Top color
                setTextColor( strokeLayers[ 1 ] );
            }
            else
            {
                setTextColor( strokeColor );
            }
            
            
            // Top most Stroke
            getPaint().setStrokeWidth( strokeWidth );
            super.onDraw( canvas );
        }
        
        //
        getPaint().setStyle( Paint.Style.FILL );
        getPaint().setShader( shader );
        setTextColor( textColor );
        super.onDraw( canvas );
        

/*
        
        if ( bounds != null )
        {
            Layout layout = getLayout();
            
            Paint rectPaint = new Paint();
            rectPaint.setColor( 0xFFFF00FF );
            rectPaint.setStyle( Paint.Style.STROKE );
            rectPaint.setStrokeWidth( 3 );
            canvas.drawRect( bounds, rectPaint );
        }
*/
    }
    
    
    /**
     * //#################################
     * <p>
     * Load custom attributes
     * <p>
     * //#################################
     *
     * @param context
     * @param attrs
     */
    private void readAttr( Context context, AttributeSet attrs )
    {
        TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.GradientTextView );
        
        // Read the colors
        gradientDirection = a.getInt( R.styleable.GradientTextView_gradientOrientation, 0 );
        gradient = a.getInt( R.styleable.GradientTextView_gradient, 0 );
        
        //
        colors[ 0 ] = a.getColor( R.styleable.GradientTextView_startColor, 0xFFFFFFFF ); // White
        colors[ 1 ] = a.getColor( R.styleable.GradientTextView_endColor, 0xFF000000 ); // Black
        //
        strokeColor = a.getColor( R.styleable.GradientTextView_strokeColor, 0 );
        strokeWidth = a.getDimensionPixelSize( R.styleable.GradientTextView_strokeWidth, 0 );
        isButton = a.getBoolean( R.styleable.GradientTextView_isButton, false );
        //
        underLineText = a.getBoolean( R.styleable.GradientTextView_underLineText, false );
        setUnderLineText( underLineText );
        
        //
        drawableHeight = a.getDimensionPixelSize( R.styleable.GradientTextView_drawableHeight, 0 );
        drawableWidth = a.getDimensionPixelSize( R.styleable.GradientTextView_drawableWidth, 0 );
        //
        if ( drawableHeight > 0 && drawableWidth > 0 )
        {
            Drawable[] drawables = getCompoundDrawablesRelative();
            setCompoundDrawablesRelative( drawables[ 0 ], drawables[ 1 ], drawables[ 2 ], drawables[ 3 ] );
        }
        
        
        // Regular gradient
        int id = a.getResourceId( R.styleable.GradientTextView_colorList, 0 );
        if ( id != 0 )
        {
            String[] nums2str = getResources().getStringArray( id );
            colorList = new int[ nums2str.length ];
            
            for ( int i = 0; i < nums2str.length; i++ )
            {
                colorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
            }
        }
        
        
        String strokeLayer = a.getString( R.styleable.GradientTextView_strokeLayers );
        if ( strokeLayer != null )
        {
            String[] list = strokeLayer.split( "," );
            strokeLayers = new int[ list.length ];
            
            //
            //#########################
            //
            for ( int i = 0; i < list.length; i++ )
            {
                strokeLayers[ i ] = ( int ) Long.parseLong( list[ i ].substring( 1 ), 16 );
            }
        }
        
        
        // Stroke Color layers: 2 layer stroke detail
        id = a.getResourceId( R.styleable.GradientTextView_strokeLayers, 0 );
        if ( id != 0 && strokeLayers == null )
        {
            String[] nums2str = getResources().getStringArray( id );
            strokeLayers = new int[ nums2str.length ];
            
            for ( int i = 0; i < nums2str.length; i++ )
            {
                strokeLayers[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
            }
        }
        
        
        // Stroke gradient
        id = a.getResourceId( R.styleable.GradientTextView_strokeColorList, 0 );
        if ( id != 0 )
        {
            String[] nums2str = getResources().getStringArray( id );
            strokeColorList = new int[ nums2str.length ];
            
            for ( int i = 0; i < nums2str.length; i++ )
            {
                strokeColorList[ i ] = ( int ) Long.parseLong( nums2str[ i ].substring( 2 ), 16 );
            }
        }
    
        if ( strokeWidth > 0 )
        {
            setPadding( getPaddingLeft() + strokeWidth, getPaddingTop(), getPaddingRight() + strokeWidth, getPaddingBottom() );
        }
        
        //
        a.recycle();
    }
}
