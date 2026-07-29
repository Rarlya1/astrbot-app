package com.android.astrbot.ui.terminal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class TerminalTabAdapter extends FragmentStateAdapter {

    private final List<TerminalTabView> tabs = new ArrayList<>();
    private int currentPosition = 0;

    public TerminalTabAdapter(@NonNull FragmentActivity activity) {
        super(activity);
        // Create 3 default tabs
        for (int i = 0; i < 3; i++) {
            tabs.add(new TerminalTabView());
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        while (tabs.size() <= position) {
            tabs.add(new TerminalTabView());
        }
        return tabs.get(position);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    public TerminalTabView getCurrentTab() {
        if (currentPosition < tabs.size()) {
            return tabs.get(currentPosition);
        }
        return null;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }
}
