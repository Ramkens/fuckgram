package org.telegram.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FuckgramConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

/**
 * fuckgram: settings screen (ghost mode, keep deleted messages, settings pack export / import, about).
 */
public class FuckgramSettingsActivity extends BaseFragment {

    private LinearLayout list;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.FuckgramSettings));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = scrollView;

        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(list, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        addHeader(context, LocaleController.getString(R.string.FuckgramGhostHeader));
        addSwitch(context, FuckgramConfig.GHOST_NO_READ, LocaleController.getString(R.string.FuckgramGhostNoRead), true);
        addSwitch(context, FuckgramConfig.GHOST_NO_TYPING, LocaleController.getString(R.string.FuckgramGhostNoTyping), true);
        addSwitch(context, FuckgramConfig.GHOST_OFFLINE, LocaleController.getString(R.string.FuckgramGhostOffline), false);
        addInfo(context, LocaleController.getString(R.string.FuckgramGhostInfo));

        addHeader(context, LocaleController.getString(R.string.FuckgramMessagesHeader));
        addSwitch(context, FuckgramConfig.KEEP_DELETED, LocaleController.getString(R.string.FuckgramKeepDeleted), false);
        addInfo(context, LocaleController.getString(R.string.FuckgramKeepDeletedInfo));

        addHeader(context, LocaleController.getString(R.string.FuckgramProfileHeader));
        addSwitch(context, FuckgramConfig.SHOW_BADGE, LocaleController.getString(R.string.FuckgramShowBadge), false);
        addInfo(context, LocaleController.getString(R.string.FuckgramShowBadgeInfo));

        addHeader(context, LocaleController.getString(R.string.FuckgramPackHeader));
        addButton(context, LocaleController.getString(R.string.FuckgramExport), true, v -> exportPack(context));
        addButton(context, LocaleController.getString(R.string.FuckgramImport), false, v -> importPack(context));
        addInfo(context, LocaleController.getString(R.string.FuckgramPackInfo));

        addButton(context, LocaleController.getString(R.string.FuckgramAbout), false, v -> presentFragment(new FuckgramAboutActivity()));
        addInfo(context, "");

        return fragmentView;
    }

    private void addHeader(Context context, String text) {
        TextView header = new TextView(context);
        header.setText(text);
        header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        header.setTypeface(AndroidUtilities.bold());
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        header.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(16), AndroidUtilities.dp(21), AndroidUtilities.dp(8));
        list.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addInfo(Context context, String text) {
        TextView info = new TextView(context);
        info.setText(text);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        info.setGravity(Gravity.LEFT);
        info.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(8), AndroidUtilities.dp(21), AndroidUtilities.dp(8));
        list.addView(info, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addSwitch(Context context, String key, String title, boolean divider) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setTextAndCheck(title, FuckgramConfig.get(key), divider);
        cell.setOnClickListener(v -> {
            boolean value = !cell.isChecked();
            FuckgramConfig.set(key, value);
            cell.setChecked(value);
        });
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void addButton(Context context, String title, boolean divider, View.OnClickListener listener) {
        TextCell cell = new TextCell(context);
        cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        cell.setText(title, divider);
        cell.setOnClickListener(listener);
        list.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void exportPack(Context context) {
        String json = FuckgramConfig.exportPack();
        if (json == null) {
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("fuckgram", json));
            Toast.makeText(context, LocaleController.getString(R.string.FuckgramExported), Toast.LENGTH_SHORT).show();
        }
    }

    private void importPack(Context context) {
        String text = null;
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence cs = clipboard.getPrimaryClip().getItemAt(0).coerceToText(context);
                text = cs != null ? cs.toString() : null;
            }
        } catch (Throwable ignore) {
        }
        boolean ok = text != null && FuckgramConfig.importPack(text);
        Toast.makeText(context, LocaleController.getString(ok ? R.string.FuckgramImported : R.string.FuckgramImportFailed), Toast.LENGTH_SHORT).show();
        if (ok) {
            // rebuild the screen so the switches show the imported values
            list.removeAllViews();
            createView(context);
            ((ScrollView) fragmentView).requestLayout();
            finishFragment();
            presentFragment(new FuckgramSettingsActivity());
        }
    }
}
