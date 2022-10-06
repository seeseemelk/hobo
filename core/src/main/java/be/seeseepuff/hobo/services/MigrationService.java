package be.seeseepuff.hobo.services;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.flyway.runtime.FlywayContainer;
import io.quarkus.flyway.runtime.FlywayContainerProducer;
import io.quarkus.flyway.runtime.QuarkusPathLocationScanner;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.flywaydb.core.Flyway;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class MigrationService
{
	@ConfigProperty(name = "quarkus.datasource.reactive.url")
	String datasourceUrl;

	@ConfigProperty(name = "quarkus.datasource.username")
	String datasourceUsername;

	@ConfigProperty(name = "quarkus.datasource.password")
	String datasourcePassword;

	@ConfigProperty(name = "todo.migration.files")
	List<String> files;

	public void runFlywayMigration(@Observes StartupEvent event, FlywayContainer container)
	{
		log.info("Initialising flyway...");
		log.info("Got container: {}", container.);
		QuarkusPathLocationScanner.setApplicationMigrationFiles(files.stream()
			.map(file -> "db/migration/" + file)
			.collect(Collectors.toList()));
		DataSource datasource = Flyway.configure()
			.dataSource(getDatasourceUrl(), datasourceUsername, datasourcePassword)
			.getDataSource();
		try (InstanceHandle<FlywayContainerProducer> instance = Arc.container().instance(FlywayContainerProducer.class))
		{
			FlywayContainerProducer flywayProducer = instance.get();
			FlywayContainer flywayContainer = flywayProducer.createFlyway(datasource, "<default>", true, true);
			Flyway flyway = flywayContainer.getFlyway();
			flyway.migrate();
		}
	}

	private String getDatasourceUrl()
	{
		if (datasourceUrl.startsWith("vertx-reactive:"))
			return "jdbc:" + datasourceUrl.substring("vertx-reactive:".length());
		else
			return "jdbc:" + datasourceUrl;
	}
}
