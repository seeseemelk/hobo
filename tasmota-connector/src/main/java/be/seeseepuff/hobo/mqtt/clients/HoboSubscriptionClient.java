package be.seeseepuff.hobo.mqtt.clients;

import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.mqtt.api.HoboApi;
import be.seeseepuff.hobo.mqtt.services.TasmotaService;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;

@ApplicationScoped
@Slf4j
public class HoboSubscriptionClient extends AbstractVerticle
{
	@Inject
	HoboApi hoboApi;

	@Inject
	TasmotaService tasmota;

	@ConfigProperty(name = "hobo.owner")
	String owner;

	@Override
	public void start()
	{
			log.info("Subscribing to GraphQL update notifications");
			hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner))
				.onSubscription().invoke(() -> log.info("Subscribed to changes"))
				.onItem().invoke(tasmota::onRequest)
				.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
				.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
				.onCompletion().invoke(() -> log.warn("Property requests completed"))
				.subscribe().with(item -> {});
	}
}
