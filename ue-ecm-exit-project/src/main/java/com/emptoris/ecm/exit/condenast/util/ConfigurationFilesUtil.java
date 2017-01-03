/**
 * 
 */
package com.emptoris.ecm.exit.condenast.util;

import java.util.ArrayList;
import java.util.List;

import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.common.config.EmpConfiguration;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.common.config.ExitConfig;

/**
 * Utility Class - For providing all activities around Configuration File
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ConfigurationFilesUtil {
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ConfigurationFilesUtil.class);
	
	

	/**
	 * Utility method - called to check, if the any config key have value OR not
	 * 
	 * @param exitContext
	 * @param requiredConfig
	 * @return
	 */
	public static ArrayList<String> areAllTheseConfigKeysDefinedWitValue(ExitContext exitContext,
													List<String> requiredConfig) {
		LOG.info("Begin ==> utility Method areAllTheseConfigKeysDefinedWitValue");
		
		ArrayList<String> anyNullValuedConfigKeys = new ArrayList<String>();
		
		// Loop through list containing configuration keys & if value available correct else false
		for (String eachConfigKeyPart : requiredConfig) {
			try {
				EmpConfiguration empConfig = ExitConfig.getExitConfig();				
				String configValuePart = empConfig.getRequiredStringConfiguration(eachConfigKeyPart);
				
				if(configValuePart != null){
					LOG.debug("The Value is Available for Provided ConfigKey" +eachConfigKeyPart
							 + "The Value is " + configValuePart);	
					
				} else {
					LOG.debug("The Value is NOT Available for Provided ConfigKey" +eachConfigKeyPart
							 + "The Value is " + configValuePart);
					anyNullValuedConfigKeys.add(eachConfigKeyPart);
				}

			} catch (ConfigurationException e) {				
				LOG.error("ConfigurationException while accessing Configuration Keys ");	
				anyNullValuedConfigKeys.add(eachConfigKeyPart);
			}
			
		}
		
		LOG.info("End ==> utility Method areAllTheseConfigKeysDefinedWitValue");
		
		return anyNullValuedConfigKeys;		
	}
		
	

}
