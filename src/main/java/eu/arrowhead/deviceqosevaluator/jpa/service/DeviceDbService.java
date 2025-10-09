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
package eu.arrowhead.deviceqosevaluator.jpa.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.deviceqosevaluator.jpa.entity.Device;
import eu.arrowhead.deviceqosevaluator.jpa.repository.DeviceRepository;

@Service
public class DeviceDbService {

	//=================================================================================================
	// members
	
	@Autowired
	private DeviceRepository deviceRepo;
	
	private final Logger logger = LogManager.getLogger(this.getClass());
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<Device> findByAddresses(final Set<String> addresses) {
		logger.debug("findByAddresses started");
		
		return deviceRepo.findAllByAddressIn(addresses);
	}
	
	//-------------------------------------------------------------------------------------------------
	public Device create(final String address) {
		logger.debug("create started");
		
		return deviceRepo.saveAndFlush(new Device(UUID.randomUUID(), address, null, false, false));
	}
}
