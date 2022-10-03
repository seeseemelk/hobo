package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredIntProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IntPropertyRepository extends PropertyRepository<StoredIntProperty>
{
	public IntPropertyRepository()
	{
		super("StoredIntProperty");
	}
}
