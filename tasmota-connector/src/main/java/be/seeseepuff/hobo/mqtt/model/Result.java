package be.seeseepuff.hobo.mqtt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Result
{
	@JsonProperty("Color")
	private String color;
}
