package com.moez.QKSMS.ui.welcome;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

public class WelcomePagerAdapter extends FragmentPagerAdapter {
    private final String TAG = "WelcomePagerAdapter";

    private Fragment[] mFragments = new Fragment[7];


    public final int PAGE_INTRO = 0;
    public final int PAGE_THEME = 1;
    public final int PAGE_NIGHT = 2;
    public final int PAGE_AGREEMENT = 3;
    public final int PAGE_COMPENSATION = 4;
    public final int PAGE_PRIVACY = 5;
    public final int PAGE_SUMMARY = 6;

    public WelcomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (mFragments[i] == null) {
            switch (i) {
                case PAGE_INTRO:
                    mFragments[i] = new WelcomeIntroFragment();
                    break;
                case PAGE_THEME:
                    mFragments[i] = new WelcomeThemeFragment();
                    break;
                case PAGE_NIGHT:
                    mFragments[i] = new WelcomeNightFragment();
                    break;
                case PAGE_AGREEMENT:
                    mFragments[i] = new WelcomeAgreementFragment();
                    break;
                case PAGE_COMPENSATION:
                    mFragments[i] = new WelcomeCompensationFragment();
                    break;
                case PAGE_PRIVACY:
                    mFragments[i] = new WelcomePrivacyFragment();
                    break;
                case PAGE_SUMMARY:
                    mFragments[i] = new WelcomeSummaryFragment();
                    break;
                default:
                    Log.e(TAG, "Uh oh, the pager requested a fragment at index " + i + "which doesn't exist");
            }
        }

        return mFragments[i];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }
}
