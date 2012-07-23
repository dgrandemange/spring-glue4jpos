package org.jpos.jposext.springglue.demo.transaction;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.jposext.springglue.demo.DemoQBean;
import org.jpos.q2.Q2;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;

/**
 * @author dgrandemange
 *
 */
public class DemoParticipant implements TransactionParticipant, TransactionConstants {

	private static final Log logger = Log.getLog(Q2.LOGGER_NAME,
			DemoParticipant.class.getName());	
	
	private static final String DEMO_Q_BEAN_NAMEREGISTRAR_ENTRYNAME = "demoqbean";
	
	/**
	 * Some map to be further Spring injected
	 */
	private Map<String, String> map;
	
	public int prepare(long id, Serializable context) {
		LogEvent ev;
		
		ev=new LogEvent();
		ev.addMessage("Dump of the map injected in the DemoParticipant participant");
		for (Entry<String, String> entry : map.entrySet()) {
			ev.addMessage(String.format("%s -> %s", entry.getKey(), entry.getValue()));
		}
		logger.info(ev);
				
		String demoQBeanKey=DEMO_Q_BEAN_NAMEREGISTRAR_ENTRYNAME;
		try {
			DemoQBean demoQBean = (DemoQBean) NameRegistrar.get(demoQBeanKey);
			ev= new LogEvent();
			ev.addMessage(String.format("Dump of the map injected in the NameRegistrar entry '%s'", demoQBeanKey));
			for (Entry<String, String> entry : demoQBean.getMap().entrySet()) {
				ev.addMessage(String.format("%s -> %s", entry.getKey(), entry.getValue()));
			}
			logger.info(ev);
		} catch (NotFoundException e) {
			// Shouldn't append in this demo
			logger.warn(String.format("NameRegistrar entry '%s' not found", demoQBeanKey));
		}
		
		return PREPARED | NO_JOIN;
	}

	public void commit(long id, Serializable context) {
	}

	public void abort(long id, Serializable context) {
	}

	/**
	 * @return the map
	 */
	public Map<String, String> getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(Map<String, String> map) {
		this.map = map;
	}

}
