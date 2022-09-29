package be.seeseepuff.hobo.controllers.models;

import be.seeseepuff.hobo.models.StoredIntProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class IntPropertyRequest
{
	private String property;
	private int value;
}
