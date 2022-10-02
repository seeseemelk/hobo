package be.seeseepuff.hobo.mqtt.dto;

import be.seeseepuff.hobo.graphql.requests.IntPropertyUpdateRequest;
import lombok.Data;

@Data
public class IntProperty
{
	private String name;
	private Integer requested;
	private Integer reported;

	public IntPropertyUpdateRequest unsetRequested()
	{
		IntPropertyUpdateRequest request = new IntPropertyUpdateRequest();
		request.setProperty(name);
		request.setUnset(true);
		request.setReportedValue(reported);
		return request;
	}

	public IntPropertyUpdateRequest unsetReported()
	{
		IntPropertyUpdateRequest request = new IntPropertyUpdateRequest();
		request.setProperty(name);
		request.setUnset(true);
		request.setRequestedValue(requested);
		return request;
	}
}
