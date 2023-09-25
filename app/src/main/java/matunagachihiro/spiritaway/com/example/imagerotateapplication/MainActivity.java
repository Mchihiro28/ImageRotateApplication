package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    int reloadCount = 0;
    int useLimit = 0; //一日の利用回数を制限する変数
    int today = 0;
    int yesterday = 0;
    BitmapIO bitIO = BitmapIO.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.ResultView);
        bitIO.setBitmap(((BitmapDrawable)imageView.getDrawable()).getBitmap());


        //バナー広告表示
        MobileAds.initialize(this,
                initializationStatus -> {
                });

        //AdRequest
        reloadCount = 0;
        adRequest = new AdRequest.Builder().build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadCount = 0;
        today = getDate();
        int[] limDay = readUseData();
        useLimit = limDay[0];
        yesterday = limDay[1];
        if(yesterday != today){
            if(useLimit < 5) {
                useLimit = 5;
            }
        }
        if(bitIO.getIsRewarded() > 0){
            useLimit += 10;
            bitIO.setIsRewarded(0);
        }
        saveUseData(useLimit,yesterday);
        ((TextView) findViewById(R.id.textView2)).setText("あと"+ useLimit +"回");
        loadInterstitial(adRequest);
    }

    public int[] readUseData(){
        SharedPreferences data = getSharedPreferences("Data", MODE_PRIVATE);
        int[] result = new int[2];
        result[0] = data.getInt("useLimit",5);
        result[1] = data.getInt("today",0);

        return result;
    }

    public void saveUseData(int useLimit, int today){
        SharedPreferences data = getSharedPreferences("Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();

        editor.putInt("useLimit",useLimit);
        editor.putInt("today", today);

        editor.apply();
    }

    public void loadInterstitial(AdRequest adRe){

        InterstitialAd.load(this,
                "ca-app-pub-2742833893230662/4101693897",
                adRe,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                        Log.d("maininterstitial","errorcode = " + loadAdError.getCode() + "\nreloaded ad = " + reloadCount
                                + "\n" +loadAdError.getMessage());
                        if(reloadCount < 5) {
                            new Handler().postDelayed(() ->  loadInterstitial(adRe), 2000);
                        }
                    }
                });
    }

    ImageView imageView;

    // Matrix インスタンス生成
    Matrix matrix = new Matrix();
    int imageWidth;
    int imageHeight;
    InterstitialAd mInterstitialAd;
    AdRequest adRequest;


    public void importButton(View v){
        getImage();
    }

    ActivityResultLauncher<Intent> _launcherSelectSingleImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> { //取得する用のランチャー
                if (result.getResultCode() == RESULT_OK) {
                    Intent resultData = result.getData();
                    if (resultData != null) {
                        Uri uri = resultData.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            bitIO.setBitmap(bitmap);
                            //ここにデータが保存される
                            imageView.setImageBitmap(bitIO.getBitmap());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    public void getImage(){  //画像をアルバムから取得するメソッド
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        Intent chooserIntent = Intent.createChooser(intent, "画像を選択してください");

        _launcherSelectSingleImage.launch(chooserIntent);
    }

    public void  saveButton(View v){
        if(useLimit > 0) {
            Toast toast = Toast.makeText(this, "今日はあと"+ useLimit +"回使えます", Toast.LENGTH_LONG);
            toast.show();
            final String[] items = {"JPEG", "PNG"};
            new AlertDialog.Builder(this)
                    .setTitle("保存する画像のタイプを選択してください")
                    .setItems(items, (dialog, which) -> {
                        // item_which pressed
                        useLimit-=1;
                        saveUseData(useLimit,today);
                        if (which == 0) {
                            bitIO.setType(true);
                            createFile();
                        } else {
                            bitIO.setType(false);
                            createFile();
                        }

                        //インタースティシャル広告の表示
                        bitIO.addAdCount();
                        if (bitIO.getAdcount()%2 == 0) {
                            showInterstitial();
                        }
                    })
                    .show();
        }else{
            Toast toast = Toast.makeText(this, "申し訳ございません。一日の使用回数を超えました", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void showInterstitial(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(MainActivity.this);
        } else {
            Toast.makeText(MainActivity.this,
                    "広告の読み込みに失敗しました。", Toast.LENGTH_LONG).show();
        }
    }


    public void createFile() {
        String fileName;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        if(bitIO.isType()) {
            fileName = "pic.jpeg";
            intent.setType("image/jpeg");
        }else{
            fileName = "pic.png";
            intent.setType("image/png");
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        activityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> { //保存する用のランチャー
                if ( result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        //結果を受け取った後の処理
                        Intent resultData = result.getData();
                        Uri uri = resultData.getData();
                        try(OutputStream outputStream =
                                    getContentResolver().openOutputStream(uri)) {
                            if(outputStream != null){
                                if(bitIO.isType()){
                                    bitIO.convertJPEG(bitIO.getBitmap(), outputStream);
                                }else{
                                    bitIO.convertPNG(bitIO.getBitmap(), outputStream);
                                }
                            }

                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });
    
    public void  rotateRightButton(View v){
        imageWidth = bitIO.getBitmap().getWidth();
        imageHeight = bitIO.getBitmap().getHeight();
        // 画像中心を基点に90度回転
        matrix.setRotate(90, imageWidth/2f, imageHeight/2f);
        bitIO.setBitmap(Bitmap.createBitmap(bitIO.getBitmap(), 0, 0,
                imageWidth, imageHeight, matrix, true));

        imageView.setImageBitmap(bitIO.getBitmap());
    }

    public  void rotateLeftButton(View v){
        // 画像の横、縦サイズを取得
        imageWidth = bitIO.getBitmap().getWidth();
        imageHeight = bitIO.getBitmap().getHeight();
        matrix.setRotate(-90, imageWidth/2f, imageHeight/2f);
        bitIO.setBitmap(Bitmap.createBitmap(bitIO.getBitmap(), 0, 0,
                imageWidth, imageHeight, matrix, true));

        imageView.setImageBitmap(bitIO.getBitmap());
    }

    public void goPrivacyActivity (View v){
        Intent intent = new Intent(this,PrivacyActivity.class);
        startActivity(intent);
    }

    public static int getDate(){//現在の日付を返すメソッド

        //取得する日時のフォーマットを指定
        @SuppressLint("SimpleDateFormat") final DateFormat df = new SimpleDateFormat("dd");

        //時刻をミリ秒で取得
        final Date date = new Date(System.currentTimeMillis());

        //日時を指定したフォーマットで取得
        return Integer.parseInt(df.format(date));
    }



}