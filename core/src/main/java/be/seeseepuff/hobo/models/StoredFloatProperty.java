package be.seeseepuff.hobo.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@ToString
public class StoredFloatProperty extends StoredProperty<Float>
{
	private Float requested;
	private Float reported;
}
