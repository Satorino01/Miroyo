package com.example.kobayashi_satoru.miroyo.ui.sendmovie;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kobayashi_satoru.miroyo.R;

public class SendMovieFragment extends Fragment {

    private SendMovieViewModel mViewModel;

    public static SendMovieFragment newInstance() {
        return new SendMovieFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_movie_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SendMovieViewModel.class);
        // TODO: Use the ViewModel
    }

}
