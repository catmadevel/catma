/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
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
package de.catma.queryengine.result;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import de.catma.document.Range;

public class TagQueryResultRow extends QueryResultRow {
	
	private String markupCollectionId;
	private String tagDefinitionId;
	private String tagDefinitionPath;
	private String tagDefinitionVersion;
	private String tagInstanceId;
	private SortedSet<Range> ranges;
	private String propertyDefinitionId;
	private String propertyName;
	private String propertyValue;

	public TagQueryResultRow(String sourceDocumentId, List<Range> ranges,
			String markupCollectionId, String tagDefinitionId, String tagDefinitionPath, String tagDefinitionVersion,
			String tagInstanceId, String propertyDefinitionId, String propertyName, String propertyValue) {
		this(sourceDocumentId, ranges, markupCollectionId, tagDefinitionId, tagDefinitionPath, tagDefinitionVersion, tagInstanceId);
		
		this.propertyDefinitionId = propertyDefinitionId;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	public TagQueryResultRow(String sourceDocumentId, List<Range> ranges,
			String markupCollectionId, String tagDefinitionId, String tagDefinitionPath, 
			String tagDefinitionVersion, String tagInstanceId) {
		super(sourceDocumentId, Range.getEnclosingRange(ranges));
		this.markupCollectionId = markupCollectionId;
		this.tagDefinitionId = tagDefinitionId;
		this.tagDefinitionPath = tagDefinitionPath;
		this.tagDefinitionVersion = tagDefinitionVersion;
		this.tagInstanceId = tagInstanceId;
		this.ranges = new TreeSet<Range>();
		this.ranges.addAll(ranges);
	}
	
	public String getTagInstanceId() {
		return tagInstanceId;
	}
	
	@Override
	public String toString() {
		return super.toString()
				+ ((markupCollectionId == null)?"":("MarkupColl[#"+markupCollectionId+"]"))
				+ ((tagDefinitionId == null)?"":("TagDef[#"+tagDefinitionId+"]")) 
				+ ((tagInstanceId == null)?"":("TagInstance[#"+tagInstanceId+"]"))
				+ ((propertyDefinitionId == null)?"":("PropDef[#"+propertyDefinitionId+"]"))
				+ ((propertyValue == null)?"":("PropValue["+propertyValue+"]"));
	}

	public Set<Range> getRanges() {
		return ranges;
	}
	
	public String getMarkupCollectionId() {
		return markupCollectionId;
	}
	
	public String getTagDefinitionId() {
		return tagDefinitionId;
	}
	
	public String getTagDefinitionPath() {
		return tagDefinitionPath;
	}
	
	public String getTagDefinitionVersion() {
		return tagDefinitionVersion;
	}
	
	public String getPropertyDefinitionId() {
		return propertyDefinitionId;
	}
	
	public String getPropertyValue() {
		return propertyValue;
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((propertyDefinitionId == null) ? 0 : propertyDefinitionId.hashCode());
		result = prime * result + ((tagInstanceId == null) ? 0 : tagInstanceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TagQueryResultRow other = (TagQueryResultRow) obj;
		if (propertyDefinitionId == null) {
			if (other.propertyDefinitionId != null)
				return false;
		} else if (!propertyDefinitionId.equals(other.propertyDefinitionId))
			return false;
		if (tagInstanceId == null) {
			if (other.tagInstanceId != null)
				return false;
		} else if (!tagInstanceId.equals(other.tagInstanceId))
			return false;
		return true;
	}
	
	
}
