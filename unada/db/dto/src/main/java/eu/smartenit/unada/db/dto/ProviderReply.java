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
package eu.smartenit.unada.db.dto;

import java.io.Serializable;
import java.util.List;

/**
 * The ProviderReply class.
 * 
 * @authors George Petropoulos
 * @version 2.0
 * 
 */
public class ProviderReply implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5430712218671478099L;

	public ProviderReply() {
		
	}
	
	private String type;
    private String replyAddress;
    private int replyPort;
    private long contentID;
    private List<UnadaInfo> providers;
    
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getReplyAddress() {
		return replyAddress;
	}
	public void setReplyAddress(String replyAddress) {
		this.replyAddress = replyAddress;
	}
	public int getReplyPort() {
		return replyPort;
	}
	public void setReplyPort(int replyPort) {
		this.replyPort = replyPort;
	}
	public long getContentID() {
		return contentID;
	}
	public void setContentID(long contentID) {
		this.contentID = contentID;
	}
	public List<UnadaInfo> getProviders() {
		return providers;
	}
	public void setProviders(List<UnadaInfo> providers) {
		this.providers = providers;
	}
	
	@Override
	public String toString() {
		return "ProviderReply [type=" + type + ", replyAddress=" + replyAddress
				+ ", replyPort=" + replyPort + ", contentID=" + contentID
				+ ", providers=" + providers + "]";
	}

}
