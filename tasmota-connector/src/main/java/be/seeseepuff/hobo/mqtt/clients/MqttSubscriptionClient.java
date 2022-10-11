package be.seeseepuff.hobo.mqtt.clients;

import be.seeseepuff.hobo.mqtt.model.Discovery;
import be.seeseepuff.hobo.mqtt.model.Result;
import be.seeseepuff.hobo.mqtt.model.State;
import be.seeseepuff.hobo.mqtt.services.TasmotaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.ReceivingMqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Slf4j
public class MqttSubscriptionClient
{
	@Inject
	TasmotaService tasmota;

	@Inject
	ObjectMapper mapper;

	@Incoming("discovery")
	public Uni<Void> onDiscoveryData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String payload = new String(message.getPayload());
		log.info("Received topic: {}, payload: {}", message.getTopic(), payload);
		Discovery discovery = mapper.readValue(payload, Discovery.class);
		return tasmota.onDiscoveryData(discovery)
			.replaceWith(ack(message));
	}

	@Incoming("state")
	public Uni<Void> onStateData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String topic = message.getTopic().split("/")[1];
		String payload = new String(message.getPayload());
		State state = mapper.readValue(payload, State.class);
		return tasmota.onStateData(topic, state)
			.replaceWith(ack(message));
	}

	@Incoming("stat")
	public Uni<Void> onStatData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String[] topicParts = message.getTopic().split("/");
		String device = topicParts[1];
		String endpoint = topicParts[2];

		if (endpoint.equals("RESULT") || endpoint.equals("COLOR"))
		{
			String payload = new String(message.getPayload());
			Result result = mapper.readValue(payload, Result.class);
			return tasmota.onStatData(device, result)
				.replaceWith(ack(message));
		}
		return ack(message);
	}

	private <T> Uni<Void> ack(Message<T> message)
	{
		return Uni.createFrom().completionStage(message.ack());
	}
}
