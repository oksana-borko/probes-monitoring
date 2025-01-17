package telran.probes.service;

import telran.probes.dto.ProbeData;

public interface AvgReducerService {
	Double getAvgValue(ProbeData data);
}