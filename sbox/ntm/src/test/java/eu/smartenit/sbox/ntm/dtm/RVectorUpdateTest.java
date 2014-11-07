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
package eu.smartenit.sbox.ntm.dtm;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import eu.smartenit.sbox.commons.SBoxProperties;
import eu.smartenit.sbox.commons.SBoxThreadHandler;
import eu.smartenit.sbox.commons.ThreadFactory;
import eu.smartenit.sbox.db.dto.CVector;
import eu.smartenit.sbox.db.dto.NetworkAddressIPv4;
import eu.smartenit.sbox.db.dto.RVector;
import eu.smartenit.sbox.db.dto.SBox;
import eu.smartenit.sbox.db.dto.SimpleLinkID;
import eu.smartenit.sbox.db.dto.XVector;
import eu.smartenit.sbox.interfaces.intersbox.client.InterSBoxClient;

/**
 * Includes test methods for workflow triggered by {@link DTMTrafficManager}
 * after receiving updated reference vector from Economic Analyzer component.
 * 
 * @author Lukasz Lopatowski
 * @version 1.0
 * 
 */
public class RVectorUpdateTest {

	private static final String LINK1_ID = "link1";
	private static final String LINK2_ID = "link2";
	private static final String ISP1_ID = "isp1";
	
	private int asNumber = 1;
	private XVector xVector = new XVector();
	private RVector rVector = new RVector();
	
	private RemoteSBoxContainer container = mock(RemoteSBoxContainer.class);
	private InterSBoxClient client = mock(InterSBoxClient.class);
	
	@Before
	public void setup() {
		xVector.setSourceAsNumber(asNumber);
    	xVector.addVectorValueForLink(new SimpleLinkID(LINK1_ID, ISP1_ID), 500L);
    	xVector.addVectorValueForLink(new SimpleLinkID(LINK2_ID, ISP1_ID), 500L);
    	rVector.setSourceAsNumber(asNumber);
    	rVector.addVectorValueForLink(new SimpleLinkID(LINK1_ID, ISP1_ID), 1000L);
    	rVector.addVectorValueForLink(new SimpleLinkID(LINK2_ID, ISP1_ID), 1000L);
    	
    	InterSBoxClientFactory.disableUniqueClientCreationMode();
    	InterSBoxClientFactory.setClientInstance(client);
    	
    	SBox sbox1 = new SBox(new NetworkAddressIPv4("1.1.1.1", 32));
    	SBox sbox2 = new SBox(new NetworkAddressIPv4("2.2.2.2", 32));
    	SBox sbox3 = new SBox(new NetworkAddressIPv4("3.3.3.3", 32));
    	when(container.getRemoteSBoxes(asNumber)).thenReturn(Arrays.asList(sbox1, sbox2, sbox3));
    	
    	SBoxThreadHandler.threadService = 
    			Executors.newScheduledThreadPool(SBoxProperties.CORE_POOL_SIZE, new ThreadFactory());
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionOnVectorNull() {
		rVector = null;
		
		DTMTrafficManager manager = new DTMTrafficManager();
		manager.updateRVector(rVector);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionOnInvalidVectorASNumberArgument() {
		rVector.setSourceAsNumber(0);
		
		DTMTrafficManager manager = new DTMTrafficManager();
		manager.updateRVector(rVector);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void shouldThrowExceptionOnInvalidVectorValuesListArgument() {
		rVector.setVectorValues(null);
		
		DTMTrafficManager manager = new DTMTrafficManager();
		manager.updateRVector(rVector);
	}
	
	@Test
	public void shouldSkipCalculationAfterRVectorUpdate() throws InterruptedException {
		DTMTrafficManager manager = new DTMTrafficManager();
		manager.updateRVector(rVector);
		
		Thread.sleep(1000);
		verifyZeroInteractions(client);
	}
	
	@Test
	public void shouldCalculateCAndUpdateCRVectorsAfterRVectorUpdate() throws Exception {
		DTMTrafficManager manager = new DTMTrafficManager();
		manager.setSBoxContainer(container);
		manager.initialize();
		manager.updateXVector(xVector);
		manager.updateRVector(rVector);
		
		Thread.sleep(1000);
		verify(client, times(3)).send(any(String.class), anyInt(), any(CVector.class), any(RVector.class));
		reset(client);
	}
	
}
