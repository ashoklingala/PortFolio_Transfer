package in.novopay.portfolio.transfer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


/**
 * @author Ashok
 *
 */
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
	
	@Value("${foo.gl.batchsize}")
	private int batchSize;
	
	
	private JdbcTemplate jdbcTemplate;

	private ClassPathXmlApplicationContext context;
	
	
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
            
            for( int rowIndex = 1; rowIndex <= rowNum; rowIndex++ ) {
                
                row = sheet.getRow(rowIndex);

                int colIndex = 0;

                //
                if(row == null )
                	break;
                
                //loanId
                cell = row.getCell(colIndex++);
                Long loanId = Double.valueOf(cell.toString()).longValue();
                
                //GLId
                cell = row.getCell(colIndex++);
                Long glId = Double.valueOf(cell.toString()).longValue();
                
                //officeId
                cell = row.getCell(colIndex++);
                Long officeId = Double.valueOf(cell.toString()).longValue();
                
                //Balance
                cell = row.getCell(colIndex++);
                BigDecimal balance = BigDecimal.valueOf(Double.valueOf(cell.toString()));
                	                
                GLExcelImportData excelData = GLExcelImportData.instance(loanId, glId, officeId, balance);
                
                glExcelDatas.add(excelData);
            }
            System.out.println(glExcelDatas.size());
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
         
         saveBatch(glExcelDatas, tableName);
         
         System.out.println("GL Import done.");
         
	}
	
	public void saveBatch(final List<GLExcelImportData> glExcelImportDatas, String tableName) {  
	    
		StringBuilder builder = new StringBuilder();
     	builder.append(PortfolioConstants.INSERT_QUERY).append(tableName)
     	.append(PortfolioConstants.GL_IMPORT_VALUES);
     	
     	int recordsCount = glExcelImportDatas.size();
        
        int loopCount = recordsCount/batchSize == 0 ? 1: recordsCount/batchSize;
        
        int fromIndex=0;
        int toIndex = batchSize;
        
     	System.out.println("loop count" + loopCount + " and recordsCount:" + recordsCount);
     	
     	
	    for (int j = 0; j < loopCount; j++) {

	    	 if(j == loopCount -1) {
        		 toIndex = recordsCount;
        	 }
        	 
	    	 System.out.println(" Insert data loop count =" + j);
	    	 
        	 final List<GLExcelImportData> safeSublist = glExcelImportDatas.subList(fromIndex, toIndex);
        	 
        	 jdbcTemplate.batchUpdate( builder.toString(),
 	        		new BatchPreparedStatementSetter() {
                 public void setValues(PreparedStatement ps, int i)
                         throws SQLException {
                 	GLExcelImportData employee = safeSublist.get(i);
                     ps.setLong(1, employee.getLoanId());
                     ps.setLong(2, employee.getGlId());
                     ps.setLong(3, employee.getOfficeId());
                     ps.setBigDecimal(4, employee.getBalance());
                     
                 }

                 public int getBatchSize() {
                     return safeSublist.size();
                 }
             });
        	 
        	 
        	 System.out.println(" Insert data loop count =" + j + " completed");
        	 
        	 if(toIndex >= recordsCount) {
        		 break;
        	 }
        	 
        	 fromIndex = toIndex;
        	 toIndex = toIndex + batchSize;
	    	 
	    }
	}
	
	
	public void processGLAccountExcelDatawithCsv()  
	{  
		String line = "";  
		String splitBy = ","; 
		BufferedReader br = null;
		List<GLExcelImportData> glExcelDatas = new ArrayList<GLExcelImportData>();
		try {  
			//parsing a CSV file into BufferedReader class constructor  
			File file = new File(getFilesLocation()); // creating a new file instance
			br = new BufferedReader(new FileReader(file));  
			br.readLine();
			while ((line = br.readLine()) != null)   //returns a Boolean value  
			{  
				String[] employee = line.split(splitBy);    // use comma as separator  
				GLExcelImportData excelData = GLExcelImportData.instance(employee[0], employee[1], employee[2], employee[3]);
				glExcelDatas.add(excelData);
			}  
//			String line = br.readLine();
//			br.lines().parallel().forEach(value -> {
//				String[] employee = value.split(splitBy);    // use comma as separator  
//				GLExcelImportData excelData = GLExcelImportData.instance(employee[0], employee[1], employee[2], employee[3]);
//				glExcelDatas.add(excelData);
//			});
			
			System.out.println("total GL import count is : " + glExcelDatas.size());
            processGLAccountImports(glExcelDatas);
            System.out.println("saving into the GL table completed");
		}   
		catch (IOException e)   
		{  
			e.printStackTrace();  
		} finally {
			try {
				if(null != br) {
					br.close();
				}
					
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}  
}
