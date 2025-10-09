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
package eu.arrowhead.deviceqosevaluator.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.dto.AddressDTO;
import eu.arrowhead.dto.SystemResponseDTO;
import eu.arrowhead.dto.enums.AddressType;

public class SystemDeviceMap {

	//=================================================================================================
	// members

	private int nextDeviceIdx = 0;
	private final HashMap<Integer, Set<Address>> deviceAddresses = new HashMap<>();
	private final HashMap<Integer, Set<String>> deviceSystems = new HashMap<>();
	
	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public int getDeviceSize() {
		return deviceAddresses.size();
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<Address> getDeviceAddresses(final int idx) {
		return deviceAddresses.get(idx);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Set<String> getDeviceSystems(final int idx) {
		return deviceSystems.get(idx);
	}

	//-------------------------------------------------------------------------------------------------
	public void load(final List<SystemResponseDTO> systems) {
		logger.debug("load started");

		for (final SystemResponseDTO system : systems) {
			final Set<Address> addresses = collectAddresses(system);
			Integer device = findDevice(addresses);
			
			if (device != null) {
				deviceAddresses.get(device).retainAll(addresses);
			} else {
				device = nextDeviceIdx;
				deviceAddresses.put(device, addresses);
				nextDeviceIdx++;
			}
			
			deviceSystems.putIfAbsent(device, new HashSet<>());
			deviceSystems.get(device).add(system.name());
		}
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Set<Address> collectAddresses(final SystemResponseDTO system) {
		logger.debug("collectAddresses started");

		final Set<Address> addresses = new HashSet<>(system.addresses().size());
		for (final AddressDTO addr : system.addresses()) {
			if (Utilities.isEnumValue(addr.type(), AddressType.class)) {
				addresses.add(new Address(addr.address(), AddressType.valueOf(addr.type()), false));
			}
		}

		if (system.device() != null) {
			for (final AddressDTO addr : system.device().addresses()) {
				if (Utilities.isEnumValue(addr.type(), AddressType.class)) {
					addresses.add(new Address(addr.address(), AddressType.valueOf(addr.type()), true));
				}
			}
		}

		return addresses;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Integer findDevice(final Set<Address> addresses) {
		logger.debug("findDevice started");
		
		for (final Entry<Integer, Set<Address>> entry : deviceAddresses.entrySet()) {
			for (final Address deviceAddr : entry.getValue()) {
				final boolean anyMatch = addresses.stream().anyMatch(addr -> addr.address().equals(deviceAddr.address()));
				if (anyMatch) {
					return entry.getKey();
				}
			}
		}
		
		return null;
	}

	//=================================================================================================
	// nested class

	public record Address(String address, AddressType type, boolean deviceRelated) {
	}
}
