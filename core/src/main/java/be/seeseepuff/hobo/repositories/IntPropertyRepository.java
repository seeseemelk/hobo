package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import java.util.List;

@ApplicationScoped
public class IntPropertyRepository implements PanacheRepository<StoredIntProperty>
{
	public Uni<StoredIntProperty> getProperty(StoredDevice device, String name)
	{
		return find("device = ?1 and name = ?2", device, name).singleResult();
	}

	public Uni<List<StoredIntProperty>> getPropertiesFor(StoredDevice device)
	{
		return find("device", device).list();
	}

	public Uni<List<StoredIntProperty>> getPropertiesFor(long device)
	{
		return find("device_id", device).list();
	}

	public Uni<StoredIntProperty> createProperty(StoredIntProperty property)
	{
		return persist(property);
	}

	public Uni<StoredIntProperty> createProperty(StoredDevice device, String name)
	{
		StoredIntProperty property = new StoredIntProperty();
		property.setDevice(device);
		property.setName(name);
		return createProperty(property);
	}

	public Uni<StoredIntProperty> getOrCreate(StoredDevice device, String name)
	{
		return getProperty(device, name)
			.onFailure(NoResultException.class).recoverWithUni(() -> createProperty(device, name));
	}
}
