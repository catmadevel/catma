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
public class Staticmarkupattribute extends org.jooq.impl.TableImpl<org.jooq.Record> {

	private static final long serialVersionUID = -1381252005;

	/**
	 * The singleton instance of <code>CatmaRepository.staticmarkupattribute</code>
	 */
	public static final de.catma.repository.db.jooqgen.catmarepository.tables.Staticmarkupattribute STATICMARKUPATTRIBUTE = new de.catma.repository.db.jooqgen.catmarepository.tables.Staticmarkupattribute();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.jooq.Record> getRecordType() {
		return org.jooq.Record.class;
	}

	/**
	 * The column <code>CatmaRepository.staticmarkupattribute.staticMarkupAttributeID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> STATICMARKUPATTRIBUTEID = createField("staticMarkupAttributeID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The column <code>CatmaRepository.staticmarkupattribute.name</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(45), this);

	/**
	 * The column <code>CatmaRepository.staticmarkupattribute.value</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(45), this);

	/**
	 * The column <code>CatmaRepository.staticmarkupattribute.staticMarkupInstanceID</code>. 
	 */
	public final org.jooq.TableField<org.jooq.Record, java.lang.Integer> STATICMARKUPINSTANCEID = createField("staticMarkupInstanceID", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * Create a <code>CatmaRepository.staticmarkupattribute</code> table reference
	 */
	public Staticmarkupattribute() {
		super("staticmarkupattribute", de.catma.repository.db.jooqgen.catmarepository.Catmarepository.CATMAREPOSITORY);
	}

	/**
	 * Create an aliased <code>CatmaRepository.staticmarkupattribute</code> table reference
	 */
	public Staticmarkupattribute(java.lang.String alias) {
		super(alias, de.catma.repository.db.jooqgen.catmarepository.Catmarepository.CATMAREPOSITORY, de.catma.repository.db.jooqgen.catmarepository.tables.Staticmarkupattribute.STATICMARKUPATTRIBUTE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<org.jooq.Record, java.lang.Integer> getIdentity() {
		return de.catma.repository.db.jooqgen.catmarepository.Keys.IDENTITY_STATICMARKUPATTRIBUTE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<org.jooq.Record> getPrimaryKey() {
		return de.catma.repository.db.jooqgen.catmarepository.Keys.KEY_STATICMARKUPATTRIBUTE_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.jooq.Record>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.jooq.Record>>asList(de.catma.repository.db.jooqgen.catmarepository.Keys.KEY_STATICMARKUPATTRIBUTE_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.jooq.Record, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.jooq.Record, ?>>asList(de.catma.repository.db.jooqgen.catmarepository.Keys.FK_STATICA_STATICMARKUPINSTANCEID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.catma.repository.db.jooqgen.catmarepository.tables.Staticmarkupattribute as(java.lang.String alias) {
		return new de.catma.repository.db.jooqgen.catmarepository.tables.Staticmarkupattribute(alias);
	}
}
