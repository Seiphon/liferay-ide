/*******************************************************************************
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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
 *
 * Contributors:
 *    Kamesh Sampath - initial implementation
 ******************************************************************************/

package com.liferay.ide.eclipse.hook.ui.actions.internal;

import static com.liferay.ide.eclipse.sdk.ISDKConstants.DEFAULT_WEBCONTENT_FOLDER;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.sapphire.DisposeEvent;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ModelPropertyChangeEvent;
import org.eclipse.sapphire.modeling.ModelPropertyListener;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphirePropertyEditorActionHandler;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef;

/**
 * @author <a href="mailto:kamesh.sampath@hotmail.com">Kamesh Sampath</a>
 */
public class CreateFolderActionHandler extends SapphirePropertyEditorActionHandler {

	String propertyName;
	ModelPropertyListener listener;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.sapphire.ui.SapphireActionHandler#init(org.eclipse.sapphire.ui.SapphireAction,
	 * org.eclipse.sapphire.ui.def.ISapphireActionHandlerDef)
	 */
	@Override
	public void init( SapphireAction action, ISapphireActionHandlerDef def ) {
		super.init( action, def );
		propertyName = def.getParam( "propertyName" );
		final IModelElement element = getModelElement();
		final ModelProperty property = getProperty();
		this.listener = new ModelPropertyListener() {

			@Override
			public void handlePropertyChangedEvent( final ModelPropertyChangeEvent event ) {
				refreshEnablementState();
			}
		};

		element.addListener( this.listener, property.getName() );

		attach( new Listener() {

			@Override
			public void handle( Event event ) {
				if ( event instanceof DisposeEvent ) {
					getModelElement().removeListener( listener, getProperty().getName() );
				}
			}

		} );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.sapphire.ui.SapphireActionHandler#run(org.eclipse.sapphire.ui.SapphireRenderingContext)
	 */
	@Override
	protected Object run( SapphireRenderingContext context ) {
		final IModelElement modelElement = getModelElement();
		final ModelProperty modelProperty = getProperty();
		if ( modelProperty instanceof ValueProperty ) {
			ValueProperty valueProperty = (ValueProperty) modelProperty;
			final Value<Path> value = modelElement.read( valueProperty );
			final IProject project = modelElement.adapt( IProject.class );
			try {
				// TODO can we move it some util ? including folder and file creation ?
				IPath docrootPath = project.getFolder( DEFAULT_WEBCONTENT_FOLDER ).getLocation();
				String newFolderStr = docrootPath.append( value.getText() ).toPortableString();
				if ( !new File( newFolderStr ).exists() ) {
					boolean isCreated = new File( newFolderStr ).mkdir();
					if ( isCreated ) {
						modelElement.refresh( true, true );
						refreshEnablementState();
					}
				}
			}
			catch ( Exception e ) {

			}

		}
		return null;
	}
}
