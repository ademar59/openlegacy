package org.openlegacy.definitions.support;

import org.openlegacy.Session;
import org.openlegacy.SessionAction;
import org.openlegacy.definitions.ActionDefinition;

public class SimpleActionDefinition implements ActionDefinition {

	private String displayName;
	private SessionAction<? extends Session> action;
	private String alias;
	private String actionName;

	/**
	 * Used for run-time registry
	 * 
	 * @param action
	 * @param displayName
	 */
	public SimpleActionDefinition(SessionAction<? extends Session> action, String displayName) {
		this.action = action;
		this.displayName = displayName;
	}

	/**
	 * Used for design-time code generation
	 * 
	 * @param actionName
	 * @param displayName
	 */
	public SimpleActionDefinition(String actionName, String displayName) {
		this.actionName = actionName;
		this.displayName = displayName;
	}

	public SessionAction<? extends Session> getAction() {
		return action;
	}

	public String getActionName() {
		if (actionName != null) {
			return actionName;
		}
		return action.getClass().getSimpleName();
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setAction(SessionAction<? extends Session> action) {
		this.action = action;
	}
}
