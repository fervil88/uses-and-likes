package com.harriague.curso.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * Created by Fernando on 9/29/2016.
 */
public class CustomWebViewClient extends WebViewClient {
    private AlertDialog.Builder alertDialog;
    private ProgressDialog progressBar;

    public CustomWebViewClient(ProgressDialog progressBar, AlertDialog.Builder alertDialog) {
        this.progressBar = progressBar;
        this.alertDialog = alertDialog;
    }
    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if(progressBar.isShowing()){
            progressBar.dismiss();
            alertDialog.show();
        }
    }
}
