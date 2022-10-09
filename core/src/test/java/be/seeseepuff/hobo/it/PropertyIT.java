package be.seeseepuff.hobo.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Field.field;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class PropertyIT extends AbstractIT
{
	@Test
	void devicesIsEmptyAtStart() throws ExecutionException, InterruptedException
	{
		JsonArray result = query(
			field("devices",
				field("id")
			)
		).getData().getJsonArray("devices");

		assertThat(result.size(), equalTo(0));
	}

	@Test
	void canCreateDevice() throws ExecutionException, InterruptedException
	{
		JsonObject result = mutate(
			field("createDevice",
				args(
					arg("owner", OWNER),
					arg("name", "test1")
				),
				field("id"),
				field("owner"),
				field("name")
			)
		).getData().getJsonObject("createDevice");

		assertThat(result.getInt("id"), greaterThanOrEqualTo(0));
		assertThat(result.getString("owner"), equalTo(OWNER));
		assertThat(result.getString("name"), equalTo("test1"));
	}

	@Test
	void canModifyProperty() throws ExecutionException, InterruptedException
	{
		long device = createDevice();

		JsonArray properties = mutate(
			field("updateIntProperties",
				args(
					arg("deviceId", device),
					arg("updates", listOf(
							propertyRequestReport("testProperty", 42, 5)
						)
					)
				),
				field("name"),
				field("requested"),
				field("reported")
			)
		).getData().getJsonArray("updateIntProperties");

		assertThat(properties, hasSize(1));
		JsonObject property = properties.getJsonObject(0);
		assertThat(property.getString("name"), equalTo("testProperty"));
		assertThat(property.getInt("requested"), equalTo(42));
		assertThat(property.getInt("reported"), equalTo(5));
	}
}
