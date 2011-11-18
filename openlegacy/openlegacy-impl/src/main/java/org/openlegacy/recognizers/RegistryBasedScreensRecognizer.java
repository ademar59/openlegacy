package org.openlegacy.recognizers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.terminal.TerminalScreen;
import org.openlegacy.terminal.definitions.ScreenEntityDefinition;
import org.openlegacy.terminal.spi.ScreensRecognizer;
import org.openlegacy.terminal.support.DefaultScreenEntitiesRegistry;

import javax.inject.Inject;

/**
 * Registry based recognizer. Based on @ScreenEntity (identifier=...) definitions
 * 
 */
public class RegistryBasedScreensRecognizer implements ScreensRecognizer {

	@Inject
	private DefaultScreenEntitiesRegistry screensRegistry;

	private final static Log logger = LogFactory.getLog(RegistryBasedScreensRecognizer.class);

	public Class<?> match(TerminalScreen terminalScreen) {
		ScreenEntityDefinition screenEntityDefinition = screensRegistry.match(terminalScreen);
		if (screenEntityDefinition != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Screen matched by registry:" + screenEntityDefinition.getEntityClass());
			}
			return screenEntityDefinition.getEntityClass();
		}
		return null;
	}

}
