/*******************************************************************************
 * Copyright (c) 2014 OpenLegacy Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     OpenLegacy Inc. - initial API and implementation
 *******************************************************************************/
package org.openlegacy.designtime.generators;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.openlegacy.definitions.ActionDefinition;
import org.openlegacy.definitions.support.SimpleActionDefinition;
import org.openlegacy.designtime.terminal.generators.ScreenPojoCodeModel;
import org.openlegacy.designtime.terminal.generators.support.DefaultScreenPojoCodeModel.Action;
import org.openlegacy.designtime.terminal.generators.support.DefaultScreenPojoCodeModel.Field;
import org.openlegacy.modules.table.TableCollector;
import org.openlegacy.terminal.FieldAttributeType;
import org.openlegacy.terminal.PositionedPart;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.TerminalPosition;
import org.openlegacy.terminal.actions.TerminalAction;
import org.openlegacy.terminal.actions.TerminalActions;
import org.openlegacy.terminal.actions.TerminalActions.ENTER;
import org.openlegacy.terminal.definitions.ScreenTableDefinition;
import org.openlegacy.terminal.definitions.SimpleScreenColumnDefinition;
import org.openlegacy.terminal.definitions.SimpleTerminalActionDefinition;
import org.openlegacy.terminal.exceptions.TerminalActionException;
import org.openlegacy.terminal.modules.table.ScrollableTableUtil;
import org.openlegacy.terminal.modules.table.TerminalDrilldownActions;
import org.openlegacy.terminal.table.TerminalDrilldownAction;
import org.openlegacy.utils.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("rawtypes")
public class CodeBasedScreenTableDefinition implements ScreenTableDefinition, PositionedPart {

	private ScreenPojoCodeModel codeModel;
	private List<ActionDefinition> actions;

	// TODO should be based on codeModel
	private int width;

	// TODO should be based on codeModel
	private TerminalPosition partPosition;

	public CodeBasedScreenTableDefinition(ScreenPojoCodeModel codeModel) {
		this.codeModel = codeModel;
	}

	private static void throwNotImplemented() throws UnsupportedOperationException {
		throw (new NotImplementedException("Code based table has not implemented this method"));
	}

	@Override
	public Class<?> getTableClass() {
		throwNotImplemented();
		return null;
	}

	@Override
	public String getTableEntityName() {
		return codeModel.getEntityName();
	}

	@Override
	public int getEndRow() {
		return codeModel.getEndRow();
	}

	@Override
	public List<ScreenColumnDefinition> getColumnDefinitions() {
		Collection<Field> fields = codeModel.getFields();
		List<ScreenColumnDefinition> columnDefinitions = new ArrayList<ScreenColumnDefinition>();
		List<Field> sortedFields = new ArrayList<Field>(fields);
		Collections.sort(sortedFields, new Comparator<Field>() {

			@Override
			public int compare(Field field1, Field field2) {
				return field1.getColumn() - field2.getColumn();
			}
		});

		for (Field field : sortedFields) {
			SimpleScreenColumnDefinition columnDefinition = new SimpleScreenColumnDefinition(field.getName());
			columnDefinition.setDisplayName(field.getDisplayName());
			columnDefinition.setStartColumn(field.getColumn());
			columnDefinition.setEndColumn(field.getEndColumn());
			columnDefinitions.add(columnDefinition);
			columnDefinition.setKey(field.isKey());
			columnDefinition.setSelectionField(field.isSelectionField());
			columnDefinition.setHelpText(field.getHelpText() != null ? field.getHelpText() : "");

			// @author Ivan Bort refs assembla #112
			columnDefinition.setMainDisplayField(field.isMainDisplayField());
			columnDefinition.setEditable(field.isEditable());
			columnDefinition.setSampleValue(field.getSampleValue());
			columnDefinition.setRowsOffset(field.getRowsOffset());
			String javaTypeName = field.getType();
			if (javaTypeName.equals(Integer.class.getSimpleName()) || javaTypeName.equals(int.class.getSimpleName())) {
				columnDefinition.setJavaType(Integer.class);
			} else if (javaTypeName.equals(Boolean.class.getSimpleName())) {
				columnDefinition.setJavaType(Boolean.class);
			} else {
				columnDefinition.setJavaType(String.class);
				// TODO add other types
			}
			// @author Ivan Bort refs assembla #483
			columnDefinition.setColSpan(field.getColSpan());
			columnDefinition.setSortIndex(field.getSortIndex());
			columnDefinition.setAttribute(field.getAttributeName() != null ? FieldAttributeType.valueOf(field.getAttributeName())
					: FieldAttributeType.Value);
			columnDefinition.setTargetEntityClassName(field.getTargetEntityClassName() != null ? field.getTargetEntityClassName()
					: ScreenEntity.NONE.class.getSimpleName());

			columnDefinition.setExpression(StringUtils.isEmpty(field.getExpression()) ? "" : field.getExpression());
		}
		if (isRightToLeft()) {
			Collections.reverse(columnDefinitions);
		}
		return columnDefinitions;
	}

	@Override
	public ScreenColumnDefinition getColumnDefinition(String fieldName) {
		throwNotImplemented();
		return null;
	}

	@Override
	public List<String> getKeyFieldNames() {
		throwNotImplemented();
		return null;
	}

	@Override
	public int getMaxRowsCount() {
		throwNotImplemented();
		return 0;
	}

	@Override
	public boolean isScrollable() {
		return codeModel.isScrollable();
	}

	@Override
	public int getStartRow() {
		return codeModel.getStartRow();
	}

	@Override
	public TerminalAction getNextScreenAction() {
		throwNotImplemented();
		return null;
	}

	@Override
	public TerminalAction getPreviousScreenAction() {
		throwNotImplemented();
		return null;
	}

	@Override
	public DrilldownDefinition getDrilldownDefinition() {
		throwNotImplemented();
		return null;
	}

	@Override
	public Class<? extends TableCollector> getTableCollector() {
		throwNotImplemented();
		return null;
	}

	@Override
	public void setTableCollector(Class<? extends TableCollector> tableCollector) {
		throwNotImplemented();
	}

	@Override
	public String getRowSelectionField() {
		return ScrollableTableUtil.getRowSelectionField(this);
	}

	@Override
	public List<String> getMainDisplayFields() {
		throwNotImplemented();
		return null;
	}

	@Override
	public List<ActionDefinition> getActions() {
		if (actions == null) {
			List<Action> actionsFromCodeModel = codeModel.getActions();
			List<ActionDefinition> actionDefinitions = new ArrayList<ActionDefinition>();
			for (Action action : actionsFromCodeModel) {

				// if action was set use it, if not use enter drill down action (default)
				TerminalDrilldownAction sessionAction = TerminalDrilldownActions.newAction(ENTER.class, action.getActionValue());

				if (!StringUtils.isBlank(action.getActionName())) {
					try {
						TerminalAction terminalAction = TerminalActions.newAction(action.getActionName());
						if (terminalAction != null) {
							sessionAction = TerminalDrilldownActions.newAction(terminalAction.getClass(), action.getActionValue());
						}
					} catch (TerminalActionException e) {

					}
				}

				SimpleActionDefinition actionDefinition = new SimpleTerminalActionDefinition(sessionAction,
						action.getAdditionalKey(), StringUtil.stripQuotes(action.getDisplayName()), null);

				if (!StringUtils.isBlank(action.getActionName())) {
					actionDefinition = new SimpleTerminalActionDefinition(action.getActionName(),
							StringUtil.stripQuotes(action.getDisplayName()), null);
					actionDefinition.setAction(sessionAction);
					((SimpleTerminalActionDefinition)actionDefinition).setAdditionalKey(action.getAdditionalKey());
				}
				if (action.getAlias() != null) {
					actionDefinition.setAlias(StringUtil.stripQuotes(action.getAlias()));
				}
				if (action.getTargetEntityName() != null) {
					actionDefinition.setTargetEntityName(action.getTargetEntityName());
				}
				actionDefinition.setRow(action.getRow());
				actionDefinition.setColumn(action.getColumn());
				actionDefinition.setLength(action.getLength());
				actionDefinition.setWhen(action.getWhen());
				actionDefinitions.add(actionDefinition);
			}
			actions = actionDefinitions;
		}
		return actions;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setPartPosition(TerminalPosition partPosition) {
		this.partPosition = partPosition;
	}

	@Override
	public TerminalPosition getPartPosition() {
		return partPosition;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public ActionDefinition getDefaultAction() {
		throwNotImplemented();
		return null;
	}

	@Override
	public int getRowGaps() {
		return codeModel.getRowGaps();
	}

	@Override
	public ScreenColumnDefinition getSelectionColumn() {
		throwNotImplemented();
		return null;
	}

	public String getNextScreenActionName() {
		return codeModel.getNextScreenActionName();
	}

	public String getPreviousScreenActionName() {
		return codeModel.getPreviousScreenActionName();
	}

	public String getTableCollectorName() {
		return codeModel.getTableCollectorName();
	}

	public boolean isSupportTerminalData() {
		return codeModel.isSupportTerminalData();
	}

	public String getClassName() {
		return codeModel.getClassName();
	}

	@Override
	public List<String> getSortedByFieldNames() {
		throwNotImplemented();
		return null;
	}

	@Override
	public int getScreensCount() {
		return codeModel.getScreensCount();
	}

	@Override
	public List<ScreenTableReferenceDefinition> getTableReferenceDefinitions() {
		return null;
	}

	@Override
	public String getFilterExpression() {
		return codeModel.getFilterExpression();
	}

	@Override
	public boolean isRightToLeft() {
		return codeModel.isRightToLeft();
	}

	@Override
	public String getStopExpression() {
		return codeModel.getStopExpression();
	}

}
