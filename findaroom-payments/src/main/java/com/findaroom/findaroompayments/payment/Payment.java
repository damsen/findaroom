package com.findaroom.findaroompayments.payment;

import com.paypal.orders.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@Document(collection = "payments")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    String paymentId;
    String userId;
    String orderId;
    String bookingId;
    double amount;
    String currency;
    Instant createTime;

    public static Payment of(String userId, String orderId, String bookingId, double amount, String currency) {
        return new Payment(null, userId, orderId, bookingId, amount, currency, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public static Payment of(String userId, String orderId, String bookingId, Money money) {
        return new Payment(null, userId, orderId, bookingId, Double.parseDouble(money.value()), money.currencyCode(), Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
