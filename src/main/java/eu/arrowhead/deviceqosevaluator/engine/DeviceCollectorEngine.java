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
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.arrowhead.common.Constants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.http.ArrowheadHttpService;
import eu.arrowhead.deviceqosevaluator.engine.SystemDeviceMap.Address;
import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.entity.System;
import eu.arrowhead.deviceqosevaluator.jpa.service.DeviceDbService;
import eu.arrowhead.deviceqosevaluator.jpa.service.SystemDbService;
import eu.arrowhead.deviceqosevaluator.quartz.scheduler.AugmentedMeasurementJobScheduler;
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
	
	@Autowired
	private AugmentedMeasurementJobScheduler augmentedMeasurementJobScheduler;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public void refresh() throws SchedulerException, ArrowheadException {
		logger.debug("refresh started");

		final SystemDeviceMap systemDeviceMap = acquireSystemsAndDevices();
		final List<Device> newOnesToMeasureAugmented = updateDatabase(systemDeviceMap);
		startNewAugmentedMeasurements(newOnesToMeasureAugmented);
		cleanDatabase();
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
					Constants.SERVICE_OP_SYSTEM_QUERY,
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
	private List<Device> updateDatabase(final SystemDeviceMap systemDeviceMap) throws SchedulerException {
		logger.debug("updateDatabase started");
		
		
		final List<Device> startToMeasureAugmented = new ArrayList<>();
		for (int i = 0; i < systemDeviceMap.getDeviceSize(); ++i) {
			
			// Handle devices
			final Set<Address> deviceAddresses = systemDeviceMap.getDeviceAddresses(i);	
			final List<Device> deviceRecords = deviceDbService.findByAddresses(deviceAddresses.stream().map(a -> a.address()).collect(Collectors.toSet()));
			
			Device deviceRecord = null;
			if (Utilities.isEmpty(deviceRecords)) {
				// Create new device
				final String address = selectAddress(deviceAddresses);
				if (!Utilities.isEmpty(address)) {
					deviceRecord = deviceDbService.create(address, systemDeviceMap.hasAugmented(i));
					if (systemDeviceMap.hasAugmented(i)) {
						startToMeasureAugmented.add(deviceRecord);						
					}
				}				
			} else {
				// Existing device. Should be only one but shit happens...
				deviceRecord = specifyDevice(deviceRecords);
				boolean needUpdate = false;
				if (deviceRecord.isAugmented() != systemDeviceMap.hasAugmented(i)) {
					deviceRecord.setAugmented(systemDeviceMap.hasAugmented(i));
					needUpdate = true;
				}
				if (deviceRecord.isInactive()) {
					deviceRecord.setInactive(false);
					needUpdate = true;
				}
				if (needUpdate) {
					deviceDbService.update(deviceRecord);					
				}
				if (!augmentedMeasurementJobScheduler.isScheduled(deviceRecord)) { // TODO need to stop measuring tha ones that no longer supports augmented
					startToMeasureAugmented.add(deviceRecord);
				}
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
		
		return startToMeasureAugmented;
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
			
			if (selected.deviceRelated() && selected.type() != AddressType.HOSTNAME) {
				break;
			}			
		}
		
		return selected.address();
	}
	
	//-------------------------------------------------------------------------------------------------
	private Device specifyDevice(final List<Device> devices) {
		logger.debug("selectDevices started");
		
		Device selected = devices.getFirst();
		
		if (devices.size() > 1) {
			for (final Device device : devices) {
				if (selected.isInactive() && !selected.isInactive()) {
					selected = device;
				} else if (!device.isInactive() && (selected.getCreatedAt().isAfter(device.getCreatedAt()))) {
					selected = device;
				}
			}
		}
		
		return selected;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void startNewAugmentedMeasurements(final List<Device> devices) throws SchedulerException {
		logger.debug("startNewMeasurements started");
		
		for (final Device device : devices) {
			augmentedMeasurementJobScheduler.start(device);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void cleanDatabase() {
		logger.debug("cleanDatabase started");
		
		systemDbService.deleteSystemsWithoutDevice();
	}

}
