package be.seeseepuff.hobo.connectors;

import be.seeseepuff.hobo.models.StoredConnection;

public interface Connector
{
	String getName();
	Connection start(StoredConnection connection);
}
