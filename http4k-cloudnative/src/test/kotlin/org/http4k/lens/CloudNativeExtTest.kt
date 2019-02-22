package org.http4k.lens

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.http4k.cloudnative.env.*
import org.http4k.lens.BiDiLensContract.checkContract
import org.http4k.lens.BiDiLensContract.spec
import org.junit.jupiter.api.Test
import java.time.Duration

class CloudNativeExtTest {
    @Test
    fun port() = checkContract(spec.port(), Port(123), "123", "", "invalid", "o", "o123", "o123123")

    @Test
    fun host() = checkContract(spec.host(), Host("localhost.com"), "localhost.com", "", null, "o", "olocalhost.com", "olocalhost.comlocalhost.com")

    @Test
    fun authority() = checkContract(spec.authority(), Authority(Host.localhost, Port(80)), "localhost:80", "", null, "o", "olocalhost:80", "olocalhost:80localhost:80")

    @Test
    fun timeout() = checkContract(spec.timeout(), Timeout(Duration.ofSeconds(35)), "PT35S", "", "invalid", "o", "oPT35S", "oPT35SPT35S")

    @Test
    fun secret() {
        val requiredLens = spec.secret().required("hello")
        assertThat(requiredLens("123"), equalTo(Secret("123".toByteArray())))
        assertThat({ requiredLens("") }, throws(lensFailureWith<String>(Missing(requiredLens.meta), overallType = Failure.Type.Missing)))
    }
}