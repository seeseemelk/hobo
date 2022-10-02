package be.seeseepuff.hobo.mqtt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result
{
	@JsonProperty("Color")
	private String color;

	public int getRed()
	{
		return getColorByte(0);
	}

	public int getGreen()
	{
		return getColorByte(1);
	}

	public int getBlue()
	{
		return getColorByte(2);
	}

	public int getWhite()
	{
		return getColorByte(3);
	}

	private int getColorByte(int index)
	{
		String part = color.substring(index * 2, index * 2 + 2);
		return Integer.parseInt(part, 16);
	}
}
