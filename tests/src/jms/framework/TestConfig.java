/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.framework;
import java.util.Properties;

/**
 * Class used to provide configurable options in a convenient way
 *
 * @author Jeff Mesnil (jmesnil@inrialpes.fr)
 * @version $Id: TestConfig.java,v 1.1 2008-01-10 13:21:30 tachker Exp $
 */
public class TestConfig {

    // name of the configuration file
    private static final String PROP_FILE_NAME = "test.properties";

    // name of the timeout property
    private static final String PROP_NAME = "timeout";

    /**
     * timeout value used by <code>receive</code> method in the tests. 
     * the value is specified in the <code>config/test.properties</code> file.
     */
    public static final long TIMEOUT;
    
    static {
	// load tests.properties	 
	long tempTimeOut = 0;
	try {
	    Properties props = new Properties();
	    props.load(ClassLoader.getSystemResourceAsStream(PROP_FILE_NAME));
	    props.list(System.out);
	    tempTimeOut = Long.parseLong(props.getProperty(PROP_NAME, "0"));
	} catch (Exception e) {
	    tempTimeOut = 0;
 	} finally {
	    TIMEOUT = tempTimeOut;
	}
    }
}
