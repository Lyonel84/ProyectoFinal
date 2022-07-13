package com.nttdata.bc21.wallet.request;

import lombok.Data;

@Data
public class TransactionWalletCoinRequest {
    private String phoneSource;
    private String phoneDestiny;
    private double amountSource;
    private double amountDestiny;
}