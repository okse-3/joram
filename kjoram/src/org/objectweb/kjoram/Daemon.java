/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

/**
 * The Daemon class represents a basic active component in a server. It
 * provides useful code to start and safely stop inner Thread.
 * <p>
 * Main loop of daemon:
 * <p><hr>
 * <blockquote><pre>
 *  try {
 *    while (running) {
 *	canStop = true;
 *
 *	// Get a notification, then execute the right reaction.
 *	try {
 *	  // Get a request
 *	  ...
 *	} catch (InterruptedException exc) {
 *	  continue;
 *	}
 *	
 *	canStop = false;
 *
 *	// executes the request
 *	...
 *    }
 *  } finally {
 *    finish();
 *  }
 * </pre></blockquote>
 */
public abstract class Daemon implements Runnable {

  /**
   * Tests if this daemon is alive.
   * @return	true if this daemon is alive; false otherwise.
   */
  public synchronized boolean isRunning() {
    return ((thread != null) && thread.isAlive());
  }

  /**
   * Boolean variable used to stop the daemon properly. The daemon tests
   * this variable between each request, and stops if it is false.
   * @see start
   * @see stop
   */
  protected volatile boolean running;
  /**
   * Boolean variable used to stop the daemon properly. If this variable
   * is true then the daemon is waiting for a long time and it can interupted,
   * else it handles a request and it will exit after (it tests the
   * <code>{@link #running running}</code> variable between
   * each reaction)
   */
  protected volatile boolean canStop;
  /** The active component of this daemon. */ 
  protected Thread thread = null;
  /** The <code>daemon</code>'s name. */ 
  private String name;
  /** The <code>priority</code> that is assigned to the daemon. */
  protected int priority = Thread.NORM_PRIORITY;

  /**
   * Returns this <code>daemon</code>'s name.
   *
   * @return this <code>daemon</code>'s name.
   */
  public final String getName() {
    return name;
  }
  
  public final void setName(String name) {
    this.name = name;
  }

  /**
   * Allocates a new Daemon object.
   *
   * @param name	the name of the new Daemon
   */
  protected Daemon(String name) {
    this.name = name;

    running = false;
    canStop = false;
    thread = null;
  }


  /**
   * Changes the priority of this daemon.
   *
   * If the daemon is running calls the setPriority method on corresponding
   * thread, else stores value for next start.
   *
   * @see Thread.setPriority
   */
  public void setPriority(int newPriority) {
    if ((newPriority > Thread.MAX_PRIORITY) ||
        (newPriority < Thread.MIN_PRIORITY)) {
      throw new IllegalArgumentException();
    }
    if (running && (thread != null) && thread.isAlive())
      thread.setPriority(newPriority);
    priority = newPriority;
  }

  /**
   * Causes this daemon to begin execution. A new thread is created to
   * execute the run method.
   *
   * @exception IllegalThreadStateException
   *		If the daemon was already started.
   */
  public synchronized void start() {
    if ((thread != null) && thread.isAlive()) {
      throw new IllegalThreadStateException("already started");
    }

    thread = new Thread(this, getName());
    if (priority != Thread.NORM_PRIORITY)
      thread.setPriority(priority);
    running = true;
    canStop = true;
    thread.start();
  }

  /**
   * Releases any resources attached to this daemon. Be careful, its method
   * should be called more than one time.
   */
  protected abstract void close();

  /**
   * Interrupts a thread that waits for long periods. In some cases, we must
   * use application specific tricks. For example, if a thread is waiting on
   * a known socket, we have to close the socket to cause the thread to return
   * immediately. Unfortunately, there really isn't any technique that works
   * in general.
   */
  protected abstract void shutdown();

  /**
   * Interrupts this daemon.
   */
  public void interrupt() {
    thread.interrupt();
  }

  final protected void finish() {
    running = false;
    close();
  }

  /**
   * Forces the daemon to stop executing. This method notifies thread that
   * it should stop running, if the thread is waiting it is first interrupted
   * then the shutdown method is called to close all resources.
   */
  public synchronized void stop() {
    running = false;
    if (thread != null) {
      while (thread.isAlive()) {
        if (canStop) {

          if (thread.isAlive())
            thread.interrupt();

          shutdown();
        }
        try {
          thread.join();
        } catch (InterruptedException exc) {
          continue;
        }
      }
      finish();
      thread = null;
    }
  }

  /**
   * Tests if the daemon's thread is the current one.
   */
  public boolean isCurrentThread() {
    return ((thread != null) && (thread == Thread.currentThread()));
  }
}
