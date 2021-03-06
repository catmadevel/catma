/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooqgen.catmaindex.routines;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Gettagdefinitionpath extends org.jooq.impl.AbstractRoutine<java.lang.String> {

	private static final long serialVersionUID = 226065957;

	/**
	 * The parameter <code>CatmaIndex.getTagDefinitionPath.RETURN_VALUE</code>. 
	 */
	public static final org.jooq.Parameter<java.lang.String> RETURN_VALUE = createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.VARCHAR.length(2048));

	/**
	 * The parameter <code>CatmaIndex.getTagDefinitionPath.curTagDefinitionID</code>. 
	 */
	public static final org.jooq.Parameter<java.lang.Integer> CURTAGDEFINITIONID = createParameter("curTagDefinitionID", org.jooq.impl.SQLDataType.INTEGER);

	/**
	 * Create a new routine call instance
	 */
	public Gettagdefinitionpath() {
		super("getTagDefinitionPath", de.catma.repository.db.jooqgen.catmaindex.Catmaindex.CATMAINDEX, org.jooq.impl.SQLDataType.VARCHAR.length(2048));

		setReturnParameter(RETURN_VALUE);
		addInParameter(CURTAGDEFINITIONID);
	}

	/**
	 * Set the <code>curTagDefinitionID</code> parameter IN value to the routine
	 */
	public void setCurtagdefinitionid(java.lang.Integer value) {
		setValue(de.catma.repository.db.jooqgen.catmaindex.routines.Gettagdefinitionpath.CURTAGDEFINITIONID, value);
	}

	/**
	 * Set the <code>curTagDefinitionID</code> parameter to the function to be used with a {@link org.jooq.Select} statement
	 */
	public void setCurtagdefinitionid(org.jooq.Field<java.lang.Integer> field) {
		setField(CURTAGDEFINITIONID, field);
	}
}
