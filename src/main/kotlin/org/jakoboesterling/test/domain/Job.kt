package org.jakoboesterling.test.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Job.
 */
@Entity
@Table(name = "job")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class Job(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,

    @Column(name = "job_title")
    var jobTitle: String? = null,

    @Column(name = "min_salary")
    var minSalary: Long? = null,

    @Column(name = "max_salary")
    var maxSalary: Long? = null,

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "job_task",
        joinColumns = [JoinColumn(name = "job_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "task_id", referencedColumnName = "id")])
    var tasks: MutableSet<Task> = mutableSetOf(),

    @ManyToOne @JsonIgnoreProperties("jobs")
    var employee: Employee? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
) : Serializable {

    fun addTask(task: Task): Job {
        this.tasks.add(task)
        return this
    }

    fun removeTask(task: Task): Job {
        this.tasks.remove(task)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Job) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Job{" +
        "id=$id" +
        ", jobTitle='$jobTitle'" +
        ", minSalary=$minSalary" +
        ", maxSalary=$maxSalary" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
