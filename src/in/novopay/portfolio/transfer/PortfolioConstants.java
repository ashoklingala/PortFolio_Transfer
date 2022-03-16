package in.novopay.portfolio.transfer;

/**
 * @author Ashok Lingala
 *
 */
public class PortfolioConstants {

	public static final String ROOTDIRECTORY = "PortFolio_Transfer";
	
	public static final int INTERMEDIATE_ACCOUNT_GLCODE = 80944;
	

	public static final int GL15_LOANSANDADVANCES_GLCODE=44171;
	public static final int GL17_INTERESTUNREALIZED_GLCODE=48201;
	
	public static final int GL19_INTERESTINCOMEONADVANCES_GLCODE=61101;
	public static final int GL16_INTERESTACCRUEDBUTNOTDUEONADVANCES_GLCODE=48103;
	public static final int GL_20_INTERESTINCOMEACCRUEDONADVANCES_GLCODE=61102;
	
	public static final int GL6_EXCESSFUNDSRECEIVEDFROMBORROWERS_GLCODE=24209;
	public static final int GL99_OVERPAYMENTLIABILITY_GLCODE=24209;
	
	public static final int GL47_INTERESTSUSPENSEFORNPA_GLCODE=25909;
	public static final int GL107_INTERESTSUSPENSEACCRUEDBUTNOTDUENPA_GLCODE=25906;
	
	public static final int INTER_ENTITY=99999;
	public static final String SOURCE_CODE="004";
	public static final int SPARE_1=999;
	public static final int SPARE_2=9999;
	public static final String CREDIT = "CREDIT";
	public static final String DEBIT = "DEBIT";
	

	public static final String INSERT_QUERY = " INSERT INTO ";

	public static final String LOAN_IMPORT_VALUES = " (`office_id`, `loan_id`, `closedon_date`, `account_no`, `loan_status`, `branch_code`, `profit_center`, `cost_center`, `natural_account`, `product_code`, `rbi_classification`, `inter_entity`, `source_code`, `spare1`, `spare2` ) VALUES(";
	public static final String CLOSEING_QUERY = " ); ";
	public static final String APPENDING_QUERY = " ), (";
	public static final String GL_IMPORT_VALUES = " (`loan_id`, `gl_id`, `office_id`, `balance`) VALUES(?, ?, ?, ?);";

	public static final String BLANK_SPACES = " ";

	public static String getLoanTableName(Long officeId, String tableNameSuffix) {
		String tableName = "loan_accounts_officeid_" + officeId + "_transfer_" + tableNameSuffix;
		return tableName;
	}
	
	public static String getGlTableName(Long officeId, String tableNameSuffix) {
		String tableName = "gl_accounts_officeid_" + officeId + "_transfer_" + tableNameSuffix;
		return tableName;
	}
	
	
	public static String getLoanImportTableScript(Long officeId, String tableNameSuffix) {
		
		String lineSeparator = System.lineSeparator();
		String commaSepartor = ", ";
		
		String tableName = getLoanTableName(officeId, tableNameSuffix);
		
		StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append("(").append(lineSeparator);
		builder.append(" id BIGINT(20) NOT NULL AUTO_INCREMENT").append(commaSepartor).append(lineSeparator)
		.append("office_id BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("loan_id BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		
		.append("closedon_date DATE DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("account_no VARCHAR(200) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("loan_status BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("branch_code BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("profit_center BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("cost_center BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("natural_account BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("product_code BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("rbi_classification BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("inter_entity BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		
		.append("source_code BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("spare1 BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		.append("spare2 BIGINT(20) DEFAULT NULL").append(commaSepartor).append(lineSeparator)
		
		.append(" PRIMARY KEY (id) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" UNIQUE INDEX loan_id (loan_id) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX rbi_classification (rbi_classification) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX product_code (product_code) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX office_id (office_id) USING BTREE); ");
		
		System.out.println("Loan Import table schema : " + builder.toString());
		
		return builder.toString();
	}
	
	public static String getC40FetchQuery(String loanTableName, String glTableName, Long officeId) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT loanTable.rbi_classification, loanTable.product_code, loanTable.profit_center, loanTable.cost_center, SUM(gl.balance) as balance, gl.gl_id FROM ")
		.append(loanTableName).append(" loanTable JOIN ").append(glTableName).append(" gl ON gl.loan_id = loanTable.loan_id and gl.office_id= loanTable.office_id ")
		.append( " where gl.office_id=").append(officeId)
		.append( " GROUP BY loanTable.rbi_classification, loanTable.product_code, loanTable.profit_center, loanTable.cost_center, loanTable.office_id, gl.gl_id ORDER BY gl.gl_id ");
		System.out.println(builder.toString());
		return builder.toString();
		
	}
	
	public static String getGLImportTableScript(Long officeId, String tableNameSuffix) {
		
		String lineSeparator = System.lineSeparator();
		String commaSepartor = ", ";
		
		String tableName = getGlTableName(officeId, tableNameSuffix);
		
		StringBuilder builder = new StringBuilder("CREATE TABLE ").append(tableName).append("(").append(lineSeparator);
		
		builder.append(" id BIGINT(20) NOT NULL AUTO_INCREMENT").append(commaSepartor).append(lineSeparator)
		.append("loan_id BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("gl_id BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("office_id BIGINT(20) NOT NULL").append(commaSepartor).append(lineSeparator)
		.append("balance DECIMAL(19,6) NOT NULL").append(commaSepartor).append(lineSeparator)

		.append(" PRIMARY KEY (id) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX loan_id (loan_id) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX gl_id (gl_id) USING BTREE ").append(commaSepartor).append(lineSeparator)
		.append(" INDEX office_id (office_id) USING BTREE); ");
		
		System.out.println("GL Import table schema : " + builder.toString());
		
		return builder.toString();
	}

}
