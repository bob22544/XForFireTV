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
        // X is a single-page app. Reinstall the handlers only once per document,
        // while a MutationObserver keeps styling new feed items as they arrive.
        String js = "(function(){" +
            "if(window.__xftvRemoteInstalled)return;window.__xftvRemoteInstalled=true;" +
            "var style=document.createElement('style');style.id='xftv-style';" +
            "style.textContent='html,body{scroll-behavior:smooth!important;}'+" +
            "'[data-testid=\"sidebarColumn\"]{display:none!important;}'+" +
            "'nav[aria-label=\"Primary\"]{display:none!important;}'+" +
            "'article[data-testid=\"tweet\"].xftv-selected-post{outline:3px solid #1d9bf0!important;outline-offset:4px!important;border-radius:12px!important;}'+" +
            "'video{max-width:100%;}'+ '[data-testid=\"tweet\"]{scroll-margin-top:28px;}';" +
            "document.head.appendChild(style);" +
            "var lastVideo=null;" +
            "function posts(){return Array.from(document.querySelectorAll('article[data-testid=\"tweet\"]')).filter(function(p){var r=p.getBoundingClientRect();return r.width>0&&r.height>0;});}" +
            "function postOf(el){return el&&el.closest?el.closest('article[data-testid=\"tweet\"]'):null;}" +
            "function mark(p,scroll){if(!p)return null;document.querySelectorAll('article.xftv-selected-post').forEach(function(x){x.classList.remove('xftv-selected-post');});p.classList.add('xftv-selected-post');p.setAttribute('tabindex','-1');try{p.focus({preventScroll:true});}catch(e){}if(scroll!==false)p.scrollIntoView({behavior:'smooth',block:'center'});return p;}" +
            "function visiblePost(){var all=posts();if(!all.length)return null;var y=window.innerHeight*.45;return all.reduce(function(best,p){var r=p.getBoundingClientRect(),d=Math.abs((r.top+r.bottom)/2-y);return !best||d<best.d?{p:p,d:d}:best;},null).p;}" +
            "function currentPost(){return postOf(document.activeElement)||document.querySelector('article.xftv-selected-post')||visiblePost();}" +
            "function move(delta){var all=posts();if(!all.length)return;var cur=currentPost(),i=all.indexOf(cur);if(i<0){mark(visiblePost());return;}var next=Math.max(0,Math.min(all.length-1,i+delta));mark(all[next]);}" +
            "function visibleVideo(p){if(!p)return null;var vs=Array.from(p.querySelectorAll('video'));return vs.find(function(v){var r=v.getBoundingClientRect();return r.width>0&&r.height>0;})||vs[0]||null;}" +
            "function pickVideo(){var fs=document.fullscreenElement||document.webkitFullscreenElement;if(fs){if(fs.tagName==='VIDEO')return fs;var fv=fs.querySelector&&fs.querySelector('video');if(fv)return fv;}" +
            "if(lastVideo&&document.contains(lastVideo))return lastVideo;var playing=Array.from(document.querySelectorAll('video')).find(function(v){return !v.paused&&!v.ended;});if(playing)return playing;return visibleVideo(currentPost())||Array.from(document.querySelectorAll('video')).find(function(v){var r=v.getBoundingClientRect();return r.width>0&&r.height>0;})||null;}" +
            "function unmute(v){if(!v)return;try{v.muted=false;v.removeAttribute('muted');if(v.volume===0)v.volume=1;}catch(e){}}" +
            "function playVideo(v){if(!v)return;lastVideo=v;unmute(v);try{var r=v.play();if(r&&r.catch)r.catch(function(){/* X may require another explicit OK press. */});}catch(e){}}" +
            "function seek(v,delta){if(!v)return;lastVideo=v;try{var end=Number.isFinite(v.duration)?v.duration:Infinity;v.currentTime=Math.max(0,Math.min(end,v.currentTime+delta));}catch(e){}}" +
            "function fullscreen(v){if(!v)return;lastVideo=v;try{if(v.webkitEnterFullscreen){v.webkitEnterFullscreen();return;}if(v.requestFullscreen){var r=v.requestFullscreen();if(r&&r.catch)r.catch(function(){});return;}var c=v.closest('[data-testid*=\"video\"]')||v.parentElement;if(c&&c.requestFullscreen){var q=c.requestFullscreen();if(q&&q.catch)q.catch(function(){});}}catch(e){}}" +
            "window.__xftvRemoteAction=function(action){var v=pickVideo();if(action==='unmute'){if(v&&v.muted)unmute(v);return true;}" +
            "if(action==='play'){if(!v){v=visibleVideo(currentPost());if(v)lastVideo=v;}if(v){playVideo(v);return true;}return false;}" +
            "if(action==='pause'){if(v){lastVideo=v;if(!v.paused)v.pause();return true;}return false;}" +
            "if(action==='toggle'){if(!v){v=visibleVideo(currentPost());if(v)lastVideo=v;}if(v){if(v.paused)playVideo(v);else v.pause();return true;}return false;}" +
            "if(action==='forward'){if(v){seek(v,10);return true;}return false;}if(action==='rewind'){if(v){seek(v,-10);return true;}return false;}if(action==='fullscreen'){if(v)fullscreen(v);return !!v;}return false;};" +
            "document.addEventListener('keydown',function(e){var k=e.key,code=e.keyCode||e.which;var target=e.target;var typing=target&&(target.tagName==='INPUT'||target.tagName==='TEXTAREA'||target.isContentEditable);if(typing)return;" +
            "var up=(k==='ArrowUp'||code===38),down=(k==='ArrowDown'||code===40),left=(k==='ArrowLeft'||code===37),right=(k==='ArrowRight'||code===39);" +
            "var isControl=target&&(target.closest('video')||target.closest('[role=\"slider\"]')||target.closest('[aria-label*=\"Seek\"]')||target.closest('[data-testid*=\"video\"]')||target.matches('input[type=\"range\"]'));" +
            "if(up||down){e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();move(down?1:-1);return;}" +
            "if((left||right)&&isControl){e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();window.__xftvRemoteAction(right?'forward':'rewind');return;}" +
            "if((k==='Enter'||k===' '||code===13||code===23||code===66)&&!isControl){var p=postOf(target)||currentPost();if(p){var v=visibleVideo(p);if(v){e.preventDefault();e.stopPropagation();if(e.stopImmediatePropagation)e.stopImmediatePropagation();lastVideo=v;window.__xftvRemoteAction(v.paused?'play':'toggle');return;}}}" +
            "},true);" +
            "document.addEventListener('focusin',function(e){var p=postOf(e.target);if(p&&!e.target.matches('input,textarea,[contenteditable=\"true\"]'))mark(p,false);},true);" +
            "var observer=new MutationObserver(function(){if(!document.getElementById('xftv-style')&&document.head)document.head.appendChild(style);if(!document.querySelector('article.xftv-selected-post')&&posts().length)mark(visiblePost(),false);});" +
            "observer.observe(document.documentElement,{childList:true,subtree:true});" +
            "document.addEventListener('play',function(e){if(e.target&&e.target.tagName==='VIDEO'){lastVideo=e.target;unmute(e.target);}},true);" +
            "document.addEventListener('volumechange',function(e){if(e.target&&e.target.tagName==='VIDEO')lastVideo=e.target;},true);" +
            "Array.from(document.querySelectorAll('video')).forEach(function(v){v.setAttribute('playsinline','true');v.addEventListener('playing',function(){lastVideo=v;});});" +
            "if(posts().length)mark(visiblePost(),false);" +
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
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        boolean firstDown = down && event.getRepeatCount() == 0;

        // Keep physical volume keys connected to Fire OS. If X has muted the
        // HTML video, Volume Up also clears the video's mute flag without
        // stealing the system-volume behaviour.
        if (code == KeyEvent.KEYCODE_VOLUME_UP) {
            if (firstDown && web != null) runVideoAction("unmute");
            return super.dispatchKeyEvent(event);
        }
        if (code == KeyEvent.KEYCODE_VOLUME_DOWN || code == KeyEvent.KEYCODE_VOLUME_MUTE) {
            return super.dispatchKeyEvent(event);
        }

        // Fire TV remotes vary: some send media keys, others send D-pad keys.
        // Route media transport keys to X's in-page HTML video in both inline
        // and WebChromeClient full-screen modes, never to an external player.
        if (web != null && (code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
                code == KeyEvent.KEYCODE_MEDIA_PLAY || code == KeyEvent.KEYCODE_MEDIA_PAUSE ||
                code == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || code == KeyEvent.KEYCODE_MEDIA_REWIND)) {
            if (firstDown) {
                if (code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) runVideoAction("toggle");
                else if (code == KeyEvent.KEYCODE_MEDIA_PLAY) runVideoAction("play");
                else if (code == KeyEvent.KEYCODE_MEDIA_PAUSE) runVideoAction("pause");
                else if (code == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) runVideoAction("forward");
                else if (code == KeyEvent.KEYCODE_MEDIA_REWIND) runVideoAction("rewind");
            }
            return true;
        }
        if (firstDown && customVideoView != null && web != null) {
            if (code == KeyEvent.KEYCODE_DPAD_RIGHT) { runVideoAction("forward"); return true; }
            if (code == KeyEvent.KEYCODE_DPAD_LEFT) { runVideoAction("rewind"); return true; }
        }

        if (down && code == KeyEvent.KEYCODE_BACK && customVideoView != null) {
            exitFullscreen();
            return true;
        }
        if (down && customVideoView != null &&
                (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN)) {
            exitFullscreen();
            return true;
        }
        if (down && code == KeyEvent.KEYCODE_MENU) {
            if (buttons != null && selected >= 0 && selected < buttons.length) buttons[selected].requestFocus();
            return true;
        }
        if (down && code == KeyEvent.KEYCODE_DPAD_LEFT && web != null && customVideoView == null) {
            web.evaluateJavascript("(function(){return window.scrollX===0 && (document.activeElement===document.body || document.activeElement===document.documentElement);})()", value -> {
                if ("true".equals(value)) buttons[selected].requestFocus();
            });
        }
        if (down && code == KeyEvent.KEYCODE_BACK) {
            if (web != null && web.canGoBack()) { web.goBack(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }

    private void runVideoAction(String action) {
        if (web == null) return;
        // JavaScript stays in the same WebView even while its custom video view
        // is displayed full screen. The function picks the active/selected X video.
        web.evaluateJavascript("(function(){if(window.__xftvRemoteAction){window.__xftvRemoteAction('" + action + "');}})()", null);
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
