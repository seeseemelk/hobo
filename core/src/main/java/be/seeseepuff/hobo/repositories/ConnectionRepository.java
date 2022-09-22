package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredConnection;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ConnectionRepository implements PanacheRepository<StoredConnection>
{
	public List<StoredConnection> getConnections()
	{
		return findAll().stream().collect(Collectors.toList());
	}

	public void addConnection(StoredConnection connection)
	{
		persist(connection);
	}
}
