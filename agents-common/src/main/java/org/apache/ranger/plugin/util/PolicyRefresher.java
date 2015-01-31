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

package org.apache.ranger.plugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ranger.plugin.policyengine.RangerPolicyEngine;
import org.apache.ranger.plugin.store.ServiceStore;


public class PolicyRefresher extends Thread {
	private static final Log LOG = LogFactory.getLog(PolicyRefresher.class);

	private RangerPolicyEngine policyEngine      = null;
	private String             serviceType       = null;
	private String             serviceName       = null;
	private ServiceStore       serviceStore      = null;
	private long               pollingIntervalMs = 30 * 1000;

	private boolean         shutdownFlag      = false;
	private ServicePolicies lastKnownPolicies = null;


	public PolicyRefresher(RangerPolicyEngine policyEngine, String serviceType, String serviceName, ServiceStore serviceStore, long pollingIntervalMs, String cacheDir) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("==> PolicyRefresher.PolicyRefresher(serviceName=" + serviceName + ")");
		}

		this.policyEngine      = policyEngine;
		this.serviceType       = serviceType;
		this.serviceName       = serviceName;
		this.serviceStore      = serviceStore;
		this.pollingIntervalMs = pollingIntervalMs;

		if(LOG.isDebugEnabled()) {
			LOG.debug("<== PolicyRefresher.PolicyRefresher(serviceName=" + serviceName + ")");
		}
	}

	/**
	 * @return the policyEngine
	 */
	public RangerPolicyEngine getPolicyEngine() {
		return policyEngine;
	}

	/**
	 * @return the serviceType
	 */
	public String getServiceType() {
		return serviceType;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @return the serviceStore
	 */
	public ServiceStore getServiceStore() {
		return serviceStore;
	}

	/**
	 * @return the pollingIntervalMilliSeconds
	 */
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}

	/**
	 * @param pollingIntervalMilliSeconds the pollingIntervalMilliSeconds to set
	 */
	public void setPollingIntervalMilliSeconds(long pollingIntervalMilliSeconds) {
		this.pollingIntervalMs = pollingIntervalMilliSeconds;
	}

	public void startRefresher() {
		shutdownFlag = false;

		super.start();
	}

	public void stopRefresher() {
		shutdownFlag = true;
	}

	public void run() {
		if(LOG.isDebugEnabled()) {
			LOG.debug("==> PolicyRefresher.run()");
		}

		while(! shutdownFlag) {
			try {
				long lastKnownVersion = (lastKnownPolicies == null || lastKnownPolicies.getPolicyVersion() == null) ? 0 : lastKnownPolicies.getPolicyVersion().longValue();

				ServicePolicies svcPolicies = serviceStore.getServicePoliciesIfUpdated(serviceName, lastKnownVersion);

				long newVersion = (svcPolicies == null || svcPolicies.getPolicyVersion() == null) ? 0 : svcPolicies.getPolicyVersion().longValue();

				boolean isUpdated = newVersion != 0 && lastKnownVersion != newVersion;

				if(isUpdated) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("PolicyRefresher(serviceName=" + serviceName + ").run(): found updated version. lastKnownVersion=" + lastKnownVersion + "; newVersion=" + newVersion);
					}

					policyEngine.setPolicies(serviceName, svcPolicies.getServiceDef(), svcPolicies.getPolicies());
					
					lastKnownPolicies = svcPolicies;
				} else {
					if(LOG.isDebugEnabled()) {
						LOG.debug("PolicyRefresher(serviceName=" + serviceName + ").run(): no update found. lastKnownVersion=" + lastKnownVersion + "; newVersion=" + newVersion);
					}
				}
			} catch(Exception excp) {
				LOG.error("PolicyRefresher(serviceName=" + serviceName + ").run(): ", excp);
			}

			try {
				Thread.sleep(pollingIntervalMs);
			} catch(Exception excp) {
				LOG.error("PolicyRefresher(serviceName=" + serviceName + ").run(): error while sleep. exiting thread", excp);

				throw new RuntimeException(excp);
			}
		}

		if(LOG.isDebugEnabled()) {
			LOG.debug("<== PolicyRefresher.run()");
		}
	}
}