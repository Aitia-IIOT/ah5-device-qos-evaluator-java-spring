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
package eu.arrowhead.deviceqosevaluator;

import eu.arrowhead.common.Defaults;

public class DeviceQoSEvaluatorDefaults extends Defaults {
	
	//=================================================================================================
	// members
	
	public static final String DEVICE_COLLECTOR_JOB_INTERVAL_DEFAULT = "30"; // sec
	public static final String AUGMENTED_MEASUEREMENT_JOB_INTERVAL_DEFAULT = "10"; // sec
	public static final String CLEANING_JOB_INTERVAL_DEFAULT = "60"; // sec
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private DeviceQoSEvaluatorDefaults() {
		throw new UnsupportedOperationException();
	}
}
