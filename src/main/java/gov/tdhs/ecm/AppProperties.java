package gov.tdhs.ecm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("box")
public class AppProperties {

	private String clientID;
	private String clientSecret;
	private String publicKeyID;
	private String privateKey;
	private String passphrase;
	private String enterpriseID;
	private String parentFolderID;

	private String downloadOneUserID;
	private String downloadTwoUserID;
	private String downloadThreeUserID;

	private String developerToken;

	public String getDeveloperToken() {
		return developerToken;
	}

	public void setDeveloperToken(String developerToken) {
		this.developerToken = developerToken;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getPublicKeyID() {
		return publicKeyID;
	}

	public void setPublicKeyID(String publicKeyID) {
		this.publicKeyID = publicKeyID;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getEnterpriseID() {
		return enterpriseID;
	}

	public void setEnterpriseID(String enterpriseID) {
		this.enterpriseID = enterpriseID;
	}

	public String getParentFolderID() {
		return parentFolderID;
	}

	public void setParentFolderID(String parentFolderID) {
		this.parentFolderID = parentFolderID;
	}

	public String getDownloadOneUserID() {
		return downloadOneUserID;
	}

	public void setDownloadOneUserID(String downloadOneUserID) {
		this.downloadOneUserID = downloadOneUserID;
	}

	public String getDownloadTwoUserID() {
		return downloadTwoUserID;
	}

	public void setDownloadTwoUserID(String downloadTwoUserID) {
		this.downloadTwoUserID = downloadTwoUserID;
	}

	public String getDownloadThreeUserID() {
		return downloadThreeUserID;
	}

	public void setDownloadThreeUserID(String downloadThreeUserID) {
		this.downloadThreeUserID = downloadThreeUserID;
	}

}
