package org.jpos.jposext.springglue;

/**
 * Spring MBean interface
 * 
 * @author dgrandemange
 *
 */
public interface SpringMBean extends org.jpos.q2.QBeanSupportMBean {

    public String[] getConfig();
    
	public String getSpringCtxDepRegistrationKey();

	public boolean isSpringCtxDepOptional();
}
