package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.graphql.requests.PropertyBoolUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyFloatUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyIntUpdateRequest;
import be.seeseepuff.hobo.graphql.requests.PropertyStringUpdateRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropertyUpdateRequest<T>
{
	String property;
	boolean unset;
	T report;
	T request;

	public static PropertyUpdateRequest<Float> from(PropertyFloatUpdateRequest request)
	{
		return PropertyUpdateRequest.<Float>builder()
			.property(request.getProperty())
			.unset(request.isUnset())
			.report(request.getReport())
			.request(request.getRequest())
			.build();
	}

	public static PropertyUpdateRequest<Integer> from(PropertyIntUpdateRequest request)
	{
		return PropertyUpdateRequest.<Integer>builder()
			.property(request.getProperty())
			.unset(request.isUnset())
			.report(request.getReport())
			.request(request.getRequest())
			.build();
	}

	public static PropertyUpdateRequest<Boolean> from(PropertyBoolUpdateRequest request)
	{
		return PropertyUpdateRequest.<Boolean>builder()
			.property(request.getProperty())
			.unset(request.isUnset())
			.report(request.getReport())
			.request(request.getRequest())
			.build();
	}

	public static PropertyUpdateRequest<String> from(PropertyStringUpdateRequest request)
	{
		return PropertyUpdateRequest.<String>builder()
			.property(request.getProperty())
			.unset(request.isUnset())
			.report(request.getReport())
			.request(request.getRequest())
			.build();
	}
}
