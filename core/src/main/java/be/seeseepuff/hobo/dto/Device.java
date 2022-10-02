package be.seeseepuff.hobo.dto;

import org.eclipse.microprofile.graphql.NonNull;

public abstract class Device
{
	public abstract long getId();
	@NonNull
	public abstract String getOwner();
	@NonNull
	public abstract String getName();
}
