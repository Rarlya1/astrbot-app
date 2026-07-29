package com.astrbot.app.ui.terminal;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.astrbot.app.R;

public class TerminalTabView extends Fragment {

    private TextView outputView;
    private ScrollView scrollView;
    private final StringBuilder buffer = new StringBuilder();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal_tab, container, false);
        outputView = view.findViewById(R.id.terminal_output);
        scrollView = view.findViewById(R.id.terminal_scroll);
        return view;
    }

    public void appendOutput(String text) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            buffer.append(text);
            SpannableString spannable = new SpannableString(buffer.toString());
            spannable.setSpan(new ForegroundColorSpan(0xFF00FF00),
                    0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            outputView.setText(spannable);
            scrollView.fullScroll(View.FOCUS_DOWN);
        });
    }
}
