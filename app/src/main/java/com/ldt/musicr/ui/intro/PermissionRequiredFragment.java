package com.ldt.musicr.ui.intro;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ldt.musicr.R;
import com.ldt.musicr.ui.AppActivity;
import com.ldt.musicr.ui.widget.fragmentnavigationcontroller.NavigationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PermissionRequiredFragment extends NavigationFragment implements AppActivity.PermissionListener {
    private static final String TAG ="IntroStepOneFragment";




    @BindView(R.id.allow_button)
    ImageView mAllowButton;

    @OnClick(R.id.allow_button)
    void allowAccess() {
        getMainActivity().requestPermission();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        getMainActivity().setPermissionListener(this);
        return inflater.inflate(R.layout.screen_grant_permission,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getMainActivity().removePermissionListener();

    }


    @Override
    public void onPermissionGranted() {

        Log.d(TAG, "onPermissionGranted");
        getMainActivity().showMainUI();
    }

    @Override
    public void onPermissionDenied() {

        Log.d(TAG, "onPermissionDenied");
    }
}
