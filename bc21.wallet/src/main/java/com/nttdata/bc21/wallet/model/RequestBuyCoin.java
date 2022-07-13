package com.nttdata.bc21.wallet.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestBuyCoin extends BaseModel{
    private ClientCoin clientCoin;
    private double coinsToBuy;
    private boolean accepted;
}