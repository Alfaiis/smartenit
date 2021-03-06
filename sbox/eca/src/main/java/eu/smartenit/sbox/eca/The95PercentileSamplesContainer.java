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
package eu.smartenit.sbox.eca;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.smartenit.sbox.db.dto.LinkID;
import eu.smartenit.sbox.db.dto.LocalRVector;
import eu.smartenit.sbox.db.dto.LocalVectorValue;
import eu.smartenit.sbox.db.dto.SimpleLinkID;
import eu.smartenit.sbox.db.dto.XVector;
import eu.smartenit.sbox.db.dto.ZVector;

/**
 * Container for 95th percentile traffic samples. 
 * 
 * @author Lukasz Lopatowski
 * @version 3.1
 *
 */
public class The95PercentileSamplesContainer extends TrafficSamplesContainer {

	private static final Logger logger = LoggerFactory.getLogger(The95PercentileSamplesContainer.class);
	
	private int sampleCounter = 0;
	private Map<SimpleLinkID, List<TrafficSample>> linkTrafficSamples = new HashMap<>();
	private Map<SimpleLinkID, List<TrafficSample>> tunnelTrafficSamples = new HashMap<>();
	
	private final int samplesInAccountingPeriod;
	private LocalRVector currentRVector = null;
	private Map<LinkID, Integer> numberOfSamplesBelowRVector = new HashMap<>();
	private List<LinkID> linksWithRVectorAchieved = new ArrayList<>();
	private boolean changeInLinksWithRVectorAchieved = false;
	private The95thPercentileSamplesHistory history = new The95thPercentileSamplesHistory();
	
	/**
	 * The constructor with arguments.
	 * 
	 * @param link1
	 *            identifier of the first link
	 * @param link2
	 *            identifier of the second link
	 */
	public The95PercentileSamplesContainer(SimpleLinkID link1, SimpleLinkID link2) {
		this(link1, link2, 0);
	}
	
	/**
	 * The constructor with arguments.
	 * 
	 * @param link1
	 *            identifier of the first link
	 * @param link2
	 *            identifier of the second link
	 * @param samplesInAccountingPeriod
	 *            number of samples collected during entire accounting period
	 */
	public The95PercentileSamplesContainer(SimpleLinkID link1, SimpleLinkID link2, int samplesInAccountingPeriod) {
		super(link1, link2);
		this.samplesInAccountingPeriod = samplesInAccountingPeriod;
		this.numberOfSamplesBelowRVector.put(link1, 0);
		this.numberOfSamplesBelowRVector.put(link2, 0);
	}

	/**
	 * Stores link and tunnel traffic values. Checks if for some links the
	 * reference vector is already achieved.
	 * 
	 * @param xVector
	 *            link traffic vector
	 * @param zVectors
	 *            list of tunnel traffic vectors
	 */
	@Override
	public void storeTrafficValues(XVector xVector, List<ZVector> zVectors) {
		logger.debug("Storing new traffic samples.");
		if (xVector == null || zVectors == null || zVectors.size() != 1)
			throw new IllegalArgumentException("Invalid input.");
		
		sampleCounter++;
		for (LocalVectorValue vectorValue : xVector.getVectorValues()) {
			TrafficSample sample = new TrafficSample();
			sample.sampleNumber = sampleCounter;
			sample.value = vectorValue.getValue();
			storeLinkTrafficSampleAndRecalculate((SimpleLinkID)vectorValue.getLinkID(), sample);
		}
			
		for (LocalVectorValue vectorValue : zVectors.get(0).getVectorValues()) {
			TrafficSample sample = new TrafficSample();
			sample.sampleNumber = sampleCounter;
			sample.value = vectorValue.getValue();
			storeTunnelTrafficSample((SimpleLinkID)vectorValue.getLinkID(), sample);
		}
	}

	/**
	 * Resets structures holding traffic samples and other supporting fields
	 * (after the accounting period has expired).
	 */
	@Override
	public void resetTrafficValues() {
		history.store(linkTrafficSamples, tunnelTrafficSamples);
		sampleCounter = 0;
		linkTrafficSamples = new HashMap<>();
		tunnelTrafficSamples = new HashMap<>();
		changeInLinksWithRVectorAchieved = false;
		linksWithRVectorAchieved = new ArrayList<>();
		numberOfSamplesBelowRVector.put(link1, 0);
		numberOfSamplesBelowRVector.put(link2, 0);
	}

	/**
	 * Calculates and returns 95th percentile traffic samples for all links.
	 * 
	 * @return table of samples
	 */
	@Override
	public long[] getTrafficValuesForLinks() {
		logger.debug("Returning 95th percentile sample from following lists: \n" 
				+ linkTrafficSamples.get(link1) + "\n"
				+ linkTrafficSamples.get(link2));
		logger.info("95TH-PERCENTILE-SAMPLES-LINK-" + link1.getLocalLinkID() + ":" + linkTrafficSamples.get(link1)); 
		logger.info("95TH-PERCENTILE-SAMPLES-LINK-" + link2.getLocalLinkID() + ":" + linkTrafficSamples.get(link2));
		long[] the95thPercentileXSamples = new long[2];
		
		Collections.sort(linkTrafficSamples.get(link1));
		Collections.sort(linkTrafficSamples.get(link2));
		
		the95thPercentileXSamples[EconomicAnalyzerInternal.x1] = 
				linkTrafficSamples.get(link1).get(calculate95thPercentileSampleIndex()).value;
		the95thPercentileXSamples[EconomicAnalyzerInternal.x2] = 
				linkTrafficSamples.get(link2).get(calculate95thPercentileSampleIndex()).value;
		
		logger.info("Returned 95th percentile X sample is [" + the95thPercentileXSamples[0] + ", " + the95thPercentileXSamples[1] + "]");
		return the95thPercentileXSamples;
	}

	/**
	 * Calculates and returns 95th percentile traffic samples for all tunnels.
	 * 
	 * @return table of samples
	 */
	@Override
	public long[] getTrafficValuesForTunnels() {
		logger.debug("Returning 95th percentile sample from following lists: \n" 
				+ tunnelTrafficSamples.get(link1) + "\n"
				+ tunnelTrafficSamples.get(link2));
		logger.info("95TH-PERCENTILE-SAMPLES-TUNNELS-" + link1.getLocalLinkID() + ":" + tunnelTrafficSamples.get(link1)); 
		logger.info("95TH-PERCENTILE-SAMPLES-TUNNELS-" + link2.getLocalLinkID() + ":" + tunnelTrafficSamples.get(link2)); 
		long[] the95thPercentileZSamples = new long[2];
		
		Collections.sort(tunnelTrafficSamples.get(link1));
		Collections.sort(tunnelTrafficSamples.get(link2));
		
		the95thPercentileZSamples[EconomicAnalyzerInternal.x1] = 
				tunnelTrafficSamples.get(link1).get(calculate95thPercentileSampleIndex()).value;
		the95thPercentileZSamples[EconomicAnalyzerInternal.x2] = 
				tunnelTrafficSamples.get(link2).get(calculate95thPercentileSampleIndex()).value;
		
		logger.info("Returned 95th percentile Z sample is [" + the95thPercentileZSamples[0] + ", " + the95thPercentileZSamples[1] + "]");
		return the95thPercentileZSamples;
	}
	
	private void storeLinkTrafficSampleAndRecalculate(SimpleLinkID linkID, TrafficSample sample) {
		if (!linkTrafficSamples.containsKey(linkID))
			linkTrafficSamples.put(linkID, new ArrayList<TrafficSample>());
		
		linkTrafficSamples.get(linkID).add(sample);
		recalculateSamplesBelowRVector(linkID, sample);
	}
	
	private void storeTunnelTrafficSample(SimpleLinkID linkID, TrafficSample sample) {
		if (!tunnelTrafficSamples.containsKey(linkID))
			tunnelTrafficSamples.put(linkID, new ArrayList<TrafficSample>());
		
		tunnelTrafficSamples.get(linkID).add(sample);
	}
	
	private void recalculateSamplesBelowRVector(SimpleLinkID linkID, TrafficSample sample) {
		if (currentRVector != null) {
			logger.debug("Recalculating samples below R vector");
			if (sample.value <= currentRVector.getVectorValueForLink(linkID)) {
				int count = numberOfSamplesBelowRVector.get(linkID);
				count++;
				numberOfSamplesBelowRVector.put(linkID, count);
				if (count == (int)Math.ceil(samplesInAccountingPeriod*0.95)) {
					logger.debug("R vector achieved on link: {}", ((SimpleLinkID)linkID).toString());
					changeInLinksWithRVectorAchieved = true;
					linksWithRVectorAchieved.add(linkID);
				}
			}
		}
	}
	
	private int calculate95thPercentileSampleIndex() {
		return ((int)(sampleCounter*0.95)) - 1;
	}
	
	/**
	 * Checks whether after storing latest traffic samples the reference vector
	 * was achieved on new link or links.
	 * 
	 * @return <code>true</code> if reference vector was achieved on new link or
	 *         links
	 */
	public boolean isChangeInLinksWithRVectorAchieved() {
		if (changeInLinksWithRVectorAchieved == true) {
			changeInLinksWithRVectorAchieved = false;
			return true;
		}
		return false;
	}

	/**
	 * Returns current list of links on which the reference vector has been
	 * achieved.
	 * 
	 * @return list of link identifiers
	 */
	public List<LinkID> getLinksWithRVectorAchieved() {
		return linksWithRVectorAchieved;
	}
	
	/**
	 * Sets reference vector to be used to determine the number of traffic
	 * samples with values less than the reference vector component value.
	 * 
	 * @param rVector
	 *            reference vector for current accounting period
	 */
	public void setCurrentRVector(LocalRVector rVector) {
		this.currentRVector = rVector;
	}
	
	/**
	 * Returns the whole history of traffic samples stored in previous
	 * accounting periods.
	 * 
	 * @return samples history
	 */
	public The95thPercentileSamplesHistory getHistory()  {
		return history;
	}

	public class TrafficSample implements Comparable<TrafficSample>{
		public int sampleNumber = 0;
		public long value = 0;
		
		@Override
		public int compareTo(TrafficSample other) {
			return Long.compare(value, other.value);
		}
		
		@Override
		public String toString() {
			return "[" + sampleNumber + ":" + value + "]";
		}
	}

}
