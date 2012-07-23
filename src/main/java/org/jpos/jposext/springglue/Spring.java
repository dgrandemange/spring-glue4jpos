package org.jpos.jposext.springglue;

import java.util.Iterator;
import java.util.Properties;

import org.jdom.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Spring QBean<br>
 * 
 * Inspired by Anthony Schexnaildre initial work<br>
 * 
 * @author dgrandemange
 * @jmx:mbean description="Spring QBean" extends="org.jpos.q2.QBeanSupportMBean"
 */
public class Spring extends QBeanSupport implements SpringMBean {

	ApplicationContext context;
	String[] configFiles;
	String requiredSpringCtxRegistrationKey;
	Properties springProperties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.q2.QBeanSupport#initService()
	 */
	public void initService() throws ConfigurationException {
		configFiles = cfg.getAll("config");
		if (configFiles.length < 1)
			throw new ConfigurationException("config property not specified");

		requiredSpringCtxRegistrationKey = cfg
				.get("requiredRegisteredSpringContext");

		springProperties = new Properties();
		@SuppressWarnings("rawtypes")
		Iterator iter = getPersist().getChildren("spring-property").iterator();
		while (iter.hasNext()) {
			Element e = (Element) iter.next();
			String name = e.getAttributeValue("name");
			if (name == null) {
				log.warn("spring-property : name attribute is null");
			} else {
				String value = e.getAttributeValue("value");
				if (value == null) {
					log.warn(String
							.format("spring-property : value attribute is null for name '%s'",
									name));
				} else {
					springProperties.put(name, value);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.q2.QBeanSupport#startService()
	 */
	public void startService() {
		ApplicationContext dependencyCtx = null;

		if ((null != requiredSpringCtxRegistrationKey)
				&& (requiredSpringCtxRegistrationKey.trim().length() > 0)) {
			try {
				log.debug(String
						.format("Spring Q2MBean '%s' : search for Spring context dependency '%s' in NameRegistrar",
								getName(), requiredSpringCtxRegistrationKey));
				dependencyCtx = ((Spring) NameRegistrar
						.get(requiredSpringCtxRegistrationKey)).getContext();
				log.debug(String
						.format("Spring Q2MBean '%s' : Spring context dependency '%s' found in NameRegistrar",
								getName(), requiredSpringCtxRegistrationKey));
			} catch (NotFoundException e) {
				log.error(String
						.format("Spring Q2MBean '%s' : spring context dependency '%s' not found in NameRegistrar",
								getName(), requiredSpringCtxRegistrationKey));
				throw new RuntimeException(e);
			}
		}

		try {
			if (null == dependencyCtx) {
				context = new FileSystemXmlApplicationContext(configFiles);
			} else {
				context = new FileSystemXmlApplicationContext(configFiles,
						dependencyCtx);
			}
			log.info(String.format(
					"Spring Q2MBean '%s' : spring context has been loaded",
					getName()));
		} catch (BeansException e) {
			log.error(String
					.format("Spring Q2MBean '%s' : an error occured while loading this spring context. Error message = %s",
							getName(), e.getMessage()));
			throw e;
		}

		NameRegistrar.register(getName(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.q2.QBeanSupport#stopService()
	 */
	public void stopService() {
		NameRegistrar.unregister(getName());
	}

	/**
	 * Returns the Spring ApplicationContext
	 */
	public ApplicationContext getContext() {
		return context;
	}

	/**
	 * @jmx:managed-attribute description="Configuration Files"
	 */
	public String[] getConfig() {
		return configFiles;
	}

	/**
	 * @jmx:managed-attribute description="Configuration Files"
	 */
	public synchronized void setConfig(String[] configs) {
		this.configFiles = configs;
	}

	public String getRequiredSpringCtxRegistrationKey() {
		return requiredSpringCtxRegistrationKey;
	}

	public void setRequiredSpringCtxRegistrationKey(
			String requiredSpringCtxRegistrationKey) {
		this.requiredSpringCtxRegistrationKey = requiredSpringCtxRegistrationKey;
	}

}