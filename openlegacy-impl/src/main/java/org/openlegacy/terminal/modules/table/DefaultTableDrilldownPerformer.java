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
package org.openlegacy.terminal.modules.table;

import org.openlegacy.modules.table.drilldown.DrilldownAction;
import org.openlegacy.modules.table.drilldown.DrilldownException;
import org.openlegacy.modules.table.drilldown.RowComparator;
import org.openlegacy.modules.table.drilldown.RowFinder;
import org.openlegacy.modules.table.drilldown.RowSelector;
import org.openlegacy.modules.table.drilldown.TableScrollStopConditions;
import org.openlegacy.modules.table.drilldown.TableScroller;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.ScreenPojoFieldAccessor;
import org.openlegacy.terminal.TerminalSession;
import org.openlegacy.terminal.definitions.ScreenTableDefinition;
import org.openlegacy.terminal.definitions.ScreenTableDefinition.DrilldownDefinition;
import org.openlegacy.terminal.exceptions.ScreenEntityNotAccessibleException;
import org.openlegacy.terminal.providers.TablesDefinitionProvider;
import org.openlegacy.terminal.table.ScreenTableDrilldownPerformer;
import org.openlegacy.terminal.utils.SimpleScreenPojoFieldAccessor;
import org.openlegacy.utils.ProxyUtil;
import org.openlegacy.utils.SpringUtil;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

/**
 * Default terminal session drill down performer. Fetch the source entity class table definitions, looks from a row in the given
 * screen, and performs scroll until the row is found
 * 
 */
public class DefaultTableDrilldownPerformer implements ScreenTableDrilldownPerformer {

	@Inject
	private TablesDefinitionProvider tablesDefinitionProvider;

	@Inject
	private ApplicationContext applicationContext;

	private int maxPaging = 20;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> T drilldown(DrilldownDefinition drilldownDefinition, TerminalSession session, Class<?> sourceEntityClass,
			Class<T> targetEntityClass, DrilldownAction<?> drilldownAction, Object... rowKeys) {

		RowFinder rowFinder = getDefaultBean(drilldownDefinition.getRowFinder());

		RowSelector rowSelector = getDefaultBean(drilldownDefinition.getRowSelector());

		TableScroller tableScroller = getDefaultBean(drilldownDefinition.getTableScroller());

		RowComparator rowComparator = getDefaultBean(drilldownDefinition.getRowComparator());

		TableScrollStopConditions tableScrollStopConditions = getDefaultBean(drilldownDefinition.getTableScrollStopCondition());

		Entry<String, ScreenTableDefinition> singleScrollableTableDefinition = ScrollableTableUtil.getSingleScrollableTableDefinition(
				tablesDefinitionProvider, sourceEntityClass);
		ScreenTableDefinition tableDefinition = singleScrollableTableDefinition.getValue();
		String tableFieldName = singleScrollableTableDefinition.getKey();

		ScreenEntity currentEntity = (ScreenEntity)session.getEntity(sourceEntityClass);

		ScreenPojoFieldAccessor fieldAccessor = null;

		Integer rowNumber = null;

		int totalPaging = 0;
		do {
			fieldAccessor = new SimpleScreenPojoFieldAccessor(currentEntity);
			List<?> tableRows = (List<?>)fieldAccessor.getFieldValue(tableFieldName);
			try {
				rowNumber = rowFinder.findRow(rowComparator, tableRows, rowKeys);
			} catch (IllegalArgumentException e) {
				throw (new ScreenEntityNotAccessibleException(e, targetEntityClass.getSimpleName()));
			}
			if (rowNumber == null) {
				currentEntity = (ScreenEntity)tableScroller.scroll(session, sourceEntityClass, tableScrollStopConditions, rowKeys);
			}
			totalPaging++;
		} while (rowNumber == null && currentEntity != null && totalPaging < maxPaging);

		if (rowNumber != null) {
			rowSelector.selectRow(session, currentEntity, drilldownAction, rowNumber);
			currentEntity = session.getEntity();

			if (ProxyUtil.isClassesMatch(currentEntity.getClass(), targetEntityClass)) {
				return (T)currentEntity;
			}
			return null;
		}
		throw (new DrilldownException("Unable to drilldown into " + targetEntityClass + ", with key field: "
				+ Arrays.toString(tableDefinition.getKeyFieldNames().toArray()) + " with keys values:" + Arrays.toString(rowKeys)));
	}

	private <T> T getDefaultBean(Class<T> clazz) {
		return SpringUtil.getDefaultBean(applicationContext, clazz);
	}

	public void setMaxPaging(int maxPaging) {
		this.maxPaging = maxPaging;
	}
}
