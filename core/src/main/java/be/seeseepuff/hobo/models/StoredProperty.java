package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.Property;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@ToString
//@NamedQueries({
//	@NamedQuery(name = "StoredIntProperty.getProperty", query = "from #{#entityName} where device = ?1 and name = ?2 order by name, timestamp desc"),
//	@NamedQuery(name = "StoredIntProperty.getProperties", query = "from #{#entityName} p1 where device_id = ?1 and p1.timestamp = (select max(timestamp) from #{#entityName} p2 where p2.device = p1.device and p2.name = p1.name)")
//})
public abstract class StoredProperty<T> extends Property<T>
{
	@Id
	@GeneratedValue
	private long id;
	@ManyToOne
	@JoinColumn(name="device_id")
	private StoredDevice device;
	private String name;
	private LocalDateTime timestamp;
}
