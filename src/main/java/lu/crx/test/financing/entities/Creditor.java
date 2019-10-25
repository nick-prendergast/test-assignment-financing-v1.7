package lu.crx.test.financing.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A creditor is an entity that provided some goods to the debtor according to the invoice and is waiting for
 * this invoice to be paid by the debtor.
 */
@Entity
@Getter
@Setter
@ToString
public class Creditor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Basic(optional = false)
    private String name;

    /**
     * Maximum acceptable financing rate for this creditor.
     */
    @Basic(optional = false)
    private int maxFinancingRateInBps;

}
