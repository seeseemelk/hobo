package be.seeseepuff.hobo.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class StoredConnection
{
	@Id
	@GeneratedValue
	private long id;
	private String type;
	private String name;
}
