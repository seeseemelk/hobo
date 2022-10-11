package be.seeseepuff.hobo.mqtt.services;

import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyStringUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateCondition;
import be.seeseepuff.hobo.mqtt.clients.HoboClient;
import be.seeseepuff.hobo.mqtt.clients.MqttClient;
import be.seeseepuff.hobo.mqtt.dto.Property;
import be.seeseepuff.hobo.mqtt.dto.PropertyUpdate;
import be.seeseepuff.hobo.mqtt.model.*;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
@Slf4j
public class TasmotaService
{
	@Inject
	MqttClient mqtt;

	@Inject
	HoboClient hobo;

	private final Map<String, Long> topicToId = new HashMap<>();

	private final Map<Long, Context> contexts = new HashMap<>();

	public void onRequest(PropertyUpdate update)
	{
		log.info("Got property update requests for device {}", update.getDevice());
		Context context = getContext(update.getDevice().getId());
		Color color = new Color();

		List<PropertyFloatUpdateRequest> requests = new ArrayList<>();
		LocalDateTime noNewerThen = LocalDateTime.MIN;

		if (update.getFloatProperties() != null)
		{
			for (Property<Float> property : update.getFloatProperties())
			{
				boolean needsUpdate = property.getRequested() != null && !property.getRequested().equals(property.getReported());
				log.info("Testing property {} to {} (from {}) (needsUpdate={})", property.getName(), property.getRequested(), property.getReported(), needsUpdate);
				if (property.getRequestUpdated() != null && property.getRequestUpdated().isAfter(noNewerThen))
					noNewerThen = property.getRequestUpdated();

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
		}

		log.info("Testing if color needs to be changed");
		if (color.isSet())
		{
			log.info("Color needs to be changed");
			color.copyMissingFrom(context.getColor());
			context.setNewColor(color);
			context.setUpdateColor(true);
			updateContext(context);
		}
		else
			log.info("Color needs no change");

		log.info("Updating float properties");
		PropertyUpdateCondition condition = new PropertyUpdateCondition();
		condition.setNoNewerThen(noNewerThen);
		hobo.updateFloatProperties(context, requests, condition).subscribe().with(list -> {});
		log.info("Finished");
	}

	public Uni<Void> onDiscoveryData(Discovery discovery)
	{
		log.info("Discovered {}", discovery);
		return hobo.getOrCreateDevice(discovery.getMac())
			.onItem().transform(id ->
			{
				topicToId.put(discovery.getTopic(), id);
				Context context = getContext(id);
				context.setDeviceId(id);
				context.setTopic(discovery.getTopic());
				context.setMac(discovery.getMac());
				context.setIp(discovery.getIp());
				return context;
			})
			.onItem().call(
				context -> Uni.combine().all().unis(
					reportFloatProperties(context),
					reportStringProperties(context)
				).asTuple().replaceWithVoid())
			.onItem().invoke(mqtt::requestColor)
			.onFailure().retry().withBackOff(Duration.ofSeconds(3)).atMost(10)
			.replaceWithVoid();
	}

	public Uni<Void> onStateData(String topic, State state)
	{
		Optional<Context> optionalContext = getContext(topic);
		if (optionalContext.isEmpty())
		{
			log.error("No context mapping found for topic {}", topic);
			return Uni.createFrom().voidItem();
		}
		Context context = optionalContext.get();
		context.setColor(Color.fromString(state.getColor()));

		return reportFloatProperties(context);
	}

	public Uni<Void> onStatData(String device, Result result)
	{
		if (result.getColor() == null)
			return Uni.createFrom().voidItem();

		Optional<Context> contextResult = getContext(device);
		if (contextResult.isEmpty())
		{
			log.error("Could not get context for device with topic {}", device);
			return Uni.createFrom().voidItem();
		}

		Context context = contextResult.get();
		context.setWaitingForResult(false);
		updateContext(context);
		if (result.getColor() == null)
			return Uni.createFrom().voidItem();

		context.setColor(Color.fromString(result.getColor()));
		return reportFloatProperties(context);
	}

	private void updateContext(Context context)
	{
		if (context.isWaitingForResult())
		{
			log.warn("Device is already waiting for a command");
			return;
		}

		if (context.isUpdateColor())
		{
			context.setUpdateColor(false);
			context.setWaitingForResult(true);
			mqtt.sendColor(context, context.getNewColor());
		}
	}

	private Uni<Void> reportFloatProperties(Context context)
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
		PropertyUpdateCondition condition = new PropertyUpdateCondition();
		condition.setModifyUnchanged(true);
		return hobo.updateFloatProperties(context, requests, condition);
	}

	private Uni<Void> reportStringProperties(Context context)
	{
		List<PropertyStringUpdateRequest> requests = new ArrayList<>();
		if (context.getIp() != null)
			requests.add(PropertyStringUpdateRequest.withReport("ip", context.getIp()));
		PropertyUpdateCondition condition = new PropertyUpdateCondition();
		condition.setModifyUnchanged(false);
		return hobo.updateStringProperties(context, requests, condition);
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

	private Optional<Context> getContext(String topic)
	{
		return getDeviceId(topic).map(this::getContext);
	}

	private Optional<Long> getDeviceId(String topic)
	{
		return Optional.ofNullable(topicToId.get(topic));
	}
}
