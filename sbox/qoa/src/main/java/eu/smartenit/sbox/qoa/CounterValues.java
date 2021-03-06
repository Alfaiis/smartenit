/**
 * Copyright (C) 2015 The SmartenIT consortium (http://www.smartenit.eu)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import eu.smartenit.sbox.db.dto.LinkID;
import eu.smartenit.sbox.db.dto.LocalVectorValue;
import eu.smartenit.sbox.db.dto.TunnelID;

/**
 * Used to store counter values (as 64bit numbers) on per {@link LinkID}
 * and per {@link TunnelID} basis.
 * 
 * @author <a href="mailto:jgutkow@man.poznan.pl">Jakub Gutkowski</a> (PSNC)
 * @author <a href="mailto:llopat@man.poznan.pl">Lukasz Lopatowski</a> (PSNC)
 * @version 1.2
 * 
 */
public class CounterValues {
	
	protected Map<LinkID, Long> linkCounterValues = new HashMap<LinkID, Long>();
	protected Map<TunnelID, Long> tunnelCounterValues = new HashMap<TunnelID, Long>();

	/**
	 * Stores counter value for given link in the structure.
	 * 
	 * @param linkID
	 *            identifier of the monitored link
	 * @param counterValue
	 *            counter value collected from the device
	 */
	public void storeCounterValue(LinkID linkID, long counterValue) {
		linkCounterValues.put(linkID, counterValue);
	}

	/**
	 * Adds counter value to existing entry in the structure or creates a new entry.
	 * 
	 * @param linkID
	 *            identifier of the monitored link
	 * @param counterValue
	 *            counter value collected from the device
	 */
	public void addCounterValue(LinkID linkID, long counterValue) {
		long currentCounterValue = 0;
		if (linkCounterValues.containsKey(linkID)) {
			currentCounterValue = linkCounterValues.get(linkID);
		}
		linkCounterValues.put(linkID, currentCounterValue + counterValue);
	}

	/**
	 * Stores counter value for given tunnel in the structure.
	 * 
	 * @param tunnelID
	 *            identifier of the monitored tunnel
	 * @param counterValue
	 *            counter value collected from the device
	 */
	public void storeCounterValue(TunnelID tunnelID, long counterValue) {
		tunnelCounterValues.put(tunnelID, counterValue);
	}
	
	/**
	 * Populates the local structure with provided links and tunnels counters
	 * data.
	 * 
	 * @param counterValues
	 *            provided {@link CounterValues} instance
	 */
	public void addLinksAndTunnels(CounterValues counterValues) {
		linkCounterValues.putAll(counterValues.getLinkCounterValues());
		tunnelCounterValues.putAll(counterValues.getTunnelCounterValues());
	}
	
	/**
	 * Update the local structure with provided links and tunnels counters
	 * data.
	 * 
	 * @param counterValues
	 *            provided new {@link CounterValues} instance
	 */
	public void updateLinksAndTunnels(CounterValues newCounterValues) {
		updateLinks(newCounterValues.getLinkCounterValues());
		updateTunnels(newCounterValues.getTunnelCounterValues());
	}

	protected void updateLinks(Map<LinkID, Long> newLinkCounterValues) {
		if(linkCounterValues.size() < 1)
			linkCounterValues.putAll(newLinkCounterValues);
		else {
			for (LinkID linkId : newLinkCounterValues.keySet()) {
				if(!linkCounterValues.containsKey(linkId)) {
					linkCounterValues.put(linkId, newLinkCounterValues.get(linkId));
				} else {
					linkCounterValues.put(linkId, linkCounterValues.get(linkId) + newLinkCounterValues.get(linkId));
				}
			}
		}
	}
	
	protected void updateTunnels(Map<TunnelID, Long> newTunnelCounterValues) {
		if(tunnelCounterValues.size() < 1) {
			tunnelCounterValues.putAll(newTunnelCounterValues);
		}
		else {
			for (TunnelID tunnelId : newTunnelCounterValues.keySet()) {
				if(!tunnelCounterValues.containsKey(tunnelId)) {
					tunnelCounterValues.put(tunnelId, newTunnelCounterValues.get(tunnelId));
				} else {
					tunnelCounterValues.put(tunnelId, tunnelCounterValues.get(tunnelId) + newTunnelCounterValues.get(tunnelId));
				}
			}
		}
	}

	/**
	 * Retrieves counter values for all links stored in the structure.
	 * 
	 * @return list of link identifier - counter value pairs
	 */
	public Map<LinkID, Long> getLinkCounterValues() {
		return this.linkCounterValues;
	}
	
	/**
	 * Retrieves counter values for all tunnels stored in the structure.
	 * 
	 * @return list of tunnel identifier - counter value pairs
	 */
	public Map<TunnelID, Long> getTunnelCounterValues() {
		return this.tunnelCounterValues;
	}
	
	/**
	 * Retrieves identifiers of all links stored in the structure.
	 * 
	 * @return set of {@link LinkID} instances
	 */
	public Set<LinkID> getAllLinkIds() {
		return linkCounterValues.keySet();
	}
	
	/**
	 * Retrieves identifiers of all tunnels stored in the structure.
	 * 
	 * @return set of {@link TunnelID} instances
	 */
	public Set<TunnelID> getAllTunnelsIds() {
		return tunnelCounterValues.keySet();
	}
	
	/**
	 * Retrieves counter value for given link from the structure.
	 * 
	 * @param linkID
	 *            identifier of a monitored link
	 * @return 64-bit counter value
	 */
	public Long getCounterValue(LinkID linkID) {
		return linkCounterValues.get(linkID);
	}
	
	/**
	 * Retrieves counter value for given tunnel from the structure.
	 * 
	 * @param tunnelID
	 *            identifier of a monitored tunnel
	 * @return 64-bit counter value
	 */
	public Long getCounterValue(TunnelID tunnelID) {
		return tunnelCounterValues.get(tunnelID);
	}
	
	/**
	 * Returns new list of {@link LocalVectorValue} based on stored counter
	 * values for links.
	 * 
	 * @return list of vector values
	 */
	public List<LocalVectorValue> toLocalVectorValues() {
		List<LocalVectorValue> vectorValues = new ArrayList<LocalVectorValue>();
		for(Entry<LinkID, Long> entry : linkCounterValues.entrySet()) {
			vectorValues.add(new LocalVectorValue(entry.getValue(), entry.getKey()));
		}
		return vectorValues;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((linkCounterValues == null) ? 0 : linkCounterValues
						.hashCode());
		result = prime
				* result
				+ ((tunnelCounterValues == null) ? 0 : tunnelCounterValues
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CounterValues other = (CounterValues) obj;
		if (linkCounterValues == null) {
			if (other.linkCounterValues != null)
				return false;
		} else if (!linkCounterValues.equals(other.linkCounterValues))
			return false;
		if (tunnelCounterValues == null) {
			if (other.tunnelCounterValues != null)
				return false;
		} else if (!tunnelCounterValues.equals(other.tunnelCounterValues))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CounterValues [linkCounterValues=" + linkCounterValues
				+ ", tunnelCounterValues=" + tunnelCounterValues + "]";
	}

}
