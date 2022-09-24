package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.model.Discovery;
import be.seeseepuff.hobo.mqtt.model.IntPropertyRequest;
import be.seeseepuff.hobo.mqtt.model.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.ReceivingMqttMessage;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.net.ConnectException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Slf4j
public class MqttClient
{
	@Inject
	HoboApi hoboApi;

	@Inject
	ObjectMapper mapper;

	@ConfigProperty(name = "hobo.owner")
	String owner;

	private final Map<String, Long> topicToId = new HashMap<>();

	public Multi<Void> onStartup(@Observes StartupEvent e, Vertx vertx)
	{
		vertx.executeBlockingAndAwait()
		return hoboApi.intPropertyRequestsForOwner(owner)
			.onItem().call(this::onRequest)
			.onItem().ignore();
	}

	private Uni<Void> onRequest(IntPropertyRequest request)
	{
		log.info("Got property request for {}", request.getName());
		return Uni.createFrom().voidItem();
	}

	@Incoming("discovery")
	public CompletionStage<Void> onDiscoveryData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String payload = new String(message.getPayload());
		log.info("Received topic: {}, payload: {}", message.getTopic(), payload);
		Discovery discovery = mapper.readValue(payload, Discovery.class);
		log.info("Discovered {}", discovery);
		return getOrCreateDevice(discovery.getMac())
			.map(DeviceId::getId)
			.onItem().call(id -> reportProperties(id, discovery))
			.onItem().invoke(id -> topicToId.put(discovery.getTopic(), id))
			.onItem().transformToUni(id -> ack(message))
			.onFailure(ConnectException.class).retry().withBackOff(Duration.ofSeconds(3)).atMost(10)
			.subscribeAsCompletionStage();
	}

	@Incoming("state")
	public CompletionStage<Void> onStateData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String topic = message.getTopic().split("/")[1];
		long id = Objects.requireNonNull(topicToId.get(topic), () -> "No id found for topic " + topic);
		String payload = new String(message.getPayload());
		State state = mapper.readValue(payload, State.class);
		return Uni.combine().all().unis(
			reportProperty(id, "red", state.getRed()),
			reportProperty(id, "blue", state.getBlue()),
			reportProperty(id, "green", state.getGreen()),
			reportProperty(id, "white", state.getWhite())
		)
			.discardItems()
			.subscribeAsCompletionStage();
	}

	private Uni<DeviceId> getOrCreateDevice(String mac)
	{
		return hoboApi.getDeviceByOwnerAndName(owner, mac)
			.onFailure().recoverWithUni(() -> hoboApi.createDevice(owner, mac));
	}

	private Uni<Void> reportProperties(long deviceId, Discovery discovery)
	{
		return reportProperty(deviceId, "protocol_version", discovery.getProtocolVersion())
			.onFailure(ConnectException.class).retry().withBackOff(Duration.ofSeconds(3)).atMost(10);
	}

	private Uni<Void> reportProperty(long deviceId, String property, int value)
	{
		log.info("Reporting property {} with value {} for device {}", property, value, deviceId);
		return hoboApi.reportIntProperty(deviceId, property, value).replaceWithVoid()
			.onFailure(ConnectException.class).retry().withBackOff(Duration.ofSeconds(3)).atMost(10);
	}

	private <T> Uni<Void> ack(Message<T> message)
	{
		return Uni.createFrom().completionStage(message.ack());
	}



//	@Inject
//	DynamicGraphQLClient graphQl;
//
//	void onStart(@Observes StartupEvent event)
//	{
//		graphQl.subscription("")
//			.onItem().
//	}
}
