/**
 * 
 */
package com.emptoris.ecm.exit.condenast.handler;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.implfive.BTVDefaultValuesCreate;
import com.emptoris.ecm.exit.condenast.implfour.PsContractIDCreate;
import com.emptoris.ecm.exit.condenast.implone.ContractCreationNotification;
import com.emptoris.ecm.exit.condenast.implsix.ContractSendToSAP;
import com.emptoris.ecm.exit.condenast.implthree.ContractSecurity;
import com.emptoris.ecm.exit.condenast.impltwo.PopulatingEntities;
import com.emptoris.ecm.exit.condenast.util.ContractTemplateUtility;

/**
 * Handler Class - Contract Create Process - according to design 
 * each requirement is called & following operations are performed
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractCreateHandler {
	
	// LOG Variable to store all the details performed in this BIC
	
	private static final ILogger LOG = Logger.getLogger(ContractCreateHandler.class);
	

	/**
	 * Called after the CORE Create process
	 * 
	 * @param exitContext
	 * @throws PluginException 
	 */
	public void executePostContracrtCreate(ExitContext exitContext) {		
		LOG.info("Start --> PostContractCreate Method : "+ exitContext.getContract().getNumber());		
		
		//Variable used to save - weather the contract is created from SAVEAs or Not
		boolean  isFromSaveAs = ContractTemplateUtility.saveASChecker(exitContext);
		LOG.info("Is the Contract created from SAVE As :" + isFromSaveAs);


		// requirement 5 - BTV Default values requirement
		LOG.info("Starting the BTV Default values  requirement process");
		// Initializing the BIC & loading any required details
		BTVDefaultValuesCreate btvDefaultValuesCreate = new BTVDefaultValuesCreate(exitContext);
		if(btvDefaultValuesCreate.isAllPreConditionsSatisfied(exitContext)){
			if(btvDefaultValuesCreate.checkFlag()){
				if(btvDefaultValuesCreate.validateConfigurations(exitContext)){
					btvDefaultValuesCreate.executeLogic(exitContext);					
				}
				btvDefaultValuesCreate.runErrorDumpToDB(exitContext);
			}				
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}			
		LOG.info("Ending the BTV Default values  requirement process");



		// requirement 4 -- psContract Id requirement
		LOG.info("Starting the psContract Id  requirement process");
		// Initializing the BIC & loading any required details
		PsContractIDCreate pscontractId = new PsContractIDCreate(exitContext);
		if(pscontractId.isAllPreConditionsSatisfied(exitContext)){
			if(pscontractId.checkFlag()){
				if(pscontractId.validateConfigurations(exitContext)){
					pscontractId.executeLogic(exitContext ,isFromSaveAs);
				}
				pscontractId.runErrorDumpToDB(exitContext);
			}	
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}
		LOG.info("Ending the psContract Id  requirement process");



		// requirement 3 -- Contract Security
		LOG.info("Starting the contractSecurity requirement process");
		// Initializing the BIC & loading any required details
		ContractSecurity contractSecurity = new ContractSecurity(exitContext);	
		if(contractSecurity.isAllPreConditionsSatisfied(exitContext)){
			if(contractSecurity.checkFlag()){
				if(contractSecurity.validateConfigurations(exitContext)){
					contractSecurity.executeLogic(exitContext);
				}
				contractSecurity.runErrorDumpToDB(exitContext);
			}				
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}
		LOG.info("Ending the contractSecurity requirement process");



		// requirement 2 -- Populating Entities
		LOG.info("Starting the populating Entities requirement process");
		// Initializing the BIC & loading any required details
		PopulatingEntities populatingEntities = new PopulatingEntities(exitContext);
		if(populatingEntities.isAllPreConditionsSatisfied(exitContext)){
			if(populatingEntities.checkFlag()){			
				if(populatingEntities.validationOfLines(exitContext)){
					populatingEntities.executeLogic(exitContext, isFromSaveAs);
				}
				populatingEntities.runErrorDumpToDB(exitContext);
			}	
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}
		LOG.info("Ending the populating Entities requirement process");




		// requirement 1 -- Contract WebService Notification
		LOG.info("Starting the Contract Creation Notification requirement process");
		// Initializing the BIC & loading any required details
		ContractCreationNotification contractCreationNotification = new ContractCreationNotification(exitContext);	
		if(contractCreationNotification.isAllPreConditionsSatisfied(exitContext)){
			if(contractCreationNotification.checkFlag()){
				if(contractCreationNotification.validateConfigurations(exitContext)){
					contractCreationNotification.executeLogic(exitContext);
				}				
				contractCreationNotification.runErrorDumpToDB(exitContext);			
			}
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}
		LOG.info("Ending the Contract Creation Notification requirement process");		
		
		
		
		
		// requirement 6 -- New Requirement Integration with SAP system
		LOG.info("Starting the Contract sending to SAP requirement process");
		// Initializing the BIC & loading any required details		
		ContractSendToSAP contractSendToSap = new ContractSendToSAP(exitContext);		
		if(contractSendToSap.isAllPreConditionsSatisfied(exitContext)){
			if(contractSendToSap.checkFlag()){
				if(contractSendToSap.validateConfigurations(exitContext)){
					contractSendToSap.executeLogic(exitContext);
				}				
				contractSendToSap.runErrorDumpToDB(exitContext);			
			}
		} else {
			LOG.info("PreConditions are Not Set ByPass the requirement");
		}
		LOG.info("Ending the Contract Creation Notification requirement process");	



		LOG.info("End --> PostContractCreate Method for :" + exitContext.getContract().getNumber());
	}	

}
