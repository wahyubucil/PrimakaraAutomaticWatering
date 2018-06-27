package dev.primakara.automatic_watering.primakaraautomaticwatering;

import android.app.Application;

import dev.primakara.automatic_watering.primakaraautomaticwatering.rest.ApiClient;

public class MainApplication extends Application {

    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        apiClient = new ApiClient();
    }

    public static ApiClient getApiClient() {
        return apiClient;
    }
}
