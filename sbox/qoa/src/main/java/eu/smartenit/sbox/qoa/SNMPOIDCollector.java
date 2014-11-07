/**
 * Copyright (C) 2014 The SmartenIT consortium (http://www.smartenit.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.smartenit.sbox.qoa;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.smartenit.sbox.db.dto.BGRouter;
import eu.smartenit.sbox.db.dto.DARouter;
import eu.smartenit.sbox.db.dto.Link;
import eu.smartenit.sbox.db.dto.Tunnel;

/**
 * Helper class for preparing SNMP OID numbers for link and tunnel counters.
 * 
 * @author <a href="mailto:jgutkow@man.poznan.pl">Jakub Gutkowski</a> (<a
 *         href="http://psnc.pl">PSNC</a>)
 * @version 1.0
 * 
 */
public class SNMPOIDCollector {
	private static final Logger logger = LoggerFactory.getLogger(SNMPOIDCollector.class);
	private static final String IF_NAME_OID = "1.3.6.1.2.1.31.1.1.1.1";
	private static final String IF_HC_IN_OCTETS_OID = "1.3.6.1.2.1.31.1.1.1.6";
	private static final String IF_HC_OUT_OCTETS_OID = "1.3.6.1.2.1.31.1.1.1.10";
	
	private SNMPWrapper snmpWrapper;
	private MonitoredLinksInventory links;
	private MonitoredTunnelsInventory tunnels;
	
	/**
	 * The constructor with arguments.
	 * 
	 * @param links
	 *            links to be monitored
	 * @param tunnels
	 *            tunnels to be monitored
	 */
	public SNMPOIDCollector(MonitoredLinksInventory links, MonitoredTunnelsInventory tunnels) {
		this.links = links;
		this.tunnels = tunnels;
		this.snmpWrapper = SNMPWrapperFactory.getInstance();
	}

	/**
	 * Triggers OID information collection for all link counters from all BG
	 * routers.
	 * 
	 * @throws IOException
	 */
	public void collectOIDsForLinks() throws IOException {
		for (BGRouter bgRouter : links.getBGRouters()) {
			logger.debug("Collecting link OIDs from BG router {} ...", bgRouter.getManagementAddress().getPrefix());
			setOIDsForLinks(bgRouter);
			logger.debug("... done.");
		}
	}

	/**
	 * Triggers OID information collection for all tunnel counters from all DA
	 * routers.
	 * 
	 * @throws IOException
	 */
	public void collectOIDsForTunnels() throws IOException {
		for (DARouter daRouter : tunnels.getDARouters()) {
			logger.debug("Collecting tunnel OIDs from DA router {} ...", daRouter.getManagementAddress().getPrefix());
			setOIDsForTunnels(daRouter);
			logger.debug("... done.");
		}
	}

	private void setOIDsForTunnels(DARouter daRouter) throws IOException {
		final List<String> interfaces = getIntrefacesForDARouter(daRouter);
		for (Tunnel tunnel : tunnels.getTunnels(daRouter)) {
			tunnel.setInboundInterfaceCounterOID(getInboundInterfaceCounterOID(tunnel.getPhysicalLocalInterfaceName(), interfaces));
			tunnel.setOutboundInterfaceCounterOID(getOutboundInterfaceCounterOID(tunnel.getPhysicalLocalInterfaceName(), interfaces));
		}
	}
	
	private void setOIDsForLinks(BGRouter bgRouter) throws IOException {
		final List<String> interfaces = getIntrefacesForBGRouter(bgRouter);
		for (Link link : links.getLinks(bgRouter)) {
			link.setInboundInterfaceCounterOID(getInboundInterfaceCounterOID(link.getPhysicalInterfaceName(), interfaces));
			link.setOutboundInterfaceCounterOID(getOutboundInterfaceCounterOID(link.getPhysicalInterfaceName(), interfaces));
		}		
	}
	
	private List<String> getIntrefacesForBGRouter(BGRouter bgRouter) throws IOException {
		snmpWrapper.startClient();
		final List<String> interfaces = snmpWrapper.snmpWalk(
				snmpWrapper.prepareOID(IF_NAME_OID), 
				snmpWrapper.prepareTarget(bgRouter.getManagementAddress().getPrefix(), bgRouter.getSnmpCommunity()));
		snmpWrapper.stopClient();
		return interfaces;
	}
	
	private List<String> getIntrefacesForDARouter(DARouter daRouter) throws IOException {
		snmpWrapper.startClient();
		final List<String> interfaces = snmpWrapper.snmpWalk(
				snmpWrapper.prepareOID(IF_NAME_OID), 
				snmpWrapper.prepareTarget(daRouter.getManagementAddress().getPrefix(), daRouter.getSnmpCommunity()));
		snmpWrapper.stopClient();
		return interfaces;
	}
	
	protected String getOutboundInterfaceCounterOID(String physicalInterfaceName, List<String> interfaces) throws IOException {
		final StringBuilder oid = new StringBuilder();
		oid.append(IF_HC_OUT_OCTETS_OID).append(".").append(parseInterface(physicalInterfaceName, interfaces));
		return oid.toString();
	}
	
	protected String getInboundInterfaceCounterOID(String physicalInterfaceName, List<String> interfaces) throws IOException {
		final StringBuilder oid = new StringBuilder();
		oid.append(IF_HC_IN_OCTETS_OID).append(".").append(parseInterface(physicalInterfaceName, interfaces));
		return oid.toString();
	}
	
	protected String parseInterface(String physicalInterfaceName, List<String> snmpWalk) {
		if(physicalInterfaceName == null) {
			throw new IllegalArgumentException("Physical interface name is null.");
		}
		if(snmpWalk == null) {
			throw new IllegalArgumentException("Cannot find OID for " + physicalInterfaceName);
		}
		
		String response = null;
		for (String inf : snmpWalk) {
			if(inf.contains(physicalInterfaceName)) {
				final String temp = inf.substring(0, inf.indexOf("=") - 1);
				response = temp.substring(temp.lastIndexOf(".") + 1);
				break;
			}
		}

		return response;
	}
}