package edu.wustl.catissuecore.util.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import krishagni.catissueplus.csd.CatissueUserContextProviderImpl;
import krishagni.catissueplus.util.FormProcessor;
import edu.common.dynamicextensions.nutility.FormProperties;
import edu.wustl.dynamicextensions.formdesigner.usercontext.CSDProperties;

public class CatissueCoreServletContextListener implements ServletContextListener
{

	/**
	 * DATASOURCE_JNDI_NAME.
	 */
	private static final String JNDI_NAME = "java:/openspecimen";

	public void contextInitialized(final ServletContextEvent sce)
	{
		try
		{
			CSDProperties.getInstance().setUserContextProvider(new CatissueUserContextProviderImpl());			
			FormProperties.getInstance().setPostProcessor(new FormProcessor());            									
		} catch (final Exception e)	{
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	public void contextDestroyed(final ServletContextEvent sce)	{
	}

}
