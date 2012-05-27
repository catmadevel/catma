package de.catma.repository.db.model;

// Generated 22.05.2012 21:58:37 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.catma.core.tag.ITagLibrary;
import de.catma.core.tag.TagDefinition;
import de.catma.core.tag.TagLibrary;
import de.catma.core.tag.TagsetDefinition;

/**
 * Taglibrary generated by hbm2java
 */
@Entity
@Table(name = "taglibrary", catalog = "CatmaRepository")
public class DBTagLibrary implements java.io.Serializable, ITagLibrary {

	private Integer tagLibraryId;
	private boolean independent;
	private ITagLibrary tagLibraryDelegate;
	private Set<DBUserTagLibrary> dbUserTagLibraries = 
			new HashSet<DBUserTagLibrary>();
	
	public DBTagLibrary() {
		this.tagLibraryDelegate = new TagLibrary(null, null);
	}
	
	public DBTagLibrary(String name, boolean independent) {
		this.tagLibraryDelegate = new TagLibrary(null, name);
		this.independent = independent;
	}

	public DBTagLibrary(ITagLibrary tagLibrary, boolean independent) {
		this.tagLibraryDelegate = tagLibrary;
		this.independent = independent;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "tagLibraryID", unique = true, nullable = false)
	public Integer getTagLibraryId() {
		return this.tagLibraryId;
	}

	public void setTagLibraryId(Integer tagLibraryId) {
		this.tagLibraryId = tagLibraryId;
		this.tagLibraryDelegate.setId(tagLibraryId.toString());
	}

	@Column(name = "independent", nullable = false)
	public boolean isIndependent() {
		return independent;
	}
	
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}
	
	@Column(name = "name", nullable = false, length = 300)
	public String getName() {
		return tagLibraryDelegate.getName();
	}

	public void setName(String name) {
		tagLibraryDelegate.setName(name);
	}

	@OneToMany(mappedBy = "dbTagLibrary")
	public Set<DBUserTagLibrary> getDbUserTagLibraries() {
		return dbUserTagLibraries;
	}
	
	public void setDbUserTagLibraries(Set<DBUserTagLibrary> dbUserTagLibraries) {
		this.dbUserTagLibraries = dbUserTagLibraries;
	}
	
	public void add(TagsetDefinition tagsetDefinition) {
		tagLibraryDelegate.add(tagsetDefinition);
	}

	public void replace(TagsetDefinition tagsetDefinition) {
		tagLibraryDelegate.replace(tagsetDefinition);
	}

	@Transient
	public TagDefinition getTagDefinition(String tagDefinitionID) {
		return tagLibraryDelegate.getTagDefinition(tagDefinitionID);
	}

	public Iterator<TagsetDefinition> iterator() {
		return tagLibraryDelegate.iterator();
	}

	@Transient
	public TagsetDefinition getTagsetDefinition(String tagsetDefinitionID) {
		return tagLibraryDelegate.getTagsetDefinition(tagsetDefinitionID);
	}

	@Transient
	public List<TagDefinition> getChildren(TagDefinition tagDefinition) {
		return tagLibraryDelegate.getChildren(tagDefinition);
	}
	
	@Transient
	public TagsetDefinition getTagsetDefinition(TagDefinition tagDefinition) {
		return tagLibraryDelegate.getTagsetDefinition(tagDefinition);
	}

	@Transient
	public Set<String> getChildIDs(TagDefinition tagDefinition) {
		return tagLibraryDelegate.getChildIDs(tagDefinition);
	}

	public void remove(TagsetDefinition tagsetDefinition) {
		tagLibraryDelegate.remove(tagsetDefinition);
	}

	@Transient
	public String getId() {
		return tagLibraryDelegate.getId();
	}

	public boolean contains(TagsetDefinition tagsetDefinition) {
		return tagLibraryDelegate.contains(tagsetDefinition);
	}

	@Transient
	public String getTagPath(TagDefinition tagDefinition) {
		return tagLibraryDelegate.getTagPath(tagDefinition);
	}
	
	@Override
	public String toString() {
		return tagLibraryDelegate.toString();
	}

	public void setId(String id) {
		tagLibraryDelegate.setId(id);
	}
}
