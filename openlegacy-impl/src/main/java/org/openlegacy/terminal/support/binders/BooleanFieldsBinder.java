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
package org.openlegacy.terminal.support.binders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.definitions.BooleanFieldTypeDefinition;
import org.openlegacy.exceptions.EntityNotFoundException;
import org.openlegacy.terminal.FieldAttributeType;
import org.openlegacy.terminal.ScreenEntity;
import org.openlegacy.terminal.ScreenEntityBinder;
import org.openlegacy.terminal.ScreenPojoFieldAccessor;
import org.openlegacy.terminal.TerminalField;
import org.openlegacy.terminal.TerminalPosition;
import org.openlegacy.terminal.TerminalSendAction;
import org.openlegacy.terminal.TerminalSnapshot;
import org.openlegacy.terminal.definitions.ScreenFieldDefinition;
import org.openlegacy.terminal.exceptions.ScreenEntityNotAccessibleException;
import org.openlegacy.terminal.providers.ScreenFieldsDefinitionProvider;
import org.openlegacy.terminal.support.DefaultTerminalSession;
import org.openlegacy.terminal.support.SimpleTerminalPosition;
import org.openlegacy.terminal.utils.SimpleScreenPojoFieldAccessor;
import org.openlegacy.utils.ProxyUtil;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;

import javax.inject.Inject;

/**
 * Boolean binder implementation for binding boolean fields to a screenEntity based on {@link ScreenBooleanField) annotation
 * settings
 */
public class BooleanFieldsBinder implements ScreenEntityBinder, Serializable {

	private static final long serialVersionUID = 1L;

	private final static Log logger = LogFactory.getLog(DefaultTerminalSession.class);

	@Inject
	private ScreenFieldsDefinitionProvider fieldMappingsProvider;

	@Override
	public void populateEntity(Object screenEntity, TerminalSnapshot terminalSnapshot) throws EntityNotFoundException,
	ScreenEntityNotAccessibleException {

		ScreenPojoFieldAccessor fieldAccessor = null;

		Class<? extends Object> class1 = ProxyUtil.getOriginalClass(screenEntity.getClass());

		Collection<ScreenFieldDefinition> fieldDefinitions = fieldMappingsProvider.getFieldsMappingDefinitions(terminalSnapshot,
				class1);

		for (ScreenFieldDefinition fieldDefinition : fieldDefinitions) {
			if (!(fieldDefinition.getFieldTypeDefinition() instanceof BooleanFieldTypeDefinition)
					|| fieldDefinition.getAttribute() != FieldAttributeType.Value) {
				continue;
			}
			// lazy creation - mostly not used
			if (fieldAccessor == null) {
				fieldAccessor = new SimpleScreenPojoFieldAccessor(screenEntity);
			}
			BooleanFieldTypeDefinition fieldTypeDefinition = (BooleanFieldTypeDefinition)fieldDefinition.getFieldTypeDefinition();
			Assert.notNull(fieldTypeDefinition, "A field of type Boolean is defined without @ScreenBooleanField annotation");
			TerminalPosition position = fieldDefinition.getPosition();

			TerminalField booleanField = terminalSnapshot.getField(SimpleTerminalPosition.newInstance(position.getRow(),
					position.getColumn()));

			if (booleanField == null) {
				logger.warn("A mapped field not found as terminal field on snapshot:" + fieldDefinition);
				return;
			}
			if (booleanField.isHidden()) {
				logger.debug("A hidden field was not bound " + fieldDefinition.getName());
				return;
			}
			if (booleanField.getValue().equals(fieldTypeDefinition.getTrueValue())) {
				fieldAccessor.setFieldValue(fieldDefinition.getName(), Boolean.TRUE);
			} else {
				fieldAccessor.setFieldValue(fieldDefinition.getName(), Boolean.FALSE);
			}

		}
	}

	@Override
	public void populateAction(TerminalSendAction sendAction, TerminalSnapshot terminalSnapshot, Object entity) {

		if (entity == null) {
			return;
		}

		Assert.isTrue(entity instanceof ScreenEntity, "screen entity must implement ScreenEntity interface");

		ScreenEntity screenEntity = (ScreenEntity)entity;

		Collection<ScreenFieldDefinition> fieldDefinitions = fieldMappingsProvider.getFieldsMappingDefinitions(terminalSnapshot,
				screenEntity.getClass());

		if (fieldDefinitions == null) {
			return;
		}

		ScreenPojoFieldAccessor fieldAccessor = null;

		for (ScreenFieldDefinition fieldDefinition : fieldDefinitions) {
			// lazy creation - mostly not used
			if (fieldAccessor == null) {
				fieldAccessor = new SimpleScreenPojoFieldAccessor(screenEntity);
			}

			if (!fieldDefinition.isEditable() || fieldDefinition.getJavaType() != Boolean.class
					|| fieldDefinition.getAttribute() != FieldAttributeType.Value) {
				continue;
			}
			BooleanFieldTypeDefinition fieldTypeDefinition = (BooleanFieldTypeDefinition)fieldDefinition.getFieldTypeDefinition();
			Assert.notNull(fieldTypeDefinition, "A field of type Boolean is defined without @ScreenBooleanField annotation");
			TerminalPosition position = fieldDefinition.getPosition();
			TerminalField booleanField = terminalSnapshot.getField(SimpleTerminalPosition.newInstance(position.getRow(),
					position.getColumn()));

			Object fieldValue = fieldAccessor.evaluateFieldValue(fieldDefinition.getName());
			if (booleanField == null || !booleanField.isEditable()) {
				continue;
			}
			if (Boolean.TRUE.equals(fieldValue)) {
				if (!booleanField.getValue().equals(fieldTypeDefinition.getTrueValue())) {
					booleanField.setValue(fieldTypeDefinition.getTrueValue());
					sendAction.getFields().add(booleanField);
				}
			} else {
				if (fieldValue == null && fieldTypeDefinition.isTreatNullAsEmpty()) {
					if (!booleanField.getValue().equals("")) {
						booleanField.setValue("");
						sendAction.getFields().add(booleanField);
					}
				} else {
					if (!booleanField.getValue().equals(fieldTypeDefinition.getFalseValue())) {
						booleanField.setValue(fieldTypeDefinition.getFalseValue());
						sendAction.getFields().add(booleanField);
					}
				}
			}
		}
	}

}
