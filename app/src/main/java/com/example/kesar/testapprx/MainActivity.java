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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import rx.Subscriber;
import rx.functions.Action;
import rx.functions.Action1;
import rx.functions.Func2;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "Successfull";
    private String uploadUrl = "http://10.0.2.2/TestApp/updateinfo.php";

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

    private Observable<String> mObservable;
    private Observer<String> mObserver;

    File apkStorage = null;
    File outputFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Test App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initViews();
        initListeners();

        Observable<String> firstUrl = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String data = Utils.firstURL;
            }
        });

        Observable<String> secondUrl = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String data = Utils.secondURL;
            }
        });

        Observable<String> thirdUrl = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String data = Utils.thirdURL;
            }
        });

        Observable<String> fourthUrl = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String data = Utils.fourthURL;
            }
        });

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

        mObservable = Observable.fromArray(Utils.firstURL, Utils.secondURL, Utils.thirdURL, Utils.fourthURL);

        mObservable.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {

                try {

                    URL url = new URL(s);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    c.connect();

                    if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {

                        Log.e(TAG, "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage());

                    }

                    if (new CheckForSDCard().isSDCardPresent()) {

                        apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + Utils.downloadDirectory);

                    } else {

                        Toast.makeText(MainActivity.this, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();

                    }

                    if (!apkStorage.exists()) {

                        apkStorage.mkdir();
                        Log.e(TAG, "Directory Created.");

                    }

                    outputFile = new File(apkStorage, s);

                    if (!outputFile.exists()) {

                        outputFile.createNewFile();
                        Log.e(TAG, "File Created");

                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;

                    while ((len1 = is.read(buffer)) != -1) {

                        fos.write(buffer, 0, len1);

                    }

                    fos.close();
                    is.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    outputFile = null;
                    Log.e(TAG, "Download Error Exception " + e.getMessage());

                }

            }

            @Override
            public void onError(Throwable e) {

                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());

            }

            @Override
            public void onComplete() {

                try {
                    if (outputFile != null) {

                        Long secondTimeStampLong = System.currentTimeMillis()/1000;
                        String timeStampSecond = getDateCurrentTimeZone(secondTimeStampLong);
                        mEnd.setText(timeStampSecond);

                        Toast.makeText(MainActivity.this, "Files Downloaded Successfully", Toast.LENGTH_LONG).show();

                        Long startTimeStampLong = System.currentTimeMillis()/1000;
                        String timeStartStamp = getDateCurrentTimeZone(startTimeStampLong);
                        mStartSave.setText(timeStartStamp);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                uploadFile();

                            }
                        }, 1419);

                    } else {

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(MainActivity.this, "Downloading Failed", Toast.LENGTH_LONG).show();

                            }
                        }, 3000);

                        Log.e(TAG, "Download Failed");

                    }
                } catch (Exception e) {

                    e.printStackTrace();

                    //buttonText.setText("First Url");
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                        }
                    }, 3000);

                    Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

                }

            }
        });

    }

    public void subscribeNow(String string) {

        mObservable.subscribe(mObserver);

    }

    private void uploadFile() {

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Response = jsonObject.getString("response");

                    Long endTimeStampLong = System.currentTimeMillis()/1000;
                    String timeStampSecond = getDateCurrentTimeZone(endTimeStampLong);
                    mEndSave.setText(timeStampSecond);

                    Toast.makeText(MainActivity.this, "Files Uploaded on Database", Toast.LENGTH_LONG).show();

                    Toast.makeText(MainActivity.this, Response, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("name", name.trim());
                params.put("file", String.valueOf(outputFile));

                return params;
            }
        };

        MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
        Long endTimeStampLong = System.currentTimeMillis()/1000;
        String timeStampSecond = getDateCurrentTimeZone(endTimeStampLong);
        mEndSave.setText(timeStampSecond);

        Toast.makeText(MainActivity.this, "Files Uploaded on Database", Toast.LENGTH_LONG).show();

    }


    private void initViews() {

        mFirstUrl = findViewById(R.id.main_first_url);
        mSecondUrl = findViewById(R.id.main_second_url);
        mThirdUrl = findViewById(R.id.main_third_url);
        mFourthUrl = findViewById(R.id.main_fourth_url);

        mCurrentTimeStamp = findViewById(R.id.main_current_timestamp);

        mStart = findViewById(R.id.card_start);
        mEnd = findViewById(R.id.card_end);
        mStartSave = findViewById(R.id.card_start_save);
        mEndSave = findViewById(R.id.card_end_save);

        mStartSecond = findViewById(R.id.card_start_second);
        mEndSecond = findViewById(R.id.card_end_second);
        mStartSaveSecond = findViewById(R.id.card_start_save_second);
        mEndSaveSecond = findViewById(R.id.card_end_save_second);

        mStartThird = findViewById(R.id.card_start_third);
        mEndThird = findViewById(R.id.card_end_third);
        mStartSaveThird = findViewById(R.id.card_start_save_third);
        mEndSaveThird = findViewById(R.id.card_end_save_third);

        mStartFourth = findViewById(R.id.card_start_fourth);
        mEndFourth = findViewById(R.id.card_end_fourth);
        mStartSaveFourth = findViewById(R.id.card_start_save_fourth);
        mEndSaveFourth = findViewById(R.id.card_end_save_fourth);

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

    //Check for Internet Connectivity
    private boolean isInternetAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public String getDateCurrentTimeZone(long timestamp) {

        try{

            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);

        }catch (Exception e) {

        }
        return "";

    }

}
