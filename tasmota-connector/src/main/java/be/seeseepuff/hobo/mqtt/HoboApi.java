package be.seeseepuff.hobo.mqtt;

import be.seeseepuff.hobo.graphql.requests.DeviceFilter;
import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyIntUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import be.seeseepuff.hobo.mqtt.dto.PropertyName;
import be.seeseepuff.hobo.mqtt.dto.PropertyUpdate;
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
	Uni<List<PropertyName>> updateIntProperties(long deviceId, List<PropertyIntUpdateRequest> updates);

	@Mutation
	Uni<List<PropertyName>> updateFloatProperties(long deviceId, List<PropertyFloatUpdateRequest> updates);

	@Subscription
	Multi<PropertyUpdate> propertyUpdates(PropertyUpdateFilter filter);
}
