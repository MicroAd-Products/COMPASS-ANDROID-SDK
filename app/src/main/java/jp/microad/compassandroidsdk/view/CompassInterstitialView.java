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

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.microad.compassandroidsdk.model.KvSet;
import jp.microad.compassandroidsdk.util.HtmlMacroReplacer;
import jp.microad.compassandroidsdk.util.WebContentFetcher;

public class CompassInterstitialView extends WebView {

    private static final String HTML_URL = "https://cdn.microad.jp/compass-sdk/android/interstitial_ad.html";
    private static final String JAVASCRIPT_INTERFACE_NAME = "CompassAndroidSDKInterface";

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("SetJavaScriptEnabled")
    public CompassInterstitialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        setBackgroundColor(0x00000000);
        setVisibility(View.GONE);

        addJavascriptInterface(new WebAppInterface(this), JAVASCRIPT_INTERFACE_NAME);
    }

    public void load(String spot, KvSet kvSet) throws RuntimeException {
        executorService.execute(() -> {
            try {
                // Advertising IDを取得
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                String ifa = adInfo != null ? adInfo.getId() : "";

                // Bundle IDを取得する
                String appId = getContext().getPackageName();

                // HTMLを取得
                String htmlContent = new WebContentFetcher().fetchContent(HTML_URL);

                String replacedHtml = new HtmlMacroReplacer().replace(htmlContent, spot, ifa, appId, kvSet);

                post(() -> loadDataWithBaseURL(
                        HTML_URL,
                        replacedHtml,
                        "text/html",
                        "utf-8",
                        null
                ));
            } catch (IOException | GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}

class WebAppInterface {
    private final WebView webView;

    public WebAppInterface(WebView webView) {
        this.webView = webView;
    }

    @JavascriptInterface
    public void showWebView() {
        webView.setVisibility(View.VISIBLE);
    }

    @JavascriptInterface
    public void hideWebView() {
        webView.setVisibility(View.GONE);
    }

    @JavascriptInterface
    public void redirect(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        webView.getContext().startActivity(intent);
    }
}
