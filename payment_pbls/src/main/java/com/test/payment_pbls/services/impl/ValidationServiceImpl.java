package com.test.payment_pbls.services.impl;


import com.test.payment_pbls.services.ValidationService;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {

    private static final int[] IIN_WEIGHTS =  {-1, 5, 7, 9, 4, 6, 10, 5, 7};
    private static final int[] EDRPOU_BASE_WEIGHTS = {1, 2, 3, 4, 5, 6, 7};
    private static final int[] EDRPOU_ALTERNATIVE_WEIGHTS = {7, 1, 2, 3, 4, 5, 6};

    @Override
    public void validatePayerIinChecksum(String iin) {

        if (iin == null || !iin.matches("^\\d{10}$")) {
            throw new ValidationException("IIN must be a 10-digit number.");
        }

        int sum = 0;
        int checkDigit = Character.getNumericValue(iin.charAt(9));

        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(iin.charAt(i));
            sum += digit * IIN_WEIGHTS[i];
        }

        int calculatedChecksum = sum % 11;

        if (calculatedChecksum == 10) {
            calculatedChecksum = 0;
        }

        if (calculatedChecksum != checkDigit) {
            throw new ValidationException("Invalid IIN checksum. Data integrity violation.");
        }
    }

    @Override
    public void validatePayerEdrpouChecksum(String edrpou) {

        if (edrpou == null ||!edrpou.matches("^\\d{8}$")) {
            throw new ValidationException("EDRPOU must be a 8-digit number.");
        }

        int checkDigit = Character.getNumericValue(edrpou.charAt(7));

        String digits = edrpou.substring(0, 7);

        int calculatedChecksum = calculateEdrpouMod11(digits, EDRPOU_BASE_WEIGHTS);

        if (calculatedChecksum == 10) {
            calculatedChecksum = calculateEdrpouMod11(digits, EDRPOU_ALTERNATIVE_WEIGHTS);
        }

        if (calculatedChecksum != checkDigit) {
            throw new ValidationException("Invalid EDRPOU checksum. Data integrity violation.");
        }
    }

    private int calculateEdrpouMod11(String digits, int[] weights) {
        int sum = 0;

        for (int i = 0; i < 7; i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            sum += digit * weights[i];
        }

        return sum % 11;
    }
}
