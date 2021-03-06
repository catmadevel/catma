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
package de.catma.ui.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.converter.Converter.ConversionException;

public class StringListProperty extends AbstractProperty {

	public List<String> list;
	
	public Class<?> getType() {
		return List.class;
	}
	public Object getValue() {
		return list;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValue(Object newValue) throws ReadOnlyException,
			ConversionException {
		list = new ArrayList<String>();
		list.addAll((Collection)newValue);
	}
	
	public List<String> getList() {
		return list;
	}
}
