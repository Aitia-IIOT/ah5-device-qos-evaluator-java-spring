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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InternalServerError;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.jpa.entity.StatCpuTotalLoad;
import eu.arrowhead.deviceqosevaluator.jpa.entity.StatMemoryUsed;
import eu.arrowhead.deviceqosevaluator.jpa.entity.StatRoundTripTime;
import eu.arrowhead.deviceqosevaluator.jpa.entity.mapped.StatEntity;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatCpuTotalLoadRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatMemoryUsedRepository;
import eu.arrowhead.deviceqosevaluator.jpa.repository.StatRoundTripTimeRepository;

@Service
public class StatDbService {

	//=================================================================================================
	// members
	
	@Autowired
	private StatRoundTripTimeRepository rttStatRepo;

	@Autowired
	private StatCpuTotalLoadRepository cpuStatRepo;

	@Autowired
	private StatMemoryUsedRepository memoryStatRepo;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void save(final ZonedDateTime timestamp, final OidGroup oidGroup, final UUID deviceId, final List<Double> data) {
		logger.debug("save started");
		Assert.notNull(timestamp, "timestamp is null");
		Assert.notNull(oidGroup, "oidGroup is null");
		Assert.notNull(deviceId, "deviceId is null");
		Assert.isTrue(!Utilities.isEmpty(data), "data is empty");
		Assert.isTrue(!Utilities.containsNull(data), "data contains null element");
		Assert.isTrue(data.size() == 5, "data list has invalid size");

		try {
			switch (oidGroup) {
			case RTT:
				rttStatRepo.saveAndFlush(new StatRoundTripTime(deviceId, timestamp, data.get(0), data.get(1), data.get(2), data.get(3), data.get(4)));
				break;
			case CPU_TOTAL_LOAD:
				cpuStatRepo.saveAndFlush(new StatCpuTotalLoad(deviceId, timestamp, data.get(0), data.get(1), data.get(2), data.get(3), data.get(4)));
				break;
			case MEMORY_USED:
				memoryStatRepo.saveAndFlush(new StatMemoryUsed(deviceId, timestamp, data.get(0), data.get(1), data.get(2), data.get(3), data.get(4)));
				break;
			}

		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			logger.debug(ex);
			throw new InternalServerError("Database operation error");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public List<StatEntity> getByDeviceIdUntilTimestamp(final OidGroup oidGroup, final UUID deviceId, final ZonedDateTime timestamp) {
		logger.debug("getByDeviceId started");
		Assert.notNull(oidGroup, "oidGroup is null");
		Assert.notNull(deviceId, "deviceId is null");
		
		final List<StatEntity> result = new ArrayList<>();
		try {
			switch (oidGroup) {
			case RTT:
				result.addAll(rttStatRepo.findAllByUuidAndTimestampAfter(deviceId, timestamp));
				break;
			case CPU_TOTAL_LOAD:
				result.addAll(cpuStatRepo.findAllByUuidAndTimestampAfter(deviceId, timestamp));
				break;
			case MEMORY_USED:
				result.addAll(memoryStatRepo.findAllByUuidAndTimestampAfter(deviceId, timestamp));
				break;
			}
			
			return result;
			
		} catch (final Exception ex) {
			logger.error(ex.getMessage());
			logger.debug(ex);
			throw new InternalServerError("Database operation error");
		}
	}
	
}
