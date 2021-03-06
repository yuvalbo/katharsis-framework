package io.katharsis.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import io.katharsis.resource.annotations.JsonApiId;
import io.katharsis.resource.annotations.JsonApiResource;

@Entity
@JsonApiResource(type = "singleTableBase")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SingleTableBaseEntity {

	public static final String ATTR_id = "id";

	public static final String ATTR_stringValue = "stringValue";

	@Id
	@JsonApiId
	private Long id;

	@Column
	private String stringValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

}