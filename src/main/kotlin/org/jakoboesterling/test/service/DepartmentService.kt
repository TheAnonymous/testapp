package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.Department

/**
 * Service Interface for managing [Department].
 */
interface DepartmentService {

    /**
     * Save a department.
     *
     * @param department the entity to save.
     * @return the persisted entity.
     */
    fun save(department: Department): Department

    /**
     * Get all the departments.
     *
     * @return the list of entities.
     */
    fun findAll(): MutableList<Department>

    /**
     * Get the "id" department.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Department>

    /**
     * Delete the "id" department.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
