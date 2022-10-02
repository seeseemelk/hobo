package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.IntProperty;
import be.seeseepuff.hobo.exceptions.DeviceNotFoundException;
import be.seeseepuff.hobo.exceptions.InvalidFilterException;
import be.seeseepuff.hobo.graphql.requests.DeviceFilter;
import be.seeseepuff.hobo.graphql.requests.IntPropertyUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.models.IntPropertyUpdate;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.services.DeviceService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.*;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

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
	public Uni<List<IntProperty>> updateIntProperty(long deviceId, List<IntPropertyUpdateRequest> updates)
	{
		return Panache.withTransaction(() -> deviceService
			.getDeviceById(deviceId)
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + deviceId, ex))
			.flatMap(device -> deviceService.updateIntProperties(device, updates)))
			.map(ArrayList::new);
	}

	@Subscription
	public Multi<IntPropertyUpdate> intPropertyUpdates(PropertyUpdateFilter filter)
	{
		Multi<be.seeseepuff.hobo.models.IntPropertyUpdate> result = deviceService.getIntPropertyRequests();
		if (filter != null)
		{
			if (filter.getDeviceId() != null)
				result = result.filter(property -> property.getDevice().getId() == filter.getDeviceId());
			if (filter.getOwner() != null)
				result = result.filter(property -> filter.getOwner().equals(property.getDevice().getOwner()));
		}
		return result;
	}

	public Uni<List<IntProperty>> intProperties(
		@Source Device device,
		@Description("Set to true if only properties that need updates should be shown") @DefaultValue("false") boolean requiringUpdate
	)
	{
		return deviceService.getIntProperties(device.getId())
			.map(ArrayList::new);
	}
}
