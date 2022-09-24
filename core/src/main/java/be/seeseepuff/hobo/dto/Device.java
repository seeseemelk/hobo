package be.seeseepuff.hobo.dto;

import io.smallrye.graphql.api.Nullable;
import org.eclipse.microprofile.graphql.NonNull;

import java.util.List;

public abstract class Device
{
	public abstract long getId();
	@Nullable
	public abstract String getDisplayableName();
	@NonNull
	public abstract String getOwner();
	@NonNull
	public abstract String getName();
	public abstract boolean isOnline();
	@NonNull
	public abstract List<? extends IntProperty> getIntProperties();
}
