package example

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import java.util.*
import javax.transaction.Transactional

@MicronautTest(transactional = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TxTest3 : AbstractTest(false) {
    @Inject
    lateinit var recordCoroutineRepository: RecordCoroutineRepository

    @Inject
    lateinit var recordTransactionalService: RecordTransactionalService

    @Test
    fun `coroutines returning flow inside transaction`(): Unit = runBlocking {
        val records = (1..2).map { Record(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()) }

        recordTransactionalService.saveAll(records).collect { }

        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[0].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[0].bar))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByFoo(records[1].foo))
        Assertions.assertEquals(true, recordCoroutineRepository.existsByBar(records[1].bar))
    }

}


@Transactional(Transactional.TxType.MANDATORY)
@R2dbcRepository(dialect = Dialect.POSTGRES)
interface RecordTransactionalCoroutineRepository : CoroutineCrudRepository<Record, UUID>

@Singleton
@Transactional
open class RecordTransactionalService(private val repository: RecordTransactionalCoroutineRepository) {
    open fun saveAll(records: Iterable<Record>): Flow<Record> = repository.saveAll(records)
}
