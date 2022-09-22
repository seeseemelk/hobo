package be.seeseepuff.hobo.models;

import javax.persistence.*;

@Entity
public class StoredConnectionConfig
{
	@Id
	@GeneratedValue
	private long id;

	@ManyToOne
	private StoredConnection connection;

	private String key;
	private String value;
}
