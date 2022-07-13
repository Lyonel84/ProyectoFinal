package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.ClientCoin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IClientCoinRepository extends ReactiveMongoRepository<ClientCoin, String> {

    Mono<ClientCoin> findByPhone(String phone);
}
