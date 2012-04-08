package org.openlegacy.designtime.generators;

import org.apache.commons.lang.NotImplementedException;
import org.openlegacy.definitions.ActionDefinition;
import org.openlegacy.definitions.support.SimpleActionDefinition;
import org.openlegacy.designtime.terminal.generators.ScreenPojoCodeModel;
import org.openlegacy.designtime.terminal.generators.support.DefaultScreenPojoCodeModel.Action;
import org.openlegacy.designtime.terminal.generators.support.DefaultScreenPojoCodeModel.Field;
import org.openlegacy.modules.table.TableCollector;
import org.openlegacy.terminal.actions.TerminalAction;
import org.openlegacy.terminal.definitions.ScreenTableDefinition;
import org.openlegacy.terminal.definitions.SimpleScreenColumnDefinition;
import org.openlegacy.terminal.modules.table.ScrollableTableUtil;
import org.openlegacy.terminal.modules.table.TerminalDrilldownActions;
import org.openlegacy.terminal.table.TerminalDrilldownAction;
import org.openlegacy.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
public class CodeBasedScreenTableDefinition implements ScreenTableDefinition {

	private ScreenPojoCodeModel codeModel;
	private List<ActionDefinition> actions;

	public CodeBasedScreenTableDefinition(ScreenPojoCodeModel codeModel) {
		this.codeModel = codeModel;
	}

	private static void throwNotImplemented() throws UnsupportedOperationException {
		throw (new NotImplementedException("Code based table has not implemented this method"));
	}

	public Class<?> getTableClass() {
		throwNotImplemented();
		return null;
	}

	public String getTableEntityName() {
		return codeModel.getEntityName();
	}

	public int getEndRow() {
		return codeModel.getEndRow();
	}

	public List<ScreenColumnDefinition> getColumnDefinitions() {
		Collection<Field> fields = codeModel.getFields();
		List<ScreenColumnDefinition> columnDefinitions = new ArrayList<ScreenColumnDefinition>();
		for (Field field : fields) {
			SimpleScreenColumnDefinition columnDefinition = new SimpleScreenColumnDefinition(field.getName());
			columnDefinition.setDisplayName(StringUtil.stripQuotes(field.getDisplayName()));
			columnDefinition.setStartColumn(field.getColumn());
			columnDefinition.setEndColumn(field.getEndColumn());
			columnDefinitions.add(columnDefinition);
			columnDefinition.setKey(field.isKey());
			columnDefinition.setSelectionField(field.isSelectionField());
		}
		return columnDefinitions;
	}

	public ScreenColumnDefinition getColumnDefinition(String fieldName) {
		throwNotImplemented();
		return null;
	}

	public List<String> getKeyFieldNames() {
		throwNotImplemented();
		return null;
	}

	public int getMaxRowsCount() {
		throwNotImplemented();
		return 0;
	}

	public boolean isScrollable() {
		throwNotImplemented();
		return false;
	}

	public int getStartRow() {
		return codeModel.getStartRow();
	}

	public TerminalAction getNextScreenAction() {
		throwNotImplemented();
		return null;
	}

	public TerminalAction getPreviousScreenAction() {
		throwNotImplemented();
		return null;
	}

	public DrilldownDefinition getDrilldownDefinition() {
		throwNotImplemented();
		return null;
	}

	public Class<? extends TableCollector> getTableCollector() {
		throwNotImplemented();
		return null;
	}

	public void setTableCollector(Class<? extends TableCollector> tableCollector) {
		throwNotImplemented();
	}

	public String getRowSelectionField() {
		return ScrollableTableUtil.getRowSelectionField(this);
	}

	public String getMainDisplayField() {
		throwNotImplemented();
		return null;
	}

	public List<ActionDefinition> getActions() {
		if (actions == null) {
			List<Action> actionsFromCodeModel = codeModel.getActions();
			List<ActionDefinition> actionDefinitions = new ArrayList<ActionDefinition>();
			for (Action action : actionsFromCodeModel) {

				// if action was set use it, if not use enter drill down action (default)
				TerminalDrilldownAction sessionAction = TerminalDrilldownActions.enter(StringUtil.stripQuotes(action.getActionValue()));
				SimpleActionDefinition actionDefinition = new SimpleActionDefinition(sessionAction,
						StringUtil.stripQuotes(action.getDisplayName()));
				if (action.getAlias() != null) {
					actionDefinition.setAlias(StringUtil.stripQuotes(action.getAlias()));
				}
				actionDefinitions.add(actionDefinition);
			}
			actions = actionDefinitions;
		}
		return actions;
	}
}
