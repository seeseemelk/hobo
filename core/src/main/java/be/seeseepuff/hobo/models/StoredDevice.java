package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.Device;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class StoredDevice extends Device
{
	@Id
	@GeneratedValue
	private long id;
	private String owner;
	private String name;
}
