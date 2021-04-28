package in.novopay.portfolio.transfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Ashok
 *
 */
@Component("generateC40Service")
public class GenerateC40ScriptService {

	@Value("${foo.location}")
	private String location;
	
	@Value("${foo.loanaccounts.filename}")
	private String filename;
	
	@Value("${foo.source.officeid}")
	private String sourceOfficeId;
	
	@Value("${foo.destination.officeid}")
	private String destinationOfficeId;
	
	@Value("${foo.output.filename}")
	private String outputFileName;
	
	@Value("${foo.source.branchcode}")
	private String sourceBranchCode;
	
	@Value("${foo.destination.branchcode}")
	private String destinationBranchCode;
	
	@Value("${foo.tablenames.suffix}")
	private String tableNameSuffix;
	
	public JdbcTemplate jdbcTemplate;
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public List<C40ExtractData> getC40Data(Long officeId, String tableNameSuffixLocal) {
		
		String loanTableName = PortfolioConstants.getLoanTableName(officeId, tableNameSuffixLocal);
		String glTableName = PortfolioConstants.getGlTableName(officeId, tableNameSuffixLocal);
		
		String sqlQuery = PortfolioConstants.getC40FetchQuery(loanTableName, glTableName, officeId);
		final C40Mapper rm = new C40Mapper();
		return this.jdbcTemplate.query(sqlQuery, rm, new Object[] {});
	}
	
	private static final class C40Mapper implements RowMapper<C40ExtractData> {

		
        public C40ExtractData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        	final Long rbiClassification = (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("rbi_classification"), Long.class);
        	final Long productCode = (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("product_code"), Long.class);
        	final Long glId = (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("gl_id"), Long.class);
        	final BigDecimal balance = rs.getBigDecimal("balance");
        	final Long profitCenter = (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("profit_center"), Long.class);
        	final Long costCenter = (Long) JdbcUtils.getResultSetValue(rs, rs.findColumn("cost_center"), Long.class);
        	
        	
            return new C40ExtractData(rbiClassification, productCode, glId, balance, profitCenter, costCenter);
        }
    }
	
	private String getOutputFileLocation() {
		File file = new File(location);
		if(file.exists()) {
			StringBuilder builder = new StringBuilder(location);
			builder.append(File.separator).append(outputFileName);
			return builder.toString();
		}
		System.out.println("File Location not exist.");
		return null;
	}
	
	@Transactional
	public void processC40Scripts() {
		
		List<C40ExtractData> c40Datas = getC40Data(Long.valueOf(sourceOfficeId), tableNameSuffix);
		
		List<C40ExtractData> filterC40DataWithNonZeroBalance = new ArrayList<C40ExtractData>();
		for (C40ExtractData c40extract : c40Datas) {
			if(c40extract.getBalance().compareTo(BigDecimal.ZERO) != 0) {
				System.out.println(c40extract.toString());
				filterC40DataWithNonZeroBalance.add(c40extract);
			}
		}
		writeToXlsxFile(filterC40DataWithNonZeroBalance, getOutputFileLocation());
	}
	
	
	 private void writeToXlsxFile(List<C40ExtractData> extractDatas, String fileName) {

	        try {
	     
	        	FileOutputStream fileStream = new FileOutputStream(fileName);
	        	XSSFWorkbook workbook = new XSSFWorkbook();
	            XSSFSheet sheet = workbook.createSheet("c_40_generator");

	            // Create a header row describing what the columns mean
	            CellStyle boldStyle = workbook.createCellStyle();
	            XSSFFont font = workbook.createFont();
	            font.setBold(true);
	            boldStyle.setFont(font);
	            
	            CellStyle dataCellStyle = workbook.createCellStyle();
                DataFormat format = workbook.createDataFormat();
                dataCellStyle.setDataFormat(format.getFormat("0.0"));

                List<String> val = new ArrayList<String>();
                val.add("GL_Id");
	            XSSFRow headerRow = sheet.createRow(0);
	            addStringCells(headerRow,
//	                    List.of("GL_Id", "BranchCode", "ProfitCenter", "CostCentre",	"NaturalAccount", "ProductCode", 
//	                    		"RBIClassification", "InterEntity",	"SourceCode", "Spare1",	"Spare2", "Type", "Balance", 
//	                    		" ", "C-40 script"),
	            		val,
	                    boldStyle);

	            // Add the person data as rows
	           int temp=0;
	            for (int i = 0; i < extractDatas.size(); i++) {
	                // Add one due to the header row
	            	temp = temp+1;
	                XSSFRow row1 = sheet.createRow(temp);
	                temp = temp+1;
	                XSSFRow row2 = sheet.createRow(temp);
	                
	                temp = temp+1;
	                XSSFRow row3 = sheet.createRow(temp);
	                
	                temp = temp+1;
	                XSSFRow row4 = sheet.createRow(temp);
	                
	                C40ExtractData extractData = extractDatas.get(i);
	                addCells(extractData, row1, row2, row3, row4, dataCellStyle);
	                
	                temp = temp+1;
	            }

	            workbook.write(fileStream);
	            
	        } catch (IOException e) {
	            System.err.println("Could not create XLSX file at " + fileName);
	            e.printStackTrace();
	        }
	    }

	// Adds strings as styled cells to a row
	    private void addStringCells(Row row, List<String> strings,
	                                      CellStyle style) {
	        for (int i = 0; i < strings.size(); i++) {
	            Cell cell = row.createCell(i, CellType.STRING);
	            cell.setCellValue(strings.get(i));
	            cell.setCellStyle(style);
	        }
	    }
	    
	    
	    private void addCells(C40ExtractData extractData, Row sourceDebit,
				Row sourceCredit, Row destinationDebit, Row destinationCredit, CellStyle dataCellStyle) {

	    	
	    	// 0 GLId
	    	Cell  sourceDebitGLIdCell = sourceDebit.createCell(0, CellType.NUMERIC);
	    	sourceDebitGLIdCell.setCellValue(extractData.getGlId());
	    	
	    	Cell sourceCreditGLIdCell = sourceCredit.createCell(0, CellType.NUMERIC);
	    	sourceCreditGLIdCell.setCellValue(extractData.getGlId());
	    	
	    	Cell  destiDebitGLIdCell = destinationDebit.createCell(0, CellType.NUMERIC);
	    	destiDebitGLIdCell.setCellValue(extractData.getGlId());
	    	
	    	Cell  destiCreditGLIdCell = destinationCredit.createCell(0, CellType.NUMERIC);
	    	destiCreditGLIdCell.setCellValue(extractData.getGlId());
			
	    	// 1 Branch Code
			Cell  sourceDebitBranchCodeCell = sourceDebit.createCell(1, CellType.NUMERIC);
			sourceDebitBranchCodeCell.setCellValue(sourceBranchCode);
			
			Cell  sourceCreditBranchCodeCell = sourceCredit.createCell(1, CellType.NUMERIC);
			sourceCreditBranchCodeCell.setCellValue(sourceBranchCode);
			
			Cell  destiDebitBranchCodeCell = destinationDebit.createCell(1, CellType.NUMERIC);
			destiDebitBranchCodeCell.setCellValue(destinationBranchCode);
			
			Cell  destiCreditBranchCodeCell = destinationCredit.createCell(1, CellType.NUMERIC);
			destiCreditBranchCodeCell.setCellValue(destinationBranchCode);
			
			// 2 Profit Center
			Cell  sourceDebitProfitCenterCodeCell = sourceDebit.createCell(2, CellType.NUMERIC);
			sourceDebitProfitCenterCodeCell.setCellValue(extractData.getProfitCenter());
			
			Cell  sourceCreditProfitCenterCodeCell = sourceCredit.createCell(2, CellType.NUMERIC);
			sourceCreditProfitCenterCodeCell.setCellValue(extractData.getProfitCenter());
			
			Cell  destiDebitProfitCenterCodeCell = destinationDebit.createCell(2, CellType.NUMERIC);
			destiDebitProfitCenterCodeCell.setCellValue(extractData.getProfitCenter());
			
			Cell  destiCreditProfitCenterCodeCell = destinationCredit.createCell(2, CellType.NUMERIC);
			destiCreditProfitCenterCodeCell.setCellValue(extractData.getProfitCenter());
			
			
			//3 cost center
			
			Cell  sourceDebitCostCenterCodeCell = sourceDebit.createCell(3, CellType.NUMERIC);
			sourceDebitCostCenterCodeCell.setCellValue(extractData.getCostCenter());
			
			Cell  sourceCreditCostCenterCodeCell = sourceCredit.createCell(3, CellType.NUMERIC);
			sourceCreditCostCenterCodeCell.setCellValue(extractData.getCostCenter());
			
			Cell  destiDebitCostCenterCodeCell = destinationDebit.createCell(3, CellType.NUMERIC);
			destiDebitCostCenterCodeCell.setCellValue(extractData.getCostCenter());
			
			Cell  destiCreditCostCenterCodeCell = destinationCredit.createCell(3, CellType.NUMERIC);
			destiCreditCostCenterCodeCell.setCellValue(extractData.getCostCenter());
			
			
			//4 Natural Account +ve
			
			int sourceDebitNaturalAccount = getGLNaturalAccount(extractData.getGlId().intValue()); 
			int  sourceCreditNaturalAccount = PortfolioConstants.INTERMEDIATE_ACCOUNT_GLCODE; 
			int  destinationDebitNaturalAccount = PortfolioConstants.INTERMEDIATE_ACCOUNT_GLCODE;
			int  destinationCreditNaturalAccount = getGLNaturalAccount(extractData.getGlId().intValue());
			
			
			if( extractData.getBalance().compareTo(BigDecimal.ZERO) < 0 ) {
				sourceDebitNaturalAccount = PortfolioConstants.INTERMEDIATE_ACCOUNT_GLCODE;
				sourceCreditNaturalAccount = getGLNaturalAccount(extractData.getGlId().intValue());
			 	destinationDebitNaturalAccount = getGLNaturalAccount(extractData.getGlId().intValue());
			 	destinationCreditNaturalAccount = PortfolioConstants.INTERMEDIATE_ACCOUNT_GLCODE;
				
			} 
			
			Cell  sourceDebitNaturalAccountCodeCell = sourceDebit.createCell(4, CellType.NUMERIC);
			sourceDebitNaturalAccountCodeCell.setCellValue(sourceDebitNaturalAccount);
			
			Cell  sourceCreditNaturalAccountCodeCell = sourceCredit.createCell(4, CellType.NUMERIC);
			sourceCreditNaturalAccountCodeCell.setCellValue(sourceCreditNaturalAccount);
			
			Cell  destiDebitNaturalAccountCodeCell = destinationDebit.createCell(4, CellType.NUMERIC);
			destiDebitNaturalAccountCodeCell.setCellValue(destinationDebitNaturalAccount);
			
			Cell  destiCreditNaturalAccountCodeCell = destinationCredit.createCell(4, CellType.NUMERIC);
			destiCreditNaturalAccountCodeCell.setCellValue(destinationCreditNaturalAccount);
		
			//5 ProductCode
			Cell  sourceDebitProductCodeCodeCell = sourceDebit.createCell(5, CellType.NUMERIC);
			sourceDebitProductCodeCodeCell.setCellValue(extractData.getProductCode());
			
			Cell  sourceCreditProductCodeCodeCell = sourceCredit.createCell(5, CellType.NUMERIC);
			sourceCreditProductCodeCodeCell.setCellValue(extractData.getProductCode());
			
			Cell  destiDebitProductCodeCodeCell = destinationDebit.createCell(5, CellType.NUMERIC);
			destiDebitProductCodeCodeCell.setCellValue(extractData.getProductCode());
			
			Cell  destiCreditProductCodeCodeCell = destinationCredit.createCell(5, CellType.NUMERIC);
			destiCreditProductCodeCodeCell.setCellValue(extractData.getProductCode());
			
			//6 RBI Classification
			Cell  sourceDebitRbiClassificationCodeCell = sourceDebit.createCell(6, CellType.NUMERIC);
			sourceDebitRbiClassificationCodeCell.setCellValue(extractData.getRbiClassification());
			
			Cell  sourceCreditRbiClassificationCodeCell = sourceCredit.createCell(6, CellType.NUMERIC);
			sourceCreditRbiClassificationCodeCell.setCellValue(extractData.getRbiClassification());
			
			Cell  destiDebitRbiClassificationCodeCell = destinationDebit.createCell(6, CellType.NUMERIC);
			destiDebitRbiClassificationCodeCell.setCellValue(extractData.getRbiClassification());
			
			Cell  destiCreditRbiClassificationCodeCell = destinationCredit.createCell(6, CellType.NUMERIC);
			destiCreditRbiClassificationCodeCell.setCellValue(extractData.getRbiClassification());
			
			//7 InterEntity
			Cell  sourceDebitInterEntityCodeCell = sourceDebit.createCell(7, CellType.NUMERIC);
			sourceDebitInterEntityCodeCell.setCellValue(PortfolioConstants.INTER_ENTITY);
			
			Cell  sourceCreditInterEntityCodeCell = sourceCredit.createCell(7, CellType.NUMERIC);
			sourceCreditInterEntityCodeCell.setCellValue(PortfolioConstants.INTER_ENTITY);
			
			Cell  destiDebitInterEntityCodeCell = destinationDebit.createCell(7, CellType.NUMERIC);
			destiDebitInterEntityCodeCell.setCellValue(PortfolioConstants.INTER_ENTITY);
			
			Cell  destiCreditInterEntityCodeCell = destinationCredit.createCell(7, CellType.NUMERIC);
			destiCreditInterEntityCodeCell.setCellValue(PortfolioConstants.INTER_ENTITY);
			
			//8 SourceCode
			Cell  sourceDebitSourceCodeCodeCell = sourceDebit.createCell(8, CellType.NUMERIC);
			sourceDebitSourceCodeCodeCell.setCellValue(PortfolioConstants.SOURCE_CODE);
			
			Cell  sourceCreditSourceCodeCodeCell = sourceCredit.createCell(8, CellType.NUMERIC);
			sourceCreditSourceCodeCodeCell.setCellValue(PortfolioConstants.SOURCE_CODE);
			
			Cell  destiDebitSourceCodeCodeCell = destinationDebit.createCell(8, CellType.NUMERIC);
			destiDebitSourceCodeCodeCell.setCellValue(PortfolioConstants.SOURCE_CODE);
			
			Cell  destiCreditSourceCodeCodeCell = destinationCredit.createCell(8, CellType.NUMERIC);
			destiCreditSourceCodeCodeCell.setCellValue(PortfolioConstants.SOURCE_CODE);
			
			//9 spare-1
			Cell  sourceDebitSpare1CodeCell = sourceDebit.createCell(9, CellType.NUMERIC);
			sourceDebitSpare1CodeCell.setCellValue(PortfolioConstants.SPARE_1);
			
			Cell  sourceCreditSpare1CodeCell = sourceCredit.createCell(9, CellType.NUMERIC);
			sourceCreditSpare1CodeCell.setCellValue(PortfolioConstants.SPARE_1);
			
			Cell  destiDebitSpare1CodeCell = destinationDebit.createCell(9, CellType.NUMERIC);
			destiDebitSpare1CodeCell.setCellValue(PortfolioConstants.SPARE_1);
			
			Cell  destiCreditSpare1CodeCell = destinationCredit.createCell(9, CellType.NUMERIC);
			destiCreditSpare1CodeCell.setCellValue(PortfolioConstants.SPARE_1);
			
			//10 spare-2
			Cell  sourceDebitSpare2CodeCell = sourceDebit.createCell(10, CellType.NUMERIC);
			sourceDebitSpare2CodeCell.setCellValue(PortfolioConstants.SPARE_2);
			
			Cell  sourceCreditSpare2CodeCell = sourceCredit.createCell(10, CellType.NUMERIC);
			sourceCreditSpare2CodeCell.setCellValue(PortfolioConstants.SPARE_2);
			
			Cell  destiDebitSpare2CodeCell = destinationDebit.createCell(10, CellType.NUMERIC);
			destiDebitSpare2CodeCell.setCellValue(PortfolioConstants.SPARE_2);
			
			Cell  destiCreditSpare2CodeCell = destinationCredit.createCell(10, CellType.NUMERIC);
			destiCreditSpare2CodeCell.setCellValue(PortfolioConstants.SPARE_2);
			
			//11 Type
			Cell  sourceDebitTypeCodeCell = sourceDebit.createCell(11, CellType.NUMERIC);
			sourceDebitTypeCodeCell.setCellValue(PortfolioConstants.DEBIT);
			
			Cell  sourceCreditTypeCodeCell = sourceCredit.createCell(11, CellType.NUMERIC);
			sourceCreditTypeCodeCell.setCellValue(PortfolioConstants.CREDIT);
			
			Cell  destiDebitTypeCodeCell = destinationDebit.createCell(11, CellType.NUMERIC);
			destiDebitTypeCodeCell.setCellValue(PortfolioConstants.DEBIT);
			
			Cell destiCreditTypeCodeCell = destinationCredit.createCell(11, CellType.NUMERIC);
			destiCreditTypeCodeCell.setCellValue(PortfolioConstants.CREDIT);
			
			//12 Balance
			BigDecimal balance = extractData.getBalance().abs().setScale(2, RoundingMode.HALF_UP);
			
			Cell sourceDebitBalanceCodeCell = sourceDebit.createCell(12, CellType.NUMERIC);
			//sourceDebitBalanceCodeCell.setCellStyle(dataCellStyle);
			sourceDebitBalanceCodeCell.setCellValue(balance.doubleValue());
			
			Cell sourceCreditBalanceCodeCell = sourceCredit.createCell(12, CellType.NUMERIC);
			//sourceCreditBalanceCodeCell.setCellStyle(dataCellStyle);
			sourceCreditBalanceCodeCell.setCellValue(balance.doubleValue());
			
			Cell destiDebitBalanceCodeCell = destinationDebit.createCell(12, CellType.NUMERIC);
			//destiDebitBalanceCodeCell.setCellStyle(dataCellStyle);
			destiDebitBalanceCodeCell.setCellValue(balance.doubleValue());
			
			Cell destiCreditBalanceCodeCell = destinationCredit.createCell(12, CellType.NUMERIC);
			//destiCreditBalanceCodeCell.setCellStyle(dataCellStyle);
			destiCreditBalanceCodeCell.setCellValue(balance.doubleValue());
			
			
			// 13 Empty Cell
			Cell sourceDebitSpaceCell = sourceDebit.createCell(13, CellType.STRING);
			sourceDebitSpaceCell.setCellValue(PortfolioConstants.BLANK_SPACES);
			
			Cell sourceCreditSpaceCell = sourceCredit.createCell(13, CellType.STRING);
			sourceCreditSpaceCell.setCellValue(PortfolioConstants.BLANK_SPACES);
			
			Cell destiDebitSpaceCell = destinationDebit.createCell(13, CellType.STRING);
			destiDebitSpaceCell.setCellValue(PortfolioConstants.BLANK_SPACES);
			
			Cell destiCreditSpaceCell = destinationCredit.createCell(13, CellType.STRING);
			destiCreditSpaceCell.setCellValue(PortfolioConstants.BLANK_SPACES);
			
			
			// 14 C-40 script 
			
			StringBuilder sourceDebitBuilder = new StringBuilder();
			sourceDebitBuilder.append(sourceBranchCode).append(extractData.getProfitCenter())
			.append(extractData.getCostCenter()).append(sourceDebitNaturalAccount).append(extractData.getProductCode())
			.append(extractData.getRbiClassification()).append(PortfolioConstants.INTER_ENTITY)
			.append(PortfolioConstants.SOURCE_CODE).append(PortfolioConstants.SPARE_1).append(PortfolioConstants.SPARE_2)
			.append(" ").append(PortfolioConstants.DEBIT).append(" ").append(balance);
			
			StringBuilder sourceCreditBuilder = new StringBuilder();
			sourceCreditBuilder.append(sourceBranchCode).append(extractData.getProfitCenter())
			.append(extractData.getCostCenter()).append(sourceCreditNaturalAccount).append(extractData.getProductCode())
			.append(extractData.getRbiClassification()).append(PortfolioConstants.INTER_ENTITY)
			.append(PortfolioConstants.SOURCE_CODE).append(PortfolioConstants.SPARE_1).append(PortfolioConstants.SPARE_2)
			.append(" ").append(PortfolioConstants.CREDIT).append(" ").append(balance);
			
			StringBuilder destinationDebitBuilder = new StringBuilder();
			destinationDebitBuilder.append(destinationBranchCode).append(extractData.getProfitCenter())
			.append(extractData.getCostCenter()).append(destinationDebitNaturalAccount).append(extractData.getProductCode())
			.append(extractData.getRbiClassification()).append(PortfolioConstants.INTER_ENTITY)
			.append(PortfolioConstants.SOURCE_CODE).append(PortfolioConstants.SPARE_1).append(PortfolioConstants.SPARE_2)
			.append(" ").append(PortfolioConstants.DEBIT).append(" ").append(balance);
			
			StringBuilder destinationCreditBuilder = new StringBuilder();
			destinationCreditBuilder.append(destinationBranchCode).append(extractData.getProfitCenter())
			.append(extractData.getCostCenter()).append(destinationCreditNaturalAccount).append(extractData.getProductCode())
			.append(extractData.getRbiClassification()).append(PortfolioConstants.INTER_ENTITY)
			.append(PortfolioConstants.SOURCE_CODE).append(PortfolioConstants.SPARE_1).append(PortfolioConstants.SPARE_2)
			.append(" ").append(PortfolioConstants.CREDIT).append(" ").append(balance);
			
			Cell sourceDebitC40Cell = sourceDebit.createCell(14, CellType.STRING);
			sourceDebitC40Cell.setCellValue(sourceDebitBuilder.toString());
			
			Cell sourceCreditC40Cell = sourceCredit.createCell(14, CellType.STRING);
			sourceCreditC40Cell.setCellValue(sourceCreditBuilder.toString());
			
			Cell destiDebitC40Cell = destinationDebit.createCell(14, CellType.STRING);
			destiDebitC40Cell.setCellValue(destinationDebitBuilder.toString());
			
			Cell destiCreditC40ell = destinationCredit.createCell(14, CellType.STRING);
			destiCreditC40ell.setCellValue(destinationCreditBuilder.toString());
			
		}

		private int getGLNaturalAccount(Integer glId) {
			
			int naturalAccount;
			
			switch (glId) {
			case 15:
				naturalAccount =  PortfolioConstants.GL15_LOANSANDADVANCES_GLCODE;
				break;

			case 17:
				naturalAccount =  PortfolioConstants.GL17_INTERESTUNREALIZED_GLCODE;
				break;
				
			case 19:
				naturalAccount =  PortfolioConstants.GL19_INTERESTINCOMEONADVANCES_GLCODE;
				break;
				
			case 16:
				naturalAccount =  PortfolioConstants.GL16_INTERESTACCRUEDBUTNOTDUEONADVANCES_GLCODE;
				break;
				
			case 20:
				naturalAccount =  PortfolioConstants.GL_20_INTERESTINCOMEACCRUEDONADVANCES_GLCODE;
				break;
				
			case 6:
				naturalAccount =  PortfolioConstants.GL6_EXCESSFUNDSRECEIVEDFROMBORROWERS_GLCODE;
				break;
				
			case 99:
				naturalAccount =  PortfolioConstants.GL99_OVERPAYMENTLIABILITY_GLCODE;
				break;
				
			case 47:
				naturalAccount =  PortfolioConstants.GL47_INTERESTSUSPENSEFORNPA_GLCODE;
				break;
				
			case 107:
				naturalAccount =  PortfolioConstants.GL107_INTERESTSUSPENSEACCRUEDBUTNOTDUENPA_GLCODE;
				break;
				
			default:
				naturalAccount = 0;
				break;
			}
			return naturalAccount;
		}
	
	


}
