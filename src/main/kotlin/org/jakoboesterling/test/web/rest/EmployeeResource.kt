package org.jakoboesterling.test.web.rest

import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.PaginationUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.jakoboesterling.test.domain.Employee
import org.jakoboesterling.test.repository.EmployeeRepository
import org.jakoboesterling.test.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val ENTITY_NAME = "employee"
/**
 * REST controller for managing [org.jakoboesterling.test.domain.Employee].
 */
@RestController
@RequestMapping("/api")
@Transactional
class EmployeeResource(
    private val employeeRepository: EmployeeRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /employees` : Create a new employee.
     *
     * @param employee the employee to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new employee, or with status `400 (Bad Request)` if the employee has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/employees")
    fun createEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        log.debug("REST request to save Employee : {}", employee)
        if (employee.id != null) {
            throw BadRequestAlertException(
                "A new employee cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = employeeRepository.save(employee)
        return ResponseEntity.created(URI("/api/employees/" + result.id))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /employees` : Updates an existing employee.
     *
     * @param employee the employee to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated employee,
     * or with status `400 (Bad Request)` if the employee is not valid,
     * or with status `500 (Internal Server Error)` if the employee couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/employees")
    fun updateEmployee(@RequestBody employee: Employee): ResponseEntity<Employee> {
        log.debug("REST request to update Employee : {}", employee)
        if (employee.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = employeeRepository.save(employee)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, false, ENTITY_NAME,
                     employee.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /employees` : get all the employees.
     *

     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of employees in body.
     */
    @GetMapping("/employees")
    fun getAllEmployees(
        pageable: Pageable
    ): ResponseEntity<MutableList<Employee>> {
        log.debug("REST request to get a page of Employees")
        val page = employeeRepository.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /employees/:id` : get the "id" employee.
     *
     * @param id the id of the employee to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the employee, or with status `404 (Not Found)`.
     */
    @GetMapping("/employees/{id}")
    fun getEmployee(@PathVariable id: Long): ResponseEntity<Employee> {
        log.debug("REST request to get Employee : {}", id)
        val employee = employeeRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(employee)
    }
    /**
     *  `DELETE  /employees/:id` : delete the "id" employee.
     *
     * @param id the id of the employee to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/employees/{id}")
    fun deleteEmployee(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Employee : {}", id)

        employeeRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build()
    }
}
