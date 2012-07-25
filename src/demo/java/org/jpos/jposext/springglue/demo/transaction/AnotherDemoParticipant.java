package org.jpos.jposext.springglue.demo.transaction;

import java.io.Serializable;

import org.jpos.q2.Q2;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;

/**
 * @author dgrandemange
 *
 */
public class AnotherDemoParticipant implements TransactionParticipant, TransactionConstants {

	private static final Log logger = Log.getLog(Q2.LOGGER_NAME,
			AnotherDemoParticipant.class.getName());	
	
	public int prepare(long id, Serializable context) {
		LogEvent ev;
		ev=new LogEvent();
		ev.addMessage("Hi, this is AnotherDemoParticipant speaking and i haven't been injected at all, which is expected");
		logger.info(ev);
		
		return PREPARED | NO_JOIN;
	}

	public void commit(long id, Serializable context) {
	}

	public void abort(long id, Serializable context) {
	}

}
