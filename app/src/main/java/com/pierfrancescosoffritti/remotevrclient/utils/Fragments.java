package com.pierfrancescosoffritti.remotevrclient.utils;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;

/**
 * Utility class from fragments
 *
 * @author Pierfrancesco Soffritti
 */
public class Fragments {

    private static FragmentTransaction replaceFragment_internal(FragmentManager supportFragmentManager, int fragmentContainer, Fragment newFragment, Pair<View, String>... sharedViews) {
        newFragment = findFragment(supportFragmentManager, newFragment);

        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            for(int i=0; i<sharedViews.length; i++)
                if(sharedViews[i] != null)
                    fragmentTransaction.addSharedElement(sharedViews[i].first, sharedViews[i].second);

        fragmentTransaction.replace(fragmentContainer, newFragment, newFragment.getClass().getName());

        return fragmentTransaction;
    }

    public static Fragment swapFragments(FragmentManager supportFragmentManager, int fragmentContainer, Fragment newFragment, Pair<View, String>... sharedViews) {

        FragmentTransaction fragmentTransaction = replaceFragment_internal(supportFragmentManager, fragmentContainer, newFragment, sharedViews);

        fragmentTransaction.commit();

        return newFragment;
    }

    public static Fragment swapFragmentsAddBackStack(FragmentManager supportFragmentManager, int fragmentContainer, Fragment newFragment, Pair<View, String>... sharedViews) {

        FragmentTransaction fragmentTransaction = replaceFragment_internal(supportFragmentManager, fragmentContainer, newFragment, sharedViews);

        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        return newFragment;
    }

    public static Fragment addFragment(FragmentManager supportFragmentManager, int fragmentContainer, Fragment newFragment, String ... TAG) {
        newFragment = findFragment(supportFragmentManager, newFragment);

        String tag;
        if(TAG.length > 0)
            tag = TAG[0];
        else
            tag = newFragment.getClass().getName();

        supportFragmentManager
                .beginTransaction()
                .add(fragmentContainer, newFragment, tag)
                .commit();

        return newFragment;
    }

    public static Fragment findFragment(FragmentManager supportFragmentManager, Fragment newFragment, String ... tag) {
        String newFragmentClass = newFragment.getClass().getName();

        Fragment oldFragment = supportFragmentManager.findFragmentById(newFragment.getId());
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragment.getTag());
        if(oldFragment == null && tag.length > 0)
            oldFragment = supportFragmentManager.findFragmentByTag(tag[0]);
        if(oldFragment == null)
            oldFragment = supportFragmentManager.findFragmentByTag(newFragmentClass);

        if (oldFragment != null) {
            newFragment = oldFragment;
            Log.d("Fragments", "Fragment founded: " +newFragmentClass);
        }

        return newFragment;
    }

    public static void removeFragment(FragmentManager supportFragmentManager, Fragment fragment) {
        supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit();
    }
}
