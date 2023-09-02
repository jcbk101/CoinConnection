package com.genesyseast.coinconnection.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.CustomControls.BoardTile;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameBoard;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.GameEngine.LogicThread;
import com.genesyseast.coinconnection.GameGraphics.ConnectionsGridLayout;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Variables.PointXYZ;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.VISIBLE;

public class HelpDialog
        extends DialogFragment
        implements View.OnClickListener, DialogInterface.OnDismissListener
{
    private final int                   KILL_THREAD   = 0;
    private final int                   START_THREAD  = 1;
    //
    private       GameEngine            gameEngine;
    private       GameBoard             gameBoard;
    private       View                  view_dialog;
    //
    private       int[]                 coinGrids     = {
            R.array.grid_1, R.array.grid_2, R.array.grid_3, R.array.grid_4, R.array.grid_5, R.array.grid_6
    };
    private       int[]                 gridScripts   = {
            R.string.grid_script_1, R.string.grid_script_2, R.string.grid_script_3, R.string.grid_script_4, R.string.grid_script_5, R.string.grid_script_6
    };
    //
    private       String[]              helpText;
    private       int                   currentGrid   = 0;
    private       ConnectionsGridLayout gridLayout;
    private       ArrayList<BoardTile>  boardTiles;
    private       LogicThread           logicThread;
    private       boolean               scrimEnabled  = true;
    private       ValueAnimator         script;
    private       ValueAnimator         dummy;
    private       ArrayList<Integer>    safetyNet;
    //
    private       OnDismisser           onDismisser;
    private       boolean               userCancelled = false;
    private       boolean               cannotSolve   = false;
    
    
    public interface OnDismisser
    {
        void onDismiss();
    }
    
    /**
     * All other instantiations
     */
    public HelpDialog()
    {
    
    }
    
    public HelpDialog( boolean scrimEnabled )
    {
        this.scrimEnabled = scrimEnabled;
    }
    
    
    public void setOnDismisser( OnDismisser onDismisser )
    {
        this.onDismisser = onDismisser;
    }
    
    
    @Override
    public void dismiss()
    {
        // Kill the thread
        processThread( KILL_THREAD );
        
        userCancelled = true;
        if ( dummy != null )
        {
            dummy.end();
        }
        
        if ( script != null )
        {
            script.end();
        }
        
        //
        gridLayout = null;
        boardTiles = null;
        if ( onDismisser != null )
        {
            onDismisser.onDismiss();
        }
        
        super.dismiss();
    }
    
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        GradientTextView button;
        ImageView        arrow;
        
        
        try
        {
            if ( getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_help_layout, null );
/*
                
                helpText = new String[]{
                        getString( R.string.grid_text_1 ), getString( R.string.grid_text_2 ), getString( R.string.grid_text_3 ), getString( R.string.grid_text_4 ),
                        getString( R.string.grid_text_5 ), getString( R.string.grid_text_6 )
                };
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
                //                gameBoard = GameBoard.getInstance( getContext() );
                
                
                // Main exit button
                button = view_dialog.findViewById( R.id.helpDoneButton );
                button.setOnClickListener( this );
                
                
                // Grid layout
                gridLayout = view_dialog.findViewById( R.id.boardGridLayout );
                gridLayout.setMainView( view_dialog );
                
                
                // Set up box clipping
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                //                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                // Scrim setting
                if ( !scrimEnabled )
                {
                    getDialog().getWindow().setDimAmount( 0f );
                }
                else
                {
                    button = view_dialog.findViewById( R.id.helpTextSmall );
                    button.setText( getString( R.string.enjoy_CC ) );
                    button.setVisibility( VISIBLE );
                    //
                    button = view_dialog.findViewById( R.id.helpText );
                    button.setVisibility( View.INVISIBLE );
                }
                
                
                //##############################
                //
                //
                //
                //##############################
                boardTiles = new ArrayList<>();
                safetyNet = new ArrayList<>();
                GameEngine.isKilled = false;
                //gridLayout.setHelperGrid( boardTiles, 4, 4 );
                processThread( START_THREAD );
                
                
                //#################################
                //
                //
                //
                //#################################
                button = view_dialog.findViewById( R.id.helpTextInfo );
                button.setText( helpText[ currentGrid ] );
                button.setVisibility( VISIBLE );
                button.setAlpha( 0f );
                button.animate().alpha( 1f ).setDuration( 1000 ).start();
                
                
                //
                // Arrows used to control the grids
                currentGrid = 0;
                //
                arrow = view_dialog.findViewById( R.id.rightArrow );
                arrow.setOnClickListener( this );
                arrow.setVisibility( (currentGrid >= (coinGrids.length - 1) ? View.INVISIBLE : VISIBLE) );
                //
                arrow = view_dialog.findViewById( R.id.leftArrow );
                arrow.setOnClickListener( this );
                arrow.setVisibility( (currentGrid == 0 ? View.INVISIBLE : VISIBLE) );
                
                // Set the page number information
                button = view_dialog.findViewById( R.id.helpPageNums );
                button.setText( String.format( Locale.getDefault(), "%d / %d", currentGrid + 1, coinGrids.length ) );
                
                // Make a record of the student attending class!
                gameEngine.helpWasReceived();
*/
            }
            else
            {
                dismiss();
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.printStackTrace();
            dismiss();
            return null;
        }
        
        //
        return view_dialog;
    }
    
    
    @Override
    public void onStart()
    {
        Dialog dialog = getDialog();
        
        if ( dialog != null && dialog.getWindow() != null )
        {
            int width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout( width, height );
        }
        
/*
        view_dialog.setAlpha( 0f );
        view_dialog.animate().alpha( 1f ).setDuration( 500 ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                gridLayout.setHelperGrid( boardTiles, 4, 4, true );
                processGridChange( currentGrid );
            }
        } ).start();
*/
        
        super.onStart();
    }
    
    /**
     * Click listener for the button on
     * rom info form
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
/*
        
        if ( id == R.id.leftArrow || id == R.id.rightArrow )
        {
            GradientTextView text;
            ImageView        arrow;
            boolean          displayText = false;
            
            //
            if ( id == R.id.leftArrow && currentGrid > 0 )
            {
                currentGrid--;
                displayText = true;
                
                //
                processGridChange( currentGrid );
            }
            else if ( id == R.id.rightArrow && currentGrid < (coinGrids.length - 1) )
            {
                currentGrid++;
                displayText = true;
                
                //
                processGridChange( currentGrid );
            }
            
            
            // Helpful text
            if ( displayText )
            {
                text = view_dialog.findViewById( R.id.helpTextInfo );
                text.setText( helpText[ currentGrid ] );
                text.setVisibility( VISIBLE );
                text.setAlpha( 0f );
                text.animate().alpha( 1f ).setDuration( 1000 ).start();
                
                // Remove the right Arrow?
                arrow = view_dialog.findViewById( R.id.rightArrow );
                arrow.setVisibility( (currentGrid >= (coinGrids.length - 1) ? View.INVISIBLE : VISIBLE) );
                // Remove the left Arrow?
                arrow = view_dialog.findViewById( R.id.leftArrow );
                arrow.setVisibility( (currentGrid == 0 ? View.INVISIBLE : VISIBLE) );
                
                // Set the page number information
                text = view_dialog.findViewById( R.id.helpPageNums );
                text.setText( String.format( Locale.getDefault(), "%d / %d", currentGrid + 1, coinGrids.length ) );
            }
        }
        else if ( id == R.id.helpDoneButton )
        {
            //@@@@@@@@@@@@@@@@@@ Close dialog box
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
            }
            
            dismiss();
        }
*/
    }
    
    
    /**
     * For the sake of the help tutorial, we want to ALWAYS
     * start a new thread when called to start a thread
     *
     * @param setStatus
     */
    private void processThread( int setStatus )
    {
        //#################################
        //
        // Start the thread
        //
        //#################################
        if ( logicThread == null )
        {
            logicThread = new LogicThread( getActivity() );
            //
            logicThread.clearData();
            gridLayout.setLogicThread( logicThread );
            logicThread.setGridLayout( gridLayout );
        }
    }
    
    
    /**
     * Show the grid requested
     *
     * @param currentGrid
     */
    private void processGridChange( int currentGrid )
    {
        String[]         grid = getResources().getStringArray( coinGrids[ currentGrid ] );
        GradientTextView text;
        
        //
        if ( gridLayout == null || gridLayout.lineBuffer == null )
        {
            return;
        }
        
        
        //###############################
        //
        // Always rebuild the same board
        //
        //###############################
        // LoopingAnimator
        if ( dummy != null )
        {
            userCancelled = true;
            dummy.cancel();
            dummy = null;
        }
        
        // Scripting Animator
        if ( script != null )
        {
            userCancelled = true;
            script.cancel();
        }
        
        userCancelled = false;
        // Kill it then refresh it
        processThread( START_THREAD );
        
        
        // Clear some buffers, etc
        gridLayout.lineBuffer.eraseColor( Color.TRANSPARENT );
        //        gridLayout.invalidate();
        boardTiles.clear();
        gridLayout.tilePoints.clear();
        //
        GameEngine.arraySetData( gridLayout.boardMap, gridLayout.getRowCount(), gridLayout.getColumnCount(), 1 );
        GameEngine.arraySetData( gridLayout.matchList, gridLayout.getRowCount(), gridLayout.getColumnCount(), 1 );
        
        
        //##############################
        //
        // Build the board mapper
        //
        //##############################
        for ( int i = 0; i < grid.length; i++ )
        {
            String[] list = grid[ i ].split( "," );
            
            for ( int c = 0; c < list.length; c++ )
            {
                int       num = Integer.parseInt( list[ c ] );
                BoardTile tile;
                
                //
                tile = new BoardTile( getContext(), BoardTile.STATE_ACTIVE, (i * gridLayout.getColumnCount()) + c );
                tile.setScaleType( ImageView.ScaleType.FIT_CENTER );
                
                // Assign a color to the new tile
                tile.tileNum = num;
                tile.setSpecialItem( 0 );
                
                //
                if ( tile.tileNum >= BoardTile.BOMB )
                {
                    tile.setSpecialItem( 2 );
                }
                //
                tile.specialTile = -1;
                boardTiles.add( tile );
            }
        }
        
        
        //########################
        //
        // Draw the data
        //
        //########################
/*
        // Helpful text
        text = view_main.findViewById( R.id.helpTextInfo );
        text.setText( helpText[ currentGrid ] );
        text.setVisibility( VISIBLE );
        text.setAlpha( 0f );
        text.animate().alpha( 1f ).setDuration( 1000 ).start();
*/
        //
        // Grid imaging
        //
        gridLayout.setVisibility( VISIBLE );
        gridLayout.setAlpha( 0f );
        gridLayout.onDrawGrid( true );
        gridLayout.animate().alpha( 1f ).setDuration( 1000 ).withEndAction( new Runnable()
        {
            @Override
            public void run()
            {
                if ( !userCancelled )
                {
                    scriptHelpAnimation( currentGrid );
                }
            }
        } ).start();
    }
    
    
    /**
     * //###############################
     * <p>
     * Sort of a scripting engine for
     * the help animations
     * <p>
     * //###############################
     */
    public void scriptHelpAnimation( int currentGrid )
    {
        //
        String[]  grid  = getString( gridScripts[ currentGrid ] ).split( "," );
        int[]     resId = new int[ grid.length ];
        final int size  = getResources().getDimensionPixelSize( R.dimen._32sdp );
        
        
        //#############################
        // Fill the animation variable
        // with the scripting info
        //#############################
        for ( int i = 0; i < grid.length; i++ )
        {
            resId[ i ] = Integer.parseInt( grid[ i ].trim() );
        }
        
        // Animate using the scripting data
        gridLayout.tilePoints.clear();
        safetyNet.clear();
        script = ValueAnimator.ofInt( resId );
        cannotSolve = false;
        
        
        //
        // Main scripting engine animator
        //
        script.setInterpolator( new LinearInterpolator() );
        script.setEvaluator( new CustomEvaluator() );
        script.setDuration( 250 * resId.length );
        script.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate( ValueAnimator animation )
            {
                //###############################
                //
                // This will draw all connecting
                // lines onto the grid layout
                //
                //###############################
                int index = ( int ) animation.getAnimatedValue();
                int x     = (index % gridLayout.getColumnCount());
                int y     = (index / gridLayout.getRowCount());
                
                
                //
                // Replace a tile
                //
                if ( index > 100 && !safetyNet.contains( index ) )
                {
                    int posi = index / 100;
                    int num  = index - (posi * 100);
                    
                    //
                    BoardTile tile = boardTiles.get( posi );
                    tile.tileNum = num;
                    tile.setImageResource( GameBoard.coinSet[ tile.tileNum ] );
                    //
                    if ( tile.tileNum >= BoardTile.BOMB )
                    {
                        tile.setSpecialItem( 2 );
                    }
                    
                    //
                    safetyNet.add( index );
                }
                //
                // Draw a line
                //
                else if ( index > -1 && !safetyNet.contains( index ) )
                {
                    BoardTile tile = boardTiles.get( index );
                    safetyNet.add( index );
                    
                    // Set the center of the tile
                    //                    PointXYZ pressed = new PointXYZ( (x * size) + (size / 2), (y * size) + (size / 2), index );
                    //                    gridLayout.tilePoints.add( pressed );
                    
                    gridLayout.tilePoints.add( (y * gridLayout.getColumnCount() + x) );
                    
                    // Affected tiles
                    gridLayout.matchList[ y ][ x ] = 1;
                    if ( tile.tileNum >= BoardTile.BOMB )
                    {
                        gridLayout.matchList[ y ][ x ] = 0;
                    }
                    
                    if ( gridLayout.tilePoints.size() > 1 )
                    {
                        gridLayout.drawBufferLineHelper( size );
                    }
                }
                //
                // Remove a tile from Line drawing
                //
                else if ( index < 0 && !safetyNet.contains( index ) )
                {
                    int num = Math.abs( index );
                    
                    safetyNet.add( index );
                    x = (num % gridLayout.getColumnCount());
                    y = (num / gridLayout.getRowCount());
                    
                    //
                    BoardTile tile = boardTiles.get( num );
                    tile.setImageResource( GameBoard.coinSet[ tile.tileNum ] );
                    
                    Integer i = (y * gridLayout.getColumnCount() + x);
                    gridLayout.tilePoints.remove( i );
                    
                    // Erase the lines
                    gridLayout.lineBuffer.eraseColor( Color.TRANSPARENT );
                }
            }
        } );
        script.addListener( new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd( Animator animation )
            {
                if ( !userCancelled )
                {
                    // When the lines are drawn, this will solve for matches
                    gridLayout.lineBuffer.eraseColor( Color.TRANSPARENT );
                    
                    if ( !cannotSolve )
                    {
                        //####################################
                        // Run the match process
                        // If we have a special, create it!
                        //####################################
                        if ( gridLayout.tilePoints.size() >= ConnectionsGridLayout.MIN_FOR_SPECIAL )
                        {
                            logicThread.addToStack( LogicThread.CMD_CREATE_SPECIALS );
                        }
                        else
                        {
                            logicThread.addToStack( LogicThread.CMD_PROCESS_MATCHES );
                        }
                    }
                    //
                    // Go and wait for the LOGICTHREAD to be finished
                    //
                    onTutorialFinished();
                }
                
                super.onAnimationEnd( animation );
            }
        } );
        //
        gameEngine.animatorList.add( script );
        script.start();
    }
    
    
    /**
     * //###########################
     * <p>
     * Reset the tutorial
     * <p>
     * //###########################
     */
    public void onTutorialFinished()
    {
        if ( getActivity() != null )
        {
            final Activity activity = getActivity();
            
            //###############################
            // Loop in a thread until the
            // main thread is done
            //###############################
            Thread thread = new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    int loops = 0;
                    
                    if ( logicThread != null )
                    {
                        while ( logicThread.getCurrentCmd() != LogicThread.CMD_IDLE && logicThread.getCurrentCmd() != LogicThread.CMD_LEVEL_COMPLETE && loops < 10000 )
                        {
                            loops++;
                        }
                    }
                    
                    
                    //###################################
                    // Adjust offsets on Main UI thread
                    // since it owns all
                    // the instances of views
                    //###################################
                    activity.runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dummy = ValueAnimator.ofInt( 1, 100 );
                            dummy.setDuration( 4000 ).addListener( new AnimatorListenerAdapter()
                            {
                                @Override
                                public void onAnimationEnd( Animator animation )
                                {
                                    if ( !userCancelled )
                                    {
                                        processGridChange( currentGrid );
                                    }
                                    
                                    super.onAnimationEnd( animation );
                                }
                            } );
                            //
                            gameEngine.animatorList.add( dummy );
                            dummy.start();
                        }
                    } );
                }
            } );
            
            thread.start();
        }
    }
    
    
    @Override
    public void onDestroyView()
    {
        gameEngine = null;
        gameBoard = null;
        view_dialog = null;
        helpText = null;
        gridLayout = null;
        boardTiles = null;
        //
        if ( logicThread != null )
        {
            logicThread.killThread();
            logicThread = null;
        }
        //
        script = null;
        dummy = null;
        safetyNet = null;
        onDismisser = null;
        
        super.onDestroyView();
    }
}
