package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.TransactionWalletCoin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ITransactionWalletCoinRepository extends ReactiveMongoRepository<TransactionWalletCoin,String> {
}
