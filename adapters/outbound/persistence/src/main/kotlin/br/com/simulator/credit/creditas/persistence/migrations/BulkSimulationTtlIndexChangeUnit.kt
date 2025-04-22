package br.com.simulator.credit.creditas.persistence.migrations

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptions
import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.bson.Document

@ChangeUnit(
  id = "add-ttl-index-bulk-simulation",
  order = "001",
  author = "gabs",
)
class BulkSimulationTtlIndexChangeUnit {
  @Execution
  fun createTtlIndex(database: MongoDatabase) {
    val indexSpec = Document("lockedAt", 1)
    val options =
      IndexOptions()
        .expireAfter(60, java.util.concurrent.TimeUnit.SECONDS)
        .name("ttl_lockedAt_index")

    val collectionName = "bulkSimulationDocument"
    val indexes = database.getCollection(collectionName).listIndexes()

    val alreadyExists = indexes.any { it.getString("name") == "ttl_lockedAt_index" }
    if (!alreadyExists) {
      database.getCollection(collectionName).createIndex(indexSpec, options as IndexOptions)
    }
  }

  @RollbackExecution
  fun rollback(database: MongoDatabase) {
    database.getCollection("bulkSimulationDocument").dropIndex("ttl_lockedAt_index")
  }
}
