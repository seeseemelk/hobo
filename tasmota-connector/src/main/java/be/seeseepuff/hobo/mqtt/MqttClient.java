package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.graphql.requests.IntPropertyUpdateRequest;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.IntProperty;
import be.seeseepuff.hobo.mqtt.dto.IntPropertyName;
import be.seeseepuff.hobo.mqtt.dto.IntPropertyUpdate;
import be.seeseepuff.hobo.mqtt.model.Discovery;
import be.seeseepuff.hobo.mqtt.model.State;
import be.seeseepuff.hobo.mqtt.sm.InitialState;
import be.seeseepuff.hobo.mqtt.sm.StateMachine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import io.smallrye.reactive.messaging.mqtt.ReceivingMqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.ConnectException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@ApplicationScoped
@Slf4j
public class MqttClient
{
	@Inject
	HoboApi hoboApi;

	@Inject
	@GraphQLClient("hobo")
	DynamicGraphQLClient dynamicHoboApi;

	@Inject
	ObjectMapper mapper;

	@ConfigProperty(name = "hobo.owner")
	String owner;

	@Broadcast
	@Channel("state")
	Emitter<String> mqttEmitter;

	private final Map<String, Long> topicToId = new HashMap<>();

	private final Map<Long, StateMachine> stateMachines = new HashMap<>();

//	public void onStartup(@Observes StartupEvent e)
//	{
//		hoboApi.intPropertyUpdates(PropertyUpdateFilter.withOwner(owner))
//			.onCompletion().invoke(() -> log.warn("Property requests completed"))
//			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
//			.onItem().call(this::onRequest)
//			.subscribe().with(item -> {});
//			//.subscribe().asStream()
//			//.forEach(this::onRequest);
//	}

	private Uni<Void> onRequest(IntPropertyUpdate update)
	{
		log.info("Got property update requests for device {}", update.getDevice());
		for (IntProperty property : update.getIntProperties())
		{
			log.info("Updating property {}", property.getName());
		}
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
		return hoboApi.getOrCreateDevice(owner, mac);
	}

	private Uni<Void> reportProperties(long deviceId, Discovery discovery)
	{
		return reportProperty(deviceId, "protocol_version", discovery.getProtocolVersion())
			.onFailure(ConnectException.class).retry().withBackOff(Duration.ofSeconds(3)).atMost(10)
			.replaceWithVoid();
	}

	private Uni<List<IntPropertyName>> reportProperty(long deviceId, String property, int value)
	{
		log.info("Reporting property {} with value {} for device {}", property, value, deviceId);
		return hoboApi.updateIntProperty(deviceId, IntPropertyUpdateRequest.withRequest(property, value));
	}

	private void requestColor(long deviceId)
	{
		mqttEmitter.send(MqttMessage.of("cmnd/(topic)/Color", ""));
	}

	private void executeStateMachine(long deviceId, Function<StateMachine, StateMachine> update)
	{
		StateMachine sm = getStateMachine(deviceId);
		sm = update.apply(sm);
		stateMachines.put(deviceId, sm);
	}

	private StateMachine getStateMachine(long deviceId)
	{
		if (!stateMachines.containsKey(deviceId))
			stateMachines.put(deviceId, new InitialState());
		return stateMachines.get(deviceId);
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
