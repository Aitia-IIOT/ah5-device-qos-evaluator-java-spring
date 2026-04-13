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
package eu.arrowhead.deviceqosevaluator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.arrowhead.common.service.PageService;
import eu.arrowhead.deviceqosevaluator.dto.DTOConverter;
import eu.arrowhead.deviceqosevaluator.engine.MeasurementEngine;
import eu.arrowhead.deviceqosevaluator.jpa.service.StatDbService;
import eu.arrowhead.deviceqosevaluator.service.validation.DeviceQualityDataManagementValidation;

@ExtendWith(MockitoExtension.class)
public class DeviceQualityDataManagementServiceTest {

	//=================================================================================================
	// members

	@InjectMocks
	private DeviceQualityDataManagementService service;

	@Mock
	private StatDbService statDbService;

	@Mock
	private MeasurementEngine measurementEngine;

	@Mock
	private PageService pageService;

	@Mock
	private DeviceQualityDataManagementValidation validator;

	@Mock
	private DTOConverter dtoConverter;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryOriginNull() {
		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.query(null, null));

		assertEquals("origin is empty", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testQueryOriginEmpty() {
		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.query(null, ""));

		assertEquals("origin is empty", ex.getMessage());
	}
}