package dev.primakara.automatic_watering.primakaraautomaticwatering.model;

import com.google.gson.annotations.SerializedName;

public class Watering{

	@SerializedName("success")
	private boolean success;

	@SerializedName("humidity")
	private int humidity;

	@SerializedName("automatic_watering")
	private boolean automaticWatering;

	public void setSuccess(boolean success){
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public void setHumidity(int humidity){
		this.humidity = humidity;
	}

	public int getHumidity(){
		return humidity;
	}

	public void setAutomaticWatering(boolean automaticWatering){
		this.automaticWatering = automaticWatering;
	}

	public boolean isAutomaticWatering(){
		return automaticWatering;
	}

	@Override
 	public String toString(){
		return 
			"Watering{" + 
			"success = '" + success + '\'' + 
			",humidity = '" + humidity + '\'' + 
			",automatic_watering = '" + automaticWatering + '\'' + 
			"}";
		}
}