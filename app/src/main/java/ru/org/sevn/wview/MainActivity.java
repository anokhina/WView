package ru.org.sevn.wview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ZZZ";

    private WebView wview;
    private Button buttonGo;
    private EditText etextUrl;

    private ValueCallback<Uri[]> mUploadMessage;
    private ValueCallback<Uri> mUploadMessageUno;
    private final static int FILECHOOSER_RESULTCODE=1;

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,
//                                    Intent intent) {
//        if(requestCode==FILECHOOSER_RESULTCODE) {
//            if (null == mUploadMessage) return;
//            Uri result = intent == null || resultCode != RESULT_OK ? null
//                    : intent.getData();
//            mUploadMessage.onReceiveValue(result);
//            mUploadMessage = null;
//        }
//    }
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "_______00_______"+requestCode);
        if (requestCode == FILECHOOSER_RESULTCODE && (mUploadMessage!= null || mUploadMessageUno != null) ) {}
        else {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri result = null;

        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    result = Uri.parse(dataString);
                }
            }
        }
        Log.d(TAG, "_______0________"+result);

        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(new Uri[]{result});
            mUploadMessage = null;
        } else {
            mUploadMessageUno.onReceiveValue(result);
            mUploadMessageUno = null;
        }
        return;
    }

    public class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            Log.d("zzzzzzzzz", "onUrlChange" + url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        wview = (WebView) findViewById(R.id.webview);
        buttonGo = (Button) findViewById(R.id.button);
        etextUrl = (EditText) findViewById(R.id.editText);

        //webSettings
        WebSettings ws = wview.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setDatabaseEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setDomStorageEnabled(true);
        String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        ws.setDatabasePath(databasePath);
        Log.d(TAG, "setDatabasePath>>>>>>>" + databasePath);
        /*
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.ECLAIR) {
            try {
                Log.d(TAG, "Enabling HTML5-Features");
                Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
                m1.invoke(ws, Boolean.TRUE);

                Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
                m2.invoke(ws, Boolean.TRUE);

                Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
                m3.invoke(ws, "/data/data/" + getPackageName() + "/databases/");

                Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
                m4.invoke(ws, 1024*1024*8);

                Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
                m5.invoke(ws, "/data/data/" + getPackageName() + "/cache/");

                Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
                m6.invoke(ws, Boolean.TRUE);

                Log.d(TAG, "Enabled HTML5-Features");
            }
            catch (NoSuchMethodException e) {
                Log.e(TAG, "Reflection fail", e);
            }
            catch (InvocationTargetException e) {
                Log.e(TAG, "Reflection fail", e);
            }
            catch (IllegalAccessException e) {
                Log.e(TAG, "Reflection fail", e);
            }
        }
        */

        wview.addJavascriptInterface(new MyJavaScriptInterface(), "android");

        final WebViewClient wviewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                etextUrl.setText(url);
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                etextUrl.setText(url);
                //view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
                super.onPageFinished(view, url);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.e(TAG, ">>>>>>>" + url);
                if (zzz(url)) {
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }

//                if( URLUtil.isNetworkUrl(url) ) {
//                    return false;
//                }
//
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity( intent );
//                return true;
            }
        };
        wview.setWebViewClient(wviewClient);

        //wview.setWebChromeClient(new WebChromeClient());
        WebChromeClient chrome = new WebChromeClient() {
            @Override
            public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize,
                                                long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(estimatedSize * 2);
            }

//            // For Android 3.0+
//            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
//
//                mUploadMessage = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("image/*");
//                getActivity().startActivityForResult(Intent.createChooser(i,"File Chooser"), FILECHOOSER_RESULTCODE);
//
//            }
//
            // For Android 3.0+
            public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
                Log.d(TAG, "_______1________"+uploadMsg.getClass().getName());
                mUploadMessageUno = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                getActivity().startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                Log.d(TAG, "________2_______"+uploadMsg.getClass().getName());
                mUploadMessageUno = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("text/*");
                getActivity().startActivityForResult( Intent.createChooser( i, "File Chooser" ), FILECHOOSER_RESULTCODE );

            }
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    WebChromeClient.FileChooserParams fileChooserParams) {

                // Double check that we don't have any existing callbacks
                if(mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = filePathCallback;

                // Set up the intent to get an existing image
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                //contentSelectionIntent.setType("image/*");
                contentSelectionIntent.setType("text/*");

                // Set up the intents for the Intent chooser
                Intent[] intentArray;
                intentArray = new Intent[0];

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

                return true;
            }
        };
        wview.setWebChromeClient(chrome);

        wview.requestFocus(View.FOCUS_DOWN);

        //http://html5test.com
        //file:///sdcard/nika/test.html
        //wview.loadUrl("http://www.ya.ru");
        wview.loadUrl("file:///mnt/sdcard/htmlprog/events.html");

        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etextUrl.getText().toString();
                if (!zzz(url)) {
                    wview.loadUrl(url);
                }
            }
        });
    }

    private Activity getActivity() {
        return this;
    }
    private boolean zzz(String url) {
        if (url.startsWith("zzz:")) {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                Log.d(TAG, "No SDCARD");
            } else {
                wview.loadUrl("file://"+Environment.getExternalStorageDirectory()+url.substring("zzz:".length()));
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wview.canGoBack()) {
            wview.goBack();
            return true;
        }
//        case KeyEvent.KEYCODE_MENU:
//        webView.loadUrl("javascript:open_menu()");
        return super.onKeyDown(keyCode, event);
    }
}
