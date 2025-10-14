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

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class AugmentedMeasurementJob extends QuartzJobBean {

	//=================================================================================================
	// members

	private UUID deviceId;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.debug("AugmentedMeasurementJob.execute started");

		System.out.println("Augmented measurement execute: " + deviceId);
	}

	//-------------------------------------------------------------------------------------------------
	public void setDeviceId(final UUID deviceId) {
		this.deviceId = deviceId;
	}
}
