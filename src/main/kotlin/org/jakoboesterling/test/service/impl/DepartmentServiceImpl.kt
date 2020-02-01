package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.Department
import org.jakoboesterling.test.repository.DepartmentRepository
import org.jakoboesterling.test.service.DepartmentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Department].
 */
@Service
@Transactional
class DepartmentServiceImpl(
    private val departmentRepository: DepartmentRepository
) : DepartmentService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a department.
     *
     * @param department the entity to save.
     * @return the persisted entity.
     */
    override fun save(department: Department): Department {
        log.debug("Request to save Department : {}", department)
        return departmentRepository.save(department)
    }

    /**
     * Get all the departments.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): MutableList<Department> {
        log.debug("Request to get all Departments")
        return departmentRepository.findAll()
    }

    /**
     * Get one department by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Department> {
        log.debug("Request to get Department : {}", id)
        return departmentRepository.findById(id)
    }

    /**
     * Delete the department by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Department : {}", id)

        departmentRepository.deleteById(id)
    }
}
