package com.mediatek.mcstutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.mediatek.mcs.Utils.UIUtils;
import com.mediatek.mcs.domain.McsDataChannel;
import com.mediatek.mcs.domain.McsResponse;
import com.mediatek.mcs.entity.DataChannelEntity;
import com.mediatek.mcs.entity.DataPointEntity;
import com.mediatek.mcs.entity.HistoryDataPointsEntity;
import com.mediatek.mcs.entity.api.DeviceInfoEntity;
import com.mediatek.mcs.entity.api.DeviceSummaryEntity;
import com.mediatek.mcs.net.McsJsonRequest;
import com.mediatek.mcs.net.RequestApi;
import com.mediatek.mcs.net.RequestManager;
import com.mediatek.mcs.socket.McsSocketListener;
import com.mediatek.mcs.socket.SocketManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Exchanger;
import java.util.concurrent.RunnableFuture;

import org.json.JSONException;
import org.json.JSONObject;

public class newActivity extends AppCompatActivity {

    String mDeviceId = "";
    DeviceInfoEntity mDeviceInfo;
    McsDataChannel mDataChannel;
    TextView condition, comfort, health, all;
    Button button;
    List<Float> data = new LinkedList<Float>();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private TimerTask timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        this.condition = (TextView) findViewById((R.id.condition));
        this.comfort = (TextView) findViewById((R.id.comfort));
        this.health = (TextView) findViewById((R.id.health));
        this.all = (TextView) findViewById(R.id.all);
        button = (Button) findViewById(R.id.button);
        final Intent intent = new Intent(newActivity.this, SessionActivity.class);
        startActivity(intent);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        start_req();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_req();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * GET device list.
     */
    private void requestDevices() {
        // Default method is GET
        int method = McsJsonRequest.Method.GET;
        String url = RequestApi.DEVICES;
        McsResponse.SuccessListener<JSONObject> successListener =
                new McsResponse.SuccessListener<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        List<DeviceSummaryEntity> summary = new Gson().fromJson(
                                response.toString(), DeviceSummaryEntity.class).getResults();

                        if (summary.size() > 0) {
                            mDeviceId = summary.get(0).getDeviceId();
                        }
                        //   printJson(response);
                    }
                };

        /**
         * Optional.
         * Default error message would be shown in logcat.
         */
        McsResponse.ErrorListener errorListener = new McsResponse.ErrorListener() {
            @Override
            public void onError(Exception e) {

            }
        };

        McsJsonRequest request = new McsJsonRequest(method, url, successListener, errorListener);
        RequestManager.sendInBackground(request);
    }

    /**
     * GET device info.
     */
    private void requestDeviceInfo(String deviceId) {
        McsJsonRequest request = new McsJsonRequest(
                RequestApi.DEVICE
                        .replace("{deviceId}", deviceId),
                new McsResponse.SuccessListener<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        mDeviceInfo = UIUtils.getFormattedGson()
                                .fromJson(response.toString(), DeviceInfoEntity.class)
                                .getResults().get(0);
                        List<DataChannelEntity> d = mDeviceInfo.getDataChannels();
                        all.setText(String.valueOf(d.size()));
                        if(d != null){
                            for(int i = 0; i < d.size(); i++){
                                if(d.get(i).getDataPoint() == null){
                                    float a = 0;
                                    data.add(a);
                                }
                                else{
                                    float value = Float.valueOf(d.get(i).getDataPoint().getValues().getValue());
                                    data.add(value);
                                }
                            }
                        }
                        else Toast.makeText(newActivity.this, "No data to show", Toast.LENGTH_SHORT).show();
                        calculate();

                    }
                }
        );

        RequestManager.sendInBackground(request);
    }

    /**
     * Socket control of single data channel
     */
    private void turnOnSocket() {
        SocketManager.connectSocket();
        SocketManager.registerSocket(mDataChannel, mDataChannel.getMcsSocketListener());
    }

    private void turnOffSocket() {
        SocketManager.unregisterSocket(mDataChannel, mDataChannel.getMcsSocketListener());
        SocketManager.disconnectSocket();
    }

    private void start_req(){
        requestDevices();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestDeviceInfo(mDeviceId);
            }
        }, 3000);
    }

    private void calculate(){

      float inA = data.get(2);
      float outA = data.get(3);
      float inB = data.get(1);
      float outB = data.get(4);
      float inC = data.get(0);
      float outC = data.get(5);
      float temp = data.get(7);
      float hum =  data.get(6);
      float A = inA + outA;
      float B = inB + outB;
      float C = inC + outC;
      float in = inA + inB + inC;
      float out = outA + outB + outC;
      float arch = B/(A+B+C);
      float dir = in/out;
      int total = 10;
      if((temp > 26 && temp < 30) || hum > 60){
          comfort.setText("不適");
          total -= 3;
      }
      else if(temp > 30|| hum > 88){
          comfort.setText("極度不適");
          total -= 5;
      }
      else comfort.setText("舒適");

      if(A < 100 || C < 100){  //walking
        condition.setText("站立");
        if(arch > 0.26){
            health.setText("足弓過低(扁平)");
            total -= 5;
        }
        else if(arch < 0.21){
            health.setText("足弓過高");
            total -= 5;
        }
        else health.setText("健康");
      }
      else{
          condition.setText("步行");
          if(dir > 0.5){
              health.setText("內八");
              total -= 5;
          }
          else if(dir < 0.5){
              health.setText("外八");
              total -= 5;
          }
          else health.setText("健康");
      }

      all.setText(String.valueOf(total) + " / 10");






    }
    /**
     * Pretty-print a JSONObject.
     */
//    private void printJson(JSONObject jsonObject) {
//        try {
//            comfort_tude.setText(jsonObject.toString(2));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "new Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.mediatek.mcstutorial/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "new Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.mediatek.mcstutorial/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }


}