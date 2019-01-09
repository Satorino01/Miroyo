package com.example.kobayashi_satoru.miroyo;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public final class HttpRequestTask extends AsyncTask<HashMap, Void, Void> {

    @Override
    protected Void doInBackground(HashMap... params) {
        String ret = "";
        HttpURLConnection urlConnection = null;
        String argStrApiUrl = params[0].get("baseUrl").toString();
        HashMap mapData = (HashMap) params[0].get("data");
        try {
            //ステップ1.接続URLを決める。
            URL url = new URL(argStrApiUrl);

            //ステップ2.URLへのコネクションを取得する。
            urlConnection = (HttpURLConnection) url.openConnection();

            //ステップ3.接続設定(メソッドの決定,タイムアウト値,ヘッダー値等)を行う。
            //接続タイムアウトを設定する。
            urlConnection.setConnectTimeout(100000);
            //レスポンスデータ読み取りタイムアウトを設定する。
            urlConnection.setReadTimeout(100000);
            //ヘッダーにUser-Agentを設定する。
            urlConnection.setRequestProperty("User-Agent", "Android");
            //ヘッダーにAccept-Languageを設定する。
            urlConnection.setRequestProperty("Accept-Language", Locale.getDefault().toString());
            //ヘッダーにContent-Typeを設定する
            urlConnection.addRequestProperty("Content-Type", "application/json");
            //ヘッダーにContent-Typeを設定する
            urlConnection.addRequestProperty("Authorization","key=");
            //HTTPのメソッドをPOSTに設定する。
            urlConnection.setRequestMethod("POST");
            //リクエストのボディ送信を許可する
            urlConnection.setDoOutput(true);
            //レスポンスのボディ受信を許可する
            urlConnection.setDoInput(true);
            //String headerFieldKey = urlConnection.getHeaderFieldKey();

            //ステップ4.コネクションを開く
            urlConnection.connect();

            //ステップ5:リクエストボディの書き出しを行う。
            OutputStream outputStream = urlConnection.getOutputStream();
            if (mapData.size() > 0) {
                //JSON形式の文字列に変換する。
                JSONObject responseJsonObject = new JSONObject(mapData);
                String jsonText = responseJsonObject.toString();
                PrintStream ps = new PrintStream(urlConnection.getOutputStream());
                ps.print(jsonText);
                ps.close();
            }
            outputStream.close();

            //ステップ6.レスポンスボディの読み出しを行う。
            int responseCode = urlConnection.getResponseCode();
            ret = convertToString(urlConnection.getInputStream());
            Log.d("execute", "URL:" + argStrApiUrl);
            Log.d("execute", "HttpStatusCode:" + responseCode);
            Log.d("execute", "ResponseData:" + ret);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                //7.コネクションを閉じる。
                urlConnection.disconnect();
            }
        }
        return null;
    }

    public String convertToString(InputStream stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}