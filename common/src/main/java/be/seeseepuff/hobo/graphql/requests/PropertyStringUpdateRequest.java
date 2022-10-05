package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

@Data
public class PropertyStringUpdateRequest
{
	@Description("The name of the property to update")
	@NonNull
	private String property;

	@Description("The new value that should be requested. If null the value will not be changed.")
	private String request;

	@Description("The new value that should be reported. If null the value will not be changed.")
	private String report;

	@Description("When set to true, the reported and requested value will be set to null if no value is provided for them.")
	@DefaultValue("false")
	private boolean unset;

	public static PropertyStringUpdateRequest withRequest(String property, String value)
	{
		PropertyStringUpdateRequest request = new PropertyStringUpdateRequest();
		request.setProperty(property);
		request.setRequest(value);
		return request;
	}

	public static PropertyStringUpdateRequest withReport(String property, String value)
	{
		PropertyStringUpdateRequest request = new PropertyStringUpdateRequest();
		request.setProperty(property);
		request.setReport(value);
		return request;
	}
}
