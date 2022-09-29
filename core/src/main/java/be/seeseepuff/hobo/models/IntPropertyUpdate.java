package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.IntProperty;
import lombok.Data;

import java.util.List;

@Data
public class IntPropertyUpdate
{
	private StoredDevice device;
	private List<IntProperty> intProperties;
}
