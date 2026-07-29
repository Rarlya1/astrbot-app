package com.astrbot.app.ui.terminal;
import com.android.astrbot.R;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.astrbot.app.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TerminalFragment extends Fragment {

    private ViewPager2 tabPager;
    private EditText commandInput;
    private ImageButton btnSend;
    private TerminalTabAdapter adapter;
    private Process shellProcess;
    private OutputStream shellInput;
    private Thread outputReader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terminal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tabPager = view.findViewById(R.id.tab_pager);
        commandInput = view.findViewById(R.id.command_input);
        btnSend = view.findViewById(R.id.btn_send);

        adapter = new TerminalTabAdapter(requireActivity());
        tabPager.setAdapter(adapter);

        tabPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                adapter.setCurrentPosition(position);
            }
        });

        startProotShell();

        btnSend.setOnClickListener(v -> sendCommand());

        commandInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                sendCommand();
                return true;
            }
            return false;
        });
    }

    private void startProotShell() {
        try {
            Context ctx = requireContext();
            File filesDir = ctx.getFilesDir();
            File ubuntuDir = new File(filesDir, "ubuntu");

            // Extract ubuntu image if not exists
            if (!ubuntuDir.exists()) {
                appendToTab("Extracting Ubuntu environment...\n");
                ubuntuDir.mkdirs();
                extractUbuntuImage(ctx, ubuntuDir);
            }

            String nativeLibDir = ctx.getApplicationInfo().nativeLibDir;

            // Busybox path
            String busybox = nativeLibDir + "/libbusybox.so";

            // Build proot command
            List<String> cmd = new ArrayList<>();
            cmd.add(nativeLibDir + "/libproot.so");
            cmd.add("-0");  // fake root
            cmd.add("-r");
            cmd.add(ubuntuDir.getAbsolutePath());
            cmd.add("-b");
            cmd.add("/dev");
            cmd.add("-b");
            cmd.add("/proc");
            cmd.add("-b");
            cmd.add("/sys");
            cmd.add("-b");
            cmd.add(filesDir.getAbsolutePath() + ":/data/data/host");
            cmd.add("-w");
            cmd.add("/root");
            cmd.add("/usr/bin/env");
            cmd.add("-i");
            cmd.add("PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
            cmd.add("HOME=/root");
            cmd.add("TERM=xterm-256color");
            cmd.add("/bin/bash");
            cmd.add("--login");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(ubuntuDir);
            pb.environment().put("LD_LIBRARY_PATH", nativeLibDir);
            shellProcess = pb.start();
            shellInput = shellProcess.getOutputStream();

            appendToTab("Ubuntu environment started.\n");

            // Read output in background
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(shellProcess.getInputStream()));
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(shellProcess.getErrorStream()));

            outputReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendToTab(line + "\n");
                    }
                } catch (Exception ignored) {}
            });
            outputReader.start();

            new Thread(() -> {
                try {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        appendToTab(line + "\n");
                    }
                } catch (Exception ignored) {}
            }).start();

        } catch (Exception e) {
            appendToTab("Failed to start proot: " + e.getMessage() + "\n");
            startFallbackShell();
        }
    }

    private void startFallbackShell() {
        try {
            shellProcess = Runtime.getRuntime().exec("/system/bin/sh");
            shellInput = shellProcess.getOutputStream();
            appendToTab("Fallback shell started.\n");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(shellProcess.getInputStream()));

            outputReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendToTab(line + "\n");
                    }
                } catch (Exception ignored) {}
            });
            outputReader.start();
        } catch (Exception e) {
            appendToTab("Fallback shell also failed: " + e.getMessage() + "\n");
        }
    }

    private void extractUbuntuImage(Context ctx, File targetDir) {
        try {
            java.io.InputStream is = ctx.getAssets().open("ubuntu-noble-aarch64-pd-v4.18.0.tar.xz");
            java.io.File tmp = new File(ctx.getCacheDir(), "ubuntu.tar.xz");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();
            is.close();

            // Extract with busybox tar
            String nativeLibDir = ctx.getApplicationInfo().nativeLibDir;
            Process extract = Runtime.getRuntime().exec(new String[]{
                    nativeLibDir + "/libbusybox.so", "tar", "-xJf",
                    tmp.getAbsolutePath(), "-C", targetDir.getAbsolutePath()
            });
            extract.waitFor();
            tmp.delete();
        } catch (Exception e) {
            appendToTab("Extract error: " + e.getMessage() + "\n");
        }
    }

    private void sendCommand() {
        String cmd = commandInput.getText().toString().trim();
        if (cmd.isEmpty()) return;
        commandInput.setText("");

        if (shellInput != null) {
            try {
                shellInput.write((cmd + "\n").getBytes());
                shellInput.flush();
                appendToTab("$ " + cmd + "\n");
            } catch (Exception e) {
                appendToTab("Error: " + e.getMessage() + "\n");
            }
        }
    }

    private void appendToTab(String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                TerminalTabView currentTab = adapter.getCurrentTab();
                if (currentTab != null) {
                    currentTab.appendOutput(text);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (outputReader != null) outputReader.interrupt();
        if (shellProcess != null) shellProcess.destroy();
    }
}
