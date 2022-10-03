package be.seeseepuff.hobo.repositories;

import be.seeseepuff.hobo.models.StoredFloatProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FloatPropertyRepository extends PropertyRepository<StoredFloatProperty>
{
	public FloatPropertyRepository()
	{
		super("StoredFloatProperty");
	}
}
