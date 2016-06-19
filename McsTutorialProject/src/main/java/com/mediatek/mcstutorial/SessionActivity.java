package com.mediatek.mcstutorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.mcs.domain.McsResponse;
import com.mediatek.mcs.domain.McsSession;
import com.mediatek.mcs.pref.McsUser;
import org.json.JSONObject;

public class SessionActivity extends AppCompatActivity {

   String email = "shi359@gmail.com";
    String pwd="nthuteam08";
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

        requestSignIn();
        onBackPressed();
  }

  private void requestSignIn() {


    McsSession.getInstance().requestSignIn(email, pwd,
        new McsResponse.SuccessListener<JSONObject>() {
          @Override public void onSuccess(JSONObject response) {
              Toast.makeText(SessionActivity.this,"User sign in successfully", Toast.LENGTH_SHORT).show();
          }
        },
        /**
         * Optional.
         * Default error message would be shown in logcat.
         */
        new McsResponse.ErrorListener() {
          @Override public void onError(Exception e) {

          }
        });
  }

  private void requestSignOut() {
    McsSession.getInstance().requestSignOut(
        new McsResponse.SuccessListener<JSONObject>() {
          @Override public void onSuccess(JSONObject response) {

          }
        }
    );
  }
}
