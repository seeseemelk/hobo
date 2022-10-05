package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.dto.Property;
import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.graphql.requests.PropertyBoolUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyIntUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyStringUpdateRequest;
import be.seeseepuff.hobo.models.*;
import be.seeseepuff.hobo.repositories.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ApplicationScoped
@Slf4j
public class DeviceService
{
	@Inject
	DeviceRepository deviceRepository;

	@Inject
	IntPropertyRepository intPropertyRepository;

	@Inject
	FloatPropertyRepository floatPropertyRepository;

	@Inject
	StringPropertyRepository stringPropertyRepository;

	@Inject
	BoolPropertyRepository boolPropertyRepository;

	@Getter
	private final BroadcastProcessor<PropertyUpdate> propertyRequests = BroadcastProcessor.create();

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

	private <T, P extends StoredProperty<T>> P enrichNewProperty(P property, PropertyUpdateRequest<T> request, StoredDevice device)
	{
		property.setName(request.getProperty());
		property.setDevice(device);
		return property;
	}

	private <T, P extends StoredProperty<T>> P updateProperty(P newProperty, P oldProperty, PropertyUpdateRequest<T> request)
	{
		newProperty.setDevice(oldProperty.getDevice());
		newProperty.setName(oldProperty.getName());
		newProperty.setTimestamp(LocalDateTime.now());

		if (request.getRequest() != null)
			newProperty.setRequested(request.getRequest());
		else if (request.isUnset())
			newProperty.setRequested(null);
		else
			newProperty.setRequested(oldProperty.getRequested());

		if (request.getReport() != null)
			newProperty.setReported(request.getReport());
		else if (request.isUnset())
			newProperty.setReported(null);
		else
			newProperty.setReported(oldProperty.getReported());
		return newProperty;
	}

	private <T, P extends StoredProperty<T>> Multi<P> updateProperty(
		P oldProperty,
		StoredDevice device,
		PropertyUpdateRequest<T> update,
		Supplier<P> propertySupplier
	)
	{
		if (oldProperty == null)
			oldProperty = enrichNewProperty(propertySupplier.get(), update, device);
		else
		{
			boolean reportedEqual = Objects.equals(oldProperty.getReported(), update.getReport());
			boolean requestedEqual = Objects.equals(oldProperty.getRequested(), update.getRequest());
			log.info("Unset: {}, reportedEqual: {}, requestedEqual: {}", update.isUnset(), reportedEqual, requestedEqual);
			if (update.isUnset() && reportedEqual && requestedEqual)
			{
				log.info("Property needs no change");
				return Multi.createFrom().empty();
			}

			boolean reportedNull = update.getReport() == null;
			boolean requestedNull = update.getRequest() == null;
			log.info("reportedNull: {}, requestedNull: {}", reportedNull, requestedNull);
			if (!update.isUnset() && !((!reportedNull && !reportedEqual) || (!requestedNull && !requestedEqual)))
			{
				log.info("Property needs no change");
				return Multi.createFrom().empty();
			}
		}

		return Multi.createFrom().item(updateProperty(propertySupplier.get(), oldProperty, update));
	}

	private Uni<StoredProperty<Integer>> updateIntProperty(StoredDevice device, PropertyUpdateRequest<Integer> update)
	{
		return intPropertyRepository.getProperty(device, update.getProperty())
			.replaceIfNullWith(() -> enrichNewProperty(new StoredIntProperty(), update, device))
			.flatMap(property -> intPropertyRepository.insertProperty(updateProperty(new StoredIntProperty(), property, update)));
	}

	private Uni<StoredProperty<String>> updateStringProperty(StoredDevice device, PropertyUpdateRequest<String> update)
	{
		return stringPropertyRepository.getProperty(device, update.getProperty())
			.replaceIfNullWith(() -> enrichNewProperty(new StoredStringProperty(), update, device))
			.flatMap(property -> stringPropertyRepository.insertProperty(updateProperty(new StoredStringProperty(), property, update)));
	}

	private Multi<StoredProperty<Float>> updateFloatProperty(StoredDevice device, PropertyUpdateRequest<Float> update)
	{
		return floatPropertyRepository.getProperty(device, update.getProperty())
			.onItem().transformToMulti(property -> updateProperty(property, device, update, StoredFloatProperty::new))
			.onItem().transformToUniAndMerge(property -> floatPropertyRepository.insertProperty(property));
	}

	private Uni<StoredProperty<Boolean>> updateBoolProperty(StoredDevice device, PropertyUpdateRequest<Boolean> update)
	{
		return boolPropertyRepository.getProperty(device, update.getProperty())
			.replaceIfNullWith(() -> enrichNewProperty(new StoredBoolProperty(), update, device))
			.flatMap(property -> boolPropertyRepository.insertProperty(updateProperty(new StoredBoolProperty(), property, update)));
	}

	public Uni<List<StoredProperty<Integer>>> updateIntProperties(StoredDevice device, List<PropertyIntUpdateRequest> updates)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.onItem().transformToUniAndMerge(update -> updateIntProperty(device, update))
			.collect().asList()
			.invoke(this::notifyIntChanges);
	}

	public Uni<List<StoredProperty<String>>> updateStringProperties(StoredDevice device, List<PropertyStringUpdateRequest> updates)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.onItem().transformToUniAndMerge(update -> updateStringProperty(device, update))
			.collect().asList()
			.invoke(this::notifyStringChanges);
	}

	public Uni<List<StoredProperty<Float>>> updateFloatProperties(StoredDevice device, List<PropertyFloatUpdateRequest> updates)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.onItem().transformToMultiAndMerge(update -> updateFloatProperty(device, update))
			.collect().asList()
			.invoke(this::notifyFloatChanges);
	}

	public Uni<List<StoredProperty<Boolean>>> updateBoolProperties(StoredDevice device, List<PropertyBoolUpdateRequest> updates)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.onItem().transformToUniAndMerge(update -> updateBoolProperty(device, update))
			.collect().asList()
			.invoke(this::notifyBoolChanges);
	}

	private <T, P extends StoredProperty<T>> void notifyChanges(
		List<P> properties,
		BiConsumer<PropertyUpdate, List<Property<T>>> setList,
		Function<PropertyUpdate, List<Property<T>>> getList
	)
	{
		Map<StoredDevice, PropertyUpdate> propertyMap = new HashMap<>();
		for (StoredProperty<T> property : properties)
		{
			StoredDevice device = property.getDevice();
			if (!propertyMap.containsKey(device))
			{
				PropertyUpdate update = new PropertyUpdate();
				update.setDevice(device);
				setList.accept(update, new ArrayList<>());
				propertyMap.put(device, update);
			}
			getList.apply(propertyMap.get(device)).add(property);
		}

		for (PropertyUpdate update : propertyMap.values())
			propertyRequests.onNext(update);
	}

	private void notifyIntChanges(List<StoredProperty<Integer>> properties)
	{
		notifyChanges(properties, PropertyUpdate::setIntProperties, PropertyUpdate::getIntProperties);
	}

	private void notifyStringChanges(List<StoredProperty<String>> properties)
	{
		notifyChanges(properties, PropertyUpdate::setStringProperties, PropertyUpdate::getStringProperties);
	}

	private void notifyFloatChanges(List<StoredProperty<Float>> properties)
	{
		notifyChanges(properties, PropertyUpdate::setFloatProperties, PropertyUpdate::getFloatProperties);
	}

	private void notifyBoolChanges(List<StoredProperty<Boolean>> properties)
	{
		notifyChanges(properties, PropertyUpdate::setBoolProperties, PropertyUpdate::getBoolProperties);
	}

	public Uni<List<StoredDevice>> getDevicesByOwner(String owner)
	{
		return deviceRepository.findDevicesByOwner(owner);
	}

	public Uni<List<StoredIntProperty>> getIntProperties(long device)
	{
		return intPropertyRepository.getPropertiesFor(device);
	}

	public Uni<List<StoredFloatProperty>> getFloatProperties(long device)
	{
		return floatPropertyRepository.getPropertiesFor(device);
	}

	public Uni<List<StoredBoolProperty>> getBoolProperties(long device)
	{
		return boolPropertyRepository.getPropertiesFor(device);
	}

	public Uni<List<StoredStringProperty>> getStringProperties(long device)
	{
		return stringPropertyRepository.getPropertiesFor(device);
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
}
