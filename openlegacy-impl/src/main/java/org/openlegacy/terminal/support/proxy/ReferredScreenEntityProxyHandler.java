package org.openlegacy.terminal.support.proxy;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.exceptions.OpenLegacyRuntimeException;
import org.openlegacy.terminal.TerminalSession;
import org.openlegacy.terminal.definitions.ScreenEntityDefinition;
import org.openlegacy.terminal.spi.ScreenEntitiesRegistry;

import java.text.MessageFormat;

import javax.inject.Inject;

public class ReferredScreenEntityProxyHandler implements ScreenEntityProxyHandler {

	@Inject
	private ScreenEntitiesRegistry screenEntitiesRegistry;

	private final static Log logger = LogFactory.getLog(ScreenEntityMethodInterceptor.class);

	public Object invoke(TerminalSession terminalSession, MethodInvocation invocation) throws OpenLegacyRuntimeException {

		Class<?> returnType = invocation.getMethod().getReturnType();

		// if return type is in the registry - handle child entity fetching
		ScreenEntityDefinition childScreenEntityDefinition = screenEntitiesRegistry.get(returnType);
		if (childScreenEntityDefinition == null) {
			return null;
		}
		Class<?> childScreenEntityClass = returnType;

		Object childScreenEntity = terminalSession.getEntity(childScreenEntityClass);
		logger.info(MessageFormat.format("Collected child screen for class {0}", childScreenEntityClass));

		return childScreenEntity;
	}
}