package jp.microad.compassandroidsdk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import jp.microad.compassandroidsdk.model.KvSet;
import jp.microad.compassandroidsdk.util.HtmlMacroReplacer;

public class CompassInterstitialView extends WebView {

    // TODO: HTMLはCDNに配置してhttpsで取得するようにする
    private static final String htmlContent =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"ja\">\n" +
                    "<head>\n" +
                    "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=0\">\n" +
                    "    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\">\n" +
                    "    <meta http-equiv=\"Content-Script-Type\" content=\"text/javascript\">\n" +
                    "    <title></title>\n" +
                    "    <script type=\"text/javascript\">\n" +
                    "        var microadCompass = microadCompass || {};\n" +
                    "        microadCompass.queue = microadCompass.queue || [];\n" +
                    "        microadCompass.isSkipTrackers = true;\n" +
                    "    </script>\n" +
                    "    <script type=\"text/javascript\" charset=\"UTF-8\" src=\"https://j.microad.net/js/compass.js\" " +
                    "onload=\"new microadCompass.AdInitializer().initialize();\" async></script>\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "    <div id=\"${COMPASS_SPOT}\">\n" +
                    "        <script type=\"text/javascript\">\n" +
                    "            microadCompass.overridingHandler = {\n" +
                    "                onAdLoaded: () => { CompassAndroidSDKInterface.showWebView(); },\n" +
                    "                onClose: () => { CompassAndroidSDKInterface.hideWebView(); },\n" +
                    "                onRedirect: (url) => { CompassAndroidSDKInterface.redirect(url); }\n" +
                    "            };\n" +
                    "            microadCompass.queue.push({\n" +
                    "                \"spot\": \"${COMPASS_SPOT}\",\n" +
                    "                \"ifa\": \"${COMPASS_EXT_IFA}\",\n" +
                    "                \"appid\": \"${COMPASS_EXT_APPID}\",\n" +
                    "                \"kv_set\": {\n" +
                    "                    \"gender\": \"${COMPASS_EXT_GENDER}\",\n" +
                    "                    \"birthday\": \"${COMPASS_EXT_BIRTHDAY}\",\n" +
                    "                    \"age\": \"${COMPASS_EXT_AGE}\",\n" +
                    "                    \"postal_code\": \"${COMPASS_EXT_POSTALCODE}\",\n" +
                    "                    \"email\": \"${COMPASS_EXT_EMAIL}\",\n" +
                    "                    \"hashed_email\": \"${COMPASS_EXT_HASHED_EMAIL}\"\n" +
                    "                }\n" +
                    "            });\n" +
                    "        </script>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";


    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("SetJavaScriptEnabled")
    public CompassInterstitialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true); // JavaScriptを有効化
        settings.setDomStorageEnabled(true); // DOM Storage有効化
        settings.setAllowFileAccess(true); // ファイルアクセスを許可
        settings.setAllowContentAccess(true); // コンテンツアクセスを許可
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // JavaScriptで新しいウィンドウを開く許可

        setBackgroundColor(0x00000000);
        setVisibility(View.GONE);

        addJavascriptInterface(new WebAppInterface(this), "CompassAndroidSDKInterface");
    }

    public void load(String spot, KvSet kvSet, Function<String, Void> errorHandler) {
        executorService.execute(() -> {
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
                String ifa = adInfo != null ? adInfo.getId() : "";

                // TODO: Bunble IDを取得する処理を記述する
                String appId = "";

                String replacedHtml = new HtmlMacroReplacer().replace(htmlContent, spot, ifa, appId, kvSet);

                post(() -> loadDataWithBaseURL(
                        "https://cdn.microad.jp/compass-sdk/android/interstitial_ad.html",
                        replacedHtml,
                        "text/html",
                        "utf-8",
                        null
                ));

            } catch (Exception e) {
                e.printStackTrace();

                if (errorHandler != null) {
                    errorHandler.apply("Error occurred: " + e.getMessage());
                }
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