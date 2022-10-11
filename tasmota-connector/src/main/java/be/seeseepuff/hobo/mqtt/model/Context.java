package be.seeseepuff.hobo.mqtt.model;

import lombok.Data;

@Data
public class Context
{
	private long deviceId;
	private String topic;
	private String mac;
	private String ip;
	private Color color = new Color();
	private Color newColor = new Color();

	private boolean waitingForResult = false;
	private boolean updateColor = false;
}
