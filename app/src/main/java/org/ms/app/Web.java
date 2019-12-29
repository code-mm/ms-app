package org.ms.app;

import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alipay.mobile.framework.app.ui.BaseActivity;

public class Web extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        WebView webView = (WebView) findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {


                System.out.println(request.getUrl());

                return super.shouldOverrideUrlLoading(view, request);
            }


        });
        webView.setWebChromeClient(new WebChromeClient() {


            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {


                System.out.println(" =============   " + consoleMessage.message());


                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");


    }
}
