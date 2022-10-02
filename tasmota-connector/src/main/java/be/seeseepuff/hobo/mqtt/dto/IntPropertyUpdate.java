package be.seeseepuff.hobo.mqtt.dto;

import lombok.Data;

import java.util.List;

@Data
public class IntPropertyUpdate
{
	private DeviceId device;
	private List<IntProperty> intProperties;
}
