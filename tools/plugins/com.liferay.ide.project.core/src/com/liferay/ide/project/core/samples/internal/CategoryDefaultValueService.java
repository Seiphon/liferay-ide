/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.ide.project.core.samples.internal;

import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.project.core.samples.NewSampleOp;
import com.liferay.ide.project.core.util.SampleProjectUtil;

import java.io.IOException;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.sapphire.DefaultValueService;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.Value;

/**
 * @author Seiphon Wang
 */
public class CategoryDefaultValueService extends DefaultValueService implements SapphireContentAccessor {

	@Override
	public void dispose() {
		super.dispose();

		PossibleValuesService possibleValuesService = _possibleValuesService();

		possibleValuesService.detach(_listener);
	}

	@Override
	protected String compute() {
		NewSampleOp op = _op();

		try {
			if (SampleProjectUtil.isBladeRepoArchiveExist(get(op.getLiferayVersion()))) {
				PossibleValuesService possibleValuesService = _possibleValuesService();

				Set<String> values = possibleValuesService.values();

				if (!values.isEmpty()) {
					Iterator<String> iterator = values.iterator();

					_defaultValue = iterator.next();
				}
			}
		}
		catch (IOException ioe) {
		}

		return _defaultValue;
	}

	@Override
	protected void initDefaultValueService() {
		super.initDefaultValueService();

		PossibleValuesService possibleValuesService = _possibleValuesService();

		_listener = new Listener() {

			@Override
			public void handle(Event event) {
				Set<String> values = possibleValuesService.values();

				if (!values.isEmpty()) {
					Iterator<String> iterator = values.iterator();

					_defaultValue = iterator.next();

					refresh();
				}
			}

		};

		possibleValuesService.attach(_listener);
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private PossibleValuesService _possibleValuesService() {
		NewSampleOp op = _op();

		Value<Object> property = op.property(NewSampleOp.PROP_CATEGORY);

		PossibleValuesService possibleValuesService = property.service(PossibleValuesService.class);

		return possibleValuesService;
	}

	private String _defaultValue = null;
	private Listener _listener;

}