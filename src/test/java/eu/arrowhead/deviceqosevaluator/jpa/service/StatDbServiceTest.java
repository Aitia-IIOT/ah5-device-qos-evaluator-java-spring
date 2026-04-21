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
package eu.arrowhead.deviceqosevaluator.jpa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import eu.arrowhead.common.Constants;
import eu.arrowhead.common.exception.InternalServerError;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.jpa.entity.StatCpuTotalLoad;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatCpuTotalLoadRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatMemoryUsedRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatNetEgressLoadRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatNetIngressLoadRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatRoundTripTimeRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.SystemRepository;

@ExtendWith(MockitoExtension.class)
public class StatDbServiceTest {

	//=================================================================================================
	// members

	@InjectMocks
	private StatDbService service;

	@Mock
	private StatRoundTripTimeRepository rttStatRepo;

	@Mock
	private StatCpuTotalLoadRepository cpuStatRepo;

	@Mock
	private StatMemoryUsedRepository memoryStatRepo;

	@Mock
	private StatNetEgressLoadRepository netEgressStatRepo;

	@Mock
	private StatNetIngressLoadRepository netIngressStatRepo;

	@Mock
	private SystemRepository systemRepo;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveTimestampNull() {
		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(null, null, null, null));

		assertEquals("timestamp is null", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveOidGroupNull() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, null, null, null));

		assertEquals("oidGroup is null", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveDeviceIdNull() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, null, null));

		assertEquals("deviceId is null", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveDataListNull() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));
		final String uuid = "2f0a6b4d-3207-4eec-8694-b44780f18182";
		final UUID deviceId = UUID.fromString(uuid);

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, deviceId, null));

		assertEquals("data is empty", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveDataListEmpty() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));
		final String uuid = "2f0a6b4d-3207-4eec-8694-b44780f18182";
		final UUID deviceId = UUID.fromString(uuid);

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, deviceId, List.of()));

		assertEquals("data is empty", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveDataListContainsNull() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));
		final String uuid = "2f0a6b4d-3207-4eec-8694-b44780f18182";
		final UUID deviceId = UUID.fromString(uuid);
		final List<Double> list = new ArrayList<>(1);
		list.add(null);

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, deviceId, list));

		assertEquals("data contains null element", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSaveDataListInvalidSize() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));
		final String uuid = "2f0a6b4d-3207-4eec-8694-b44780f18182";
		final UUID deviceId = UUID.fromString(uuid);

		final Throwable ex = assertThrows(
				IllegalArgumentException.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, deviceId, List.of(1., 2.)));

		assertEquals("data list has invalid size", ex.getMessage());
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("checkstyle:MagicNumber")
	@Test
	public void testSaveInternalServerError() {
		final ZonedDateTime timestamp = ZonedDateTime.of(2026, 4, 21, 10, 0, 0, 0, ZoneId.of(Constants.UTC));
		final String uuid = "2f0a6b4d-3207-4eec-8694-b44780f18182";
		final UUID deviceId = UUID.fromString(uuid);
		final StatCpuTotalLoad stat = new StatCpuTotalLoad(deviceId, timestamp, 1., 2., 3., 4., 5.);

		when(cpuStatRepo.saveAndFlush(stat)).thenThrow(RuntimeException.class);

		final Throwable ex = assertThrows(
				InternalServerError.class,
				() -> service.save(timestamp, OidGroup.CPU_TOTAL_LOAD, deviceId, List.of(1., 2., 3., 4., 5.)));

		assertEquals("Database operation error", ex.getMessage());

		verify(cpuStatRepo).saveAndFlush(stat);
	}
}