package telran.probes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.*;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import telran.probes.dto.ProbeData;
import telran.probes.service.AvgReducerService;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class AvgReducerControllerTest
{
	private static final long SENSOR_ID_AVG = 123;
	private static final double VALUE = 100;
	private static final double AVG_VALUE = 150;
	private static final long SENSOR_ID_NO_AVG = 2;
	private static final long DIFF_TIMESTAMP = 2000;
	@Autowired
	InputDestination producer;
	@Autowired
	OutputDestination consumer;
	@MockBean
	AvgReducerService avgReducerService;
	String producerBindingName = "test-out-0";
	String consumerBindingName = "avgReducerConsumer-in-0";
	ProbeData probeNoAvgValue = new ProbeData(SENSOR_ID_NO_AVG, VALUE, 0);
	ProbeData probeAvgValue = new ProbeData(SENSOR_ID_AVG, VALUE, 0);
	@BeforeEach
	void setServiceMock()
	{
		when(avgReducerService.getAvgValue(probeNoAvgValue)).thenReturn(null);
		when(avgReducerService.getAvgValue(probeAvgValue)).thenReturn(AVG_VALUE);
	}
	@Test
	void noAvgValueTest()
	{
		producer.send(new GenericMessage<ProbeData>(probeNoAvgValue), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNull(message);
	}
	@Test
	void avgValueTest() throws Exception
	{
		producer.send(new GenericMessage<ProbeData>(probeAvgValue), consumerBindingName);
		Message<byte[]> message = consumer.receive(10, producerBindingName);
		assertNotNull(message);
		ObjectMapper mapper = new ObjectMapper();
		ProbeData actual = mapper.readValue(message.getPayload(), ProbeData.class);
		ProbeData expected = new ProbeData(SENSOR_ID_AVG, AVG_VALUE, System.currentTimeMillis());
		assertEquals(expected.id(), actual.id());
		assertEquals(expected.value(), actual.value());
		assertTrue(Math.abs(expected.timestamp() - actual.timestamp()) < DIFF_TIMESTAMP);
	}
}