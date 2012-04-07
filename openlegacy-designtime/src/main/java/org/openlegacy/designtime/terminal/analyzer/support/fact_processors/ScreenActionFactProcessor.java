package org.openlegacy.designtime.terminal.analyzer.support.fact_processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.designtime.terminal.model.ScreenEntityDesigntimeDefinition;
import org.openlegacy.terminal.TerminalPosition;
import org.openlegacy.terminal.definitions.TerminalActionDefinition;
import org.openlegacy.terminal.support.TerminalPositionContainerComparator;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class ScreenActionFactProcessor extends AbstractActionFactProcessor {

	private final static Log logger = LogFactory.getLog(ScreenActionFactProcessor.class);

	@Override
	protected TerminalActionDefinition buildActionDefinition(String action, String caption, TerminalPosition terminalPosition) {
		TerminalActionDefinition actionDefinition = getTerminalActionAnalyzer().toTerminalActionDefinition(action, caption,
				terminalPosition);
		return actionDefinition;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void addAction(ScreenEntityDesigntimeDefinition screenEntityDefinition, ScreenActionFact screenActionFact,
			TerminalActionDefinition actionDefinition) {
		List actions = screenEntityDefinition.getActions();

		actions.add(actionDefinition);

		Collections.sort(actions, TerminalPositionContainerComparator.instance());

		logger.info(MessageFormat.format("Added action {0}:{1} to screen entity",
				actionDefinition.getAction().getClass().getName(), actionDefinition.getDisplayName()));

	}

}
