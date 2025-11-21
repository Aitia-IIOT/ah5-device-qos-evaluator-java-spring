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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.service.validation.name.SystemNameValidator;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;
import eu.arrowhead.deviceqosevaluator.service.normalization.QualitiyEvaluationNormalization;
import eu.arrowhead.dto.QoSDeviceDataEvaluationConfigDTO;

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
	public void validateQoSEvaluationRequest(final List<String> systems, final QoSDeviceDataEvaluationConfigDTO config, final boolean needThreshold, final String origin) {
		logger.debug("validateQoSEvaluationRequestDTO started");

		if (Utilities.isEmpty(systems)) {
			throw new InvalidParameterException("System list is empty", origin);
		}

		if (Utilities.containsNullOrEmpty(systems)) {
			throw new InvalidParameterException("System list contains empty element", origin);
		}

		if (config == null) {
			throw new InvalidParameterException("Configuration payload is missing", origin);
		}

		if (Utilities.isEmpty(config.metricNames())) {
			throw new InvalidParameterException("Metric names configuration is empty", origin);
		}

		if (Utilities.containsNullOrEmpty(config.metricNames())) {
			throw new InvalidParameterException("Metric names configuration contains empty element", origin);
		}

		if (!Utilities.isEmpty(config.metricWeights())) {
			if (Utilities.containsNull(config.metricWeights())) {
				throw new InvalidParameterException("Metric weights configuration contains empty element", origin);
			}

			if (config.metricNames().size() != config.metricWeights().size()) {
				throw new InvalidParameterException("Metric names and weights configuration lists have different size", origin);
			}
		}

		if (needThreshold) {
			if (config.threshold() == null) {
				throw new InvalidParameterException("Threshold configuration is missing", origin);
			}

			if (config.threshold() < 0 || config.threshold() > 100) {
				throw new InvalidParameterException("Invalid threshold configuration, must be between 0 and 100", origin);
			}
		}

		if (config.timeWindow() != null) {
			if (config.timeWindow() <= 0) {
				throw new InvalidParameterException("Invalid time window configuration, must be greater than 0", origin);
			}

			if (config.timeWindow() > sysInfo.getEvaluationTimeWindow()) {
				throw new InvalidParameterException("Invalid time window configuration, must be not greater than " + sysInfo.getEvaluationTimeWindow(), origin);
			}
		}
	}

	// NORMALIZATION

	//-------------------------------------------------------------------------------------------------
	public Pair<List<String>, QoSDeviceDataEvaluationConfigDTO> validateAndNormalizeQoSEvaluationRequest(final List<String> systems, final QoSDeviceDataEvaluationConfigDTO config, final boolean needThreshold, final String origin) {
		logger.debug("validateAndNormalizeQoSEvaluationRequestDTO started");

		validateQoSEvaluationRequest(systems, config, needThreshold, origin);
		final List<String> normalizedSystemNames = normalizator.normalizeSystemNames(systems);
		final QoSDeviceDataEvaluationConfigDTO normalizedConfig = normalizator.normalizeQoSDeviceDataEvaluationConfigDTO(config);

		try {
			for (final String provider : normalizedSystemNames) {
				systemNameValidator.validateSystemName(provider);
			}
		} catch (final InvalidParameterException ex) {
			throw new InvalidParameterException(ex.getMessage(), origin);
		}

		for (final String metricName : normalizedConfig.metricNames()) {
			final String[] split = metricName.split(DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER);
			if (!Utilities.isEnumValue(split[0] + DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER + split[1], OidGroup.class)
					|| !Utilities.isEnumValue(split[2], OidMetric.class)) {
				throw new InvalidParameterException("Invalid metric name " + metricName, origin);
			}
		}

		return Pair.of(normalizedSystemNames, normalizedConfig);
	}
}
