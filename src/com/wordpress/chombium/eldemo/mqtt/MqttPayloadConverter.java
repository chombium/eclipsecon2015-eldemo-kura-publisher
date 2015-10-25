package com.wordpress.chombium.eldemo.mqtt;

public interface MqttPayloadConverter {
	public String toJson(MqttPayload obj);
	public MqttPayload fromJson(String json);
}
