package org.activiti.designer.eclipse.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

	private static CredentialsProvider provider;
	private static SSLConnectionSocketFactory sslsf;
	private static String ftdProxyRsPrefix = "https://165.227.16.142.nip.io:8443/ftdproxyrs/";
	private static String modelsUrl = ftdProxyRsPrefix + "models";
	private static String modelUrl = modelsUrl + "/%s";
	private static String modelXmlSourceUrl = modelsUrl + "/%s/source.xml";
	private static String modelDeploymentUrl = ftdProxyRsPrefix + "deployments/models/%s";
	private static String user = "rest";
	private static String password = "test";
	
	static {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, createTrustAllStrategy());
			sslsf = new SSLConnectionSocketFactory(builder.build());
			provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY, 
					new UsernamePasswordCredentials(user, password));

		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, String> getForms() {
		return getCollection(ftdProxyRsPrefix + "forms");
	}

	public static Map<String, String> getUsers() {
		return getCollection(ftdProxyRsPrefix + "users/data.designer");
	}
	
	public static Map<String, String> getGroups() {
		return getCollection(ftdProxyRsPrefix + "groups/data.designer");
	}
	
	// returns a hash map with pairs where key is modelId and value is modelName
	// for example
	//	[
	//     {
	//         "name": "my-test-model2",
	//         "id": "3b792c3b-c23a-11ea-82a3-ae1447660022",
	//         "lastUpdateTime": "2020-07-09T16:16:34.991-07:00"
	//     },
	//     {
	//         "name": "my-test-model2",
	//         "id": "47d00c1c-c23a-11ea-82a3-ae1447660022",
	//         "lastUpdateTime": "2020-07-09T16:16:55.693-07:00"
	//     },
	//     {
	//         "name": "test-model4",
	//         "id": "56d87ef9-c320-11ea-838e-ae1447660022",
	//         "lastUpdateTime": "2020-07-10T20:08:23.625-07:00"
	//     }
	//    ]
	public static List<Map<String, String>> getModels() {
		return getCollectionsList(modelsUrl);
	}

	// returns model source in form of xml string by modelId
	public static String getModelSource(String modelId) {
		byte[] modelSource = getBytes(String.format(modelXmlSourceUrl, modelId));
		return modelSource != null? new String(modelSource) : ""; 
	}

	// stores a new model with given name and model source
	// returns modelId of a newly stored model
	public static String saveNewModel(String name, String modelXmlSource) {
		String modelId = createNewModel(name);
		return updateModelSource(modelId, modelXmlSource)? modelId : null;
	}
	
	// updates model source for a given modelId
	public static boolean updateModelSource(String modelId, String modelXmlSource) {
		return putFile(String.format(modelXmlSourceUrl, modelId), modelXmlSource.getBytes());
	}
	
	// creates a model with given name (this is an internal method) 
	private static String createNewModel(String name) {
		MapData mapData = new MapData();
		Map<String, String> map = new HashMap<>();
		mapData.setMap(map);
		map.put("name", name);
		MapData result = post(modelsUrl, mapData, MapData.class);
		return result != null && result.getMap().size() != 0? 
				result.getMap().get("id") : "";
	}
	
	// deletes model by modelId
	public static boolean deleteModel(String modelId) {
		return delete(String.format(modelUrl, modelId));
	}

	// deploys model with deployment name
	public static void deployModel(String modelId, String deploymentName) throws Exception {
		HashMap<String, String> map = new HashMap();
		map.put("name", deploymentName);
		post(String.format(modelDeploymentUrl, modelId), map);
	}
	
	private static Map<String, String> getCollection(String url) {
		MapData mapData = get(url, MapData.class);
		return mapData.getMap();
	}
	
	private static List<Map<String, String>> getCollectionsList(String url) {
		MapListData mapListData = get(url, MapListData.class);
		return mapListData.getMapList();
	}
	
	
	private static boolean delete(String url) {
		boolean result = false;
		HttpDelete request = new HttpDelete(url);
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("couldn't invoke url ");
			}
			result = true;
		} catch (Exception ex) {
		}

		return result;
	}
	
	private static boolean putFile(String url, byte[] data) {
		boolean result = false;
		HttpPut request = new HttpPut(url);
		HttpEntity entity = MultipartEntityBuilder
			    .create()
			    .addBinaryBody("upload_file", data, 
			    		ContentType.create("application/octet-stream"), "filename")
			    .build();
		
		request.setEntity(entity);
		
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				System.out.println("error occured: " 
								+ IOUtils.toString(response.getEntity().getContent(), "UTF-8");
				throw new Exception("couldn't invoke url ");
			}
			result = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return result;
	}
	
	private static <T> T post(String url, Object data, Class<T> cls) {
		HttpPost request = new HttpPost(url);
		request.addHeader("Accept", "application/json");
		request.addHeader("Content-Type", "application/json");
		
		try {
			request.setEntity(
					new StringEntity(
							new ObjectMapper().writeValueAsString(data)));
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
		
		T result;
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("couldn't invoke url ");
			}
			result = new ObjectMapper().readValue(response.getEntity().getContent(), cls);
			
		} catch (Exception ex) {
			result = null;
		}
		
		return result;
	}
	
	private static void post(String url, Object data) throws Exception {
		HttpPost request = new HttpPost(url);
		request.addHeader("Content-Type", "application/json");
		
		try {
			request.setEntity(
					new StringEntity(
							new ObjectMapper().writeValueAsString(data)));
		} catch (UnsupportedEncodingException | JsonProcessingException e) {
			e.printStackTrace();
			throw new Exception("couldn't json serialize object");
		}
		
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("error status code returned " + response.getStatusLine().getStatusCode());
			}
			
		} catch (Exception ex) {
			throw new Exception("couldn't invoke url ");
		}
	}
	
	private static <T> T get(String url, Class<T> cls) {
		HttpGet request = new HttpGet(url);
		request.addHeader("Accept", "application/json");
		request.addHeader("Content-Type", "application/json");
		T result;
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("couldn't invoke url ");
			}
			result = new ObjectMapper().readValue(response.getEntity().getContent(), cls);
			
		} catch (Exception ex) {
			result = null;
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static byte[] getBytes(String url) {
		HttpGet request = new HttpGet(url);
		request.addHeader("Accept", "application/octet-stream");
		byte[] result;
		try (CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.setHostnameVerifier(createHostnameVerifier())
				.setDefaultCredentialsProvider(provider)
				.build();
				CloseableHttpResponse response = httpClient.execute(request)) {
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new Exception("couldn't invoke url ");
			}
			result = IOUtils.toByteArray(response.getEntity().getContent());
			
		} catch (Exception ex) {
			result = null;
		}
		
		return result;
	}
	

	private static TrustStrategy createTrustAllStrategy() {
		return new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		};
	}

	private static X509HostnameVerifier createHostnameVerifier() {
		return new X509HostnameVerifier() {
			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {
			}

			@Override
			public void verify(String host, X509Certificate cert) throws SSLException {
			}

			@Override
			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}

			@Override
			public boolean verify(String s, SSLSession sslSession) {
				return true;
			}
		};
	}

}
