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

package fr.dyade.aaa.ip;

import java.io.*;
import java.net.*;
import java.util.*;
import fr.dyade.aaa.agent.*;

/**
 * Class providing a TCP Server. This class is used to create a server
 * proxy to handle multiple connections. There are two modes of using it:
 * <ul>
 * <li> on each connection, a new connected proxy server is created</li>
 * <li> on each connection, the proxy's driver sends a <code>ConnectNot</code>
 * notification to an existing proxy.</li>
 * </ul><p>
 * Connections may be queued when the target proxy is not ready to accept it.
 * This allows a closing proxy not to reject a connection. However this may
 * lead to a client waiting forever a really busy target proxy.
 *
 * @author	Freyssinet Andre
 * @version	v1.0
 */
public abstract class TcpMultiServerProxy extends ProxyAgent {
public static final String RCS_VERSION="@(#)$Id: TcpMultiServerProxy.java,v 1.2 2000-08-01 09:13:51 tachkeni Exp $";
  /** Listening port, may be 0 */
  protected int listenPort = -1;
  /** Listening ServerSocket */
  protected transient ServerSocket listenSocket = null;

  /** key identifying the connection socket */
  protected Integer key = null;
  /** Connection Socket */
  protected transient Socket socket = null;

  /**
   * simple incremented counter providing socket identifiers.
   * Access is protected by the <code>lock</code> object.
   */
  protected static int keyIdx = 0;
  /** lock protecting the access to <code>keyIdx</code> */
  protected static Object lock = new Object();
  /** pool of connected sockets */
  protected static Hashtable sockets = new Hashtable();

  /**
   * Specifies the using mode of this object.
   * When <code>true</code>, which is the default, a new proxy is created
   * for each new connection. When <code>false</code>, an existing proxy
   * is looked for from its id which is given first onto the connection.
   */
  protected boolean newClient = true;

  /**
   * Maximum number of elements queued in {@link #connectQueue}.
   * A <code>-1</code> value means no size limit.
   */
  protected int connectQSize = 0;

  /**
   * Queued incoming connections as {@link #ConnectNot} objects.
   * Incoming connections are handled by the head proxy, accepted,
   * and then routed to the target proxy depending on header data.
   * This means that a busy proxy may not use the TCP listen queue to make
   * its clients wait. Clients are then queued as <code>ConnectNot</code>
   * notifications in this variable.
   */
  protected Vector connectQueue = null;

  /*
   * Creates a local connected TCP server to be configured.
   */
  protected TcpMultiServerProxy() {
    super(Server.getServerId(), null);
    blockingCnx = false;
    multipleCnx = false;
  }

  /**
   * Creates a local listening TCP server with fixed port.
   *
   * @param listenPort	port number > 0, or 0 for any port
   */
  public TcpMultiServerProxy(int listenPort) {
    this(Server.getServerId(), null, listenPort);
  }

  /**
   * Creates a listening TCP server.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	agent name
   * @param listenPort	port number > 0, or 0 for any port
   */
  public TcpMultiServerProxy(short to, String name, int listenPort) {
    this(to, name, listenPort, true);
  }

  /**
   * Creates a listening TCP server.
   *
   * @param to		agent server id where agent is to be deployed
   * @param name	agent name
   * @param listenPort	port number > 0, or 0 for any port
   * @param newClient	creates new client's agent (otherwise forwards
   *			connectNot to existing clients).
   */
  public TcpMultiServerProxy(short to, String name,
			     int listenPort,
			     boolean newClient) {
    super(to, name);
    if (listenPort >= 0)
      this.listenPort = listenPort;
    else
      this.listenPort = 0;
    this.newClient = newClient;
    blockingCnx = true;
    multipleCnx = true;
  }

  /**
   * Sets the maximum number of elements queued in {@link #connectQueue}.
   */
  public void setConnectQSize(int connectQSize) {
    this.connectQSize = connectQSize;
    if (connectQSize > 0)
      connectQueue = new Vector(connectQSize);
    else if (connectQSize < 0)
      connectQueue = new Vector();
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",listenPort=" + listenPort +
      ",newClient=" + newClient +
      ",connectQSize=" + connectQSize +
      ",key=" + key + ")";
  }

  /**
   * Reinitializes the agent, that is reconnects its input and output.
   * This function may be called only when all drivers are null.
   *
   * @exception IOException
   *	unspecialized exception
   */
  protected void reinitialize() throws IOException {
    if (listenPort >= 0) {
      // this is a listen server correctly configured, initializes
      // the listen socket
      if (listenSocket == null)
	listenSocket = new ServerSocket(listenPort);
    } else if (key == null) {
      // this is an old connected server
      if (newClient) {
	delete();
      } else {
	// checks for queued connections
	if ((connectQueue != null) &&
	    ! connectQueue.isEmpty()) {
	  sendTo(getId(), (Notification) connectQueue.firstElement());
	  connectQueue.removeElementAt(0);
	}
      }
      return;
    } else {
      socket = (Socket) sockets.get(key);
    }
    super.reinitialize();
  }

  /**
   * Reads an agent id from a String: it is used in connect to
   * identify the destination agent. This function must be overidden in order
   * to customize the connection.
   *
   * @param str
   *	the string identification of the proxy to connect to
   *
   * @return
   *	the agent identifier of the proxy
   */
  protected AgentId idFromString(String str) {
    return AgentId.fromString(str);
  }

  /**
   * Initializes the connection with the outside.
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void connect() throws Exception {
    if (listenPort >= 0) {
      // this is a listening server
      Integer key = null;
      Socket sock = listenSocket.accept();
      synchronized(lock) {
	key = new Integer(keyIdx++);
	sockets.put(key, sock);
      }
      if (newClient) {
	Channel.channel.directSendTo(getId(), new ConnectNot(key));
      } else {
	// Gets the destination agent id from intput stream, then
	// sends it a ConnectNot.
	DataInputStream dis = new DataInputStream(sock.getInputStream());
	String header = dis.readUTF();
	AgentId to = idFromString(header);
	if (to == null)
	  Channel.channel.directSendTo(getId(), new ConnectNot(key, header));
	else
	  Channel.channel.directSendTo(to, new ConnectNot(key, header));
      }
    } else if (socket != null) {
      // this is a connected client
      oos = setOutputFilters(socket.getOutputStream());
      ois = setInputFilters(socket.getInputStream());
    }
  }

  /**
   * Closes the connection with the outside.
   *
   * @exception IOException
   *	unspecialized exception
   */
  public void disconnect() throws IOException {
    if (listenPort >= 0) {
      // this is a listening server
      if (listenSocket != null) {
	listenSocket.close();
	listenSocket = null;
      }
    } else {
      // this is a connected client
      if (ois != null) {
	ois.close();
	ois = null;
      }
      if (oos != null) {
	oos.close();
	oos = null;
      }
      if (socket != null) {
	socket.close();
	socket = null;
      }
      if (key != null) {
	sockets.remove(key);
	key = null;
      }
    }
  }

  /**
   * Reacts to end of in driver execution.
   *
   * @exception IOException
   *	unspecialized exception
   */
  protected void driverDone(DriverDone not) throws IOException {
    super.driverDone(not);
    if (ois != null || oos != null) {
      // wait for both drivers to terminate execution
	  
      stop();
      return;
    }

    if (listenPort < 0) {
      // it seems there is a recovery case when key may be null
      if (key != null)
	sockets.remove(key);
      key = null;
      socket = null;
    }

    // try to reconnect
    reinitialize();
  }

  protected void initNewProxy(TcpMultiServerProxy child) {}

 /**
  * @param header received header after accepting connection (ignored in this
  * implementation.
  */
  protected TcpMultiServerProxy createNewProxy(String header) throws Exception {
    return (TcpMultiServerProxy) getClass().newInstance();
  }

  /**
   * Reacts to notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   */
  public void react(AgentId from, Notification not) throws Exception {
    try {
      if (not instanceof ConnectNot) {
	ConnectNot cnot = (ConnectNot) not;
	if (listenPort >= 0) {
	  // This is a listen Server, creates a new proxy for the
	  // connection.
	  TcpMultiServerProxy agent = null;
	  agent = createNewProxy(cnot.header);
	  // Initialize the new proxy (Should be used in subclass).
	  initNewProxy(agent);
	  agent.key = cnot.key;
	  agent.deploy();
	} else {
	  // This is a Server
	  if (cnot.key == null)
	    return;

	  if (socket == null) {
	    // There is no connection alive, accept it
	    if ((connectQueue != null) &&
		! connectQueue.isEmpty() &&
		! from.equals(getId())) {
	      // this is a new connection
	      // but older ones are waiting in the connection queue
	      cnot = (ConnectNot) connectQueue.firstElement();
	      connectQueue.removeElementAt(0);
	      connectQueue.addElement(not);
	    }
	    key = cnot.key;
	    reinitialize();
	  } else {
	    if (((connectQSize > 0) && (connectQueue.size() < connectQSize)) ||
		(connectQSize < 0)) {
	      connectQueue.addElement(not);
	    } else {
	      // rejects connection
	      ((Socket) sockets.get(cnot.key)).close();
	      sockets.remove(cnot.key);
	    }
	  }
	}
      } else {
	super.react(from, not);
      }
    } catch (Exception exc) {
      stop();
      throw exc;
    }
  }

  /**
    * Creates a (chain of) filter(s) for transforming the specified
    * <code>InputStream</code> into a <code>NotificationInputStream</code>.
    */
  protected abstract NotificationInputStream
    setInputFilters(InputStream in)
      throws StreamCorruptedException, IOException;

  /**
    * Creates a (chain of) filter(s) for transforming the specified
    * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
    */
  protected abstract NotificationOutputStream
    setOutputFilters(OutputStream out) throws IOException;
}
