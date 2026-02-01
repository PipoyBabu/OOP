package bagtas.carparkmanagementsystem;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author unit 1
 */
public abstract class Payment extends BasePayment {
    private final double amount;
    private final double cashGiven;      // for cash
    private final String cardNumber;     // for card
    private final String cardHolder;     // for card
    private final String cardPin;        // optional PIN for card

    public Payment(double amount) {
        this(amount, 0.0, null, null, null);
    }

    public Payment(double amount, double cashGiven) {
        this(amount, cashGiven, null, null, null);
    }

    public Payment(double amount, String cardNumber, String cardHolder) {
        this(amount, 0.0, cardNumber, cardHolder, null);
    }

    // Optional constructor that accepts a card PIN (e.g., 6-digit PIN)
    public Payment(double amount, String cardNumber, String cardHolder, String cardPin) {
        this(amount, 0.0, cardNumber, cardHolder, cardPin);
    }

    private Payment(double amount, double cashGiven, String cardNumber, String cardHolder, String cardPin) {
        this.amount = amount;
        this.cashGiven = cashGiven;
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.cardPin = cardPin;
    }

    public double getAmount() { return amount; }
    public double getCashGiven() { return cashGiven; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolder() { return cardHolder; }
    public String getCardPin() { return cardPin; }


    public abstract PaymentResult process();
}