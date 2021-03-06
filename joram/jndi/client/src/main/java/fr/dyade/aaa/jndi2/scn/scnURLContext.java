/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Initial developer(s): David Feliot
 * Contributor(s):
 */
package fr.dyade.aaa.jndi2.scn;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ResolveResult;

import fr.dyade.aaa.jndi2.client.NamingContextFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Context implementation for the "scn:comp" namespace.
 * Package must be named .../scn (See Initial Context)
 * Most operations consist of retrieving the actual CompNamingContext
 * and sending it the operation for processing.
 */
public class scnURLContext implements Context {

  public final static Logger logger;
  
  static {
    logger = fr.dyade.aaa.common.Debug.getLogger("fr.dyade.aaa.jndi2.scn");    
  }

  static private final String URL_PREFIX = "scn:comp/";
  static private final String ENV_PREFIX = "env";
    
  private Hashtable myEnv;
  private InitialContextFactory namingFactory;
    
  /**
   * Constructor
   */
  public scnURLContext(Hashtable env) throws NamingException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "scnURLContext.<init>(" + env + ')');
    if (env != null) {
      // clone env to be able to change it.
      myEnv = (Hashtable)env.clone();

      String factoryClassName = (String)myEnv.get("scn.naming.factory.class");
      if (factoryClassName == null) {
        namingFactory = new NamingContextFactory();
      } else {
        try {
          Class factoryClass = Class.forName(factoryClassName);
          namingFactory = 
            (InitialContextFactory)factoryClass.newInstance();
        } catch (Exception exc) {
          NamingException ne = new NamingException();
          ne.setRootCause(exc);
          throw ne;
        }
      }
    }
  }
  
  /*
   * get name without the url prefix
   */
  private String getRelativeName(String name) throws NamingException {
    // We suppose that all names must be prefixed as this
    if (!name.startsWith(URL_PREFIX)) {
      throw new NameNotFoundException("Invalid name:" + name);
    }

    name = name.substring(URL_PREFIX.length());
    return name;
  }

  /*
   * Resolve the name inside the javaURLContext
   * Result must be a Context + the name in this Context
   */
  private ResolveResult findContextFor(String name) throws NamingException {
    String rname = getRelativeName(name);
    Context context = null;

    if (rname.equals("")) {
      // null name refers to this context
      context = new scnURLContext(myEnv);
    } else {
      // other names are component independant
      context = namingFactory.getInitialContext(myEnv);
    }
    
    // Check context is not null to avoid nullPointerException
    if (context == null) {
      throw new NameNotFoundException("No context for this component");
    }
    
    // Build a ResolveResult object to return
    ResolveResult r = new ResolveResult(context, rname);
    return r;
  }

  // ------------------------------------------------------------------
  // Context implementation
  // ------------------------------------------------------------------

  /**
   * Retrieves the named object.
   *
   * @param name the name of the object to look up
   * @return	the object bound to name
   * @throws	NamingException if a naming exception is encountered
   */
  public Object lookup(Name name) throws NamingException {
    // Just use the string version for now.
    return lookup(name.toString());
  }

  /**
   * Retrieves the named object.
   *
   * @param name the name of the object to look up
   * @return	the object bound to name
   * @throws	NamingException if a naming exception is encountered
   */
  public Object lookup(String name) throws NamingException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "scnURLContext.lookup(" + name + ')');
    // Name empty: returns a new instance of this context.
    if (name.equals("")) {
      return new scnURLContext(myEnv);
    }

    // Retrieve the correct context to resolve the reminding name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();
    

    Object o =  ctx.lookup(rname);
    return o;
    ////////////////////////////
    // Resolve the name in its proper context
    //return ctx.lookup(rname);
  }
    
  /**
   * Binds a name to an object.
   *
   * @param name the name to bind; may not be empty
   * @param obj the object to bind; possibly null
   * @throws	NameAlreadyBoundException if name is already bound
   * @throws	javax.naming.directory.InvalidAttributesException
   *	 	if object did not supply all mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #bind(String, Object)
   * @see #rebind(Name, Object)
   * @see javax.naming.directory.DirContext#bind(Name, Object,
   *		javax.naming.directory.Attributes)
   */
  public void bind(Name name, Object obj) throws NamingException {
    // Just use the string version for now.
    bind(name.toString(), obj);
  }

  /**
   * Binds a name to an object.
   * All intermediate contexts and the target context (that named by all
   * but terminal atomic component of the name) must already exist.
   *
   * @param name
   *		the name to bind; may not be empty
   * @param obj
   *		the object to bind; possibly null
   * @throws	NameAlreadyBoundException if name is already bound
   * @throws	javax.naming.directory.InvalidAttributesException
   *	 	if object did not supply all mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   */
  public void bind(String name, Object obj) throws NamingException {
    // Retrieve the correct context for this name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // Bind the name in its proper context
    ctx.bind(rname, obj);
  }

  /**
   * Binds a name to an object, overwriting any existing binding.
   * All intermediate contexts and the target context (that named by all
   * but terminal atomic component of the name) must already exist.
   *
   * If the object is a DirContext, any existing attributes
   * associated with the name are replaced with those of the object.
   * Otherwise, any existing attributes associated with the name remain
   * unchanged.
   *
   * @param name
   *		the name to bind; may not be empty
   * @param obj
   *		the object to bind; possibly null
   * @throws	javax.naming.directory.InvalidAttributesException
   *	 	if object did not supply all mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   *
   */
  public void rebind(Name name, Object obj) throws NamingException {
    // Just use the string version for now.
    rebind(name.toString(), obj);
  }

  /**
   * Binds a name to an object, overwriting any existing binding.
   * See {@link #rebind(Name, Object)} for details.
   *
   * @param name
   *		the name to bind; may not be empty
   * @param obj
   *		the object to bind; possibly null
   * @throws	javax.naming.directory.InvalidAttributesException
   *	 	if object did not supply all mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   */
  public void rebind(String name, Object obj) throws NamingException {
    // Retrieve the correct context for this name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // Rebind the name in its proper context
    ctx.rebind(rname, obj);
  }

  /**
   * Unbinds the named object.
   * Removes the terminal atomic name in name
   * from the target context--that named by all but the terminal
   * atomic part of name.
   *
   * This method is idempotent.
   * It succeeds even if the terminal atomic name
   * is not bound in the target context, but throws
   * NameNotFoundException
   * if any of the intermediate contexts do not exist.
   *
   * Any attributes associated with the name are removed.
   * Intermediate contexts are not changed.
   *
   * @param name
   *		the name to unbind; may not be empty
   * @throws	NameNotFoundException if an intermediate context does not exist
   * @throws	NamingException if a naming exception is encountered
   * @see #unbind(String)
   */
  public void unbind(Name name) throws NamingException {
    // Just use the string version for now.
    unbind(name.toString());
  }

  /**
   * Unbinds the named object.
   * See {@link #unbind(Name)} for details.
   *
   * @param name
   *		the name to unbind; may not be empty
   * @throws	NameNotFoundException if an intermediate context does not exist
   * @throws	NamingException if a naming exception is encountered
   */
  public void unbind(String name) throws NamingException {
    // Retrieve the correct context for this name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // Unbind the name in its proper context
    ctx.unbind(rname);
  }

  /**
   * Binds a new name to the object bound to an old name, and unbinds
   * the old name. This operation is not supported (read only env.)
   *
   * @param oldName
   *		the name of the existing binding; may not be empty
   * @param newName
   *		the name of the new binding; may not be empty
   * @throws	NamingException if a naming exception is encountered
   */
  public void rename(Name oldName, Name newName) throws NamingException {
    // Just use the string version for now.
    rename(oldName.toString(), newName.toString());
  }

  /**
   * Binds a new name to the object bound to an old name, and unbinds
   * the old name. Not supported.
   *
   * @param oldName
   *		the name of the existing binding; may not be empty
   * @param newName
   *		the name of the new binding; may not be empty
   * @throws	NamingException if a naming exception is encountered
   */
  public void rename(String oldName, String newName) throws NamingException {
    throw new OperationNotSupportedException("Rename not supported in scn:comp in " + this.getClass().getName());
  }

  /**
   * Enumerates the names bound in the named context, along with the
   * class names of objects bound to them.
   * The contents of any subcontexts are not included.
   *
   * If a binding is added to or removed from this context,
   * its effect on an enumeration previously returned is undefined.
   *
   * @param name
   *		the name of the context to list
   * @return	an enumeration of the names and class names of the
   *		bindings in this context.  Each element of the
   *		enumeration is of type NameClassPair.
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #list(String)
   * @see #listBindings(Name)
   * @see NameClassPair
   */
  public NamingEnumeration list(Name name) throws NamingException {
    // Just use the string version for now.
    return list(name.toString());
  }

  /**
   * Enumerates the names bound in the named context, along with the
   * class names of objects bound to them.
   * See {@link #list(Name)} for details.
   *
   * @param name
   *		the name of the context to list
   * @return	an enumeration of the names and class names of the
   *		bindings in this context.  Each element of the
   *		enumeration is of type NameClassPair.
   * @throws	NamingException if a naming exception is encountered
   */
  public NamingEnumeration list(String name) throws NamingException {
    // Retrieve the correct context to resolve the reminding name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // List the correct Context
    return ctx.list(rname);
  }

  /**
   * Enumerates the names bound in the named context, along with the
   * objects bound to them.
   * The contents of any subcontexts are not included.
   *
   * If a binding is added to or removed from this context,
   * its effect on an enumeration previously returned is undefined.
   *
   * @param name
   *		the name of the context to list
   * @return	an enumeration of the bindings in this context.
   *		Each element of the enumeration is of type
   *		Binding.
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #listBindings(String)
   * @see #list(Name)
   * @see Binding
   */
  public NamingEnumeration listBindings(Name name) throws NamingException {
    // Just use the string version for now.
    return listBindings(name.toString());
  }

  /**
   * Enumerates the names bound in the named context, along with the
   * objects bound to them.
   * See {@link #listBindings(Name)} for details.
   *
   * @param name
   *		the name of the context to list
   * @return	an enumeration of the bindings in this context.
   *		Each element of the enumeration is of type
   *		Binding.
   * @throws	NamingException if a naming exception is encountered
   */
  public NamingEnumeration listBindings(String name) throws NamingException {
    // Retrieve the correct context to resolve the reminding name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // List the correct Context
    return ctx.listBindings(rname);
  }

  /**
   * Destroys the named context and removes it from the namespace.
   * Any attributes associated with the name are also removed.
   * Intermediate contexts are not destroyed.
   *
   * This method is idempotent.
   * It succeeds even if the terminal atomic name
   * is not bound in the target context, but throws
   * NameNotFoundException
   * if any of the intermediate contexts do not exist.
   *
   * In a federated naming system, a context from one naming system
   * may be bound to a name in another.  One can subsequently
   * look up and perform operations on the foreign context using a
   * composite name.  However, an attempt destroy the context using
   * this composite name will fail with
   * NotContextException, because the foreign context is not
   * a "subcontext" of the context in which it is bound.
   * Instead, use unbind() to remove the
   * binding of the foreign context.  Destroying the foreign context
   * requires that the destroySubcontext() be performed
   * on a context from the foreign context's "native" naming system.
   *
   * @param name
   *		the name of the context to be destroyed; may not be empty
   * @throws	NameNotFoundException if an intermediate context does not exist
   * @throws	NotContextException if the name is bound but does not name a
   *		context, or does not name a context of the appropriate type
   * @throws	ContextNotEmptyException if the named context is not empty
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #destroySubcontext(String)
   */
  public void destroySubcontext(Name name) throws NamingException {
    // Just use the string version for now.
    destroySubcontext(name.toString());
  }

  /**
   * Destroys the named context and removes it from the namespace.
   * See {@link #destroySubcontext(Name)} for details.
   *
   * @param name
   *		the name of the context to be destroyed; may not be empty
   * @throws	NameNotFoundException if an intermediate context does not exist
   * @throws	NotContextException if the name is bound but does not name a
   *		context, or does not name a context of the appropriate type
   * @throws	ContextNotEmptyException if the named context is not empty
   * @throws	NamingException if a naming exception is encountered
   */
  public void destroySubcontext(String name) throws NamingException {
    throw new OperationNotSupportedException("destroySubcontext not supported in scn:comp in " + this.getClass().getName());
  }

  /**
   * Creates and binds a new context.
   * Creates a new context with the given name and binds it in
   * the target context (that named by all but terminal atomic
   * component of the name).  All intermediate contexts and the
   * target context must already exist.
   *
   * @param name
   *		the name of the context to create; may not be empty
   * @return	the newly created context
   *
   * @throws	NameAlreadyBoundException if name is already bound
   * @throws	javax.naming.directory.InvalidAttributesException
   *		if creation of the subcontext requires specification of
   *		mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #createSubcontext(String)
   * @see javax.naming.directory.DirContext#createSubcontext
   */
  public Context createSubcontext(Name name) throws NamingException {
    // Just use the string version for now.
    return createSubcontext(name.toString());
  }

  /**
   * Creates and binds a new context.
   * See {@link #createSubcontext(Name)} for details.
   *
   * @param name
   *		the name of the context to create; may not be empty
   * @return	the newly created context
   *
   * @throws	NameAlreadyBoundException if name is already bound
   * @throws	javax.naming.directory.InvalidAttributesException
   *		if creation of the subcontext requires specification of
   *		mandatory attributes
   * @throws	NamingException if a naming exception is encountered
   */
  public Context createSubcontext(String name) throws NamingException {
    // Retrieve the correct context to resolve the reminding name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();
    
    // create subcontext
    return ctx.createSubcontext(rname);
  }

  /**
   * Retrieves the named object, following links except
   * for the terminal atomic component of the name.
   * If the object bound to name is not a link,
   * returns the object itself.
   *
   * @param name
   *		the name of the object to look up
   * @return	the object bound to name, not following the
   *		terminal link (if any).
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #lookupLink(String)
   */
  public Object lookupLink(Name name) throws NamingException {
    // Just use the string version for now.
    return lookupLink(name.toString());
  }

  /**
   * Retrieves the named object, following links except
   * for the terminal atomic component of the name.
   * See {@link #lookupLink(Name)} for details.
   *
   * @param name
   *		the name of the object to look up
   * @return	the object bound to name, not following the
   *		terminal link (if any)
   * @throws	NamingException if a naming exception is encountered
   */
  public Object lookupLink(String name) throws NamingException {
    // Name empty: returns a new instance of this context.
    if (name.equals("")) {
      return new scnURLContext(myEnv);
    }

    // Retrieve the correct context to resolve the reminding name
    ResolveResult r = findContextFor(name);
    Context ctx = (Context) r.getResolvedObj();
    String rname = r.getRemainingName().toString();

    // Resolve the name in its proper context
    return ctx.lookupLink(rname);
  }

  /**
   * Retrieves the parser associated with the named context.
   * In a federation of namespaces, different naming systems will
   * parse names differently.  This method allows an application
   * to get a parser for parsing names into their atomic components
   * using the naming convention of a particular naming system.
   * Within any single naming system, NameParser objects
   * returned by this method must be equal (using the equals()
   * test).
   *
   * @param name
   *		the name of the context from which to get the parser
   * @return	a name parser that can parse compound names into their atomic
   *		components
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #getNameParser(String)
   * @see CompoundName
   */
  public NameParser getNameParser(Name name) throws NamingException {
    return null;
  }

  /**
   * Retrieves the parser associated with the named context.
   * See {@link #getNameParser(Name)} for details.
   *
   * @param name
   *		the name of the context from which to get the parser
   * @return	a name parser that can parse compound names into their atomic
   *		components
   * @throws	NamingException if a naming exception is encountered
   */
  public NameParser getNameParser(String name) throws NamingException {
    return null;
  }

  /**
   * Composes the name of this context with a name relative to
   * this context.
   *
   * @param name
   *		a name relative to this context
   * @param prefix
   *		the name of this context relative to one of its ancestors
   * @return	the composition of prefix and name
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #composeName(String, String)
   */
  public Name composeName(Name name, Name prefix) throws NamingException {
    throw new OperationNotSupportedException("scnURLContext: composeName not supported");
  }

  /**
   * Composes the name of this context with a name relative to
   * this context: Not supported.
   *
   * @param name
   *		a name relative to this context
   * @param prefix
   *		the name of this context relative to one of its ancestors
   * @return	the composition of prefix and name
   * @throws	NamingException if a naming exception is encountered
   */
  public String composeName(String name, String prefix) throws NamingException {
    throw new OperationNotSupportedException("composeName not supported in scn:comp in " + this.getClass().getName());
  }

  /**
   * Adds a new environment property to the environment of this
   * context.  If the property already exists, its value is overwritten.
   * See class description for more details on environment properties.
   *
   * @param propName
   *		the name of the environment property to add; may not be null
   * @param propVal
   *		the value of the property to add; may not be null
   * @return	the previous value of the property, or null if the property was
   *		not in the environment before
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #getEnvironment()
   * @see #removeFromEnvironment(String)
   */
  public Object addToEnvironment(String propName, Object propVal) throws NamingException {
    if (myEnv == null) {
      myEnv = new Hashtable();
    } 
    return myEnv.put(propName, propVal);
  }

  /**
   * Removes an environment property from the environment of this
   * context.  See class description for more details on environment
   * properties.
   *
   * @param propName
   *		the name of the environment property to remove; may not be null
   * @return	the previous value of the property, or null if the property was
   *		not in the environment
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #getEnvironment()
   * @see #addToEnvironment(String, Object)
   */
  public Object removeFromEnvironment(String propName) throws NamingException {
    if (myEnv == null) {
      return null;
    }
    return myEnv.remove(propName);
  }

  /**
   * Retrieves the environment in effect for this context.
   * See class description for more details on environment properties.
   *
   * The caller should not make any changes to the object returned:
   * their effect on the context is undefined.
   * The environment of this context may be changed using
   * addToEnvironment() and removeFromEnvironment().
   *
   * @return	the environment of this context; never null
   * @throws	NamingException if a naming exception is encountered
   *
   * @see #addToEnvironment(String, Object)
   * @see #removeFromEnvironment(String)
   */
  public Hashtable getEnvironment() throws NamingException {
    if (myEnv == null) {
      myEnv = new Hashtable();
    } 
    return myEnv;
  }

  /**
   * Closes this context.
   * This method releases this context's resources immediately, instead of
   * waiting for them to be released automatically by the garbage collector.
   *
   * This method is idempotent:  invoking it on a context that has
   * already been closed has no effect.  Invoking any other method
   * on a closed context is not allowed, and results in undefined behaviour.
   *
   * @throws	NamingException if a naming exception is encountered
   */
  public void close() throws NamingException {
    myEnv = null;
  }

  /**
   * Retrieves the full name of this context within its own namespace.
   * Not implemented.
   *
   * @return	this context's name in its own namespace; never null
   * @throws	OperationNotSupportedException if the naming system does
   *		not have the notion of a full name
   * @throws	NamingException if a naming exception is encountered
   */
  public String getNameInNamespace() throws NamingException {
    throw new OperationNotSupportedException("getNameInNamespace not implemented in scn:comp in " + this.getClass().getName());
  }
}

