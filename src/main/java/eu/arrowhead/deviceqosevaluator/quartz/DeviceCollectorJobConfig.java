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

package eu.arrowhead.deviceqosevaluator.quartz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;

@Configuration
public class DeviceCollectorJobConfig {

	//=================================================================================================
	// members

	@Value(DeviceQoSEvaluatorConstants.$DEVICE_COLLECTOR_JOB_INTERVAL_WD)
	private long interval;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Bean(DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB)
	JobDetailFactoryBean deviceCollectorJobDetail() {
		final JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
		jobDetailFactory.setJobClass(DeviceCollectorJob.class);
		jobDetailFactory.setDescription("Refeshing the device table");
		jobDetailFactory.setDurability(true);

		return jobDetailFactory;
	}
}
