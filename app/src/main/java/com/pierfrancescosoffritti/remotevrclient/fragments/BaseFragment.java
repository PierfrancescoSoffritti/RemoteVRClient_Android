package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * abstract fragment with basic functionalities
 *
 * @author Pierfrancesco Soffritti
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregister();
    }

    protected abstract void unregister();
    protected abstract void register();
}
