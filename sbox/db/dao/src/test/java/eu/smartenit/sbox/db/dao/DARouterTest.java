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
package eu.smartenit.sbox.db.dao;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.smartenit.sbox.db.dao.util.Tables;
import eu.smartenit.sbox.db.dto.DARouter;
import eu.smartenit.sbox.db.dto.NetworkAddressIPv4;
import eu.smartenit.sbox.db.dto.SDNController;

public class DARouterTest {

	private static final Logger logger = LoggerFactory
			.getLogger(DARouterTest.class);
	
	private static DARouterDAO ddao;

	private static SDNControllerDAO sdao;

	@BeforeClass
	public static void setupDb() {
		logger.info("Creating all tables for tests.");
		Tables tables = new Tables();
		tables.deleteAll();
		tables.createAll();

		logger.info("Preparing SDNController and DARouter tables.");
		sdao = new SDNControllerDAO();
		sdao.deleteAll();
		
		ddao = new DARouterDAO();
		ddao.deleteAll();
	}

	
	@Test
	public void testSimpleFunctions() throws Exception {
		List<DARouter> dlist = ddao.findAll();
		assertEquals(dlist.size(), 0);
		
		DARouter d = new DARouter();
		d.setManagementAddress(new NetworkAddressIPv4("5.5.5.5", 0));
		d.setSnmpCommunity("snmp");
        d.setOfSwitchDPID("abcdefg");
		
		ddao.insert(d);
		
		dlist = ddao.findAll();
		assertEquals(dlist.size(), 1);
		
		d = ddao.findById("4.3.5.5");
		assertNull(d);
		d = ddao.findById("5.5.5.5");
		assertNotNull(d);
		assertEquals(d.getSnmpCommunity(), "snmp");
        assertEquals(d.getOfSwitchDPID(), "abcdefg");
        assertEquals(d.getLocalDCOfSwitchPortNumbers().size(), 0);
        
        ddao.deleteById("5.5.5.5");
        dlist = ddao.findAll();
		assertEquals(dlist.size(), 0);
	}

	@Test
	public void testDARouterSDNFunctions() throws Exception {
		
		DARouter d = new DARouter();
		d.setManagementAddress(new NetworkAddressIPv4("5.5.5.5", 0));
		d.setSnmpCommunity("snmp");
        d.setOfSwitchDPID("abcdefg");
        d.setLocalDCOfSwitchPortNumbers(Arrays.asList(32, 4, 22));
		
		ddao.insert(d);
		List<DARouter> dlist = ddao.findAll();
		assertEquals(dlist.size(), 1);
		
		List<SDNController> list = sdao.findAll();
		assertEquals(list.size(), 0);
		
		SDNController s = new SDNController();
		s.setManagementAddress(new NetworkAddressIPv4("1.2.3.4", 0));
		s.setOpenflowHost(new NetworkAddressIPv4("1.2.3.5", 0));
		s.setOpenflowPort(8080);
		s.setRestHost(new NetworkAddressIPv4("1.2.3.3", 0));
		s.setRestPort(8081);
		  
		sdao.insert(s);
		list = sdao.findAll();
		assertEquals(list.size(), 1);
		s = sdao.findById("1.2.3.4");
		assertNotNull(s);
		assertEquals(s.getOpenflowHost().getPrefix(), "1.2.3.5");
		
		d = ddao.findById("5.5.5.5");
		assertNotNull(d);
		assertEquals(d.getSnmpCommunity(), "snmp");
        assertEquals(d.getOfSwitchDPID(), "abcdefg");
        assertEquals(d.getLocalDCOfSwitchPortNumbers().size(), 3);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(0).intValue(), 32);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(1).intValue(), 4);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(2).intValue(), 22);
		
		d.setSnmpCommunity("newsnmp");
        d.setOfSwitchDPID("asdfg");
        d.setLocalDCOfSwitchPortNumbers(Arrays.asList(44, 55, 66, 77));
		ddao.update(d);
		
		d = ddao.findById("5.5.5.5");
		assertEquals(d.getSnmpCommunity(), "newsnmp");
        assertEquals(d.getOfSwitchDPID(), "asdfg");
        assertEquals(d.getLocalDCOfSwitchPortNumbers().size(), 4);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(0).intValue(), 44);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(1).intValue(), 55);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(2).intValue(), 66);
        assertEquals(d.getLocalDCOfSwitchPortNumbers().get(3).intValue(), 77);
		
		ddao.updateBySDNControllerAddress(d.getManagementAddress().getPrefix(), 
				s.getManagementAddress().getPrefix());
		
		DARouter d2 = new DARouter();
		d2.setManagementAddress(new NetworkAddressIPv4("8.8.8.8", 0));
		d2.setSnmpCommunity("snmp2");
        d2.setOfSwitchDPID("qwerty");
		ddao.insertBySDNControllerAddress(d2, s.getManagementAddress().getPrefix());
		
		dlist = ddao.findBySDNControllerAddress(s.getManagementAddress().getPrefix());
		assertEquals(dlist.size(), 2);
		
		ddao.deleteById("5.5.5.5");
		dlist = ddao.findAll();
		assertEquals(dlist.size(), 1);
		
		sdao.deleteAll();
		assertEquals(sdao.findAll().size(), 0);
		assertEquals(ddao.findAll().size(), 0);
	}
	
	@AfterClass
	public static void dropDb() {

		logger.info("Deleting SDNController table.");
		sdao.deleteAll();
		
		logger.info("Deleting DARouter table.");
		ddao.deleteAll();
	}
}
