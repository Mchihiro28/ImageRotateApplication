package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.ResultView);
        bitIO.setBitmap(((BitmapDrawable)imageView.getDrawable()).getBitmap());

    }

    ImageView imageView;
    BitmapIO bitIO = new BitmapIO();
    // Matrix インスタンス生成
    Matrix matrix = new Matrix();
    int imageWidth;
    int imageHeight;


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
        final String[] items = {"JPEG", "PNG"};
        new AlertDialog.Builder(this)
                .setTitle("保存する画像のタイプを選択してください")
                .setItems(items, (dialog, which) -> {
                    // item_which pressed
                    if(which == 0){
                        bitIO.setType(true);
                        createFile();
                    }else{
                        bitIO.setType(false);
                        createFile();
                    }
                })
                .show();
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
        matrix.setRotate(90, imageWidth/2, imageHeight/2);
        bitIO.setBitmap(Bitmap.createBitmap(bitIO.getBitmap(), 0, 0,
                imageWidth, imageHeight, matrix, true));

        imageView.setImageBitmap(bitIO.getBitmap());
    }

    public  void rotateLeftButton(View v){
        // 画像の横、縦サイズを取得
        imageWidth = bitIO.getBitmap().getWidth();
        imageHeight = bitIO.getBitmap().getHeight();
        matrix.setRotate(-90, imageWidth/2, imageHeight/2);
        bitIO.setBitmap(Bitmap.createBitmap(bitIO.getBitmap(), 0, 0,
                imageWidth, imageHeight, matrix, true));

        imageView.setImageBitmap(bitIO.getBitmap());
    }

    public void goPrivacyActivity (View v){
        Intent intent = new Intent(this,PrivacyActivity.class);
        startActivity(intent);
    }
}