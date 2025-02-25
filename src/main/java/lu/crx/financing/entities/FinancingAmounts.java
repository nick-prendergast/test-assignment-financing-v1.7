package lu.crx.financing.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancingAmounts {
    private long earlyPaymentAmountInCents;
    private long discountAmountInCents;
}