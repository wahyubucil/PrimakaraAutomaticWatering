package dev.primakara.automatic_watering.primakaraautomaticwatering;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ApiService mApiService;

    @BindView(R.id.srl_main) SwipeRefreshLayout mMainSwipeRefresh;

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

        mApiService = MainApplication.getApiClient().getApiService();

        mMainSwipeRefresh.setOnRefreshListener(this);

        checkWifiConnected();

        setupMainLayout();
        setupFailedConnectLayout();
    }

    @Override
    public void onRefresh() {
        checkWifiConnected();
    }

    private void setupMainLayout() {
        mAutomaticWateringSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            handleAutomaticWateringSwitch(isChecked);
        });

        mFlushButton.setOnClickListener(view -> {
            handleFlushButton();
        });
    }

    private void handleAutomaticWateringSwitch(boolean isChecked) {
        mMainLoading.setVisibility(View.VISIBLE);
        if (isChecked) {
            mApiService.getOn().enqueue(new Callback<Watering>() {
                @Override
                public void onResponse(Call<Watering> call, Response<Watering> response) {
                    Watering watering = response.body();
                    handleAutomaticWateringResponse(watering);
                }

                @Override
                public void onFailure(Call<Watering> call, Throwable t) {
                    //TODO: ERROR LAYOUT
                }
            });
        } else {
            mApiService.getOff().enqueue(new Callback<Watering>() {
                @Override
                public void onResponse(Call<Watering> call, Response<Watering> response) {
                    Watering watering = response.body();
                    handleAutomaticWateringResponse(watering);
                }

                @Override
                public void onFailure(Call<Watering> call, Throwable t) {
                    //TODO: ERROR LAYOUT
                }
            });
        }
    }

    private void handleAutomaticWateringResponse(Watering watering) {
        mMainLoading.setVisibility(View.GONE);

        if (watering != null && watering.isSuccess()) {
            mAutomaticWateringSwitch.setChecked(watering.isAutomaticWatering());
            toggleFlushButton(watering.isAutomaticWatering());
        } else {
            //TODO: ERROR LAYOUT
        }
    }

    private void handleFlushButton() {
        mMainLoading.setVisibility(View.VISIBLE);
        mApiService.getFlush().enqueue(new Callback<Watering>() {
            @Override
            public void onResponse(Call<Watering> call, Response<Watering> response) {
                mMainLoading.setVisibility(View.GONE);
                Watering watering = response.body();
                if (watering != null && watering.isSuccess()) {
                    Toast.makeText(MainActivity.this,
                            "Refresh dalam beberapa saat untuk cek kelembapan setelah disiram",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Watering> call, Throwable t) {
                //TODO: ERROR LAYOUT
            }
        });
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
                mMainSwipeRefresh.setRefreshing(false);
            }
        }
    }

    private void loadData() {
        mApiService.getRoot().enqueue(new Callback<Watering>() {
            @Override
            public void onResponse(Call<Watering> call, Response<Watering> response) {
                mMainLoading.setVisibility(View.GONE);
                mMainSwipeRefresh.setRefreshing(false);

                Watering watering = response.body();
                if (watering != null && watering.isSuccess()) {
                    handleSuccessData(watering);
                } else {
                    // TODO: ERROR LAYOUT
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
        mMainLayout.setVisibility(View.VISIBLE);

        mAutomaticWateringSwitch.setChecked(watering.isAutomaticWatering());

        toggleFlushButton(watering.isAutomaticWatering());

        String humidityString = String.valueOf(watering.getHumidity());
        mHumidityTextView.setText(humidityString);

        if (watering.isDry()) {
            mHumidityStatusTextView.setText(R.string.dry_soil);

            int orangeColorDry = getResources().getColor(R.color.colorOrangeDry);
            mHumidityStatusTextView.setTextColor(orangeColorDry);

            GradientDrawable background = (GradientDrawable) mHumidityTextView.getBackground();
            background.setColor(ContextCompat.getColor(this, R.color.colorOrangeDry));
        } else {
            mHumidityStatusTextView.setText(R.string.wet_soil);

            int blueColorWet = getResources().getColor(R.color.colorBlueWet);
            mHumidityStatusTextView.setTextColor(blueColorWet);

            GradientDrawable background = (GradientDrawable) mHumidityTextView.getBackground();
            background.setColor(ContextCompat.getColor(this, R.color.colorBlueWet));
        }
    }

    private void toggleFlushButton(boolean isAutomaticWatering) {
        if (isAutomaticWatering) {
            mFlushButton.setVisibility(View.GONE);
        } else {
            mFlushButton.setVisibility(View.VISIBLE);
        }
    }

}
