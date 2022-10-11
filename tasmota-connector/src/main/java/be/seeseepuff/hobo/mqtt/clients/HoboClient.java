package be.seeseepuff.hobo.mqtt.clients;

import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyStringUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateCondition;
import be.seeseepuff.hobo.mqtt.api.HoboApi;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.model.Context;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class HoboClient
{
	@Inject
	HoboApi hoboApi;

	@ConfigProperty(name = "hobo.owner")
	String owner;

	public Uni<Long> getOrCreateDevice(String mac)
	{
		return hoboApi.getOrCreateDevice(owner, mac)
			.map(DeviceId::getId);
	}

	public Uni<Void> updateFloatProperties(Context context, List<PropertyFloatUpdateRequest> requests, PropertyUpdateCondition condition)
	{
		if (requests.isEmpty())
			return Uni.createFrom().voidItem();
		else
		{
			String propertyNames = requests.stream()
				.map(PropertyFloatUpdateRequest::getProperty)
				.collect(Collectors.joining(", "));
			log.info("Updating properties {} for device {}", propertyNames, context.getTopic());
			return hoboApi.updateFloatProperties(context.getDeviceId(), requests, condition).replaceWithVoid();
		}
	}

	public Uni<Void> updateStringProperties(Context context, List<PropertyStringUpdateRequest> requests, PropertyUpdateCondition condition)
	{
		if (requests.isEmpty())
			return Uni.createFrom().voidItem();
		else
		{
			String propertyNames = requests.stream()
				.map(PropertyStringUpdateRequest::getProperty)
				.collect(Collectors.joining(", "));
			log.info("Updating properties {} for device {}", propertyNames, context.getTopic());
			return hoboApi.updateStringProperties(context.getDeviceId(), requests, condition).replaceWithVoid();
		}
	}
}
