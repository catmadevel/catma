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
package de.catma.repository.db.model;

// Generated 22.05.2012 21:58:37 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import de.catma.document.standoffmarkup.usermarkup.UserMarkupCollection;

/**
 * Usermarkupcollection generated by hbm2java
 */
@Entity
@Table(name = "usermarkupcollection", catalog = "CatmaRepository")
public class DBUserMarkupCollection implements java.io.Serializable {

	private Integer usermarkupCollectionId;
	private int sourceDocumentId;
	private Set<DBUserUserMarkupCollection> dbUserUserMarkupCollections = 
			new HashSet<DBUserUserMarkupCollection>();
	private Set<DBTagReference> dbTagReferences = new HashSet<DBTagReference>();
	private String title;
	private String publisher;
	private String author;
	private String description;
	private Integer dbTagLibraryId;
	
	public DBUserMarkupCollection() {
	}
	
	public DBUserMarkupCollection(int sourceDocumentId, String title) {
		this.sourceDocumentId = sourceDocumentId;
		this.title = title;
	}

	public DBUserMarkupCollection(Integer sourceDocumentId,
			UserMarkupCollection umc, Integer dbTagLibraryId) {
		this(sourceDocumentId, umc.getContentInfoSet().getTitle());
		this.author = umc.getContentInfoSet().getAuthor();
		this.publisher = umc.getContentInfoSet().getPublisher();
		this.description = umc.getContentInfoSet().getDescription();
		this.dbTagLibraryId = dbTagLibraryId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "usermarkupCollectionID", unique = true, nullable = false)
	public Integer getUsermarkupCollectionId() {
		return this.usermarkupCollectionId;
	}

	public void setUsermarkupCollectionId(Integer usermarkupCollectionId) {
		this.usermarkupCollectionId = usermarkupCollectionId;
	}

	@Column(name = "title", length = 300)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "publisher", length = 300)
	public String getPublisher() {
		return this.publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Column(name = "author", length = 300)
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Column(name = "description", length = 300)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "sourceDocumentID", nullable = false)
	public int getSourceDocumentId() {
		return this.sourceDocumentId;
	}

	public void setSourceDocumentId(int sourceDocumentId) {
		this.sourceDocumentId = sourceDocumentId;
	}

	@Cascade({CascadeType.DELETE, CascadeType.SAVE_UPDATE})
	@Column(name = "tagLibraryID", nullable = false)
	public Integer getDbTagLibraryId() {
		return dbTagLibraryId;
	}
	
	public void setDbTagLibraryId(Integer dbTagLibraryId) {
		this.dbTagLibraryId = dbTagLibraryId;
	}
	
	@OneToMany(mappedBy = "dbUserMarkupCollection")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<DBUserUserMarkupCollection> getDbUserUserMarkupCollections() {
		return dbUserUserMarkupCollections;
	}
	
	public void setDbUserUserMarkupCollections(
			Set<DBUserUserMarkupCollection> dbUserUserMarkupCollections) {
		this.dbUserUserMarkupCollections = dbUserUserMarkupCollections;
	}
	
	@OneToMany(mappedBy = "dbUserMarkupCollection")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<DBTagReference> getDbTagReferences() {
		return dbTagReferences;
	}
	
	public void setDbTagReferences(Set<DBTagReference> dbTagReferences) {
		this.dbTagReferences = dbTagReferences;
	}
	
	public String toString() {
		return getTitle();
	}

	public boolean hasAccess(DBUser dbUser) {
		for (DBUserUserMarkupCollection uumc : getDbUserUserMarkupCollections()) {
			if (uumc.getDbUser().getUserId().equals(dbUser.getUserId())) {
				return true;
			}
		}
		return false;
	}
	
	@Transient
	public String getId() {
		return (getUsermarkupCollectionId() == null) ? null :
			String.valueOf(getUsermarkupCollectionId());
	}
}