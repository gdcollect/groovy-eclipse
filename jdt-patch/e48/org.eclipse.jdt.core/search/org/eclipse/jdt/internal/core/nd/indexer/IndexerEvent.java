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
package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.jdt.core.IJavaElementDelta;

public class IndexerEvent {
	final IJavaElementDelta delta;

	private IndexerEvent(IJavaElementDelta delta) {
		this.delta = delta;
	}

	public static IndexerEvent createChange(IJavaElementDelta delta) {
		return new IndexerEvent(delta);
	}

	public IJavaElementDelta getDelta() {
		return this.delta;
	}
}
