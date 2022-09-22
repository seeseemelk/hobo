package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.connectors.Connection;
import be.seeseepuff.hobo.connectors.Connector;
import be.seeseepuff.hobo.models.StoredConnection;
import be.seeseepuff.hobo.repositories.ConnectionRepository;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class ConnectionService
{
	@Inject
	ConnectionRepository repository;
	@Inject
	Instance<Connector> connectors;

	private final Map<StoredConnection, Connection> liveConnections = new HashMap<>();

	public boolean isStarted(StoredConnection connection)
	{
		synchronized (liveConnections)
		{
			return liveConnections.containsKey(connection);
		}
	}

	private void markAsStarted(StoredConnection storedConnection, Connection connection)
	{
		synchronized (liveConnections)
		{
			liveConnections.put(storedConnection, connection);
		}
	}

	public void startConnection(StoredConnection connection)
	{
		if (isStarted(connection))
		{
			log.info("Connection {} has already been started", connection.getName());
			return;
		}

		connectors.stream()
			.filter(connector -> connector.getName().equals(connection.getName()))
			.findAny()
			.ifPresentOrElse(connector ->
			{
				log.info("Starting connection {}...", connection.getName());
				markAsStarted(connection, connector.start(connection));
			}, () ->
			{
				log.warn("Could not find a connector for connection {} of type {}", connection.getName(), connection.getType());
			});
	}

	public void addConnection(StoredConnection connection)
	{
		repository.addConnection(connection);
		startConnection(connection);
	}

//	void onStart(@Observes StartupEvent event)
//	{
//		log.info("Starting connections...");
//		repository.getConnections().forEach(this::startConnection);
//		log.info("Started all connections");
//	}
}
