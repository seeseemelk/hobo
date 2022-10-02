package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;

@Data
public class PropertyUpdateFilter
{
	private Long deviceId;
	private String owner;

	public static PropertyUpdateFilter withDevice(Long deviceId)
	{
		PropertyUpdateFilter filter = new PropertyUpdateFilter();
		filter.setDeviceId(deviceId);
		return filter;
	}

	public static PropertyUpdateFilter withOwner(String owner)
	{
		PropertyUpdateFilter filter = new PropertyUpdateFilter();
		filter.setOwner(owner);
		return filter;
	}
}
