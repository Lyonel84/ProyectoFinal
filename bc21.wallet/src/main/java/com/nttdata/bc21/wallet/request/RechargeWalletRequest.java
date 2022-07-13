package com.nttdata.bc21.wallet.request;

import lombok.Data;

@Data
public class RechargeWalletRequest {
    private String phoneReceiver;
    private double amount;
}
