package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.IntProperty;
import be.seeseepuff.hobo.exceptions.DeviceNotFoundException;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.services.DeviceService;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@GraphQLApi
public class GraphController
{
	@Inject
	DeviceService deviceService;

	@Query("devices")
	public List<? extends Device> getAllDevices()
	{
		return deviceService.getAllDevices();
	}

	@Query("getDevice")
	public Device getDevice(long id)
	{
		return deviceService.getDeviceById(id)
			.orElseThrow(() -> new DeviceNotFoundException("Could not find a device with id " + id));
	}

	@Query
	public Device getDeviceByOwnerAndName(String owner, String name)
	{
		return deviceService.getDeviceByOwnerAndName(owner, name)
			.orElseThrow(() -> new DeviceNotFoundException("Could not find a device with owner " + owner + " and name " + name));
	}

	@Query("getIntProperty")
	public Optional<? extends IntProperty> getIntProperty(long deviceId, String propertyName)
	{
		return deviceService.getDeviceById(deviceId).flatMap(device -> deviceService.getIntProperty(device, propertyName));
	}

	@Mutation
	@Transactional
	public Device createDevice(String owner, String name)
	{
		StoredDevice device = new StoredDevice();
		device.setName(name);
		device.setOwner(owner);
		deviceService.createDevice(device);
		return device;
	}

	@Mutation
	@Transactional
	public Optional<? extends Device> markOnline(long id)
	{
		return deviceService.markDeviceOnline(id);
	}

	@Mutation
	@Transactional
	public Optional<? extends Device> markOffline(long id)
	{
		return deviceService.markDeviceOffline(id);
	}

	@Mutation
	@Transactional
	public Optional<? extends IntProperty> requestedIntProperty(long deviceId, String property, int value)
	{
		return deviceService.getDeviceById(deviceId)
			.map(device -> deviceService.updateIntProperty(device, property, value));
	}

	@Mutation
	@Transactional
	public IntProperty reportIntProperty(long deviceId, String property, int value)
	{
		StoredDevice device = deviceService.getDeviceById(deviceId)
			.orElseThrow(() -> new DeviceNotFoundException("Could not find a device with id " + deviceId));
		StoredIntProperty storedIntProperty = deviceService.reportIntProperty(device, property, value);
		return storedIntProperty;
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
	public Multi<? extends IntProperty> intPropertyRequestsForOwner(String owner)
	{
		return deviceService.getIntPropertyRequests()
			.filter(property -> owner.equals(property.getDevice().getOwner()));
	}
}
