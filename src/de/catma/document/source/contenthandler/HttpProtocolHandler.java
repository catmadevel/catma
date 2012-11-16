package de.catma.document.source.contenthandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import de.catma.document.source.FileType;
import de.catma.document.source.SourceDocumentHandler;

public class HttpProtocolHandler implements ProtocolHandler {
	
	private byte[] byteContent;
	private String encoding;
	private String mimeType;
	
	public HttpProtocolHandler(URI sourceDocURI) throws IOException {
		handle(sourceDocURI);
	}

	private void handle(URI sourceDocURI) throws IOException {
		SourceDocumentHandler sourceDocumentHandler = new SourceDocumentHandler();
		
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpGet httpGet = new HttpGet(sourceDocURI);
			
			HttpResponseHandler responseHandler = new HttpResponseHandler();
			
			this.byteContent = httpclient.execute(httpGet, responseHandler);

			this.encoding = sourceDocumentHandler.getEncoding(
	        		responseHandler.getEncoding(), responseHandler.getContentType(), 
	        		byteContent,
	        		Charset.defaultCharset().name());
	        this.mimeType = sourceDocumentHandler.getMimeType(
	        		sourceDocURI.getPath(), 
	        		responseHandler.getContentType(), 
	        		FileType.TEXT.getMimeType());
		}
		finally {
			httpclient.getConnectionManager().shutdown();
		}
        
	}

	public byte[] getByteContent() {
		return byteContent;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getMimeType() {
		return mimeType;
	}

}