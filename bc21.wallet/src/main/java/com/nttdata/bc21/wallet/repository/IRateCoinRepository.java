package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.RateCoin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IRateCoinRepository extends ReactiveMongoRepository<RateCoin,String> {
}
