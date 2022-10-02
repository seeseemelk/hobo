package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import java.util.List;

@Data
@Description("Updates an integer property")
public class IntPropertyUpdateRequest
{
	@Description("The name of the property to update")
	@NonNull
	private String property;

	@Description("The new value that should be requested. If null the value will not be changed.")
	private Integer requestedValue;

	@Description("The new value that should be reported. If null the value will not be changed.")
	private Integer reportedValue;

	@Description("When set to true, the reported and requested value will be set to null if no value is provided for them.")
	private boolean unset = false;

	public static IntPropertyUpdateRequest withRequest(String property, int value)
	{
		IntPropertyUpdateRequest request = new IntPropertyUpdateRequest();
		request.setProperty(property);
		request.setRequestedValue(value);
		return request;
	}

	public static List<IntPropertyUpdateRequest> withRequests(String property, int value)
	{
		return List.of(withRequest(property, value));
	}

	public static IntPropertyUpdateRequest withReport(String property, int value)
	{
		IntPropertyUpdateRequest request = new IntPropertyUpdateRequest();
		request.setProperty(property);
		request.setReportedValue(value);
		return request;
	}

	public static List<IntPropertyUpdateRequest> withReports(String property, int value)
	{
		IntPropertyUpdateRequest request = new IntPropertyUpdateRequest();
		request.setProperty(property);
		request.setReportedValue(value);
		return List.of(request);
	}
}
