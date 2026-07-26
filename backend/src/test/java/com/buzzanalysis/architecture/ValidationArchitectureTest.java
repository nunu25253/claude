package com.buzzanalysis.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * {@code @RequestBody}で受け取るリクエストDTOに宣言的バリデーション({@code @NotBlank}等)を
 * 付与していても、コントローラのパラメータ自体に{@code @Valid}が無ければ一切発火しない。
 * Phase8〜17で追加されたAIコスト系エンドポイント群でこの付与漏れが10箇所発生していたため、
 * 再発防止のガードレールとして「{@code @RequestBody}パラメータには必ず{@code @Valid}を伴う」
 * ことを機械的に検証する。
 */
class ValidationArchitectureTest {

    @Test
    void allRequestBodyParametersMustBeAnnotatedWithValid() {
        JavaClasses importedClasses = new ClassFileImporter()
                .importPackages("com.buzzanalysis.presentation.controller");

        ArchRule rule = methods()
                .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should(haveValidOnEveryRequestBodyParameter());

        rule.check(importedClasses);
    }

    private static ArchCondition<JavaMethod> haveValidOnEveryRequestBodyParameter() {
        return new ArchCondition<JavaMethod>("have @Valid on every @RequestBody parameter") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaParameter parameter : method.getParameters()) {
                    boolean hasRequestBody = parameter.isAnnotatedWith(RequestBody.class);
                    boolean hasValid = parameter.isAnnotatedWith(Valid.class);
                    if (hasRequestBody && !hasValid) {
                        String message = String.format(
                                "%s has a @RequestBody parameter (index %d) without @Valid",
                                method.getFullName(), parameter.getIndex());
                        events.add(SimpleConditionEvent.violated(method, message));
                    }
                }
            }
        };
    }
}
