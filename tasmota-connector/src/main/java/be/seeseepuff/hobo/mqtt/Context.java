package be.seeseepuff.hobo.mqtt;

import lombok.Data;

@Data
public class Context
{
	private long deviceId;
	private String topic;
	private Color color = new Color();
}
