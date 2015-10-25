package com.wordpress.chombium.eldemo.mqtt;

import java.util.Date;

public class MqttPayload {
	private Date timestamp;
	private String unit;
	private String value;
	
	public MqttPayload(){}
	
	public MqttPayload(Date timestamp, String unit, String value) {
		super();
		this.timestamp = timestamp;
		this.unit = unit;
		this.value = value;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MqttPayload [timestamp=" + timestamp + ", unit=" + unit
				+ ", value=" + value + "]";
	}
}
