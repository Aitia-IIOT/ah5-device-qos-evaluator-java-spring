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

import javax.naming.ConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.init.ApplicationInitListener;
import eu.arrowhead.deviceqosevaluator.quartz.DeviceCollectorJobScheduler;

@Component
public class DeviceQoSEvaluatorApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private DeviceCollectorJobScheduler deviceCollectorJobScheduler;
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(ContextRefreshedEvent event) throws InterruptedException, ConfigurationException {
		try {
			deviceCollectorJobScheduler.startScheduling();
			logger.info("Device collection job has been started");
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
			deviceCollectorJobScheduler.stopScheduling();
			logger.info("Device collection job has been terminated");
		} catch (SchedulerException ex) {
			logger.error("Error while terminating device collection job scheduling");
			logger.debug(ex);
		}
	}
	
	
}
