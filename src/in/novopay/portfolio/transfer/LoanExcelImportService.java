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
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
	
	@Value("${foo.account.batchsize}")
	private int batchSize;
	
	
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
            
            for( int rowIndex = 1; rowIndex <= rowNum; rowIndex++ ) {
                
                row = sheet.getRow(rowIndex);

                int colIndex = 0;

                //
                if(row == null )
                	break;
                
                //officeId
                cell = row.getCell(colIndex++);
                Long officeId = Double.valueOf(cell.toString()).longValue();
                
                //loanId
                cell = row.getCell(colIndex++);
                Long loanId = Double.valueOf(cell.toString()).longValue();
                
                //closedDate
                cell = row.getCell(colIndex++);
                Date closedDate = null;
                if(cell != null && !(cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                	try {
                		closedDate = cell.getDateCellValue();
                	} catch (Exception e) {
						// TODO: handle exception
					}
                	
                }
                
                //AccountNo
                cell = row.getCell(colIndex++);
                String accountNo = cell.toString();
                
                //loanStatus
                cell = row.getCell(colIndex++);
                Long loanStatus = Double.valueOf(cell.toString()).longValue();
                
                //BranchCode
                Long branchCode = null;
                cell = row.getCell(colIndex++);
                if(cell != null && !(cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                	if(!cell.toString().equals(" ")) {
                		branchCode = Double.valueOf(cell.toString()).longValue();
                	}
                	
                }
                 
                //profitCenter
                cell = row.getCell(colIndex++);
                Long profitCenter = Double.valueOf(cell.toString()).longValue();
                
                //costCenter
                cell = row.getCell(colIndex++);
                Long costCenter = Double.valueOf(cell.toString()).longValue();
                
                //naturalAccount
                Long naturalAccount= null;
                cell = row.getCell(colIndex++);
                if(cell != null && !(cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                	String val = cell.toString();
                	if(!val.isEmpty() && !val.isEmpty() && !val.equalsIgnoreCase(" "))
                	naturalAccount = Double.valueOf(cell.toString()).longValue();
                }
                
                //productCode
                cell = row.getCell(colIndex++);
                Long productCode = Double.valueOf(cell.toString()).longValue();
                
                // RBI Classification
                cell = row.getCell(colIndex++);
                Long rbiClassification = Double.valueOf(cell.toString()).longValue();
                
                //Inter Entity
                cell = row.getCell(colIndex++);
                Long interEntity = Double.valueOf(cell.toString()).longValue();
              
                //Source Code
                cell = row.getCell(colIndex++);
                Long sourceCode = Double.valueOf(cell.toString()).longValue();
                
                //spare1
                cell = row.getCell(colIndex++);
                Long spare1 = Double.valueOf(cell.toString()).longValue();
                
                //Spare2
                cell = row.getCell(colIndex++);
                Long spare2 = Double.valueOf(cell.toString()).longValue();
              
                LoanExcelImportData excelData = LoanExcelImportData.instance(officeId, loanId, closedDate, accountNo, loanStatus, branchCode, profitCenter, costCenter, naturalAccount, 
                		productCode, rbiClassification, interEntity, sourceCode, spare1, spare2);
                
                loanExcelDatas.add(excelData);
            }
            
            System.out.println("Reading from Excel Done");
            processLoanAccountImports(loanExcelDatas);
            System.out.println("processing from Excel Done");
        
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
         
         int recordsCount = loanExcelDatas.size();
         
         int loopCount = recordsCount/batchSize == 0 ? 1: recordsCount/batchSize;
         
         int fromIndex=0;
         int toIndex = batchSize;
         
         System.out.println("loop count" + loopCount + " and recordsCount:" + recordsCount);
         for(int i=0; i< loopCount; i++) {
        	 
        	 if(i == loopCount -1) {
        		 toIndex = recordsCount;
        	 }
        	 
        	 System.out.println(" Insert data loop count =" + i);
        	 List<LoanExcelImportData> safeSublist = loanExcelDatas.subList(fromIndex, toIndex);
        	 insertData(safeSublist, tableName, commaSeparator);
        	 System.out.println(" Insert data loop count =" + i + " completed");
        	 if(toIndex >= recordsCount) {
        		 break;
        	 }
        	 
        	 fromIndex = toIndex;
        	 toIndex = toIndex + batchSize;
         }
	}
	
	private void insertData(List<LoanExcelImportData> loanExcelDatas, String tableName, String commaSeparator) {
		 
		StringBuilder builder = new StringBuilder();
     	builder.append(PortfolioConstants.INSERT_QUERY).append(tableName)
     	.append(PortfolioConstants.LOAN_IMPORT_VALUES);
     	
     	int count = 0;
     	int size = loanExcelDatas.size();
		for(LoanExcelImportData loanImportData : loanExcelDatas) {
	         	count = count + 1;
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
	         	.append(loanImportData.getSpare2());
	         	
	         	if(size != count) {
	         		builder.append(PortfolioConstants.APPENDING_QUERY);
	         	} else {
	         		builder.append(PortfolioConstants.CLOSEING_QUERY);
	         	}
		}
		jdbcTemplate.batchUpdate(builder.toString());
	}

	
	
}
