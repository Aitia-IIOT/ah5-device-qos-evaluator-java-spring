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

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

@Service
public class DeviceCollectorJobScheduler {

	//=================================================================================================
	// members

	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;
	
	@Autowired
	private SchedulerFactoryBean schedulerFactory;

	@Resource(name = DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB)
	private JobDetail jobDetail;

	private Scheduler scheduler;
	private Trigger currentTrigger;
	private boolean jobScheduled = false;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@PostConstruct
	public void init() {
		scheduler = schedulerFactory.getScheduler();
	}

	//-------------------------------------------------------------------------------------------------
	public synchronized void startScheduling() throws SchedulerException {
		if (jobScheduled) {
			return;
		}

		currentTrigger = TriggerBuilder.newTrigger()
				.withIdentity(DeviceQoSEvaluatorConstants.DEVICE_COLLECTOR_JOB_TRIGGER, "deviceJobs")
				.forJob(jobDetail)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule()
						.withIntervalInMilliseconds(sysInfo.getDeviceCollectorJobInterval() * 1000) // from sec to milisec
						.repeatForever())
				.build();

		scheduler.scheduleJob(currentTrigger);

		scheduler.start();
		jobScheduled = true;
	}

	//-------------------------------------------------------------------------------------------------
	public synchronized void stopScheduling() throws SchedulerException {
		if (!jobScheduled) {
			return;
		}

		if (currentTrigger != null) {
			scheduler.unscheduleJob(currentTrigger.getKey());
			currentTrigger = null;
		}

		scheduler.deleteJob(jobDetail.getKey());
		jobScheduled = false;
	}
}
