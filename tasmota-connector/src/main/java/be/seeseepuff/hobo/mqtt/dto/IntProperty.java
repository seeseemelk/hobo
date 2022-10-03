package be.seeseepuff.hobo.mqtt.dto;

import be.seeseepuff.hobo.graphql.requests.PropertyUpdateRequest;
import lombok.Data;

@Data
public class IntProperty
{
	private String name;
	private Integer requested;
	private Integer reported;

	public PropertyUpdateRequest<Integer> unsetRequested()
	{
		PropertyUpdateRequest request = new PropertyUpdateRequest();
		request.setProperty(name);
		request.setUnset(true);
		request.setReportedValue(reported);
		return request;
	}

	public PropertyUpdateRequest unsetReported()
	{
		PropertyUpdateRequest request = new PropertyUpdateRequest();
		request.setProperty(name);
		request.setUnset(true);
		request.setRequestedValue(requested);
		return request;
	}
}
