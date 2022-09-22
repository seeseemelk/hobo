package be.seeseepuff.hobo.connectors;

import be.seeseepuff.hobo.models.StoredConnection;
import io.vertx.core.Verticle;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MqttTasmotaConnector implements Connector
{
	@Inject
	Verticle verticle;

	@Override
	public String getName()
	{
		return "mqtt_tasmota";
	}

	@Override
	public Connection start(StoredConnection storedConnection)
	{
		MqttTasmotaConnection connection = new MqttTasmotaConnection(storedConnection);
		connection.start();
		return connection;
	}

	@RequiredArgsConstructor
	private class MqttTasmotaConnection implements Connection
	{
		private final StoredConnection connection;

		private void start()
		{
		}
	}
}
