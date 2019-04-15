package com.jule.domino.auth.model;
/**
 * 支付详情
 * 
 * @author ran
 */
public class PurchaseData {
	//app包名
	private String packageName;
	//产品ID
	private String productId;
	//时间
	private long purchaseTime;
	//状态  0 成功 1失败
	private int purchaseState;
	//订单ID
	private String developerPayload;

	private String purchaseToken;
	
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public long getPurchaseTime() {
		return purchaseTime;
	}
	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}
	public int getPurchaseState() {
		return purchaseState;
	}
	public void setPurchaseState(int purchaseState) {
		this.purchaseState = purchaseState;
	}
	public String getDeveloperPayload() {
		return developerPayload;
	}
	public void setDeveloperPayload(String developerPayload) {
		this.developerPayload = developerPayload;
	}
	public String getPurchaseToken() {
		return purchaseToken;
	}
	public void setPurchaseToken(String purchaseToken) {
		this.purchaseToken = purchaseToken;
	}
}
