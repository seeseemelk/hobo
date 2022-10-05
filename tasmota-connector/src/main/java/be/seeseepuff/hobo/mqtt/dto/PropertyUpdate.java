package be.seeseepuff.hobo.mqtt.dto;

import lombok.Data;

import java.util.List;

@Data
public class PropertyUpdate
{
	private DeviceId device;
	private List<Property<Integer>> intProperties;
	private List<Property<Float>> floatProperties;
	private List<Property<Boolean>> boolProperties;
	private List<Property<String>> stringProperties;
}
