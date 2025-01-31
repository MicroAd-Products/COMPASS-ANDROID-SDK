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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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

    public void load(String spot, KvSet kvSet, Function<String, Void> errorHandler) {
        executorService.execute(() -> {
            try {
                // Advertising IDを取得
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                String ifa = adInfo != null ? adInfo.getId() : "";

                // Bundle IDを取得する（現在は未実装）
                String appId = getContext().getPackageName();

                // HTMLを取得
                String htmlContent = new WebContentFetcher().fetchContent(HTML_URL);

                if (htmlContent == null) {
                    if (errorHandler != null) {
                        errorHandler.apply("Error: Failed to fetch HTML from server.");
                    }
                    return;
                }

                // プレースホルダーを置換
                String replacedHtml = new HtmlMacroReplacer().replace(htmlContent, spot, ifa, appId, kvSet);

                post(() -> loadDataWithBaseURL(
                        HTML_URL,
                        replacedHtml,
                        "text/html",
                        "utf-8",
                        null
                ));

            } catch (Exception e) {
                e.printStackTrace();
                if (errorHandler != null) {
                    errorHandler.apply("Error: " + e.getMessage());
                }
            }
        });
    }

    private String fetchHtmlFromServer() {
        try {
            URL url = new URL(HTML_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();
            connection.disconnect();

            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
