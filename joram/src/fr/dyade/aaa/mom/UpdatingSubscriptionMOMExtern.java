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

import java.lang.*;

/** 
 *	a subscription allows a client to update a subscription to a Topic
 * 
 *	fr.dyade.aaa.mom.Topic
 */ 
 
public class UpdatingSubscriptionMOMExtern extends MessageMOMExtern { 
	
	/** the name of the subscription given by the agentClient */
	public String nameSubscription;
	
	/** Topic where subscription will be done */ 
	public fr.dyade.aaa.mom.TopicNaming topic; 
	
	/** the agentClient choses if he wants to receive its own meseage  
	 *	true means not 
	 */ 
	public boolean noLocal;  
	 
	/** the selector of the request */ 
	public String selector;  
	 
	/** constructor */
	public UpdatingSubscriptionMOMExtern(long requestID, String nameSubscription, fr.dyade.aaa.mom.TopicNaming topic, boolean noLocal, String selector) {
		super(requestID);
		this.nameSubscription = nameSubscription;
		this.topic = topic; 
		this.noLocal = noLocal; 
		this.selector = selector ;
	}
	
}
