package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.IntProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
public class StoredIntProperty extends IntProperty
{
	@Id
	@GeneratedValue
	private long id;
	@ManyToOne
	private StoredDevice device;
	private String name;
	private Integer requested;
	private Integer reported;
}
