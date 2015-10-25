package com.wordpress.chombium.eldemo.mqtt;

import com.google.gson.Gson;

public class MqttPayloadConverterImpl implements MqttPayloadConverter {

	private Gson gson;
	
	public MqttPayloadConverterImpl(Gson gson) {
		super();
		this.gson = gson;
	}

	@Override
	public String toJson(MqttPayload obj) {
		return gson.toJson(obj);
	}

	@Override
	public MqttPayload fromJson(String json) {
		return gson.fromJson(json, MqttPayload.class);
	}

}
