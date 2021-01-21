package lu.crx.test.financing.services;

import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FinancingService {

    @Transactional
    public void finance() {
        log.info("Running the financing");

        // TODO
        // This is the financing algorithm that needs to be implemented according to the specification.

        // For every invoice that may be financed, pick the winning purchaser. Calculate the amount of money received
        // by the creditor and the interest of the purchaser, persist calculated data.

        // The invoices will keep coming in, and the financing will be repeated multiple times. Make sure
        // already financed invoices won't be financed the second time.

        // You may improve the data structure as needed (add new entities, fields, change the mapping, etc.)

        // Take performance considerations into account: the total amount of invoices in the database could be
        // tens of millions of records, with tens of thousands actually financeable in each round.
    }

}
