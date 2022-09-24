package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredDevice;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DeviceRepository implements PanacheRepository<StoredDevice>
{
	public List<StoredDevice> getAllDevices()
	{
		return findAll().list();
	}

	public Optional<StoredDevice> getDeviceById(long id)
	{
		return findByIdOptional(id);
	}

	public Optional<StoredDevice> findDeviceByOwnerAndName(String owner, String name)
	{
		return find("owner = ?1 and name = ?2", owner, name).singleResultOptional();
	}

	public void createDevice(StoredDevice device)
	{
		persist(device);
	}
}
