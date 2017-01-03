package com.emptoris.ecm.exit;

import com.dicarta.infra.common.PluginException;
import com.dicarta.infra.logging.ILogger;
import com.dicarta.infra.logging.Logger;
import com.emptoris.common.config.ConfigurationException;
import com.emptoris.ecm.exit.common.config.ExitConfig;
import com.emptoris.ecm.exit.common.config.ProjectExitContext;

public class CustomExitContext extends ProjectExitContext {
    private final ILogger LOG  = Logger.getLogger(CustomExitContext.class);

    public static CustomExitContext _instance = null;
    public synchronized static CustomExitContext getInstance() throws ConfigurationException
    {
        if (_instance == null) {
            _instance = new CustomExitContext();
        }
        return _instance;
    }

    protected CustomExitContext() throws ConfigurationException {
        this(ExitConfig.ECM_PLUGIN_CONFIG_PATH,
             ExitConfig.ECM_PLUGIN_CONFIG_FILE);
    }

    protected CustomExitContext(String configFilePath, String configFileName)
            throws ConfigurationException {
        super(configFilePath, configFileName);
    }

    @Override
    public void logError(String messageText, Throwable e)
            throws PluginException {
        LOG.error(messageText, e);
    }

    // ===== Keys for configuration parameters - Begin =====
    // Example of String parameter name
//    public static final String TERM_RESEND = "terms.resend";
    // ===== Keys for configuration parameters - End =====

    // ===== Key values for configuration parameters - Begin =====
    // Example: Value from resource file for configuration parameter TERM_RESEND
//    private String resendTerm;
    // ===== Key values for configuration parameters - End =====

    protected void loadAndValidateCustomConfiguration() throws ConfigurationException {
        // Example of loading a required string parameter
//        this.resendTerm = this.config.getRequiredStringConfiguration(TERM_RESEND);
    }

    // Example: Provide getter for TERM_RESET for exit code, but no setter.
//    public String getResendTerm() {
//        return resendTerm;
//    }
}
