package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class PrivacyActivity extends AppCompatActivity {

    int reloadCount = 0;
    private RewardedAd rewardedAd;
    BitmapIO bitIO = BitmapIO.getInstance();
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
                if(reloadCount < 10) {
                    reloadCount++;
                    Log.d("privacybanner","errorcode = " + loadAdError.getCode() + "\nreloaded ad = " + reloadCount
                            + "\n" +loadAdError.getMessage());
                    new Handler().postDelayed(() -> adView.loadAd(adRequest), 2000);
                }
            }
        });
        rewardedAd = createAndLoadRewardedAd();
    }

    public void  afterReward(){
        bitIO.setIsRewarded(1);

        Toast.makeText(this,
                "報酬を獲得しました！", Toast.LENGTH_SHORT).show();
        Log.d("MYDEBUG", "user get the reward");
        createAndLoadRewardedAd();
    }

    public void rewardAdButton(View v){
        //リワード広告
        if (rewardedAd != null) {
            rewardedAd.show( this,
                    rewardItem -> afterReward());
        } else {
            Log.d("MYDEBUG", "The rewarded ad wasn't loaded yet.");
        }
    }

    public RewardedAd createAndLoadRewardedAd() {

        FullScreenContentCallback fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Code to be invoked when the ad showed full screen content.
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        // Code to be invoked when the ad dismissed full screen content.
                    }
                };
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd adT){
                // Ad successfully loaded.
                rewardedAd = adT;
                rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Ad failed to load.
                Log.d("maininterstitial","errorcode = " + adError.getCode() + "\nreloaded ad = " + reloadCount
                        + "\n" +adError.getMessage());
                if(reloadCount < 10) {
                    new Handler().postDelayed(() ->  createAndLoadRewardedAd(), 2000);}
            }
        };
        RewardedAd.load(this,"ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    public void BackButton (View v){
        finish();
    }
}