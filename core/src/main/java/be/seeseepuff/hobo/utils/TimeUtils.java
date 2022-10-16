package be.seeseepuff.hobo.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class TimeUtils
{
	public boolean isAfter(LocalDateTime earlier, LocalDateTime later)
	{
		if (later == null)
			return true;
		else
			return later.isAfter(earlier);
	}
}
