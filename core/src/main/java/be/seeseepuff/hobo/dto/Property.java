package be.seeseepuff.hobo.dto;

import io.smallrye.common.constraint.NotNull;
import io.smallrye.common.constraint.Nullable;

public abstract class Property<T>
{
	@NotNull
	public abstract Device getDevice();
	public abstract String getName();
	@Nullable
	public abstract T getRequested();

	public abstract void setRequested(@Nullable T value);
	@Nullable
	public abstract T getReported();

	public abstract void setReported(@Nullable T value);

	public boolean requiresUpdate()
	{
		if (getRequested() == null)
			return false;
		else if (getReported() == null)
			return true;
		else
			return !getRequested().equals(getReported());
	}
}
