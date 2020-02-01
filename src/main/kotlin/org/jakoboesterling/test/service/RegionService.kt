package org.jakoboesterling.test.service

import java.util.Optional
import org.jakoboesterling.test.domain.Region

/**
 * Service Interface for managing [Region].
 */
interface RegionService {

    /**
     * Save a region.
     *
     * @param region the entity to save.
     * @return the persisted entity.
     */
    fun save(region: Region): Region

    /**
     * Get all the regions.
     *
     * @return the list of entities.
     */
    fun findAll(): MutableList<Region>

    /**
     * Get the "id" region.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    fun findOne(id: Long): Optional<Region>

    /**
     * Delete the "id" region.
     *
     * @param id the id of the entity.
     */
    fun delete(id: Long)
}
