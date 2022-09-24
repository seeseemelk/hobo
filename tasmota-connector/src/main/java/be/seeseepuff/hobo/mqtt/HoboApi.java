package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.PropertyName;
import be.seeseepuff.hobo.mqtt.model.IntPropertyRequest;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLClientApi(configKey = "hobo")
public interface HoboApi
{
	@Query
	Uni<DeviceId> getDeviceByOwnerAndName(String owner, String name);

	@Mutation
	Uni<DeviceId> createDevice(String owner, String name);

	@Mutation("reportIntProperty")
	Uni<PropertyName> reportIntProperty(long deviceId, String property, int value);

	@Subscription
	Multi<IntPropertyRequest> intPropertyRequestsForOwner(String owner);
}
