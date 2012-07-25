package org.jpos.transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.q2.Q2;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.PriorityOrdered;

/**
 * @author dgrandemange
 * 
 */
public class TxnMgrParticipantsSpringGlue implements BeanFactoryPostProcessor,
		PriorityOrdered {

	private static final Log logger = Log.getLog(Q2.LOGGER_NAME,
			TxnMgrParticipantsSpringGlue.class.getName());

	private String txnmgrRegistrationName;

	private static final int DEFAULT_PROCESSING_DELAY = 10000;

	private int processingDelay = DEFAULT_PROCESSING_DELAY;

	private int order;

	public TxnMgrParticipantsSpringGlue() {
		order = 0x7fffffff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor#
	 * postProcessBeanFactory
	 * (org.springframework.beans.factory.config.ConfigurableListableBeanFactory
	 * )
	 */
	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
			throws BeansException {
		TransactionManager txnmgr = null;

		long startTime = Calendar.getInstance().getTimeInMillis();
		long now = Calendar.getInstance().getTimeInMillis();

		while (((now - startTime) < processingDelay) && (txnmgr == null)) {
			txnmgr = (TransactionManager) NameRegistrar
					.getIfExists(txnmgrRegistrationName);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new ApplicationContextException(e.getMessage(), e);
			}
			now = Calendar.getInstance().getTimeInMillis();
		}

		if (null == txnmgr) {
			logger.error(String
					.format("Unable to spring inject participants dependencies : transaction manager of name '%s' not found",
							txnmgrRegistrationName));
			throw new ApplicationContextException(
					String.format(
							"Unable to spring inject participants dependencies : transaction manager of name '%s' not found",
							txnmgrRegistrationName));
		} else {
			Map<String, String> beanDefsByParticipantClassName = new HashMap<String, String>();
			for (String participantBeanDefName : factory
					.getBeanNamesForType(TransactionParticipant.class)) {
				BeanDefinition beanDefinition = factory
						.getBeanDefinition(participantBeanDefName);
				String beanClassName = beanDefinition.getBeanClassName();
				beanDefsByParticipantClassName.put(beanClassName,
						participantBeanDefName);
				logger.debug(String
						.format("Found bean '%s' defining dependencies injection for transaction participant class '%s'",
								participantBeanDefName, beanClassName));
			}

			Map<String, List<TransactionParticipant>> groups = txnmgr.groups;
			for (Entry<String, List<TransactionParticipant>> entry : groups
					.entrySet()) {
				for (TransactionParticipant participant : entry.getValue()) {
					String currentParticipantClassName = participant.getClass()
							.getName();

					String participantBeanId = beanDefsByParticipantClassName
							.get(currentParticipantClassName);

					if (null != participantBeanId) {
						if (factory.containsBean(participantBeanId)) {
							factory.applyBeanPropertyValues(participant,
									participantBeanId);
							logger.debug(String
									.format("Dependencies injected for participant of class '%s' (transaction manager '%s')",
											currentParticipantClassName,
											txnmgrRegistrationName));
						}
					}
				}
			}
		}
	}

	/**
	 * @return
	 */
	public String getTxnmgrRegistrationName() {
		return txnmgrRegistrationName;
	}

	/**
	 * @param txnmgrRegistrationName
	 */
	public void setTxnmgrRegistrationName(String txnmgrRegistrationName) {
		this.txnmgrRegistrationName = txnmgrRegistrationName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * @return the processingDelay
	 */
	public int getProcessingDelay() {
		return processingDelay;
	}

	/**
	 * @param processingDelay
	 *            the processingDelay to set
	 */
	public void setProcessingDelay(int processingDelay) {
		this.processingDelay = processingDelay;
	}

}
