package telran.probes.repo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;

@RedisHash
@Getter
public class ProbesList {
	@Id
	Long sensorId;
	List<Double> values = new ArrayList<>();
	public ProbesList(Long sensorId) {
		super();
		this.sensorId = sensorId;
	}
	
	
}