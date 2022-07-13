package com.nttdata.bc21.wallet.service.impl;

import com.nttdata.bc21.wallet.model.TransactionWallet;
import com.nttdata.bc21.wallet.model.TransactionWalletCoin;
import com.nttdata.bc21.wallet.model.Wallet;
import com.nttdata.bc21.wallet.producer.KafkaStringProducer;
import com.nttdata.bc21.wallet.repository.*;
import com.nttdata.bc21.wallet.request.TransactionWalletCoinRequest;
import com.nttdata.bc21.wallet.request.TransactionWalletRequest;
import com.nttdata.bc21.wallet.request.WalletRequest;
import com.nttdata.bc21.wallet.service.IWalletService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements IWalletService {

    @Autowired
    IWalletRepository iWalletRepository;
    @Autowired
    ITransactionWalletCoinRepository iTransactionWalletCoinRepository;

    private final KafkaStringProducer kafkaStringProducer;

    public WalletServiceImpl(KafkaStringProducer kafkaStringProducer) {
        this.kafkaStringProducer = kafkaStringProducer;
    }

    @Override
    public Mono<Wallet> create(WalletRequest walletRequest) {
        Wallet wallet = new Wallet();
        wallet.setId(new ObjectId().toString());
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setDocumentType(walletRequest.getDocumentType());
        wallet.setDocumentNumber(walletRequest.getDocumentNumber());
        wallet.setEmail(walletRequest.getEmail());
        wallet.setPhone(walletRequest.getPhone());
        wallet.setImei(walletRequest.getImei());
        wallet.setAmount(0);
        this.kafkaStringProducer.sendMessage("Rate Coin Create");
        return iWalletRepository.save(wallet);
    }

    @Override
    public Mono<Wallet> update(Wallet wallet) {
        wallet.setUpdatedAt(LocalDateTime.now());
        return iWalletRepository.save(wallet);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return iWalletRepository.deleteById(id);
    }

    @Override
    public Mono<Wallet> findById(String id) {
        return iWalletRepository.findById(id);
    }

    @Override
    public Flux<Wallet> findAll() {
        return iWalletRepository.findAll();
    }


    @Override
    public Flux<TransactionWalletCoin> transactionWalletFindAll() {
        return iTransactionWalletCoinRepository.findAll();
    }

    @Override
    public Mono<TransactionWalletCoin> transactionWalletCoin(TransactionWalletCoinRequest transactionWalletRequest) {
        return iWalletRepository.findByPhone(transactionWalletRequest.getPhoneSource())
                .flatMap(walletSourceResponse ->{
                    if(walletSourceResponse.getAmount() < transactionWalletRequest.getAmountSource())
                        return null; //Insufficient balance
                    else
                        return iWalletRepository.findByPhone(transactionWalletRequest.getPhoneDestiny())
                                .flatMap(walletDestinyResponse -> {
                                    walletSourceResponse.setAmount(walletSourceResponse.getAmount() - transactionWalletRequest.getAmountSource());
                                    walletSourceResponse.setUpdatedAt(LocalDateTime.now());
                                    return this.update(walletSourceResponse)
                                            .flatMap(walletSourceUpdateResponse -> {
                                                walletDestinyResponse.setAmount(walletDestinyResponse.getAmount() + transactionWalletRequest.getAmountDestiny());
                                                walletDestinyResponse.setUpdatedAt(LocalDateTime.now());
                                                return this.update(walletDestinyResponse)
                                                        .flatMap(walletDestinyUpdateResponse -> {
                                                            TransactionWalletCoin transactionWalletCoin = new TransactionWalletCoin();
                                                            transactionWalletCoin.setId(new ObjectId().toString());
                                                            transactionWalletCoin.setCreatedAt(LocalDateTime.now());
                                                            transactionWalletCoin.setPhoneSource(transactionWalletRequest.getPhoneSource());
                                                            transactionWalletCoin.setPhoneDestiny(transactionWalletRequest.getPhoneDestiny());
                                                            transactionWalletCoin.setAmountSource(transactionWalletRequest.getAmountSource());
                                                            transactionWalletCoin.setAmountDestiny(transactionWalletRequest.getAmountDestiny());
                                                            return iTransactionWalletCoinRepository.save(transactionWalletCoin);
                                                        });
                                            });
                                });
                });
    }
}
