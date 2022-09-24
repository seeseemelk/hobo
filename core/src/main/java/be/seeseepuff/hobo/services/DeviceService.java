package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.repositories.DeviceRepository;
import be.seeseepuff.hobo.repositories.IntPropertyRepository;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DeviceService
{
	@Inject
	DeviceRepository deviceRepository;

	@Inject
	IntPropertyRepository intPropertyRepository;

	@Getter
	private final BroadcastProcessor<StoredIntProperty> intPropertyRequests = BroadcastProcessor.create();

	public void createDevice(StoredDevice device)
	{
		if (deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName()).isPresent())
			throw new DeviceExistsException("Device with owner " + device.getOwner() + " and name " + device.getName() + " already exists");
		deviceRepository.createDevice(device);
	}

	public List<StoredDevice> getAllDevices()
	{
		return deviceRepository.getAllDevices();
	}

	public Optional<StoredDevice> getDeviceById(long id)
	{
		return deviceRepository.getDeviceById(id);
	}

	public Optional<StoredDevice> getDeviceByOwnerAndName(String owner, String name)
	{
		return deviceRepository.findDeviceByOwnerAndName(owner, name);
	}

	public StoredDevice markDeviceOnline(StoredDevice device)
	{
		device.setOnline(true);
		device.getIntProperties().forEach(this::notifyChange);
		return device;
	}

	public StoredDevice markDeviceOffline(StoredDevice device)
	{
		device.setOnline(false);
		return device;
	}

	public Optional<StoredDevice> markDeviceOnline(long id)
	{
		return getDeviceById(id).map(this::markDeviceOnline);
	}

	public Optional<StoredDevice> markDeviceOffline(long id)
	{
		return getDeviceById(id).map(this::markDeviceOffline);
	}

	public Optional<StoredIntProperty> getIntProperty(StoredDevice device, String propertyName)
	{
		return intPropertyRepository.getProperty(device, propertyName);
	}

	public StoredIntProperty updateIntProperty(StoredDevice device, String propertyName, int requestedValue)
	{
		StoredIntProperty property = intPropertyRepository.getOrCreate(device, propertyName);
		property.setRequested(requestedValue);
		notifyChange(property);
		return property;
	}

	public StoredIntProperty reportIntProperty(StoredDevice device, String propertyName, int reportedValue)
	{
		StoredIntProperty property = intPropertyRepository.getOrCreate(device, propertyName);
		property.setReported(reportedValue);
		notifyChange(property);
		return property;
	}

	private void notifyChange(StoredIntProperty property)
	{
		if (property.getRequested() != property.getReported())
		{
			intPropertyRequests.onNext(property);
		}
	}
}
