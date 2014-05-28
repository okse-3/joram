package joram.bridgejms;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.JMSDistributionQueue;

public class AdminTest15x {
  javax.naming.Context jndiCtx = null;
  
  AdminTest15x () throws Exception {
    // Initializes JNDI context
    jndiCtx = new javax.naming.InitialContext();
  }
  
  public void localAdmin(ConnectionFactory cf, String client) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    int sid = AdminModule.getLocalServerId();
    
    // Creates the local user
    User.create("anonymous", "anonymous");
    
    // Creates the distribution queue
    Properties props = new Properties();
    props.setProperty("period", "1000");     
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("distribution.async", "true");
    Queue queue1 = JMSDistributionQueue.create(sid, "queue1", "queue1" + client, props);
    queue1.setFreeWriting();
    
    // Creates the acquisition queue
    props.clear();
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("persistent", "true");
    props.setProperty("acquisition.max_msg", "50");
    props.setProperty("acquisition.min_msg", "20");
    props.setProperty("acquisition.max_pnd", "200");
    props.setProperty("acquisition.min_pnd", "50");
    Queue queue2 = JMSAcquisitionQueue.create(sid, "queue2", "queue2" + client, props);
    queue2.setFreeReading();
    
    AdminModule.disconnect();
  }

  public void centralAdmin(ConnectionFactory cf, String clients) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    
    jndiCtx.rebind("centralCF", cf);

    // Creates the local user
    User.create("anonymous", "anonymous");
    
    String ctab[] = clients.split(":");
    for (int i=0; i<ctab.length; i++) {
      addClient(ctab[i]);
    }

    jndiCtx.close();
    AdminModule.disconnect();
  }
 
  public void addClient(String client) throws Exception {
    // Creates queue #1 for client
    Queue queue1 = Queue.create("queue1"+ client);
    queue1.setFreeReading();
    queue1.setFreeWriting();
    jndiCtx.rebind("queue1"+ client, queue1);
    System.out.println("creates queue1 = " + queue1);
    
    // Creates queue #2 for client
    Queue queue2 = Queue.create("queue2"+ client);
    queue2.setFreeReading();
    queue2.setFreeWriting();
    jndiCtx.rebind("queue2"+ client, queue2);
    System.out.println("creates queue2 = " + queue2);
  }
}
