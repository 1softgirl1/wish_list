package com.example.wish_list

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class ArchitectureRulesKonsistTest {

    @Test
    fun `domain does not depend on Android framework`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("..domain..")
            .assertFalse {
                it.hasImport { importDeclaration ->
                    val importName = importDeclaration.name
                    importName.startsWith("android.") || importName.startsWith("androidx.")
                }
            }
    }

    @Test
    fun `data does not depend on ui components`() {
        Konsist
            .scopeFromProject()
            .files
            .withPackage("..data..")
            .assertFalse {
                it.hasImport { importDeclaration ->
                    val importName = importDeclaration.name
                    importName.contains(".ui.")
                }
            }
    }

    @Test
    fun `feature modules do not depend directly on each other`() {
        Konsist
            .scopeFromModule("feature/wishlist")
            .files
            .assertFalse {
                it.hasImport { importDeclaration ->
                    val importName = importDeclaration.name
                    importName.startsWith("com.example.wish_list.feature.publicwishlist") ||
                        importName.startsWith("com.example.wish_list.feature.reservation")
                }
            }

        Konsist
            .scopeFromModule("feature/public-wishlist")
            .files
            .assertFalse {
                it.hasImport { importDeclaration ->
                    importDeclaration.name.startsWith("com.example.wish_list.feature.wishlist")
                }
            }

        Konsist
            .scopeFromModule("feature/reservation")
            .files
            .assertFalse {
                it.hasImport { importDeclaration ->
                    importDeclaration.name.startsWith("com.example.wish_list.feature.wishlist")
                }
            }
    }

    @Test
    fun `use cases are located in domain layer`() {
        Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.resideInPackage("..domain.usecase..")
            }
    }

    @Test
    fun `repositories are interfaces in domain and implementations in data`() {
        val domainRepositories = Konsist
            .scopeFromProject()
            .interfaces()
            .withNameEndingWith("Repository")
        domainRepositories.assertTrue { it.resideInPackage("..domain.repository..") }

        val dataRepositories = Konsist
            .scopeFromProject()
            .classes()
            .withNameEndingWith("Repository")
        dataRepositories.assertTrue { it.resideInPackage("..data.repository..") }

        assertEquals(
            "Каждому domain Repository интерфейсу должна соответствовать реализация в data",
            domainRepositories.size,
            dataRepositories.size
        )
    }
}
