package com.nttdata.bc21.wallet.service.impl;

import com.nttdata.bc21.wallet.model.ClientCoin;
import com.nttdata.bc21.wallet.model.RateCoin;
import com.nttdata.bc21.wallet.model.RequestBuyCoin;
import com.nttdata.bc21.wallet.model.TransactionCoin;
import com.nttdata.bc21.wallet.producer.KafkaStringProducer;
import com.nttdata.bc21.wallet.repository.*;
import com.nttdata.bc21.wallet.request.AcceptRequestBuyCoinRequest;
import com.nttdata.bc21.wallet.request.CoinRequest;
import com.nttdata.bc21.wallet.request.RequestBuyCoinRequest;
import com.nttdata.bc21.wallet.request.TransactionWalletCoinRequest;
import com.nttdata.bc21.wallet.service.ICoinService;
import com.nttdata.bc21.wallet.service.IWalletService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CoinServiceImpl implements ICoinService {

    @Autowired
    IClientCoinRepository iClientCoinRepository;

    @Autowired
    IWalletRepository iWalletRepository;

    @Autowired
    IRateCoinRepository iRateCoinRepository;

    @Autowired
    IRequestBuyCoinRepository iRequestBuyCoinRepository;

    @Autowired
    ITransactionCoinRepository iTransactionCoinRepository;

    @Autowired
    IWalletService walletService;
    private final KafkaStringProducer kafkaStringProducer;

    public CoinServiceImpl(KafkaStringProducer kafkaStringProducer) {
        this.kafkaStringProducer = kafkaStringProducer;
    }

    @Override
    public Mono<ClientCoin> create(CoinRequest coinRequest) {
        return iWalletRepository.findByPhone(coinRequest.getPhone())
                .flatMap(walletResponse -> {
                    ClientCoin clientCoin = new ClientCoin();
                    clientCoin.setId(new ObjectId().toString());
                    clientCoin.setCreatedAt(LocalDateTime.now());
                    clientCoin.setDocumentType(coinRequest.getDocumentType());
                    clientCoin.setDocumentNumber(coinRequest.getDocumentNumber());
                    clientCoin.setPhone(coinRequest.getPhone());
                    clientCoin.setEmail(coinRequest.getEmail());
                    clientCoin.setCoins(0);
                    this.kafkaStringProducer.sendMessage("Client Coin created");
                    return iClientCoinRepository.save(clientCoin);
                });
    }

    @Override
    public Mono<ClientCoin> update(ClientCoin clientCoin) {
        clientCoin.setUpdatedAt(LocalDateTime.now());
        this.kafkaStringProducer.sendMessage("Client Coin Update");
        return iClientCoinRepository.save(clientCoin);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return iClientCoinRepository.deleteById(id);
    }

    @Override
    public Mono<ClientCoin> findByPhone(String phone) {
        return iClientCoinRepository.findByPhone(phone);
    }


    @Override
    public Mono<ClientCoin> findById(String id) {
        return iClientCoinRepository.findById(id);
    }

    @Override
    public Flux<ClientCoin> findAll() {
        return iClientCoinRepository.findAll();
    }

    @Override
    public Flux<RateCoin> getRate() {
        return iRateCoinRepository.findAll();
    }


    @Override
    public Mono<RateCoin> setRateCoin(RateCoin rateCoin) {
        return iRateCoinRepository.findAll()
                .filter(f -> f.getRateType().getName().equals(rateCoin.getRateType().getName()))
                .singleOrEmpty()
                .switchIfEmpty(iRateCoinRepository.save(rateCoin))
                .flatMap(rateCoinResponse -> {
                    rateCoinResponse.setRate(rateCoin.getRate());
                    this.kafkaStringProducer.sendMessage("Rate Coin Create");
                    return iRateCoinRepository.save(rateCoinResponse);
                });
    }

    @Override
    public Mono<RequestBuyCoin> requestBuyCoin(RequestBuyCoinRequest requestBuyCoinRequest) {
        return iClientCoinRepository.findByPhone(requestBuyCoinRequest.getPhoneClientCoin())
                .flatMap(clientCoinResponse -> {
                    RequestBuyCoin requestBuyCoin = new RequestBuyCoin();
                    requestBuyCoin.setCoinsToBuy(requestBuyCoinRequest.getCoinsToBuy());
                    requestBuyCoin.setClientCoin(clientCoinResponse);
                    requestBuyCoin.setAccepted(false);
                    this.kafkaStringProducer.sendMessage("Request buy Coin Create");
                    return iRequestBuyCoinRepository.save(requestBuyCoin);
                });
    }

    @Override
    public Flux<RequestBuyCoin> findAllRequestBuyCoin() {
        return iRequestBuyCoinRepository.findAll().filter(f -> !f.isAccepted());
    }

    @Override
    public Mono<TransactionCoin> acceptRequestBuyCoin(AcceptRequestBuyCoinRequest acceptRequestBuyCoinRequest) {
        return iRequestBuyCoinRepository.findById(acceptRequestBuyCoinRequest.getIdRequestBuyCoin())
                .flatMap(requestBuyCoinResponse -> {
                    return iRateCoinRepository.findAll().filter(f -> f.getRateType().getName().equals("COMPRA")).singleOrEmpty()
                            .flatMap(rateBuyCoin -> {
                                return iRateCoinRepository.findAll().filter(f -> f.getRateType().getName().equals("VENTA")).singleOrEmpty()
                                        .flatMap(rateSaleCoin -> {
                                            TransactionWalletCoinRequest transactionWalletCoinRequest = new TransactionWalletCoinRequest();
                                            transactionWalletCoinRequest.setAmountSource(requestBuyCoinResponse.getCoinsToBuy() * rateBuyCoin.getRate());
                                            transactionWalletCoinRequest.setAmountDestiny(requestBuyCoinResponse.getCoinsToBuy() * rateSaleCoin.getRate());
                                            transactionWalletCoinRequest.setPhoneDestiny(acceptRequestBuyCoinRequest.getPhone());
                                            transactionWalletCoinRequest.setPhoneSource(requestBuyCoinResponse.getClientCoin().getPhone());

                                            return walletService.transactionWalletCoin(transactionWalletCoinRequest)
                                                    .flatMap(transactionWalletCoin -> {
                                                        return this.transferCoin(acceptRequestBuyCoinRequest, requestBuyCoinResponse);
                                                    });
                                        });
                            });
                });
    }

    private Mono<TransactionCoin> transferCoin(AcceptRequestBuyCoinRequest acceptRequestBuyCoinRequest, RequestBuyCoin requestBuyCoin){
        return iClientCoinRepository.findByPhone(acceptRequestBuyCoinRequest.getPhone())
                .flatMap(clientSaleCoinResponse -> {
                    if (clientSaleCoinResponse.getCoins() < requestBuyCoin.getCoinsToBuy())
                       return null; //"Insufficient coins."
                    return iClientCoinRepository.findByPhone(requestBuyCoin.getClientCoin().getPhone())
                            .flatMap(clientBuyCoinResponse -> {
                                clientSaleCoinResponse.setCoins(clientSaleCoinResponse.getCoins() - requestBuyCoin.getCoinsToBuy());
                                clientSaleCoinResponse.setUpdatedAt(LocalDateTime.now());
                                return iClientCoinRepository.save(clientSaleCoinResponse)
                                         .flatMap(clientSaleCoinUpdatedResponse -> {
                                            clientBuyCoinResponse.setCoins(clientBuyCoinResponse.getCoins() + requestBuyCoin.getCoinsToBuy());
                                            clientBuyCoinResponse.setUpdatedAt(LocalDateTime.now());
                                            return iClientCoinRepository.save(clientBuyCoinResponse)
                                                   .flatMap(clientBuyCoinUpdatedResponse -> {
                                                        requestBuyCoin.setAccepted(true);
                                                        return iRequestBuyCoinRepository.save(requestBuyCoin)
                                                                .flatMap(requestBuyCoinUpdate -> {
                                                                    TransactionCoin transactionCoin = new TransactionCoin();
                                                                    transactionCoin.setIdRequestBuyCoin(acceptRequestBuyCoinRequest.getIdRequestBuyCoin());
                                                                    transactionCoin.setRequestBuyCoin(requestBuyCoinUpdate);
                                                                    transactionCoin.setIdClientCoinSale(clientSaleCoinResponse.getId());
                                                                    transactionCoin.setClientCoinSale(clientSaleCoinResponse);
                                                                    transactionCoin.setCreatedAt(LocalDateTime.now());
                                                                    transactionCoin.setId(new ObjectId().toString());
                                                                    return iTransactionCoinRepository.save(transactionCoin);
                                                                });
                                                    });
                                        });
                            });
                });
    }
}
