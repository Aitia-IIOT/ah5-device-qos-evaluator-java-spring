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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.arrowhead.common.Constants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.ArrowheadHttpService;
import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Address;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.entity.System;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.SystemDbService;
import eu.arrowhead.dto.PageDTO;
import eu.arrowhead.dto.SystemListResponseDTO;
import eu.arrowhead.dto.SystemQueryRequestDTO;
import eu.arrowhead.dto.enums.AddressType;

@Service
public class DeviceCollectorEngine {

	//=================================================================================================
	// members

	@Autowired
	private ArrowheadHttpService ahHttpService;
	
	@Autowired
	private DeviceDbService deviceDbService;
	
	@Autowired
	private SystemDbService systemDbService;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void refresh() {
		logger.debug("refresh started");

		final SystemDeviceMap systemDeviceMap = acquireSystemsAndDevices();
		
		for (int i = 0; i < systemDeviceMap.getDeviceSize(); ++i) {
			final Set<Address> deviceAddresses = systemDeviceMap.getDeviceAddresses(i);	
			final List<Device> deviceRecords = deviceDbService.findByAddresses(deviceAddresses.stream().map(a -> a.address()).collect(Collectors.toSet()));
			
			Device deviceRecord = null;
			if (Utilities.isEmpty(deviceRecords)) {
				// create new device with systems
				final String address = selectAddress(deviceAddresses);
				if (!Utilities.isEmpty(address)) {
					deviceRecord = deviceDbService.create(address);					
				}
				
			} else {
				
				// should be one, but if not, set systems to one which is active and oldest
			}
			
			// Handle systems
			final Set<String> deviceSystems = systemDeviceMap.getDeviceSystems(i);
			final List<System> toSave = new ArrayList<>(deviceSystems.size()); 
			
			final List<System> systemRecordsByDevice = systemDbService.findByDeviceId(deviceRecord.getId());
			for (final System sysRecord : systemRecordsByDevice) {
				if (!deviceSystems.contains(sysRecord.getName())) {
					sysRecord.setDevice(null);
					toSave.add(sysRecord);
				}
			}
			systemDbService.save(toSave);
			toSave.clear();
			
			final List<System> systemRecords = systemDbService.findByNames(deviceSystems);
			for (final String sysName : deviceSystems) {
				boolean systemExists = false;
				for (final System sysRecord : systemRecords) {
					if (sysRecord.getName().equals(sysName)) {
						sysRecord.setDevice(deviceRecord);
						systemExists = true;
						toSave.add(sysRecord);
						break;
					}
				}
				if (!systemExists) {
					toSave.add(new System(sysName, deviceRecord));
				}
			}
			systemDbService.save(toSave);
		}
		
		// delete systems without device and inactivate device without systems 
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private SystemDeviceMap acquireSystemsAndDevices() {
		logger.debug("acquireSystemsAndDevices started");
		
		final SystemDeviceMap map = new SystemDeviceMap();

		boolean hasMorePage = false;
		int pageNumber = 0;
		Integer pageSize = null;
		final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(1);
		queryParams.put(Constants.VERBOSE, List.of(String.valueOf(true)));

		long counter = 0;
		do {
			final SystemListResponseDTO response = ahHttpService.consumeService(
					Constants.SERVICE_DEF_SERVICE_REGISTRY_MANAGEMENT,
					Constants.SERVICE_OP_QUERY,
					SystemListResponseDTO.class,
					new SystemQueryRequestDTO(new PageDTO(pageNumber == 0 ? null : pageNumber, pageSize, null, null), null, null, null, null, null, null),
					queryParams);

			counter = counter + response.entries().size();
			hasMorePage = counter < response.count();
			pageNumber = hasMorePage ? pageNumber + 1 : pageNumber;
			pageSize = pageSize == null ? response.entries().size() : pageSize;
			
			map.load(response.entries());
		} while (hasMorePage);

		return map;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String selectAddress(final Set<Address> addresses) {
		logger.debug("selectAddress started");
		
		Address selected = null;
		
		for (final Address address : addresses) {
			if (address.type() == AddressType.MAC) {
				continue;
			}
			
			if (selected == null || selected.type() == AddressType.HOSTNAME) {
				selected = address;
			}
			
			if (selected.deviceRelated()) {
				break;
			}			
		}
		
		return selected.address();
	}
}
