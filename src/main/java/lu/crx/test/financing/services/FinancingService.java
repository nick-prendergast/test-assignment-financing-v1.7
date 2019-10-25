package lu.crx.test.financing.services;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FinancingService {

    @Transactional
    public void finance() {
        // This is the part that needs to be implemented. Here you need to implement the financing algorithm
        // according to the specification.

        // For every invoice that may be financed, pick the winning purchaser. Calculate the amount of money received
        // by the creditor and the interest of the purchaser, persist calculated data.

        // The invoices will keep coming in, and the financing will be repeated multiple times. Make sure
        // already financed invoices won't be financed the second time.

        // You may improve the data structure as needed (add new entities, fields, change the mapping, etc.)

        // Take performance considerations into account: the total amount of invoices in the database could be
        // tens of millions of records, with tens of thousands actually financeable in each round.
    }

}
