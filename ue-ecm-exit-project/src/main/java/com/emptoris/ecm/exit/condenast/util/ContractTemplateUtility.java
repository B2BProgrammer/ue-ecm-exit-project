/**
 * 
 */
package com.emptoris.ecm.exit.condenast.util;

import java.util.HashMap;

import com.dicarta.appfound.contract.ContractData;
import com.dicarta.appfound.contract.ContractFindByIdCriteria;
import com.dicarta.appfound.contract.server.persistence.ContractPersister;
import com.dicarta.appfound.termdefinition.ITermDefinitionEditorData;
import com.dicarta.appfound.termdefinition.server.bo.TermDefinitionServicesBO;

import com.dicarta.infra.common.IId;
import com.dicarta.infra.common.IdData;
import com.dicarta.infra.common.ObjectNotFoundException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.dicarta.infra.persistence.common.iterator.IDataIterator;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.condenast.constants.ContractConstants;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;

/**
 * Utility Class - For providing all activities around Contract Template
 * 
 * @author Ajith.Ajjarani
 *
 */
public class ContractTemplateUtility {
	
	// LOG Variable to store all the details performed in this BIC
	private static final ILogger LOG = Logger.getLogger(ContractTemplateUtility.class);

	
	
	/**
	 *  * Utility method to check - weather the contract was created from 
	 * a) New Template
	 * b) Using SaveAs Option
	 * 
	 * 
	 * @param exitContext
	 * @return
	 */
	public static boolean saveASChecker (ExitContext exitContext){
		
		boolean isFromSaveAS = false;		
		
		
	    try {
			 //check if the template is a contract instance or template	
			ContractData templateInfo = getContractTemplateData(exitContext.getContract().getTemplateId());
			
			// Can be "Template or Instance"
			String usageData = templateInfo.getUsage();
			
			
			if(usageData.equalsIgnoreCase(ContractConstants.TEMPLATE)){
				isFromSaveAS = false;
			}
			
			if(usageData.equalsIgnoreCase(ContractConstants.INSTANCE)){
				isFromSaveAS = true;
			}
			
			
		} catch (PersistenceException e) {
			LOG.error("PersistenceException while fetching Data from contractTemplate Data:" + e.getLocalizedMessage());
			String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_ContractTemplateData"); 
			String errorCode = ErrorCodes.CS_ER_EX;			
			ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
			return isFromSaveAS;
			
		}
		
	 return isFromSaveAS;
	}
	
	
	
	
	/**look up template data with input template id

	 * @param templateId
	 * @return
	 * @throws PersistenceException
	 */
	public static ContractData getContractTemplateData(String templateId)
			throws PersistenceException {
		
		 ContractData templateData = null;
		 ContractFindByIdCriteria c = new ContractFindByIdCriteria();
	     c.setId(templateId);
	      
		 ContractPersister p = new ContractPersister(); 
		 //look up template data with input template id
		 IDataIterator i = p.findById(c);

		 if (i.hasNext()) {
			templateData = (ContractData) i.next();
		 }
		 
		 return templateData;
	}
	
	
	
	
	
	
	/**
	 * Utility method used to get the Term's Label Name
	 * 
	 * @param exitContext
	 * @param termName
	 * @return
	 */
	public static String getTermLabelName(ExitContext exitContext, String termName){

		ITermDefinitionEditorData ii = getTermDetails(exitContext, termName);
		String termLableName = ii.getLabel();
		
		
		// Returns the Term Label Name
		return termLableName;
	}
	
	
	/**
	 * Utility method used - To fetch complete details of Terms
	 * @param exitContext
	 * @param termName
	 * @return
	 */
	public static ITermDefinitionEditorData getTermDetails(ExitContext exitContext , String termName){
		
		ITermDefinitionEditorData termEditorData  = getTermDefEditorData(exitContext, termName);
		
		return termEditorData;

	}
	
	


	
	
	/**
	 * Utility method used - To fetch Terms Editor Details
	 * 
	 * @param exitContext
	 * @param termName
	 * @return
	 */
	public final static ITermDefinitionEditorData getTermDefEditorData(ExitContext exitContext, String termName)	 {
		
	 HashMap<String, ITermDefinitionEditorData> termNameDefEditorData = new HashMap<String, ITermDefinitionEditorData>(); ;
	 
	 TermDefinitionServicesBO termDefSerBoInstance = TermDefinitionServicesBO.getInstance(); 

		// if term definition editor data is not retrieved before, retrieve it
		// now
		if (!termNameDefEditorData.containsKey(termName)) {
			//String idString = getTermIdByName(termName);
			IId termId = new IdData(getTermIdByName(exitContext, termName));
			try {
				if(termId != null){
					ITermDefinitionEditorData termEditorData = termDefSerBoInstance.loadTermDefinition(exitContext.getInternalUserObject(), termId);
					termNameDefEditorData.put(termName, termEditorData);
				}
				
			} catch (PersistenceException e) {
				LOG.error("PersistenceException while loading Term Definiton Data" + e.getLocalizedMessage());
				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_Loading_termDefinition"); 
				String errorCode = ErrorCodes.CS_ER_EX;			
				ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
				return null;				
			} catch (ObjectNotFoundException e) {
				LOG.error("ObjectNotFoundException while loading Term Definiton Data" + e.getLocalizedMessage());
				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "ObjectNotFoundException_Loading_termDefinition"); 
				String errorCode = ErrorCodes.CS_ER_EX;			
				ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
				return null;				
			}

		}

		return termNameDefEditorData.get(termName);
	}
	
	
	
	
	/**
	 * Get the term id (UUID) by looking up with term name
	 * @param exitContext 
	 * 
	 * @param termName - Name of the term for which the id (UUID) has to be returned
	 * @return String - UUID if the term with name matching the input parameter.
	 * @throws PluginException  - If input parameter is null or Persistence exception form the core.
	 */
	public final static String getTermIdByName(ExitContext exitContext, String termName) {
		HashMap<String, String> termNameIdMap = new HashMap<String, String>(); 
		TermDefinitionServicesBO termDefSerBoInstance = TermDefinitionServicesBO.getInstance(); 
		
		// if the term id already retrieved, then return it or retrieve and
		// return
		// term definition for term using
		// TermDefinitionServicesBO.getInstance();
		if (!termNameIdMap.containsKey(termName)) {
			// read the term id value and store it in to the hashmap
			try {
				String termId = termDefSerBoInstance.getIdByInternalName(termName);
				if(termId != null){
					termNameIdMap.put(termName, termId);
				}				
				
			} catch (PersistenceException e) {
				LOG.error("PersistenceException while getting Id by Internal Name" + e.getLocalizedMessage());
				String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CNE_EXE.trim(), null , "PersistenceException_ID_Internal_Name"); 
				String errorCode = ErrorCodes.CS_ER_EX;			
				ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
				return null;					
			}
		}

		// return the term id value
		return termNameIdMap.get(termName);
	}

}
