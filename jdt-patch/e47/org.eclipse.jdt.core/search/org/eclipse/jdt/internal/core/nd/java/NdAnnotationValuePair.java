/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdAnnotationValuePair extends NdNode {
	public static final FieldManyToOne<NdAnnotation> APPLIES_TO;
	public static final FieldString NAME;
	public static final FieldOneToOne<NdConstant> VALUE;

	@SuppressWarnings("hiding")
	public static final StructDef<NdAnnotationValuePair> type;

	static {
		type = StructDef.create(NdAnnotationValuePair.class, NdNode.type);
		APPLIES_TO = FieldManyToOne.createOwner(type, NdAnnotation.ELEMENT_VALUE_PAIRS);
		NAME = type.addString();
		VALUE = FieldOneToOne.create(type, NdConstant.class, NdConstant.PARENT_ANNOTATION_VALUE);
		type.done();
	}

	public NdAnnotationValuePair(Nd nd, long address) {
		super(nd, address);
	}

	public NdAnnotationValuePair(NdAnnotation annotation, char[] name) {
		super(annotation.getNd());
		Nd nd = annotation.getNd();
		APPLIES_TO.put(nd, this.address, annotation);
		NAME.put(nd, this.address, name);
	}

	public NdAnnotation getAnnotation() {
		return APPLIES_TO.get(getNd(), this.address);
	}

	public IString getName() {
		return NAME.get(getNd(), this.address);
	}

	public void setName(String name) {
		NAME.put(getNd(), this.address, name);
	}

	/**
	 * Returns the value of this annotation or null if none
	 */
	public NdConstant getValue() {
		return VALUE.get(getNd(), this.address);
	}

	public void setValue(NdConstant value) {
		VALUE.put(getNd(), this.address, value);
	}
}
