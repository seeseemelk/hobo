package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;

@Data
public class DeviceFilter
{
	private Long id;
	private DeviceFilterOwnerName ownerName;

	public static DeviceFilter withId(Long id)
	{
		DeviceFilter filter = new DeviceFilter();
		filter.setId(id);
		return filter;
	}

	public static DeviceFilter withOwnerAndName(String owner, String name)
	{
		DeviceFilter filter = new DeviceFilter();
		filter.setOwnerName(new DeviceFilterOwnerName(owner, name));
		return filter;
	}
}
