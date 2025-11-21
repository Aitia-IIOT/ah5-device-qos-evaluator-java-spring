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
package eu.arrowhead.deviceqosevaluator.service.normalization;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.service.validation.name.SystemNameNormalizer;
import eu.arrowhead.deviceqosevaluator.DeviceQoSEvaluatorSystemInfo;
import eu.arrowhead.dto.QoSDeviceDataEvaluationConfigDTO;

@Service
public class QualitiyEvaluationNormalization {

	//=================================================================================================
	// members

	@Autowired
	private DeviceQoSEvaluatorSystemInfo sysInfo;

	@Autowired
	private SystemNameNormalizer systemNameNormalizer;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<String> normalizeSystemNames(final List<String> names) {
		Assert.notNull(names, "names is null");

		return names.stream().map(n -> systemNameNormalizer.normalize(n)).toList();
	}

	//-------------------------------------------------------------------------------------------------
	public QoSDeviceDataEvaluationConfigDTO normalizeQoSDeviceDataEvaluationConfigDTO(final QoSDeviceDataEvaluationConfigDTO dto) {
		Assert.notNull(dto, "dto is null");

		return new QoSDeviceDataEvaluationConfigDTO(
				dto.metricNames().stream().map(mn -> mn.toUpperCase().trim()).toList(),
				normalizeMetricWeights(dto.metricWeights(), dto.metricNames().size()),
				normalizeTimeWindow(dto.timeWindow()),
				dto.threshold());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<Double> normalizeMetricWeights(final List<Double> weights, final int size) {
		final List<Double> normalized = new ArrayList<>(size);

		if (Utilities.isEmpty(weights)) {
			final double w = 1 / size;
			for (int i = 0; i < size; ++i) {
				normalized.add(w);
			}
			return normalized;
		}

		double sum = 0;
		for (final Double w : weights) {
			sum += w;
		}

		if (sum < 0.9d || sum > 1.1d) {
			for (final Double w : weights) {
				normalized.add(w / sum);
			}
		}

		return normalized;
	}

	//-------------------------------------------------------------------------------------------------
	private int normalizeTimeWindow(final Integer window) {
		if (window == null) {
			return (int) sysInfo.getEvaluationTimeWindow();
		}

		if (window < sysInfo.getAugmentedMeasurementJobInterval()) {
			return (int) sysInfo.getAugmentedMeasurementJobInterval();
		}

		return window;
	}
}
