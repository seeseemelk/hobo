package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.IntProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
public class StoredIntProperty extends IntProperty
{
	@Id
	@GeneratedValue
	private long id;
	@ManyToOne
	@JoinColumn(name="device_id")
	private StoredDevice device;
	private String name;
	private Integer requested;
	private Integer reported;

	public boolean requiresUpdate()
	{
		if (requested == null)
			return false;
		else if (reported == null)
			return true;
		else
			return requested.intValue() != reported.intValue();
	}
}
