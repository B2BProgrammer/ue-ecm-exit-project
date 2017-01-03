/**
 * 
 */
package com.emptoris.ecm.exit.condenast.handler;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.implfive.BTVDefaultValuesSave;
import com.emptoris.ecm.exit.condenast.implfour.PsContractIDSave;

/**
 * Handler Class - Contract Save Process - according to design 
 * each requirement is called & following operations are performed
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractSaveHandler {
	
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ContractSaveHandler.class);

	/**
	 * Handler method called from PRE Contract SAVE Process
	 * 
	 * @param exitContext
	 * @throws PluginException 
	 * @throws WarningMessageException 
	 */
	public void executePreContractSave(ExitContext exitContext) throws PluginException, WarningMessageException {		
		LOG.info("Start --> PreContractSave Method" + exitContext.getContract().getNumber());


		// requirement 5 - BTV Default values requirement
		LOG.info("Starting the BTV Default values  requirement for Save process");
		// Initializing the BIC & loading any required details
		BTVDefaultValuesSave btvDefaultValuesSave = new BTVDefaultValuesSave(exitContext);	
		if(btvDefaultValuesSave.isAllPreConditionsSatisfied(exitContext)){
			if(btvDefaultValuesSave.checkFlag()){
				if(btvDefaultValuesSave.validateConfigurations(exitContext)){
					btvDefaultValuesSave.executeLogic(exitContext);
				}
				btvDefaultValuesSave.runErrorDumpToDB(exitContext);
			}					
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}		
		LOG.info("Ending the BTV Default values  requirement for Save process");


		// requirement 4 -- psContract Id requirement
		LOG.info("Starting the psContract Id  requirement for Save process");
		PsContractIDSave pscontractIdSave = new PsContractIDSave(exitContext);
		// Initializing the BIC & loading any required details
		if(pscontractIdSave.isAllPreConditionsSatisfied(exitContext)){
			if(pscontractIdSave.checkFlag()){
				if(pscontractIdSave.validateConfigurations(exitContext)){
					pscontractIdSave.executeLogic(exitContext);
				}
				pscontractIdSave.runErrorDumpToDB(exitContext);
			}				
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}			
		LOG.info("Ending the psContract Id  requirement for Save process");


		LOG.info("End --> PreContractSave Method"+ exitContext.getContract().getNumber());
	}

}
