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
package eu.arrowhead.deviceqosevaluator.enums;

public enum OidMetric {

	//=================================================================================================
	// members
	
	MINIMUM("1"), MAXIMUM("2"), MEAN("3"), MEDIAN("4"), CURRENT("5");
	
	private String value;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	private OidMetric(String value) {
		this.value = value;
	}

	//-------------------------------------------------------------------------------------------------
	public String getValue() {
		return value;
	}
}
