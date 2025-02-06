package jp.microad.compassandroidsdk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.microad.compassandroidsdk.R;
import jp.microad.compassandroidsdk.model.KvSet;
import jp.microad.compassandroidsdk.util.HtmlMacroReplacer;
import jp.microad.compassandroidsdk.util.WebContentFetcher;

public class CompassInterstitialView extends FrameLayout {

    private static final String HTML_URL = "https://cdn.microad.jp/compass-sdk/android/interstitial_ad.html";
    private static final String JAVASCRIPT_INTERFACE_NAME = "CompassAndroidSDKInterface";

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final WebView adWebView;

    @SuppressLint("SetJavaScriptEnabled")
    public CompassInterstitialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_compass_interstitial, this);

        adWebView = findViewById(R.id.ad_webview);
        setVisibility(View.GONE);
        setBackgroundColor(0x00000000);

        final WebSettings settings = adWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        adWebView.addJavascriptInterface(new WebAppInterface(this), JAVASCRIPT_INTERFACE_NAME);
    }

    public CompassInterstitialView(Context context) {
        this(context, null, 0);
    }

    public CompassInterstitialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * CompassInterstitialView内にインタースティシャル広告を表示する
     *
     * @param spot     事前にお渡しした広告枠ID (spot id)
     * @param kvSet    ターゲットユーザのKV情報
     * @param callback 広告のロード成功・失敗を通知するコールバック
     */
    public void load(String spot, KvSet kvSet, LoadCallback callback) {
        executorService.execute(() -> {
            try {
                // Advertising IDを取得
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                String ifa = adInfo.getId();

                // Bundle IDを取得する
                String appId = getContext().getPackageName();

                // HTMLを取得
                String htmlContent = new WebContentFetcher().fetchContent(HTML_URL);

                String replacedHtml = new HtmlMacroReplacer().replace(htmlContent, spot, ifa, appId, kvSet);

                post(() -> {
                    adWebView.loadDataWithBaseURL(
                            HTML_URL,
                            replacedHtml,
                            "text/html; charset=utf-8",
                            "utf-8",
                            null
                    );
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (IOException | GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException ex) {
                if (callback != null) {
                    post(() -> callback.onError(ex));
                }
            }
        });
    }
}

interface LoadCallback {
    void onSuccess();

    void onError(Exception e);
}

class WebAppInterface {
    private final FrameLayout rootView;

    public WebAppInterface(FrameLayout rootView) {
        this.rootView = rootView;
    }

    @JavascriptInterface
    public void showWebView() {
        rootView.post(() -> {
            rootView.setVisibility(View.VISIBLE);
        });
    }

    @JavascriptInterface
    public void hideWebView() {
        rootView.post(() -> rootView.setVisibility(View.GONE));
    }

    @JavascriptInterface
    public void redirect(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        rootView.getContext().startActivity(intent);
    }
}
