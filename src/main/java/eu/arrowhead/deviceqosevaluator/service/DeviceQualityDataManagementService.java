package eu.arrowhead.deviceqosevaluator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.deviceqosevaluator.engine.MeasurementEngine;

@Service
public class DeviceQualityDataManagementService {

	//=================================================================================================
	// members

	@Autowired
	private MeasurementEngine measurementEngine;
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void query() {
		// TODO
	}

	//-------------------------------------------------------------------------------------------------
	public String reload(final String origin) {
		logger.debug("reloadSystems started");

		try {
			final Pair<Integer, Integer> results = measurementEngine.organize();
			if (results == null) {
				return "Reload operation is already in proggress";
			}
			return  results.getFirst() + " more systems found, " + results.getSecond() + " systems removed";
		} catch (final ArrowheadException | SchedulerException ex) {
			logger.error(ex.getMessage());
			logger.debug(ex);
			throw new ArrowheadException(ex.getMessage(), origin);
		}
	}
}
