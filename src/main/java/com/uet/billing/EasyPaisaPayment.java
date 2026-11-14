package com.uet.billing;

/** EasyPaisa: an 11-digit mobile number that starts with "03" receives an OTP push. */
public class EasyPaisaPayment implements PaymentStrategy {

    private static final int MOBILE_NUMBER_LENGTH = 11;
    private static final String MOBILE_PREFIX = "03";

    @Override
    public String methodName() {
        return "EASYPAISA";
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        String mobileNumber = request.getMobileNumber();
        if (mobileNumber.length() == MOBILE_NUMBER_LENGTH && mobileNumber.startsWith(MOBILE_PREFIX)) {
            return PaymentResult.paid("Sending EasyPaisa OTP push to " + mobileNumber
                    + " for PKR " + request.getAmount());
        }
        return PaymentResult.declined("EasyPaisa failed: Invalid mobile number.");
    }
}
