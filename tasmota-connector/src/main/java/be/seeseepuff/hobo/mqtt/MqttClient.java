package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.Property;
import be.seeseepuff.hobo.mqtt.dto.PropertyUpdate;
import be.seeseepuff.hobo.mqtt.model.Discovery;
import be.seeseepuff.hobo.mqtt.model.Result;
import be.seeseepuff.hobo.mqtt.model.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
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
		subscribeToChanges();
	}

	private void subscribeToChanges()
	{
		hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner))
			.onSubscription().invoke(() -> log.info("Subscribed to changes"))
			.onItem().invoke(this::onRequest)
			.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
			.onCompletion().invoke(() -> log.warn("Property requests completed"))
			.onCompletion().invoke(this::subscribeToChanges)
			.subscribe().with(item -> {});
	}

	private void onRequest(PropertyUpdate update)
	{
		log.info("Got property update requests for device {}", update.getDevice());
		Context context = getContext(update.getDevice().getId());
		Color color = new Color();

		List<PropertyFloatUpdateRequest> requests = new ArrayList<>();

		for (Property<Float> property : update.getFloatProperties())
		{
			boolean needsUpdate = property.getRequested() != null && !property.getRequested().equals(property.getReported());
			log.info("Testing property {} to {} (from {}) (needsUpdate={})", property.getName(), property.getRequested(), property.getReported(), needsUpdate);
			switch (property.getName())
			{
			case "red":
				if (needsUpdate)
				{
					color.setRed(property.getRequested());
					requests.add(PropertyFloatUpdateRequest.withRequest("red", color.getRed()));
				}
				break;
			case "green":
				if (needsUpdate)
				{
					color.setGreen(property.getRequested());
					requests.add(PropertyFloatUpdateRequest.withRequest("green", color.getGreen()));
				}
				break;
			case "blue":
				if (needsUpdate)
				{
					color.setBlue(property.getRequested());
					requests.add(PropertyFloatUpdateRequest.withRequest("blue", color.getBlue()));
				}
				break;
			case "white":
				if (needsUpdate)
				{
					color.setWhite(property.getRequested());
					requests.add(PropertyFloatUpdateRequest.withRequest("white", color.getWhite()));
				}
				break;
			}
		}

		log.info("Testing if color needs to be changed");
		if (color.isSet())
		{
			log.info("Color needs to be changed");
			color.copyMissingFrom(context.getColor());

			String rgbw = color.toString();
			log.info("Setting color of {} to {}", context.getTopic(), rgbw);
			String topic = String.format("cmnd/%s/Color", context.getTopic());
			mqttEmitter.send(MqttMessage.of(topic, rgbw));
		}
		else
			log.info("Color needs no change");

		log.info("Updating float properties");
		updateFloatProperties(context, requests).subscribe().with(list -> {});
		log.info("Finished");
	}

	@Incoming("discovery")
	public Uni<Void> onDiscoveryData(ReceivingMqttMessage message) throws JsonProcessingException
	{
		String payload = new String(message.getPayload());
		log.info("Received topic: {}, payload: {}", message.getTopic(), payload);
		Discovery discovery = mapper.readValue(payload, Discovery.class);
		log.info("Discovered {}", discovery);
		return getOrCreateDevice(discovery.getTopic())
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
		context.setColor(Color.fromString(state.getColor()));

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
			if (result.getColor() != null)
			{
				return getContext(device)
					.chain(context ->
					{
						context.setColor(Color.fromString(result.getColor()));
						return reportProperties(context);
					})
					.chain(() -> ack(message));
			}
		}
		return ack(message);
	}

	private Uni<Long> getOrCreateDevice(String topic)
	{
		return hoboApi.getOrCreateDevice(owner, topic)
			.map(DeviceId::getId);
	}

	private Uni<Void> reportProperties(Context context)
	{
		List<PropertyFloatUpdateRequest> requests = new ArrayList<>();
		Color color = context.getColor();
		if (color.getRed() != null)
			requests.add(PropertyFloatUpdateRequest.withReport("red", color.getRed()));
		if (color.getGreen() != null)
			requests.add(PropertyFloatUpdateRequest.withReport("green", color.getGreen()));
		if (color.getBlue() != null)
			requests.add(PropertyFloatUpdateRequest.withReport("blue", color.getBlue()));
		if (color.getWhite() != null)
			requests.add(PropertyFloatUpdateRequest.withReport("white", color.getWhite()));

		return updateFloatProperties(context, requests);
	}

	private Uni<Void> updateFloatProperties(Context context, List<PropertyFloatUpdateRequest> requests)
	{
		if (requests.isEmpty())
			return Uni.createFrom().voidItem();
		else
		{
			String propertyNames = requests.stream()
				.map(PropertyFloatUpdateRequest::getProperty)
				.collect(Collectors.joining(", "));
			log.info("Updating properties {} for device {}", propertyNames, context.getTopic());
			return hoboApi.updateFloatProperties(context.getDeviceId(), requests).replaceWithVoid();
		}
	}

	private void requestColor(Context context)
	{
		log.info("Requesting color of {}", context.getTopic());
		String topic = String.format("cmnd/%s/Color", context.getTopic());
		mqttEmitter.send(MqttMessage.of(topic, ""));
	}

	private Uni<Long> getDeviceId(String topic)
	{
		return Uni.createFrom().item(topicToId.get(topic))
			.onItem().ifNull().switchTo(() -> getOrCreateDevice(topic));
	}

	private Context getContext(long deviceId)
	{
		if (!contexts.containsKey(deviceId))
		{
			Context context = new Context();
			context.setDeviceId(deviceId);
			contexts.put(deviceId, context);
		}
		return contexts.get(deviceId);
	}

	private Uni<Context> getContext(String topic)
	{
		return getDeviceId(topic).map(this::getContext);
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
