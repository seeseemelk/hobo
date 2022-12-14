package be.seeseepuff.hobo.models;

import be.seeseepuff.hobo.dto.Property;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class StoredProperty<T> extends Property<T>
{
	@Id
	@GeneratedValue
	private long id;
	@ManyToOne
	@JoinColumn(name="device_id")
	private StoredDevice device;
	private String name;
	private LocalDateTime requestUpdated;
	private LocalDateTime reportUpdated;
	@Generated(value = GenerationTime.ALWAYS)
	private LocalDateTime lastUpdated;
}
