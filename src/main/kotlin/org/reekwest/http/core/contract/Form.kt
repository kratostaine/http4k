package org.reekwest.http.core.contract

import org.reekwest.http.core.ContentType.Companion.APPLICATION_FORM_URLENCODED
import org.reekwest.http.core.HttpMessage
import org.reekwest.http.core.contract.ContractBreach.Companion.Invalid
import org.reekwest.http.core.contract.Header.Common.CONTENT_TYPE
import java.net.URLDecoder.decode
import java.nio.ByteBuffer

typealias FormFields = Map<String, List<String>>

private object FormLocator : Locator<HttpMessage, ByteBuffer> {
    override val name = "form"
    override fun get(target: HttpMessage, name: String): List<ByteBuffer> {
        if (CONTENT_TYPE(target) != APPLICATION_FORM_URLENCODED) throw Invalid(CONTENT_TYPE)
        return target.body?.let { listOf(it) } ?: emptyList()
    }

    override fun set(target: HttpMessage, name: String, values: List<ByteBuffer>) = values
        .fold(target, { memo, next -> memo.with(Body.binary() to next) })
        .with(CONTENT_TYPE to APPLICATION_FORM_URLENCODED)
}

private val formSpec: BodySpec<FormFields> = BodySpec(LensSpec(FormLocator,
    {
        String(it.array())
            .split("&")
            .filter { it.contains("=") }
            .map { it.split("=") }
            .map { decode(it[0], "UTF-8") to if (it.size > 1) decode(it[1], "UTF-8") else "" }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }
    },
    { it.toString().toByteBuffer() }))

fun Body.form() = formSpec.required("form")

fun Body.aForm(vararg fields: FormField) =
    formSpec.map({ fields ->
        // do some validation here...
        AForm(fields)
    }, { it.fields })

data class AForm constructor(val fields: Map<String, List<String>>) {
    operator fun plus(kv: Pair<String, String>): AForm =
        copy(fields.plus(kv.first to fields.getOrDefault(kv.first, emptyList()).plus(kv.second)))

    fun with(vararg modifiers: (AForm) -> AForm): AForm = modifiers.fold(this, { memo, next -> next(memo) })
}

