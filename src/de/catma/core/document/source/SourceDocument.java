/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

package de.catma.core.document.source;

import java.util.List;

import de.catma.core.document.Range;
import de.catma.core.document.source.contenthandler.SourceContentHandler;
import de.catma.core.document.standoffmarkup.structure.StructureMarkupCollectionReference;
import de.catma.core.document.standoffmarkup.user.UserMarkupCollectionReference;

/**
 * The representation of a Source Document.
 *
 * @author Marco Petris
 *
 */
public class SourceDocument {
	
	private SourceContentHandler handler;
	private ContentInfoSet contentInfoSet;
	private IndexInfoSet indexInfoSet;
	private TechInfoSet techInfoSet;
	private List<StructureMarkupCollectionReference> structureMarkupCollectionRefs;
	private List<UserMarkupCollectionReference> userMarkupCollectionRefs;
	
	public SourceDocument(
			SourceContentHandler handler,
			ContentInfoSet contentInfoSet,
			IndexInfoSet indexInfoSet,
			TechInfoSet techInfoSet,
			List<StructureMarkupCollectionReference> structureMarkupCollectionRefs,
			List<UserMarkupCollectionReference> userMarkupCollectionRefs) {
		super();
		this.handler = handler;
		this.contentInfoSet = contentInfoSet;
		this.indexInfoSet = indexInfoSet;
		this.techInfoSet = techInfoSet;
		this.structureMarkupCollectionRefs = structureMarkupCollectionRefs;
		this.userMarkupCollectionRefs = userMarkupCollectionRefs;
	}

	/**
	 * Displays the content of the document as text.
	 */
	@Override
	public String toString() {
		return getContent( 0 ).toString();
	}

	/**
	 * @param range the range of the content
	 * @return the content between the startpoint and the endpoint of the range
	 */
	public String getContent( Range range ) {
		return getContent( range.getStartPoint(), range.getEndPoint() );
	}
	
	/**
	 * @param startPoint startpoint of the content
	 * @param endPoint endpoint of the content
	 * @return the content between the startpoint and the endpoint
	 */
	public String getContent( long startPoint, long endPoint ) {
		return handler.getContent( startPoint, endPoint );
	}
	
	/**
	 * @param startPoint startpoint of the content
	 * @return the content after the startpoint
	 */
	public String getContent( long startPoint ) {
		return handler.getContent( startPoint );
	}
	
	/**
	 * @return the size of the document
	 */
	public long getSize() {
		return handler.getContent( 0 ).length();
	}
}