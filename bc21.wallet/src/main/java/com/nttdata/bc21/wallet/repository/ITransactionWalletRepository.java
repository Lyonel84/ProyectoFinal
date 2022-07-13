package com.nttdata.bc21.wallet.repository;

import com.nttdata.bc21.wallet.model.TransactionWallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ITransactionWalletRepository extends ReactiveMongoRepository<TransactionWallet,String> {
}
