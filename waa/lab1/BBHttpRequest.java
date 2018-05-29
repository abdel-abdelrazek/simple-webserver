package edu.mum.waa;

import java.util.ArrayList;

public class BBHttpRequest {
	private String method;
	private String uri;
	private String httpVersion;
	private ArrayList<String> fields;
	private ArrayList<String> message;
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getHttpVersion() {
		return httpVersion;
	}
	public void setHttpVersion(String version) {
		this.httpVersion = version;
	}
	public ArrayList<String> getFields() {
		return fields;
	}
	public void setFields(ArrayList<String> fields) {
		this.fields = fields;
	}
	public ArrayList<String> getMessage() {
		return message;
	}
	public void setMessage(ArrayList<String> messge) {
		this.message = messge;
	}
	public String getStartLine(){
		return method+" "+uri+" "+httpVersion+"\r\n";
	}
}
