package com.openlegacy.enterprise.ide.eclipse.editors.pages.providers.jpa;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openlegacy.ide.eclipse.Activator;

/**
 * @author Ivan Bort
 * 
 */
public class GeneralMasterBlockLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		return Activator.getDefault().getImage(Activator.IMG_ANNOTATION);
	}

	@Override
	public String getText(Object element) {
		return element.toString();
	}

}
