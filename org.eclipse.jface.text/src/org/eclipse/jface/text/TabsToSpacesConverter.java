/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Auto edit strategy that converts tabs into spaces.
 * <p>
 * Clients usually instantiate and configure this class but
 * can also extend it in their own subclass.
 * </p>
 *
 * @since 3.3
 */
public class TabsToSpacesConverter implements IAutoEditStrategy {

	private int fTabRatio;
	private ILineTracker fLineTracker;


	public void setNumberOfSpacesPerTab(int ratio) {
		fTabRatio= ratio;
	}

	public void setLineTracker(ILineTracker lineTracker) {
		fLineTracker= lineTracker;
	}

	private int insertTabString(StringBuilder buffer, int offsetInLine) {

		if (fTabRatio == 0)
			return 0;

		int remainder= offsetInLine % fTabRatio;
		remainder= fTabRatio - remainder;
		for (int i= 0; i < remainder; i++)
			buffer.append(' ');
		return remainder;
	}

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		String text= command.text;
		if (text == null)
			return;

		int index= text.indexOf('\t');
		if (index > -1) {

			StringBuilder buffer= new StringBuilder();

			fLineTracker.set(command.text);
			int lines= fLineTracker.getNumberOfLines();

			try {

				for (int i= 0; i < lines; i++) {

					int offset= fLineTracker.getLineOffset(i);
					int endOffset= offset + fLineTracker.getLineLength(i);
					String line= text.substring(offset, endOffset);

					int position= 0;
					if (i == 0) {
						IRegion firstLine= document.getLineInformationOfOffset(command.offset);
						position= command.offset - firstLine.getOffset();
					}

					int length= line.length();
					for (int j= 0; j < length; j++) {
						char c= line.charAt(j);
						if (c == '\t') {
							position += insertTabString(buffer, position);
						} else {
							buffer.append(c);
							++ position;
						}
					}

				}

				command.text= buffer.toString();

			} catch (BadLocationException x) {
			}
		}
	}
}
