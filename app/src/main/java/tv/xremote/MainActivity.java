package tv.xremote;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 11, 16);
    private static final int PANEL = Color.rgb(19, 25, 33);
    private static final int BLUE = Color.rgb(29, 155, 240);
    private static final int LIGHT_BG = Color.rgb(238, 242, 246);
    private static final int LIGHT_PANEL = Color.rgb(255, 255, 255);
    private WebView web;
    private LinearLayout nav;
    private LinearLayout root;
    private FrameLayout content;
    private View customVideoView;
    private WebChromeClient.CustomViewCallback customVideoCallback;
    private int selected = 0;
    private boolean darkMode = true;
    private Button themeButton;
    private final String[] labels = {"Home", "Explore", "Bookmarks", "Notifications", "Messages", "Profile", "Log in"};
    private final String[] urls = {
        "https://x.com/home", "https://x.com/explore", "https://x.com/i/bookmarks",
        "https://x.com/notifications", "https://x.com/messages", "https://x.com/settings/profile", "https://x.com/i/flow/login"
    };
    private Button[] buttons;
    private int previousSystemUiVisibility;
    private boolean fullscreenExitInProgress = false;
    private static final String PREFS = "xftv_preferences";
    private static final String PREF_DARK = "dark_mode";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CookieManager.getInstance().setAcceptCookie(true);
        darkMode = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_DARK, true);
        buildUi();
        web.loadUrl(urls[0]);
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(BG);
        setContentView(root);

        nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.VERTICAL);
        nav.setPadding(dp(16), dp(14), dp(16), dp(14));
        nav.setBackgroundColor(PANEL);
        root.addView(nav, new LinearLayout.LayoutParams(dp(210), ViewGroup.LayoutParams.MATCH_PARENT));

        TextView brand = new TextView(this);
        brand.setText("𝕏   FOR TV");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(22);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setPadding(dp(12), 0, 0, dp(12));
        nav.addView(brand, new LinearLayout.LayoutParams(-1, dp(58)));

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
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(51));
            lp.bottomMargin = dp(4);
            nav.addView(b, lp);
        }

        themeButton = new Button(this);
        themeButton.setText(darkMode ? "☾  Dark mode: ON" : "☀  Dark mode: OFF");
        themeButton.setAllCaps(false);
        themeButton.setTextSize(14);
        themeButton.setTextColor(Color.WHITE);
        themeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(PANEL));
        themeButton.setOnClickListener(v -> toggleTheme());
        LinearLayout.LayoutParams themeLp = new LinearLayout.LayoutParams(-1, dp(48));
        themeLp.topMargin = dp(8);
        nav.addView(themeButton, themeLp);

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        web = new WebView(this);
        web.setBackgroundColor(BG);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setUserAgentString(s.getUserAgentString() + " XForFireTV/1.3");
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customVideoView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customVideoView = view;
                customVideoCallback = callback;
                previousSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                web.setVisibility(View.GONE);
                nav.setVisibility(View.GONE);
                content.addView(view, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                view.setFocusableInTouchMode(true);
                view.setFocusable(true);
                view.requestFocus();
            }

            @Override public void onHideCustomView() {
                if (!fullscreenExitInProgress) exitFullscreen();
            }

            @Override public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                return host != null && !(host.equals("x.com") || host.endsWith(".x.com") ||
                    host.equals("twitter.com") || host.endsWith(".twitter.com") ||
                    host.equals("twimg.com") || host.endsWith(".twimg.com") ||
                    host.equals("t.co") || host.equals("youtube.com") || host.endsWith(".youtube.com") ||
                    host.equals("youtu.be") || host.equals("googlevideo.com") || host.endsWith(".googlevideo.com"));
            }

            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyTvBrowsingMode();
                applyTheme();
                web.requestFocus();
            }
        });
        content.addView(web, new FrameLayout.LayoutParams(-1, -1));
        updateSelection();
    }

    private void applyTvBrowsingMode() {
        // X is a single-page app, so install a delegated handler and observe later
        // feed updates rather than patching only the first page load.
        String js = "(function(){" +
            "if(window.__xftvRemoteInstalled)return;window.__xftvRemoteInstalled=true;" +
            "var style=document.createElement('style');style.id='xftv-style';" +
            "style.textContent=" +
            "'html,body{scroll-behavior:smooth!important;}' +" +
            "'[data-testid=\"sidebarColumn\"]{display:none!important;}' +" +
            "'nav[aria-label=\"Primary\"]{display:none!important;}' +" +
            "'article[data-testid=\"tweet\"].xftv-selected-post{outline:3px solid #1d9bf0!important;outline-offset:5px!important;border-radius:12px!important;}' +" +
            "'video{max-width:100%;}' +" +
            "'[data-testid=\"tweet\"]{scroll-margin-top:28px;}' ;" +
            "document.head.appendChild(style);" +
            "function posts(){return Array.from(document.querySelectorAll('article[data-testid=\"tweet\"]')).filter(function(p){var r=p.getBoundingClientRect();return r.width>0&&r.height>0;});}" +
            "function postOf(el){return el&&el.closest?el.closest('article[data-testid=\"tweet\"]'):null;}" +
            "function mark(p){if(!p)return null;document.querySelectorAll('article.xftv-selected-post').forEach(function(x){x.classList.remove('xftv-selected-post');});" +
            "p.classList.add('xftv-selected-post');p.setAttribute('tabindex','-1');try{p.focus({preventScroll:true});}catch(e){p.focus();}p.scrollIntoView({behavior:'smooth',block:'center'});return p;}" +
            "function visiblePost(){var all=posts();if(!all.length)return null;var y=window.innerHeight*0.45;return all.reduce(function(best,p){var r=p.getBoundingClientRect();var d=Math.abs((r.top+r.bottom)/2-y);return !best||d<best.d?{p:p,d:d}:best;},null).p;}" +
            "function move(delta){var all=posts();if(!all.length)return;var cur=postOf(document.activeElement)||document.querySelector('article.xftv-selected-post');" +
            "var i=all.indexOf(cur);if(i<0){mark(visiblePost());return;}var next=Math.max(0,Math.min(all.length-1,i+delta));" +
            "if(next===i){all[i].scrollIntoView({behavior:'smooth',block:'center'});return;}mark(all[next]);}" +
            "function videoIn(p){if(!p)return null;var vs=Array.from(p.querySelectorAll('video'));return vs.find(function(v){var r=v.getBoundingClientRect();return r.width>0&&r.height>0;})||null;}" +
            "function goFullscreen(v){if(!v)return;try{if(v.requestFullscreen){var q=v.requestFullscreen();if(q&&q.catch)q.catch(function(){});}" +
            "else if(v.webkitEnterFullscreen){v.webkitEnterFullscreen();}" +
            "else{var c=v.closest('[data-testid*=\"video\"]')||v.parentElement;if(c&&c.requestFullscreen)c.requestFullscreen();}}catch(e){}}" +
            "document.addEventListener('keydown',function(e){" +
            "var k=e.key,code=e.keyCode||e.which;var down=(k==='ArrowDown'||code===40),up=(k==='ArrowUp'||code===38);" +
            "var enter=(k==='Enter'||k===' '||code===13||code===23||code===66);" +
            "var target=e.target,active=document.activeElement,post=postOf(target)||postOf(active)||document.querySelector('article.xftv-selected-post');" +
            "var typing=target&&(target.tagName==='INPUT'||target.tagName==='TEXTAREA'||target.isContentEditable);" +
            "if(typing)return;" +
            "if(down||up){" +
            "var inVideoUi=target&&(target.closest('video')||target.closest('[aria-label*=\"Seek\"]')||target.closest('[role=\"slider\"]')||target.matches('input[type=\"range\"]'));" +
            "if(inVideoUi||post||target===document.body||target===document.documentElement){" +
            "e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();move(down?1:-1);return;}}" +
            "if(enter&&post&&document.activeElement===post){" +
            "var v=videoIn(post);if(v){e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();" +
            "if(!v.paused){goFullscreen(v);return;}var result=v.play();if(result&&result.catch)result.catch(function(){});post.dataset.xftvVideoStarted='1';" +
            "v.addEventListener('playing',function(){post.dataset.xftvVideoStarted='1';},{once:true});return;}}" +
            "},true);" +
            "document.addEventListener('focusin',function(e){var p=postOf(e.target);if(p&&!e.target.matches('button,a,input,[role=\"button\"],[role=\"slider\"]')){mark(p);}},true);" +
            "var observer=new MutationObserver(function(){if(!document.getElementById('xftv-style'))document.head.appendChild(style);if(!document.querySelector('article.xftv-selected-post')&&posts().length)mark(visiblePost());});" +
            "observer.observe(document.documentElement,{childList:true,subtree:true});" +
            "document.querySelectorAll('video').forEach(function(v){v.setAttribute('playsinline','true');v.addEventListener('playing',function(){var p=postOf(v);if(p)p.dataset.xftvVideoStarted='1';});});" +
            "})();";
        web.evaluateJavascript(js, null);
    }

    private void applyTheme() {
        if (web == null) return;
        String js;
        if (darkMode) {
            js = "document.documentElement.style.colorScheme='dark';" +
                 "document.documentElement.setAttribute('data-xftv-theme','dark');";
        } else {
            js = "document.documentElement.style.colorScheme='light';" +
                 "document.documentElement.setAttribute('data-xftv-theme','light');";
        }
        web.evaluateJavascript(js, null);
    }

    private void toggleTheme() {
        darkMode = !darkMode;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_DARK, darkMode).apply();
        themeButton.setText(darkMode ? "☾  Dark mode: ON" : "☀  Dark mode: OFF");
        int background = darkMode ? BG : LIGHT_BG;
        int panel = darkMode ? PANEL : LIGHT_PANEL;
        root.setBackgroundColor(background);
        nav.setBackgroundColor(panel);
        web.setBackgroundColor(background);
        themeButton.setTextColor(darkMode ? Color.WHITE : Color.BLACK);
        themeButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(panel));
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setTextColor(darkMode ? Color.WHITE : Color.BLACK);
            buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(i == selected ? BLUE : panel));
        }
        applyTheme();
        Toast.makeText(this, darkMode ? "Dark mode enabled" : "Light mode enabled", Toast.LENGTH_SHORT).show();
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
            buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(i == selected ? BLUE : (darkMode ? PANEL : LIGHT_PANEL)));
            buttons[i].setTextColor(darkMode ? Color.WHITE : Color.BLACK);
        }
    }

    private void exitFullscreen() {
        if (customVideoView == null || fullscreenExitInProgress) return;
        fullscreenExitInProgress = true;
        View videoView = customVideoView;
        WebChromeClient.CustomViewCallback callback = customVideoCallback;
        customVideoView = null;
        customVideoCallback = null;
        content.removeView(videoView);
        web.setVisibility(View.VISIBLE);
        nav.setVisibility(View.VISIBLE);
        getWindow().getDecorView().setSystemUiVisibility(previousSystemUiVisibility);
        if (callback != null) callback.onCustomViewHidden();
        web.requestFocus();
        fullscreenExitInProgress = false;
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN || code == KeyEvent.KEYCODE_VOLUME_MUTE) {
            return super.dispatchKeyEvent(event);
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_BACK && customVideoView != null) {
            exitFullscreen();
            return true;
        }
        // If the web player's seek control captures the remote in full-screen,
        // Up/Down is an escape hatch back to the feed. Left/Right remain available
        // to seek within the video.
        if (event.getAction() == KeyEvent.ACTION_DOWN && customVideoView != null &&
                (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN)) {
            exitFullscreen();
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_MENU) {
            if (buttons != null && selected >= 0 && selected < buttons.length) buttons[selected].requestFocus();
            return true;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_DPAD_LEFT && web != null && customVideoView == null) {
            // A left press at the start of the page is a quick way back to the app's section menu.
            web.evaluateJavascript("(function(){return window.scrollX===0 && (document.activeElement===document.body || document.activeElement===document.documentElement);})()", value -> {
                if ("true".equals(value)) buttons[selected].requestFocus();
            });
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_BACK) {
            if (web != null && web.canGoBack()) { web.goBack(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public void onBackPressed() {
        if (customVideoView != null) { exitFullscreen(); return; }
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (web != null) { web.stopLoading(); web.destroy(); web = null; }
        super.onDestroy();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
}
