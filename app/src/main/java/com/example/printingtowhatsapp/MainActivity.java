package com.example.printingtowhatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl("file:///android_asset/index.html");

        webView.addJavascriptInterface(new JavaScriptInterface(this), "AndroidFunction");
    }

    public class JavaScriptInterface {
        Context mainContext;

        JavaScriptInterface(Context context){
            mainContext = context;
        }

        @JavascriptInterface
        public void shareData(String data){
            Bitmap image = textToBitmap(data);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "tmp.jpg");
            try{
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(bytes.toByteArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///tmp.jpg"));
            startActivity(Intent.createChooser(share, "Share image"));

            Toast.makeText(getApplicationContext(),"Sharing...", Toast.LENGTH_SHORT).show();

            /*Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, data);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);

            Toast.makeText(getApplicationContext(),"Sharing...", Toast.LENGTH_SHORT).show();*/
        }
    }

    private Bitmap textToBitmap(String text){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(35);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);

        float baseline = -paint.ascent();
        int width = (int) (paint.measureText(text) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }
}
