package org.jakoboesterling.test.domain

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Department.
 */
@Entity
@Table(name = "department")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Department(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @get: NotNull
    @Column(name = "department_name", nullable = false)
    var departmentName: String? = null,

    @OneToOne @JoinColumn(unique = true)
    var location: Location? = null,

    /**
     * A relationship
     */
    @ApiModelProperty(value = "A relationship")
    @OneToMany(mappedBy = "department")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var employees: MutableSet<Employee> = mutableSetOf()

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {

    fun addEmployee(employee: Employee): Department {
        this.employees.add(employee)
        employee.department = this
        return this
    }

    fun removeEmployee(employee: Employee): Department {
        this.employees.remove(employee)
        employee.department = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Department) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Department{" +
        "id=$id" +
        ", departmentName='$departmentName'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
