package com.openlegacy.enterprise.ide.eclipse.editors.pages.dnd.rpc;

import com.openlegacy.enterprise.ide.eclipse.Messages;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.AbstractAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.ActionType;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcBigIntegerFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcBooleanFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcDateFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcEnumFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcIntegerFieldAction;
import com.openlegacy.enterprise.ide.eclipse.editors.actions.rpc.RpcPartAction;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcBigIntegerFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcBooleanFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcDateFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcEntity;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcEntityModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcEnumFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcIntegerFieldModel;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcNamedObject;
import com.openlegacy.enterprise.ide.eclipse.editors.models.rpc.RpcPartModel;
import com.openlegacy.enterprise.ide.eclipse.editors.utils.rpc.RpcEntityUtils;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Ivan Bort
 * 
 */
public class FieldsTreeViewerDropHelper {

	public static void performDrop(RpcEntity entity, RpcNamedObject transferredObject, RpcNamedObject target) {
		if (transferredObject instanceof RpcFieldModel) {
			moveRpcField(entity, (RpcFieldModel)transferredObject, target);
		} else if (transferredObject instanceof RpcPartModel) {
			moveRpcPart(entity, (RpcPartModel)transferredObject, target);
		}
	}

	private static void moveRpcField(RpcEntity entity, RpcFieldModel field, RpcNamedObject target) {
		if (target.getUUID().equals(field.getParent().getUUID())) {
			return;
		}
		RpcFieldModel clone = (RpcFieldModel)getClone(field, target);
		// remove all actions for clone
		entity.removeActionsForTypes(clone.getUUID(), new ActionType[] { ActionType.ADD, ActionType.MODIFY });
		// entity.removeActionsSet(clone.getUUID());
		if (target instanceof RpcEntityModel) {
			// add field to entity model
			entity.getFields().put(clone.getUUID(), getEmptyField(clone));
			entity.getSortedFields().add(clone);
			// remove field from part model
			entity.removeRpcFieldModelFromPart(field);
		} else if (target instanceof RpcPartModel) {
			// add field to part model
			RpcPartModel model = entity.getPartByUUID(clone.getParent().getUUID());
			model.getFields().put(clone.getUUID(), getEmptyField(clone));
			model.getSortedFields().add(clone);
			// add field to sorted part model
			model = entity.getSortedPartByUUID(clone.getParent().getUUID());
			model.getFields().put(clone.getUUID(), getEmptyField(clone));
			model.getSortedFields().add(clone);
			// remove field from parent (entity or another part)
			if (field.getParent() instanceof RpcEntityModel) {
				entity.removeRpcFieldModel(field);
			} else if (field.getParent() instanceof RpcPartModel) {
				entity.removeRpcFieldModelFromPart(field);
			}
		}
		// generate actions
		generateFieldRemoveActions(entity, field);
		generateFieldModifyActions(entity, clone);
	}

	private static void moveRpcPart(RpcEntity entity, RpcPartModel part, RpcNamedObject target) {
		if (target.getUUID().equals(part.getUUID()) || target.getUUID().equals(part.getParent().getUUID())) {
			return;
		}
		/*
		 * When moved RpcPart, no need to delete and create new RpcPart. We can delete and create new java field that would be
		 * relate to this RpcPart
		 */
		RpcPartModel clone = (RpcPartModel)getClone(part, target);
		// remove all actions for clone
		entity.removeActionsForTypes(clone.getUUID(), new ActionType[] { ActionType.ADD, ActionType.MODIFY });
		// entity.removeActionsSet(clone.getUUID());
		if (target instanceof RpcEntityModel) {
			// add part to entity model
			entity.getParts().put(clone.getUUID(), getEmptyPart(clone));
			entity.getSortedParts().add(clone);
			// remove part from part model
			entity.removeRpcPartModelFromPart(part);
		} else if (target instanceof RpcPartModel) {
			// add part to part model
			RpcPartModel model = entity.getPartByUUID(clone.getParent().getUUID());
			model.getParts().put(clone.getUUID(), getEmptyPart(clone));
			model.getSortedParts().add(clone);
			// add part to sorted part model
			model = entity.getSortedPartByUUID(clone.getParent().getUUID());
			model.getParts().put(clone.getUUID(), getEmptyPart(clone));
			model.getSortedParts().add(clone);
			// remove part from parent (entity or another part)
			if (part.getParent() instanceof RpcEntityModel) {
				entity.removeRpcPartModel(part);
			} else if (part.getParent() instanceof RpcPartModel) {
				entity.removeRpcPartModelFromPart(part);
			}
		}
		// to create correct actions we need to know if clone is clone of new field
		clone.setNew(part.isNew());
		// generate actions
		generatePartRemoveActions(entity, part);
		generatePartModifyActions(entity, clone);
	}

	private static RpcNamedObject getClone(RpcNamedObject model, RpcNamedObject target) {
		RpcNamedObject clone = null;
		if (model instanceof RpcFieldModel) {
			if (model instanceof RpcBigIntegerFieldModel) {
				clone = ((RpcBigIntegerFieldModel)model).clone();
			} else if (model instanceof RpcIntegerFieldModel) {
				clone = ((RpcIntegerFieldModel)model).clone();
			} else if (model instanceof RpcBooleanFieldModel) {
				clone = ((RpcBooleanFieldModel)model).clone();
			} else if (model instanceof RpcDateFieldModel) {
				clone = ((RpcDateFieldModel)model).clone();
			} else if (model instanceof RpcEnumFieldModel) {
				clone = ((RpcEnumFieldModel)model).clone();
			} else {
				clone = ((RpcFieldModel)model).clone();
			}
		} else if (model instanceof RpcPartModel) {
			clone = ((RpcPartModel)model).clone();
		}
		clone.setParent(target);
		return clone;
	}

	private static RpcFieldModel getEmptyField(RpcFieldModel model) {
		if (model instanceof RpcBooleanFieldModel) {
			return new RpcBooleanFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		} else if (model instanceof RpcBigIntegerFieldModel) {
			return new RpcBigIntegerFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		} else if (model instanceof RpcIntegerFieldModel) {
			return new RpcIntegerFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		} else if (model instanceof RpcDateFieldModel) {
			return new RpcDateFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		} else if (model instanceof RpcEnumFieldModel) {
			return new RpcEnumFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		} else {
			return new RpcFieldModel(model.getUUID(), (RpcNamedObject)model.getParent());
		}
	}

	private static RpcPartModel getEmptyPart(RpcPartModel model) {
		RpcPartModel part = new RpcPartModel(model.getUUID(), (RpcNamedObject)model.getParent());
		// set parts and fields
		for (RpcFieldModel field : model.getFields().values()) {
			part.addRpcFieldModel(field.clone());
		}
		for (RpcPartModel partModel : model.getParts().values()) {
			part.addRpcPartModel(partModel.clone());
		}
		return part;
	}

	private static AbstractAction getAddFieldAction(RpcFieldModel model) {
		if (model instanceof RpcBooleanFieldModel) {
			return new RpcBooleanFieldAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
					model.getFieldName(), null);
		} else if (model instanceof RpcBigIntegerFieldModel) {
			return new RpcBigIntegerFieldAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
					model.getFieldName(), null);
		} else if (model instanceof RpcIntegerFieldModel) {
			return new RpcIntegerFieldAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
					model.getFieldName(), null);
		} else if (model instanceof RpcDateFieldModel) {
			return new RpcDateFieldAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
					model.getFieldName(), null);
		} else if (model instanceof RpcEnumFieldModel) {
			return new RpcEnumFieldAction(model.getUUID(), (RpcEnumFieldModel)model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
					model.getFieldName(), null);
		} else {
			return new RpcFieldAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION, model.getFieldName(),
					null);
		}
	}

	private static void generateFieldModifyActions(RpcEntity entity, RpcFieldModel model) {
		entity.addAction(getAddFieldAction(model));
		if (model instanceof RpcBooleanFieldModel) {
			RpcEntityUtils.ActionGenerator.generateRpcBooleanFieldActions(entity, (RpcBooleanFieldModel)model);
		} else if (model instanceof RpcIntegerFieldModel) {
			RpcEntityUtils.ActionGenerator.generateRpcNumericFieldActions(entity, (RpcIntegerFieldModel)model);
		} else if (model instanceof RpcDateFieldModel) {
			RpcEntityUtils.ActionGenerator.generateRpcDateFieldActions(entity, (RpcDateFieldModel)model);
		} else if (model instanceof RpcEnumFieldModel) {
			RpcEntityUtils.ActionGenerator.generateRpcEnumFieldActions(entity, (RpcEnumFieldModel)model);
		}
		RpcEntityUtils.ActionGenerator.generateRpcFieldActions(entity, model);
		entity.setDirty(true);
	}

	private static void generateFieldRemoveActions(RpcEntity entity, RpcFieldModel model) {
		// try to remove action for new field
		if (model.isNew()) {
			entity.removeAction(model.getUUID(), Messages.getString("Field.new"));//$NON-NLS-1$
		}
		entity.addAction(new RpcFieldAction(model.getUUID(), model, ActionType.REMOVE, ASTNode.FIELD_DECLARATION,
				model.getParent().getUUID().toString(), null));
	}

	private static void generatePartModifyActions(RpcEntity entity, RpcPartModel model) {
		if (model.isNew()) {
			entity.addAction(new RpcPartAction(model.getUUID(), model, ActionType.ADD, ASTNode.TYPE_DECLARATION,
					model.getClassName(), null));
		}
		entity.addAction(new RpcPartAction(model.getUUID(), model, ActionType.ADD, ASTNode.FIELD_DECLARATION,
				Messages.getString("Field.new"), null));//$NON-NLS-1$
		RpcEntityUtils.ActionGenerator.generateRpcPartAction(entity, model);
		RpcEntityUtils.ActionGenerator.generateRpcPartListAction(entity, model);
		entity.setDirty(true);
	}

	private static void generatePartRemoveActions(RpcEntity entity, RpcPartModel model) {
		// try to remove action for new part
		if (model.isNew()) {
			entity.removeAction(model.getUUID(), model.getClassName());
		}
		entity.addAction(new RpcPartAction(model.getUUID(), model, ActionType.REMOVE, ASTNode.FIELD_DECLARATION,
				model.getParent().getUUID().toString(), null));
	}

}
