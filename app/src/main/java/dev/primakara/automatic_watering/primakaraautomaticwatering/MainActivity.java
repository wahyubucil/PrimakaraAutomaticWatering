package dev.primakara.automatic_watering.primakaraautomaticwatering;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.primakara.automatic_watering.primakaraautomaticwatering.model.Watering;
import dev.primakara.automatic_watering.primakaraautomaticwatering.rest.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ApiService mApiService;

    @BindView(R.id.main_layout) View mMainLayout;
    @BindView(R.id.switch_automatic_watering) Switch mAutomaticWateringSwitch;
    @BindView(R.id.tv_humidity) TextView mHumidityTextView;
    @BindView(R.id.tv_humidity_status) TextView mHumidityStatusTextView;
    @BindView(R.id.btn_flush) Button mFlushButton;

    @BindView(R.id.failed_connect) View mFailedConnect;

    @BindView(R.id.main_loading) View mMainLoading;
    @BindView(R.id.btn_connect) Button mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        checkWifiConnected();

        setupFailedConnectLayout();
    }

    private void setupFailedConnectLayout() {
        mConnectButton.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
        });
    }

    private void checkWifiConnected() {
        WifiManager wifiManager = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
        }

        String ssid = null;
        if (wifiInfo != null) {
            ssid = wifiInfo.getSSID();
        }

        if (ssid != null) {
            if (ssid.equals(getString(R.string.wifi_ssid))) {
                mFailedConnect.setVisibility(View.GONE);
                loadData();
            } else {
                mFailedConnect.setVisibility(View.VISIBLE);
                mMainLoading.setVisibility(View.GONE);
            }
        }
    }

    private void loadData() {
        mApiService = MainApplication.getApiClient().getApiService();

        mApiService.getRoot().enqueue(new Callback<Watering>() {
            @Override
            public void onResponse(Call<Watering> call, Response<Watering> response) {
                Watering watering = response.body();
                if (watering != null) {
                    handleSuccessData(watering);
                }
            }

            @Override
            public void onFailure(Call<Watering> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error retrieving data from internet : " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "OnFailure : " + t.getMessage());
                // TODO: CREATE ERROR LAYOUT
            }
        });
    }

    private void handleSuccessData(Watering watering) {
        if (watering.isSuccess()) {
            mMainLoading.setVisibility(View.GONE);
            mMainLayout.setVisibility(View.VISIBLE);

            mAutomaticWateringSwitch.setChecked(watering.isAutomaticWatering());

            String humidityString = String.valueOf(watering.getHumidity());
            mHumidityTextView.setText(humidityString);

            int humidityStatus = watering.isDry() ? R.string.dry_soil : R.string.wet_soil;
            mHumidityStatusTextView.setText(humidityStatus);
        } else {
            // TODO: USE ERROR LAYOUT
        }
    }
}
