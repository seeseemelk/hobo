package be.seeseepuff.hobo.mqtt.sm;

import be.seeseepuff.hobo.mqtt.sm.events.DiscoverEvent;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class InitialState implements StateMachine
{
	private final Context context = new Context();

	@Override
	public Uni<StateMachine> onEvent(DiscoverEvent event)
	{
		log.info("Discover event");
		StateMachine newState = new IdleState(context);
		return newState.onEvent(event);
	}
}
