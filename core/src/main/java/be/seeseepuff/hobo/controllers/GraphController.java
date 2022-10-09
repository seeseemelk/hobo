package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.Property;
import be.seeseepuff.hobo.exceptions.DeviceNotFoundException;
import be.seeseepuff.hobo.exceptions.InvalidFilterException;
import be.seeseepuff.hobo.graphql.requests.*;
import be.seeseepuff.hobo.models.PropertyUpdate;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredProperty;
import be.seeseepuff.hobo.services.DeviceService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.*;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.quarkus.hibernate.reactive.panache.PanacheEntity_.id;

@GraphQLApi
public class GraphController
{
	@Inject
	DeviceService deviceService;

	@Query("devices")
	public Uni<List<? extends Device>> getAllDevices()
	{
		return deviceService.getAllDevices()
			.map(devices -> devices);
	}

	@Query
	public Uni<Device> getDevice(DeviceFilter filter)
	{
		Uni<StoredDevice> result;
		if (filter.getId() != null)
			result = deviceService.getDeviceById(filter.getId());
		else if (filter.getOwnerName() != null)
			result = deviceService.getDeviceByOwnerAndName(filter.getOwnerName().getOwner(), filter.getOwnerName().getName());
		else
			result = Uni.createFrom().failure(() -> new InvalidFilterException("Missing filter"));

		return result
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + id, ex))
			.map(device -> device);
	}

	@Mutation
	public Uni<Device> createDevice(String owner, String name)
	{
		StoredDevice device = new StoredDevice();
		device.setName(name);
		device.setOwner(owner);
		return Panache.withTransaction(() -> deviceService.createDevice(device).map(d -> d));
	}

	@Mutation("getOrCreateDevice")
	public Uni<Device> getOrCreateDevice(String owner, String name)
	{
		StoredDevice device = new StoredDevice();
		device.setName(name);
		device.setOwner(owner);
		return Panache.withTransaction(() -> deviceService.getOrCreate(device).map(d -> d));
	}

	@Mutation
	public Uni<List<Property<Integer>>> updateIntProperties(long deviceId, List<PropertyIntUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return updateProperty(deviceId, device -> deviceService.updateIntProperties(device, updates, improveCondition(condition)));
	}

	@Mutation
	public Uni<List<Property<String>>> updateStringProperties(long deviceId, List<PropertyStringUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return updateProperty(deviceId, device -> deviceService.updateStringProperties(device, updates, improveCondition(condition)));
	}

	@Mutation
	public Uni<List<Property<Float>>> updateFloatProperties(long deviceId, List<PropertyFloatUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return updateProperty(deviceId, device -> deviceService.updateFloatProperties(device, updates, improveCondition(condition)));
	}

	@Mutation
	public Uni<List<Property<Boolean>>> updateBoolProperties(long deviceId, List<PropertyBoolUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return updateProperty(deviceId, device -> deviceService.updateBoolProperties(device, updates, improveCondition(condition)));
	}

	@Subscription
	public Multi<PropertyUpdate> propertyUpdates(PropertyUpdateFilter filter)
	{
		Multi<PropertyUpdate> result = deviceService.getPropertyRequests();
		if (filter != null)
		{
			if (filter.getDeviceId() != null)
				result = result.filter(property -> property.getDevice().getId() == filter.getDeviceId());
			if (filter.getOwner() != null)
				result = result.filter(property -> filter.getOwner().equals(property.getDevice().getOwner()));
		}
		return result.onOverflow().dropPreviousItems();
	}

	public Uni<List<Property<Integer>>> intProperties(
		@Source Device device,
		@Description("Set to true if only properties that need updates should be shown") @DefaultValue("false") boolean requiringUpdate
	)
	{
		return deviceService.getIntProperties(device.getId())
			.map(list -> filterList(list, requiringUpdate));
	}

	public Uni<List<Property<Float>>> floatProperties(
		@Source Device device,
		@Description("Set to true if only properties that need updates should be shown") @DefaultValue("false") boolean requiringUpdate
	)
	{
		return deviceService.getFloatProperties(device.getId())
			.map(list -> filterList(list, requiringUpdate));
	}

	public Uni<List<Property<Boolean>>> boolProperties(
		@Source Device device,
		@Description("Set to true if only properties that need updates should be shown") @DefaultValue("false") boolean requiringUpdate
	)
	{
		return deviceService.getBoolProperties(device.getId())
			.map(list -> filterList(list, requiringUpdate));
	}

	public Uni<List<Property<String>>> stringProperties(
		@Source Device device,
		@Description("Set to true if only properties that need updates should be shown") @DefaultValue("false") boolean requiringUpdate
	)
	{
		return deviceService.getStringProperties(device.getId())
			.map(list -> filterList(list, requiringUpdate));
	}

	private <T> Uni<List<Property<T>>> updateProperty(long deviceId, Function<StoredDevice, Uni<List<StoredProperty<T>>>> updater)
	{
		return Panache.withTransaction(() -> deviceService
				.getDeviceById(deviceId)
				.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + deviceId, ex))
				.flatMap(updater::apply))
			.map(ArrayList::new);
	}

	private <T> List<Property<T>> filterList(List<? extends Property<T>> list, boolean requiringUpdate)
	{
		if (!requiringUpdate)
		{
			return new ArrayList<>(list);
		}
		else
		{
			return list.stream()
				.filter(Property::requiresUpdate)
				.collect(Collectors.toList());
		}
	}

	private PropertyUpdateCondition improveCondition(PropertyUpdateCondition condition)
	{
		if (condition == null)
			condition = new PropertyUpdateCondition();
		if (condition.getNoNewerThen() == null)
			condition.setNoNewerThen(LocalDateTime.MAX);
		return condition;
	}
}
