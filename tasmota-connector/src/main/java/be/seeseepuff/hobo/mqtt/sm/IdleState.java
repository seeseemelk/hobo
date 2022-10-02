package be.seeseepuff.hobo.mqtt.sm;

import be.seeseepuff.hobo.mqtt.sm.events.DiscoverEvent;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@RequiredArgsConstructor
@Slf4j
public class IdleState implements StateMachine
{
	private final Context context;

	@Override
	public Uni<StateMachine> onEvent(DiscoverEvent event)
	{
		return StateMachine.super.onEvent(event);
	}
}
