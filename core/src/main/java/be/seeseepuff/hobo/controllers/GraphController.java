package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.IntProperty;
import be.seeseepuff.hobo.exceptions.DeviceNotFoundException;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.services.DeviceService;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.List;

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

	@Query("getDevice")
	public Uni<Device> getDevice(long id)
	{
		return deviceService.getDeviceById(id)
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + id, ex))
			.map(device -> device);
	}

	@Query
	public Uni<Device> getDeviceByOwnerAndName(String owner, String name)
	{
		return deviceService.getDeviceByOwnerAndName(owner, name)
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with owner " + owner + " and name " + name, ex))
			.map(device -> device);
	}

	@Query("getIntProperty")
	public Uni<IntProperty> getIntProperty(long deviceId, String propertyName)
	{
		return deviceService.getDeviceById(deviceId)
			.flatMap(device -> deviceService.getIntProperty(device, propertyName))
			.map(property -> property);
	}

	@Mutation
	public Uni<Device> createDevice(String owner, String name)
	{
		StoredDevice device = new StoredDevice();
		device.setName(name);
		device.setOwner(owner);
		return Panache.withTransaction(() -> deviceService.createDevice(device).map(d -> d));
	}

//	@Mutation
//	@Transactional
//	public Optional<? extends Device> markOnline(long id)
//	{
//		return deviceService.markDeviceOnline(id);
//	}
//
//	@Mutation
//	@Transactional
//	public Optional<? extends Device> markOffline(long id)
//	{
//		return deviceService.markDeviceOffline(id);
//	}

	@Mutation
	public Uni<IntProperty> requestedIntProperty(long deviceId, String property, int value)
	{
		return Panache.withTransaction(() -> deviceService
			.getDeviceById(deviceId)
			.flatMap(device -> deviceService.requestIntProperty(device, property, value)));
	}

	@Mutation
	public Uni<IntProperty> reportIntProperty(long deviceId, String property, int value)
	{
		return Panache.withTransaction(() -> deviceService
			.getDeviceById(deviceId)
			.onFailure(NoResultException.class).transform(ex -> new DeviceNotFoundException("Could not find a device with id " + deviceId, ex))
			.flatMap(device -> deviceService.reportIntProperty(device, property, value)));
//		StoredDevice device = deviceService.getDeviceById(deviceId)
//			.orElseThrow(() -> new DeviceNotFoundException("Could not find a device with id " + deviceId));
//		StoredIntProperty storedIntProperty = deviceService.reportIntProperty(device, property, value);
//		return storedIntProperty;
	}

	@Subscription
	public Multi<? extends IntProperty> intPropertyRequests()
	{
		return deviceService.getIntPropertyRequests();
	}

	@Subscription
	public Multi<? extends IntProperty> intPropertyRequestsForDevice(long deviceId)
	{
		return deviceService.getIntPropertyRequests()
			.filter(property -> property.getDevice().getId() == deviceId);
	}

	@Subscription
//	@ActivateRequestContext
	public Multi<? extends IntProperty> intPropertyRequestsForOwner(String owner)
	{
		return deviceService.getIntPropertyRequests().filter(property -> owner.equals(property.getDevice().getOwner()));
//		return deviceService.getIntPropertiesRequiringUpdate(owner)
//			.onCompletion().switchTo(deviceService.getIntPropertyRequests()
//				.filter(property -> owner.equals(property.getDevice().getOwner())));
//		Stream<StoredIntProperty> properties = deviceService.getIntPropertiesRequiringUpdate(owner);
//		return Multi.createBy().concatenating().streams(
////			deviceService.getIntPropertiesRequiringUpdate(owner),
//			deviceService.getIntPropertyRequests().filter(property -> owner.equals(property.getDevice().getOwner()))
//		);
	}

	public Uni<List<? extends IntProperty>> intProperties(@Source Device device)
	{
		return deviceService.getIntProperties(device.getId())
			.map(properties -> properties);
	}
}
