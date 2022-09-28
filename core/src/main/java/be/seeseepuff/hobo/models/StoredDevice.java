package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.Device;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"owner", "name"})
	}
)
public class StoredDevice extends Device
{
	@Id
	@GeneratedValue
	private long id;
	private String owner;
	private String displayableName;
	private String name;
	private boolean online;
//	@OneToMany(mappedBy = "device")
//	private List<StoredIntProperty> intProperties;
}
