/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.admin;

import java.util.Vector;

import javax.naming.NamingException;

/**
 * The <code>DeadMQueue</code> class allows administrators to manipulate
 * dead message queues.
 */
public class DeadMQueue extends fr.dyade.aaa.joram.Queue
{
  /**
   * Constructs a <code>DeadMQueue</code> instance.
   *
   * @param agentId  Identifier of the dead message queue agent.
   */
  public DeadMQueue(String agentId)
  {
    super(agentId);
  }

  /**
   * Codes a <code>DeadMQueue</code> as a vector for travelling through the
   * SOAP protocol.
   *
   * @exception NamingException  Never thrown.
   */
  public Vector code() throws NamingException
  {
    Vector vec = new Vector();
    vec.add("DeadMQueue");
    vec.add(agentId);
    return vec;
  }

  /**
   * Decodes a coded <code>DeadMQueue</code>.
   *
   * @exception NamingException  If incorrectly coded.
   */
  public static AdministeredObject decode(Vector vec) throws NamingException
  {
    try {
      return new DeadMQueue((String) vec.remove(0));
    }
    catch (Exception exc) {
      throw new NamingException("Vector " + vec.toString()
                                + " incorrectly codes a DeadMQueue.");
    }
  }
}
