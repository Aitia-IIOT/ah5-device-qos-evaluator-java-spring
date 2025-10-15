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
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.MeasurementOrganizerJobScheduler;

@Component
public class DeviceQoSEvaluatorApplicationInitListener extends ApplicationInitListener {

	//=================================================================================================
	// members

	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;

	@Autowired
	private MeasurementOrganizerJobScheduler measurementOrganizerJobScheduler;
	
	@Autowired
	private AugmentedMeasurementJobScheduler rttMeasurementJobScheduler;

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
			measurementOrganizerJobScheduler.start();
			logger.info("Mesaurement organizer job has been started");

			cleaningJobScheduler.start();
			logger.info("Cleaning job has been started");
		} catch (final SchedulerException ex) {
			logger.error("Error occured while scheduling jobs at start-up");
			logger.debug(ex);
			throw new ConfigurationException(ex.getMessage());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customDestroy() {
		try {
			measurementOrganizerJobScheduler.stop();
			logger.info("Mesaurement organizer job has been terminated");

			cleaningJobScheduler.stop();
			logger.info("Cleaning job has been terminated");

			augmentedMeasurementJobScheduler.stop(List.of()); // TODO
			rttMeasurementJobScheduler.stop(List.of()); // TODO 
		} catch (SchedulerException ex) {
			logger.error("Error occured while terminating jobs scheduling");
			logger.debug(ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	public void validateConfiguration() throws ConfigurationException {
		if (sysInfo.getMeasurementOrganizerJobInterval() < DeviceQoSEvaluatorConstants.MEASUREMENT_ORGANIZER_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.MEASUREMENT_ORGANIZER_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.MEASUREMENT_ORGANIZER_JOB_INTERVAL_MIN_VALUE + " sec");
		}
		if (sysInfo.getRttMeasurementJobInterval() < DeviceQoSEvaluatorConstants.RTT_MEASUREMENT_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.RTT_MEASUREMENT_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.RTT_MEASUREMENT_JOB_INTERVAL + " sec");
		}
		if (sysInfo.getAugmentedMeasurementJobInterval() < DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.AUGMENTED_MEASUREMENT_JOB_INTERVAL + " sec");
		}
		if (sysInfo.getCleaningJobInterval() < DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL_MIN_VALUE) {
			throw new ConfigurationException("Invalid configuration: '" + DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL + "' cannot be less than " + DeviceQoSEvaluatorConstants.CLEANING_JOB_INTERVAL_MIN_VALUE + " sec");
		}
	}
}
