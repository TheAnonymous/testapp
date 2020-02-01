package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.JobHistory
import org.jakoboesterling.test.repository.JobHistoryRepository
import org.jakoboesterling.test.service.JobHistoryService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [JobHistory].
 */
@Service
@Transactional
class JobHistoryServiceImpl(
    private val jobHistoryRepository: JobHistoryRepository
) : JobHistoryService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a jobHistory.
     *
     * @param jobHistory the entity to save.
     * @return the persisted entity.
     */
    override fun save(jobHistory: JobHistory): JobHistory {
        log.debug("Request to save JobHistory : {}", jobHistory)
        return jobHistoryRepository.save(jobHistory)
    }

    /**
     * Get all the jobHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(pageable: Pageable): Page<JobHistory> {
        log.debug("Request to get all JobHistories")
        return jobHistoryRepository.findAll(pageable)
    }

    /**
     * Get one jobHistory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<JobHistory> {
        log.debug("Request to get JobHistory : {}", id)
        return jobHistoryRepository.findById(id)
    }

    /**
     * Delete the jobHistory by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete JobHistory : {}", id)

        jobHistoryRepository.deleteById(id)
    }
}
