package lu.crx.test.financing.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Purchaser is an entity (usually a bank) that wants to purchase the invoices, i.e. it issues a loan
 * to the creditor for the term and the value of the invoice, according to the rate set up by this purchaser.
 */
@Entity
@Getter
@Setter
@ToString
public class Purchaser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    private int minimumFinancingTermInDays;

    /**
     * The per-creditor settings for financing.
     */
    @OneToMany(cascade = CascadeType.PERSIST)
    private Set<PurchaserFinancingSettings> purchaserFinancingSettings = new HashSet<>();

}
