package com.genesyseast.coinconnection.Fragments;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.genesyseast.coinconnection.R;

public class FragmentLoader

{
    public static final int STACK_REPLACE_FRAGMENT = 3;
    public static final int STACK_ADD_FRAGMENT     = 2;
    public static final int REPLACE_FRAGMENT       = 1;
    public static final int ADD_FRAGMENT           = 0;
    
    /**
     * Swap fragment method
     *
     * @param context       N/A
     * @param fragment      N/A
     * @param placementType N/A
     */
    public static void SwapFragment( Activity activity, Context context, Fragment fragment, int placementType )
    {
        // Cannot hold a NULL value
        if ( activity != null && context != null )
        {   // Get the manager
            FragmentManager     fragmentManager;
            FragmentTransaction fragmentTransaction;
            
            fragmentManager = (( FragmentActivity ) activity).getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            
            switch ( placementType )
            {
                case STACK_REPLACE_FRAGMENT:
                    fragmentTransaction.replace( R.id.fragment_container, fragment ).addToBackStack( null );
                    break;
    
                case STACK_ADD_FRAGMENT:
                    fragmentTransaction.add( R.id.fragment_container, fragment ).addToBackStack( null );
                    break;
                    
                case REPLACE_FRAGMENT:
                    fragmentTransaction.replace( R.id.fragment_container, fragment );
                    break;
                
                case ADD_FRAGMENT:
                default:
                    fragmentTransaction.add( R.id.fragment_container, fragment );
                    break;
            }
            
            // Commit the changes
            fragmentTransaction.commit();
        }
    }
}
