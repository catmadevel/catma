/**
 * This class is generated by jOOQ
 */
package de.catma.repository.db.jooqgen.catmarepository;

/**
 * This class is generated by jOOQ.
 *
 * Convenience access to all stored procedures and functions in CatmaRepository
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.1.0" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Routines {

	/**
	 * Call <code>CatmaRepository.getTagDefinitionChildren</code>
	 */
	public static void gettagdefinitionchildren(org.jooq.Configuration configuration, java.lang.Integer starttagdefinitionid) {
		de.catma.repository.db.jooqgen.catmarepository.routines.Gettagdefinitionchildren p = new de.catma.repository.db.jooqgen.catmarepository.routines.Gettagdefinitionchildren();
		p.setStarttagdefinitionid(starttagdefinitionid);

		p.execute(configuration);
	}
}
