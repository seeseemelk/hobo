package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Input;

import java.time.LocalDateTime;

@Data
@Input
public class PropertyUpdateCondition
{
	@Description("Set to true if an unchanged property should still have the value be saved.")
	private Boolean modifyUnchanged;

	@Description("When set no property updated at a later timestamp then this will be modified.")
	private LocalDateTime noNewerThen;
}
