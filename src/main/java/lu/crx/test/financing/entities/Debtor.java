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
 * A debtor is an entity that purchased some goods from the creditor according to the invoice and is obliged to pay
 * for the invoice at maturity date.
 */
@Entity
@Getter
@Setter
@ToString
public class Debtor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Basic(optional = false)
    private String name;

}
