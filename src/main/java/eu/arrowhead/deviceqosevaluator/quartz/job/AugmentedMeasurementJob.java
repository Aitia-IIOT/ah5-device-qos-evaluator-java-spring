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

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.Constants;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.common.http.HttpUtilities;
import eu.arrowhead.deviceqosevaluator.dto.AugmentedMeasurementsDTO;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;

public class AugmentedMeasurementJob extends QuartzJobBean {

	//=================================================================================================
	// members

	@Autowired
	private DeviceDbService deviceDbService;
	
	@Autowired
	private HttpService httpService;
	
	private UUID deviceId;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.debug("AugmentedMeasurementJob.executeInternal started");
		Assert.notNull(deviceId, "device id is null");
		System.out.println("Augmented measurement execute: " + deviceId);
		
		try {
			final Optional<Device> optional = deviceDbService.findById(deviceId);
			if (optional.isEmpty()) {
				logger.error("Device not exists: " + deviceId.toString());
				return;
			}			
			final Device device = optional.get();
			
			if (device.isInactive()) {
				logger.error("Device is inactive: " + deviceId.toString());
				return;
			}
			if (!device.isAugmented()) {
				logger.error("Device is not supporting augmented measurements: " + deviceId.toString());
				return;
			}
			
			final UriComponents uri = HttpUtilities.createURI(Constants.HTTP, device.getAddress(), 59473, "/device-qos");
			final AugmentedMeasurementsDTO response = httpService.sendRequest(uri, HttpMethod.GET, AugmentedMeasurementsDTO.class);
			
			for (Entry<String, List<Double>> entry : response.entrySet()) {
				System.out.println(entry.getKey() + ": " + entry.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")));
			}
			
		} catch (final Exception ex) {
			logger.error("Device collecting job failure");
			logger.error(ex.getMessage());
			logger.debug(ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public void setDeviceId(final UUID deviceId) {
		this.deviceId = deviceId;
	}
}
