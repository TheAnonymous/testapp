package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.JobHistory
import org.jakoboesterling.test.service.JobHistoryService
import org.jakoboesterling.test.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val ENTITY_NAME = "jobHistory"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.JobHistory].
 */
@RestController
@RequestMapping("/api")
class JobHistoryResource(
    private val jobHistoryService: JobHistoryService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /job-histories` : Create a new jobHistory.
     *
     * @param jobHistory the jobHistory to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new jobHistory, or with status `400 (Bad Request)` if the jobHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/job-histories")
    fun createJobHistory(@RequestBody jobHistory: JobHistory): ResponseEntity<JobHistory> {
        log.debug("REST request to save JobHistory : {}", jobHistory)
        if (jobHistory.id != null) {
            throw BadRequestAlertException(
                "A new jobHistory cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = jobHistoryService.save(jobHistory)
        return ResponseEntity.created(URI("/api/job-histories/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /job-histories` : Updates an existing jobHistory.
     *
     * @param jobHistory the jobHistory to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated jobHistory,
     * or with status `400 (Bad Request)` if the jobHistory is not valid,
     * or with status `500 (Internal Server Error)` if the jobHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/job-histories")
    fun updateJobHistory(@RequestBody jobHistory: JobHistory): ResponseEntity<JobHistory> {
        log.debug("REST request to update JobHistory : {}", jobHistory)
        if (jobHistory.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = jobHistoryService.save(jobHistory)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     jobHistory.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /job-histories` : get all the jobHistories.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of jobHistories in body.
     */
    @GetMapping("/job-histories")
    fun getAllJobHistories(
        pageable: Pageable
    ): ResponseEntity<MutableList<JobHistory>> {
        log.debug("REST request to get a page of JobHistories")
        val page = jobHistoryService.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /job-histories/:id` : get the "id" jobHistory.
     *
     * @param id the id of the jobHistory to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the jobHistory, or with status `404 (Not Found)`.
     */
    @GetMapping("/job-histories/{id}")
    fun getJobHistory(@PathVariable id: Long): ResponseEntity<JobHistory> {
        log.debug("REST request to get JobHistory : {}", id)
        val jobHistory = jobHistoryService.findOne(id)
        return ResponseUtil.wrapOrNotFound(jobHistory)
    }
    /**
     *  `DELETE  /job-histories/:id` : delete the "id" jobHistory.
     *
     * @param id the id of the jobHistory to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/job-histories/{id}")
    fun deleteJobHistory(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete JobHistory : {}", id)
        jobHistoryService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
