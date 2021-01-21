The application 
* creates an H2 database in the root directory of the project
* sets up the database structure according to the entities
* seeds some initial data and runs the financing algorithm
* seeds some more data and runs the financing once again

You need to implement the financing algorithm according to the specification. The algorithm has to
calculate the results of the financing and persist them (the data structure for the results is not provided).

The invoices financed in a financing round are considered to be "sold" and may not be sold twice. 

Your entry point is the `FinancingService` class. Naturally, you may create additional classes, if needed. 
You can also add new relations, new entities or fields to existing ones. You may also use any third-party 
dependencies you need. 

If you don't like something in the provided code, you are free to change it. 

What we'd like to see:
* a financing algorithm implementation
* tests that verify that your solution is correct
* any kind of documentation you think is necessary for your solution
