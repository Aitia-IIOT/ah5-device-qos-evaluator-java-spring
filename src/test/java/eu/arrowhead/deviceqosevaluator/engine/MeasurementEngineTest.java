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
package eu.arrowhead.deviceqosevaluator.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.data.util.Pair;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.arrowhead.common.Constants;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.ArrowheadHttpService;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.SystemDbService;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.AugmentedMeasurementJobScheduler;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.RttMeasurementJobScheduler;
import eu.arrowhead.dto.PageDTO;
import eu.arrowhead.dto.SystemListResponseDTO;
import eu.arrowhead.dto.SystemQueryRequestDTO;
import eu.arrowhead.dto.SystemResponseDTO;

@ExtendWith(MockitoExtension.class)
public class MeasurementEngineTest {

	//=================================================================================================
	// members

	@InjectMocks
	private MeasurementEngine engine;

	@Mock
	private ArrowheadHttpService ahHttpService;

	@Mock
	private DeviceDbService deviceDbService;

	@Mock
	private SystemDbService systemDbService;

	@Mock
	private RttMeasurementJobScheduler rttMeasurementJobScheduler;

	@Mock
	private AugmentedMeasurementJobScheduler augmentedMeasurementJobScheduler;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testOrganizeAlreadyWorking() throws ArrowheadException, SchedulerException {
		ReflectionTestUtils.setField(engine, "working", true);

		final Pair<Integer, Integer> result = engine.organize();

		assertNull(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	@Disabled
	public void testOrganizeOk1() throws ArrowheadException, SchedulerException {
		ReflectionTestUtils.setField(engine, "working", false);
		final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(1);
		queryParams.put(Constants.VERBOSE, List.of(String.valueOf(true)));
		final SystemQueryRequestDTO sysRequest1 = new SystemQueryRequestDTO(new PageDTO(null, null, null, null), null, null, null, null, null, null);
		final SystemResponseDTO sysResponse1 = new SystemResponseDTO("TestSystem", null, "1.0.0", List.of(), null, null, null);

		when(ahHttpService.consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest1,
				queryParams)).thenReturn(new SystemListResponseDTO(List.of(sysResponse1), 2));
		
		// TODO: continue after SystemDeviceMap test

		final Pair<Integer, Integer> result = engine.organize();

		verify(ahHttpService).consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest1,
				queryParams);
	}
}