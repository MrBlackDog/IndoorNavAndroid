package com.example.wifiindoornavigation;
import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Element[] nets;
    private WifiManager wifiManager;
    StringBuffer TextToFile = new StringBuffer(1000);
    private List<ScanResult> wifiList;
    public int ScanIterations = 0;

    BroadcastReceiver wifiScanReceiver;
    public String FileName;
    FileOutputStream fos = null;
    TextView ScansItersTextView;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //ScansItersTextView  = findViewById(R.id.IterationsCount);

        //ScansItersTextView.setText(ScanIterations);

        String[] PERMS_INITIAL = {
                Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(this, PERMS_INITIAL, 127);

        wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).startScan();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
                mTimer = new Timer();
                mMyTimerTask = new MyTimerTask();
                mTimer.schedule(mMyTimerTask, 0,30000);
                Snackbar.make(view, "Сканирование...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });

    }

    public void detectWifi() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.wifiManager.startScan();
        this.wifiList = this.wifiManager.getScanResults();

        Log.d("TAG", wifiList.toString());

        this.nets = new Element[wifiList.size()];

        for (int i = 0; i < wifiList.size(); i++) {
            String item = wifiList.get(i).toString();
            String[] vector_item = item.split(",");
            String BSSID =  wifiList.get(i).BSSID;
            String item_essid = vector_item[0];
            String item_capabilities = vector_item[2];
            String item_level = vector_item[3];
            String ssid = item_essid.split(": ")[1];
            String security = item_capabilities.split(": ")[1];
            String level = item_level.split(":")[1];
            nets[i] = new Element(ssid,BSSID ,security, level);
        }

        AdapterElements adapterElements = new AdapterElements(this);
        ListView netList = (ListView) findViewById(R.id.listItem);
        netList.setAdapter(adapterElements);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        EditText editText = findViewById(R.id.myEditText);
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
            {

                return true;
            }
            case R.id.edit_text_menu:
            {
                FileName = editText.getText().toString() ;
                Toast.makeText(this, "New File Name "+ FileName, Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    class AdapterElements extends ArrayAdapter<Object> {
        Activity context;

        public AdapterElements(Activity context) {
            super(context, R.layout.items, nets);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View item = inflater.inflate(R.layout.items, null);

            TextView tvSsid = (TextView) item.findViewById(R.id.tvSSID);
            tvSsid.setText(nets[position].getTitle());

            TextView tvBssid = (TextView) item.findViewById(R.id.tvBSSID);
            tvBssid.setText(nets[position].getBSSID());

            TextView tvSecurity = (TextView) item.findViewById(R.id.tvSecurity);
            tvSecurity.setText(nets[position].getSecurity());

            TextView tvLevel = (TextView) item.findViewById(R.id.tvLevel);
            String level = nets[position].getLevel();
            tvLevel.setText(level);
           /* try{
                int i = Integer.parseInt(level);
                if (i>-50){
                    tvLevel.setText("Высокий");
                } else if (i<=-50 && i>-80){
                    tvLevel.setText("Средний");
                } else if (i<=-80){
                    tvLevel.setText("Низкий");
                }
            } catch (NumberFormatException e){
                Log.d("TAG", "Неверный формат строки");
            }*/
            return item;
        }
    }

    private void scanFailure() {
        wifiList = wifiManager.getScanResults();
    }

    public void scanSuccess() {
        this.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        TextToFile.delete(0,TextToFile.length());
        wifiList = wifiManager.getScanResults();
        this.nets = new Element[wifiList.size()];
        ScanIterations++;
        for (int i = 0; i < wifiList.size(); i++) {
            nets[i] = new Element(wifiList.get(i).SSID,wifiList.get(i).BSSID,wifiList.get(i).capabilities, String.valueOf(wifiList.get(i).level));
          /*  String item = wifiList.get(i).toString();
            String[] vector_item = item.split(",");
            String item_essid = vector_item[0];
            String item_capabilities = vector_item[2];
            String item_level = vector_item[3];
            String ssid = item_essid.split(": ")[1];
            String security = item_capabilities.split(": ")[1];
            String level = item_level.split(":")[1];
            nets[i] = new Element(ssid,wifiList.get(i).BSSID,security, level);*/
            TextToFile.append(nets[i].getTitle()).append(wifiList.get(i).BSSID).append( nets[i].getLevel()).append("\n");
        }
        try {
                if(isExternalStorageWritable()) {
                    File myFile = new File( android.os.Environment.getExternalStorageDirectory(),FileName +".txt");
                    FileOutputStream outputStream = new FileOutputStream(myFile, true);
                    outputStream.write(TextToFile.toString().getBytes());
                    outputStream.close();
                    Toast.makeText(this, "File Appened" + ScanIterations, Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        AdapterElements adapterElements = new AdapterElements(this);
        ListView netList = (ListView) findViewById(R.id.listItem);
        netList.setAdapter(adapterElements);

        if(ScanIterations == 20)
        {
            mTimer.cancel();
           // mTimer.schedule(mMyTimerTask,,1);
            Toast.makeText(this, "Scan Stopped",Toast.LENGTH_SHORT).show();
            getApplicationContext().unregisterReceiver(wifiScanReceiver);
            ScanIterations = 0;
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //http client method
    public void GetZone(float x, float y, float z){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://mrblackdog.ddns.net:533/rawmeas/getMeasurments?Name=Client&X="+x+"&Y="+y+"&Z="+z)
                .build();
        //client.setConnectTimeout(15, TimeUnit.SECONDS);

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            }
        });
    }

    //timer class
    class MyTimerTask extends TimerTask {

    @Override
    public void run() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE)).startScan();
            }
        });
    }
}

    @Override
    protected void onDestroy() {
        //this.unregisterReceiver(wifiScanReceiver);
        super.onDestroy();
    }
}