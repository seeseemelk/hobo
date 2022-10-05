package be.seeseepuff.hobo.mqtt.dto;

import lombok.Data;

@Data
public class Property<T>
{
	private String name;
	private T requested;
	private T reported;
}
