package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.repositories.DeviceRepository;
import be.seeseepuff.hobo.repositories.IntPropertyRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.util.List;

@ApplicationScoped
public class DeviceService
{
	@Inject
	DeviceRepository deviceRepository;

	@Inject
	IntPropertyRepository intPropertyRepository;

	@Getter
	private final BroadcastProcessor<StoredIntProperty> intPropertyRequests = BroadcastProcessor.create();

	public Uni<StoredDevice> createDevice(StoredDevice device)
	{
		return deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName())
			.onItem().failWith(result -> new DeviceExistsException("Device with owner " + device.getOwner() + " and name " + device.getName() + " already exists"))
			.onFailure(NoResultException.class).recoverWithUni(() -> deviceRepository.createDevice(device));
//		if (deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName()).isPresent())
//			throw new DeviceExistsException("Device with owner " + device.getOwner() + " and name " + device.getName() + " already exists");
//		deviceRepository.createDevice(device);
	}

	public Uni<List<StoredDevice>> getAllDevices()
	{
		return deviceRepository.getAllDevices();
	}

	public Uni<StoredDevice> getDeviceById(long id)
	{
		return deviceRepository.getDeviceById(id);
	}

	public Uni<StoredDevice> getDeviceByOwnerAndName(String owner, String name)
	{
		return deviceRepository.findDeviceByOwnerAndName(owner, name);
	}

//	public StoredDevice markDeviceOnline(StoredDevice device)
//	{
//		device.setOnline(true);
//		device.getIntProperties().forEach(this::notifyChange);
//		return device;
//	}
//
//	public StoredDevice markDeviceOffline(StoredDevice device)
//	{
//		device.setOnline(false);
//		return device;
//	}

//	public Uni<StoredDevice> markDeviceOnline(long id)
//	{
//		return getDeviceById(id).map(this::markDeviceOnline);
//	}
//
//	public Optional<StoredDevice> markDeviceOffline(long id)
//	{
//		return getDeviceById(id).map(this::markDeviceOffline);
//	}

	public Uni<StoredIntProperty> getIntProperty(StoredDevice device, String propertyName)
	{
		return intPropertyRepository.getProperty(device, propertyName);
	}

	public Uni<StoredIntProperty> requestIntProperty(StoredDevice device, String propertyName, int requestedValue)
	{
//		StoredIntProperty property = intPropertyRepository.getOrCreate(device, propertyName);
//		property.setRequested(requestedValue);
//		notifyChange(property);
//		return property;
		return intPropertyRepository.getOrCreate(device, propertyName)
			.invoke(property -> property.setRequested(requestedValue))
			.invoke(this::notifyChange);
	}

	public Uni<StoredIntProperty> reportIntProperty(StoredDevice device, String propertyName, int reportedValue)
	{
//		StoredIntProperty property = intPropertyRepository.getOrCreate(device, propertyName);
//		property.setReported(reportedValue);
//		notifyChange(property);
//		return property;
		return intPropertyRepository.getOrCreate(device, propertyName)
			.invoke(property -> property.setReported(reportedValue))
			.invoke(this::notifyChange);
	}

	private void notifyChange(StoredIntProperty property)
	{
		if (property.requiresUpdate())
		{
			intPropertyRequests.onNext(property);
		}
	}

	public Uni<List<StoredDevice>> getDevicesByOwner(String owner)
	{
		return deviceRepository.findDevicesByOwner(owner);
	}

	public Uni<List<StoredIntProperty>> getIntProperties(long device)
	{
		return intPropertyRepository.getPropertiesFor(device);
	}

//	@Transactional
//	@ActivateRequestContext
	public Multi<StoredIntProperty> getIntPropertiesRequiringUpdate(String owner)
	{
		return getDevicesByOwner(owner)
			.onItem().<StoredDevice>disjoint()
//			.onItem().transform(device -> intPropertyRepository.getPropertiesFor(device))
			.onItem().transformToUniAndMerge(device -> intPropertyRepository.getPropertiesFor(device))
			.onItem().<StoredIntProperty>disjoint()
//				.onItem().transformToMulti(properties -> Multi.createFrom().items(properties.stream())))
//			.onItem().transformToIterable(StoredDevice::getIntProperties)
			.filter(StoredIntProperty::requiresUpdate);
//		return getDevicesByOwner(owner).stream()
//			.flatMap(device -> device.getIntProperties().stream())
//			.filter(StoredIntProperty::requiresUpdate);
//		return Multi.createFrom().items(() -> getDevicesByOwner(owner).stream())
//			//.emitOn(Infrastructure.getDefaultWorkerPool())
//			.onItem().transformToIterable(StoredDevice::getIntProperties)
//			.filter(StoredIntProperty::requiresUpdate);
	}
}
