package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.graphql.requests.DeviceFilter;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.IntPropertyName;
import be.seeseepuff.hobo.mqtt.dto.IntPropertyUpdate;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLClientApi(configKey = "hobo")
public interface HoboApi
{
	@Query
	Uni<DeviceId> getDevice(DeviceFilter filter);

	@Mutation("getOrCreateDevice")
	Uni<DeviceId> getOrCreateDevice(String owner, String name);

	@Mutation
	Uni<List<IntPropertyName>> updateIntProperty(long deviceId, List<PropertyUpdateRequest<Integer>> updates);

	@Subscription
	Multi<IntPropertyUpdate> intPropertyUpdates(PropertyUpdateFilter filter);
}
