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
package eu.arrowhead.deviceqosevaluator.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorConstants;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.deviceqosevaluator.engine.StatisticsEngine;
import eu.arrowhead.deviceqosevaluator.enums.OidGroup;
import eu.arrowhead.deviceqosevaluator.enums.OidMetric;
import eu.arrowhead.deviceqosevaluator.service.model.OidMetricModel;
import eu.arrowhead.deviceqosevaluator.service.model.SystemEvalModel;
import eu.arrowhead.deviceqosevaluator.service.validation.QualitiyEvaluationValidation;
import eu.arrowhead.dto.QoSEvaluationFilterResponseDTO;
import eu.arrowhead.dto.QoSEvaluationRequestDTO;
import eu.arrowhead.dto.QoSEvaluationSortResponseDTO;

@Service
public class QualityEvaluationService {

	//=================================================================================================
	// members
	
	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;
	
	@Autowired
	private QualitiyEvaluationValidation validator;
	
	@Autowired
	private StatisticsEngine statEngine;

	private final Logger logger = LogManager.getLogger(this.getClass());

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public QoSEvaluationFilterResponseDTO filter(final QoSEvaluationRequestDTO dto, final String origin) {
		logger.debug("filter started");

		final QoSEvaluationRequestDTO normalized = validator.validateAndNormalizeQoSEvaluationRequest(dto, true, origin);
		
		final List<SystemEvalModel> evaluated = evaluateRequest(normalized);
		final List<String> passedProviders = new ArrayList<>();
		final List<String> droppedProviders = new ArrayList<>();
		final Map<String, List<String>> warnings = new HashMap<>();
		
		for (final SystemEvalModel sysResult : evaluated) {
			if (sysResult.getScore() <= normalized.configuration().threshold()) {
				passedProviders.add(sysResult.getName());
			} else {
				droppedProviders.add(sysResult.getName());
			}
			
			if (!Utilities.isEmpty(sysResult.getNoStat())) {
				warnings.put(sysResult.getName(), sysResult.getNoStat().stream().map(item -> item.name()).toList());
			}
		}
		
		return new QoSEvaluationFilterResponseDTO(passedProviders, droppedProviders, warnings);
	}

	//-------------------------------------------------------------------------------------------------
	public QoSEvaluationSortResponseDTO sort(final QoSEvaluationRequestDTO dto, final String origin) {
		logger.debug("sort started");

		final QoSEvaluationRequestDTO normalized = validator.validateAndNormalizeQoSEvaluationRequest(dto, false, origin);
		
		final List<SystemEvalModel> evaluated = evaluateRequest(normalized);
		final List<String> sortedProviders = new ArrayList<>();
		final Map<String, List<String>> warnings = new HashMap<>();
		
		evaluated.sort(Comparator.comparingDouble(SystemEvalModel::getScore)); //ascending
		for (final SystemEvalModel sysResult : evaluated) {
			sortedProviders.add(sysResult.getName());
			if (!Utilities.isEmpty(sysResult.getNoStat())) {
				warnings.put(sysResult.getName(), sysResult.getNoStat().stream().map(item -> item.name()).toList());
			}
		}
		
		return new QoSEvaluationSortResponseDTO(sortedProviders, warnings);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private List<SystemEvalModel> evaluateRequest(final QoSEvaluationRequestDTO normalized) {
		logger.debug("evaluateRequest started");
		
		final Set<String> providers = new HashSet<>(normalized.providers());
		final List<OidMetricModel> metricModels = parseMetricModels(normalized.configuration().metricNames(), normalized.configuration().metricWeights());
		long timeWindow = normalized.configuration().timeWindow() == null ? sysInfo.getEvaluationTimeWindow() : normalized.configuration().timeWindow();
		if (timeWindow < sysInfo.getAugmentedMeasurementJobInterval()) {
			timeWindow = sysInfo.getAugmentedMeasurementJobInterval();
		}
		
		return statEngine.evaluate(providers, metricModels, timeWindow);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OidMetricModel> parseMetricModels(final List<String> metricNames, final List<Double> metricWeights) {
		logger.debug("parseMetricModels started");
		
		final Map<OidGroup, OidMetricModel> metricModels = new HashMap<>();
		for (int i = 0; i < metricNames.size(); ++i) {
			final String[] split = metricNames.get(i).toUpperCase().split(DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER);
			final OidGroup oidGroup = OidGroup.valueOf(split[0] + DeviceQoSEvaluatorConstants.OID_NAME_DELIMITER + split[1]);
			metricModels.putIfAbsent(oidGroup, new OidMetricModel(oidGroup));
			metricModels.get(oidGroup).getMetricWeight().put(OidMetric.valueOf(split[2]), metricWeights.get(i));
		}
		
		return new ArrayList<>(metricModels.values());
	}
}
