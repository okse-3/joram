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
package fr.dyade.aaa.mom.jms;

/**
 * A <code>SetThreshold</code> instance is used by a JMS administrator for
 * setting or unsetting a given threshold value as a default threshold, or
 * the threshold of a given queue, or the threshold of a given user.
 */
public class SetThreshold extends JmsAdminRequest
{
  /** Identifier of the queue or user's proxy. */
  private String name;
  /**
   * Threshold value, <code>null</code> for unsetting an already set
   * threshold.
   */
  private Integer threshold;
  /** <code>true</code> if the target is an user. */
  private boolean toUser;

  /**
   * Constructs a <code>SetThreshold</code> instance.
   *
   * @param name  Identifier of the queue, or of the user's proxy, or
   *          <code>null</code> for a default setting.
   * @param threshold  Threshold value, negative or 0 for threshold,
              <code>null</code> for unsetting a current threshold.
   * @param toUser  <code>true</code> if the target is an user.
   */
  public SetThreshold(String name, Integer threshold, boolean toUser)
  {
    this.name = name;
    this.threshold = threshold;
    this.toUser = toUser;
  }

  
  /**
   * Returns the identifier of a queue, or the identifier of a user proxy,
   * or <code>null</code>.
   */
  public String getName()
  {
    return name;
  }

  /** Returns the threshold value. */
  public Integer getThreshold()
  {
    return threshold;
  }

  /** Returns <code>true</code> if the target is an user. */
  public boolean toUser()
  {
    return toUser;
  }
}
