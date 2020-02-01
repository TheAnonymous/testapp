package org.jakoboesterling.test.repository

import org.jakoboesterling.test.domain.Employee
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Employee] entity.
 */
@Suppress("unused")
@Repository
interface EmployeeRepository : JpaRepository<Employee, Long>
