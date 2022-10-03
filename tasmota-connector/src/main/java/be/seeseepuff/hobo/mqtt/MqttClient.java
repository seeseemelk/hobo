package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateRequest;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.IntProperty;
import be.seeseepuff.hobo.mqtt.dto.IntPropertyUpdate;
import be.seeseepuff.hobo.mqtt.model.Discovery;
import be.seeseepuff.hobo.mqtt.model.Result;
import be.seeseepuff.hobo.mqtt.model.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.CheckReturnValue;
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
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.net.ConnectException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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

	@Broadcast
	@Channel("cmnd")
	Emitter<String> mqttEmitter;

	private final Map<String, Long> topicToId = new HashMap<>();

	private final Map<Long, Context> contexts = new HashMap<>();

	public void onStartup(@Observes StartupEvent e)
	{
		hoboApi.intPropertyUpdates(PropertyUpdateFilter.withOwner(owner))
			.onCompletion().invoke(() -> log.warn("Property requests completed"))
			.onItem().call(this::onRequest)
			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
			.subscribe().with(item -> {});
			//.subscribe().asStream()
			//.forEach(this::onRequest);
	}

	private Uni<Void> onRequest(IntPropertyUpdate update)
	{
		log.info("Got property update requests for device {}", update.getDevice());
		Context context = getContext(update.getDevice().getId());
		List<PropertyUpdateRequest<Integer>> requests = new ArrayList<>();
		Integer red = null;
		Integer green = null;
		Integer blue = null;
		Integer white = null;
		for (IntProperty property : update.getIntProperties())
		{
			boolean needsUpdate = property.getRequested() != null && !property.getRequested().equals(property.getReported());
			switch (property.getName())
			{
			case "red":
				red = needsUpdate ? property.getRequested() : null;
				break;
			case "green":
				green = needsUpdate ? property.getRequested() : null;
				break;
			case "blue":
				blue = needsUpdate ? property.getRequested() : null;
				break;
			case "white":
				white = needsUpdate ? property.getRequested() : null;
				break;
			}
			log.info("Updating property {}", property.getName());
		}

		if (red != null || green != null || blue != null || white != null)
		{
			red = Objects.requireNonNullElseGet(red, context::getRed);
			green = Objects.requireNonNullElseGet(green, context::getGreen);
			blue = Objects.requireNonNullElseGet(blue, context::getBlue);
			white = Objects.requireNonNullElseGet(white, context::getWhite);

			String color = convertColor(red, green, blue, white);
			log.info("Setting color of {} to {}", context.getTopic(), color);
			String topic = String.format("cmnd/%s/Color", context.getTopic());
			mqttEmitter.send(MqttMessage.of(topic, color));
		}
		return updateProperties(context, requests);
	}

	@Incoming("discovery")
	public Uni<Void> onDiscoveryData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String payload = new String(message.getPayload());
		log.info("Received topic: {}, payload: {}", message.getTopic(), payload);
		Discovery discovery = mapper.readValue(payload, Discovery.class);
		log.info("Discovered {}", discovery);
		return getOrCreateDevice(discovery.getMac())
			.map(DeviceId::getId)
			.onItem().transform(id ->
			{
				topicToId.put(discovery.getTopic(), id);
				Context context = getContext(id);
				context.setDeviceId(id);
				context.setTopic(discovery.getTopic());
				return context;
			})
			.onItem().call(this::reportProperties)
			.onItem().invoke(this::requestColor)
			.onItem().transformToUni(id -> ack(message))
			.onFailure(ConnectException.class).retry().withBackOff(Duration.ofSeconds(3)).atMost(10);
	}

	@Incoming("state")
	public Uni<Void> onStateData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String topic = message.getTopic().split("/")[1];
		long id = Objects.requireNonNull(topicToId.get(topic), () -> "No id found for topic " + topic);
		String payload = new String(message.getPayload());
		State state = mapper.readValue(payload, State.class);

		Context context = getContext(id);
		context.setRed(state.getRed());
		context.setGreen(state.getGreen());
		context.setBlue(state.getBlue());
		context.setWhite(state.getWhite());

		return reportProperties(context).replaceWith(ack(message));
	}

	@Incoming("stat")
	public Uni<Void> onStatData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String[] topicParts = message.getTopic().split("/");
		String device = topicParts[1];
		String endpoint = topicParts[2];
		if (endpoint.equals("RESULT"))
		{
			String payload = new String(message.getPayload());
			Result result = mapper.readValue(payload, Result.class);
			Context context = getContext(device);
			if (result.getColor() != null)
			{
				context.setRed(result.getRed());
				context.setGreen(result.getGreen());
				context.setBlue(result.getBlue());
				context.setWhite(result.getWhite());
				return reportProperties(context)
					.replaceWith(ack(message));
			}
		}
		return ack(message);
	}

	private Uni<DeviceId> getOrCreateDevice(String mac)
	{
		return hoboApi.getOrCreateDevice(owner, mac);
	}

	private Uni<Void> reportProperties(Context context)
	{
		List<PropertyUpdateRequest<Integer>> requests = new ArrayList<>();
		if (context.getRed() != null)
			requests.add(PropertyUpdateRequest.withReport("red", context.getRed()));
		if (context.getGreen() != null)
			requests.add(PropertyUpdateRequest.withReport("green", context.getGreen()));
		if (context.getBlue() != null)
			requests.add(PropertyUpdateRequest.withReport("blue", context.getBlue()));
		if (context.getWhite() != null)
			requests.add(PropertyUpdateRequest.withReport("white", context.getWhite()));

		return updateProperties(context, requests);
	}

	private Uni<Void> updateProperties(Context context, List<PropertyUpdateRequest<Integer>> requests)
	{
		if (requests.isEmpty())
			return Uni.createFrom().voidItem();
		else
		{
			String propertyNames = requests.stream()
				.map(PropertyUpdateRequest::getProperty)
				.collect(Collectors.joining(", "));
			log.info("Updating properties {} for device {}", propertyNames, context.getTopic());
			return hoboApi.updateIntProperty(context.getDeviceId(), requests).replaceWithVoid();
		}
	}

	private void requestColor(Context context)
	{
		log.info("Requesting color of {}", context.getTopic());
		String topic = String.format("cmnd/%s/Color", context.getTopic());
		mqttEmitter.send(MqttMessage.of(topic, ""));
	}

	private long getDeviceId(String topic)
	{
		return Objects.requireNonNull(topicToId.get(topic), () -> "No id found for topic " + topic);
	}

	@CheckReturnValue
	private Context getContext(long deviceId)
	{
		if (!contexts.containsKey(deviceId))
			contexts.put(deviceId, new Context());
		return contexts.get(deviceId);
	}

	private Context getContext(String topic)
	{
		return getContext(getDeviceId(topic));
	}

	private <T> Uni<Void> ack(Message<T> message)
	{
		return Uni.createFrom().completionStage(message.ack());
	}

	private String convertColor(int red, int green, int blue, int white)
	{
		return String.format("%02X%02X%02X%02X", red, green, blue, white);
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
