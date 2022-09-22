package be.seeseepuff.hobo.controllers;

import be.seeseepuff.hobo.models.HelloPacket;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

@GraphQLApi
public class GraphController
{
	@Mutation
	public String sayHello(HelloPacket packet)
	{
		return "hi!";
	}
}
