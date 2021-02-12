package in.novopay.portfolio.transfer;

import java.math.BigDecimal;

public class C40ExtractData {

	public Long rbiClassification;
	public Long productCode;
	public Long profitCenter;
	public Long costCenter;
	public Long glId;
	public BigDecimal balance;
	
	public C40ExtractData(Long rbiClassification, Long productCode, Long glId, BigDecimal balance, Long profitCenter, Long costCenter) {
		super();
		this.rbiClassification = rbiClassification;
		this.productCode = productCode;
		this.glId = glId;
		this.balance = balance;
		this.profitCenter = profitCenter;
		this.costCenter = costCenter;
	}

	public Long getRbiClassification() {
		return rbiClassification;
	}

	public Long getProductCode() {
		return productCode;
	}

	public Long getGlId() {
		return glId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public Long getProfitCenter() {
		return profitCenter;
	}

	public Long getCostCenter() {
		return costCenter;
	}
	
}
