package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.JobHistory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Service Interface for managing [JobHistory].
 */
interface JobHistoryService {

    /**
     * Save a jobHistory.
     *
     * @param jobHistory the entity to save.
     * @return the persisted entity.
     */
    fun save(jobHistory: JobHistory): JobHistory

    /**
     * Get all the jobHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    fun findAll(pageable: Pageable): Page<JobHistory>

    /**
     * Get the "id" jobHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<JobHistory>

    /**
     * Delete the "id" jobHistory.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
