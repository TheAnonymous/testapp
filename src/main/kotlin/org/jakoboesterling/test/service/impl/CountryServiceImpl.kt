package org.jakoboesterling.test.service.impl

import java.util.Optional
import org.jakoboesterling.test.domain.Country
import org.jakoboesterling.test.repository.CountryRepository
import org.jakoboesterling.test.service.CountryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service Implementation for managing [Country].
 */
@Service
@Transactional
class CountryServiceImpl(
    private val countryRepository: CountryRepository
) : CountryService {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Save a country.
     *
     * @param country the entity to save.
     * @return the persisted entity.
     */
    override fun save(country: Country): Country {
        log.debug("Request to save Country : {}", country)
        return countryRepository.save(country)
    }

    /**
     * Get all the countries.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    override fun findAll(): MutableList<Country> {
        log.debug("Request to get all Countries")
        return countryRepository.findAll()
    }

    /**
     * Get one country by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    override fun findOne(id: Long): Optional<Country> {
        log.debug("Request to get Country : {}", id)
        return countryRepository.findById(id)
    }

    /**
     * Delete the country by id.
     *
     * @param id the id of the entity.
     */
    override fun delete(id: Long) {
        log.debug("Request to delete Country : {}", id)

        countryRepository.deleteById(id)
    }
}
