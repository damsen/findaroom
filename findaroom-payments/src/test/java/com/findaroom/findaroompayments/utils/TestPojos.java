package com.findaroom.findaroompayments.utils;

import com.findaroom.findaroompayments.payment.Payment;
import com.paypal.orders.*;

import java.util.List;

public class TestPojos {

    public static Payment payment() {
        return Payment.of("444", "abc", "111", 20.0d, "EUR");
    }

    public static Order order() {
        Money money = new Money();
        money.currencyCode("EUR");
        money.value("20.0");
        Capture capture = new Capture();
        capture.amount(money);
        PaymentCollection paymentCollection = new PaymentCollection();
        paymentCollection.captures(List.of(capture));
        PurchaseUnit purchaseUnit = new PurchaseUnit();
        purchaseUnit.payments(paymentCollection);
        Order order = new Order();
        order.status("COMPLETED");
        order.purchaseUnits(List.of(purchaseUnit));
        return order;
    }
}
