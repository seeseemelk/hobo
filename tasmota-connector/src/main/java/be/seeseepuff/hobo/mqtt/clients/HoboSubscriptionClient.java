package be.seeseepuff.hobo.mqtt.clients;

import be.seeseepuff.hobo.graphql.requests.PropertyUpdateFilter;
import be.seeseepuff.hobo.mqtt.api.HoboApi;
import be.seeseepuff.hobo.mqtt.services.TasmotaService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private AtomicBoolean keepRunning = new AtomicBoolean(true);

	@SneakyThrows
	@Override
	public void start()
	{
		log.info("Subscribing to GraphQL update notifications");
//		Multi.createFrom()
//			.items(hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner))
//				.onSubscription().invoke(() -> log.info("Subscribed to changes"))
//				.onItem().invoke(tasmota::onRequest)
//				.onCompletion().invoke(() -> log.warn("Property requests completed"))
//				.subscribe().asStream())
//			.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
//			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
//			.subscribe().with(item -> {});

//		Multi.createFrom().item(() -> true)
//			.onItem().transformToMultiAndConcatenate(item -> hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner)))
//			//.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
//			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
//			.onCompletion().invoke(() -> log.warn("Property requests completed"))
//			.subscribe().with(tasmota::onRequest);

//		Multi.createFrom()
//			.<PropertyUpdate>emitter(emitter -> {
//				hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner))
//					.onSubscription().invoke(() -> log.info("Subscribed to changes"))
////					.onItem().invoke(tasmota::onRequest)
//					.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
//					.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
//					.onFailure().recoverWithMulti(hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner)))
//					.onCompletion().invoke(() -> log.warn("Property requests completed"))
//					.subscribe().with(emitter::emit);
//			})
//			.onFailure().invoke(ex -> log.error("Exception occurred while processing event", ex))
//			.onFailure().retry().withBackOff(Duration.ofSeconds(5)).indefinitely()
//			.subscribe().with(tasmota::onRequest);

		Multi.createFrom().deferred(() -> hoboApi.propertyUpdates(PropertyUpdateFilter.withOwner(owner)))
			.onSubscription().invoke(() -> log.info("Subscribed to property updates"))
			.onItem().invoke(tasmota::onRequest)
			.onCancellation().invoke(() ->
			{
				log.info("Cancelling property update subscription");
				keepRunning.set(false);
			})
			.subscribe().with(item -> {});
	}

	@Override
	public void stop() throws Exception
	{
		Log.info("Stopping property update subscription");
		keepRunning.set(false);
	}
}
