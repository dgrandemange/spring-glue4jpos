package org.jpos.jposext.springglue;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @author dgrandemange
 *
 */
public class JMXHelper {
	
	static MBeanServer findFirstMBeanServerById(String id) {
		ArrayList<MBeanServer> foundMBeanServers = MBeanServerFactory.findMBeanServer(id);
		if (foundMBeanServers.size()>0) {
			return foundMBeanServers.get(0);
		}
		else {
			return null;
		}
	}
}
