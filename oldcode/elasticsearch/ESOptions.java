package de.catma.indexer.elasticsearch;

import java.util.ResourceBundle;

public class ESOptions {
	private static final String BUNDLE_NAME = "de.catma.indexer.elasticsearch.esoptions"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private ESOptions() {
	}

	public static String getString(String key) {
		return RESOURCE_BUNDLE.getString(key);
	}
}
