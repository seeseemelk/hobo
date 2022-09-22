package be.seeseepuff.hobo.connectors;

import be.seeseepuff.hobo.models.StoredConnection;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DevelcoConnector implements Connector
{
	@Override
	public String getName()
	{
		return "develco";
	}

	@Override
	public Connection start(StoredConnection connection)
	{
		return new DevelcoConnection(connection);
	}

	@RequiredArgsConstructor
	private class DevelcoConnection implements Connection
	{
		private final StoredConnection connection;
	}
}
