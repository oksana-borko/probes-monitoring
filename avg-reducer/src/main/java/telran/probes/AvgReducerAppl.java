package telran.probes;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.ProbeData;
import telran.probes.service.AvgReducerService;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class AvgReducerAppl {
	final AvgReducerService avgReducerService;
	final StreamBridge streamBridge;
	@Value("${app.avg.reducer.producer.binding.name}")
	private String producerBindingName;

	public static void main(String[] args) {
		SpringApplication.run(AvgReducerAppl.class, args);
	}

	@Bean
	Consumer<ProbeData> avgReducerConsumer() {
		return probeData -> {
			log.trace("received {}", probeData);
			Double avgValue = avgReducerService.getAvgValue(probeData);
			long sensorId = probeData.id();
			if (avgValue != null) {
				ProbeData avgProbeData = new ProbeData(sensorId, avgValue, System.currentTimeMillis());
				streamBridge.send(producerBindingName, avgProbeData);
				log.debug("reduced probe data: {} has been sent to {} binding name", avgProbeData, producerBindingName);
			} else {
				log.trace("no avg value for sensor {}", sensorId);
			}
		};
	}
}
