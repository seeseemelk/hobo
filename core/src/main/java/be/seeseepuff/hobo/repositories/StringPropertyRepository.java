package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredStringProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StringPropertyRepository extends PropertyRepository<StoredStringProperty>
{
	public StringPropertyRepository()
	{
		super("StoredStringProperty");
	}
}
