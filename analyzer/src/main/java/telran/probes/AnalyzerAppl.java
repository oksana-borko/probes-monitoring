package telran.probes;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.probes.dto.DeviationData;
import telran.probes.dto.ProbeData;
import telran.probes.dto.Range;
import telran.probes.service.RangeProviderClient;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class AnalyzerAppl {

	@Value("${app.analyzer.producer.binding.name}")
	String producerBindingName;

	final RangeProviderClient service;
	final StreamBridge bridge;

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerAppl.class, args);
	}

	@Bean
	Consumer<ProbeData> analyzerConsumer() {
		return probeData -> {
			log.trace("received probe: {}", probeData);
			long sensorId = probeData.id();
			Range range = service.getRange(sensorId);
			double value = probeData.value();
			double deviation = 0;
			if (value < range.min())
				deviation = value - range.min();
			else if (value > range.max())
				deviation = value - range.max();
			if (deviation != 0) {
				log.debug("deviation: {}", deviation);
				DeviationData dataDeviation = new DeviationData(sensorId, deviation, value, 
						System.currentTimeMillis());
				bridge.send(producerBindingName, dataDeviation);
				log.debug("deviation data {} sent to {}", dataDeviation, producerBindingName);
			} else {
				log.debug("Deviation not detected");
			}
		};
	}
	
	@Bean
	RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}