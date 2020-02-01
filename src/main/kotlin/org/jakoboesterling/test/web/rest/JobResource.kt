package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.Job
import org.jakoboesterling.test.repository.JobRepository
import org.jakoboesterling.test.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val ENTITY_NAME = "job"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Job].
 */
@RestController
@RequestMapping("/api")
@Transactional
class JobResource(
    private val jobRepository: JobRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /jobs` : Create a new job.
     *
     * @param job the job to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new job, or with status `400 (Bad Request)` if the job has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/jobs")
    fun createJob(@RequestBody job: Job): ResponseEntity<Job> {
        log.debug("REST request to save Job : {}", job)
        if (job.id != null) {
            throw BadRequestAlertException(
                "A new job cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = jobRepository.save(job)
        return ResponseEntity.created(URI("/api/jobs/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /jobs` : Updates an existing job.
     *
     * @param job the job to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated job,
     * or with status `400 (Bad Request)` if the job is not valid,
     * or with status `500 (Internal Server Error)` if the job couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/jobs")
    fun updateJob(@RequestBody job: Job): ResponseEntity<Job> {
        log.debug("REST request to update Job : {}", job)
        if (job.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = jobRepository.save(job)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     job.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /jobs` : get all the jobs.
     *

     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the [ResponseEntity] with status `200 (OK)` and the list of jobs in body.
     */
    @GetMapping("/jobs")
    fun getAllJobs(
        pageable: Pageable,
        @RequestParam(required = false, defaultValue = "false") eagerload: Boolean
    ): ResponseEntity<MutableList<Job>> {
        log.debug("REST request to get a page of Jobs")
        val page: Page<Job> = if (eagerload) {
            jobRepository.findAllWithEagerRelationships(pageable)
        } else {
            jobRepository.findAll(pageable)
        }
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /jobs/:id` : get the "id" job.
     *
     * @param id the id of the job to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the job, or with status `404 (Not Found)`.
     */
    @GetMapping("/jobs/{id}")
    fun getJob(@PathVariable id: Long): ResponseEntity<Job> {
        log.debug("REST request to get Job : {}", id)
        val job = jobRepository.findOneWithEagerRelationships(id)
        return ResponseUtil.wrapOrNotFound(job)
    }
    /**
     *  `DELETE  /jobs/:id` : delete the "id" job.
     *
     * @param id the id of the job to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/jobs/{id}")
    fun deleteJob(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Job : {}", id)

        jobRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
