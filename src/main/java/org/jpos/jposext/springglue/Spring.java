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
	String springCtxDepRegistrationKey;
	boolean springCtxDepOptional;
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

		springCtxDepOptional = false;
		springCtxDepRegistrationKey = cfg
				.get("requiredRegisteredSpringContext");
		
		if (null == springCtxDepRegistrationKey) {
			springCtxDepRegistrationKey = cfg
					.get("optionalRegisteredSpringContext");
			springCtxDepOptional = true;			
		}
		
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

		if ((null != springCtxDepRegistrationKey)
				&& (springCtxDepRegistrationKey.trim().length() > 0)) {
			try {
				log.debug(String
						.format("Spring Q2MBean '%s' : search for Spring context dependency '%s' in NameRegistrar",
								getName(), springCtxDepRegistrationKey));

				Object object = NameRegistrar
						.get(springCtxDepRegistrationKey);
				if (object instanceof Spring) {
					dependencyCtx = ((Spring) object).getContext();
				} else if (object instanceof ApplicationContext) {
					dependencyCtx = ((ApplicationContext) object);
				} else {
					String errMsg = String
							.format("Spring Q2MBean '%s' : spring context dependency '%s' found in NameRegistrar but is not a Spring ApplicaitonContext, neither a Spring QBean",
									getName(), springCtxDepRegistrationKey);
					log.error(errMsg);
					throw new RuntimeException(errMsg);
				}

				log.debug(String
						.format("Spring Q2MBean '%s' : Spring context dependency '%s' found in NameRegistrar",
								getName(), springCtxDepRegistrationKey));
			} catch (NotFoundException e) {
				if (!this.springCtxDepOptional) {
					log.error(String
							.format("Spring Q2MBean '%s' : spring context dependency '%s' not found in NameRegistrar",
									getName(), springCtxDepRegistrationKey));
					throw new RuntimeException(e);
				} else {
					log.info(String
							.format("Spring Q2MBean '%s' : optional spring context dependency '%s' not found in NameRegistrar",
									getName(), springCtxDepRegistrationKey));					
				}
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

	/**
	 * @return the springCtxDepRegistrationKey
	 */
	public String getSpringCtxDepRegistrationKey() {
		return springCtxDepRegistrationKey;
	}

	/**
	 * @param springCtxDepRegistrationKey the springCtxDepRegistrationKey to set
	 */
	public void setSpringCtxDepRegistrationKey(String springCtxDepRegistrationKey) {
		this.springCtxDepRegistrationKey = springCtxDepRegistrationKey;
	}

	/**
	 * @return the springCtxDepOptional
	 */
	public boolean isSpringCtxDepOptional() {
		return springCtxDepOptional;
	}

	/**
	 * @param springCtxDepOptional the springCtxDepOptional to set
	 */
	public void setSpringCtxDepOptional(boolean springCtxDepOptional) {
		this.springCtxDepOptional = springCtxDepOptional;
	}



}