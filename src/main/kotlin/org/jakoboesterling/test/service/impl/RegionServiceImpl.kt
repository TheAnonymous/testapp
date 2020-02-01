package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.Region
import org.jakoboesterling.test.repository.RegionRepository
import org.jakoboesterling.test.service.RegionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Region].
 */
@Service
@Transactional
class RegionServiceImpl(
    private val regionRepository: RegionRepository
) : RegionService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a region.
     *
     * @param region the entity to save.
     * @return the persisted entity.
     */
    override fun save(region: Region): Region {
        log.debug("Request to save Region : {}", region)
        return regionRepository.save(region)
    }

    /**
     * Get all the regions.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): MutableList<Region> {
        log.debug("Request to get all Regions")
        return regionRepository.findAll()
    }

    /**
     * Get one region by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Region> {
        log.debug("Request to get Region : {}", id)
        return regionRepository.findById(id)
    }

    /**
     * Delete the region by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Region : {}", id)

        regionRepository.deleteById(id)
    }
}
