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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Address;
import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Bool;
import eu.arrowhead.dto.enums.AddressType;

public class SystemDeviceMapTest {

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetDeviceSize() {
		final SystemDeviceMap map = new SystemDeviceMap();

		assertEquals(0, map.getDeviceSize());

		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		final Bool trueBool = new Bool();
		trueBool.setValue(true);
		devices.put(0, Triple.of(
				Set.of(new Address("localhost", AddressType.HOSTNAME, false)),
				Set.of("TestSystem"),
				trueBool));
		ReflectionTestUtils.setField(map, "devices", devices);

		assertEquals(1, map.getDeviceSize());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetDeviceAddresses() {
		final SystemDeviceMap map = new SystemDeviceMap();
		final Set<Address> addresses = Set.of(new Address("localhost", AddressType.HOSTNAME, false));
		final Bool trueBool = new Bool();
		trueBool.setValue(true);
		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		devices.put(0, Triple.of(
				addresses,
				Set.of("TestSystem"),
				trueBool));
		ReflectionTestUtils.setField(map, "devices", devices);

		assertEquals(addresses, map.getDeviceAddresses(0));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetDeviceSystems() {
		final SystemDeviceMap map = new SystemDeviceMap();
		final Set<Address> addresses = Set.of(new Address("localhost", AddressType.HOSTNAME, false));
		final Bool trueBool = new Bool();
		trueBool.setValue(true);
		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		devices.put(0, Triple.of(
				addresses,
				Set.of("TestSystem"),
				trueBool));
		ReflectionTestUtils.setField(map, "devices", devices);

		assertEquals(Set.of("TestSystem"), map.getDeviceSystems(0));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testHasAugmentetd() {
		final SystemDeviceMap map = new SystemDeviceMap();
		final Set<Address> addresses = Set.of(new Address("localhost", AddressType.HOSTNAME, false));
		final Bool trueBool = new Bool();
		trueBool.setValue(true);
		final HashMap<Integer, Triple<Set<Address>, Set<String>, Bool>> devices = new HashMap<>();
		devices.put(0, Triple.of(
				addresses,
				Set.of("TestSystem"),
				trueBool));
		ReflectionTestUtils.setField(map, "devices", devices);

		assertTrue(map.hasAugmented(0));
	}
}