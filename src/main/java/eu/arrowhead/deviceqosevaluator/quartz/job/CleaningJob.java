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

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class CleaningJob extends QuartzJobBean {

	//=================================================================================================
	// members
	
	//=================================================================================================
	// methods
	
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		System.out.println("cleaning job..."); // TODO
		// Set devices to inactive if no system is associated
		// Delete inactive devices after a certain age compared to updatedAt
	}
}
