package org.telegram.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/**
 * fuckgram: "About" screen with the logo, version and the list of developers.
 */
public class FuckgramAboutActivity extends BaseFragment {

    public static final String CREATOR_USERNAME = "scamchrome";

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.FuckgramAbout));
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
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView = scrollView;

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(36), AndroidUtilities.dp(16), AndroidUtilities.dp(24));
        scrollView.addView(content, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        // Logo
        ImageView logo = new ImageView(context);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setImageResource(R.drawable.fg_logo);
        content.addView(logo, LayoutHelper.createLinear(128, 128, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 20));

        // Title (display font)
        TextView title = new TextView(context);
        title.setText(LocaleController.getString(R.string.AppName));
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setGravity(Gravity.CENTER);
        Typeface display = AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_FUCKGRAM_DISPLAY);
        if (display != null) {
            title.setTypeface(display);
        }
        content.addView(title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 6));

        // Tagline + version
        TextView tagline = new TextView(context);
        tagline.setText(LocaleController.getString(R.string.FuckgramTagline) + " · v" + BuildVars.BUILD_VERSION_STRING);
        tagline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tagline.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        tagline.setGravity(Gravity.CENTER);
        content.addView(tagline, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 32));

        // Section header
        TextView header = new TextView(context);
        header.setText(LocaleController.getString(R.string.FuckgramDevelopers));
        header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        header.setTypeface(AndroidUtilities.bold());
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader));
        content.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT, 8, 0, 8, 8));

        // Developer card
        content.addView(createDeveloperRow(context, "@" + CREATOR_USERNAME, LocaleController.getString(R.string.FuckgramCreator)),
                LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 20));

        // Legal note
        TextView legal = new TextView(context);
        legal.setText(LocaleController.getString(R.string.FuckgramBasedOn));
        legal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        legal.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        legal.setGravity(Gravity.CENTER);
        content.addView(legal, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 16, 8, 16, 0));

        return fragmentView;
    }

    private View createDeveloperRow(Context context, String name, String role) {
        FrameLayout card = new FrameLayout(context);
        card.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(14), Theme.getColor(Theme.key_windowBackgroundWhite)));
        card.setPadding(AndroidUtilities.dp(16), AndroidUtilities.dp(14), AndroidUtilities.dp(16), AndroidUtilities.dp(14));

        // Avatar: accent circle with the first letter of the nickname
        TextView avatar = new TextView(context);
        String letter = name.length() > 1 ? name.substring(1, 2).toUpperCase() : "?";
        avatar.setText(letter);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        avatar.setTextColor(0xffffffff);
        avatar.setTypeface(AndroidUtilities.bold());
        avatar.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(24), Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader)));
        card.addView(avatar, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        card.addView(texts, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 62, 0, 0, 0));

        TextView nameView = new TextView(context);
        nameView.setText(name);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        nameView.setTypeface(AndroidUtilities.bold());
        nameView.setSingleLine();
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        nameView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        texts.addView(nameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        TextView roleView = new TextView(context);
        roleView.setText(role);
        roleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        roleView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        texts.addView(roleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 0));

        card.setOnClickListener(v -> {
            if (getParentActivity() != null) {
                Browser.openUrl(getParentActivity(), "https://t.me/" + CREATOR_USERNAME);
            }
        });
        return card;
    }
}
