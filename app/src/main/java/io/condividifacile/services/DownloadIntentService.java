package io.condividifacile.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by gianma on 30/08/17.
 */

public class DownloadIntentService extends IntentService {

    private static final String TAG = DownloadIntentService.class.getSimpleName();

    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String URL_EXTRA = "url";
    public static final String RSS_RESULT_EXTRA = "url";

    public static final int RESULT_CODE = 0;
    public static final int INVALID_URL_CODE = 1;
    public static final int ERROR_CODE = 2;

    public DownloadIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        if(reply != null) {
            try {
                try {
                    URL url = new URL(intent.getStringExtra(URL_EXTRA));
                    Bitmap bm = null;
                    try {
                        URLConnection conn = url.openConnection();
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        is.close();
                    } catch (IOException e) {
                        Log.e("swag", "Error getting bitmap", e);
                    }

                    Intent result = new Intent();
                    result.putExtra(RSS_RESULT_EXTRA, bm);

                    reply.send(this, RESULT_CODE, result);
                } catch (MalformedURLException exc) {
                    reply.send(INVALID_URL_CODE);
                }
            } catch (PendingIntent.CanceledException exc) {
                Log.d(TAG, "reply cancelled", exc);
            }
        }
    }

}
