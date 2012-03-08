/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2012  University Of Hamburg
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
package de.catma.ui.client.ui.tagger.shared;

/**
 * @author marco.petris@web.de
 *
 */
public enum EventAttribute {
	/**
	 * Attribute signals a new page to the client side. This
	 * event is usually accompanied by one or more numbered 
	 * {@link EventAttribute#TAGINSTANCE_ADD} attributes.
	 */
	PAGE_SET,
	TAGDEFINITION_SELECTED,
	TAGINSTANCE_CLEAR,
	TAGINSTANCE_ADD,
	TAGINSTANCES_ADD,
	LOGMESSAGE, 
	TAGINSTANCE_REMOVE,
	TAGSETDEFINITION_ATTACH,
	;
}