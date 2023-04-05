package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;

public class PrivacyActivity extends AppCompatActivity {

    int reloadCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        MobileAds.initialize(this,
                initializationStatus -> {
                });

        reloadCount = 0;
        AdView adView = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if(reloadCount < 5) {
                    reloadCount++;
                    Log.d("privacybanner","errorcode = " + loadAdError.getCode() + "\nreloaded ad = " + reloadCount
                            + "\n" +loadAdError.getMessage());
                    new Handler().postDelayed(() -> adView.loadAd(adRequest), 2000);
                }
            }
        });
    }

    public void BackButton (View v){
        finish();
    }
}