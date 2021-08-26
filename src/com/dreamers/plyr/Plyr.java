package com.dreamers.plyr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.json.JSONException;

import java.util.Locale;

public class Plyr extends AndroidNonvisibleComponent {
    private final Context context;
    private WebView webView;
    private final Plyr.JsRunner jsRunner;
    private String poster;
    private String source;
    private String subtitles = "";
    private String subtitlesLanguage = "";
    private String subtitleLabel = "";
    private static final String READY = "ready";
    private static final String PLAYED = "played";
    private static final String PAUSED = "paused";
    private static final String COMPLETED = "completed";
    private static final String FULL_SCREEN_ENABLED = "fullscreenenabled";
    private static final String FULL_SCREEN_DISABLED = "fullscreendisabled";
    private final Activity activity;
    private String mediaType;
    private String controls;

    public Plyr(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        this.jsRunner = new Plyr.JsRunner();
        this.activity = container.$context();
    }

    @SimpleFunction(
            description = "Initialize Player"
    )
    public void Initialize(HVArrangement layout, String path, String thumbnail, String subtitle, String subtitlesLang, String mediaType, Object controls) {
        if (!mediaType.isEmpty()) {
            if (!path.isEmpty()) {

                this.mediaType = mediaType;
                this.poster = thumbnail;
                this.source = path;

                if (!subtitle.isEmpty()) {
                    this.subtitles = subtitle;
                    this.subtitlesLanguage = subtitlesLang;
                    if (subtitlesLang.length() > 2) {
                        this.subtitleLabel = subtitlesLang.substring(0, 2);
                    }
                }

                if (controls instanceof YailList) {
                    String[] controlsArray = ((YailList) controls).toStringArray();
                    if (controlsArray.length <= 0) {
                        this.controls = "[]";
                    } else {
                        StringBuilder controlsBuilder = new StringBuilder();
                        controlsBuilder.append("[");

                        for (String control : controlsArray) {
                            controlsBuilder.append("'").append(control).append("',");
                        }

                        controlsBuilder.append("]");
                        this.controls = controlsBuilder.toString();
                    }
                }

                this.webView = new WebView(this.context);
                WebSettings settings = this.webView.getSettings();
                this.webView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                settings.setAllowFileAccessFromFileURLs(true);
                settings.setAllowUniversalAccessFromFileURLs(true);
                settings.setLoadsImagesAutomatically(true);
                settings.setAllowUniversalAccessFromFileURLs(true);
                settings.setAppCacheEnabled(true);
                settings.setMediaPlaybackRequiresUserGesture(false);
                settings.setLoadsImagesAutomatically(true);

                this.webView.setVerticalScrollBarEnabled(false);
                this.webView.setHorizontalScrollBarEnabled(false);
                this.webView.setScrollbarFadingEnabled(false);
                this.webView.setVerticalFadingEdgeEnabled(false);
                this.webView.setHorizontalScrollBarEnabled(false);
                this.webView.setWebViewClient(new Plyr.MyClient());
                this.webView.setWebChromeClient(new Plyr.MyChrome());
                this.webView.addJavascriptInterface(new Plyr.JSInterface(), "Interface");

                String activityName = this.activity.getClass().getName();
                boolean isCompanion = activityName.contains("io.makeroid.companion");

                try {
                    String indexFile;
                    if (isCompanion) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            indexFile = context.getExternalFilesDir(null).toString() + "/assets/external_comps/com.dreamers.plyr/assets/index.html";
                        } else {
                            indexFile = "file:///storage/emulated/0/Kodular/assets/external_comps/com.dreamers.plyr/assets/index.html";
                        }
                    } else {
                        indexFile = this.form.getAssetPathForExtension(this, "index.html");
                    }
                    webView.loadUrl(indexFile);
                } catch (Exception e) {
                    this.OnError(e.getMessage());
                }

                ((ViewGroup) layout.getView()).addView(this.webView);

            } else {
                this.OnError("Path can't be empty");
            }
        } else {
            this.OnError("Media Type can't be empty");
        }

    }

    // Player UI Controls
    // --------------------------------------------
    @SimpleProperty
    public String ControlsPlay() {
        return "play";
    }

    @SimpleProperty
    public String ControlsPlayLarge() {
        return "play-large";
    }

    @SimpleProperty
    public String ControlsRewind() {
        return "rewind";
    }

    @SimpleProperty
    public String ControlsForward() {
        return "fast-forward";
    }

    @SimpleProperty
    public String ControlsProgress() {
        return "progress";
    }

    @SimpleProperty
    public String ControlsCurrentTime() {
        return "current-time";
    }

    @SimpleProperty
    public String ControlsDuration() {
        return "duration";
    }

    @SimpleProperty
    public String ControlsMute() {
        return "mute";
    }

    @SimpleProperty
    public String ControlsVolume() {
        return "volume";
    }

    @SimpleProperty
    public String ControlsCaptions() {
        return "captions";
    }

    @SimpleProperty
    public String ControlsSettings() {
        return "settings";
    }

    @SimpleProperty
    public String ControlsFullScreen() {
        return "fullscreen";
    }

    // Media Types
    // --------------------------------------------
    @SimpleProperty
    public String MediaTypeVideo() {
        return "video";
    }

    @SimpleProperty
    public String MediaTypeAudio() {
        return "audio";
    }

    @SimpleProperty
    public String MediaTypeHls() {
        return "hls";
    }

    @SimpleProperty
    public String MediaTypeVimeo() {
        return "vimeo";
    }

    @SimpleProperty
    public String MediaTypeYouTube() {
        return "youtube";
    }

    // Media Functions
    // --------------------------------------------

    // Play
    @SimpleFunction(description = "Play media")
    public void Play() {
        this.jsRunner.EvaluateJS("window.player.play();");
    }

    // Pause
    @SimpleFunction(description = "Pause media")
    public void Pause() {
        this.jsRunner.EvaluateJS("window.player.pause();");
    }

    // Stop / Reset
    @SimpleFunction(description = "Stop media playback and reset to initial position")
    public void Stop() {
        this.jsRunner.EvaluateJS("window.player.stop();");
    }

    // Loop
    @SimpleProperty(description = "Toggle loop")
    public void Loop(boolean loop) {
        this.jsRunner.EvaluateJS("player.loop = " + loop + ";");
    }

    // Set Source
    @SimpleProperty(description = "Set media source.")
    public void SetSource(String source) {
        if (source != null && source.length() > 0) {
            this.source = source;
            this.jsRunner.EvaluateJS("video.src = \"" + this.source + "\";");
        } else {
            this.OnError("Path can't be empty");
        }

    }

    // Add Subtitles
    @SimpleFunction(description = "Add subtitles")
    public void AddSubtitle(String path, String language) {
        if (path != null && path.length() > 0) {
            String var3 = "";
            if (language.length() > 2) {
                var3 = language.substring(0, 2);
            }

            this.jsRunner.EvaluateJS("addTrack(\"" + path + "\", \"" + language + "\", \"" + var3 + "\");");
        } else {
            this.OnError("Path can't be empty");
        }

    }

    // Set Thumbnail
    @SimpleProperty(description = "Set thumbnail image")
    public void Thumbnail(String poster) {
        this.jsRunner.EvaluateJS("updatePoster(\"" + poster + "\");");
    }

    // Seek to specific point
    @SimpleFunction(description = "Seek to specific milliseconds")
    public void SeekTo(int milliseconds) {
        milliseconds /= 1000;
        this.jsRunner.EvaluateJS("player.currentTime =" + milliseconds + ";");
    }

    // Set quality
    @SimpleProperty(description = "Change quality of media")
    public void CurrentQuality(int index) {
        this.jsRunner.EvaluateJS("changeQuality(" + --index + ");");
    }

    // Get metadata ( Properties associated with video like title,type,quality etc )
    @SimpleFunction(description = "Get metadata ( Properties associated with video like title,type,quality etc )")
    public void GetMetadata() {
        this.jsRunner.EvaluateJS("createMeta();");
    }

    // Toggle Controls visibility
    @SimpleProperty(description = "Toggle Controls visibility")
    public void ControlsVisible(boolean visible) {
        this.jsRunner.EvaluateJS("toggleControls(" + visible + ");");
    }

    // Toggle Captions visibility
    @SimpleProperty(description = "Toggle Captions visibility")
    public void CaptionsVisible(boolean visible) {
        this.jsRunner.EvaluateJS("toggleCaptions(" + visible + ")");
    }

    // Remove All Subtitles
    @SimpleFunction(description = "Remove All Subtitles")
    public void RemoveSubtitles() {
        this.jsRunner.EvaluateJS("removeTracks();");
    }

    // Set current track
    @SimpleProperty(description = "Set current track")
    public void CurrentTrack(int index) {
        this.jsRunner.EvaluateJS("selectTrack(" + --index + ");");
    }

    // Set speed
    @SimpleProperty(description = "Change playback speed")
    public void Speed(double speed) {
        this.jsRunner.EvaluateJS("player.speed =" + speed + ";");
    }

    // Seek forward
    @SimpleFunction(description = "Seek forward")
    public void Forward(int milliseconds) {
        this.jsRunner.EvaluateJS("player.forward(" + milliseconds / 1000 + ");");
    }

    //Seek backwards
    @SimpleFunction(description = "Seek backward")
    public void Rewind(int milliseconds) {
        this.jsRunner.EvaluateJS("player.rewind(" + milliseconds / 1000 + ");");
    }

    // Convert milliseconds to hh:mm:ss time format
    @SimpleFunction(description = "Convert milliseconds to hh:mm:ss time format")
    public String Format(long milliseconds) {
        long seconds = milliseconds / 1000L;
        return String.format(Locale.US, "%02d:%02d:%02d", seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
    }

    // Events
    // --------------------------------------------

    // Event raised when an error occurs
    @SimpleEvent(description = "Event raised when an error occurs")
    public void OnError(String error) {
        EventDispatcher.dispatchEvent(this, "OnError", error);
    }

    // Event raised when video is paused
    @SimpleEvent(description = "Event raised when video is paused")
    public void OnPause() {
        EventDispatcher.dispatchEvent(this, "OnPause");
    }

    // Event raised when video is played
    @SimpleEvent(description = "Event raised when video is played")
    public void OnPlay() {
        EventDispatcher.dispatchEvent(this, "OnPlay");
    }

    // Event raised when player is ready
    @SimpleEvent(description = "Event raised when player is ready")
    public void OnReady() {
        EventDispatcher.dispatchEvent(this, "OnReady");
    }

    // Event raised when video is completed
    @SimpleEvent(description = "Event raised when video is completed")
    public void OnComplete() {
        EventDispatcher.dispatchEvent(this, "OnComplete");
    }

    // Event raised when fullscreen changes
    @SimpleEvent(description = "Event raised when fullscreen changes")
    public void OnFullScreenChanged(boolean isFullscreen) {
        EventDispatcher.dispatchEvent(this, "OnFullScreenChanged", isFullscreen);
    }

    // Event raised when video metadata is loaded
    @SimpleEvent(description = "Event raised when video metadata is loaded")
    public void GotMetadata(Object data) {
        EventDispatcher.dispatchEvent(this, "GotMetadata", data);
    }

    // Event raised when time changes
    @SimpleEvent(description = "Event raised when time changes")
    public void TimeUpdated(String time) {
        EventDispatcher.dispatchEvent(this, "TimeUpdated", time);
    }

    // Event raised when player returns caption text for current time
    @SimpleEvent(description = "Event raised when player returns caption text for current time")
    public void GotText(String text) {
        EventDispatcher.dispatchEvent(this, "GotText", text);
    }

    // Event raised when new track is added
    @SimpleEvent(description = "Event raised when new track is added")
    public void TrackAdded(String title, String language) {
        EventDispatcher.dispatchEvent(this, "TrackAdded", title, language);
    }

    // Event raised when loading state changes
    @SimpleEvent(description = "Event raised when loading state changes")
    public void LoadingState(boolean isLoading) {
        EventDispatcher.dispatchEvent(this, "LoadingState", isLoading);
    }

    // Custom Chrome Client for adding support for fullscreen
    public class MyChrome extends WebChromeClient {
        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;
        private final View decorView;

        MyChrome() {
            this.decorView = Plyr.this.activity.getWindow().getDecorView();
        }

        public Bitmap getDefaultVideoPoster() {
            return this.mCustomView == null ? null : BitmapFactory.decodeResource(Plyr.this.context.getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) this.decorView).removeView(this.mCustomView);
            this.mCustomView = null;
            this.decorView.setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            Plyr.this.activity.setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (this.mCustomView != null) {
                this.onHideCustomView();
            } else {
                this.mCustomView = view;
                this.mOriginalSystemUiVisibility = this.decorView.getSystemUiVisibility();
                this.mOriginalOrientation = Plyr.this.activity.getRequestedOrientation();
                this.mCustomViewCallback = callback;
                ((FrameLayout) this.decorView).addView(this.mCustomView, new android.widget.FrameLayout.LayoutParams(-1, -1));
                this.decorView.setSystemUiVisibility(3846);
                this.decorView.setFitsSystemWindows(true);
                this.mCustomView.setFitsSystemWindows(true);
            }
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            String message = consoleMessage.message();

            switch (message) {
                case READY:
                    Plyr.this.OnReady();
                    break;
                case PLAYED:
                    Plyr.this.OnPlay();
                    break;
                case PAUSED:
                    Plyr.this.OnPause();
                    break;
                case COMPLETED:
                    Plyr.this.OnComplete();
                    break;
                case FULL_SCREEN_ENABLED:
                    Plyr.this.OnFullScreenChanged(true);
                    break;
                case FULL_SCREEN_DISABLED:
                    Plyr.this.OnFullScreenChanged(false);
                    break;
            }
            if (message.startsWith("error")) {
                Plyr.this.OnError(consoleMessage.message());
            }
            return true;
        }
    }

    // JS Interface to receive data from webview
    public class JSInterface {

        @JavascriptInterface
        public void gotMeta(final String meta) {
            Plyr.this.activity.runOnUiThread(() -> {
                Object data = meta;
                try {
                    data = JsonUtil.getObjectFromJson(meta, true);
                } catch (JSONException var3) {
                    var3.printStackTrace();
                }

                Plyr.this.GotMetadata(data);
            });
        }

        @JavascriptInterface
        public void timeUpdated(final String time) {
            Plyr.this.activity.runOnUiThread(() -> Plyr.this.TimeUpdated(time));
        }

        @JavascriptInterface
        public void gotText(final String text) {
            Plyr.this.activity.runOnUiThread(() -> Plyr.this.GotText(text));
        }

        @JavascriptInterface
        public void trackAdded(final String title, final String lang) {
            Plyr.this.activity.runOnUiThread(() -> Plyr.this.TrackAdded(title, lang));
        }

        @JavascriptInterface
        public void loadingChanged(final boolean isLoading) {
            Plyr.this.activity.runOnUiThread(() -> Plyr.this.LoadingState(isLoading));
        }
    }

    // Web view client
    public class MyClient extends WebViewClient {
        private MyClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            return true;
        }

        public void onReceivedError(WebView webView, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(webView, request, error);
            Plyr.this.OnError("" + error.getDescription());
        }

        public void onPageFinished(WebView webView, String url) {
            Plyr.this.jsRunner.EvaluateJS("init(\"" + Plyr.this.source + "\",\"" + Plyr.this.poster + "\",\"" + Plyr.this.subtitles + "\", \"" + Plyr.this.subtitlesLanguage + "\", \"" + Plyr.this.subtitleLabel + "\", \"" + Plyr.this.mediaType + "\" ," + Plyr.this.controls + ");");
        }
    }

    // Class to help execute javascript code
    public class JsRunner {
        public void EvaluateJS(final String js) {
            Plyr.this.webView.loadUrl("javascript:" + js);
        }
    }
}

