package be.seeseepuff.hobo.graphql.requests;

import lombok.Data;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import java.util.List;

@Data
@Description("Updates an integer property")
public class PropertyUpdateRequest<T>
{
	@Description("The name of the property to update")
	@NonNull
	private String property;

	@Description("The new value that should be requested. If null the value will not be changed.")
	private T requestedValue;

	@Description("The new value that should be reported. If null the value will not be changed.")
	private T reportedValue;

	@Description("When set to true, the reported and requested value will be set to null if no value is provided for them.")
	private boolean unset = false;

	public static <T> PropertyUpdateRequest<T> withRequest(String property, T value)
	{
		PropertyUpdateRequest<T> request = new PropertyUpdateRequest<>();
		request.setProperty(property);
		request.setRequestedValue(value);
		return request;
	}

	public static <T> List<PropertyUpdateRequest<T>> withRequests(String property, T value)
	{
		return List.of(withRequest(property, value));
	}

	public static <T> PropertyUpdateRequest<T> withReport(String property, T value)
	{
		PropertyUpdateRequest<T> request = new PropertyUpdateRequest<>();
		request.setProperty(property);
		request.setReportedValue(value);
		return request;
	}

	public static <T> List<PropertyUpdateRequest<T>> withReports(String property, T value)
	{
		PropertyUpdateRequest<T> request = new PropertyUpdateRequest<>();
		request.setProperty(property);
		request.setReportedValue(value);
		return List.of(request);
	}
}
