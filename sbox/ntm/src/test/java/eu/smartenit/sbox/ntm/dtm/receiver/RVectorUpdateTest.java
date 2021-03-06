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
package eu.smartenit.sbox.ntm.dtm.receiver;

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
import eu.smartenit.sbox.db.dao.LinkDAO;
import eu.smartenit.sbox.db.dao.SystemControlParametersDAO;
import eu.smartenit.sbox.db.dao.TimeScheduleParametersDAO;
import eu.smartenit.sbox.db.dto.CVector;
import eu.smartenit.sbox.db.dto.ChargingRule;
import eu.smartenit.sbox.db.dto.Link;
import eu.smartenit.sbox.db.dto.LocalRVector;
import eu.smartenit.sbox.db.dto.NetworkAddressIPv4;
import eu.smartenit.sbox.db.dto.RVector;
import eu.smartenit.sbox.db.dto.SBox;
import eu.smartenit.sbox.db.dto.SimpleLinkID;
import eu.smartenit.sbox.db.dto.SystemControlParameters;
import eu.smartenit.sbox.db.dto.TimeScheduleParameters;
import eu.smartenit.sbox.db.dto.XVector;
import eu.smartenit.sbox.interfaces.intersbox.client.InterSBoxClient;
import eu.smartenit.sbox.ntm.dtm.DAOFactory;
import eu.smartenit.sbox.ntm.dtm.receiver.DTMTrafficManager;
import eu.smartenit.sbox.ntm.dtm.receiver.InterSBoxClientFactory;
import eu.smartenit.sbox.ntm.dtm.receiver.RemoteSBoxContainer;

/**
 * Includes test methods for workflow triggered by {@link DTMTrafficManager}
 * after receiving updated reference vector from Economic Analyzer component.
 * 
 * @author Lukasz Lopatowski
 * @version 3.0
 * 
 */
public class RVectorUpdateTest {

	private static final String LINK1_ID = "link1";
	private static final String LINK2_ID = "link2";
	private static final String ISP1_ID = "isp1";
	
	private int asNumber = 1;
	private XVector xVector = new XVector();
	private LocalRVector rVector = new LocalRVector();
	
	private RemoteSBoxContainer container = mock(RemoteSBoxContainer.class);
	private InterSBoxClient client = mock(InterSBoxClient.class);
	private LinkDAO dao = mock(LinkDAO.class);
	private SystemControlParametersDAO scpDAO = mock(SystemControlParametersDAO.class);
	private TimeScheduleParametersDAO tspDAO = mock(TimeScheduleParametersDAO.class);
	
	@Before
	public void setup() {
		SimpleLinkID linkID1 = new SimpleLinkID(LINK1_ID, ISP1_ID);
		SimpleLinkID linkID2 = new SimpleLinkID(LINK2_ID, ISP1_ID);
		xVector.setSourceAsNumber(asNumber);
    	xVector.addVectorValueForLink(linkID1, 500L);
    	xVector.addVectorValueForLink(linkID2, 500L);
    	rVector.setSourceAsNumber(asNumber);
    	rVector.addVectorValueForLink(linkID1, 1000L);
    	rVector.addVectorValueForLink(linkID2, 1000L);
    	
    	InterSBoxClientFactory.disableUniqueClientCreationMode();
    	InterSBoxClientFactory.setClientInstance(client);
    	
    	SBox sbox1 = new SBox(new NetworkAddressIPv4("1.1.1.1", 32));
    	SBox sbox2 = new SBox(new NetworkAddressIPv4("2.2.2.2", 32));
    	SBox sbox3 = new SBox(new NetworkAddressIPv4("3.3.3.3", 32));
    	when(container.getRemoteSBoxes(asNumber)).thenReturn(Arrays.asList(sbox1, sbox2, sbox3));
    	
    	when(dao.findById(linkID1)).thenReturn(
    			new Link(linkID1, null, null, 0, null, null, null, null, null, new NetworkAddressIPv4("10.10.10.10", 24)));
    	when(dao.findById(linkID2)).thenReturn(
    			new Link(linkID2, null, null, 0, null, null, null, null, null, new NetworkAddressIPv4("20.20.20.20", 24)));
    	SystemControlParameters scp = new SystemControlParameters(ChargingRule.volume, null, 0);
    	when(scpDAO.findLast()).thenReturn(scp);
    	TimeScheduleParameters tsp = new TimeScheduleParameters();
    	when(tspDAO.findLast()).thenReturn(tsp);
    	
    	DAOFactory.setLinkDAOInstance(dao);
    	DAOFactory.setSCPDAOInstance(scpDAO);
    	DAOFactory.setTSPDAOInstance(tspDAO);
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
