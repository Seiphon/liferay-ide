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

import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.core.util.SapphireContentAccessor;
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.modules.BladeCLI;
import com.liferay.ide.project.core.modules.BladeCLIException;
import com.liferay.ide.project.core.samples.NewSampleOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;

/**
 * @author Terry Jia
 */
public class SampleNamePossibleValuesService extends PossibleValuesService implements SapphireContentAccessor {

	@Override
	public void dispose() {
		if (_op() != null) {
			SapphireUtil.detachListener(_op().property(NewSampleOp.PROP_CATEGORY), _listener);
		}

		super.dispose();
	}

	@Override
	public boolean ordered() {
		return true;
	}

	@Override
	protected void compute(Set<String> values) {
		String category = get(_op().getCategory());

		if (CoreUtil.isNullOrEmpty(category)) {
			return;
		}

		List<String> categoryList = new ArrayList<>();

		List<String> samplesList = new ArrayList<>();

		try {
			String[] lines = BladeCLI.execute("samples");

			for (int i = 2; i < lines.length; i++) {
				if (lines[i].contains(":")) {
					lines[i] = lines[i].trim();

					lines[i] = lines[i].replace(":", "");

					categoryList.add(lines[i]);
				}
			}

			boolean belongCategory = false;

			for (int i = 2; i < lines.length; i++) {
				lines[i] = lines[i].trim();

				if (lines[i].equals(category)) {
					belongCategory = true;
				}

				if (categoryList.contains(lines[i]) && !lines[i].equals(category)) {
					belongCategory = false;
				}

				if (belongCategory) {
					samplesList.add(lines[i]);
				}
			}

			if (!samplesList.isEmpty()) {
				values.addAll(samplesList.subList(1, samplesList.size()));
			}
		}
		catch (BladeCLIException bclie) {
		}
	}

	@Override
	protected void initPossibleValuesService() {
		super.initPossibleValuesService();

		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(PropertyContentEvent event) {
				refresh();
			}

		};

		SapphireUtil.attachListener(_op().property(NewSampleOp.PROP_CATEGORY), _listener);
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private FilteredListener<PropertyContentEvent> _listener;

}