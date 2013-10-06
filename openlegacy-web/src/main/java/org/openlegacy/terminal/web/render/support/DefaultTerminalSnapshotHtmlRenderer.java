/*******************************************************************************
 * Copyright (c) 2012 OpenLegacy Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     OpenLegacy Inc. - initial API and implementation
 *******************************************************************************/
package org.openlegacy.terminal.web.render.support;

import org.apache.commons.io.IOUtils;
import org.openlegacy.exceptions.OpenLegacyRuntimeException;
import org.openlegacy.terminal.TerminalField;
import org.openlegacy.terminal.TerminalPosition;
import org.openlegacy.terminal.TerminalSnapshot;
import org.openlegacy.terminal.utils.FieldsQuery;
import org.openlegacy.terminal.utils.FieldsQuery.EditableFieldsCriteria;
import org.openlegacy.terminal.web.render.ElementsProvider;
import org.openlegacy.terminal.web.render.HtmlProportionsHandler;
import org.openlegacy.terminal.web.render.TerminalSnapshotHtmlRenderer;
import org.openlegacy.utils.DomUtils;
import org.openlegacy.utils.StringUtil;
import org.openlegacy.web.HtmlConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

public class DefaultTerminalSnapshotHtmlRenderer implements TerminalSnapshotHtmlRenderer {

	private static final String DEFAULT_SNAPSHOT_STYLE_SETTINGS = "#terminalSnapshot {direction:ltr;font-family:FONT_FAMILY;font-size:FONTpx;letter-spacing:LETTER_SPACINGpx} #terminalSnapshot span {white-space: pre;position:absolute;} #terminalSnapshot input, #terminalSnapshot textarea {position:absolute;font-family:Courier New;font-size:FONTpx;height:INPUT-HEIGHTpx;INPUT_STYLE}";

	private static final String TERMINAL_HTML = "TERMINAL_HTML";

	private String completeStyleSettings = DEFAULT_SNAPSHOT_STYLE_SETTINGS;

	private String templateResourceName = "defaultHtmlEmulationTemplate.html";
	private boolean includeTemplate = true;

	private String fontFamily = "Courier New";
	private String inputStyle = "";

	@Inject
	ApplicationContext applicationContext;

	private DefaultElementsProvider elementsProvider;

	@Inject
	@Qualifier("htmlProportionsHandler")
	private HtmlProportionsHandler htmlProportionsHandler80X24;

	@Inject
	@Qualifier("htmlProportionsHandler132X27")
	private HtmlProportionsHandler htmlProportionsHandler132X27;

	private String formActionURL = "emulation";
	private String formMethod = HtmlConstants.POST;

	public String render(TerminalSnapshot terminalSnapshot) {
		String htmlContent = renderHtml(terminalSnapshot);

		if (includeTemplate) {
			InputStream htmlEmulationTemplateStream = getClass().getResourceAsStream(templateResourceName);
			String htmlEmulationTemplateContent;
			try {
				htmlEmulationTemplateContent = IOUtils.toString(htmlEmulationTemplateStream);
			} catch (IOException e) {
				throw (new RuntimeException(e));
			}
			htmlEmulationTemplateContent = htmlEmulationTemplateContent.replace(TERMINAL_HTML, htmlContent);
			return htmlEmulationTemplateContent;
		}
		return htmlContent;
	}

	private String renderHtml(TerminalSnapshot terminalSnapshot) {

		// elementsProvider is prototype
		elementsProvider = (DefaultElementsProvider)applicationContext.getBean(ElementsProvider.class);
		elementsProvider.setSnapshot(terminalSnapshot);

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

		Document doc;
		try {
			doc = dfactory.newDocumentBuilder().newDocument();

			String styleSettings = createStyleSettings(terminalSnapshot);

			Element formTag = createWrappingTag(doc);

			createHiddens(terminalSnapshot, formTag);

			createFields(terminalSnapshot, formTag);

			String cursorFieldName = getCursorFieldName(terminalSnapshot);
			createScript(formTag, cursorFieldName);

			calculateWidthHeight(terminalSnapshot, formTag);

			// generate style before the document. cause non aligned page when it's part of the document
			styleSettings = MessageFormat.format("<style>{0}</style>", styleSettings);
			return generate(styleSettings, doc);

		} catch (ParserConfigurationException e) {
			throw (new OpenLegacyRuntimeException(e));
		} catch (TransformerException e) {
			throw (new OpenLegacyRuntimeException(e));
		}

	}

	private static String getCursorFieldName(TerminalSnapshot terminalSnapshot) {
		TerminalPosition cursorPosition = terminalSnapshot.getCursorPosition();
		// on emulation, set cursor to 1st field if on protected field
		TerminalField field = terminalSnapshot.getField(cursorPosition);
		if (field != null && !field.isEditable()) {
			List<TerminalField> editableFields = FieldsQuery.queryFields(terminalSnapshot, EditableFieldsCriteria.instance());
			if (editableFields.size() > 0) {
				cursorPosition = editableFields.get(0).getPosition();
			}
		}
		return cursorPosition != null ? HtmlNamingUtil.getFieldName(cursorPosition) : "";
	}

	private HtmlProportionsHandler getProportionHandler(TerminalSnapshot snapshot) {
		if (snapshot.getSize().getColumns() == 132) {
			return htmlProportionsHandler132X27;
		}
		return htmlProportionsHandler80X24;
	}

	private String createStyleSettings(TerminalSnapshot terminalSnapshot) {
		HtmlProportionsHandler htmlProportionsHandler = getProportionHandler(terminalSnapshot);
		String actualSyleSettings = completeStyleSettings.replaceAll("FONT_FAMILY", String.valueOf(fontFamily));
		actualSyleSettings = actualSyleSettings.replaceAll("FONT", String.valueOf(htmlProportionsHandler.getFontSize()));

		actualSyleSettings = actualSyleSettings.replaceAll("LETTER_SPACING",
				String.valueOf(htmlProportionsHandler.getLetterSpacing()));

		actualSyleSettings = actualSyleSettings.replaceAll("INPUT-HEIGHT",
				String.valueOf(htmlProportionsHandler.getInputHeight()));
		actualSyleSettings = actualSyleSettings.replace("INPUT_STYLE", inputStyle);
		return actualSyleSettings;
	}

	private Element createWrappingTag(Document doc) {
		Element formTag = (Element)doc.appendChild(doc.createElement("form"));
		formTag.setAttribute("action", formActionURL);
		formTag.setAttribute("formMethod", formMethod);
		formTag.setAttribute("name", "openlegacyForm");
		formTag.setAttribute("id", "openlegacyForm");

		Element wrapperTag = elementsProvider.createTag(formTag, HtmlConstants.DIV);
		formTag.appendChild(wrapperTag);
		wrapperTag.setAttribute(HtmlConstants.ID, TerminalHtmlConstants.WRAPPER_TAG_ID);

		return wrapperTag;
	}

	private void createScript(Element formTag, String cursorFieldName) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("try { document.");
		stringBuilder.append(TerminalHtmlConstants.HTML_EMULATION_FORM_NAME);
		stringBuilder.append(".");
		stringBuilder.append(cursorFieldName);
		stringBuilder.append(".focus(); } catch(exception) { }");
		String script = stringBuilder.toString();

		elementsProvider.createScriptTag(formTag, script);
	}

	private void createHiddens(TerminalSnapshot terminalSnapshot, Element formTag) {
		String cursorFieldName = getCursorFieldName(terminalSnapshot);
		Element cursorHidden = elementsProvider.createHidden(formTag, TerminalHtmlConstants.TERMINAL_CURSOR_HIDDEN);
		cursorHidden.setAttribute(HtmlConstants.VALUE, cursorFieldName);

		// hidden for keyboard action
		elementsProvider.createHidden(formTag, TerminalHtmlConstants.KEYBOARD_KEY);

		// hidden for session sequence
		Element sequenceHidden = elementsProvider.createHidden(formTag, TerminalHtmlConstants.SEQUENCE);
		sequenceHidden.setAttribute(HtmlConstants.VALUE, String.valueOf(terminalSnapshot.getSequence()));
	}

	private static String generate(String styleSettings, Document doc) throws TransformerConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DomUtils.render(doc, baos);
		return styleSettings + StringUtil.toString(baos);
	}

	private void calculateWidthHeight(TerminalSnapshot terminalSnapshot, Element wrapperTag) {
		HtmlProportionsHandler htmlProportionsHandler = getProportionHandler(terminalSnapshot);
		int width = htmlProportionsHandler.toWidth(terminalSnapshot.getSize().getColumns() + 1);
		int height = htmlProportionsHandler.toHeight(terminalSnapshot.getSize().getRows() + 1);

		wrapperTag.setAttribute(
				HtmlConstants.STYLE,
				MessageFormat.format("position:relative;{0}{1}", HtmlNamingUtil.toStyleWidth(width),
						HtmlNamingUtil.toStyleHeight(height)));
	}

	private void createFields(TerminalSnapshot terminalSnapshot, Element formTag) {
		Collection<TerminalField> fields = terminalSnapshot.getFields();
		for (TerminalField terminalField : fields) {
			if (terminalField.isEditable()) {
				if (terminalField.isMultyLine()) {
					elementsProvider.createTextarea(formTag, terminalField);
				} else {
					elementsProvider.createInput(formTag, terminalField);
				}
			} else {
				elementsProvider.createLabel(formTag, terminalField, terminalSnapshot.getSize());
			}
		}
	}

	public void setFormActionURL(String formActionURL) {
		this.formActionURL = formActionURL;
	}

	public void setFormMethod(String formMethod) {
		this.formMethod = formMethod;
	}

	public void setIncludeTemplate(boolean includeTemplate) {
		this.includeTemplate = includeTemplate;
	}

	public void setTemplateResourceName(String templateResourceName) {
		this.templateResourceName = templateResourceName;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public void setInputStyle(String inputStyle) {
		this.inputStyle = inputStyle;
	}

	public void setCompleteStyleSettings(String completeStyleSettings) {
		this.completeStyleSettings = completeStyleSettings;
	}
}
