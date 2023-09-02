package com.genesyseast.coinconnection.Fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.MainActivity;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameCompleted
        extends Fragment
        implements View.OnTouchListener
{
    
    private GameEngine        gameEngine;
    private ArrayList<String> text;
    private View              view_main;
    private LinearLayout      listLayout;
    private ObjectAnimator    credits;
    private long              creditsPosition = 0;
    private ScrollView        listScroll;
    private long              startPlayTime   = 0;
    private boolean           userCanScroll   = false;
    
    public GameCompleted()
    {
        // Required empty public constructor
        gameEngine = GameEngine.getInstance( getContext() );
    }
    
    /**
     * Create the required view
     *
     * @param inflater           N/A
     * @param container          N/A
     * @param savedInstanceState N/A
     *
     * @return N/A
     */
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if ( getContext() != null )
        {
            final int        textSize   = getContext().getResources().getDimensionPixelSize( R.dimen._13ssp );
            final int        strokeSize = getContext().getResources().getDimensionPixelSize( R.dimen._2ssp );
            final Typeface   typeface   = ResourcesCompat.getFont( getContext(), R.font.badaga );
            String           webLink    = null;
            GradientTextView button;
            
            
            view_main = inflater.inflate( R.layout.game_completed_fragment, container, false );
            text = new ArrayList<>();
            
            //
            view_main.setFocusableInTouchMode( true );
            view_main.requestFocus();
            view_main.setOnKeyListener( new View.OnKeyListener()
            {
                @Override
                public boolean onKey( View v, int keyCode, KeyEvent event )
                {
                    if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && userCanScroll )
                    {
                        //
                        if ( getActivity() != null )
                        {
                            //@@@@@@@@@@@@@@@@@@@@@
                            gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
                            
                            gameEngine.soundPlayer.fadeOutMusic().addListener( new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    MainActivity.removeBackStack();
                                    super.onAnimationEnd( animation );
                                }
                            } );
                        }
                        
                        return true;
                    }
                    
                    return true;
                }
            } );
            
            // Get the list view
            //
            listLayout = view_main.findViewById( R.id.infoList );
            listLayout.removeAllViews();
            
            // scrollView
            listScroll = view_main.findViewById( R.id.listScroll );
            
            //
            button = view_main.findViewById( R.id.gameCompleteDone );
            button.setVisibility( View.INVISIBLE );
            button.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if ( getActivity() != null )
                    {
                        //@@@@@@@@@@@@@@@@@@@@@
                        gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
                        
                        gameEngine.soundPlayer.fadeOutMusic().addListener( new AnimatorListenerAdapter()
                        {
                            @Override
                            public void onAnimationEnd( Animator animation )
                            {
                                MainActivity.removeBackStack();
                                super.onAnimationEnd( animation );
                            }
                        } );
                    }
                }
            } );
            
            // Read the data to process
            readTextFile();
            
            //###################################
            //
            // Process the file into sections
            //
            //###################################
            if ( text != null && text.size() > 0 )
            {
                for ( String line : text )
                {
                    //
                    if ( line.toUpperCase().contains( "INFO:" ) )
                    {
                        GradientTextView tv = new GradientTextView( getContext() );
                        String           header;
                        
                        // Actual info
                        tv = new GradientTextView( getContext() );
                        tv.setTypeface( typeface );
                        tv.setGravity( Gravity.CENTER );
                        tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                        tv.setTextColor( Color.WHITE );
                        tv.setTextSize( textSize );
                        
                        Spanned htmlAsSpanned = Html.fromHtml( line.substring( 5 ).trim() );
                        
                        tv.setText( htmlAsSpanned );
                        
                        //                        tv.setText( line.substring( 5 ).trim() );
                        listLayout.addView( tv );
                    }
                    else if ( line.toUpperCase().contains( "HEADER:" ) )
                    {
                        GradientTextView tv = new GradientTextView( getContext() );
                        
                        tv.setTypeface( typeface );
                        tv.setTextSize( textSize );
                        tv.setGravity( Gravity.CENTER );
                        tv.setStrokeWidth( strokeSize );
                        tv.setStrokeColor( Color.BLACK );
                        
                        // Header
                        tv.setBackgroundColor( 0x8F000000 );
                        tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                        tv.setTextColor( Color.WHITE );
                        tv.setText( line.substring( 7 ).trim() );
                        listLayout.addView( tv );
                    }
                    else if ( line.toUpperCase().contains( "WEB:" ) )
                    {
                        webLink = line.substring( 4 ).trim();
                        
                        if ( webLink.toUpperCase().equals( "NONE" ) || !line.contains( "." ) )
                        {
                            webLink = null;
                        }
                    }
                    else if ( line.toUpperCase().contains( "LINK:" ) )
                    {
                        String   linker;
                        TextView tv = new TextView( getContext() );
                        
                        //
                        if ( webLink != null )
                        {
                            String subString = line.substring( 5 ).trim();
                            
                            //
                            if ( subString.length() < 1 )
                            {
                                subString = "Click here";
                            }
                            
                            //
                            tv.setTypeface( typeface );
                            tv.setTextSize( textSize - (textSize / 6f) );
                            tv.setGravity( Gravity.CENTER );
                            tv.setLinkTextColor( 0xFFFFFF00 );
                            tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                            tv.setTextColor( Color.WHITE );
                            
                            linker = String.format( Locale.getDefault(), "\n<a href=\"%s\">%s</a>", webLink, subString );
                            Spanned htmlAsSpanned = Html.fromHtml( linker );
                            //
                            tv.setText( htmlAsSpanned );
                            tv.setMovementMethod( LinkMovementMethod.getInstance() );
                            listLayout.addView( tv );
                            
                            // Erase for next use
                            webLink = null;
                        }
                    }
                    else if ( line.length() > 0 && line.substring( 0 ).contains( "-" ) )
                    {
                        GradientTextView tv        = new GradientTextView( getContext() );
                        String           subString = line.substring( 1 ).trim();
                        
                        tv.setTypeface( typeface );
                        tv.setTextSize( textSize + (textSize / 2f) );
                        tv.setGradient( true );
                        tv.setGradientDirection( GradientTextView.HORIZONTAL );
                        tv.setColorList( R.array.blue_reflection );
                        tv.setGravity( Gravity.CENTER );
                        tv.setBackgroundColor( 0x7F440000 );
                        tv.setStrokeWidth( strokeSize + strokeSize );
                        tv.setStrokeColor( Color.BLACK );
                        tv.setTextColor( Color.WHITE );
                        tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                        
                        tv.setText( subString );
                        if ( subString.length() <= 1 )
                        {
                            tv.setTextSize( textSize / 4f );
                        }
                        listLayout.addView( tv );
                    }
                    else
                    {
                        String           header;
                        GradientTextView tv = new GradientTextView( getContext() );
                        
                        //
                        if ( line.toUpperCase().contains( "SONG:" ) )
                        {
                            header = "Song";
                            tv.setTypeface( typeface );
                            tv.setTextSize( textSize - (textSize / 4f) );
                            tv.setGravity( Gravity.CENTER );
                            tv.setStrokeColor( Color.BLACK );
                            tv.setStrokeWidth( strokeSize );
                            
                            // Header
                            tv.setBackgroundColor( 0x5F000000 );
                            tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                            tv.setTextColor( Color.WHITE );
                            tv.setText( header );
                            listLayout.addView( tv );
                            
                            header = line.substring( 5 ).trim();
                        }
                        else if ( line.toUpperCase().contains( "AUTHOR:" ) )
                        {
                            header = "Author";
                            tv.setTypeface( typeface );
                            tv.setTextSize( textSize + (textSize / 4f) );
                            tv.setGravity( Gravity.CENTER );
                            tv.setTextColor( Color.WHITE );
                            tv.setGradient( true );
                            tv.setGradientDirection( GradientTextView.HORIZONTAL );
                            tv.setColorList( R.array.red_reflection );
                            tv.setText( header );
                            tv.setStrokeColor( Color.BLACK );
                            tv.setStrokeWidth( strokeSize );
                            
                            // Header
                            //tv.setBackgroundColor( 0x8F000000 );
                            tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                            listLayout.addView( tv );
                            
                            //
                            header = line.substring( 7 ).trim();
                        }
                        else if ( line.toUpperCase().contains( "SKIP:" ) )
                        {
                            StringBuilder string = new StringBuilder();
                            int           count  = Integer.parseInt( line.substring( 5 ).trim() );
                            
                            for ( int i = 0; i < count; i++ )
                            {
                                string.append( "\n" );
                            }
                            
                            tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                            
                            //
                            tv.setText( string.toString() );
                            tv.setTextSize( textSize );
                            listLayout.addView( tv );
                            continue;
                        }
                        else
                        {
                            header = line;
                            tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                            tv.setTextSize( textSize );
                            listLayout.addView( tv );
                            continue;
                        }
                        
                        //
                        // Actual data
                        tv = new GradientTextView( getContext() );
                        tv.setTypeface( typeface );
                        tv.setGravity( Gravity.CENTER );
                        tv.setLayoutParams( new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT ) );
                        tv.setTextColor( Color.WHITE );
                        tv.setTextSize( textSize );
                        tv.setText( String.format( Locale.getDefault(), "%s\n", header ) );
                        listLayout.addView( tv );
                    }
                }
                
                //
                //###########################
                //
                gameEngine.soundPlayer.fadeOutMusic().addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        view_main.animate().setUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate( ValueAnimator animation )
                            {
                                if ( view_main.getTag() == null )
                                {
                                    view_main.setTag( 1 );
                                    gameEngine.animatorList.add( animation );
                                }
                            }
                        } );
                        view_main.setAlpha( 0f );
                        view_main.animate().setDuration( 1000 ).alpha( 1f ).withEndAction( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                
                                //###########################
                                //
                                // Animate the credits
                                //
                                //###########################
                                int lines = listLayout.getHeight();
                                credits = ObjectAnimator.ofInt( listScroll, "scrollY", 0, lines );
                                
                                credits.setStartDelay( 1000 );
                                credits.setDuration( lines * 20 );
                                credits.setInterpolator( new LinearInterpolator() );
                                credits.addListener( new Animator.AnimatorListener()
                                {
                                    @Override
                                    public void onAnimationStart( Animator animation )
                                    {
                                        //@@@@@@@@@@@@@@@@@ Play Free Spins sound
                                        if ( gameEngine.soundPlayer != null )
                                        {
                                            gameEngine.soundPlayer.playBgMusic( PlaySound.GAME_COMPLETE_BG_HAPPY_ADVETURE, PlaySound.LOOP );
                                            gameEngine.currentBGMusic = (PlaySound.GAME_COMPLETE_BG_HAPPY_ADVETURE << 2);
                                        }
                                    }
                                    
                                    @Override
                                    public void onAnimationEnd( Animator animation )
                                    {
                                        userCanScroll = true;
                                        FrameLayout scroll = view_main.findViewById( R.id.messageFrame );
                                        
                                        scroll.setVisibility( View.VISIBLE );
                                        scroll.setAlpha( 0f );
                                        scroll.animate().alpha( 1f ).setDuration( 1000 ).start();
                                        
                                        button.setVisibility( View.VISIBLE );
                                        button.setAlpha( 0f );
                                        button.animate().alpha( 1f ).setDuration( 1000 ).start();
                                    }
                                    
                                    @Override
                                    public void onAnimationCancel( Animator animation )
                                    {
                                    
                                    }
                                    
                                    @Override
                                    public void onAnimationRepeat( Animator animation )
                                    {
                                    
                                    }
                                } );
                                gameEngine.animatorList.add( credits );
                                credits.start();
                                view_main.setTag( null );
                            }
                        } ).start();
                    }
                } );
                
                
                // Enable touch pausing / resuming and postioning
                listScroll.setOnTouchListener( this );
            }
        }
        // Blank Credits
        else
        {
            view_main = inflater.inflate( R.layout.game_completed_fragment, container, false );
        }
        
        
        return view_main;
    }
    
    
    /**
     * Process a text file from assets
     */
    private void readTextFile()
    {
        BufferedReader reader = null;
        
        try
        {
            if ( getContext() != null )
            {
                reader = new BufferedReader( new InputStreamReader( getContext().getAssets().open( "credits.txt" ), StandardCharsets.ISO_8859_1 ) );
                
                // do reading, usually loop until end of file reading
                String mLine;
                while ( (mLine = reader.readLine()) != null )
                {
                    if ( mLine.length() > 2 && mLine.substring( 0, 2 ).contains( "//" ) )
                    {
                        continue;
                    }
                    
                    text.add( mLine );
                }
            }
            else
            {
                text = null;
            }
        }
        catch ( IOException e )
        {
            Toast.makeText( getContext(), "Error reading file!", Toast.LENGTH_LONG ).show();
            e.printStackTrace();
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    //log the exception
                }
            }
        }
    }
    
    
    @Override
    public boolean onTouch( View v, MotionEvent event )
    {
        switch ( event.getAction() )
        {
            case MotionEvent.ACTION_DOWN:
                if ( credits.isStarted() )
                {
                    credits.pause();
                    creditsPosition = listScroll.getScrollY();
                }
                
                return true;
            //
            case MotionEvent.ACTION_UP:
                if ( credits.isStarted() )
                {
                    credits.resume();
                }
                
/*
                if ( creditsPosition != listScroll.getScrollY() )
                {
                    creditsPosition = listScroll.getScrollY() * 50;
                    credits.setCurrentPlayTime( creditsPosition );
                }
*/
                
                return true;
            //
            default:
                if ( userCanScroll )
                {
                    return false;
                }
                else
                {
                    return true;
                }
        }
    }
    
    
    @Override
    public void onDestroy()
    {
        gameEngine = null;
        text = null;
        view_main = null;
        listLayout = null;
        credits = null;
        
        super.onDestroy();
    }
}

