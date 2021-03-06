/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.plugin.policyevaluator;


import java.util.Map;
import java.util.Set;

import org.apache.ranger.plugin.conditionevaluator.RangerConditionEvaluator;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.apache.ranger.plugin.model.RangerPolicy.RangerPolicyResource;
import org.apache.ranger.plugin.model.RangerServiceDef;
import org.apache.ranger.plugin.policyengine.RangerAccessRequest;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.apache.ranger.plugin.policyengine.RangerAccessResource;
import org.apache.ranger.plugin.policyengine.RangerPolicyEngineOptions;

public interface RangerPolicyEvaluator extends Comparable<RangerPolicyEvaluator> {
	public static final String EVALUATOR_TYPE_DEFAULT   = "default";
	public static final String EVALUATOR_TYPE_OPTIMIZED = "optimized";
	public static final String EVALUATOR_TYPE_CACHED    = "cached";

	void init(RangerPolicy policy, RangerServiceDef serviceDef, RangerPolicyEngineOptions options);

	RangerPolicy getPolicy();

	RangerServiceDef getServiceDef();

	Map<String, RangerConditionEvaluator> getConditionEvaluators();

	int getEvalOrder();

	void evaluate(RangerAccessRequest request, RangerAccessResult result);

	boolean isMatch(RangerAccessResource resource);

	boolean isSingleAndExactMatch(RangerAccessResource resource);

	boolean isAccessAllowed(RangerAccessResource resource, String user, Set<String> userGroups, String accessType);

	boolean isAccessAllowed(Map<String, RangerPolicyResource> resources, String user, Set<String> userGroups, String accessType);
}
