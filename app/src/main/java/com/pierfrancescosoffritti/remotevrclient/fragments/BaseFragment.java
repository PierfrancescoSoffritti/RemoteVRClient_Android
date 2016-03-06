package com.pierfrancescosoffritti.remotevrclient.fragments;

import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by  Pierfrancesco on 06/03/2016.
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
