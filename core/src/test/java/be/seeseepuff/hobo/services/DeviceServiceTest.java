package be.seeseepuff.hobo.services;

import be.seeseepuff.hobo.models.StoredDevice;
import be.seeseepuff.hobo.models.StoredIntProperty;
import be.seeseepuff.hobo.repositories.DeviceRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;

@QuarkusTest
public class DeviceServiceTest
{
	@Inject
	DeviceService service;

	@InjectMock
	DeviceRepository repository;

	@Test
	void getIntPropertiesRequiringUpdate_returnPropertiesRequiringUpdates()
	{
		StoredIntProperty correctProperty1 = new StoredIntProperty();
		correctProperty1.setName("correct1");
		correctProperty1.setRequested(5);
		correctProperty1.setReported(5);

		StoredIntProperty correctProperty2 = new StoredIntProperty();
		correctProperty2.setName("correct2");
		correctProperty2.setRequested(null);
		correctProperty2.setReported(5);

		StoredIntProperty wrongProperty1 = new StoredIntProperty();
		wrongProperty1.setName("wrong1");
		wrongProperty1.setRequested(10);
		wrongProperty1.setReported(5);

		StoredIntProperty wrongProperty2 = new StoredIntProperty();
		wrongProperty2.setName("wrong2");
		wrongProperty2.setRequested(10);
		wrongProperty2.setReported(null);

		StoredDevice device = new StoredDevice();
		device.setOwner("tasmota");
		device.setName("lamp");
//		device.setIntProperties(List.of(correctProperty1, correctProperty2, wrongProperty1, wrongProperty2));
		List<StoredDevice> devices = List.of(device);
//		when(repository.findDevicesByOwner("tasmota")).thenReturn(devices);

//		List<StoredIntProperty> properties = service.getIntPropertiesRequiringUpdate("tasmota")
//			.collect().asList().await().atMost(Duration.ofSeconds(5));
//		assertThat(properties, contains(wrongProperty1, wrongProperty2));
	}
}
