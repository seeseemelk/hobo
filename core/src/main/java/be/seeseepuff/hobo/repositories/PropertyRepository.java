package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredProperty;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class PropertyRepository<P extends StoredProperty<?>> implements PanacheRepository<P>
{
	private final String queryGetProperty;
	private final String queryGetProperties;

	public PropertyRepository(String entityName)
	{
		queryGetProperty = String.format("from %s where device = ?1 and name = ?2 order by name, lastUpdated desc", entityName);
		queryGetProperties = String.format("from %s p1 where device_id = ?1 and lastUpdated = (select max(lastUpdated) from %s p2 where p2.device = p1.device and p2.name = p1.name)", entityName, entityName);
	}

	public Uni<P> getProperty(StoredDevice device, String name)
	{
		return find(queryGetProperty, device, name).firstResult();
	}

	public Uni<List<P>> getPropertiesFor(long device)
	{
		return list(queryGetProperties, device);
	}

	public Uni<P> insertProperty(P property)
	{
		property.setLastUpdated(null);
		log.info("Persisting property: {}", property);
		return persistAndFlush(property);
	}
}
