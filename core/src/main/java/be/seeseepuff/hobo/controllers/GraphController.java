package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.controllers.models.DeviceFilter;
import be.seeseepuff.hobo.controllers.models.IntPropertyRequest;
import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.IntProperty;
import be.seeseepuff.hobo.exceptions.DeviceNotFoundException;
import be.seeseepuff.hobo.exceptions.InvalidFilterException;
import be.seeseepuff.hobo.models.IntPropertyUpdate;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.services.DeviceService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple;
import io.smallrye.mutiny.tuples.Tuple2;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.inject.Inject;
import javax.persistence.NoResultException;
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

	@Query("device")
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

	@Mutation
	public Uni<Device> getOrCreateDevice(String owner, String name)
	{
		StoredDevice device = new StoredDevice();
		device.setName(name);
		device.setOwner(owner);
		return Panache.withTransaction(() -> deviceService.getOrCreate(device).map(d -> d));
	}

	@Mutation
	public Uni<List<StoredIntProperty>> requestIntProperty(long deviceId, List<IntPropertyRequest> requests)
	{
		return Panache.withTransaction(() -> deviceService
			.getDeviceById(deviceId)
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + deviceId, ex))
			.flatMap(device -> deviceService.requestIntProperties(device, requests)));
	}

//	@Mutation
//	public Uni<IntProperty> reportIntProperty(long deviceId, String property, int value)
//	{
//		return Panache.withTransaction(() -> deviceService
//			.getDeviceById(deviceId)
//			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + deviceId, ex))
//			.flatMap(device -> deviceService.reportIntProperty(device, property, value)));
//	}

	@Subscription
	public Multi<IntPropertyUpdate> intPropertyRequests(Long deviceId, String owner)
	{
		Multi<IntPropertyUpdate> result = deviceService.getIntPropertyRequests();
		if (deviceId != null)
			result = result.filter(property -> property.getDevice().getId() == deviceId);
		if (owner != null)
			result = result.filter(property -> owner.equals(property.getDevice().getOwner()));
		return result;
	}

	public Uni<List<? extends IntProperty>> intProperties(@Source Device device)
	{
		return deviceService.getIntProperties(device.getId())
			.map(properties -> properties);
	}
}
