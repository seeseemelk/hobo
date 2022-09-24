package be.seeseepuff.hobo.dto;

import io.smallrye.common.constraint.NotNull;
import io.smallrye.common.constraint.Nullable;

public abstract class IntProperty
{
	@NotNull
	public abstract Device getDevice();
	public abstract String getName();
	@Nullable
	public abstract Integer getRequested();
	@Nullable
	public abstract Integer getReported();
}
