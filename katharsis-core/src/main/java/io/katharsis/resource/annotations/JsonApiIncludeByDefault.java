package io.katharsis.resource.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.katharsis.errorhandling.exception.ResourceException;

/**
 * Indicates additional resources that should be included by default with every primary resource.
 * The field can be added to every relation defined by {@link JsonApiToOne} or {@link JsonApiToMany}. Otherwise,
 * {@link ResourceException} will be thrown at the initialization phrase.
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonApiIncludeByDefault {
}
