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

import java.util.List;
import java.util.Properties;

import eu.smartenit.sbox.db.dao.gen.AbstractSegmentDAO;
import eu.smartenit.sbox.db.dto.Segment;
import eu.smartenit.sbox.db.dto.SimpleLinkID;

import org.skife.jdbi.v2.DBI;
import org.sqlite.SQLiteConfig;

/**
 * The SegmentDAO class.
 *
 * @authors Antonis Makris, George Petropoulos
 * @version 1.0
 * 
 */
public class SegmentDAO {
	
	final AbstractSegmentDAO dao;
	
	/**
	 * The constructor.
	 */
	public SegmentDAO() {
		Properties connectionProperties = new Properties();
		SQLiteConfig config = new SQLiteConfig();
		config.enforceForeignKeys(true);
		connectionProperties = config.toProperties(); 
		DBI dbi = new DBI(DbConstants.DBI_URL, connectionProperties);
		dao = dbi.onDemand(AbstractSegmentDAO.class);
	}

	/**
	 * The method that creates the Segment table.
	 */
	public void createTable() {
		dao.createTable();
	}

	/**
	 * The method that deletes the Segment table.
	 */
	public void deleteTable() {
		dao.deleteTable();
	}
	
	/**
	 * The method that inserts a Segment into the Segment table.
	 * 
	 * @param s The Segment to be inserted.
	 * @param linkID The SimpleLinkID.
	 */
	public void insertByLinkId(Segment s, SimpleLinkID linkID) throws Exception {
		dao.insertByLinkId(s, linkID);
	}
	
	/**
	 * The method that inserts a batch of Segments of a link 
	 * into the Segment table.
	 * 
	 * @param sList The Segments list to be inserted.
	 * @param linkID The SimpleLinkID.
	 */
	public void insertBatchByLinkId(List<Segment> slist, SimpleLinkID linkID) throws Exception  {
		dao.insertBatchByLinkId(slist, linkID);
	}
	
	/**
	 * The method that finds all the stored Segments from the Segment table.
	 * 
	 * @return The list of stored Segments.
	 */
	public List<Segment> findAll() {
		return dao.findAll();
	}

	/**
	 * The method that finds all the stored Segments of a link 
	 * from the Segment table.
	 * 
	 * @param linkID The SimpleLinkID.
	 * @return The list of stored Segments.
	 */
	public List<Segment> findAllByLinkId(SimpleLinkID linkID) {
		return dao.findAllByLinkId(linkID);
	}
	
	/**
	 * The method that deletes all the stored Segments of a link 
	 * from the Segment table.
	 * 
	 * @param linkID The SimpleLinkID.
	 */
	public void deleteByLinkId(SimpleLinkID linkID) {
		dao.deleteByLinkId(linkID);
	}

	/**
	 * The method that deletes all the stored Segments  
	 * from the Segment table.
	 * 
	 */
	public void deleteAll() {
		dao.deleteAll();
	}
	
	

}
