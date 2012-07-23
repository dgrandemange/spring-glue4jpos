package org.jpos.jposext.springglue.demo;

import java.util.Map;

import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

/**
 * @author dgrandemange
 * 
 */
public class DemoQBean extends QBeanSupport {

	/**
	 * The dependency to inject
	 */
	private Map<String, String> map;

	@Override
	protected void startService() throws Exception {
		new Thread(new DemoThread(this)).start();
		
		NameRegistrar.register(getName(), this);
	}

	/**
	 * @return the map
	 */
	public Map<String, String> getMap() {
		return map;
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(Map<String, String> map) {
		this.map = map;
	}
}
