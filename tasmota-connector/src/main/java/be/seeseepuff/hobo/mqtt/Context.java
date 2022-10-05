package be.seeseepuff.hobo.mqtt;

import lombok.Data;

@Data
public class Context
{
	private long deviceId;
	private String topic;
	private String mac;
	private String ip;
	private Color color = new Color();
}
