package org.openlegacy.db.loaders.support;

import org.apache.commons.lang.StringUtils;
import org.openlegacy.EntitiesRegistry;
import org.openlegacy.EntityDefinition;
import org.openlegacy.db.definitions.DbEntityDefinition;
import org.openlegacy.db.definitions.DbFieldDefinition;
import org.openlegacy.db.definitions.SimpleDbColumnFieldDefinition;
import org.openlegacy.db.definitions.SimpleDbFieldDefinition;
import org.openlegacy.db.definitions.SimpleDbOneToManyDefinition;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

@Component
public class DbJpaOneToManyAnnotationLoader extends DbJpaColumnAnnotationLoader {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean match(EntitiesRegistry entitiesRegistry, Field field) {
		return field.getAnnotation(OneToMany.class) != null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void load(EntitiesRegistry entitiesRegistry, Field field, Class<?> containingClass, int fieldOrder) {

		DbEntityDefinition dbEntityDefinition = (DbEntityDefinition)entitiesRegistry.get(containingClass);
		if (dbEntityDefinition != null) {
			DbFieldDefinition dbFieldDefinition = dbEntityDefinition.getColumnFieldsDefinitions().get(field.getName());
			if (dbFieldDefinition == null) {
				dbFieldDefinition = createFrom(field,
						(SimpleDbFieldDefinition)dbEntityDefinition.getFieldsDefinitions().get(field.getName()));
			}
			if (dbFieldDefinition instanceof SimpleDbColumnFieldDefinition) {
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				SimpleDbOneToManyDefinition simpleDbOneToMany = new SimpleDbOneToManyDefinition();
				simpleDbOneToMany.setCascadeTypeNames(StringUtils.join(oneToMany.cascade(), ',').split(","));
				simpleDbOneToMany.setFetchTypeName(oneToMany.fetch().name());
				simpleDbOneToMany.setMappedBy(oneToMany.mappedBy());
				simpleDbOneToMany.setOrphanRemoval(oneToMany.orphanRemoval());
				simpleDbOneToMany.setTargetEntity(oneToMany.targetEntity());
				simpleDbOneToMany.setTargetEntityClassName(oneToMany.targetEntity().getSimpleName());
				JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
				if (joinColumn != null) {
					simpleDbOneToMany.setJoinColumnName(joinColumn.name());
				}

				Type genericFieldType = field.getGenericType();
				if (genericFieldType instanceof ParameterizedType) {
					ParameterizedType pType = (ParameterizedType)genericFieldType;
					Type[] fieldArgTypes = pType.getActualTypeArguments();
					Class actualClass = (Class)fieldArgTypes[fieldArgTypes.length - 1];
					EntityDefinition entityDefinition = entitiesRegistry.get(actualClass);

					if (entityDefinition != null) {
						simpleDbOneToMany.setTargetEntityDefinition(entityDefinition);
					}
				}

				SimpleDbColumnFieldDefinition columnFieldDefinition = (SimpleDbColumnFieldDefinition)dbFieldDefinition;
				columnFieldDefinition.setOneToManyDefinition(simpleDbOneToMany);
			}
		}
	}
}
