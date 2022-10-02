package be.seeseepuff.hobo.mqtt.dto;

import lombok.Data;

@Data
public class IntProperty
{
	private String name;
	private Integer requested;
	private Integer reported;
}
