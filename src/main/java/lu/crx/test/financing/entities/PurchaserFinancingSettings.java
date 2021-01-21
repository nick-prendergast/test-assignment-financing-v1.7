package lu.crx.test.financing.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Financing settings set by the purchaser for a specific creditor.
 */
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaserFinancingSettings implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(optional = false)
    private Creditor creditor;

    /**
     * The annual financing rate set by the purchaser for this creditor.
     * The rate is measured in bps (basis points). One basis point is 0,01 percent, or 0,0001.
     * <p>
     * The annual rate of 50 bps means the following. Suppose we have an invoice with the value of 10 000,00 EUR.
     * The term of the invoice (time to maturity) is 30 days.
     * The rate for this term would be 50 bps * 30 days / 360 days/year = 4,167 bps
     * The purchaser's interest is then 10 000,00 EUR * 4,167 bps * 0,0001 = 4,17 EUR
     * The creditor will get 10 000,00 EUR - 4,17 EUR = 9 995,83 EUR
     */
    @Basic(optional = false)
    private int rateInBps;

    /**
     * The maximum amount of currently financed invoices for this creditor.
     */
    @Basic
    private long creditLineLimitInCents;

}
