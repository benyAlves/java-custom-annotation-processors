package mz.co.bmaluleque.processor;

import com.google.auto.service.AutoService;
import mz.co.bmaluleque.annotation.FieldHidingNotAllowed;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("mz.co.bmaluleque.annotation.FieldHidingNotAllowed")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class FieldHidingProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(FieldHidingNotAllowed.class)) {
            if (annotatedElement.getKind() == ElementKind.FIELD && annotatedElement instanceof VariableElement) {
                VariableElement fieldElement = (VariableElement) annotatedElement;
                String fieldName = fieldElement.getSimpleName().toString();
                TypeElement enclosingClass = (TypeElement) fieldElement.getEnclosingElement();

                List<? extends TypeMirror> subclasses = processingEnv.getTypeUtils().directSupertypes(enclosingClass.asType());
                for (TypeMirror subclass : subclasses) {
                    Element subclassElement = processingEnv.getTypeUtils().asElement(subclass);
                    List<? extends Element> enclosedElements = subclassElement.getEnclosedElements();
                    for (Element enclosedElement : enclosedElements) {
                        if (enclosedElement.getKind() == ElementKind.FIELD && enclosedElement instanceof VariableElement) {
                            VariableElement subclassFieldElement = (VariableElement) enclosedElement;
                            String subclassFieldName = subclassFieldElement.getSimpleName().toString();
                            if (subclassFieldName.equals(fieldName)) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "Field hiding violation: " + fieldName, subclassFieldElement);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }


}
