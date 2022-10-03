package be.seeseepuff.hobo.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@ToString
public class StoredStringProperty extends StoredProperty<String>
{
	private String requested;
	private String reported;
}
