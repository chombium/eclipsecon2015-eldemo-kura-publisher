package com.wordpress.chombium.eldemo.publisher;

import java.util.Date;
import java.util.Map;

import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wordpress.chombium.eldemo.mqtt.MqttPayload;
import com.wordpress.chombium.eldemo.mqtt.MqttPayloadConverterImpl;
import com.wordpress.chombium.eldemo.sensors.SensorChangedListener;
import com.wordpress.chombium.eldemo.sensors.SensorService;

public class ElDemoPublisher implements ConfigurableComponent, SensorChangedListener, CloudClientListener {
    private static final Logger s_logger = LoggerFactory.getLogger(ElDemoPublisher.class);
    private static final String APP_ID = "ELDEMOPUBLISHER";
    private Map<String, Object> _properties;
    
	private static final String CB_INITIAL_PROP_NAME      = "cb.initial.value";
	private static final String CB_TOPIC_CMD_PROP_NAME    = "cb.command.topic";
	private static final String CB_TOPIC_STATUS_PROP_NAME = "cb.status.topic";
	
	private static final String QOS_PROP_NAME            = "qos";
	private static final String PUBLISH_RATE_PROP_NAME   = "publish.rate";
	private static final String PUBLISH_RETAIN_PROP_NAME = "publish.retain";
	
	private static final String TIMESTAMP_FORMAT_PROP_NAME = "timestamp.format";

	private SensorService _sensorService;
	private CloudService _cloudService;
	private CloudClient  _cloudClient;
	
	private MqttPayloadConverterImpl _payloadConverter;
	
	public ElDemoPublisher() {
		super();
	}
	
	protected void setSensorService(
			SensorService sensorService) {
		_sensorService = sensorService;
	}

	protected void unsetSensorService(
			SensorService sensorService) {
		_sensorService = null;
	}

	public void setCloudService(CloudService cloudService) {
		s_logger.info(APP_ID + "Set CloudService");
		_cloudService = cloudService;
	}

	public void unsetCloudService(CloudService cloudService) {
		s_logger.info(APP_ID + "UnSet CloudService");
		_cloudService = null;
	}
	
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
    	s_logger.info("Activating With Configuration " + APP_ID);
    	
        updated(properties);
        
		String timestampFormat  = (String) _properties.get(TIMESTAMP_FORMAT_PROP_NAME);
		Gson gson               = new GsonBuilder().setDateFormat(timestampFormat).create();
		_payloadConverter   = new MqttPayloadConverterImpl(gson);
        
		// get the mqtt client for this application
		try  {
			
			// Acquire a Cloud Application Client for this Application 
			s_logger.info("Getting CloudClient for {}...", APP_ID);
			_cloudClient = _cloudService.newCloudClient(APP_ID);
			_cloudClient.addCloudClientListener(this);
			
			if (_cloudClient.isConnected()) {
				this.manageTopics(true);
			}
		}
		catch (Exception e) {
			s_logger.error("Error during component activation", e);
			throw new ComponentException(e);
		}
        s_logger.info("Bundle " + APP_ID + " has started with config!");
    }
    
    protected void deactivate(ComponentContext componentContext) {
    	
 		s_logger.info("Releasing CloudApplicationClient for {}...", APP_ID);
 		
		if ( _cloudClient != null && _cloudClient.isConnected()) {
			this.manageTopics(true);
		}

		_cloudClient.release();
        s_logger.info("Bundle " + APP_ID + " has stopped!");
    }
    
    public void updated(Map<String, Object> properties) {
		s_logger.info("Updated " + APP_ID + "...");

		// store the properties received
		this._properties = properties;
		for (String s : properties.keySet()) {
			s_logger.info("Update - " + s + ": "+properties.get(s));
		}
		
		s_logger.info("Updated " + APP_ID + "... Done.");
    }
    
	private synchronized void publishMessage(String topic, MqttPayload mqttPayload) {
		Integer qos    = (Integer) this._properties.get(QOS_PROP_NAME);
		Boolean retain = (Boolean) this._properties.get(PUBLISH_RETAIN_PROP_NAME);
		
		// Publish the message
		try {
			try {
				String json = _payloadConverter.toJson(mqttPayload);
				
				// Allocate a new payload
				KuraPayload payload = new KuraPayload();
				
				payload.setBody(json.getBytes());
				
				_cloudClient.publish(topic, payload, qos, retain);
				s_logger.info("Published to {} message: {}", topic, payload);
				
			} catch (Exception e) {
				s_logger.info("Can not publish to topic \"{}\"!", topic);
			}
			
			
			String json = _payloadConverter.toJson(mqttPayload);
			
			// Allocate a new payload
			KuraPayload payload = new KuraPayload();
			
			payload.setBody(json.getBytes());
		
			_cloudClient.publish(topic, payload, qos, retain);
			s_logger.info("Published to {} message: {}", topic, payload);
		} 
		catch (Exception e) {
			s_logger.error("Cannot publish topic: "+topic, e);
		}
	}
    
	private void manageTopics(boolean subscribe){
		// fetch the publishing configuration from the publishing properties
		String  topic  = (String) this._properties.get(CB_TOPIC_CMD_PROP_NAME);
		Integer qos    = (Integer) this._properties.get(QOS_PROP_NAME);
		
		try {
			if (subscribe == true) {
				_cloudClient.subscribe(topic, qos);
				s_logger.info("Subscribed to {}", topic);
			} else {
				_cloudClient.unsubscribe(topic);
				s_logger.info("Unsubscribed from {}", topic);
			}
		} 
		catch (Exception e) {
			if (subscribe == true) {
				s_logger.error("Cannot subscribe to topic: "+topic, e);
			} else {
				s_logger.error("Cannot unsubscribe from topic: "+topic, e);
			}
		}
	}

	@Override
	public void onControlMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {}

	@Override
	public void onMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {
		s_logger.info("Bundle " + APP_ID + " new MQTT message has arrived. Payload {}!", new String(msg.getBody()));
		
		try {
			MqttPayload payload = _payloadConverter.fromJson(new String(msg.getBody()));
			s_logger.info(APP_ID + " - new MQTT Message: - payload " + payload.toString());
			
			switch (appTopic) {
			case "LEAD1/CB1/CMD":
				s_logger.info(APP_ID + " - CB1 Command received");
				
				_sensorService.setActuatorValue("circuitBreaker", payload.getValue());
				
				break;
			default:
				s_logger.warn(APP_ID + " - Message not handled. Topic: " + appTopic);
			}
		} catch (Exception e) {
			s_logger.error(APP_ID + " - Can not process message for topic: {} Error: {}", appTopic, e.getMessage());
		}
	}

	@Override
	public void onConnectionLost() {
		s_logger.info(APP_ID + ": Connection Lost. Unsubscribing from topics");
		this.manageTopics(false);		
	}

	@Override
	public void onConnectionEstablished() {
		s_logger.info(APP_ID + ": Connection Established. Subscribing to topics");
		this.manageTopics(true);		
	}

	@Override
	public void onMessageConfirmed(int messageId, String appTopic) {}

	@Override
	public void onMessagePublished(int messageId, String appTopic) {}

	@Override
	public void sensorChanged(String sensorName, Object newValue) {
		String topic = null;
				
		if (sensorName.equals("circuitBreaker")) {
			topic = (String) _properties.get(CB_TOPIC_STATUS_PROP_NAME);
		}
		
		if (topic != null) {
			s_logger.info(APP_ID + ": sensor name :" + sensorName);
			
			MqttPayload mqttPayload = new MqttPayload(new Date(), "String", (String) newValue);
			this.publishMessage(topic, mqttPayload);
		} else {
			s_logger.warn(APP_ID + ": Unknown sensor name :" + sensorName);
		}
	}
}
