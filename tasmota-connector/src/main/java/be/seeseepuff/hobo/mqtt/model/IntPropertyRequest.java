package be.seeseepuff.hobo.mqtt.model;

import be.seeseepuff.hobo.mqtt.dto.DeviceId;
import lombok.Data;

@Data
public class IntPropertyRequest
{
	private DeviceId device;
	private String name;
	private Integer requested;
}
