package de.catma.repository.db.model;

// Generated 23.05.2012 12:54:30 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * CorpusStaticmarkupcollection generated by hbm2java
 */
@Entity
@Table(name = "corpus_staticmarkupcollection", catalog = "CatmaRepository")
public class CorpusStaticmarkupcollection implements java.io.Serializable {

	private Integer corpusStaticmarkupcollectionId;
	private int corpusId;
	private int staticMarkupCollectionId;

	public CorpusStaticmarkupcollection() {
	}

	public CorpusStaticmarkupcollection(int corpusId,
			int staticMarkupCollectionId) {
		this.corpusId = corpusId;
		this.staticMarkupCollectionId = staticMarkupCollectionId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "corpus_staticmarkupcollectionID", unique = true, nullable = false)
	public Integer getCorpusStaticmarkupcollectionId() {
		return this.corpusStaticmarkupcollectionId;
	}

	public void setCorpusStaticmarkupcollectionId(
			Integer corpusStaticmarkupcollectionId) {
		this.corpusStaticmarkupcollectionId = corpusStaticmarkupcollectionId;
	}

	@Column(name = "corpusID", nullable = false)
	public int getCorpusId() {
		return this.corpusId;
	}

	public void setCorpusId(int corpusId) {
		this.corpusId = corpusId;
	}

	@Column(name = "staticMarkupCollectionID", nullable = false)
	public int getStaticMarkupCollectionId() {
		return this.staticMarkupCollectionId;
	}

	public void setStaticMarkupCollectionId(int staticMarkupCollectionId) {
		this.staticMarkupCollectionId = staticMarkupCollectionId;
	}

}
