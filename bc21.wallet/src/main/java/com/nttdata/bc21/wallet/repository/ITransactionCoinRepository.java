package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.TransactionCoin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ITransactionCoinRepository extends ReactiveMongoRepository<TransactionCoin, String> {
}
