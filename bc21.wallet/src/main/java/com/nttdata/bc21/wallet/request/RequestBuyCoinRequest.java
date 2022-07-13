package com.nttdata.bc21.wallet.request;

import lombok.Data;

@Data
public class RequestBuyCoinRequest {
    private String phoneClientCoin;
    private double coinsToBuy;
}