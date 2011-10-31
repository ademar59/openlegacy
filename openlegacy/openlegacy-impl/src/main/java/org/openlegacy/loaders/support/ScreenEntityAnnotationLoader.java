package org.openlegacy.loaders.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openlegacy.HostEntitiesRegistry;
import org.openlegacy.annotations.screen.ScreenEntity;
import org.openlegacy.loaders.ClassAnnotationsLoader;
import org.openlegacy.terminal.definitions.SimpleScreenEntityDefinition;
import org.openlegacy.terminal.spi.ScreenEntitiesRegistry;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;

public class ScreenEntityAnnotationLoader implements ClassAnnotationsLoader {

	private final static Log logger = LogFactory.getLog(ScreenEntityAnnotationLoader.class);
	private ScreenEntitiesRegistry screenEntitiesRegistry;

	public boolean match(Annotation annotation) {
		return annotation.annotationType() == ScreenEntity.class;
	}

	public void load(HostEntitiesRegistry<?, ?> screenEntitiesRegistry, Annotation annotation, Class<?> containingClass) {
		this.screenEntitiesRegistry = (ScreenEntitiesRegistry)screenEntitiesRegistry;
		processScreenEntity((ScreenEntity)annotation, containingClass);
	}

	public void processScreenEntity(ScreenEntity screenEntity, Class<?> screenEntityClass) {
		String screenName = screenEntity.name().length() > 0 ? screenEntity.name() : screenEntityClass.getSimpleName();
		SimpleScreenEntityDefinition screenEntityDefinition = new SimpleScreenEntityDefinition(screenName, screenEntityClass);
		screenEntityDefinition.setName(screenName);
		screenEntityDefinition.setType(screenEntity.screenType());
		logger.info(MessageFormat.format("Screen \"{0}\" was added to the screen registry ({1})", screenName,
				screenEntityClass.getName()));

		screenEntitiesRegistry.add(screenEntityDefinition);
	}

}
