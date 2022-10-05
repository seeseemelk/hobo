package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

@Data
public class PropertyIntUpdateRequest
{
	@Description("The name of the property to update")
	@NonNull
	private String property;

	@Description("The new value that should be requested. If null the value will not be changed.")
	private Integer request;

	@Description("The new value that should be reported. If null the value will not be changed.")
	private Integer report;

	@Description("When set to true, the reported and requested value will be set to null if no value is provided for them.")
	@DefaultValue("false")
	private boolean unset;

	public static PropertyIntUpdateRequest withRequest(String property, Integer value)
	{
		PropertyIntUpdateRequest request = new PropertyIntUpdateRequest();
		request.setProperty(property);
		request.setRequest(value);
		return request;
	}

	public static PropertyIntUpdateRequest withReport(String property, Integer value)
	{
		PropertyIntUpdateRequest request = new PropertyIntUpdateRequest();
		request.setProperty(property);
		request.setReport(value);
		return request;
	}
}
