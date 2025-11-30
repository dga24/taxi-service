package org.dga.taxiservice.infrastructure.perssistence

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class OutBoxRepositoryConfig {

    companion object {
        const val FIND_UNPUBLISHED_PROD = """
            SELECT * FROM outbox_events
            WHERE published = false
            ORDER BY created_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
        """

        const val FIND_UNPUBLISHED_TEST = """
            SELECT * FROM outbox_events
            WHERE published = false
            ORDER BY created_at ASC
            LIMIT 100
        """

        fun getFindUnpublishedQuery(profile: String): String {
            return when (profile) {
                "prod" -> FIND_UNPUBLISHED_PROD
                else -> FIND_UNPUBLISHED_TEST
            }
        }
    }
}
