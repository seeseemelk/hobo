package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.graphql.requests.IntPropertyUpdateRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DeviceService
{
	@Inject
	DeviceRepository deviceRepository;

	@Inject
	IntPropertyRepository intPropertyRepository;

	@Getter
	private final BroadcastProcessor<be.seeseepuff.hobo.models.IntPropertyUpdate> intPropertyRequests = BroadcastProcessor.create();

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

	private Uni<StoredIntProperty> updateIntProperty(StoredDevice device, IntPropertyUpdateRequest update)
	{
		return intPropertyRepository.getOrCreate(device, update.getProperty())
			.invoke(property ->
			{
				if (update.getRequestedValue() != null)
					property.setRequested(update.getRequestedValue());
				if (update.getReportedValue() != null)
					property.setReported(update.getReportedValue());
			});
	}

	private void notifyChanges(List<StoredIntProperty> properties)
	{
		Map<StoredDevice, be.seeseepuff.hobo.models.IntPropertyUpdate> propertyMap = new HashMap<>();
		for (StoredIntProperty property : properties)
		{
			StoredDevice device = property.getDevice();
			if (!propertyMap.containsKey(device))
			{
				be.seeseepuff.hobo.models.IntPropertyUpdate update = new be.seeseepuff.hobo.models.IntPropertyUpdate();
				update.setDevice(device);
				update.setIntProperties(new ArrayList<>());
				propertyMap.put(device, update);
			}
			propertyMap.get(device).getIntProperties().add(property);
		}

		for (be.seeseepuff.hobo.models.IntPropertyUpdate update : propertyMap.values())
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

	public Uni<List<StoredIntProperty>> updateIntProperties(StoredDevice device, List<IntPropertyUpdateRequest> updates)
	{
		return Multi.createFrom().iterable(updates)
			.onItem().transformToUniAndMerge(update -> updateIntProperty(device, update))
			.collect().asList()
			.invoke(this::notifyChanges);
	}
}
