package be.seeseepuff.hobo.it;

import io.quarkus.test.common.http.TestHTTPResource;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.*;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractIT
{
	public static final String OWNER = "test";
	private static DynamicGraphQLClient client;

	@TestHTTPResource("/graphql")
	URL url;


	@BeforeEach
	void setUp()
	{
		client = DynamicGraphQLClientBuilder.newBuilder().url(url.toString()).build();
	}

	@AfterEach
	void cleanUp() throws ExecutionException, InterruptedException
	{
		mutate(
			field("deleteDevices",
				args(arg("owner", OWNER)),
				field("count")
			)
		);
	}

	public long createDevice() throws ExecutionException, InterruptedException
	{
		return mutate(
			field("createDevice",
				args(
					arg("owner", OWNER),
					arg("name", UUID.randomUUID().toString())
				),
				field("id")
			)
		).getData().getJsonObject("createDevice").getInt("id");
	}

	public Response query(FieldOrFragment fof) throws ExecutionException, InterruptedException
	{
		return execute(operation(fof));
	}

	public Response mutate(FieldOrFragment fof) throws ExecutionException, InterruptedException
	{
		return execute(operation(OperationType.MUTATION, fof));
	}

	private Response execute(Operation operation) throws ExecutionException, InterruptedException
	{
		Response response = client.executeSync(document(operation));
		assertThat(response.hasError(), equalTo(false));
		assertThat(response.hasData(), equalTo(true));
		return response;
	}

	@SafeVarargs
	public final <T> T[] listOf(T... values)
	{
		return values;
	}

	public <T> InputObject propertyRequestReport(String property, T request, T report)
	{
		return inputObject(
			prop("property", property),
			prop("request", request),
			prop("report", report)
		);
	}
}
