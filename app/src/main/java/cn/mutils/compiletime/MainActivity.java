package cn.mutils.compiletime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView messageText = (TextView) findViewById(R.id.message);
        try {
            messageText.append("App=");
            messageText.append(PageActionUtil.getPage("App").getName());
            messageText.append("\nLibrary=");
            messageText.append(PageActionUtil.getPage("Library").getName());
        } catch (Exception e) {
            messageText.append(e.toString());
        }

    }
}
