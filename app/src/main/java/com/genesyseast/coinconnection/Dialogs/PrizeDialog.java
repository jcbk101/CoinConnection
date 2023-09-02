package com.genesyseast.coinconnection.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.Fragments.LevelSelector;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;

import java.util.Locale;

public class PrizeDialog
        extends DialogFragment
        implements View.OnClickListener, DialogInterface.OnDismissListener
{
    private GameEngine     gameEngine;
    private View           view_dialog;
    //
    private OnDismisser    onDismisser;
    private OnPrizeClaimed onPrizeClaimed;
    private boolean        userCancelled = false;
    private boolean        cannotSolve   = false;
    private int            baseLevel;
    private int            starCount;
    private int[]          winnableItems = {
            R.drawable.coin_gold, R.drawable.bomb_on, R.drawable.bolt_on
    };
    
    
    public interface OnDismisser
    {
        void onDismiss();
    }
    
    public interface OnPrizeClaimed
    {
        void prizeWasClaimed();
    }
    
    
    /**
     * //#########################
     * <p>
     * All other instantiations
     * <p>
     * //#########################
     */
    public PrizeDialog( int baseLevel )
    {
        this.baseLevel = baseLevel;
    }
    
    public void setOnDismisser( OnDismisser onDismisser )
    {
        this.onDismisser = onDismisser;
    }
    
    public void setOnPrizeClaimed( OnPrizeClaimed onPrizeClaimed )
    {
        this.onPrizeClaimed = onPrizeClaimed;
    }
    
    
    /**
     * //#########################
     * <p>
     * Dismiss the dialog
     * <p>
     * //#########################
     */
    @Override
    public void dismiss()
    {
        if ( onDismisser != null )
        {
            onDismisser.onDismiss();
        }
        
        super.dismiss();
    }
    
    
    /**
     * //##########################
     * <p>
     * Build main view
     * <p>
     * //##########################
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState )
    {
        TextView button;
        
        try
        {
            if ( getDialog() != null && getDialog().getWindow() != null )
            // Requesting a basic About dialog
            {
                view_dialog = View.inflate( getContext(), R.layout.popup_level_prize_layout, null );
                
                //
                gameEngine = GameEngine.getInstance( getContext() );
                
                
                // Main exit button
                button = view_dialog.findViewById( R.id.prizeDone );
                button.setOnClickListener( this );
                
                // Set up box clipping
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimations;
                
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                // Get the star count for the level set
                starCount = 0;
                for ( int i = 0; i < GameEngine.MAX_LEVEL_PER_SELECTOR; i++ )
                {
                    starCount += GameEngine.getStarCountFromLevel( baseLevel + i );
                }
                
                
                // Set star counts and levels
                button = view_dialog.findViewById( R.id.prizeStarCount );
                button.setText( String.format( Locale.getDefault(), " %d/36 ", starCount ) );
                
                button = view_dialog.findViewById( R.id.prizeLevelCount );
                button.setText( String.format( Locale.getDefault(), " Level: %d - %d ", baseLevel + 1, baseLevel + 12 ) );
                
                // List Views
                buildPrizeViews();
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.printStackTrace();
            return null;
        }
        
        //
        return view_dialog;
    }
    
    
    /**
     * //##########################
     * <p>
     * Build each view section
     * for claims
     * <p>
     * //##########################
     */
    private void buildPrizeViews()
    {
        int       base    = 12;
        TextView  tv;
        View      layout;
        ViewGroup parent  = view_dialog.findViewById( R.id.listPrizeFrame );
        View      frame;
        int[]     bitTest = { 1, 2, 4 };
        int       bits    = GameEngine.prizeStatus[ baseLevel / GameEngine.MAX_LEVEL_PER_SELECTOR ];
        
        // Clear all children
        parent.removeAllViews();
        
        
        for ( int i = 0; i < 3; i++ )
        {
            layout = View.inflate( getContext(), R.layout.prize_list_layout, null );
            
            tv = layout.findViewById( R.id.starIcon );
            tv.setText( String.format( Locale.getDefault(), "%d", base + (i * GameEngine.MAX_LEVEL_PER_SELECTOR) ) );
            
            
            //##########################
            //
            // Gold coin
            //
            //##########################
            frame = layout.findViewById( R.id.goldCoinFrame );
            frame.setVisibility( View.VISIBLE );
            tv = layout.findViewById( R.id.goldCoinPrize );
            tv.setText( String.format( Locale.getDefault(), "%d", i + 1 ) );
            frame.setVisibility( View.VISIBLE );
            
            
            //##########################
            //
            // Bomb frame
            //
            //##########################
            frame = layout.findViewById( R.id.bombFrame );
            tv = layout.findViewById( R.id.bombPrize );
            if ( i > 0 )
            {
                frame.setVisibility( View.VISIBLE );
                tv.setText( String.format( Locale.getDefault(), "%d", 1 ) );
            }
            else
            {
                frame.setVisibility( View.GONE );
            }
            
            
            //##########################
            //
            // Bolt frame
            //
            //##########################
            frame = layout.findViewById( R.id.boltFrame );
            tv = layout.findViewById( R.id.boltPrize );
            if ( i > 1 )
            {
                frame.setVisibility( View.VISIBLE );
                tv.setText( String.format( Locale.getDefault(), "%d", 1 ) );
            }
            else
            {
                frame.setVisibility( View.GONE );
            }
            
            
            //##########################
            //
            // Claim button
            //
            //##########################
            
            tv = layout.findViewById( R.id.claimPrizeButton );
            View      view  = layout.findViewById( R.id.claimCheckMark );
            
            if ( starCount >= ((i + 1) * GameEngine.MAX_LEVEL_PER_SELECTOR)  )
            {
                tv.setTag( i );
                tv.setEnabled( true );

                if ( (bits & bitTest[ i ]) == 0)
                {
                    // Prize index to determine what the player receives
                    tv.setVisibility( View.VISIBLE );
                    view.setVisibility( View.INVISIBLE );
                    tv.setOnClickListener( this );
                }
                else
                {
                    // Has been claimed already
                    view.setVisibility( View.VISIBLE );
                    tv.setVisibility( View.INVISIBLE );
                    tv.setOnClickListener( null );
                }
            }
            else
            {
                // Prize index to determine what the player receives
                tv.setTag( i );
                tv.setEnabled( false );
                tv.setOnClickListener( null );
            }
            
            // Add the view to the parent
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            int                       margin = getResources().getDimensionPixelSize( R.dimen._1sdp );
            
            params.setMargins( margin, margin, margin, margin );
            parent.addView( layout, params );
        }
    }
    
    
    /**
     * //##################################
     * <p>
     * Click listener for the button on
     * Prize claim form
     * <p>
     * //#################################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        int id = v.getId();
        
        if ( id == R.id.claimPrizeButton )
        {
            int index = ( int ) v.getTag();
            // Set the bit for this claim button
            GameEngine.prizeStatus[ baseLevel / GameEngine.MAX_LEVEL_PER_SELECTOR ] |= (1 << index);
            
            // Give the player the correct prize(s)
            if ( index == 1 )
            {
                // Two gifts: 2 coins, 1 Bomb
                givePlayerPrize( R.drawable.coin_gold, 2 );
                givePlayerPrize( R.drawable.bomb_on, 1 );
            }
            else if ( index == 2 )
            {
                // All gifts: 3 coins, 1 Bomb, 1 Bolt
                givePlayerPrize( R.drawable.coin_gold, 3 );
                givePlayerPrize( R.drawable.bomb_on, 1 );
                givePlayerPrize( R.drawable.bolt_on, 1 );
            }
            else
            {
                // Single gift: 1 Coin
                givePlayerPrize( R.drawable.coin_gold, 1 );
            }
            
            //##############################
            //
            // REport back to the caller
            //
            //##############################
            if ( onPrizeClaimed != null )
            {
                onPrizeClaimed.prizeWasClaimed();
            }
            
            // Animate it away
            v.setEnabled( false );
            v.setOnClickListener( null );
            v.animate().rotation( 360 ).setDuration( 500 ).scaleY( 0f ).scaleX( 0f );
            v.animate().setInterpolator( new LinearInterpolator() );
            v.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    ViewGroup group = ( ViewGroup ) v.getParent();
                    View      view  = group.findViewById( R.id.claimCheckMark );
                    
                    v.setVisibility( View.INVISIBLE );
                    
                    view.setVisibility( View.VISIBLE );
                    view.setScaleY( 0f );
                    view.setScaleX( 0f );
                    //
                    view.animate().rotation( 360 ).setDuration( 500 ).scaleX( 1f ).scaleY( 1f );
                    view.animate().setInterpolator( new CustomBounceInterpolator( .2, 20 ) );
                    view.animate().start();
                }
            } ).start();
            
            return;
        }
        
        
        //@@@@@@@@@@@@@@@@@@ Close dialog box
        if ( gameEngine.soundPlayer != null )
        {
            gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
        }
        
        dismiss();
    }
    
    
    /**
     * //############################
     * <p>
     * Give the player the item
     * they won
     * <p>
     * //############################
     *
     * @param wonItem
     */
    private void givePlayerPrize( int wonItem, int count )
    {
        switch ( wonItem )
        {
            case R.drawable.bolt_on:
                gameEngine.Boosters[ 1 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + count, 99 );
                break;
            case R.drawable.star_2_on:
                gameEngine.Boosters[ 2 ] = ( byte ) Math.min( gameEngine.Boosters[ 2 ] + count, 99 );
                break;
            case R.drawable.random_on:
                gameEngine.Boosters[ 3 ] = ( byte ) Math.min( gameEngine.Boosters[ 3 ] + count, 99 );
                break;
            case R.drawable.coin_gold:
                GameEngine.moneyOnHand = Math.min( GameEngine.moneyOnHand + count, 9999 );
                break;
            case R.drawable.card_free_moves_gold:
                GameEngine.movesOnHand = Math.min( GameEngine.movesOnHand + count, 10 );
                break;
            case R.drawable.bomb_on:
            default:
                gameEngine.Boosters[ 0 ] = ( byte ) Math.min( gameEngine.Boosters[ 1 ] + count, 99 );
                break;
        }
    }
    
    
    @Override
    public void onDestroy()
    {
        gameEngine = null;
        view_dialog = null;
        onDismisser = null;
        
        super.onDestroy();
    }
}
