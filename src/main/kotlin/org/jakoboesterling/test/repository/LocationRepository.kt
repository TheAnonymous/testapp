package org.jakoboesterling.test.repository

import org.jakoboesterling.test.domain.Location
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Location] entity.
 */
@Suppress("unused")
@Repository
interface LocationRepository : JpaRepository<Location, Long>
