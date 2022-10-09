package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredDevice;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
@Slf4j
public class DeviceRepository implements PanacheRepository<StoredDevice>
{
	public Uni<List<StoredDevice>> getAllDevices()
	{
		return listAll();
	}

	public Uni<StoredDevice> getDeviceById(long id)
	{
		return findById(id);
	}

	public Uni<StoredDevice> findDeviceByOwnerAndName(String owner, String name)
	{
		return find("owner = ?1 and name = ?2", owner, name).singleResult();
	}

	public Uni<StoredDevice> createDevice(StoredDevice device)
	{
		log.info("Creating device {} for owner {}", device.getName(), device.getOwner());
		return persist(device);
	}

	public Uni<Long> deleteDevicesByOwner(String owner)
	{
		log.info("Deleting devices for owner {}", owner);
		return delete("owner", owner);
	}

	public Uni<List<StoredDevice>> findDevicesByOwner(String owner)
	{
		return list("owner", owner);
	}
}
