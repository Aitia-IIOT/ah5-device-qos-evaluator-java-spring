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
package eu.arrowhead.deviceqosevaluator.init;

import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.init.ApplicationInitListener;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.AugmentedMeasurementJobScheduler;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.CleaningJobScheduler;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.DeviceCollectorJobScheduler;

@Component
public class DeviceQoSEvaluatorApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// members

	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;

	@Autowired
	private DeviceCollectorJobScheduler deviceCollectorJobScheduler;

	@Autowired
	private AugmentedMeasurementJobScheduler augmentedMeasurementJobScheduler;

	@Autowired
	private CleaningJobScheduler cleaningJobScheduler;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(ContextRefreshedEvent event) throws InterruptedException, ConfigurationException {
		validateConfiguration();

		try {
			deviceCollectorJobScheduler.start();
			logger.info("Device collection job has been started");

			cleaningJobScheduler.start();
			logger.info("Cleaning job has been started");
		} catch (final SchedulerException ex) {
			logger.error("Error while scheduling device collection job");
			logger.debug(ex);
			throw new ConfigurationException(ex.getMessage());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		try {
			deviceCollectorJobScheduler.stop();
			logger.info("Device collection job has been terminated");

			cleaningJobScheduler.stop();
			logger.info("Cleaning job has been terminated");

			augmentedMeasurementJobScheduler.stop(List.of()); // TODO 
		} catch (SchedulerException ex) {
			logger.error("Error while terminating jobs scheduling");
			logger.debug(ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public void validateConfiguration() throws ConfigurationException {
		if (sysInfo.getDeviceCollectorJobInterval() < DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB_INTERVAL_MIN_VALUE + " sec");
		}
		if (sysInfo.getAugmentedMeasurementJobInterval() < DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL + " sec");
		}
		if (sysInfo.getCleaningJobInterval() < DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL_MIN_VALUE + " sec");
		}
	}
}
