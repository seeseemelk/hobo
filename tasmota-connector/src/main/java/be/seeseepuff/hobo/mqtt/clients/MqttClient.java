package be.seeseepuff.hobo.mqtt.clients;

import be.seeseepuff.hobo.mqtt.model.Color;
import be.seeseepuff.hobo.mqtt.model.Context;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Slf4j
public class MqttClient
{
	@Broadcast
	@Channel("cmnd")
	Emitter<String> mqttEmitter;

	public void requestColor(Context context)
	{
		log.info("Requesting color of {}", context.getTopic());
		String topic = String.format("cmnd/%s/Color", context.getTopic());
		mqttEmitter.send(MqttMessage.of(topic, ""));
	}

	public void sendColor(Context context, Color color)
	{
		String rgbw = color.toString();
		log.info("Setting color of {} to {}", context.getTopic(), rgbw);
		String topic = String.format("cmnd/%s/Color", context.getTopic());
		mqttEmitter.send(MqttMessage.of(topic, rgbw));
	}
}
