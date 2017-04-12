package com.moez.QKSMS.ui;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import com.moez.QKSMS.R;

/**
 * Created by seanjaffe on 4/5/17.
 */

public class TutorialSlidePagerActiviy extends FragmentActivity {

    private static final int NUM_PAGES = 3;

    /**
     * Pager widget, handles animation
     */
    private ViewPager mPager;

    /**
     * Pager adapter, provides pages to view pager widget
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_slide);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
         switch(position) {
             case 1:
                 return new ScreenSlidePageFragmentOne();

             case 2:
                 return new ScreenSlidePageFragmentOne();

             case 3:
                 return new ScreenSlidePageFragmentOne();
             default:
                 return null;
         }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
