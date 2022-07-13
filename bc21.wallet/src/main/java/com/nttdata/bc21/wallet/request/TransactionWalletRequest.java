package com.nttdata.bc21.wallet.request;

import lombok.Data;

@Data
public class TransactionWalletRequest {
    private String phoneSource;
    private String phoneDestiny;
    private double amount;
}
