package example

import io.micronaut.data.annotation.Relation
import io.micronaut.serde.annotation.Serdeable
import java.util.ArrayList

@Serdeable
data class Child (
    var firstName: String,
    var gender: String,
    var grade: Int,
    @Relation(value = Relation.Kind.ONE_TO_MANY)
    var pets: List<Pet> = ArrayList()
)
