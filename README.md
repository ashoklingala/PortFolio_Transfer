# PortFolio_Transfer

# 1) foo.location : The base location of the files. System will store the o/p files in the same location
# 2) foo.loanaccounts.filename : This will refer the loan_account's file name. This file should be under the foo.location path.
# 3) foo.glaccounts.filename : This will refer the gl_account's file name. This file should be under the foo.location path.
# 4) foo.output.filename : The output file name.
# 5) foo.tablenames.suffix : The new table name's suffix. prefix is hardcoded in the code.
# 6) LoanAccount_tableName = "loan_accounts_officeid_" + <sourceOfficeId> + "_transfer_" + <foo.tablenames.suffix>;
# 7) GLAccount_tableName = "gl_accounts_officeid_" + <sourceOfficeId> + "_transfer_" + <foo.tablenames.suffix>;