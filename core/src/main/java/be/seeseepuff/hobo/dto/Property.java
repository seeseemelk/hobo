package be.seeseepuff.hobo.dto;

import io.smallrye.common.constraint.NotNull;
import io.smallrye.common.constraint.Nullable;

import java.time.LocalDateTime;

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

	public abstract LocalDateTime getRequestTimestamp();
	public abstract void setRequestTimestamp(LocalDateTime timestamp);

	public abstract LocalDateTime getReportTimestamp();
	public abstract void setReportTimestamp(LocalDateTime timestamp);

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
