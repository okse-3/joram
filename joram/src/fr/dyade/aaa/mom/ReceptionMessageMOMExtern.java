/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.mom; 

import java.lang.String;

/** 
 *	allows to a client to request a message from a Queue
 * 
 *	@see	fr.dyade.aaa.mom.MessageMOMExtern 
 */ 
 
public class ReceptionMessageMOMExtern extends MessageMOMExtern { 
	
	/** the Queue where will be extracted the message */
	public fr.dyade.aaa.mom.QueueNaming queue;
	
	/** the timeOut of the request */
	public long timeOut;
	
	/** the selector of the request */ 
	public  String selector;  
	
	/** the identifier of the Session */  
	public String sessionID;
	
	/** the mode of acknowledge */
	public int acknowledgeMode ;
	
	public ReceptionMessageMOMExtern(long requestID, fr.dyade.aaa.mom.QueueNaming queue, String selector, long timeOut, int acknowledgeMode, String sessionID) {
		super(requestID);
		this.queue = queue;
		this.selector = selector;
		this.timeOut = timeOut;
		this.acknowledgeMode = acknowledgeMode;
		this.sessionID = sessionID;
	}
	
}
