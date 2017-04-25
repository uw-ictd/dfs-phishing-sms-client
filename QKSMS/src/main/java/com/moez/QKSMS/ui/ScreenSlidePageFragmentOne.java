package com.moez.QKSMS.ui;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.R;

public class ScreenSlidePageFragmentOne extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_tutorial_slide_one, container, false);

        return rootView;
    }

}