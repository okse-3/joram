/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.scheduler;

import com.scalagent.scheduler.ScheduleTask;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;

/**
 * Task sending a SchedulerQueueNot to a SchedulerQueue.
 */
public class SchedulerQueueTask implements ScheduleTask {
  private static final long serialVersionUID = 1L;
  
  private AgentId schedulerQueue = null;
  
  public SchedulerQueueTask(AgentId schedulerQueue) {
    this.schedulerQueue = schedulerQueue;
  }
  
  /**
   * Task to execute: send a SchedulerQueueNot to the related SchedulerQueue.
   * 
   * @see com.scalagent.scheduler.ScheduleTask#run()
   */
  public void run() {
    Channel.sendTo(schedulerQueue, new SchedulerQueueNot());
  }

}
