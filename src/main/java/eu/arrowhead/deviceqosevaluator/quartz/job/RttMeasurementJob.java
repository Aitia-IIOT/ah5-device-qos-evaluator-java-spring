/*******************************************************************************
 *
 * Copyright (c) 2025 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 *
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  	AITIA - implementation
 *  	Arrowhead Consortia - conceptualization
 *
 *******************************************************************************/
package eu.arrowhead.deviceqosevaluator.quartz.job;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.driver.RttMeasurementDriver;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.StatDbService;
import eu.arrowhead.deviceqosevaluator.util.Stat;

public class RttMeasurementJob extends QuartzJobBean {

	//=================================================================================================
	// members

	@Autowired
	private DeviceDbService deviceDbService;

	@Autowired
	private RttMeasurementDriver measurementDriver;
	
	@Autowired
	private StatDbService statDbService;

	private UUID deviceId;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void setDeviceId(final UUID deviceId) {
		this.deviceId = deviceId;
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
		logger.debug("RttMeasurementJob.executeInternal started");
		Assert.notNull(deviceId, "device id is null");

		try {
			final Optional<Device> optional = deviceDbService.findById(deviceId);
			if (optional.isEmpty()) {
				logger.warn("Device not exists: " + deviceId.toString());
				return;
			}
			final Device device = optional.get();

			if (device.isInactive()) {
				logger.warn("Device is inactive: " + deviceId.toString());
				return;
			}

			double[] results = new double[10];
			boolean timeout = false;
			for (int i = 0; i < results.length; ++i) {
				results[i] = doMeasurement(device);
				if (results[i] == -1) {
					timeout = true;
					break;
				}
			}
			
			statDbService.save(
					Utilities.utcNow(),
					OidGroup.RTT,
					deviceId,
					timeout ? DeviceQoSEvaluatorConstants.NO_MEASUREMENT_VALUES : List.of(Stat.min(results), Stat.max(results), Stat.mean(results), Stat.median(results), results[results.length - 1]));

		} catch (final Exception ex) {
			logger.error("RTT measurement job failure. Device: " + deviceId.toString() + ", Error: " + ex.getMessage());
			logger.debug(ex);
		}
	}

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private long doMeasurement(final Device device) throws IOException {
		Integer rttPort = device.getRttPort();
		if (rttPort == null) {
			rttPort = randomPort();
		}

		Long result = null;
		do {
			result = measurementDriver.measure(device.getAddress(), rttPort);
			if (result == null) {
				rttPort = randomPort();
			}
		} while (result == null);
		
		if (device.getRttPort() != rttPort) {
			device.setRttPort(rttPort);
			deviceDbService.update(device);
		}
		
		return result;
	}

	//-------------------------------------------------------------------------------------------------
	private int randomPort() {
		return 45530; // TODO
	}

}
