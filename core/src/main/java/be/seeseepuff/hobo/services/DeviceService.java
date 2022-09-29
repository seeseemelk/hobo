package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.controllers.models.IntPropertyRequest;
import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.models.IntPropertyUpdate;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@ApplicationScoped
public class DeviceService
{
	@Inject
	DeviceRepository deviceRepository;

	@Inject
	IntPropertyRepository intPropertyRepository;

	@Getter
	private final BroadcastProcessor<IntPropertyUpdate> intPropertyRequests = BroadcastProcessor.create();

	public Uni<StoredDevice> createDevice(StoredDevice device)
	{
		return deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName())
			.onItem().failWith(result -> new DeviceExistsException("Device with owner " + device.getOwner() + " and name " + device.getName() + " already exists"))
			.onFailure(NoResultException.class).recoverWithUni(() -> deviceRepository.createDevice(device));
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

	public Uni<StoredIntProperty> getIntProperty(StoredDevice device, String propertyName)
	{
		return intPropertyRepository.getProperty(device, propertyName);
	}

	private Uni<StoredIntProperty> requestIntProperty(StoredDevice device, String propertyName, int requestedValue)
	{
		return intPropertyRepository.getOrCreate(device, propertyName)
			.invoke(property -> property.setRequested(requestedValue));
	}
//
//	public Uni<StoredIntProperty> reportIntProperty(StoredDevice device, String propertyName, int reportedValue)
//	{
//		return intPropertyRepository.getOrCreate(device, propertyName)
//			.invoke(property -> property.setReported(reportedValue))
//			.invoke(this::notifyChange);
//	}

	private void notifyChanges(List<StoredIntProperty> properties)
	{
		Map<StoredDevice, IntPropertyUpdate> propertyMap = new HashMap<>();
		for (StoredIntProperty property : properties)
		{
			StoredDevice device = property.getDevice();
			if (!propertyMap.containsKey(device))
			{
				IntPropertyUpdate update = new IntPropertyUpdate();
				update.setDevice(device);
				update.setIntProperties(new ArrayList<>());
				propertyMap.put(device, update);
			}
			propertyMap.get(device).getIntProperties().add(property);
		}

		for (IntPropertyUpdate update : propertyMap.values())
			intPropertyRequests.onNext(update);
	}

	public Uni<List<StoredDevice>> getDevicesByOwner(String owner)
	{
		return deviceRepository.findDevicesByOwner(owner);
	}

	public Uni<List<StoredIntProperty>> getIntProperties(long device)
	{
		return intPropertyRepository.getPropertiesFor(device);
	}

	public Multi<StoredIntProperty> getIntPropertiesRequiringUpdate(String owner)
	{
		return getDevicesByOwner(owner)
			.onItem().<StoredDevice>disjoint()
			.onItem().transformToUniAndMerge(device -> intPropertyRepository.getPropertiesFor(device))
			.onItem().<StoredIntProperty>disjoint()
			.filter(StoredIntProperty::requiresUpdate);
	}

	public Uni<StoredDevice> getOrCreate(StoredDevice device)
	{
		return deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName())
			.onFailure(NoResultException.class).recoverWithUni(() -> createDevice(device));
	}

	public Uni<List<StoredIntProperty>> requestIntProperties(StoredDevice device, List<IntPropertyRequest> requests)
	{
		return Multi.createFrom().iterable(requests)
			.onItem().transformToUniAndMerge(request -> requestIntProperty(device, request.getProperty(), request.getValue()))
			.collect().asList()
			.invoke(properties -> notifyChanges(properties));
	}
}
