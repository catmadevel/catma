package de.catma.indexer;

import de.catma.indexer.indexbuffer.IndexBufferManager;

public enum IndexBufferManagerName {
	INDEXBUFFERMANAGER,
	;
	
	private volatile IndexBufferManager indeBufferManager;

	public IndexBufferManager getIndeBufferManager() {
		return indeBufferManager;
	}

	public void setIndeBufferManager(IndexBufferManager indeBufferManager) {
		this.indeBufferManager = indeBufferManager;
	}
	
	
}
