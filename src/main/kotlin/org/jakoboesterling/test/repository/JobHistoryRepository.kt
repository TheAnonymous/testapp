package org.jakoboesterling.test.repository

import org.jakoboesterling.test.domain.JobHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [JobHistory] entity.
 */
@Suppress("unused")
@Repository
interface JobHistoryRepository : JpaRepository<JobHistory, Long>
