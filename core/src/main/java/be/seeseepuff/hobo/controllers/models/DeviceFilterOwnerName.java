package be.seeseepuff.hobo.controllers.models;

import io.smallrye.common.constraint.NotNull;
import lombok.Data;

@Data
public class DeviceFilterOwnerName
{
	@NotNull
	private String owner;
	@NotNull
	private String name;
}
