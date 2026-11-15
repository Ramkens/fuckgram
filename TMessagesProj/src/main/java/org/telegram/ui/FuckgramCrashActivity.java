package org.telegram.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.FuckgramCrashHandler;

/**
 * fuckgram: plain framework screen with the crash report. Runs in its own process (see the manifest),
 * so it uses nothing from the Telegram client.
 */
public class FuckgramCrashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String report = getIntent() != null && getIntent().getStringExtra(FuckgramCrashHandler.EXTRA_REPORT) != null
                ? getIntent().getStringExtra(FuckgramCrashHandler.EXTRA_REPORT) : "(no report)";

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView title = new TextView(this);
        title.setText("fuckgram crashed");
        title.setTextColor(0xffff2e4d);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView hint = new TextView(this);
        hint.setText("Tap Copy and send this text to the developer.");
        hint.setTextColor(0xffaaaaaa);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hint.setPadding(0, pad / 2, 0, pad / 2);
        root.addView(hint, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        TextView text = new TextView(this);
        text.setText(report);
        text.setTextColor(Color.WHITE);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        text.setTypeface(Typeface.MONOSPACE);
        text.setTextIsSelectable(true);
        scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        Button copy = new Button(this);
        copy.setText("Copy");
        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("fuckgram crash", report));
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
        Button share = new Button(this);
        share.setText("Share");
        share.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, report);
            startActivity(Intent.createChooser(send, "Share crash report"));
        });
        Button close = new Button(this);
        close.setText("Close");
        close.setOnClickListener(v -> finishAndRemoveTask());
        buttons.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));
        buttons.addView(share, new LinearLayout.LayoutParams(0, -2, 1f));
        buttons.addView(close, new LinearLayout.LayoutParams(0, -2, 1f));
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }
}
