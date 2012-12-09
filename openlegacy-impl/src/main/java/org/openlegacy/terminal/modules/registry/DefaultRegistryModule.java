package org.openlegacy.terminal.modules.registry;

import org.openlegacy.SessionsRegistry;
import org.openlegacy.modules.registry.Registry;
import org.openlegacy.support.SimpleSessionProperties;
import org.openlegacy.terminal.TerminalConnection;
import org.openlegacy.terminal.support.TerminalSessionModuleAdapter;

import java.util.Date;

import javax.inject.Inject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultRegistryModule extends TerminalSessionModuleAdapter implements Registry {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionsRegistry sessionsRegistry;

	@Override
	public void beforeConnect(TerminalConnection terminalConnection) {
		sessionsRegistry.register(getSession());
	}

	@Override
	public void afterConnect(TerminalConnection terminalConnection) {
		setLastActivity();
	}

	@Override
	public void afterSendAction(TerminalConnection terminalConnection) {
		setLastActivity();
	}

	@Override
	public void destroy() {
		sessionsRegistry.unregister(getSession());
		super.destroy();
	}

	private void setLastActivity() {
		((SimpleSessionProperties)getSession().getProperties()).setLastActivity(new Date());
	}

}