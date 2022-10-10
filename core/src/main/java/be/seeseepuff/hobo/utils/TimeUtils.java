package be.seeseepuff.hobo.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class TimeUtils
{
	public LocalDateTime latestOf(LocalDateTime a, LocalDateTime b)
	{
		if (a == null)
			return b;
		else if (b == null)
			return a;
		else if (a.isAfter(b))
			return a;
		else
			return b;
	}

	public boolean isAfter(LocalDateTime earlier, LocalDateTime later)
	{
		if (later == null)
			return true;
		else
			return later.isAfter(earlier);
	}
}
