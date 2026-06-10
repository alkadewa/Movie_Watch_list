package com.example.moviewatchlist;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return WatchlistFragment.newInstance("Want to Watch");
            case 1: return WatchlistFragment.newInstance("Watching");
            case 2: return WatchlistFragment.newInstance("Watched");
            default: return WatchlistFragment.newInstance("Want to Watch");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
