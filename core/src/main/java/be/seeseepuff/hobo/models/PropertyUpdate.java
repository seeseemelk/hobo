package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.Device;
import be.seeseepuff.hobo.dto.Property;
import lombok.Data;

import java.util.List;

@Data
public class PropertyUpdate
{
	private Device device;
	private List<Property<Integer>> intProperties;
	private List<Property<Boolean>> boolProperties;
	private List<Property<Float>> floatProperties;
	private List<Property<String>> stringProperties;
}
