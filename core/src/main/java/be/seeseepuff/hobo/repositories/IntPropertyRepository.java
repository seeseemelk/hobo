package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.models.StoredDevice;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class IntPropertyRepository implements PanacheRepository<StoredIntProperty>
{
	public Optional<StoredIntProperty> getProperty(StoredDevice device, String name)
	{
		return find("device = ?1 and name = ?2", device, name).singleResultOptional();
	}

	public void createProperty(StoredIntProperty property)
	{
		persist(property);
	}

	public StoredIntProperty createProperty(StoredDevice device, String name)
	{
		StoredIntProperty property = new StoredIntProperty();
		property.setDevice(device);
		property.setName(name);
		createProperty(property);
		return property;
	}

	public StoredIntProperty getOrCreate(StoredDevice device, String name)
	{
		return getProperty(device, name).orElseGet(() -> createProperty(device, name));
	}
}
