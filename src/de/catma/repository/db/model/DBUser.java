package de.catma.repository.db.model;

// Generated 22.05.2012 21:58:37 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * User generated by hbm2java
 */
@Entity
@Table(name = "user", catalog = "CatmaRepository")
public class DBUser implements java.io.Serializable {

	private Integer userId;
	private String identifier;
	private boolean locked;
	private Set<DBSourceDocument> dbSourceDocuments = 
			new HashSet<DBSourceDocument>(0);

	public DBUser() {
	}

	public DBUser(String identifier, boolean locked) {
		this.identifier = identifier;
		this.locked = locked;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "userID", unique = true, nullable = false)
	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Column(name = "identifier", nullable = false, length = 300)
	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Column(name = "locked", nullable = false)
	public boolean isLocked() {
		return this.locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(
		name = "user_sourcedocument", 
		joinColumns = { @JoinColumn(name = "userID") },
		inverseJoinColumns = { @JoinColumn(name = "sourceDocumentID") })
	public Set<DBSourceDocument> getDbSourceDocuments() {
		return dbSourceDocuments;
	}
	
	public void setDbSourceDocuments(Set<DBSourceDocument> dbSourceDocuments) {
		this.dbSourceDocuments = dbSourceDocuments;
	}

}