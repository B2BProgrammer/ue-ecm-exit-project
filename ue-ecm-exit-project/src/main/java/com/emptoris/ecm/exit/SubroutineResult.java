package com.emptoris.ecm.exit;

import java.util.ArrayList;

/**
 * For accumulating the error and warning messages and setting the severity
 * of conditions encountered in a subclass of ExitSubroutine.
 */
public class SubroutineResult {
	public enum SeverityCode {
		OK, ERROR, WARNING, FATAL;
	}

	private ArrayList<String> errors = null;
	private ArrayList<String> warnings = null;
	private boolean doUpdate = false;
	private boolean doUpdateLines = false;
	private boolean doActiveLines = false;

	private int maxEntries = 0;
	private SeverityCode severity = SeverityCode.OK;

	private static final String MSG_LINE_SEPARATOR = "<br>";

	public SubroutineResult() {
		super();
	}

	/**
	 * Maximum numbers of entries in each list that will be accumulated.
	 * @param maxEntries
	 */
	public SubroutineResult(int maxEntries) {
		super();
		this.maxEntries = maxEntries;
	}

	public void addError(String text) {
		if (errors == null) {
			errors = new ArrayList<String>();
		}
		if (maxEntries == 0 || errors.size() < maxEntries) {
			errors.add(text);
		}
		if (!severity.equals(SeverityCode.FATAL)) {
			severity = SeverityCode.ERROR;
		}
	}

	public void addWarning(String text) {
		if (warnings == null) {
			warnings = new ArrayList<String>();
		}
		if (maxEntries == 0 || warnings.size() < maxEntries) {
			warnings.add(text);
		}
		if (severity.equals(SeverityCode.OK)) {
			severity = SeverityCode.WARNING;
		}
	}

	public String getErrors() {
		StringBuffer sb = new StringBuffer();
		if (errors != null) {
			for (String text: errors) {
				if (sb.length() > 0) {
					sb.append(MSG_LINE_SEPARATOR);
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}

	public String getWarnings() {
		StringBuffer sb = new StringBuffer();
		if (warnings != null) {
			for (String text: warnings) {
				if (sb.length() > 0) {
					sb.append(MSG_LINE_SEPARATOR);
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}

	public String getMessages() {
		StringBuffer sb = new StringBuffer();
		int count = 0;
		if (errors != null) {
			count++;
			for (String text: errors) {
				if (sb.length() > 0) {
					sb.append(MSG_LINE_SEPARATOR);
				}
				sb.append(text);
			}
		}
		if (warnings != null) {
			for (String text: warnings) {
				if (maxEntries > 0 && count++ > maxEntries) {
					break;
				}
				if (sb.length() > 0) {
					sb.append(MSG_LINE_SEPARATOR);
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}

	public SeverityCode getSeverity() {
		return severity;
	}

	public void setSeverity(SeverityCode severity) {
		this.severity = severity;
	}

	public boolean isDoUpdate() {
		return doUpdate;
	}

	public void setDoUpdate(boolean doUpdate) {
		this.doUpdate = doUpdate;
	}

	public boolean isDoUpdateLines() {
		return doUpdateLines;
	}

	public boolean isDoActiveLines() {
		return doActiveLines;
	}

	public void setDoUpdateLines(boolean doUpdateLines, boolean doActiveLines) {
		this.doUpdateLines = doUpdateLines;
		this.doActiveLines = doActiveLines;
	}
}
