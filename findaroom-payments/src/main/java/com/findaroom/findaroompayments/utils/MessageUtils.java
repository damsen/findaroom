package com.findaroom.findaroompayments.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtils {

    public static final String PAYMENT_NOT_FOUND = "Payment not found.";

    public static final String ORDER_WAS_NOT_COMPLETED = "Order was not completed.";
    public static final String ORDER_IS_CORRUPTED = "Order is corrupted.";

}
