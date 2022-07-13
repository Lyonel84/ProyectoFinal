package com.nttdata.bc21.wallet.request;

import com.nttdata.bc21.wallet.model.DocumentType;
import lombok.Data;

@Data
public class WalletRequest {
    private DocumentType documentType;
    private String documentNumber;
    private String phone;
    private String email;
    private String imei;
}