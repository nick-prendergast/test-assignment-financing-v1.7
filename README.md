## What you get

The application in this repository: 
* creates an H2 database in the root directory of the project
* sets up the database structure according to the entities
* seeds some initial data, runs the financing algorithm and prints out the report
* seeds some more data, runs the financing once again and prints out the report

## What you need to do

You need to implement the financing algorithm according to the specification. The algorithm has to
calculate the results of the financing and persist them. One invocation of the algorithm represents
one financing round. The invoices financed in a financing round are considered to be "sold" and 
may not be sold in the subsequent financing rounds. 

To store the results of the financing, you will have to adjust the data structure. You are free to create 
new entities and adjust the existing ones.

Your entry points are `FinancingService` and `ReportingService` classes. Naturally, you may create additional
classes, if needed. You can also add new relations, new entities or fields to existing ones. You may also 
use any third-party dependencies you need. 

If you don't like something in the provided code, you are free to change it.

## Financing algorithm specification

TODO

## Glossary

* **Creditor** - a company that has sold some goods to the **Debtor**
* **Debtor** - a company that has purchased some goods from the **Creditor**
* **Invoice** - according to this document, the **Debtor** is to pay to the **Creditor** for the purchased goods 
  on a specific date in the future called **maturity date**.  
* **Maturity date** - the date when the **Creditor** expects the payment from the **Debtor**. If the **Invoice**
is financed, and the **Creditor** already got their money early from the **Purchaser**, this is the date when
  the **Debtor** pays to the **Purchaser** instead.
* **Purchaser** - a bank that is willing to finance the **Invoice** (i.e. provide money for this invoice early 
  to the **Creditor**)  
* **Financing date** - the date on which the financing has occurred.  
* **Term** of the financing - the duration in days between the **financing date** and the **Invoice**'s
  **maturity date**. The financing is essentially a loan given by the **Purchaser** to the **Creditor** for the 
  duration of the term, with a certain **financing rate** and responsibility of the **Debtor** to pay back the loan.
* Rate, or financing rate - the interest (in percent) that the **Purchaser** expects to get for financing of the 
  **Invoice**; this is the annual rate (for the term of 360 days), so the actual financing rate would be relative 
  to the term: `actualFinancingRate = annualRate * financingTerm / 360`
* **Early payment amount** - the amount of money paid by the **Purchaser** to the **Creditor** for the particular
financed invoice on **financing date**. This amount is less than the value of the invoice.
* **Maturity payment amount** - the amount of money paid back by the **Debtor** to the **Purchaser** on 
  **maturity date**. This amount is equal to the value of the invoice.
* **Purchaser interest** - the difference between **maturity payment amount** and **early payment amount**.  

## What we'd like to see

* implementation of the financing algorithm
* implementation of the reporting
* tests that verify that your solution is correct
* any kind of documentation you think is necessary for your solution

# If you want to "up the ante"

* when calculating the term of the financing, consider only working days
