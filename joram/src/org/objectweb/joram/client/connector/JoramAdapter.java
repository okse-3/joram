/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.client.connector;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Debug;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.local.QueueLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.TopicLocalConnectionFactory;
import org.objectweb.joram.client.jms.local.XALocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.CommException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/** 
 * A <code>JoramAdapter</code> instance manages connectivities to an
 * underlying JORAM server: outbound connectivity (JCA connection
 * management contract) and inbound connectivity (asynchronous message
 * delivery as specified by the JCA message inflow contract).
 */
public class JoramAdapter implements javax.resource.spi.ResourceAdapter,
                                     java.io.Serializable
{
  /**
   * Path to the directory containing the underlying JORAM platform
   * <code>a3servers.xml</code> configuration file, needed in the
   * collocated case.
   */
  private String platformConfigDir;
  /** <code>true</code> if the underlying JORAM platform is persistent. */
  private boolean persistentPlatform = false;
  /** Identifier of the underlying JORAM server. */
  private short serverId = 0;
  /** Name of the underlying JORAM server. */
  private String serverName = "s0";

  /**
   * Path to the file containing a description of the administered objects to
   * create and bind.
   */
  private String adminFile = "joram-admin.properties";

  /** <code>WorkManager</code> instance provided by the application server. */
  private transient WorkManager workManager;

  /**
   * Table holding the adapter's <code>InboundConsumer</code> instances,
   * for inbound messaging.
   * <p>
   * <b>Key:</b> <code>ActivationSpec</code> instance<br>
   * <b>Value:</b> <code>InboundConsumer</code> instance
   */
  private transient Hashtable consumers;
  /**
   * Vector holding the <code>ManagedConnectionImpl</code> instances for
   * managed outbound messaging.
   */
  private transient Vector producers;
  /** <code>true</code> if the adapter has been started. */
  private boolean started = false;
  /** <code>true</code> if the adapter has been stopped. */
  private boolean stopped = false;

  /** <code>true</code> if the underlying JORAM server is collocated. */
  boolean collocated = false;
  /** Host name or IP of the underlying JORAM server. */
  String hostName = "localhost";
  /** Port number of the underlying JORAM server. */
  int serverPort = 16010;


  /**
   * Constructs a <code>JoramAdapter</code> instance. 
   */
  public JoramAdapter()
  {
    debug("JORAM adapter instanciated.");

    consumers = new Hashtable();
    producers = new Vector();
  }
  

  /**
   * Initializes the adapter; starts, if needed, a collocated JORAM server, 
   * and if needed again, administers it.
   *
   * @exception ResourceAdapterInternalException  If the adapter could not be
   *                                              initialized.
   */
  public synchronized void start(BootstrapContext ctx)
                           throws ResourceAdapterInternalException
  {
    if (started)
      throw new ResourceAdapterInternalException("Adapter already started.");
    if (stopped)
      throw new ResourceAdapterInternalException("Adapter has been stopped.");

    debug("JORAM adapter starting deployment...");

    workManager = ctx.getWorkManager();

    // Collocated mode: starting the JORAM server. 
    if (collocated) {

      debug("  - Collocated JORAM server is starting...");

      if (persistentPlatform)
        System.setProperty("Transaction", "fr.dyade.aaa.util.ATransaction");   
      else
        System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");

      if (platformConfigDir != null)
        System.setProperty("fr.dyade.aaa.agent.A3CONF_DIR", platformConfigDir);

      try {
        String[] args = {"" + serverId, serverName};
        AgentServer.init(args);
        AgentServer.start();
        debug("  - Collocated JORAM server has successfully started.");
      }
      catch (Exception exc) {
        throw new ResourceAdapterInternalException("Could not start "
                                                   + "collocated JORAM "
                                                   + " instance: " + exc);
      }
    }

    // Administering as specified in the properties file.
    try {
      FileReader file = new FileReader(adminFile);
      BufferedReader reader = new BufferedReader(file);

      debug("  - Reading the provided admin file...");

      boolean end = false;
      String line;
      StringTokenizer tokenizer;
      String firstToken;
      String name = null;

      while (! end) {
        try {
          line = reader.readLine();

          if (line == null)
            end = true;
          else {
            tokenizer = new StringTokenizer(line);

            if (tokenizer.hasMoreTokens()) {
              firstToken = tokenizer.nextToken();
              if (firstToken.equalsIgnoreCase("Queue")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createQueue(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("Topic")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createTopic(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("User")) {
                if (tokenizer.hasMoreTokens())
                  name = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens()) {
                  String password = tokenizer.nextToken();
                  createUser(name, password);
                }
                else
                  debug("  - Missing password for user [" + name + "]");
              }
              else if (firstToken.equalsIgnoreCase("CF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createCF(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("QCF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createQCF(name);
                }
              }
              else if (firstToken.equalsIgnoreCase("TCF")) {
                if (tokenizer.hasMoreTokens()) {
                  name = tokenizer.nextToken();
                  createTCF(name);
                }
              }
            }
          }
        }
        // Error while reading one line.
        catch (IOException exc) {}
        // Error while creating the destination.
        catch (AdminException exc) {
          debug("  CREATION FAILED: " + exc);
        }
        // JNDI error.
        catch (NamingException exc) {
          debug("  BINDING FAILED: " + exc);
        }
      }
    }
    // No destination to deploy.
    catch (java.io.FileNotFoundException fnfe) { 
      debug("  - No administration task requested.");
    }

    debug("JORAM adapter successfully deployed.");

    started = true;
  }

  /**	
   * Notifies the adapter to terminate the connections it manages, and if
   * needed, to shut down the collocated JORAM server.
   */
  public synchronized void stop()
  {
    if (! started || stopped)
      return;

    // Finishing the admin session.
    AdminModule.disconnect();

    // Closing the outbound connections, if any.
    while (! producers.isEmpty()) {
      try {
        ((ManagedConnectionImpl) producers.remove(0)).destroy();
      }
      catch (Exception exc) {}
    }

    // Closing the inbound connections, if any.
    for (Enumeration keys = consumers.keys(); keys.hasMoreElements();)
      ((InboundConsumer) consumers.get(keys.nextElement())).close();

    // If JORAM server is collocated, stopping it.
    if (collocated) {
      try {
        AgentServer.stop();
      }
      catch (Exception exc) {}
    }
    stopped = true;
  }

  /**
   * Notifies the adapter to setup asynchronous message delivery for an
   * application server endoint.
   *
   * @exception IllegalStateException  If the adapter is either not started,
   *                                   or stopped.
   * @exception NotSupportedException  If the provided activation parameters 
   *                                   are invalid.
   * @exception CommException          If the JORAM server is not reachable.
   * @exception SecurityException      If connecting is not allowed.
   * @exception ResourceException      Generic exception.
   */
  public void endpointActivation(MessageEndpointFactory endpointFactory,
                                 ActivationSpec spec)
              throws ResourceException
  {
    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    if (! (spec instanceof ActivationSpecImpl))
      throw new ResourceException("Provided ActivationSpec instance is not "
                                  + "a JORAM activation spec.");

    ActivationSpecImpl specImpl = (ActivationSpecImpl) spec;

    if (! specImpl.getResourceAdapter().equals(this))
      throw new ResourceException("Supplied ActivationSpec instance "
                                  + "associated to an other ResourceAdapter.");

    boolean durable =
      specImpl.getSubscriptionDurability() != null
      && specImpl.getSubscriptionDurability().equalsIgnoreCase("Durable");

    boolean transacted = false;
    try {
      Class listenerClass = Class.forName("javax.jms.MessageListener");
      Class[] parameters = { Class.forName("javax.jms.Message") };
      Method meth = listenerClass.getMethod("onMessage", parameters);
      transacted = endpointFactory.isDeliveryTransacted(meth);
    }
    catch (Exception exc) {
      throw new ResourceException("Could not determine transactional "
                                  + "context: " + exc);
    }

    int maxWorks = 0;
    try {
      maxWorks = Integer.parseInt(specImpl.getMaxNumberOfWorks());
    }
    catch (Exception exc) {
      throw new ResourceException("Invalid max number of works instances "
                                  + "number: " + exc);
    }

    String destType = specImpl.getDestinationType();
    String destName = specImpl.getDestination();

    try {
      Destination dest;

      if (destType.equals("javax.jms.Queue"))
        dest = createQueue(destName);
      else if (destType.equals("javax.jms.Topic"))
        dest = createTopic(destName);
      else
        throw new NotSupportedException("Invalid destination type provided "
                                        + "as activation parameter: "
                                        + destType);

      String userName = specImpl.getUserName();
      String password = specImpl.getPassword();

      createUser(userName, password);

      XAConnectionFactory connectionFactory = null;

      if (collocated)
        connectionFactory = XALocalConnectionFactory.create();
      else
        connectionFactory =
          XATcpConnectionFactory.create(hostName, serverPort);

      XAConnection cnx =
        connectionFactory.createXAConnection(userName, password);

      // Creating and registering a consumer instance for this endpoint.
      InboundConsumer consumer =
        new InboundConsumer(workManager,
                            endpointFactory,
                            cnx,
                            dest,
                            specImpl.getMessageSelector(),
                            durable,
                            specImpl.getSubscriptionName(),
                            transacted,
                            maxWorks);

      consumers.put(specImpl, consumer);
    }
    catch (javax.jms.JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    }
    catch (javax.jms.JMSException exc) {
      throw new CommException("Could not connect to the JORAM server: "
                              + exc);
    }
    catch (AdminException exc) {
      throw new ResourceException("Problem when handling the JORAM "
                                  + "destinations: " + exc);
    }
    catch (NamingException exc) {
      throw new ResourceException("Problem when handling the JORAM "
                                  + "destinations: " + exc);
    }
  }

  /**
   * Notifies the adapter to deactivate message delivery for a given endpoint.
   */
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                   ActivationSpec spec)
  {
    if (! started || stopped)
      return;

    ((InboundConsumer) consumers.remove(spec)).close();
  }

  /** 
   *
   * @exception IllegalStateException  If the adapter is either not started,
   *                                   or stopped.
   */
  public XAResource[] getXAResources(ActivationSpec[] specs)
                      throws ResourceException
  {
    if (! started)
      throw new IllegalStateException("Non started resource adapter.");
    if (stopped)
      throw new IllegalStateException("Stopped resource adapter.");

    return null;
  }


  /** Returns a code depending on the adapter properties. */
  public int hashCode()
  {
    return (collocated + " " + hostName + " " + serverPort).hashCode();
  }

  /** Compares adapters according to their properties. */
  public boolean equals(Object o)
  {
    if (! (o instanceof JoramAdapter))
      return false;

    JoramAdapter other = (JoramAdapter) o;

    return collocated == other.collocated
           && hostName.equals(other.hostName)
           && serverPort == other.serverPort;
  }


  /**
   * Creates a queue destination on the underlying JORAM server, binds the
   * corresponding <code>Queue</code> instance.
   *
   * @exception AdminException   If the creation fails.
   * @exception NamingException  If the binding fails.
   */
  Destination createQueue(String name) throws AdminException, NamingException
  {
    try {
      adminConnect();
      Queue queue = Queue.create(name);
      queue.setFreeReading();
      queue.setFreeWriting();
      debug("  - Queue [" + name + "] has been created.");
      Context ctx = new InitialContext();
      ctx.bind(name, queue);
      return queue;
    }
    catch (ConnectException exc) {
      throw new AdminException("Admin connection has been lost.");
    }
  }

  /**
   * Creates a topic destination on the underlying JORAM server, binds the
   * corresponding <code>Topic</code> instance.
   *
   * @exception AdminException   If the creation fails.
   * @exception NamingException  If the binding fails.
   */
  Destination createTopic(String name) throws AdminException, NamingException
  {
    try {
      adminConnect();
      Topic topic = Topic.create(name);
      topic.setFreeReading();
      topic.setFreeWriting();
      debug("  - Topic [" + name + "] has been created.");
      Context ctx = new InitialContext();
      ctx.bind(name, topic);
      return topic;
    }
    catch (ConnectException exc) {
      throw new AdminException("Admin connection has been lost.");
    }
  }

  /**
   * Creates a user on the underlying JORAM server.
   *
   * @exception AdminException   If the creation fails.
   */
  void createUser(String name, String password) throws AdminException
  {
    try {
      adminConnect();
      User.create(name, password);
      debug("  - User [" + name + "] has been created.");
    }
    catch (ConnectException exc) {
      throw new AdminException("Admin connection has been lost.");
    }
  }

  /**
   * Creates a non managed connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      Context ctx = new InitialContext();
      ctx.bind(name, factory);
      debug("  - ConnectionFactory [" + name
            + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
  }

  /**
   * Creates a non managed PTP connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createQCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedQueueConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      Context ctx = new InitialContext();
      ctx.bind(name, factory);
      debug("  - QueueConnectionFactory [" + name
            + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
  }

  /**
   * Creates a non managed PubSub connection factory and binds it to JNDI.
   *
   * @exception NamingException  If the binding fails.
   */
  void createTCF(String name) throws NamingException
  {
    ManagedConnectionFactoryImpl mcf = 
      new ManagedTopicConnectionFactoryImpl();

    try {
      mcf.setResourceAdapter(this);
      mcf.setCollocated(new Boolean(false));

      Object factory = mcf.createConnectionFactory();
      Context ctx = new InitialContext();
      ctx.bind(name, factory);
      debug("  - TopicConnectionFactory [" + name
            + "] has been created and bound.");
    }
    catch (NamingException exc) {
      throw exc;
    }
    catch (Exception exc) {}
  }

  /**
   * Initiates an admin session.
   *
   * @exception AdminException  If the admin session could not be started.
   */
  void adminConnect() throws AdminException
  {
    try {
      TopicConnectionFactory factory;
      if (collocated)
        factory = TopicLocalConnectionFactory.create();
      else
        factory = TopicTcpConnectionFactory.create(hostName, serverPort);
   
      ((org.objectweb.joram.client.jms.ConnectionFactory) factory)
        .getParameters().connectingTimer = 60;

      AdminModule.connect(factory, "root", "root");
    }
    catch (ConnectException exc) {
      throw new AdminException("Admin connection can't be established.");
    }
  }

  /** Adds a given managed connection to the list of producers. */
  void addProducer(ManagedConnectionImpl managedCx)
  {
    producers.add(managedCx);
  }

  /** Removes a given managed connection from the list of producers. */
  void removeProducer(ManagedConnectionImpl managedCx)
  {
    producers.remove(managedCx);
  }

  /** Debugging method. */
  private void debug(String message)
  {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, message);
  }

  /** Deserializing method. */
  private void readObject(java.io.ObjectInputStream in)
          throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    consumers = new Hashtable();
    producers = new Vector();
  }

  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------
  public void setPlatformConfigDir(java.lang.String platformConfigDir)
  {
    this.platformConfigDir = platformConfigDir;
  }

  public void setPersistentPlatform(java.lang.Boolean persistentPlatform)
  {
    this.persistentPlatform = persistentPlatform.booleanValue();
  }

  public void setServerId(java.lang.Short serverId)
  {
    this.serverId = serverId.shortValue();
  }

  public void setServerName(java.lang.String serverName)
  {
    this.serverName = serverName;
  }

  public void setAdminFile(java.lang.String adminFile)
  {
    this.adminFile = adminFile;
  }

  public void setCollocatedServer(java.lang.Boolean collocatedServer)
  {
    collocated = collocatedServer.booleanValue();
  }

  public void setHostName(java.lang.String hostName)
  {
    this.hostName = hostName;
  }

  public void setServerPort(java.lang.Integer serverPort)
  {
    this.serverPort = serverPort.intValue();
  }
  
  public java.lang.String getPlatformConfigDir()
  {
    return platformConfigDir;
  }

  public java.lang.Boolean getPersistentPlatform()
  {
    return new Boolean(persistentPlatform);
  }

  public java.lang.Short getServerId()
  {
    return new Short(serverId);
  }

  public java.lang.String getServerName()
  {
    return serverName;
  }

  public java.lang.String getAdminFile()
  {
    return adminFile;
  }

  public java.lang.Boolean getCollocatedServer()
  {
    return new Boolean(collocated);
  }

  public java.lang.String getHostName()
  {
    return hostName;
  }

  public java.lang.Integer getServerPort()
  {
    return new Integer(serverPort);
  }
}

/**
 * Utility class for logging.
 */
class AdapterTracing
{
  public static Logger dbgAdapter = null;
  private static boolean initialized = false;

  static
  {
    dbgAdapter =
      Debug.getLogger("org.objectweb.joram.client.connector.Adapter");
  }
}
