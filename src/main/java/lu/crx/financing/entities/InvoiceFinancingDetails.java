package lu.crx.financing.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity to store details about the financing of an invoice.
 */
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFinancingDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The invoice that was financed.
     */
    @OneToOne(optional = false)
    private Invoice invoice;

    /**
     * The purchaser that financed the invoice.
     */
    @ManyToOne(optional = false)
    private Purchaser purchaser;

    /**
     * The date when the financing was applied.
     */
    @Basic(optional = false)
    private LocalDate financingDate;

    /**
     * The financing term in days.
     */
    @Basic(optional = false)
    private int financingTermInDays;

    /**
     * The applied financing rate in bps.
     */
    @Basic(optional = false)
    private BigDecimal financingRateInBps;

    /**
     * Early payment amount in cents.
     */
    @Basic(optional = false)
    private BigDecimal earlyPaymentAmountInCents;

    /**
     * Discounted amount in cents.
     */
    @Basic(optional = false)
    private BigDecimal discountedAmountInCents;
}