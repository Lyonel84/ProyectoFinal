package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.Wallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IWalletRepository extends ReactiveMongoRepository<Wallet,String> {
    Mono<Wallet> findByPhone(String phone);
}
