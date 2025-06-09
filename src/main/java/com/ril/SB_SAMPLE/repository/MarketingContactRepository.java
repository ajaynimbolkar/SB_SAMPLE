package com.ril.SB_SAMPLE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.ril.SB_SAMPLE.model.MarketingContactModel;

@Repository
public interface MarketingContactRepository extends JpaRepository<MarketingContactModel, String> {

}
