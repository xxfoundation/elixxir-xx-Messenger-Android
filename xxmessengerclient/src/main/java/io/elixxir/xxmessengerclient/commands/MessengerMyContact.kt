package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.FactType
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerMyContact(private val env: MessengerEnvironment) {

    operator fun invoke(includeFacts: IncludedFacts = IncludedFacts.All): Contact {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")
        val contact = e2e.contact

        return when (includeFacts) {
            is IncludedFacts.All -> contact.setFactsOnContact(ud.facts)
            is IncludedFacts.Types -> contact.setFactsOnContact(
                ud.facts.filter {
                    includeFacts.factsSet.contains(FactType.from(it.type))
                }
            )
        }
    }
}

sealed class IncludedFacts {
    object All : IncludedFacts()
    class Types(val factsSet: Set<FactType>) : IncludedFacts()
}