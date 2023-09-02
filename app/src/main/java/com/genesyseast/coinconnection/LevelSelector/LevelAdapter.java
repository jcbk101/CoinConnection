package com.genesyseast.coinconnection.LevelSelector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.genesyseast.coinconnection.CustomControls.ImageTextView;
import com.genesyseast.coinconnection.Dialogs.ErrorDialog;
import com.genesyseast.coinconnection.Dialogs.PrizeDialog;
import com.genesyseast.coinconnection.Fragments.LevelSelector;
import com.genesyseast.coinconnection.GameEngine.GameEngine;
import com.genesyseast.coinconnection.R;
import com.genesyseast.coinconnection.Support.CustomBounceInterpolator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class LevelAdapter
        extends ArrayAdapter<LevelScene>
        implements View.OnClickListener
{
    
    public static class ViewHolder
    {
        public int             viewType;
        public int             viewCount;
        public int             levelBase;
        public TextView        tv;
        public ImageTextView   prize;
        public ImageTextView[] imgTv;
    }
    
    private ArrayList<LevelScene>  levelList;
    private int                    widthUsed;
    private int                    levelScenes;
    private int                    baseLevel;
    private int                    marginSize;
    private ListView               listView;
    private OnLevelAdapterListener onLevelAdapterListener;
    private Bitmap                 bitmap;
    private ImageView              player;
    private int[]                  itvList    = {
            R.id.levelSlot1,
            R.id.levelSlot2,
            R.id.levelSlot3,
            R.id.levelSlot4,
            R.id.levelSlot5,
            R.id.levelSlot6,
            R.id.levelSlot7,
            R.id.levelSlot8,
            R.id.levelSlot9,
            R.id.levelSlot10,
            R.id.levelSlot11,
            R.id.levelSlot12
    };
    private int[]                  starLevel  = {
            R.drawable.earned_no_stars, R.drawable.earned_one_star, R.drawable.earned_2_stars, R.drawable.earned_3_stars
    };
    private GameEngine             gameEngine = GameEngine.getInstance( getContext() );
    
    
    public LevelAdapter( @NonNull Context context, int resource, ArrayList<LevelScene> levelList, ListView listView, int levelScenes )
    {
        super( context, resource );
        this.levelList = levelList;
        this.widthUsed = listView.getWidth();
        this.listView = listView;
        this.levelScenes = levelScenes;
        this.marginSize = (( Activity ) context).findViewById( R.id.mailCheckFrame ).getRight();
        
        loadPlayerImage();
    }
    
    
    public LevelAdapter( @NonNull Context context, int resource, ArrayList<LevelScene> levelList, ListView listView )
    {
        super( context, resource );
        this.levelList = levelList;
        this.widthUsed = listView.getWidth();
        this.listView = listView;
        this.levelScenes = 1;
        this.marginSize = (( Activity ) context).findViewById( R.id.mailCheckFrame ).getRight();
        
        loadPlayerImage();
    }
    
    
    public interface OnLevelAdapterListener
    {
        void onAvatarSelector();
        
        void onLevelSelected( int currentLevel );
        
        void onExistingLevelSelected( int currentLevel );
        
        void onPrizeViewerSelected( int baseLevel );
    }
    
    
    public void setBitmap( Bitmap bitmap )
    {
        this.bitmap = bitmap;
        
        if ( player != null )
        {
            player.setImageBitmap( this.bitmap );
            player.setTag( 1 );
        }
    }
    
    public void setOnLevelAdapterListener( OnLevelAdapterListener onLevelAdapterListener )
    {
        this.onLevelAdapterListener = onLevelAdapterListener;
    }
    
    public View getPlayer()
    {
        if ( player != null )
        {
            return player;
        }
        
        return null;
    }
    
    
    public int getPlayerDirection()
    {
        if ( (GameEngine.currentLevel / 12) < (baseLevel / 12) )
        {
            // down
            return 1;
        }
        
        // up
        return 0;
    }
    
    
    @Override
    public int getItemViewType( int position )
    {
        return levelList.get( position ).sceneIndex;
    }
    
    @Override
    public int getViewTypeCount()
    {
        return levelScenes;
    }
    
    @Override
    public int getCount()
    {
        return levelList.size();
    }
    
    @Nullable
    @Override
    public LevelScene getItem( int position )
    {
        return levelList.get( position );
    }
    
    
    @Override
    public long getItemId( int i )
    {
        return i;
    }
    
    
    /**
     * //######################################
     * <p>
     * View builder
     * <p>
     * //######################################
     *
     * @param position
     * @param convertView
     * @param parent
     *
     * @return
     */
    @NonNull
    @Override
    public View getView( int position, @Nullable View convertView, @NonNull ViewGroup parent )
    {
        LevelScene     scene      = levelList.get( position );
        ColorStateList colorStateList;
        ViewHolder     holder;
        int            theColor;
        int            viewType   = scene.sceneIndex;
        int            viewLevels = scene.baseLevel;
        int            newWidth   = widthUsed - (marginSize * 2);
        
        
        //###############################
        //
        // Select a view or inflate new
        //
        //###############################
        if ( scene.sceneIndex == -1 )
        {
            convertView = View.inflate( getContext(), scene.layoutScene, null );
            convertView.setLayoutParams( new AbsListView.LayoutParams( newWidth, newWidth / 2 ) );
            
            return convertView;
        }
        else
        {
            int size = (newWidth / 6);
            
            //
            //############################
            //
            if ( convertView == null )
            {
                holder = new ViewHolder();
                holder.imgTv = new ImageTextView[ 12 ];
                
                convertView = View.inflate( getContext(), scene.layoutScene, null );
                convertView.setLayoutParams( new AbsListView.LayoutParams( newWidth, newWidth * 2 ) );
                
                // Must be matching views
                holder.viewType = viewType;
                
                //
                convertView.setTag( holder );
                
                // Boards BG
                holder.tv = convertView.findViewById( R.id.boardImage );
                
                // Preset the count
                holder.viewCount = 12;
                
                //
                for ( int i = 0; i < 12; i++ )
                {
                    holder.imgTv[ i ] = convertView.findViewById( itvList[ i ] );
                    
                    if ( holder.imgTv[ i ] == null )
                    {
                        holder.viewCount = i;
                        break;
                    }
                    
                    holder.imgTv[ i ].setOnClickListener( this );
                    holder.imgTv[ i ].setTag( i );
                    
                    holder.imgTv[ i ].getLayoutParams().width = size;
                    holder.imgTv[ i ].getLayoutParams().height = size;
                    holder.imgTv[ i ].setTextSize( TypedValue.COMPLEX_UNIT_PX, size / 3f );
                }
                
                // Prize view
                holder.prize = convertView.findViewById( R.id.prizeClaim );
                
                //###############################
                //
                // Change path color
                //
                //###############################
                theColor = ContextCompat.getColor( getContext(), R.color.colorRoad );
                colorStateList = ColorStateList.valueOf( theColor );
                ViewCompat.setBackgroundTintList( holder.tv, colorStateList );
            }
            else
            {
                holder = ( ViewHolder ) convertView.getTag();
            }
        }
        
        
        //###############################
        //
        // Place the player image
        //
        //###############################
        if ( GameEngine.currentLevel >= viewLevels && GameEngine.currentLevel < (viewLevels + holder.viewCount) )
        {
            int                  index            = (GameEngine.currentLevel - viewLevels);
            ImageTextView        loc              = holder.imgTv[ index ];
            final RelativeLayout playerFrame      = convertView.findViewById( R.id.playerContainer );
            ConstraintLayout     constraintLayout = convertView.findViewById( R.id.levelContainer );
            ConstraintSet        constraintSet    = new ConstraintSet();
            
            // Process
            constraintSet.clone( constraintLayout );
            constraintSet.connect( playerFrame.getId(), ConstraintSet.BOTTOM, loc.getId(), ConstraintSet.TOP );
            constraintSet.connect( playerFrame.getId(), ConstraintSet.END, loc.getId(), ConstraintSet.END );
            constraintSet.connect( playerFrame.getId(), ConstraintSet.START, loc.getId(), ConstraintSet.START );
            //
            constraintSet.applyTo( constraintLayout );
            
            // Show the player Avatar
            playerFrame.setVisibility( View.VISIBLE );
            
            // Make it possible to change the player's avatar
            playerFrame.setOnClickListener( this );
            
            // Animate the player
            playerFrame.animate().alpha( 1f ).setDuration( 10 ).withEndAction( new Runnable()
            {
                @Override
                public void run()
                {
                    ObjectAnimator obj = ObjectAnimator.ofFloat( playerFrame, "translationY", 0, -(playerFrame.getHeight() / 4.0f), 0 );
                    obj.setDuration( 1000 ).setInterpolator( new DecelerateInterpolator( .5f ) );
                    obj.setRepeatCount( ValueAnimator.INFINITE );
                    obj.start();
                    playerFrame.setTag( obj );
                }
            } ).start();
            
            // Get Avatar Holder
            player = playerFrame.findViewById( R.id.playerImage );
            
            //
            if ( bitmap != null && player != null && player.getTag() == null )
            {
                player.setImageBitmap( bitmap );
                player.setTag( 1 );
            }
        }
        else
        {
            RelativeLayout playerFrame = convertView.findViewById( R.id.playerContainer );
            
            if ( playerFrame.getTag() != null )
            {
                (( ObjectAnimator ) playerFrame.getTag()).cancel();
                playerFrame.setTag( null );
                player = null;
            }
            
            //
            playerFrame.setVisibility( View.INVISIBLE );
        }
        
        
        //
        //###############################
        //
        for ( int i = 0; i < holder.viewCount; i++ )
        {
            // Default level number
            holder.imgTv[ i ].setText( String.format( Locale.US, "%d", viewLevels + i + 1 ) );
            
            //##############################
            //
            // Unlocked / Completed All
            //
            //##############################
            if ( (viewLevels + i) <= GameEngine.currentLevel )
            {
                // This is the complete level,
                // set color to "Bronze, Silver, or Gold"
                if ( (viewLevels + i) < GameEngine.currentLevel )
                {
                    holder.imgTv[ i ].setBackgroundResource( starLevel[ scene.currentStars[ i ] ] );
                }
                // This is the active level,
                // set color to "Ready for Play"
                else if ( (viewLevels + i) == GameEngine.currentLevel )
                {
                    holder.imgTv[ i ].setBackgroundResource( R.drawable.dot_green );
                }
            }
            //##############################
            //
            // Locked
            //
            //##############################
            else
            {
                holder.imgTv[ i ].setBackgroundResource( R.drawable.dot_gray );
            }
        }
        
        
        //###############################
        //
        // Prizes given for stars earn in
        // each level block of 12
        //
        //###############################
        if ( holder.prize != null )
        {
            final ImageTextView imageTextView = holder.prize;
            int                 size          = (newWidth / 6);
            
            if ( holder.prize.getTag() != null )
            {
                (( ObjectAnimator ) holder.prize.getTag()).end();
                holder.prize.setTag( null );
                holder.prize.setOnClickListener( null );
            }
            
            //
            //###############################
            //
            if ( GameEngine.prizeStatus[ viewLevels / GameEngine.MAX_LEVEL_PER_SELECTOR ] == 7 )
            {
                holder.prize.setOnClickListener( null );
                holder.prize.getLayoutParams().width = size;
                holder.prize.getLayoutParams().height = size;
                //
            }
            else
            {
                holder.prize.setOnClickListener( this );
                holder.prize.getLayoutParams().width = size;
                holder.prize.getLayoutParams().height = size;
                
                // Animate the player
                holder.prize.animate().alpha( 1f ).setDuration( 10 ).withEndAction( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        PropertyValuesHolder sx       = PropertyValuesHolder.ofFloat( "scaleX", 1, 1.15f, 1 );
                        PropertyValuesHolder sy       = PropertyValuesHolder.ofFloat( "scaleY", 1, 0.85f, 1 );
                        ObjectAnimator       animator = ObjectAnimator.ofPropertyValuesHolder( imageTextView, sx, sy );
                        animator.setDuration( 3000 ).setInterpolator( new CustomBounceInterpolator( 0.2, 20 ) );
                        animator.setRepeatCount( ValueAnimator.INFINITE );
                        animator.start();
                        imageTextView.setTag( animator );
                    }
                } ).start();
            }
        }
        
        //
        convertView.setX( (widthUsed / 2f) - (newWidth / 2f) );
        //
        holder.levelBase = viewLevels;
        baseLevel = viewLevels;
        
        return convertView;
    }
    
    
    /**
     * //######################################
     * <p>
     * Clicker to start level / Test View
     * <p>
     * //######################################
     *
     * @param v
     */
    @Override
    public void onClick( View v )
    {
        View      parentRow = ( View ) v.getParent();
        final int position  = listView.getPositionForView( parentRow );
        
        // Change player's avatar
        if ( v.getId() == R.id.playerContainer )
        {
            if ( onLevelAdapterListener != null )
            {
                onLevelAdapterListener.onAvatarSelector();
                //                Toast.makeText( getContext(), "Avatar Selection Requested", Toast.LENGTH_SHORT ).show();
            }
        }
        else if ( v.getId() == R.id.prizeClaim )
        {
            ObjectAnimator obj = LevelSelector.pressedButtonClick( v );/*bounceButtonClick( v );*/
            
            obj.addListener( new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd( Animator animation )
                {
                    super.onAnimationEnd( animation );
                    if ( onLevelAdapterListener != null )
                    {
                        onLevelAdapterListener.onPrizeViewerSelected( levelList.get( position ).baseLevel );
                    }
                }
            } );
            
            obj.start();
        }
        else
        {
            int level = (levelList.get( position ).baseLevel + ( int ) v.getTag());
            
            if ( GameEngine.currentLevel == level )
            {
                ObjectAnimator obj = LevelSelector./*pressedButtonClick( v );*/bounceButtonClick( v );
                
                obj.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        if ( onLevelAdapterListener != null )
                        {
                            onLevelAdapterListener.onLevelSelected( GameEngine.currentLevel );
                        }
                    }
                } );
                
                obj.start();
            }
            else if ( GameEngine.currentLevel > level && GameEngine.getStarCountFromLevel( level ) < 3 )
            {
                ObjectAnimator obj = LevelSelector./*pressedButtonClick( v );*/bounceButtonClick( v );
                
                obj.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );
                        
                        if ( onLevelAdapterListener != null )
                        {
                            onLevelAdapterListener.onExistingLevelSelected( levelList.get( position ).baseLevel + ( int ) v.getTag() );
                        }
                    }
                } );
                
                obj.start();
            }
            
        }
        
    }
    
    
    /**
     * //#############################
     * <p>
     * animate the click of a button
     * with a bounce effect
     * <p>
     * //#############################
     *
     * @param v
     */
/*
    public ObjectAnimator bounceButtonClick( final View v )
    {
        v.setScaleX( .5f );
        v.setScaleY( .5f );
        v.animate().scaleX( 1f ).scaleY( 1f ).setDuration( 500 );
        //
        v.animate().setInterpolator( new CustomBounceInterpolator( .2, 20 ) ).start();
    }
    
*/
    
    /**
     * //##############################
     * <p>
     * if the player's pic exist, load
     * it!
     * <p>
     * //##############################
     */
    private void loadPlayerImage()
    {
        // Saves image URI as string to Default Shared Preferences
        // Make a copy for reloading later
        InputStream    inputStream;
        Bitmap         bitmap;
        File           filePath;
        File           dir;
        File           file;
        ImageView      dummy;
        BitmapDrawable drawable;
        
        //
        // Greater than API 29
        //
        filePath = getContext().getFilesDir();
        dir = new File( filePath.getAbsoluteFile() + "/avatar/" );
        
        //
        if ( dir.exists() )
        {
            // Save to file
            file = new File( dir, "avatar.png" );
            
            try
            {
                inputStream = new FileInputStream( file );
                bitmap = BitmapFactory.decodeStream( inputStream );
                
                setBitmap( bitmap );
                inputStream.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
}
