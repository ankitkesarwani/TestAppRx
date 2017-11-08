package com.example.kesar.testapprx;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RxAndroidSamples";

    private Toolbar mToolbar;

    private Button mFirstUrl;
    private Button mSecondUrl;
    private Button mThirdUrl;
    private Button mFourthUrl;

    private Button mCurrentTimeStamp;

    private TextView mStart, mStartSecond, mStartThird, mStartFourth;
    private TextView mEnd, mEndSecond, mEndThird, mEndFourth;
    private TextView mStartSave, mStartSaveSecond, mStartSaveThird ,mStartSaveFourth;
    private TextView mEndSave, mEndSaveSecond, mEndSaveThird ,mEndSaveFourth;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Test App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initViews();
        initListeners();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 5s
                startDownload();

            }
        }, 5000);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        disposables.clear();

    }

    void startDownload() {
        disposables.add(sampleObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override public void onComplete() {
                        Log.d(TAG, "onComplete()");
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                }));
    }

    public Observable<DownloadProgress<File>> sampleObservable(@NonNull final String filename) {
        return downloadService.downloadFile(filename)
                .switchMap(response -> Observable.fromEmitter(emitter -> {
                    ResponseBody body = response.body();
                    final long contentLength = body.contentLength();
                    ForwardingSource forwardingSource = new ForwardingSource(body.source()) {
                        private long totalBytesRead = 0L;

                        @Override
                        public long read(Buffer sink, long byteCount) throws IOException {
                            long bytesRead = super.read(sink, byteCount);
                            // read() returns the number of bytes read, or -1 if this source is exhausted.
                            totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                            boolean done = bytesRead == -1;
                            float progress = done ? 1f : (float) bytesRead / contentLength;
                            emitter.onNext(new DownloadProgress<>(progress));
                            return bytesRead;
                        }
                    };
                    emitter.setCancellation(body::close);
                    try {
                        File saveLocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsoluteFile(), filename);
                        saveLocation.getParentFile().mkdirs();
                        BufferedSink sink = Okio.buffer(Okio.sink(saveLocation));
                        sink.writeAll(forwardingSource);
                        sink.close();
                        emitter.onNext(new DownloadProgress<>(saveLocation));
                        emitter.onCompleted();
                    } catch (IOException e) {
                        emitter.onError(e);
                    }
                }, Emitter.BackpressureMode.LATEST));
    }

    private void initViews() {

        mFirstUrl = (Button) findViewById(R.id.main_first_url);
        mSecondUrl = (Button) findViewById(R.id.main_second_url);
        mThirdUrl = (Button) findViewById(R.id.main_third_url);
        mFourthUrl = (Button) findViewById(R.id.main_fourth_url);

        mCurrentTimeStamp = (Button) findViewById(R.id.main_current_timestamp);

        mStart = (TextView) findViewById(R.id.card_start);
        mEnd = (TextView) findViewById(R.id.card_end);
        mStartSave = (TextView) findViewById(R.id.card_start_save);
        mEndSave = (TextView) findViewById(R.id.card_end_save);

        mStartSecond = (TextView) findViewById(R.id.card_start_second);
        mEndSecond = (TextView) findViewById(R.id.card_end_second);
        mStartSaveSecond = (TextView) findViewById(R.id.card_start_save_second);
        mEndSaveSecond = (TextView) findViewById(R.id.card_end_save_second);

        mStartThird = (TextView) findViewById(R.id.card_start_third);
        mEndThird = (TextView) findViewById(R.id.card_end_third);
        mStartSaveThird = (TextView) findViewById(R.id.card_start_save_third);
        mEndSaveThird = (TextView) findViewById(R.id.card_end_save_third);

        mStartFourth = (TextView) findViewById(R.id.card_start_fourth);
        mEndFourth = (TextView) findViewById(R.id.card_end_fourth);
        mStartSaveFourth = (TextView) findViewById(R.id.card_start_save_fourth);
        mEndSaveFourth = (TextView) findViewById(R.id.card_end_save_fourth);

    }

    private void initListeners() {

        mFirstUrl.setOnClickListener(this);
        mSecondUrl.setOnClickListener(this);
        mThirdUrl.setOnClickListener(this);
        mFourthUrl.setOnClickListener(this);

        mCurrentTimeStamp.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.main_first_url:
                if (isInternetAvailable()) {

                    Long firstTimeStampLong = System.currentTimeMillis()/1000;
                    String timeStampFirst = getDateCurrentTimeZone(firstTimeStampLong);
                    mStart.setText(timeStampFirst);
                    //new DownloadFiles(MainActivity.this, mFirstUrl, Utils.firstURL, mEnd, mStartSave, mEndSave);

                } else {

                    Toast.makeText(MainActivity.this, "There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.main_second_url:
                if (isInternetAvailable()) {

                    Long secondTimeStampLong = System.currentTimeMillis()/1000;
                    String timeStampSecond = getDateCurrentTimeZone(secondTimeStampLong);
                    mStartSecond.setText(timeStampSecond);
                    //new DownloadFiles(MainActivity.this, mSecondUrl, Utils.secondURL, mEndSecond, mStartSaveSecond, mEndSaveSecond);

                } else {

                    Toast.makeText(MainActivity.this, "There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.main_third_url:
                if (isInternetAvailable()) {

                    Long thirdTimeStampLong = System.currentTimeMillis()/1000;
                    String timeStampThird = getDateCurrentTimeZone(thirdTimeStampLong);
                    mStartThird.setText(timeStampThird);
                    //new DownloadFiles(MainActivity.this, mThirdUrl, Utils.thirdURL, mEndThird, mStartSaveThird, mEndSaveThird);

                } else {

                    Toast.makeText(MainActivity.this, "There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.main_fourth_url:
                if (isInternetAvailable()) {

                    Long fourthTimeStampLong = System.currentTimeMillis()/1000;
                    String timeStampFourth = getDateCurrentTimeZone(fourthTimeStampLong);
                    mStartFourth.setText(timeStampFourth);
                    //new DownloadFiles(MainActivity.this, mFourthUrl, Utils.fourthURL, mEndFourth, mStartSaveFourth, mEndSaveFourth);

                } else {

                    Toast.makeText(MainActivity.this, "There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.main_current_timestamp:

                Long currentTimeStamp = System.currentTimeMillis()/1000;
                String timeStamp = getDateCurrentTimeZone(currentTimeStamp);

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Current TimeStamp");
                alertDialog.setMessage(timeStamp);
                alertDialog.show();

        }

    }

    /*private void startDownload() {

        if (isInternetAvailable()) {

            Long firstTimeStampLong = System.currentTimeMillis()/1000;
            String timeStampFirst = getDateCurrentTimeZone(firstTimeStampLong);
            mStart.setText(timeStampFirst);
            //new InstantDownloadingFiles(MainActivity.this, Utils.firstURL, mEnd, mStartSave, mEndSave);

            Long secondTimeStampLong = System.currentTimeMillis()/1000;
            String timeStampSecond = getDateCurrentTimeZone(secondTimeStampLong);
            mStartSecond.setText(timeStampSecond);
            //new InstantDownloadingFiles(MainActivity.this, Utils.secondURL, mEndSecond, mStartSaveSecond, mEndSaveSecond);

            Long thirdTimeStampLong = System.currentTimeMillis()/1000;
            String timeStampThird = getDateCurrentTimeZone(thirdTimeStampLong);
            mStartThird.setText(timeStampThird);
            //new InstantDownloadingFiles(MainActivity.this, Utils.thirdURL, mEndThird, mStartSaveThird, mEndSaveThird);

            Long fourthTimeStampLong = System.currentTimeMillis()/1000;
            String timeStampFourth = getDateCurrentTimeZone(fourthTimeStampLong);
            mStartFourth.setText(timeStampFourth);
            //new InstantDownloadingFiles(MainActivity.this, Utils.fourthURL, mEndFourth, mStartSaveFourth, mEndSaveFourth);

        } else {

            Toast.makeText(MainActivity.this, "There is no internet connection. Please enable internet connection and try again.", Toast.LENGTH_SHORT).show();

        }

    }*/

    //Check for Internet Connectivity
    private boolean isInternetAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            return true;

        } else {

            return false;

        }
    }

    public String getDateCurrentTimeZone(long timestamp) {

        try{

            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);

        }catch (Exception e) {

        }
        return "";

    }

}
