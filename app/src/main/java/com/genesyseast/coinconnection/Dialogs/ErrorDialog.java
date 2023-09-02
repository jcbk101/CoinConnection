package com.genesyseast.coinconnection.Dialogs;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;
import com.genesyseast.coinconnection.Support.CustomEvaluator;
import com.genesyseast.coinconnection.CustomControls.CustomTimer;
import com.genesyseast.coinconnection.CustomControls.GradientTextView;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.PlaySound.PlaySound;
import com.genesyseast.coinconnection.R;

public class ErrorDialog
        extends DialogFragment
        implements View.OnClickListener
{
    public static int YES_BUTTON    = 1;
    public static int NO_BUTTON     = 2;
    public static int CANCEL_BUTTON = 4;
    public static int CLOSE_BUTTON  = 8;
    public static int MAIN_BUTTONS  = 7;
    public static int ALL_BUTTONS   = 15;
    public static int HIDE_BUTTONS  = 0;
    
    private       int                 buttons;
    private       String              title;
    private       String              message;
    private       String              yesText      = "Done";
    private       String              noText       = "No";
    private       String              cancelText   = "Cancel";
    private       int                 alertIcon    = 0;
    private       int                 formImage    = 0;
    private       int                 formImageBG  = 0;
    private       View                view_dialog;
    private       CustomTimer         timer;
    private       OnErrorListener     onErrorListener;
    private       OnAnimationListener onShakeAnimationListener;
    private       int[]               formImageIds = null;
    private       GameEngine          gameEngine;
    private       boolean             useHtml      = false;
    public static boolean             isShowing    = false;
    private       int                 msgresId     = 0;
    
    public interface OnErrorListener
    {
        void onErrorExitClick( int buttonClicked );
    }
    
    public interface OnAnimationListener
    {
        void onAnimationEnd();
    }
    
    /**
     * Setters
     *
     * @param title
     */
    public ErrorDialog setTitle( String title )
    {
        this.title = title;
        
        if ( view_dialog != null )
        {
            TextView tv;
            tv = view_dialog.findViewById( R.id.msgTitle );
            tv.setText( title );
        }
        
        return this;
    }
    
    public ErrorDialog setMessage( String message )
    {
        this.message = message;
        
        if ( view_dialog != null )
        {
            TextView tv;
            tv = view_dialog.findViewById( R.id.msgText );
            tv.setText( message );
        }
        
        return this;
    }
    
    public ErrorDialog setMessage( int message )
    {
        this.msgresId = message;
        
        if ( message > 0 && view_dialog != null )
        {
            this.message = getString( message );
            
            TextView tv;
            tv = view_dialog.findViewById( R.id.msgText );
            tv.setText( this.message );
        }
        
        return this;
    }
    
    
    public ErrorDialog setMessage( String message, boolean useHtml )
    {
        this.message = message;
        this.useHtml = useHtml;
        
        if ( view_dialog != null )
        {
            TextView tv;
            tv = view_dialog.findViewById( R.id.msgText );
            
            if ( useHtml )
            {
                tv.setText( Html.fromHtml( message ) );
            }
            else
            {
                tv.setText( message );
            }
        }
        
        return this;
    }
    
    
    public ErrorDialog setMessage( int message, boolean useHtml )
    {
        this.useHtml = useHtml;
        this.msgresId = message;
        
        if ( message > 0 && view_dialog != null )
        {
            this.message = getString( msgresId );
            
            TextView tv;
            tv = view_dialog.findViewById( R.id.msgText );
            
            if ( useHtml )
            {
                tv.setText( Html.fromHtml( this.message ) );
            }
            else
            {
                tv.setText( this.message );
            }
        }
        
        return this;
    }
    
    
    public ErrorDialog setButtons( int buttons )
    {
        this.buttons = buttons;
        
        View button;
        
        if ( view_dialog != null )
        {
            if ( buttons == 0 )
            {
                button = view_dialog.findViewById( R.id.errorButtonCancel );
                button.setVisibility( View.INVISIBLE );
                button = view_dialog.findViewById( R.id.errorButtonNo );
                button.setVisibility( View.INVISIBLE );
                button = view_dialog.findViewById( R.id.errorButtonDone );
                button.setVisibility( View.INVISIBLE );
            }
            
            // Close button
            if ( (buttons & CLOSE_BUTTON) != 0 || buttons == ALL_BUTTONS )
            {
                View closer = view_dialog.findViewById( R.id.msgClose );
                closer.setVisibility( View.VISIBLE );
                closer.setTag( CLOSE_BUTTON );
                closer.setOnClickListener( this );
            }
        }
        
        return this;
    }
    
    
    public ErrorDialog setYesText( String yesText )
    {
        this.yesText = yesText;
        
        if ( view_dialog != null )
        {
            Button button;
            int    width;
            
            button = view_dialog.findViewById( R.id.errorButtonDone );
            button.setText( yesText );
            
            width = ( int ) button.getPaint().measureText( yesText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( YES_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setNoText( String noText )
    {
        this.noText = noText;
        
        if ( view_dialog != null )
        {
            Button button;
            int    width;
            
            button = view_dialog.findViewById( R.id.errorButtonNo );
            button.setText( noText );
            
            width = ( int ) button.getPaint().measureText( noText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( NO_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setCancelText( String cancelText )
    {
        this.cancelText = cancelText;
        
        if ( view_dialog != null )
        {
            Button button;
            int    width;
            
            button = view_dialog.findViewById( R.id.errorButtonCancel );
            button.setText( cancelText );
            
            width = ( int ) button.getPaint().measureText( cancelText );
            button.getLayoutParams().width = width + (width / 3);
            //
            button.setTag( CANCEL_BUTTON );
            button.setVisibility( View.VISIBLE );
        }
        
        return this;
    }
    
    public ErrorDialog setAlertIcon( int alertIcon )
    {
        this.alertIcon = alertIcon;
        
        if ( view_dialog != null )
        {
            ImageView view = view_dialog.findViewById( R.id.msgIcon );
            view.setImageResource( alertIcon );
        }
        
        return this;
    }
    
    public ErrorDialog setFormImage( int formImage )
    {
        this.formImage = formImage;
        
        if ( view_dialog != null )
        {
            if ( formImageIds != null )
            {
                return this;
            }
            else
            {
                ImageView view = view_dialog.findViewById( R.id.coinFace );
                view.setImageResource( formImage );
            }
        }
        
        return this;
    }
    
    
    public ErrorDialog setFormImageBG( int formImageBG )
    {
        this.formImageBG = formImageBG;
        
        if ( view_dialog != null )
        {
            ImageView view = view_dialog.findViewById( R.id.coinFace );
            view.setBackgroundResource( formImageBG );
        }
        
        return this;
    }
    
    public ErrorDialog setTimer( CustomTimer timer )
    {
        this.timer = timer;
        return this;
    }
    
    public void setOnErrorListener( OnErrorListener onErrorListener )
    {
        this.onErrorListener = onErrorListener;
    }
    
    public void setOnShakeAnimationListener( OnAnimationListener onShakeAnimationListener )
    {
        this.onShakeAnimationListener = onShakeAnimationListener;
    }
    
    
    /**
     * //###########################
     * <p>
     * All other instantiations
     * <p>
     * //###########################
     */
    public ErrorDialog()
    {
    }
    
    
    @Override
    public void onCancel( @NonNull DialogInterface dialog )
    {
        super.onCancel( dialog );
        
        dismiss();
    }
    
    
    /**
     * //###########################
     * <p>
     * Dismiss this dialog
     * <p>
     * //###########################
     */
    @Override
    public void dismiss()
    {
        if ( timer != null )
        {
            timer.resume();
        }
        
        //
        isShowing = false;
        
        // End any animations
        if ( getDialog() != null )
        {
            View dlgView = getView();
            
            if ( dlgView != null && dlgView.getTag() != null )
            {
                (( ObjectAnimator ) dlgView.getTag()).end();
                dlgView.setTag( null );
            }
        }
        
        if ( getActivity() != null )
        {
            getActivity().getSupportFragmentManager().beginTransaction().remove( this ).commit();
        }
        
        super.dismiss();
    }
    
    
    /**
     * //###########################
     * <p>
     * Build the views
     * <p>
     * //###########################
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
        try
        {
            view_dialog = View.inflate( getContext(), R.layout.popup_error_layout, null );
            
            if ( getDialog() != null && getDialog().getWindow() != null )
            {
                getDialog().getWindow().requestFeature( Window.FEATURE_NO_TITLE );
                getDialog().getWindow().setBackgroundDrawableResource( android.R.color.transparent );
                getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogErrorAnimations;
                getDialog().getWindow().setDimAmount( 0 );
                //
                getDialog().getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN );
                
                getDialog().setOnKeyListener( new DialogInterface.OnKeyListener()
                {
                    @Override
                    public boolean onKey( DialogInterface dialog, int keyCode, KeyEvent event )
                    {
                        if ( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && getView() != null )
                        {
                            View button1 = getView().findViewById( R.id.errorButtonCancel );
                            View button2 = getView().findViewById( R.id.errorButtonNo );
                            View button3 = getView().findViewById( R.id.errorButtonDone );
                            View closer  = getView().findViewById( R.id.msgClose );
                            
                            
                            if ( button1 != null && button1.getVisibility() == View.VISIBLE )
                            {
                                button1.setPressed( true );
                                button1.callOnClick();
                                return true;
                            }
                            
                            if ( button2 != null && button2.getVisibility() == View.VISIBLE )
                            {
                                button2.setPressed( true );
                                button2.callOnClick();
                                return true;
                            }
                            
                            if ( button3 != null && button3.getVisibility() == View.VISIBLE )
                            {
                                button3.setPressed( true );
                                button3.callOnClick();
                                return true;
                            }
                            
                            if ( closer != null && closer.getVisibility() == View.VISIBLE )
                            {
                                closer.setPressed( true );
                                closer.callOnClick();
                                return true;
                            }
                            
                            dialog.dismiss();
                        }
                        
                        return false;
                    }
                } );
                
                // Need this for sounds
                gameEngine = GameEngine.getInstance( getContext() );
            }
            else
            {
                return null;
            }
        }
        catch ( NullPointerException npe )
        {
            npe.getMessage();
            return null;
        }
        //
        return view_dialog;
    }
    
    
    /**
     * //###########################
     * <p>
     * Used to set the screen into fullscreen mode
     * <p>
     * //###########################
     */
    @Override
    public void onStart()
    {
        super.onStart();
        
        Dialog dialog = getDialog();
        
        if ( dialog != null )
        {
            int                           width  = ViewGroup.LayoutParams.MATCH_PARENT;
            int                           height = ViewGroup.LayoutParams.MATCH_PARENT;
            int                           space  = 0;
            GradientTextView              button;
            ImageView                     icon;
            TextView                      tv;
            GradientTextView              imgBtn;
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams( 0, 0 );
            
            // In Displayed status
            isShowing = true;
            
            //
            if ( dialog.getWindow() != null )
            {
                dialog.getWindow().setLayout( width, height );
            }
            
            //
            tv = view_dialog.findViewById( R.id.msgTitle );
            tv.setText( title );
            
            
            tv = view_dialog.findViewById( R.id.msgText );
            
            if ( useHtml )
            {
                if ( msgresId > 0 )
                {
                    message = getString( msgresId );
                    tv.setText( Html.fromHtml( message ) );
                }
                else if ( message != null )
                {
                    tv.setText( message );
                }
                else
                {
                    tv.setText( " " );
                }
            }
            else
            {
                if ( msgresId > 0 )
                {
                    message = getString( msgresId );
                    tv.setText( message );
                }
                else if ( message != null )
                {
                    tv.setText( message );
                }
                else
                {
                    tv.setText( " " );
                }
            }
            
            
            // Icon
            icon = view_dialog.findViewById( R.id.msgIcon );
            icon.setImageResource( alertIcon );
            
            // Form Image
            icon = view_dialog.findViewById( R.id.coinFace );
            icon.setImageResource( formImage );
            icon.setBackgroundResource( formImageBG );
            icon.setVisibility( View.GONE );
            
            if ( formImage > 0 )
            {
                icon.setVisibility( View.VISIBLE );
            }
            
            if ( formImageBG > 0 )
            {
                icon.setVisibility( View.VISIBLE );
            }
            
            // We want to animate the form image
            if ( formImageIds != null )
            {
                icon.setVisibility( View.VISIBLE );
                animateFormImage( formImageIds );
            }
            
            
            //#################################
            //
            // Hide all buttons when requested
            //
            //#################################
            if ( buttons == 0 )
            {
                button = view_dialog.findViewById( R.id.errorButtonCancel );
                button.setVisibility( View.INVISIBLE );
                button = view_dialog.findViewById( R.id.errorButtonNo );
                button.setVisibility( View.INVISIBLE );
                button = view_dialog.findViewById( R.id.errorButtonDone );
                button.setVisibility( View.INVISIBLE );
                
                //
                imgBtn = view_dialog.findViewById( R.id.msgClose );
                
                // The close button, for safety, MUST be shown
                imgBtn.setVisibility( View.VISIBLE );
                imgBtn.setOnClickListener( this );
                imgBtn.setTag( CLOSE_BUTTON );
            }
            else
            {
                // Cancel, No, and Done button clicker assignment
                button = view_dialog.findViewById( R.id.errorButtonCancel );
                if ( (buttons & CANCEL_BUTTON) != 0 )
                {
                    // Cancel
                    button.setOnClickListener( this );
                    button.setText( cancelText );
                    
                    space = ( int ) button.getPaint().measureText( "  " );
                    width = ( int ) button.getPaint().measureText( cancelText );
                    button.getLayoutParams().width = width + (space * 2);
                    //                    button.getLayoutParams().height = ( int ) button.getTextSize() + space;
                    //
                    button.setTag( CANCEL_BUTTON );
                }
                else
                {
                    button.setVisibility( View.INVISIBLE );
                }
                
                // No
                button = view_dialog.findViewById( R.id.errorButtonNo );
                if ( (buttons & NO_BUTTON) != 0 )
                {
                    // No button
                    button.setOnClickListener( this );
                    button.setText( noText );
                    
                    space = ( int ) button.getPaint().measureText( "  " );
                    width = ( int ) button.getPaint().measureText( noText );
                    button.getLayoutParams().width = width + (space * 2);
                    //                  button.getLayoutParams().height = ( int ) button.getTextSize() + space;
                    //
                    button.setTag( NO_BUTTON );
                }
                else
                {
                    button.setVisibility( View.INVISIBLE );
                }
                
                
                //
                imgBtn = view_dialog.findViewById( R.id.msgClose );
                if ( (buttons & CLOSE_BUTTON) != 0 )
                {
                    // The close button, for safety, MUST be shown
                    imgBtn.setVisibility( View.VISIBLE );
                    imgBtn.setOnClickListener( this );
                    imgBtn.setTag( CLOSE_BUTTON );
                }
                else
                {
                    imgBtn.setVisibility( View.INVISIBLE );
                    imgBtn.setOnClickListener( this );
                }
                
                
                // Default to Yes
                button = view_dialog.findViewById( R.id.errorButtonDone );
                button.setOnClickListener( this );
                button.setText( yesText );
                
                space = ( int ) button.getPaint().measureText( "  " );
                width = ( int ) button.getPaint().measureText( yesText );
                button.getLayoutParams().width = width + (space * 2);
                //                button.getLayoutParams().height = ( int ) button.getTextSize() + space;
                
                //
                button.setTag( YES_BUTTON );
                button.setVisibility( View.VISIBLE );
            }
            
            // Pause if it needs to be paused
            if ( timer != null )
            {
                timer.pause();
            }
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Click listener for the button on
     * rom info form
     * <p>
     * //###########################
     *
     * @param v N/A
     */
    @Override
    public void onClick( View v )
    {
        if ( getDialog() != null )
        {
            //@@@@@@@@@@@@@@@ Play Click sound
            if ( gameEngine.soundPlayer != null )
            {
                gameEngine.soundPlayer.playBgSfx( PlaySound.BUTTON_CLICK );
            }
            
            // Close button
            if ( v.getId() == R.id.msgClose )
            {
                dismiss();
                return;
            }
            
            //
            if ( onErrorListener != null )
            {
                if ( v.getTag() != null )
                {
                    onErrorListener.onErrorExitClick( ( int ) v.getTag() );
                }
                else
                {
                    onErrorListener.onErrorExitClick( YES_BUTTON );
                }
            }
            else
            {
                if ( timer != null )
                {
                    timer.resume();
                }
                
                dismiss();
            }
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Internal call to simplfy reactions for this dialog
     * <p>
     * //###########################
     *
     * @param shakeType       N/A
     * @param readMessageTime N/A
     */
    public void animateShake( boolean shakeType, final int readMessageTime )
    {
        final View dlgView = getView();
        
        if ( dlgView != null )
        {
            if ( !shakeType )
            {
                dlgView.setRotation( 5 );
            }
            else
            {
                dlgView.setRotation( -5 );
            }
            //
            dlgView.setScaleX( .9f );
            dlgView.setScaleY( .9f );
            dlgView.animate().setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
            dlgView.animate().scaleX( 1 ).scaleX( 1 ).rotation( 0 ).setDuration( 1000 );
            dlgView.animate().withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    if ( onShakeAnimationListener != null )
                    {
                        if ( readMessageTime <= 0 )
                        {
                            onShakeAnimationListener.onAnimationEnd();
                        }
                        else
                        {
                            dlgView.setAlpha( .99f );
                            dlgView.animate().setDuration( readMessageTime );
                            dlgView.animate().alpha( 1f ).withEndAction( new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    onShakeAnimationListener.onAnimationEnd();
                                }
                            } ).start();
                        }
                    }
                }
            } ).start();
        }
    }
    
    
    /**
     * //###########################
     * <p>
     * Internal call to simplfy reactions for this dialog
     * <p>
     * //###########################
     *
     * @param shakeType N/A
     */
    public void animateShake( boolean shakeType )
    {
        animateShake( shakeType, 0 );
    }
    
    
    /**
     * //###########################
     * <p>
     * Set an animated form image
     * <p>
     * //###########################
     *
     * @param resIds
     */
    public void animateFormImage( int[] resIds )
    {
        if ( view_dialog != null && getView() != null )
        {
            final View dlgView = getView();
            
            ImageView      view = view_dialog.findViewById( R.id.coinFace );
            ObjectAnimator animator;
            
            animator = ObjectAnimator.ofInt( view, "imageResource", resIds );
            animator.setEvaluator( new CustomEvaluator() );
            animator.setDuration( 50 * (resIds.length) );
            animator.setRepeatCount( ValueAnimator.INFINITE );
            animator.setInterpolator( new LinearInterpolator() );
            animator.start();
            //
            dlgView.setTag( animator );
        }
        else
        {
            formImageIds = resIds;
        }
    }
    
    
    @Override
    public void onDestroyView()
    {
        gameEngine = null;
        timer = null;
        onErrorListener = null;
        onShakeAnimationListener = null;
        formImageIds = null;
        view_dialog = null;
        
        super.onDestroyView();
    }
}
