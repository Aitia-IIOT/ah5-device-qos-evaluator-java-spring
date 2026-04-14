/*******************************************************************************
 *
 * Copyright (c) 2026 AITIA
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
package eu.arrowhead.deviceqosevaluator.quartz.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;

import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;

@ExtendWith(MockitoExtension.class)
public class AugmentedMeasurementJobSchedulerTest {

	//=================================================================================================
	// members

	@InjectMocks
	private AugmentedMeasurementJobScheduler testedScheduler;

	@Mock
	private DeviceQoSEvaluatorSystemInfo sysInfo;

	@Mock
	private Scheduler scheduler;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStartDeviceNull() {
		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> testedScheduler.start(null));

		assertEquals("device is null", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testStartDeviceIdNull() {
		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> testedScheduler.start(new Device()));

		assertEquals("device id is null", ex.getMessage());
	}
}