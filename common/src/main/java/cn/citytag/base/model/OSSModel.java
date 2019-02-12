package cn.citytag.base.model;

import cn.citytag.base.app.BaseModel;

/**
 * Created by yangfeng01 on 2017/12/21.
 */

public class OSSModel extends BaseModel {

	//data{}	AccessKeyId	Y	String	访问密钥标识
	//AccessKeySecret	Y	String	访问密钥
	//SecurityToken	Y	String	安全令牌
	//Expiration	Y	String	失效时间

	private String AccessKeyId;	// 访问密钥标识
	private String AccessKeySecret;	// 访问密钥
	private String SecurityToken;	// 安全令牌
	private String Expiration;	// 失效时间

	public String getAccessKeyId() {
		return AccessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		AccessKeyId = accessKeyId;
	}

	public String getAccessKeySecret() {
		return AccessKeySecret;
	}

	public void setAccessKeySecret(String accessKeySecret) {
		AccessKeySecret = accessKeySecret;
	}

	public String getSecurityToken() {
		return SecurityToken;
	}

	public void setSecurityToken(String securityToken) {
		SecurityToken = securityToken;
	}

	public String getExpiration() {
		return Expiration;
	}

	public void setExpiration(String expiration) {
		Expiration = expiration;
	}
}
