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
package eu.arrowhead.deviceqosevaluator;

public class DeviceQoSEvaluatorConstants {

	//=================================================================================================
	// members

	public static final String DATABASE_ENTITY_PACKAGE = "eu.arrowhead.deviceqosevaluator.jpa.entity";
	public static final String DATABASE_REPOSITORY_PACKAGE = "eu.arrowhead.deviceqosevaluator.jpa.repository";
	
	// Config related
	
	public static final String MEASUREMENT_ORGANIZER_JOB_INTERVAL = "measurement.organizer.job.interval";
	public static final String $MEASUREMENT_ORGANIZER_JOB_INTERVAL_WD = "${" + MEASUREMENT_ORGANIZER_JOB_INTERVAL + ":" + DeviceQoSEvaluatorDefaults.MEASUREMENT_ORGANIZER_JOB_INTERVAL_DEFAULT + "}";
	public static final int MEASUREMENT_ORGANIZER_JOB_INTERVAL_MIN_VALUE = 10; // sec
	public static final String RTT_MEASUREMENT_JOB_INTERVAL = "rtt.measurement.job.interval";
	public static final String $RTT_MEASUREMENT_JOB_INTERVAL_WD = "${" + RTT_MEASUREMENT_JOB_INTERVAL + ":" + DeviceQoSEvaluatorDefaults.RTT_MEASUEREMENT_JOB_INTERVAL_DEFAULT + "}";
	public static final int RTT_MEASUREMENT_JOB_INTERVAL_MIN_VALUE = 5; // sec
	public static final String AUGMENTED_MEASUREMENT_JOB_INTERVAL = "augmented.measurement.job.interval";
	public static final String $AUGMENTED_MEASUREMENT_JOB_INTERVAL_WD = "${" + AUGMENTED_MEASUREMENT_JOB_INTERVAL + ":" + DeviceQoSEvaluatorDefaults.AUGMENTED_MEASUEREMENT_JOB_INTERVAL_DEFAULT + "}";
	public static final int AUGMENTED_MEASUREMENT_JOB_INTERVAL_MIN_VALUE = 5; // sec
	public static final String CLEANING_JOB_INTERVAL = "cleaning.job.interval";
	public static final String $CLEANING_JOB_INTERVAL_WD = "${" + CLEANING_JOB_INTERVAL + ":" + DeviceQoSEvaluatorDefaults.CLEANING_JOB_INTERVAL_DEFAULT + "}";
	public static final int CLEANING_JOB_INTERVAL_MIN_VALUE = 60; // sec
	
	// API related
	
	public static final String HTTP_API_BASE_PATH = "/deviceqosevaluator";
	public static final String HTTP_API_MONITOR_PATH = HTTP_API_BASE_PATH + "/monitor";
	
	// Quartz related
	public static final String MEASUREMENT_ORGANIZER_JOB = "measurement_organizer_job";
	public static final String MEASUREMENT_ORGANIZER_JOB_TRIGGER = "measurement_organizer_job_trigger";
	public static final String CLEANING_JOB = "cleaning_job";
	public static final String CLEANING_JOB_TRIGGER = "cleaning_job_trigger";
}
