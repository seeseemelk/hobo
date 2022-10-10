package be.seeseepuff.hobo.mqtt.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Property<T>
{
	private String name;
	private T requested;
	private T reported;
	private LocalDateTime requestUpdated;
}
