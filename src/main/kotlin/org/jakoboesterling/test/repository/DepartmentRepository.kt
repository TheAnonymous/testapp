package org.jakoboesterling.test.repository

import org.jakoboesterling.test.domain.Department
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Department] entity.
 */
@Suppress("unused")
@Repository
interface DepartmentRepository : JpaRepository<Department, Long>
