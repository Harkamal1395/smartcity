package org.egov.stms.elasticSearch.entity;

import java.util.Map;

import org.egov.search.domain.Document;

public class SewerageSearchResult {
	private Document document;
	private Map<String, String> actions;
	
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public Map<String, String> getActions() {
		return actions;
	}
	public void setActions(Map<String, String> actions) {
		this.actions = actions;
	}
	
	
	
	
	
}
