package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredDevice;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

import java.util.List;

public abstract class PropertyRepository<T> implements PanacheRepository<T>
{
	private final String queryGetProperty;
	private final String queryGetProperties;

	public PropertyRepository(String entityName)
	{
		queryGetProperty = String.format("from %s where device = ?1 and name = ?2 order by name, timestamp desc", entityName);
		queryGetProperties = String.format("from %s p1 where device_id = ?1 and p1.timestamp = (select max(timestamp) from %s p2 where p2.device = p1.device and p2.name = p1.name)", entityName, entityName);
	}

	public Uni<T> getProperty(StoredDevice device, String name)
	{
		//return find("#{#entityName}.getProperty", device, name).firstResult();
		return find(queryGetProperty, device, name).firstResult();
	}

	public Uni<List<T>> getPropertiesFor(StoredDevice device)
	{
		return getPropertiesFor(device.getId());
	}

	public Uni<List<T>> getPropertiesFor(long device)
	{
		//return find("#{#entityName}.getProperties", device).list();
		return list(queryGetProperties, device);
	}

	public Uni<T> insertProperty(T property)
	{
		return persistAndFlush(property);
	}
}
