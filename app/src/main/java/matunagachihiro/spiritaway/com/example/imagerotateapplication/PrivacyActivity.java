package matunagachihiro.spiritaway.com.example.imagerotateapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        TextView textView = findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void BackButton (View v){
        finish();
    }
}