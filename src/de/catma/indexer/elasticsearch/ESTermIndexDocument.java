package de.catma.indexer.elasticsearch;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class ESTermIndexDocument implements ESDocument {

	private String documentId;
	private UUID termId;
	private int frequency;
	private String term;
	
	public ESTermIndexDocument(String documentId, String term, int frequency) {
		this.documentId = documentId;
		this.frequency = frequency;
		this.term = term;
		this.termId = getIndexDocumentKey();
	}

	public String getDocumentId() {
		return documentId;
	}

	public int getFrequency() {
		return frequency;
	}

	public String getTerm() {
		return term;
	}

	public UUID getTermId() {
		return termId;
	}

	public String toJSON() throws JSONException {
		JSONObject j_root = new JSONObject();
		j_root.put("documentId", documentId);
		j_root.put("frequency", frequency);
		j_root.put("term", term);
		return j_root.toString();
	}

	public UUID getIndexDocumentKey() {
		return UUID.nameUUIDFromBytes((documentId + term).getBytes());
	}

	public static ESTermIndexDocument fromJSON(String jsonstring) throws JSONException {
		return fromJSON(new JSONObject(jsonstring));
	}

	public static ESTermIndexDocument fromJSON(JSONObject jsonObject) throws JSONException {
		String docid = jsonObject.getString("documentId");
		String term = jsonObject.getString("term");
		int freq = jsonObject.getInt("frequency");
		return new ESTermIndexDocument(docid, term, freq);
	}
}
