package in.novopay.portfolio.transfer;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * @author Ashok Lingala
 *
 */
public class PortFolioTransferMainApp {
	
	public static void main(String[] args) {

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	    
		JdbcTemplate jdbcTemplate = (JdbcTemplate) context.getBean("jdbcTemplate");
		
		 //Import LoanData
		LoanExcelImportService loanExcelImportService = (LoanExcelImportService) context.getBean("loanExcelImport");
		loanExcelImportService.setJdbcTemplate(jdbcTemplate);
		loanExcelImportService.processLoanAccountExcelData();
		
		// Import GLData
		GLExcelImportService glExcelImportService = (GLExcelImportService) context.getBean("glExcelImport");
		glExcelImportService.setJdbcTemplate(jdbcTemplate);
		glExcelImportService.processGLAccountExcelDatawithCsv();
		
		//Proces C-40 scripts
		GenerateC40ScriptService generateC40ScriptService = (GenerateC40ScriptService) context.getBean("generateC40Service");
		generateC40ScriptService.setJdbcTemplate(jdbcTemplate);
		generateC40ScriptService.processC40Scripts();
	
		context.close();
	}

}
