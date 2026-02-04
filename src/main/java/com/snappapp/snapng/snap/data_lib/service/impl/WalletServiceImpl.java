package com.snappapp.snapng.snap.data_lib.service.impl;

import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceAlreadyExistsException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.entities.Wallet;
import com.snappapp.snapng.snap.data_lib.repositories.WalletRepository;
import com.snappapp.snapng.snap.data_lib.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {
    private long maxAmount = 99999999999999L;
    private final WalletRepository repo;

    public WalletServiceImpl(WalletRepository repo) {
        this.repo = repo;
    }

    @Override
    public Wallet save(String name) {
        Wallet wallet = new Wallet();
        wallet.setName(name.toUpperCase());
        wallet.setWalletKey(generateKey());
        return repo.save(wallet);
    }

    @Override
    public Wallet createInternal(String name, String key, Long initialBalance) {
        if(repo.findByWalletKey(key).isPresent()){
            throw new ResourceAlreadyExistsException("Wallet with key already exists");
        }
        Wallet wallet = new Wallet();
        wallet.setName(name.toUpperCase());
        wallet.setWalletKey(key);
        wallet.setAvailableBalance(initialBalance);
        wallet.setBookBalance(initialBalance);
        return repo.save(wallet);
    }

    @Override
    public Wallet save(String walletKey, boolean canDebit, boolean canCredit) {
        Wallet wallet = get(walletKey);
        wallet.setCanCredit(canCredit);
        wallet.setCanDebit(canDebit);
        return repo.save(wallet);
    }

    @Override
    public Wallet updateCreditStatus(String walletKey, boolean canCredit) {
        Wallet wallet = get(walletKey);
        wallet.setCanCredit(canCredit);
        return repo.save(wallet);
    }

    @Override
    public Wallet updateDebitStatus(String walletKey, boolean canDebit) {
        Wallet wallet = get(walletKey);
        wallet.setCanDebit(canDebit);
        return repo.save(wallet);
    }

    @Override
    public Wallet get(String walletKey) {
        return repo.findByWalletKey(walletKey).orElseThrow(()-> new ResourceNotFoundException("Wallet is invalid"));
    }

    @Override
    public Page<Wallet> get(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Override
    public Wallet updateBalances(Long amount, String key, boolean book, boolean available) {
        validateBalances(amount, key, book, available);
        if(book && available){
            repo.updateBookAndAvailableBalance(amount,key);
        }
        else if(book){
            repo.updateBookBalance(amount,key);
        }
        else if(available){
            repo.updateAvailableBalance(amount, key);
        }
        else{
            throw new FailedProcessException("Incorrect task requested, must update available or book balance");
        }
        return get(key);
    }

    @Override
    public Wallet validateBalances(Long amount, String key, boolean book, boolean available) {
        if(amount ==0){
            throw new FailedProcessException("Invalid Transaction amount");
        }
        Wallet wallet = get(key);
        if(amount<0 && !wallet.getCanDebit()){
            throw new FailedProcessException("Wallet status does not allow debit");
        }
        if(amount>0 && !wallet.getCanCredit()){
            throw new FailedProcessException("Wallet status does not allow credit");
        }
        if(amount<0){
            if(available && 0 > (wallet.getAvailableBalance()+amount)){
                throw new FailedProcessException("Insufficient Balance");
            }
            if(book && 0 > (wallet.getBookBalance()+amount)){
                throw new FailedProcessException("Insufficient Balance");
            }
        }
        else{
            if(available && maxAmount < (wallet.getAvailableBalance()+amount)){
                throw new FailedProcessException("Balance maximum limit reached");
            }
            if(book && maxAmount < (wallet.getBookBalance()+amount)){
                throw new FailedProcessException("Balance maximum limit reached");
            }
        }
        return wallet;
    }

    @Override
    public Wallet getByWalletKey(String walletKey) {
        return repo.findByWalletKey(walletKey)
                .orElseThrow(() ->
                        new FailedProcessException("Wallet not found: " + walletKey));
    }

    /**
     * available -> book   (hold)
     */
    @Transactional
    @Override
    public void moveAvailableToBook(String walletKey, Long amount){

        Wallet wallet = getByWalletKey(walletKey);

        log.info("[WALLET_AVAILABLE_TO_BOOK] wallet={}, amount={}, availableBefore={}, bookBefore={}",
                walletKey, amount, wallet.getAvailableBalance(), wallet.getBookBalance());

        if(wallet.getAvailableBalance() < amount){
            throw new FailedProcessException("Insufficient available balance");
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance() - amount);
        wallet.setBookBalance(wallet.getBookBalance() + amount);

        repo.save(wallet);

        log.info("[WALLET_AVAILABLE_TO_BOOK_DONE] wallet={}, availableAfter={}, bookAfter={}",
                walletKey, wallet.getAvailableBalance(), wallet.getBookBalance());
    }


    /**
     * book -> remove   (user side escrow consumption)
     */
    @Transactional
    @Override
    public void creditBook(String walletKey, Long amount){

        Wallet wallet = getByWalletKey(walletKey);

        log.info("[WALLET_BOOK_CREDIT] wallet={}, amount={}, bookBefore={}",
                walletKey, amount, wallet.getBookBalance());

        wallet.setBookBalance(wallet.getBookBalance() + amount);

        repo.save(wallet);

        log.info("[WALLET_BOOK_CREDIT_DONE] wallet={}, bookAfter={}",
                walletKey, wallet.getBookBalance());
    }


    /**
     * book -> available   (recipient settlement)
     */
    @Transactional
    @Override
    public void debitBook(String walletKey, Long amount){

        Wallet wallet = getByWalletKey(walletKey);

        log.info("[WALLET_BOOK_DEBIT] wallet={}, amount={}, bookBefore={}",
                walletKey, amount, wallet.getBookBalance());

        if(wallet.getBookBalance() < amount){
            throw new FailedProcessException("Insufficient book balance");
        }

        wallet.setBookBalance(wallet.getBookBalance() - amount);

        repo.save(wallet);

        log.info("[WALLET_BOOK_DEBIT_DONE] wallet={}, bookAfter={}",
                walletKey, wallet.getBookBalance());
    }


    @Transactional
    @Override
    public void bookToAvailable(String walletKey, Long amount){

        Wallet wallet = getByWalletKey(walletKey);

        log.info("[WALLET_BOOK_TO_AVAILABLE] wallet={}, amount={}, bookBefore={}, availableBefore={}",
                walletKey, amount, wallet.getBookBalance(), wallet.getAvailableBalance());

        if(wallet.getBookBalance() < amount){
            throw new FailedProcessException("Insufficient book balance");
        }

        wallet.setBookBalance(wallet.getBookBalance() - amount);
        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);

        repo.save(wallet);

        log.info("[WALLET_BOOK_TO_AVAILABLE_DONE] wallet={}, bookAfter={}, availableAfter={}",
                walletKey, wallet.getBookBalance(), wallet.getAvailableBalance());
    }

}
