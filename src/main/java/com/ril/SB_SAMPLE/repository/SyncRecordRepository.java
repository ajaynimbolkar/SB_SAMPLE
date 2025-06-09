package com.ril.SB_SAMPLE.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ril.SB_SAMPLE.model.SyncRecordModel;

public interface SyncRecordRepository extends JpaRepository<SyncRecordModel, String> {

}
