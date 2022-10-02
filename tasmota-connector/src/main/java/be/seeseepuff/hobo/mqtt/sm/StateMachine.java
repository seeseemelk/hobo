package be.seeseepuff.hobo.mqtt.sm;

import be.seeseepuff.hobo.mqtt.sm.events.DiscoverEvent;
import be.seeseepuff.hobo.mqtt.sm.events.StateEvent;
import io.smallrye.mutiny.Uni;

public interface StateMachine
{
	default Uni<StateMachine> onEvent(DiscoverEvent event)
	{
		return Uni.createFrom().item(this);
	}

	default Uni<StateMachine> onEvent(StateEvent event)
	{
		return Uni.createFrom().item(this);
	}
}
