package be.seeseepuff.hobo.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class DeviceIntMetric
{
	@Id
	@GeneratedValue
	private long id;

	@ManyToOne
	private StoredDevice device;

	private LocalDateTime timestamp;

	private String name;

	private long reported;
}
