package com.android.astrbot.ui.settings;
import com.android.astrbot.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.astrbot.service.AstrBotForegroundService;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    private Switch switchForeground;
    private TextInputEditText serverUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchForeground = view.findViewById(R.id.switch_foreground);
        serverUrl = view.findViewById(R.id.server_url);
        View btnPassword = view.findViewById(R.id.btn_password);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("astrbot_config", 0);

        boolean serviceRunning = prefs.getBoolean("foreground_service", false);
        switchForeground.setChecked(serviceRunning);
        serverUrl.setText(prefs.getString("server_url", "http://localhost:6189"));

        switchForeground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("foreground_service", isChecked).apply();
            Intent intent = new Intent(requireActivity(), AstrBotForegroundService.class);
            if (isChecked) {
                requireActivity().startForegroundService(intent);
            } else {
                requireActivity().stopService(intent);
            }
        });

        btnPassword.setOnClickListener(v -> {
            // Password management - show dialog or navigate
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("astrbot_config", 0);
        String url = serverUrl.getText() != null ? serverUrl.getText().toString() : "";
        prefs.edit().putString("server_url", url).apply();
    }
}
