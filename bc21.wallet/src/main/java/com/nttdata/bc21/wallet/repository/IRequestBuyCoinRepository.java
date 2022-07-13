package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.RequestBuyCoin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IRequestBuyCoinRepository extends ReactiveMongoRepository<RequestBuyCoin, String> {
}
