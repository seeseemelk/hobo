package be.seeseepuff.hobo.mqtt;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class GraphClient
{
	@Inject
	DynamicGraphQLClient graphQl;

	void onStart(@Observes StartupEvent event)
	{
		graphQl.subscription("")
			.onItem().
	}
}
