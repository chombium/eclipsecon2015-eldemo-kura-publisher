package com.wordpress.chombium.eldemo.publisher;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wordpress.chombium.eldemo.sensors.SensorChangedListener;
import com.wordpress.chombium.eldemo.sensors.SensorService;

public class ElDemoPublisher implements ConfigurableComponent, SensorChangedListener, CloudClientListener {
    private static final Logger s_logger = LoggerFactory.getLogger(ElDemoPublisher.class);
    private static final String APP_ID = "Eclipsecon2015-eldemo-publisher";
    private Map<String, Object> properties;
    
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
		_cloudService = cloudService;
	}

	public void unsetCloudService(CloudService cloudService) {
		_cloudService = null;
	}
	
    protected void activate(ComponentContext componentContext) {
        s_logger.info("Bundle " + APP_ID + " has started!");
    }
    
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
    	s_logger.info("Activating " + APP_ID);
    	
        updated(properties);
        
		// get the mqtt client for this application
		try  {
			
			// Acquire a Cloud Application Client for this Application 
			s_logger.info("Getting CloudClient for {}...", APP_ID);
			_cloudClient = _cloudService.newCloudClient(APP_ID);
			_cloudClient.addCloudClientListener(this);
			
			// Don't subscribe because these are handled by the default 
			// subscriptions and we don't want to get messages twice			
			//this.doUpdate(false);
		}
		catch (Exception e) {
			s_logger.error("Error during component activation", e);
			throw new ComponentException(e);
		}
        s_logger.info("Bundle " + APP_ID + " has started with config!");
    }
    
    protected void deactivate(ComponentContext componentContext) {
    	
 		s_logger.info("Releasing CloudApplicationClient for {}...", APP_ID);
 		_cloudClient.release();
        s_logger.info("Bundle " + APP_ID + " has stopped!");
    }
    
    public void updated(Map<String, Object> properties) {
        this.properties = properties;
        if(properties != null && !properties.isEmpty()) {
            Iterator<Entry<String, Object>> it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Object> entry = it.next();
                s_logger.info("New property - " + entry.getKey() + " = " +
                entry.getValue() + " of type " + entry.getValue().getClass().toString());
            }
        }
    }

	@Override
	public void onControlMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {
		s_logger.info("Bundle " + APP_ID + " new MQTT message has arrived. Payload {}!", new String(msg.getBody()));
	}

	@Override
	public void onConnectionLost() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionEstablished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageConfirmed(int messageId, String appTopic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessagePublished(int messageId, String appTopic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sensorChanged(String sensorName, Object newValue) {
		// TODO Auto-generated method stub
		
	}
}
