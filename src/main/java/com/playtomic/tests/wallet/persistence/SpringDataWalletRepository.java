package com.playtomic.tests.wallet.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataWalletRepository extends CrudRepository<WalletEntity, String> {

}
