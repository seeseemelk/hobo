package be.seeseepuff.hobo.mqtt;

import lombok.Data;

@Data
public class Color
{
	private Float red;
	private Float green;
	private Float blue;
	private Float white;

	public void setRed(float red)
	{
		setRed(Math.round(red * 255f));
	}

	public void setGreen(float green)
	{
		setGreen(Math.round(green * 255f));
	}

	public void setBlue(float blue)
	{
		setBlue(Math.round(blue * 255f));
	}

	public void setWhite(float white)
	{
		setWhite(Math.round(white * 255f));
	}

	public void setRed(int red)
	{
		this.red = red / 255f;
	}

	public void setGreen(int green)
	{
		this.green = green / 255f;
	}

	public void setBlue(int blue)
	{
		this.blue = blue / 255f;
	}

	public void setWhite(int white)
	{
		this.white = white / 255f;
	}

	public boolean isSet()
	{
		return red != null || green != null || blue != null || white != null;
	}

	public void copyMissingFrom(Color color)
	{
		if (color != null)
		{
			red = getValue(red, color.red);
			green = getValue(green, color.green);
			blue = getValue(blue, color.blue);
			white = getValue(white, color.white);
		}
	}

	public String toString()
	{
		return String.format("%02X%02X%02X%02X", getAsInt(red), getAsInt(green), getAsInt(blue), getAsInt(white));
	}

	public static Color fromString(String rgbw)
	{
		Color color = new Color();
		color.setRed(getColorByte(rgbw, 0));
		color.setGreen(getColorByte(rgbw, 1));
		color.setBlue(getColorByte(rgbw, 2));
		color.setWhite(getColorByte(rgbw, 3));
		return color;
	}

	private static int getColorByte(String rgbw, int index)
	{
		String part = rgbw.substring(index * 2, index * 2 + 2);
		return Integer.parseInt(part, 16);
	}

	public static int getAsInt(Float color)
	{
		if (color == null)
			return 0;
		else
			return Math.round(color * 255f);
	}

	private static Float getValue(Float a, Float b)
	{
		if (a != null)
			return a;
		else if (b != null)
			return b;
		else
			return 0f;
	}
}
