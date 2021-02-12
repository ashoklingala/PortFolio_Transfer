package in.novopay.portfolio.transfer;

import java.math.BigDecimal;

public class GLExcelImportData {
	
	private Long loanId;
	private Long glId;
	private Long officeId;
	private BigDecimal balance;
	

	public GLExcelImportData(Long loanId, Long glId, Long officeId, BigDecimal balance) {
		// TODO Auto-generated constructor stub
		this.loanId = loanId;
		this.glId = glId;
		this.officeId = officeId;
		this.balance = balance;
	}

	public static GLExcelImportData instance(Long loanId, Long glId, Long officeId, BigDecimal balance) {
		return new GLExcelImportData(loanId, glId, officeId, balance);
	}

	public Long getLoanId() {
		return loanId;
	}

	public Long getGlId() {
		return glId;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public BigDecimal getBalance() {
		return balance;
	}
	
}
