package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

@Data
public class PropertyFloatUpdateRequest
{
	@Description("The name of the property to update")
	@NonNull
	private String property;

	@Description("The new value that should be requested. If null the value will not be changed.")
	private Float request;

	@Description("The new value that should be reported. If null the value will not be changed.")
	private Float report;

	@Description("When set to true, the reported and requested value will be set to null if no value is provided for them.")
	@DefaultValue("false")
	private boolean unset;

	public static PropertyFloatUpdateRequest withRequest(String property, Float value)
	{
		PropertyFloatUpdateRequest request = new PropertyFloatUpdateRequest();
		request.setProperty(property);
		request.setRequest(value);
		return request;
	}

	public static PropertyFloatUpdateRequest withReport(String property, Float value)
	{
		PropertyFloatUpdateRequest request = new PropertyFloatUpdateRequest();
		request.setProperty(property);
		request.setReport(value);
		return request;
	}
}
