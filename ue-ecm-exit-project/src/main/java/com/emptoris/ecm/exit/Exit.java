package com.emptoris.ecm.exit;



import com.dicarta.appfound.common.CreationEntryPoint;
import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.common.WarningMessageException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.api.intf.ExitContext;

import com.emptoris.ecm.api.exception.EcmApiException;
import com.emptoris.ecm.exit.ExitBase;
import com.emptoris.ecm.exit.condenast.handler.ContractCreateHandler;
import com.emptoris.ecm.exit.condenast.handler.ContractSaveHandler;




public class Exit extends ExitBase {
    private final ILogger LOG  = Logger.getLogger(Exit.class);
    private CustomExitContext _cec =  null;
    
    // Thread Local Variable's used to pass information from one context Trigger to other context Trigger  
    public static final ThreadLocal <CreationEntryPoint> creationEntryPoint = new ThreadLocal<CreationEntryPoint>();
   
       

    public Exit() throws PluginException {
        super();
        loadConfigurationProperties();
    }

    private void loadConfigurationProperties() throws PluginException
    {
        try {
            _cec = CustomExitContext.getInstance();
        } catch (ConfigurationException e) {
            throw new PluginException("Failed to load ECM Exit Configuration. " + e.getMessage());
        }
    }

    private void throwException(Throwable ex) throws PluginException, WarningMessageException {
        if (ex instanceof PluginException)         throw (PluginException) ex;
        if (ex instanceof WarningMessageException) throw (WarningMessageException) ex;
        if (ex instanceof EcmApiException) {
            EcmApiException e = (EcmApiException) ex;
            LOG.error(e.getMessage(), e);
            _cec.throwPluginException("general.ecmapi.error", (new Object[] {e.getMessage()}), e);
        }
        _cec.handleThrowable(ex);
    }

    /////////////////////////////////////////////////////////////////////////////// 
    //	                                                                         //
    //========== Below here are all of the customer-specific exit events ========//
    //	                                                                         //	
    //////////////////////////////////////////////////////////////////////////////
    
      
    
	
    
    
   
	/* (non-Javadoc)
	 * 
	 * Triggered - When Contract Process is in PRECONTRACT CREATE
	 * 
	 * @see com.emptoris.ecm.exit.ExitBase#preContractCreate(com.emptoris.ecm.api.intf.ExitContext)
	 */
	public final void preContractCreate(ExitContext exitContext)
			throws PluginException, WarningMessageException {
		LOG.info("Creation Entry Point set - To know the creation methodology");
		creationEntryPoint.set(exitContext.getCreateContractDetails().getCreationEntryPoint());				
	}
	
	
	
	 
	/* (non-Javadoc)
	 * Triggered - When Contract Process is in POSTCONTRACT CREATE
	 * 
	 * 
	 * @see com.emptoris.ecm.exit.ExitBase#postContractCreate(com.emptoris.ecm.api.intf.ExitContext)
	 */
	public final void postContractCreate(ExitContext exitContext)
			throws PluginException, WarningMessageException {			
		try{		
			
			LOG.info("===== postContractCreate - Begin - Contract #" + exitContext.getContract().getNumber());
			long timeBegin = System.currentTimeMillis();

			//Initiating the Handler Class
			LOG.info("Initiating the CREATE Handler Class ");	
			ContractCreateHandler createHandler = new ContractCreateHandler();	
			createHandler.executePostContracrtCreate(exitContext);
			LOG.info("The postContractCreate Activities Finished ");
			long timeEnd = System.currentTimeMillis();
			LOG.info("===== postContractCreate - End - Contract #"	+ exitContext.getContract().getNumber() + ", time= "
					+ (timeEnd - timeBegin) + " ms");
		} finally {
			LOG.info("Removing the value in the Creation Entry Point - while exiting PostCreate");	
			// Delete the creation Entry point value - if this is created from normal contracts
			// if from BLU - don't delete used in the preSave
			if(creationEntryPoint.get() != null) {
				if( creationEntryPoint.get().equals(CreationEntryPoint.BLU)){
					LOG.info("Contract created using BLU process; Therefore don't delete the " +
							"Thread Local use this value in Pre-Save to skip save logic");				
				} else {
					creationEntryPoint.remove();
				}
			} else {
				LOG.info("Contract created from SaveAs -creationEntryPoint will be null ");
			}
		} 			
		
	}
	
	
	
	 
	
	 
	 
	/* (non-Javadoc)
	 * Triggered - When Contract Process is in PRECONTRACT SAVE 
	 * 
	 * @see com.emptoris.ecm.exit.ExitBase#preContractSave(com.emptoris.ecm.api.intf.ExitContext)
	 */
	public final void preContractSave(ExitContext exitContext)
			throws PluginException, WarningMessageException {
		long timeBegin = System.currentTimeMillis();
		LOG.info("===== preContractSave - Begin - Contract #"	+ exitContext.getContract().getNumber());	
		
		try {
			//Initiating the Handler Class
			LOG.info("Initiating the SAVE Handler process ");
			if(creationEntryPoint.get() == null) {
				ContractSaveHandler saveHandler = new ContractSaveHandler();		
				saveHandler.executePreContractSave(exitContext);
				LOG.info("The preContractSave Activities Finished ");
			} else {
				LOG.info("Entering this block -> creationEntryPoint have value & it is continuation of BLU process");
			}			
		} finally {
			LOG.info("Clearing the creationEntry point value completely");
			creationEntryPoint.remove();
		}
		
		
		
			
		long timeEnd = System.currentTimeMillis();		
		LOG.info("===== preContractExecute - End - Contract #"	+ exitContext.getContract().getNumber() + ", time= "
				+ (timeEnd - timeBegin) + " ms");		
	}		
	 
	
	
}
	
   

