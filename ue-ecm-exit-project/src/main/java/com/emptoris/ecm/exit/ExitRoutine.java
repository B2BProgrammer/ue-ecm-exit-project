package com.emptoris.ecm.exit;

import org.apache.axis.AxisFault;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.ecm.api.intf.ExitContext;
import com.emptoris.ecm.exit.CustomExitContext;

/**
 * Parent class for operations performed from the ExitSubroutine class.
 * 
 */
public abstract class ExitRoutine {
    private final ILogger LOG  = Logger.getLogger(ExitRoutine.class);

    protected static final String ERROR_MSG_PERSISTENCE_CONTRACT = "persistence.failure.contract";
   

    // moduleName is set by the module and used for logging
    protected String moduleName = "ExitRoutine";

    protected ExitContext       _context   = null;
    protected CustomExitContext _cec =  null;

    public ExitRoutine(ExitContext context, CustomExitContext cec) {
        super();
        _context = context;
        _cec     = cec;
    }

	protected void addError(SubroutineResult result, boolean isError, String msgKey, Object[] args) {
		String msg = _cec.formatMessage(msgKey, args);
		LOG.info(moduleName + ": " + msg);
		if (isError) {
			result.addError(msg);
		} else {
			result.addWarning(msg);
		}
	}

	protected void handleWsException(Exception e) throws PluginException {
		String msg = null;
		if (e instanceof AxisFault) {
			try {
				msg = ((AxisFault)e).getFaultDetails()[0].getFirstChild().getTextContent();
				int ix = msg.indexOf('\n');
				if (ix > 0) {
					msg = msg.substring(0, ix-1);
				}
			} catch (Throwable t) {
				msg = _cec.getCauseMessages(e);
			}
		} else {
			msg = _cec.getCauseMessages(e);
		}
		LOG.error(moduleName + ": " + e.getMessage());
		LOG.error(e);
	}
}
