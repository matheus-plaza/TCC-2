package br.edu.tcc.agromotor;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Verifica a Regra de Dependência da Clean Architecture em tempo de build (RNF01):
 * as dependências do código apontam apenas para o centro.
 */
@AnalyzeClasses(packages = "br.edu.tcc.agromotor", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule camadas = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Dominio").definedBy("..domain..")
            .layer("Aplicacao").definedBy("..application..")
            .layer("Apresentacao").definedBy("..presentation..")
            .layer("Infraestrutura").definedBy("..infrastructure..")
            .whereLayer("Apresentacao").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infraestrutura").mayNotBeAccessedByAnyLayer()
            .whereLayer("Aplicacao").mayOnlyBeAccessedByLayers("Apresentacao", "Infraestrutura")
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Aplicacao", "Apresentacao", "Infraestrutura");

    @ArchTest
    static final ArchRule dominioSemFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml..")
            .because("as regras agronômicas não devem conhecer Spring, JPA nem HTTP");

    @ArchTest
    static final ArchRule aplicacaoSemSpringNemJpa = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..")
            .because("os casos de uso são registrados pela infraestrutura, sem anotações de framework");

    /** Regra de ouro 1 da especificação: zero tipos flutuantes no motor. */
    @ArchTest
    static final ArchRule semFlutuantesNoMotor = noFields()
            .that().areDeclaredInClassesThat().resideInAPackage("..domain..")
            .should().haveRawType(double.class)
            .orShould().haveRawType(float.class)
            .orShould().haveRawType(Double.class)
            .orShould().haveRawType(Float.class);

    @ArchTest
    static final ArchRule motorSemConversaoFlutuante = noClasses()
            .that().resideInAPackage("..domain..")
            .should().callMethodWhere(DescribedPredicate.describe(
                    "que retorne ou receba double/float",
                    chamada -> usaFlutuante(chamada.getTarget().getRawReturnType())
                            || chamada.getTarget().getRawParameterTypes().stream().anyMatch(ArquiteturaTest::usaFlutuante)));

    private static boolean usaFlutuante(JavaClass tipo) {
        return tipo.isEquivalentTo(double.class) || tipo.isEquivalentTo(float.class);
    }
}
