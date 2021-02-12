package in.novopay.portfolio.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("glExcelImport")
public class GLExcelImportService {

	
	@Value("${foo.location}")
	private String location;
	
	@Value("${foo.glaccounts.filename}")
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

	public void processGLAccountExcelData() {
		// TODO Auto-generated method stub

		XSSFWorkbook wb;
		
        try {
        	File file = new File(getFilesLocation()); // creating a new file instance
			FileInputStream fis = new FileInputStream(file); 
            wb = new XSSFWorkbook(fis);
        
            XSSFSheet sheet = wb.getSheetAt(0);
           
            int rowNum = sheet.getLastRowNum();
            XSSFRow row = sheet.getRow(0);
            XSSFCell cell;
            
            List<GLExcelImportData> glExcelDatas = new ArrayList<GLExcelImportData>();
            
            for( short rowIndex = 1; rowIndex <= rowNum; rowIndex++ ) {
                
                row = sheet.getRow(rowIndex);

                short colIndex = 0;

                //
                if(row == null )
                	break;
                
                //loanId
                cell = row.getCell(colIndex++);
                Long loanId = Long.valueOf(cell.toString());
                
                //GLId
                cell = row.getCell(colIndex++);
                Long glId = Long.valueOf(cell.toString());
                
                //officeId
                cell = row.getCell(colIndex++);
                Long officeId = Long.valueOf(cell.toString());
                
                //Balance
                cell = row.getCell(colIndex++);
                BigDecimal balance = BigDecimal.valueOf(Double.valueOf(cell.toString()));
                	                
                GLExcelImportData excelData = GLExcelImportData.instance(loanId, glId, officeId, balance);
                
                glExcelDatas.add(excelData);
            }
            
            processGLAccountImports(glExcelDatas);
           
        
        }  catch (IOException ioe) {
            // TODO Auto-generated catch block
        	ioe.printStackTrace();
        }
	
		
	}

	private void processGLAccountImports(List<GLExcelImportData> glExcelDatas) {
		// TODO Auto-generated method stub

		String commaSeparator = ", ";
		 String tableName = PortfolioConstants.getGlTableName(Long.valueOf(sourceOfficeId), tableNameSuffix);
         
         jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
         
         String tableSchema = PortfolioConstants.getGLImportTableScript(Long.valueOf(sourceOfficeId), tableNameSuffix);
         
         jdbcTemplate.execute(tableSchema);
         
         for(GLExcelImportData glExcelData : glExcelDatas) {
         	
         	StringBuilder builder = new StringBuilder();
         	builder.append(PortfolioConstants.INSERT_QUERY).append(tableName)
         	.append(PortfolioConstants.GL_IMPORT_VALUES);
         	
         	// (`loan_id`, `gl_id`, `office_id`, `balance`) 
         	
         	builder.append(glExcelData.getLoanId()).append(commaSeparator)
         	.append(glExcelData.getGlId()).append(commaSeparator)
         	.append(glExcelData.getOfficeId()).append(commaSeparator)
         	.append(glExcelData.getBalance()).append(PortfolioConstants.CLOSEING_QUERY);
     
         	jdbcTemplate.update(builder.toString());
         }
         
        
		
	
		
	}
}
