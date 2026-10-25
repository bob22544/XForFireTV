package tv.xremote;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 11, 16);
    private static final int PANEL = Color.rgb(19, 25, 33);
    private static final int BLUE = Color.rgb(29, 155, 240);
    private WebView web;
    private LinearLayout nav;
    private int selected = 0;
    private final String[] labels = {"Home", "Explore", "Bookmarks", "Notifications", "Messages", "Profile", "Log in"};
    private final String[] urls = {
        "https://x.com/home", "https://x.com/explore", "https://x.com/i/bookmarks",
        "https://x.com/notifications", "https://x.com/messages", "https://x.com/settings/profile", "https://x.com/i/flow/login"
    };
    private Button[] buttons;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        CookieManager.getInstance().setAcceptCookie(true);
        buildUi();
        web.loadUrl(urls[0]);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(BG);
        setContentView(root);

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.VERTICAL);
        nav.setPadding(18, 18, 18, 18);
        nav.setBackgroundColor(PANEL);
        root.addView(nav, new LinearLayout.LayoutParams(dp(205), ViewGroup.LayoutParams.MATCH_PARENT));

        TextView brand = new TextView(this);
        brand.setText("𝕏   FOR TV");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(22);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setPadding(12, 4, 4, 20);
        nav.addView(brand, new LinearLayout.LayoutParams(-1, dp(65)));

        buttons = new Button[labels.length];
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            Button b = new Button(this);
            buttons[i] = b;
            b.setText(labels[i]);
            b.setAllCaps(false);
            b.setTextSize(16);
            b.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            b.setPadding(dp(14), 0, dp(8), 0);
            b.setTextColor(Color.WHITE);
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(i == 0 ? BLUE : PANEL));
            b.setFocusable(true);
            b.setOnClickListener(v -> navigate(index));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(54));
            lp.bottomMargin = dp(5);
            nav.addView(b, lp);
        }

        web = new WebView(this);
        web.setBackgroundColor(BG);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " XForFireTV/1.0");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                return host != null && !(host.equals("x.com") || host.endsWith(".x.com") || host.equals("twitter.com") || host.endsWith(".twitter.com") || host.equals("twimg.com") || host.endsWith(".twimg.com") || host.equals("t.co"));
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Increase hit targets and make keyboard focus visible without taking over X's layout.
                String css = "javascript:(function(){if(document.getElementById('xftv-css'))return;var s=document.createElement('style');s.id='xftv-css';s.textContent='button,[role=button],a,[tabindex]:not([tabindex=\'-1\']){outline-offset:3px} button:focus,[role=button]:focus,a:focus,[tabindex]:focus{outline:3px solid #1d9bf0!important;border-radius:8px!important} video{max-width:100%;}';document.head.appendChild(s);})();";
                view.evaluateJavascript(css.replace("javascript:", ""), null);
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        updateSelection();
    }

    private void navigate(int index) {
        selected = index;
        updateSelection();
        web.loadUrl(urls[index]);
        web.requestFocus();
    }

    private void updateSelection() {
        if (buttons == null) return;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(i == selected ? BLUE : PANEL));
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            buttons[ selected ].requestFocus();
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (web != null && web.canGoBack()) { web.goBack(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
}
