package com.nttdata.bc21.wallet.service;

import com.nttdata.bc21.wallet.model.*;
import com.nttdata.bc21.wallet.request.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICoinService {
    Mono<ClientCoin> create(CoinRequest coinRequest);
    Mono<ClientCoin> update(ClientCoin clientCoin);
    Mono<Void>deleteById(String id);
    Mono<ClientCoin> findById(String id);
    Mono<ClientCoin> findByPhone(String phone);

    Flux<ClientCoin> findAll();

    Flux<RateCoin> getRate();
    Mono<RateCoin> setRateCoin(RateCoin rateCoin);
    Mono<RequestBuyCoin> requestBuyCoin(RequestBuyCoinRequest requestBuyCoinRequest);
    Flux<RequestBuyCoin> findAllRequestBuyCoin();

    Mono<TransactionCoin> acceptRequestBuyCoin(AcceptRequestBuyCoinRequest acceptRequestBuyCoinRequest);
}
