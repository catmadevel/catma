/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooqgen.catmarepository.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sourcedocument extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -405533791;

	/**
	 * The singleton instance of <code>CatmaRepository.sourcedocument</code>
	 */
	public static final de.catma.repository.db.jooqgen.catmarepository.tables.Sourcedocument SOURCEDOCUMENT = new de.catma.repository.db.jooqgen.catmarepository.tables.Sourcedocument();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.sourcedocument.sourceDocumentID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> SOURCEDOCUMENTID = createField("sourceDocumentID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.title</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> TITLE = createField("title", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.publisher</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> PUBLISHER = createField("publisher", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.author</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> AUTHOR = createField("author", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.description</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.sourceUri</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> SOURCEURI = createField("sourceUri", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.fileType</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> FILETYPE = createField("fileType", org.jooq.impl.SQLDataType.VARCHAR.length(5), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.charset</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> CHARSET = createField("charset", org.jooq.impl.SQLDataType.VARCHAR.length(50), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.fileOSType</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> FILEOSTYPE = createField("fileOSType", org.jooq.impl.SQLDataType.VARCHAR.length(15), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.checksum</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Long> CHECKSUM = createField("checksum", org.jooq.impl.SQLDataType.BIGINT, this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.mimeType</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> MIMETYPE = createField("mimeType", org.jooq.impl.SQLDataType.VARCHAR.length(255), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.xsltDocumentLocalUri</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> XSLTDOCUMENTLOCALURI = createField("xsltDocumentLocalUri", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.locale</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> LOCALE = createField("locale", org.jooq.impl.SQLDataType.VARCHAR.length(15), this);

	/**
	 * The column <code>CatmaRepository.sourcedocument.localUri</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> LOCALURI = createField("localUri", org.jooq.impl.SQLDataType.VARCHAR.length(300), this);

	/**
	 * Create a <code>CatmaRepository.sourcedocument</code> table reference
	 */
	public Sourcedocument() {
		super("sourcedocument", de.catma.repository.db.jooqgen.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.sourcedocument</code> table reference
	 */
	public Sourcedocument(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooqgen.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooqgen.catmarepository.tables.Sourcedocument.SOURCEDOCUMENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooqgen.catmarepository.Keys.IDENTITY_SOURCEDOCUMENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooqgen.catmarepository.Keys.KEY_SOURCEDOCUMENT_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooqgen.catmarepository.Keys.KEY_SOURCEDOCUMENT_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooqgen.catmarepository.tables.Sourcedocument as(java.lang.String alias) {
		return new de.catma.repository.db.jooqgen.catmarepository.tables.Sourcedocument(alias);
	}
}
