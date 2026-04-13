package com.festmanager.repository;

import com.festmanager.entity.Organisation;
import com.festmanager.entity.enums.TypeOrganisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Page<Organisation> findByType(TypeOrganisation type, Pageable pageable);

    boolean existsByEmailContact(String emailContact);
}
