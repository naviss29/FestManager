package com.festmanager.repository;

import com.festmanager.entity.JournalAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, UUID> {

    Page<JournalAudit> findByEntiteCibleAndEntiteId(String entiteCible, UUID entiteId, Pageable pageable);

    Page<JournalAudit> findByUtilisateurId(UUID utilisateurId, Pageable pageable);
}
