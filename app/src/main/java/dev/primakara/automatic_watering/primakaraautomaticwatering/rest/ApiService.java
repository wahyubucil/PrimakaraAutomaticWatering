package dev.primakara.automatic_watering.primakaraautomaticwatering.rest;

import dev.primakara.automatic_watering.primakaraautomaticwatering.model.Watering;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("/")
    Call<Watering> getRoot();
}
