package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;


public class BitmapIO extends AppCompatActivity {
    Bitmap bitmap; //画像のbitmap
    int adCount = 0;
    boolean type = true; //画像の形式　trueがjpeg、falseがpng、デフォルトはJPEG


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isType() { return type; }

    public void setType(boolean type) { this.type = type; }

    public void convertJPEG(Bitmap bitmap, OutputStream fos){ //JPEG形式に変換するメソッド
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }

    public void convertPNG(Bitmap bitmap, OutputStream fos){ //PNG形式に変換するメソッド
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
    }

    public void addAdCount(){
        adCount++;
        if(adCount > 10){
            adCount = 0;
        }
    }

    public boolean getAdcount(){
        return adCount % 2 == 0;
    }
}
