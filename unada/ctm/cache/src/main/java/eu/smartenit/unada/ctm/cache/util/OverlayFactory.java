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
package eu.smartenit.unada.ctm.cache.util;

import eu.smartenit.unada.om.OverlayManager;

/**
 * The OverlayFactory class. It includes constructor for the OverlayManager component.
 * 
 * @author George Petropoulos
 * @version 2.0
 * 
 */
public class OverlayFactory {
	
	private static OverlayManager overlayManager;
	
	public static OverlayManager getOverlayManager() {
		return overlayManager;
	}

	public static void setOverlayManager(OverlayManager overlayManager) {
		OverlayFactory.overlayManager = overlayManager;
	}
	

}