package project001.itcore.it_talk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
/**
 * Created by peten on 2017. 4. 26..
 *
 * SplshActivity.java
 *
 *
 */

public class SplashActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

       try{
            // 2초간 대기
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this,MainActivity.class));
        finish();
    }
}
