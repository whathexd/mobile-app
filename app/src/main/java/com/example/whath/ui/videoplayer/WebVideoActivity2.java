package com.example.whath.ui.videoplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.whath.ui.MainActivity;
import com.example.whath.ui.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

/**
 * Created by Star on 2017/11/5.
 */

public class WebVideoActivity2 extends Activity {


    private WebView webView;

    /** 视频全屏参数 */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.browser);
        webView = (WebView) findViewById(R.id.webView);
        initWebView();
//        isWifi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        webView.reload();
    }
//
//    /*  wifi???? */
//    public static boolean isWifi(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm == null){
//            openSetting(WebVideoActivity2);
//            return false;
//        }
//        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
//
//    }
//
//    /**
//     * 打开网络设置界面
//     */
//    public static void openSetting(Activity activity) {
//        Intent intent = null;
//        if (android.os.Build.VERSION.SDK_INT > 10) {
//            intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
//        } else {
//            intent = new Intent();
//            ComponentName component = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
//            intent.setComponent(component);
//            intent.setAction("android.intent.action.VIEW");
//        }
//        activity.startActivityForResult(intent, 0);
//    }


    /** 展示网页界面 **/
    public void initWebView() {
        WebChromeClient wvcc = new WebChromeClient();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true); // 关键点
        webSettings.setAllowFileAccess(true); // 允许访问文件
        webSettings.setSupportZoom(true); // 支持缩放
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不加载缓存内容

        webView.setWebChromeClient(wvcc);
        WebViewClient wvc = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            /**
             * 处理ssl请求
             */
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            /**
             * 页面载入完成回调
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:try{autoplay();}catch(e){}");
            }

        };


        webView.setWebViewClient(wvc);

        webView.setWebChromeClient(new WebChromeClient() {

            /*** 视频播放相关的方法 **/

            @Override
            public View getVideoLoadingProgressView() {
                FrameLayout frameLayout = new FrameLayout(WebVideoActivity2.this);
                frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return frameLayout;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                showCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }




        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Url");

        myRef.setValue("https://youtu.be/W48v6hF6qZg");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = (String) dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
                // 加载Web地址
                webView.loadUrl(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        Button mButtonSync2;
        mButtonSync2 = (Button) findViewById(R.id.button_sync);
        mButtonSync2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String urlstr = webView.getUrl();
                Log.d(TAG, "urlstr = "+urlstr);
                myRef.setValue(urlstr);
            }
        });
    }

    /** 视频播放全屏 **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        WebVideoActivity2.this.getWindow().getDecorView();

        FrameLayout decor = (FrameLayout)getWindow().getDecorView();

        fullscreenContainer = new FullscreenHolder(WebVideoActivity2.this);

        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);

        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);

        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
    }

    /** 隐藏视频全屏 */
    private void hideCustomView() {
        if (customView == null) {
            return;
        }

        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webView.setVisibility(View.VISIBLE);
    }

    /** 全屏容器界面 */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
                if (customView != null) {
                    hideCustomView();
                } else if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}