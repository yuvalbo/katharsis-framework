package io.katharsis.core.internal.dispatcher.path;

/**
 * Represents a part of a path which relate a field of a resource e.g. for /resource/1/field the first element will be
 * an object of ResourcePath type and the second will be of FieldPath type.
 *
 * FieldPath can refer only to relationship fields.
 */
public class FieldPath extends JsonPath {

    public FieldPath(String elementName) {
        super(elementName);
    }

    @Override
    public boolean isCollection() {
        return parentResource.ids == null || parentResource.ids.getIds().size() > 1;
    }

    @Override
    public String getResourceName() {
        return parentResource.elementName;
    }

    public PathIds getIds() {
        return parentResource.ids;
    }

    public void setIds(PathIds ids) {
        throw new UnsupportedOperationException("Ids can be assigned only to ResourcePath");
    }
}
