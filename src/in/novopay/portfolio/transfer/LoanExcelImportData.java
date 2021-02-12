package in.novopay.portfolio.transfer;

import java.util.Date;

/**
 * @author Ashok Lingala
 *
 */
public class LoanExcelImportData {

	public Long officeId;
	public Long loanId;
	public Date closedDate;
	public String accountNo;
	
	public Long loanStatus;
	public Long branchCode;
	
	public Long profitCenter;
	public Long costCenter;
	
	public Long naturalAccount;
	public Long productCode;
	
	public Long rbiClassification;
	public Long interEntity;
	public Long sourceCode;
	public Long spare1;
	public Long spare2;
	
	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}
	public void setLoanId(Long loanId) {
		this.loanId = loanId;
	}
	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}
	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}
	public void setLoanStatus(Long loanStatus) {
		this.loanStatus = loanStatus;
	}
	public void setBranchCode(Long branchCode) {
		this.branchCode = branchCode;
	}
	public void setProfitCenter(Long profitCenter) {
		this.profitCenter = profitCenter;
	}
	public void setCostCenter(Long costCenter) {
		this.costCenter = costCenter;
	}
	public void setNaturalAccount(Long naturalAccount) {
		this.naturalAccount = naturalAccount;
	}
	public void setProductCode(Long productCode) {
		this.productCode = productCode;
	}
	public void setRbiClassification(Long rbiClassification) {
		this.rbiClassification = rbiClassification;
	}
	public void setInterEntity(Long interEntity) {
		this.interEntity = interEntity;
	}
	public void setSourceCode(Long sourceCode) {
		this.sourceCode = sourceCode;
	}
	public void setSpare1(Long spare1) {
		this.spare1 = spare1;
	}
	public void setSpare2(Long spare2) {
		this.spare2 = spare2;
	}
	public LoanExcelImportData(Long officeId, Long loanId, Date closedDate, String accountNo, Long loanStatus,
			Long branchCode, Long profitCenter, Long costCenter, Long naturalAccount, Long productCode,
			Long rbiClassification, Long interEntity, Long sourceCode, Long spare1, Long spare2) {
		this.officeId = officeId;
		this.loanId = loanId;
		this.closedDate = closedDate;
		this.accountNo = accountNo;
		this.loanStatus = loanStatus;
		this.branchCode = branchCode;
		this.profitCenter = profitCenter;
		this.costCenter = costCenter;
		this.naturalAccount = naturalAccount;
		this.productCode = productCode;
		this.rbiClassification = rbiClassification;
		this.interEntity = interEntity;
		this.sourceCode = sourceCode;
		this.spare1 = spare1;
		this.spare2 = spare2;
	}
	
	public static LoanExcelImportData instance(Long officeId, Long loanId, Date closedDate, String accountNo, Long loanStatus,
			Long branchCode, Long profitCenter, Long costCenter, Long naturalAccount, Long productCode,
			Long rbiClassification, Long interEntity, Long sourceCode, Long spare1, Long spare2) {
		
		Date closedOnDate = null; 
		
		return new LoanExcelImportData(officeId, loanId, closedOnDate, accountNo, loanStatus, branchCode, 
				profitCenter, costCenter, naturalAccount, productCode, rbiClassification, interEntity, 
				sourceCode, spare1, spare2);
	}
	public Long getOfficeId() {
		return officeId;
	}
	public Long getLoanId() {
		return loanId;
	}
	public Date getClosedDate() {
		return closedDate;
	}
	public String getAccountNo() {
		return accountNo;
	}
	public Long getLoanStatus() {
		return loanStatus;
	}
	public Long getBranchCode() {
		return branchCode;
	}
	public Long getProfitCenter() {
		return profitCenter;
	}
	public Long getCostCenter() {
		return costCenter;
	}
	public Long getNaturalAccount() {
		return naturalAccount;
	}
	public Long getProductCode() {
		return productCode;
	}
	public Long getRbiClassification() {
		return rbiClassification;
	}
	public Long getInterEntity() {
		return interEntity;
	}
	public Long getSourceCode() {
		return sourceCode;
	}
	public Long getSpare1() {
		return spare1;
	}
	public Long getSpare2() {
		return spare2;
	}
	
	
}
