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
package eu.smartenit.sbox.interfaces.intersbox.client;

import eu.smartenit.sbox.db.dto.CVector;
import eu.smartenit.sbox.db.dto.RVector; 
import eu.smartenit.sbox.interfaces.intersbox.client.InterSBoxObject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;

import org.slf4j.Logger;                                                                                                                       
import org.slf4j.LoggerFactory;    

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;


/**
 * Unit test for simple App.
 */
public class InterSBoxClientTest 
{
    private static final Logger logger = LoggerFactory.getLogger(InterSBoxClientTest.class);

    @Test
    public void clientTest()
    {
    }
}