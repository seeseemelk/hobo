package be.seeseepuff.hobo.mqtt;

import io.smallrye.common.constraint.Nullable;
import lombok.Data;

@Data
public class Context
{
	private long deviceId;
	private String topic;
	@Nullable
	private Integer red;
	@Nullable
	private Integer green;
	@Nullable
	private Integer blue;
	@Nullable
	private Integer white;
}
