package com.pierfrancescosoffritti.remotevrclient.utils;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.transition.ChangeBounds;
import android.view.View;

/**
 * Created by  Pierfrancesco on 16/12/2015.
 */
public class Fragments {

    public static Fragment swapFragments(FragmentManager supportFragmentManager, int fragmentContainer, Fragment newFragment, Pair<View, String>... sharedViews) {

        newFragment = findFragment(supportFragmentManager, newFragment);

        // Defines enter transition only for shared element
        if(Build.VERSION.SDK_INT >= 21)
            newFragment.setSharedElementEnterTransition(new ChangeBounds());

        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();

        if(Build.VERSION.SDK_INT >= 21)
            for(int i=0; i<sharedViews.length; i++)
                fragmentTransaction.addSharedElement(sharedViews[i].first, sharedViews[i].second);

        fragmentTransaction.replace(fragmentContainer,
                newFragment,
                newFragment.getClass().getName());

        fragmentTransaction.commit();

        return newFragment;
    }

    public static Fragment findFragment(FragmentManager supportFragmentManager, Fragment newFragment) {
        String newFragmentClass = newFragment.getClass().getName();

        Fragment oldFragment = supportFragmentManager.findFragmentById(newFragment.getId());
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragment.getTag());
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragmentClass);

        if (oldFragment != null) {
            newFragment = oldFragment;
        }

        return newFragment;
    }
}
