/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bagtas.carparkmanagementsystem;

/**
 *
 * @author unit 1
 */
// Card payment implementation (mocked/placeholder)
public class CardPayment extends Payment {
    public CardPayment(double amount, String cardNumber, String cardHolder) {
        super(amount, cardNumber, cardHolder);
    }

    @Override
    public PaymentResult process() {
        String ref = makeRef();
        String pan = getCardNumber();
        if (pan == null || pan.replaceAll("\\s","").length() < 12) {
            return new PaymentResult(false, ref, 0.0, "Invalid card");
        }
        return new PaymentResult(true, ref, 0.0, "Card approved ending " + maskLast4(pan));
    }
}