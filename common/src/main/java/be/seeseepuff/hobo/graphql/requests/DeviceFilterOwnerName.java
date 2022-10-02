package be.seeseepuff.hobo.graphql.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.graphql.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceFilterOwnerName
{
	@NonNull
	private String owner;
	@NonNull
	private String name;
}
