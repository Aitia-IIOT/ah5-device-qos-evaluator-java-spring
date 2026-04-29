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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Triple;
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
import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Address;
import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Bool;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.entity.System;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.SystemDbService;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.AugmentedMeasurementJobScheduler;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.RttMeasurementJobScheduler;
import eu.arrowhead.dto.AddressDTO;
import eu.arrowhead.dto.PageDTO;
import eu.arrowhead.dto.SystemListResponseDTO;
import eu.arrowhead.dto.SystemQueryRequestDTO;
import eu.arrowhead.dto.SystemResponseDTO;
import eu.arrowhead.dto.enums.AddressType;

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
	@SuppressWarnings("checkstyle:MagicNumber")
	@Test
	@Disabled
	public void testOrganizeOk1() throws ArrowheadException, SchedulerException {
		ReflectionTestUtils.setField(engine, "working", false);
		final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(1);
		queryParams.put(Constants.VERBOSE, List.of(String.valueOf(true)));
		final SystemQueryRequestDTO sysRequest1 = new SystemQueryRequestDTO(new PageDTO(null, null, null, null), null, null, null, null, null, null);
		final AddressDTO address1 = new AddressDTO("IPV4", "10.0.0.1");
		final SystemResponseDTO sysResponse1 = new SystemResponseDTO("TestSystem", null, "1.0.0", List.of(address1), null, null, null);
		final AddressDTO address2 = new AddressDTO("IPV4", "10.0.0.2");
		final SystemResponseDTO sysResponse2 = new SystemResponseDTO("TestSystem2", null, "1.0.0", List.of(address2), null, null, null);
		final SystemQueryRequestDTO sysRequest2 = new SystemQueryRequestDTO(new PageDTO(1, 1, null, null), null, null, null, null, null, null);
		final AddressDTO address3 = new AddressDTO("IPV4", "10.0.0.1");
		final SystemResponseDTO sysResponse3 = new SystemResponseDTO("TestSystem3", null, "1.0.0", List.of(address3), null, null, null);

		when(ahHttpService.consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest1,
				queryParams)).thenReturn(new SystemListResponseDTO(List.of(sysResponse1, sysResponse2), 3));
		when(ahHttpService.consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest2,
				queryParams)).thenReturn(new SystemListResponseDTO(List.of(sysResponse3), 3));
		when(deviceDbService.findByAddresses(Set.of("10.0.0.1"))).thenReturn(List.of());

		// TODO: continue after arrangeDatabaseAndMeasurements tests

		final Pair<Integer, Integer> result = engine.organize();

		verify(ahHttpService).consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest1,
				queryParams);
		verify(ahHttpService).consumeService(
				"serviceRegistryManagement",
				"system-query",
				SystemListResponseDTO.class,
				sysRequest2,
				queryParams);
		verify(deviceDbService).findByAddresses(Set.of("10.0.0.1"));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testArrangeDatabaseAndMeasurements1() throws SchedulerException {
		final SystemDeviceMap map = new SystemDeviceMap();
		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		devices.put(0, Triple.of(
				Set.of(new Address("", AddressType.HOSTNAME, false)),
				Set.of("TestSystem"),
				new Bool()));
		ReflectionTestUtils.setField(map, "devices", devices);

		when(deviceDbService.findByAddresses(Set.of(""))).thenReturn(List.of());

		final Object resultObj = ReflectionTestUtils.invokeMethod(engine, "arrangeDatabaseAndMeasurements", map);

		assertEquals(0, resultObj);

		verify(deviceDbService).findByAddresses(Set.of(""));
		verify(deviceDbService, never()).create("", false);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testArrangeDatabaseAndMeasurements2() throws SchedulerException {
		final SystemDeviceMap map = new SystemDeviceMap();
		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		devices.put(0, Triple.of(
				Set.of(new Address("10.0.0.1", AddressType.IPV4, false)),
				Set.of("TestSystem"),
				new Bool()));
		ReflectionTestUtils.setField(map, "devices", devices);
		final UUID deviceId = UUID.fromString("9ef06aec-7865-48c0-b456-9f6faab47c22");
		final Device device = new Device(deviceId, "10.0.0.1", null, false, false);
		final System system = new System("TestSystem", device);

		when(deviceDbService.findByAddresses(Set.of("10.0.0.1"))).thenReturn(List.of());
		when(deviceDbService.create("10.0.0.1", false)).thenReturn(device);
		doNothing().when(rttMeasurementJobScheduler).start(device);
		when(systemDbService.findByDeviceId(deviceId)).thenReturn(List.of());
		when(systemDbService.findByNames(Set.of("TestSystem"))).thenReturn(List.of());
		doNothing().when(systemDbService).save(List.of(system));

		final Object resultObj = ReflectionTestUtils.invokeMethod(engine, "arrangeDatabaseAndMeasurements", map);

		assertEquals(1, resultObj);

		verify(deviceDbService).findByAddresses(Set.of("10.0.0.1"));
		verify(deviceDbService).create("10.0.0.1", false);
		verify(rttMeasurementJobScheduler).start(device);
		verify(augmentedMeasurementJobScheduler, never()).start(device);
		verify(systemDbService).findByDeviceId(deviceId);
		verify(systemDbService).save(List.of(system));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAddress1() {
		final Address address1 = new Address("mac", AddressType.MAC, false);
		final Address address2 = new Address("example.com", AddressType.HOSTNAME, false);
		final Address address3 = new Address("10.0.0.14", AddressType.IPV4, false);
		final Set<Address> set = new LinkedHashSet<>(3);
		set.add(address1);
		set.add(address2);
		set.add(address3);

		final Object resultObj = ReflectionTestUtils.invokeMethod(engine, "selectAddress", set);

		assertEquals("10.0.0.14", resultObj);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAddress2() {
		final Address address1 = new Address("10.0.0.14", AddressType.IPV4, false);
		final Address address2 = new Address("example.com", AddressType.HOSTNAME, false);
		final Set<Address> set = new LinkedHashSet<>(3);
		set.add(address1);
		set.add(address2);

		final Object resultObj = ReflectionTestUtils.invokeMethod(engine, "selectAddress", set);

		assertEquals("10.0.0.14", resultObj);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testSelectAddress3() {
		final Address address1 = new Address("example.com", AddressType.HOSTNAME, true);
		final Address address2 = new Address("10.0.0.14", AddressType.IPV4, true);
		final Address address3 = new Address("10.0.0.15", AddressType.IPV4, false);

		final Set<Address> set = new LinkedHashSet<>(3);
		set.add(address1);
		set.add(address2);
		set.add(address3);

		final Object resultObj = ReflectionTestUtils.invokeMethod(engine, "selectAddress", set);

		assertEquals("10.0.0.14", resultObj);
	}
}