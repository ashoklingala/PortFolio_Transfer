package in.novopay.portfolio.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Ashok Lingala
 *
 */
@Component("loanExcelImport")
public class LoanExcelImportService {

	@Value("${foo.location}")
	private String location;
	
	@Value("${foo.loanaccounts.filename}")
	private String filename;
	
	@Value("${foo.source.officeid}")
	private String sourceOfficeId;
	
	@Value("${foo.destination.officeid}")
	private String destinationOfficeId;
	
	@Value("${foo.tablenames.suffix}")
	private String tableNameSuffix;
	
	private JdbcTemplate jdbcTemplate;
	
	
	public String getFilesLocation() {
		File file = new File(location);
		if(file.exists()) {
			StringBuilder builder = new StringBuilder(location);
			builder.append(File.separator).append(filename);
			return builder.toString();
		}
		System.out.println("File Location not exist.");
		return null;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void processLoanAccountExcelData() {
		XSSFWorkbook wb;
        try {
        	File file = new File(getFilesLocation()); // creating a new file instance
			FileInputStream fis = new FileInputStream(file); 
            wb = new XSSFWorkbook(fis);
        
            XSSFSheet sheet = wb.getSheetAt(0);
           
            int rowNum = sheet.getLastRowNum();
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell;
            
            List<LoanExcelImportData> loanExcelDatas = new ArrayList<LoanExcelImportData>();
            
            for( short rowIndex = 1; rowIndex <= rowNum; rowIndex++ ) {
                
                row = sheet.getRow(rowIndex);

                short colIndex = 0;

                //
                if(row == null )
                	break;
                
                //officeId
                cell = row.getCell(colIndex++);
                Long officeId = Long.valueOf(cell.toString());
                
                //loanId
                cell = row.getCell(colIndex++);
                Long loanId = Long.valueOf(cell.toString());
                
                //closedDate
                cell = row.getCell(colIndex++);
                Date closedDate = null;
                if(cell != null && !(cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                	closedDate = cell.getDateCellValue();
                }
                
                //AccountNo
                cell = row.getCell(colIndex++);
                String accountNo = cell.toString();
                
                //loanStatus
                cell = row.getCell(colIndex++);
                Long loanStatus = Long.valueOf(cell.toString());
                
                //BranchCode
                Long branchCode = null;
                cell = row.getCell(colIndex++);
                if(cell != null) {
                	branchCode = Long.valueOf(cell.toString());
                }
                 
                //profitCenter
                cell = row.getCell(colIndex++);
                Long profitCenter = Long.valueOf(cell.toString());
                
                //costCenter
                cell = row.getCell(colIndex++);
                Long costCenter = Long.valueOf(cell.toString());
                
                //naturalAccount
                Long naturalAccount= null;
                cell = row.getCell(colIndex++);
                if(cell != null && !(cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                	String val = cell.toString();
                	if(!val.isEmpty() && !val.isEmpty() && !val.equalsIgnoreCase(" "))
                	naturalAccount = Long.valueOf(cell.toString());
                }
                
                //productCode
                cell = row.getCell(colIndex++);
                Long productCode = Long.valueOf(cell.toString());
                
                // RBI Classification
                cell = row.getCell(colIndex++);
                Long rbiClassification = Long.valueOf(cell.toString());
                
                //Inter Entity
                cell = row.getCell(colIndex++);
                Long interEntity = Long.valueOf(cell.toString());
              
                //Source Code
                cell = row.getCell(colIndex++);
                Long sourceCode = Long.valueOf(cell.toString());
                
                //spare1
                cell = row.getCell(colIndex++);
                Long spare1 = Long.valueOf(cell.toString());
                
                //Spare2
                cell = row.getCell(colIndex++);
                Long spare2 = Long.valueOf(cell.toString());
              
                LoanExcelImportData excelData = LoanExcelImportData.instance(officeId, loanId, closedDate, accountNo, loanStatus, branchCode, profitCenter, costCenter, naturalAccount, 
                		productCode, rbiClassification, interEntity, sourceCode, spare1, spare2);
                
                loanExcelDatas.add(excelData);
            }
            
            processLoanAccountImports(loanExcelDatas);
           
        
        }  catch (IOException ioe) {
            // TODO Auto-generated catch block
        	ioe.printStackTrace();
        }
	}
	
	@Transactional
	private void processLoanAccountImports(List<LoanExcelImportData> loanExcelDatas) {
		
		String commaSeparator = ", ";
		 String tableName = PortfolioConstants.getLoanTableName(Long.valueOf(sourceOfficeId), tableNameSuffix);
         
         jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
         
         String tableSchema = PortfolioConstants.getLoanImportTableScript(Long.valueOf(sourceOfficeId), tableNameSuffix);
         
         jdbcTemplate.execute(tableSchema);
         
         for(LoanExcelImportData loanImportData : loanExcelDatas) {
         	
         	StringBuilder builder = new StringBuilder();
         	builder.append(PortfolioConstants.INSERT_QUERY).append(tableName)
         	.append(PortfolioConstants.LOAN_IMPORT_VALUES);
         	
         	builder.append(loanImportData.getOfficeId()).append(commaSeparator)
         	.append(loanImportData.getLoanId()).append(commaSeparator)
         	.append(loanImportData.getClosedDate()).append(commaSeparator).append("'")
         	.append(loanImportData.getAccountNo()).append("'").append(commaSeparator)
         	.append(loanImportData.getLoanStatus()).append(commaSeparator)
         	.append(loanImportData.getBranchCode()).append(commaSeparator)
         	.append(loanImportData.getProfitCenter()).append(commaSeparator)
         	.append(loanImportData.getCostCenter()).append(commaSeparator)
         	.append(loanImportData.getNaturalAccount()).append(commaSeparator)
         	.append(loanImportData.getProductCode()).append(commaSeparator)
         	.append(loanImportData.getRbiClassification()).append(commaSeparator)
         	.append(loanImportData.getInterEntity()).append(commaSeparator)
         	.append(loanImportData.getSourceCode()).append(commaSeparator)
         	.append(loanImportData.getSpare1()).append(commaSeparator)
         	.append(loanImportData.getSpare2()).append(PortfolioConstants.CLOSEING_QUERY);
     
         	jdbcTemplate.update(builder.toString());
         }
         
        
		
	}

	
	
}
