package com.example.kobayashi_satoru.miroyo.ui.setmovie;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kobayashi_satoru.miroyo.R;

public class SetMovieFragment extends Fragment {

    private SetMovieViewModel mViewModel;

    public static SetMovieFragment newInstance() {
        return new SetMovieFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.set_video_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SetMovieViewModel.class);
        // TODO: Use the ViewModel
    }

}
