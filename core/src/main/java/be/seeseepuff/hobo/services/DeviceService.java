package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.dto.Property;
import be.seeseepuff.hobo.exceptions.DeviceExistsException;
import be.seeseepuff.hobo.graphql.requests.*;
import be.seeseepuff.hobo.models.*;
import be.seeseepuff.hobo.repositories.*;
import be.seeseepuff.hobo.utils.TimeUtils;
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

	private <T, P extends StoredProperty<T>> P enrichNewProperty(P property, PropertyUpdateRequest<T> request, StoredDevice device)
	{
		property.setName(request.getProperty());
		property.setDevice(device);
		return property;
	}

	private <T, P extends StoredProperty<T>> P updateProperty(P newProperty, P oldProperty, PropertyUpdateRequest<T> request)
	{
		LocalDateTime now = LocalDateTime.now();
		newProperty.setDevice(oldProperty.getDevice());
		newProperty.setName(oldProperty.getName());

		if (request.getRequest() != null)
		{
			newProperty.setRequested(request.getRequest());
			newProperty.setRequestUpdated(now);
		}
		else if (request.isUnset())
		{
			newProperty.setRequested(null);
			newProperty.setRequestUpdated(now);
		}
		else
		{
			newProperty.setRequested(oldProperty.getRequested());
			newProperty.setRequestUpdated(oldProperty.getRequestUpdated());
		}

		if (request.getReport() != null)
		{
			newProperty.setReported(request.getReport());
			newProperty.setReportUpdated(now);
		}
		else if (request.isUnset())
		{
			newProperty.setReported(null);
			newProperty.setReportUpdated(now);
		}
		else
		{
			newProperty.setReported(oldProperty.getReported());
			newProperty.setReportUpdated(oldProperty.getReportUpdated());
		}
		return newProperty;
	}

	private Multi<StoredProperty<Integer>> updateIntProperty(StoredDevice device, PropertyUpdateRequest<Integer> update, PropertyUpdateCondition condition)
	{
		return updateProperty(device, update, condition, intPropertyRepository, StoredIntProperty::new);
	}

	private Multi<StoredProperty<String>> updateStringProperty(StoredDevice device, PropertyUpdateRequest<String> update, PropertyUpdateCondition condition)
	{
		return updateProperty(device, update, condition, stringPropertyRepository, StoredStringProperty::new);
	}

	private Multi<StoredProperty<Float>> updateFloatProperty(StoredDevice device, PropertyUpdateRequest<Float> update, PropertyUpdateCondition condition)
	{
		return updateProperty(device, update, condition, floatPropertyRepository, StoredFloatProperty::new);
	}

	private Multi<StoredProperty<Boolean>> updateBoolProperty(StoredDevice device, PropertyUpdateRequest<Boolean> update, PropertyUpdateCondition condition)
	{
		return updateProperty(device, update, condition, boolPropertyRepository, StoredBoolProperty::new);
	}

	private <T, P extends StoredProperty<T>> Multi<StoredProperty<T>> updateProperty(
		StoredDevice device,
		PropertyUpdateRequest<T> update,
		PropertyUpdateCondition condition,
		PropertyRepository<P> repository,
		Supplier<P> producer
	)
	{

		PropertyUpdateCondition improvedCondition = improveCondition(condition);

		return repository.getProperty(device, update.getProperty())
			.replaceIfNullWith(() -> enrichNewProperty(producer.get(), update, device))
			.toMulti()
			.concatMap(property -> updateProperty(property, device, update, producer, improvedCondition))
			.onItem().transformToUniAndMerge(repository::insertProperty);
	}

	private <T, P extends StoredProperty<T>> Multi<P> updateProperty(
		P oldProperty,
		StoredDevice device,
		PropertyUpdateRequest<T> update,
		Supplier<P> propertySupplier,
		PropertyUpdateCondition condition
	)
	{
		if (oldProperty == null)
		{
			oldProperty = enrichNewProperty(propertySupplier.get(), update, device);
			return Multi.createFrom().item(updateProperty(propertySupplier.get(), oldProperty, update));
		}

		boolean updatesRequest = update.isUnset() || (update.getRequest() != null);
		boolean updatesReport = update.isUnset() || (update.getReport() != null);

		if (condition.getNoNewerThen() != null)
		{
			if (updatesRequest && TimeUtils.isAfter(condition.getNoNewerThen(), oldProperty.getRequestUpdated()))
				return empty();
			else if (updatesReport && TimeUtils.isAfter(condition.getNoNewerThen(), oldProperty.getReportUpdated()))
				return empty();
		}

		if (!condition.getModifyUnchanged())
		{
			boolean reportedEqual = Objects.equals(oldProperty.getReported(), update.getReport());
			boolean requestedEqual = Objects.equals(oldProperty.getRequested(), update.getRequest());

			log.info("Unset: {}, reportedEqual: {}, requestedEqual: {}", update.isUnset(), reportedEqual, requestedEqual);
			if (update.isUnset() && reportedEqual && requestedEqual)
			{
				log.info("Property needs no change");
				return empty();
			}

			boolean reportedNull = update.getReport() == null;
			boolean requestedNull = update.getRequest() == null;
			log.info("reportedNull: {}, requestedNull: {}", reportedNull, requestedNull);
			if (!update.isUnset() && !((!reportedNull && !reportedEqual) || (!requestedNull && !requestedEqual)))
			{
				log.info("Property needs no change");
				return empty();
			}
		}

		return Multi.createFrom().item(updateProperty(propertySupplier.get(), oldProperty, update));
	}

	public Uni<List<StoredProperty<Integer>>> updateIntProperties(StoredDevice device, List<PropertyIntUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.flatMap(update -> updateIntProperty(device, update, condition))
			.collect().asList()
			.invoke(this::notifyIntChanges);
	}

	public Uni<List<StoredProperty<String>>> updateStringProperties(StoredDevice device, List<PropertyStringUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.flatMap(update -> updateStringProperty(device, update, condition))
			.collect().asList()
			.invoke(this::notifyStringChanges);
	}

	public Uni<List<StoredProperty<Float>>> updateFloatProperties(StoredDevice device, List<PropertyFloatUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.flatMap(update -> updateFloatProperty(device, update, condition))
			.collect().asList()
			.invoke(this::notifyFloatChanges);
	}

	public Uni<List<StoredProperty<Boolean>>> updateBoolProperties(StoredDevice device, List<PropertyBoolUpdateRequest> updates, PropertyUpdateCondition condition)
	{
		return Multi.createFrom().iterable(updates)
			.map(PropertyUpdateRequest::from)
			.flatMap(update -> updateBoolProperty(device, update, condition))
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

	public Uni<StoredDevice> getOrCreate(StoredDevice device)
	{
		return deviceRepository.findDeviceByOwnerAndName(device.getOwner(), device.getName())
			.onFailure(NoResultException.class).recoverWithUni(() -> createDevice(device));
	}

	public Uni<Long> deleteDevicesByOwner(String owner)
	{
		return deviceRepository.deleteDevicesByOwner(owner);
	}

	private PropertyUpdateCondition improveCondition(PropertyUpdateCondition condition)
	{
		if (condition == null)
			condition = new PropertyUpdateCondition();
		if (condition.getModifyUnchanged() == null)
			condition.setModifyUnchanged(Boolean.FALSE);
		return condition;
	}

	private static <T> Multi<T> empty()
	{
		return Multi.createFrom().empty();
	}
}
