package be.seeseepuff.hobo.mqtt.services;

import be.seeseepuff.hobo.mqtt.clients.HoboSubscriptionClient;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class StartupService
{
	public void onStartup(@Observes StartupEvent event, Vertx vertx, HoboSubscriptionClient graphClient)
	{
		vertx.deployVerticle(graphClient).await().indefinitely();
	}
}
