package be.seeseepuff.hobo.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@ToString(callSuper = true)
public class StoredBoolProperty extends StoredProperty<Boolean>
{
	private Boolean requested;
	private Boolean reported;
}
