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
package eu.arrowhead.deviceqosevaluator.service.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.service.validation.name.SystemNameValidator;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;
import eu.arrowhead.deviceqosevaluator.service.normalization.QualitiyEvaluationNormalization;
import eu.arrowhead.dto.QoSEvaluationRequestDTO;

@Service
public class QualitiyEvaluationValidation {

	//=================================================================================================
	// members
	
	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;
	
	@Autowired
	private QualitiyEvaluationNormalization normalizator;
	
	@Autowired
	private SystemNameValidator systemNameValidator;
	
	private final Logger logger = LogManager.getLogger(this.getClass());
	
	//=================================================================================================
	// methods
	
	// VALIDATION
	
	//-------------------------------------------------------------------------------------------------
	public void validateQoSEvaluationRequest(final QoSEvaluationRequestDTO dto, final boolean needThreshold, final String origin) {
		logger.debug("validateQoSEvaluationRequestDTO started");
		
		if (dto == null) {
			throw new InvalidParameterException("Request payload is missing", origin);
		}
		
		if (Utilities.isEmpty(dto.providers())) {
			throw new InvalidParameterException("Provider list is empty", origin);
		}
		
		if (Utilities.containsNullOrEmpty(dto.providers())) {
			throw new InvalidParameterException("Provider list contains empty element", origin);
		}
		
		if (dto.configuration() == null) {
			throw new InvalidParameterException("Configuration is missing");
		}
		
		if (Utilities.isEmpty(dto.configuration().metricNames())) {
			throw new InvalidParameterException("Metric names configuration is empty", origin);
		}
		
		if (Utilities.containsNullOrEmpty(dto.configuration().metricNames())) {
			throw new InvalidParameterException("Metric names configuration contains empty element", origin);
		}
		
		if (!Utilities.isEmpty(dto.configuration().metricWeights())) {
			if (Utilities.containsNull(dto.configuration().metricWeights())) {
				throw new InvalidParameterException("Metric weights configuration contains empty element", origin);
			}
			
			if (dto.configuration().metricNames().size() != dto.configuration().metricWeights().size()) {
				throw new InvalidParameterException("Metric names and weights configuration lists have different size", origin);
			}			
		}
		
		if (needThreshold) {
			if (dto.configuration().threshold() == null) {
				throw new InvalidParameterException("Threshold configuration is missing", origin);
			}
			
			if (dto.configuration().threshold() < 0 || dto.configuration().threshold() > 100) {
				throw new InvalidParameterException("Invalid threshold configuration, must be between 0 and 100", origin);
			}
		}
		
		if (dto.configuration().timeWindow() != null) {
			if (dto.configuration().timeWindow() <= 0) {
				throw new InvalidParameterException("Invalid time window configuration, must be greater than 0", origin);
			}
			
			if (dto.configuration().timeWindow() > sysInfo.getEvaluationTimeWindow()) {
				throw new InvalidParameterException("Invalid time window configuration, must be not greater than " + sysInfo.getEvaluationTimeWindow(), origin);
			}
		}
	}
	
	// NORMALIZATION
	
	//-------------------------------------------------------------------------------------------------
	public QoSEvaluationRequestDTO validateAndNormalizeQoSEvaluationRequest(final QoSEvaluationRequestDTO dto, final boolean needThreshold, final String origin) {
		logger.debug("validateAndNormalizeQoSEvaluationRequestDTO started");
		
		validateQoSEvaluationRequest(dto, needThreshold, origin);		
		final QoSEvaluationRequestDTO normalized = normalizator.normalizeQoSEvaluationRequestDTO(dto);
		
		try {
			for (final String provider : normalized.providers()) {
				systemNameValidator.validateSystemName(provider);
			}			
		} catch (final InvalidParameterException ex) {
			throw new InvalidParameterException(ex.getMessage(), origin);
		}
		
		for (final String metricName : dto.configuration().metricNames()) {
			final String[] split = metricName.split(DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER);
			if (!Utilities.isEnumValue(split[0] + DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER + split[1], OidGroup.class)
					|| !Utilities.isEnumValue(split[2], OidMetric.class)) {
				throw new InvalidParameterException("Invalid metric name " + metricName, origin);
			}
		}
		
		return normalized;
	}
}
