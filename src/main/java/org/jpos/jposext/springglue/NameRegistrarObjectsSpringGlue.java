package org.jpos.jposext.springglue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jpos.q2.Q2;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.PriorityOrdered;

/**
 * Spring post processor : it manually injects dependencies into jPos
 * NameRegistrar entries.<br>
 * 
 * @author dgrandemange
 * 
 */
public class NameRegistrarObjectsSpringGlue implements
		BeanFactoryPostProcessor, PriorityOrdered {

	private static final Log logger = Log.getLog(Q2.LOGGER_NAME,
			NameRegistrarObjectsSpringGlue.class.getName());

	private static final Pattern NAME_REGISTRAR_PREFIX__REGEXP_PATTERN = Pattern
			.compile("^NameRegistrar\\.(.*)$");

	private static final int DEFAULT_PROCESSING_DELAY = 10000;

	private int processingDelay = DEFAULT_PROCESSING_DELAY;

	private int order;

	public NameRegistrarObjectsSpringGlue() {
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
	public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
			throws BeansException {

		// Lookink for Spring bean definitions dedicated to NameRegistrar
		// entries
		Map<String, String> mapBeanDefNameByRegisteredKey = new HashMap<String, String>();
		for (String beanDefName : factory.getBeanDefinitionNames()) {
			Matcher match = NAME_REGISTRAR_PREFIX__REGEXP_PATTERN
					.matcher(beanDefName);
			if (match.matches()) {
				String key = match.group(1);
				if ((null != key) && !("".equals(key.trim()))) {
					mapBeanDefNameByRegisteredKey.put(key, beanDefName);
				}
			}
		}

		long startTime = Calendar.getInstance().getTimeInMillis();
		long now = Calendar.getInstance().getTimeInMillis();

		// Trying to inject NameRegistrar entries
		while (((now - startTime) < processingDelay)
				&& (mapBeanDefNameByRegisteredKey.size() > 0)) {
			for (Iterator<Map.Entry<String, String>> it = mapBeanDefNameByRegisteredKey
					.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> entry = it.next();
				String key = entry.getKey();
				Object value = NameRegistrar.getIfExists(key);
				if (null != value) {
					String beanDefName = entry.getValue();
					factory.applyBeanPropertyValues(value, beanDefName);
					logger.debug(String
							.format("Dependencies injected for NameRegistrar entry '%s'",									
									key));					
					it.remove();
				}
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new ApplicationContextException(e.getMessage(), e);
			}
			now = Calendar.getInstance().getTimeInMillis();
		}

		// Checking dependency injection is fully processed
		if (mapBeanDefNameByRegisteredKey.size() > 0) {
			logger.error("Dependency injection have failed on some NameRegistrar entries");
			for (Entry<String, String> entry : mapBeanDefNameByRegisteredKey
					.entrySet()) {
				logger.error(String
						.format("'%s' not injected : check NameRegistrar to see if an entry is well registered with this key",
								entry.getKey()));
			}
			throw new ApplicationContextException(
					"Dependency injection have failed on some NameRegistrar entries");
		}

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
	 * @param processingDelay the processingDelay to set
	 */
	public void setProcessingDelay(int processingDelay) {
		this.processingDelay = processingDelay;
	}

}
