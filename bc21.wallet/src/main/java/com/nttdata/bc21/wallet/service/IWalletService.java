package com.nttdata.bc21.wallet.service;

import com.nttdata.bc21.wallet.model.*;
import com.nttdata.bc21.wallet.request.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IWalletService {
    Mono<Wallet> create(WalletRequest walletRequest);
    Mono<Wallet> update(Wallet wallet);
    Mono<Void> deleteById(String id);
    Mono<Wallet> findById(String id);
    Flux<Wallet> findAll();

    Flux<TransactionWalletCoin> transactionWalletFindAll();
    Mono<TransactionWalletCoin> transactionWalletCoin(TransactionWalletCoinRequest transactionWalletCoinRequest);
}
