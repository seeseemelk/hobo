package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredBoolProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BoolPropertyRepository extends PropertyRepository<StoredBoolProperty>
{
	public BoolPropertyRepository()
	{
		super("StoredBoolProperty");
	}
}
