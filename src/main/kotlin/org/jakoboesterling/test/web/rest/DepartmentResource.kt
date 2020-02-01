package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import javax.validation.Valid
import org.jakoboesterling.test.domain.Department
import org.jakoboesterling.test.service.DepartmentService
import org.jakoboesterling.test.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ENTITY_NAME = "department"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Department].
 */
@RestController
@RequestMapping("/api")
class DepartmentResource(
    private val departmentService: DepartmentService
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /departments` : Create a new department.
     *
     * @param department the department to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new department, or with status `400 (Bad Request)` if the department has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/departments")
    fun createDepartment(@Valid @RequestBody department: Department): ResponseEntity<Department> {
        log.debug("REST request to save Department : {}", department)
        if (department.id != null) {
            throw BadRequestAlertException(
                "A new department cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = departmentService.save(department)
        return ResponseEntity.created(URI("/api/departments/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /departments` : Updates an existing department.
     *
     * @param department the department to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated department,
     * or with status `400 (Bad Request)` if the department is not valid,
     * or with status `500 (Internal Server Error)` if the department couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/departments")
    fun updateDepartment(@Valid @RequestBody department: Department): ResponseEntity<Department> {
        log.debug("REST request to update Department : {}", department)
        if (department.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = departmentService.save(department)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     department.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /departments` : get all the departments.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of departments in body.
     */
    @GetMapping("/departments")
    fun getAllDepartments(): MutableList<Department> {
        log.debug("REST request to get all Departments")
        return departmentService.findAll()
    }

    /**
     * `GET  /departments/:id` : get the "id" department.
     *
     * @param id the id of the department to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the department, or with status `404 (Not Found)`.
     */
    @GetMapping("/departments/{id}")
    fun getDepartment(@PathVariable id: Long): ResponseEntity<Department> {
        log.debug("REST request to get Department : {}", id)
        val department = departmentService.findOne(id)
        return ResponseUtil.wrapOrNotFound(department)
    }
    /**
     *  `DELETE  /departments/:id` : delete the "id" department.
     *
     * @param id the id of the department to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/departments/{id}")
    fun deleteDepartment(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Department : {}", id)
        departmentService.delete(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
