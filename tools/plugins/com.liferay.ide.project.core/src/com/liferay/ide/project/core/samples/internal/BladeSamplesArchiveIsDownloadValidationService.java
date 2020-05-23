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
import com.liferay.ide.core.util.SapphireUtil;
import com.liferay.ide.project.core.samples.NewSampleOp;
import com.liferay.ide.project.core.util.SampleProjectUtil;

import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.services.ValidationService;

/**
 * @author Seiphon Wang
 */
public class BladeSamplesArchiveIsDownloadValidationService
	extends ValidationService implements SapphireContentAccessor {

	@Override
	public void dispose() {
		NewSampleOp op = _op();

		SapphireUtil.detachListener(op.property(NewSampleOp.PROP_CATEGORY), _listener);

		super.dispose();
	}

	@Override
	protected Status compute() {
		NewSampleOp op = _op();

		if (get(op.getCategory()) != null) {
			return Status.createOkStatus();
		}

		if (SampleProjectUtil.isBladeRepoArchiveDownloading(get(_op().getLiferayVersion()))) {
			return Status.createErrorStatus(
				"Liferay Samples archive is downloading, please reopen the wizard later when downloading is done.");
		}

		return Status.createOkStatus();
	}

	@Override
	protected void initValidationService() {
		super.initValidationService();

		_listener = new FilteredListener<PropertyContentEvent>() {

			@Override
			protected void handleTypedEvent(PropertyContentEvent event) {
				compute();

				refresh();
			}

		};

		NewSampleOp op = _op();

		SapphireUtil.attachListener(op.property(NewSampleOp.PROP_CATEGORY), _listener);
	}

	private NewSampleOp _op() {
		return context(NewSampleOp.class);
	}

	private FilteredListener<PropertyContentEvent> _listener;

}