package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;

import java.time.LocalDateTime;

@Data
public class PropertyUpdateCondition
{
	@Description("When set no property updated at a later timestamp then this will be modified.")
	private LocalDateTime noNewerThen;
}
