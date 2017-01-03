package com.emptoris.ecm.exit.condenast.implthree;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.dicarta.infra.authorization.ACLTypesEnum;
import com.dicarta.infra.authorization.AuthorizableObjectsEnum;
import com.dicarta.infra.authorization.IACLEditorParams;
import com.dicarta.infra.authorization.server.bo.AuthorizationBO;
import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.dicarta.infra.persistence.common.exception.PersistenceException;
import com.dicarta.webservices.services.contract.AddPermissionUsersData;
import com.dicarta.webservices.services.contract.AddSecurityUser;
import com.emptoris.ecm.api.Helper.UserGroupHelper;
import com.emptoris.ecm.api.exception.EcmApiObjectNotFound;
import com.emptoris.ecm.api.exception.UserException;
import com.emptoris.ecm.api.exception.UserGroupException;
import com.emptoris.ecm.api.impl.UserImpl;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.api.intf.User;
import com.emptoris.ecm.api.persistence.UserGroupRecord;
import com.emptoris.ecm.exit.CustomExitContext;
import com.emptoris.ecm.exit.ExitSubroutine;
import com.emptoris.ecm.exit.SubroutineResult;
import com.emptoris.ecm.exit.condenast.constants.ErrorCodes;
import com.emptoris.ecm.exit.condenast.constants.MessagekeyConstants;
import com.emptoris.ecm.exit.condenast.util.ErrorUtility;
import persistence.PermissionGroupData;
import persistence.PermissionGroupDataIterator;
import persistence.PermissionGroupPersister;


/**
 * Adds/Updates contacts and their corresponding 
 * permissions within the security tab of the contract
 * @author Ajith.Ajjarani
 *
 */
public class SecurityAssignmentSubroutine extends ExitSubroutine {
	/**
	 * Using the log variable to hold all the operation in the current class
	 */
	private final ILogger LOG  = Logger.getLogger(SecurityAssignmentSubroutine.class);
    
	public String wizardid = null;
	Date date = new Date();
	
	public SecurityAssignmentSubroutine(ExitContext exitContext, CustomExitContext cec) {		
		super(exitContext, cec);
		LOG.info("Start of method ==> SecurityAssignmentSubroutine for contract Instance");
		wizardid = exitContext.getWizardId();		
		moduleName = "SecurityAssignmentSubroutine";
		
		LOG.info("End of method ==> SecurityAssignmentSubroutine for contract Instance");
	}
	
	/**
	 * This method will add user/user group with specified permission to 
	 * security tab of the contract
	 * @param exitContext 
	 * 
	 * @param result
	 * @param userOrGroupName
	 * @param permissionOrGrpName
	 * @throws WarningMessageException 
	 */
	public final void addUserOrUserGroupToContract(ExitContext exitContext, SubroutineResult result, String userOrGroupName, 
			String permissionOrGrpName) throws PluginException, WarningMessageException  {
		
		ArrayList<String> toSendErrorDB = new ArrayList<String>();
		toSendErrorDB.add(userOrGroupName);
		toSendErrorDB.add(permissionOrGrpName);
		
		LOG.info("Start of method ==> addUserOrUserGroupToContract for contract Instance");

        long timeBegin = System.currentTimeMillis();
		try {
				LOG.debug("Assigning User Group / Permission Group to the contract");
				HashMap<String, ArrayList<String>> userwithPermissionOrGroup = 
						new HashMap<String, ArrayList<String>>();
				addtoMap(exitContext, result, userwithPermissionOrGroup, userOrGroupName,
						permissionOrGrpName);
				addSecurityData(result, userwithPermissionOrGroup);			
				LOG.debug("Assigned User Group / Permission Group to the contract");
		} catch (UserException e) {			
			
			addError(result, false, MessagekeyConstants.GENERAL_ECMAPI_ERROR, 
					new Object[] {});
			LOG.warn("Error added to subroutine result", e);
			
			// NEW ERROR Section - CS_AM_5
			LOG.error("Contract security requirement Bypassed");			
        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM5, toSendErrorDB, null); 
        	String errorCode = ErrorCodes.CS_AM_5;			
        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
        	return;				
			
		} catch (PersistenceException e) {			
			addError(result, false, MessagekeyConstants.GENERAL_ECMAPI_ERROR, 
					new Object[] {});
			LOG.warn("Error added to subroutine result", e);
			
			// NEW ERROR Section - CS_AM_5			
			LOG.error("Contract security requirement Bypassed");			
        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM5, toSendErrorDB, null); 
        	String errorCode = ErrorCodes.CS_AM_5;			
        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
        	return;	
        				
		} catch (UserGroupException e) {
			
			addError(result, false, MessagekeyConstants.GENERAL_ECMAPI_ERROR, 
					new Object[] {});
			LOG.warn("Error added to subroutine result", e);
			
			// NEW ERROR Section - CS_AM_5	
			LOG.error("Contract security requirement Bypassed");			
        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM5, toSendErrorDB, null); 
        	String errorCode = ErrorCodes.CS_AM_5;			
        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
        	return;	
        	
		} finally {
            long timeEnd = System.currentTimeMillis();
			LOG.info(moduleName + ": End - Contract #" 
					+ _contract.getNumber() + ", time= " + (timeEnd - timeBegin) + " ms");
		}
		
		LOG.info("End of method ==> addUserOrUserGroupToContract for contract Instance");
	
	}
	
	
	
	/**
	 * SAMPLE CODE FROM IBM ENGINEERING TEAM
	 * DO NOT MAKE ANY CHANGE TO THIS METHOD
	 * 
	 * @param result
	 * @param securityUsers
	 * @param userorgroupName  - user name or user group name
	 * @param permissionGroupName
	 * @throws UserException
	 * @throws PersistenceException
	 * @throws PluginException
	 * @throws UserGroupException
	 * @throws WarningMessageException 
	 */
	private void addtoMap(ExitContext exitContext, SubroutineResult result, HashMap<String, 
			ArrayList<String>> securityUsers, String userorgroupName,
			String permissionGroupName)
			throws UserException, PersistenceException, PluginException, UserGroupException, WarningMessageException {
		
		LOG.info("Start of method ==> addtoMap for contract Instance");
		
		String userId = null;
		try {
			User user = UserImpl.lookupUserByUsername(userorgroupName);
			LOG.debug("Looking up user by username");
			if (user != null)  {
				userId = user.getId();
			}
		} catch (EcmApiObjectNotFound e) {
			// NEW ERROR Section
			LOG.error("Contract security requirement Bypassed");
			LOG.error( userorgroupName +" user group is not defined in VSM/ECM");
        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM3.trim(), null , userorgroupName); 
        	String errorCode = ErrorCodes.CS_AM_3;			
        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
			
		}
		if (userId == null) {
            try {
                   UserGroupRecord ugr = UserGroupHelper.getUserGroupRecordByName(userorgroupName);
                   LOG.debug("Looking up user group name");
                   if (ugr != null) {
                         userId = ugr.getUserGroupId();
                   }
            } catch (EcmApiObjectNotFound e) {
            	// CUSTOM ADDED
            	LOG.error("Contract security requirement Bypassed");
            	LOG.error( userorgroupName +" user group is not defined in VSM/ECM");
            	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM3.trim(), null , userorgroupName); 
            	String errorCode = ErrorCodes.CS_AM_3;		
            	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);	
            	return ;
            }
     }
		if (userId != null) {
			String sqlWhere = " PERMISSION_GROUP_NAME = '" + permissionGroupName + "'";
			PermissionGroupDataIterator permissionGroupDataIterator 
					= (PermissionGroupDataIterator) PermissionGroupPersister.getInstance().
					findAllValuesForName(sqlWhere);
			permissionGroupDataIterator.prefetch();
			LOG.debug("Looking up the permission group in the core table");
			if (permissionGroupDataIterator.hasNext()) {
				PermissionGroupData permissionGroupData =
							(PermissionGroupData) permissionGroupDataIterator.next();
				String permissionGroupId = permissionGroupData.m_permissionGroupId;
				if (securityUsers.containsKey(userId)) {
					ArrayList<String> permissionList = securityUsers.get(userId);
					permissionList.add(permissionGroupId);
					securityUsers.put(userId, permissionList);
				} else {
					ArrayList<String> permissionList = new ArrayList <String>();
					permissionList.add(permissionGroupId);
					securityUsers.put(userId, permissionList);
				}
			} else {
				// NEW ERROR Section
				LOG.error("Contract security requirement Bypassed");
				LOG.error( permissionGroupName + " permission group is not defined in VSM/ECM");
	        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM4.trim(), null , permissionGroupName); 
	        	String errorCode = ErrorCodes.CS_AM_4;			
	        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
	        	return;			
			}

        } else {
        	// NEW ERROR Section
        	LOG.error("Contract security requirement Bypassed");
			LOG.error( permissionGroupName + " permission group is not defined in VSM/ECM");
        	String errorMessage = ErrorUtility.createErrorMessage(exitContext, MessagekeyConstants.MSG_CS_AM4.trim(), null , permissionGroupName); 
        	String errorCode = ErrorCodes.CS_AM_4;			
        	ErrorUtility.callToConstructMessageToDB(exitContext,  errorMessage, errorCode);
        	return;		       	
        }
		
		LOG.info("End of method ==> addtoMap for contract Instance");
	}
	
	/**
	 * SAMPLE CODE FROM IBM ENGINEERING TEAM
	 * DO NOT MAKE ANY CHANGE TO THIS METHOD
	 * 
	 * Adds the user permissions generated to the security tab of the contract
	 * @param result
	 * @param usersToBeOnContractSecurity
	 * @throws PersistenceException
	 */
	private void addSecurityData(SubroutineResult result, HashMap<String, 
				ArrayList<String>> usersToBeOnContractSecurity)
						throws PersistenceException {
		LOG.info("Start of method ==> addSecurityData for contract Instance");
		AuthorizationBO authBo = AuthorizationBO.getInstance();
		ACLEditorParams params = new ACLEditorParams(_contract.getId(), 
					AuthorizableObjectsEnum.CONTRACT, ACLTypesEnum.PRIMARY);
		AddPermissionUsersData addPermissionUsersData = new AddPermissionUsersData();
		ArrayList<AddSecurityUser> securityList = new ArrayList<AddSecurityUser>();
		String contractId = _contract.getId();
		addPermissionUsersData.setContractId(contractId);
		Set<String> usersToBeAdded = usersToBeOnContractSecurity.keySet();
		Iterator<String> usersIterator = usersToBeAdded.iterator();
		while (usersIterator.hasNext()) {
			result.setDoUpdate(true);
			String userId = usersIterator.next();
			ArrayList<String> permissionList = usersToBeOnContractSecurity.get(userId);
			Iterator<String> permissionsIterator = permissionList.iterator();
			while (permissionsIterator.hasNext()) {
				AddSecurityUser addSecurityUser = new AddSecurityUser();
				String[] userIds = new String[] {userId};
				addSecurityUser.setUserId(userIds);
				String permissionGroupId = permissionsIterator.next();
				addSecurityUser.setPermissionId(permissionGroupId);
				securityList.add(addSecurityUser);
			}
		}
		AddSecurityUser[] security = 
					(AddSecurityUser[]) securityList.toArray(new AddSecurityUser[0]);
		addPermissionUsersData.setSecurity(security);
		authBo.addPermissionUsers(_user, addPermissionUsersData, params);
		
		LOG.info("End of method ==> addSecurityData for contract Instance");
	}
	
	
	
	/*
	 * SAMPLE CODE FROM IBM ENGINEERING TEAM
	 * DO NOT MAKE ANY CHANGE TO FOLLOWING  INNER CLASS
	 *  
	 */
	private class ACLEditorParams implements IACLEditorParams {
		String id;
		AuthorizableObjectsEnum authorizableObjectsEnum;
		ACLTypesEnum aclTypesEnum;

		public ACLEditorParams(String id, AuthorizableObjectsEnum authorizableObjectsEnum, ACLTypesEnum aclTypesEnum) {
			this.id = id;
			this.authorizableObjectsEnum = authorizableObjectsEnum;
			this.aclTypesEnum = aclTypesEnum;
		}
		@Override
		public AuthorizableObjectsEnum getObjectType() {
			return authorizableObjectsEnum;
		}
		@Override
		public String getId() {
			return id;
		}
		@Override
		public ACLTypesEnum getACLType() {
			return aclTypesEnum;
		}
	}
}
