package com.example.printingtowhatsapp;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
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
    int pageHeight = 1120;
    int pageWidth = 792;

    Bitmap bmp, scaledbmp;

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/index.html");
        webView.addJavascriptInterface(new JavaScriptInterface(this), "AndroidFunction");

        if (checkPermission()) {
            Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }

    public class JavaScriptInterface {
        Context mainContext;

        JavaScriptInterface(Context context){
            mainContext = context;
        }

        @JavascriptInterface
        public void shareData(String data){
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.marlogo);
            scaledbmp = Bitmap.createScaledBitmap(bmp, 700, 600, false);

            //generatePDF(data);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");

            File pdfFile = new File(Environment.getExternalStorageDirectory() + "/ticket.pdf");
            Uri uri = Uri.fromFile(pdfFile);
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            try {
                startActivity(Intent.createChooser(intent, "Compartiendo ticket a imprimir."));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error: No he podido abrir tu archivo de ticket.", Toast.LENGTH_SHORT).show();
            }


            /*File pdfFile = new File(Environment.getExternalStorageDirectory() + "/Dir/ticket.pdf");
            Uri path = Uri.fromFile(pdfFile);

            // Setting the intent for pdf reader
            Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
            pdfIntent.setDataAndType(path, "application/pdf");

            try {
                startActivity(pdfIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Can't read pdf file", Toast.LENGTH_SHORT).show();
            }*/


            /*Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, file.toString());
            sendIntent.setType("image/png");

            Intent shareIntent = Intent.createChooser(sendIntent, "Ticket");
            startActivity(shareIntent);

            Toast.makeText(getApplicationContext(),"Sharing...", Toast.LENGTH_SHORT).show();*/
        }
    }

    private void generatePDF(String text) {
        //Document and page configuration
        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page myPage = pdfDocument.startPage(pageInfo);

        //Image banner
        Canvas canvas = myPage.getCanvas();
        canvas.drawBitmap(scaledbmp, 56, 40, paint);

        //Title text settings
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        title.setTextSize(25);
        title.setColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        canvas.drawText("MAR PRINTING SYSTEM", 229, 100, title);

        //Subtitle text settings
        title.setTextSize(20);
        title.setColor(ContextCompat.getColor(this, android.R.color.black));
        canvas.drawText("Ticket de impresion", 229, 70, title);

        //Body text setting
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.black));
        title.setTextSize(15);
        title.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, 396, 560, title);

        //Writing finished
        pdfDocument.finishPage(myPage);

        //File creation
        File file = new File(Environment.getExternalStorageDirectory(), "ticket.pdf");
        try {
            //End processor
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(MainActivity.this, "PDF generado con exito.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Close pdf thread
        pdfDocument.close();
    }

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permisos denegados.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
