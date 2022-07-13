package com.nttdata.bc21.wallet.api;


import com.nttdata.bc21.wallet.model.*;
import com.nttdata.bc21.wallet.request.*;
import com.nttdata.bc21.wallet.service.IWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/wallet")
public class WalletApi {
    @Autowired
    private IWalletService walletService;

    @PostMapping
    public Mono<Wallet> create(@RequestBody WalletRequest wallet){ return walletService.create(wallet); }

    @PutMapping
    public Mono<Wallet> update(@RequestBody Wallet wallet){ return walletService.update(wallet); }

    @GetMapping
    public Flux<Wallet> findAll(){
        return walletService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Wallet> findById(@PathVariable String id){ return walletService.findById(id); }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable String id){
        return walletService.deleteById(id);
    }

    @PostMapping("/transaction")
    public Flux<TransactionWalletCoin> transactionWalletFindAll(){
        return walletService.transactionWalletFindAll();
    }
}